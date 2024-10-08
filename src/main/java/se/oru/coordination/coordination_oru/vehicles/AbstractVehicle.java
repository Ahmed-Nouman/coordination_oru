package se.oru.coordination.coordination_oru.vehicles;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.PathPlanner;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * The AbstractVehicle class is an abstract base class for representing different types of vehicles.
 * It provides common properties and functionalities that are applicable across various vehicle types.
 * Specific types of vehicles should extend this class to tailor implementations to their needs.
 * <p>
 * Key features of this class include:
 * - Unique identification and prioritization of vehicles.
 * - Configuration of vehicle dynamics like maximum velocity and acceleration.
 * - Geometric properties such as vehicle's footprint, length, and width.
 * - Visual representation through color.
 * - Setting initial and goal poses for navigation.
 * - Incorporation of safety measures like maintaining a safe distance.
 * - Ability to generate and update the vehicle's path.
 * <p>
 * This class also lays the groundwork for implementing planning and movement algorithms in subclasses,
 * allowing for customization of specific vehicle behaviors.
 *
 * @author anm
 */

public abstract class AbstractVehicle {
    public static int vehicleNumber = 1;
    private final int ID;
    private final String name;
    private final int priority;
    private final String type = this.getClass().getSimpleName();
    private double maxVelocity;
    private final double maxAcceleration;
    private double length;
    private double width;
    private Coordinate[] footprint;
    private Color color;
    private Pose initialPose;
    private final List<Task> tasks = new ArrayList<>();
    private final int goalRepetition;
    private final double safetyDistance;
    private int safetyPathPoints;
    private PoseSteering[] path; //FIXME:should be removed later
    public List<PoseSteering[]> paths = new ArrayList<>();
    private double pathLength;
    private int currentTaskIndex = -1;
    private final ForwardModel forwardModel;

    private boolean isStopped = false;

    public AbstractVehicle(int ID, String name, int priority, Color color, double maxVelocity, double maxAcceleration,
                           double length, double width, Pose initialPose, double safetyDistance, int goalRepetition, ForwardModel model) {
        this.ID = ID;
        this.name = name;
        this.priority = priority;
        this.color = color;
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.length = length;
        this.width = width;
        this.initialPose = initialPose;
        this.safetyDistance = safetyDistance;
        this.goalRepetition = goalRepetition;
        this.footprint = makeFootprint(length, width);
        this.forwardModel = model;

        var existingVehicle = VehiclesHashMap.getVehicle(ID);
        if (existingVehicle != null) throw new IllegalStateException("ID " + ID + " already exists.");

        VehiclesHashMap.getList().put(this.ID, this);
        vehicleNumber++;
    }

    public static Coordinate[] makeFootprint(double length, double width) {
        return new Coordinate[]{
                new Coordinate(-length, width),
                new Coordinate(length, width),
                new Coordinate(length, -width),
                new Coordinate(-length, -width)
        };
    }

    public static double calculateFootprintArea(double length, double width) {
        return length * width;
    }

    public void generatePlans(PathPlanner planner) {
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                var path = planner.plan(footprint, initialPose, task.getPoses());
                if (path != null) paths.add(path);
                else System.err.println("Failed to generate path for task: " + task.getName());
                initialPose = task.getPoses()[task.getPoses().length - 1];
            }
            setSafetyPathPoints();
        }
    }

    public String serializePoseSteering(PoseSteering poseSteering) {
        return String.format("%f %f %f %f", poseSteering.getPose().getX(), poseSteering.getPose().getY(), poseSteering.getPose().getTheta(), poseSteering.getSteering());
    }
    public void savePlans(String folderName) {
        String folderPath = "./paths/" + folderName + "/";

        try {
            Files.createDirectories(Paths.get(folderPath));
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String baseFilename = folderPath + this.getName();
        String filename = baseFilename + ".path";
        File file = new File(filename);

        int fileCounter = 1;
        while (file.exists()) {
            filename = baseFilename + "(" + fileCounter++ + ").path";
            file = new File(filename);
        }

        try (var out = new PrintWriter(filename)) {
            this.getPaths().forEach(path -> {
                Arrays.stream(path).map(this::serializePoseSteering).forEach(out::println);
                out.println("---");
            });
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PoseSteering deserializePoseSteering(String line) {
        String[] parts = line.split(" ");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double theta = Double.parseDouble(parts[2]);
        double steering = Double.parseDouble(parts[3]);
        Pose pose = new Pose(x, y, theta);
        return new PoseSteering(pose, steering);
    }

    public void loadPlans(String filename) {
        List<PoseSteering[]> paths = new ArrayList<>();
        List<PoseSteering> currentPath = new ArrayList<>();

        try (var scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("---")) {
                    paths.add(currentPath.toArray(new PoseSteering[0]));
                    currentPath = new ArrayList<>();
                } else {
                    currentPath.add(deserializePoseSteering(line));
                }
            }
            if (!currentPath.isEmpty()) {
                paths.add(currentPath.toArray(new PoseSteering[0]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.paths = paths;
        setSafetyPathPoints();
    }


    @Override
    public String toString() {
        return "AbstractVehicle{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", priorityID=" + priority +
                ", type='" + type + '\'' +
                ", color=" + getColor("code") +
                ", maxVelocity=" + maxVelocity +
                ", maxAcceleration=" + maxAcceleration +
                ", length=" + length +
                ", width=" + width +
                ", initialPose=" + initialPose +
                ", safetyDistance=" + safetyDistance +
                ", pathLength=" + pathLength +
                ", footprint=" + Arrays.toString(footprint) +
                '}';
    }

    public int getID() {
        return ID;
    }

    public Coordinate[] getFootprint() {
        return footprint;
    }

    public void setColor(Object color) {
        if (color instanceof String) {
            String colorString = (String) color;
            switch (colorString.toLowerCase()) {
                case "black":
                    this.color = Color.BLACK;
                    break;
                case "white":
                    this.color = Color.WHITE;
                    break;
                case "red":
                    this.color = Color.RED;
                    break;
                case "green":
                    this.color = Color.GREEN;
                    break;
                case "blue":
                    this.color = Color.BLUE;
                    break;
                case "cyan":
                    this.color = Color.CYAN;
                    break;
                case "orange":
                    this.color = Color.ORANGE;
                    break;
                case "yellow":
                    this.color = Color.YELLOW;
                    break;
                default:
                    try {
                        this.color = Color.decode(colorString);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid color format: " + colorString);
                    }
                    break;
            }
        } else if (color instanceof Color) {
            this.color = (Color) color;
        } else {
            throw new IllegalArgumentException("Invalid type for color");
        }
    }

    public Object getColor(String input) {
        if (input.equalsIgnoreCase("name")) {
            if (color.equals(Color.BLACK)) {
                return "Black";
            } else if (color.equals(Color.WHITE)) {
                return "White";
            } else if (color.equals(Color.RED)) {
                return "Red";
            } else if (color.equals(Color.GREEN)) {
                return "Green";
            } else if (color.equals(Color.BLUE)) {
                return "Blue";
            } else if (color.equals(Color.CYAN)) {
                return "Cyan";
            } else if (color.equals(Color.ORANGE)) {
                return "Orange";
            } else if (color.equals(Color.YELLOW)) {
                return "Yellow";
            } else {
                throw new IllegalArgumentException("Invalid type for color");
            }
        } else if (input.equals("color")) {
            return color;
        } else if (input.equals("code")) {
            return "#" + String.format("%06x", 0xFFFFFF & color.getRGB());
        }
        return null;
    }

    public void setFootprint(Coordinate[] footprint) {
        this.footprint = footprint;
    }

    public double getPlanLength() {
        return pathLength;
    }

    public void setPlanLength(PoseSteering[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            double deltaS = path[i].getPose().distanceTo(path[i + 1].getPose());
            pathLength += deltaS;
        }
        pathLength = Round.round(pathLength, 2);
        VehiclesHashMap.getVehicle(this.getID()).pathLength = pathLength;
    }

    public PoseSteering[] getPath() {
        return path;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public void setWidth(double width) {
        this.width = width;
    }
    public double getWidth() {
        return width;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public double getMaxAcceleration() {
        return maxAcceleration;
    }

    public String getType() {
        return type;
    }

    public Pose getInitialPose() {
        return initialPose;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public double getPathLength() {
        return pathLength;
    }

    public int getGoalRepetition() {
        return goalRepetition;
    }

    public int getSafetyPathPoints() {
        return safetyPathPoints;
    }

    public void setSafetyPathPoints() {
        for (int i = 0; i < paths.get(0).length - 1; i++) {
            double deltaS = paths.get(0)[i].getPose().distanceTo(paths.get(0)[i + 1].getPose());
            pathLength += deltaS;
        }
        pathLength = Round.round(pathLength, 2);
        safetyPathPoints = (int) Math.round((double) paths.get(0).length / pathLength * safetyDistance);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task, int repeat) {
        for (int i = 0; i < repeat; i++) {
            this.tasks.add(new Task(task.getName(), task.getTimeInMinutes(), task.getPoses(), task.getStoppageTimes(), task.getPriority()));
        }
    }

    public void addTask(Task task) {
        addTask(task, 1);
    }

    public void setGoals(Pose goalPose) {
    this.tasks.add(new Task("", 0.0, new Pose[] {goalPose}, 0));
    }

    public void setGoals(Pose[] goalPoses) {
        this.tasks.add(new Task("", 0.0, goalPoses, 0));
    }

    public List<PoseSteering[]> getPaths() {
        return paths;
    }

    public double getSafetyDistance() {
        return safetyDistance;
    }

    public ForwardModel getForwardModel() {
        return forwardModel;
    }
    public int getCurrentTaskIndex() {
        return currentTaskIndex;
    }

    public void setCurrentTaskIndex(int currentTaskIndex) {
        this.currentTaskIndex = currentTaskIndex;
    }

    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStopped(boolean status) {
        isStopped = status;
    }
}
