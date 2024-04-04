package se.oru.coordination.coordination_oru.vehicles;

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.lang.ArrayUtils;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.DataStructure.Task;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private int ID;
    private String name;
    private final int priority;
    private final String type = this.getClass().getSimpleName();
    private double maxVelocity;
    private double maxAcceleration;
    private int trackingPeriod; //FIXME: This should be moved to the tracker class
    private double length;
    private double width;
    private Coordinate[] footprint;
    private Color color;
    private Pose initialPose;
    private Pose[] goalPoses; //FIXME:should be removed later
    private List<Task> tasks = new ArrayList<>();
    private Map<Pose, Double> missions;
    private int missionRepetition;
    private double safetyDistance;
    private int safetyPathPoints;
    private PoseSteering[] path; //FIXME:should be removed later
    private List<PoseSteering[]> paths = new ArrayList<>();

    private double pathLength;
    private ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm;

    //TODO: Add mission support to the vehicle class List<GoalPose, time>
    //FIXME: Move planning methods to a separate class. This class should only contain vehicle properties and methods
//    private Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> planSegmentsMap;
    public AbstractVehicle(int ID, String name, int priority, Color color, double maxVelocity, double maxAcceleration,
                           int trackingPeriod, double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance, int missionRepetition) {
        this.ID = ID;
        this.name = name;
        this.priority = priority;
        this.color = color;
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.trackingPeriod = trackingPeriod;
        this.length = length;
        this.width = width;
        this.initialPose = initialPose;
        this.goalPoses = goalPoses;
        this.safetyDistance = safetyDistance;
        this.missionRepetition = missionRepetition;
        this.footprint = makeFootprint(length, width);
        this.planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;

        var existingVehicle = VehiclesHashMap.getVehicle(ID);
        if (existingVehicle != null) throw new IllegalStateException("ID " + ID + " already exists.");

        VehiclesHashMap.getList().put(this.ID, this);
        vehicleNumber++;
    }
    public static Coordinate[] makeFootprint(double length, double width) {
        return new Coordinate[]{               // FIXME Currently allows four sided vehicles only
                new Coordinate(-length, width),        //back left
                new Coordinate(length, width),         //back right
                new Coordinate(length, -width),        //front right
                new Coordinate(-length, -width)        //front left
        };
    }

    public static double calculateFootprintArea(double length, double width) {
        return length * width;                  // FIXME Currently allows four sided vehicles only
    }

    public void getPlans(String map, Boolean inversePath) {
        if (initialPose != null && tasks != null)
            for (Task task : tasks) {
                getPlan(initialPose, task.getPoses(), map, inversePath, 0.09, 60, 2.0, 0.1);
                initialPose = task.getPoses()[task.getPoses().length-1];
            }
    }

    public void getPlan(AbstractVehicle vehicle, String map, Boolean inversePath) {
        if (vehicle.initialPose != null && vehicle.goalPoses != null)
            getPlan(vehicle.initialPose, vehicle.goalPoses, map, inversePath, 0.09, 60, 2.0, 0.1);
    }

    public void getPlan(Pose initialPose, Pose[] goalPoses, String map, Boolean inversePath,
                        double radius, double planningTime, double turningRadius, double distanceBetweenPathPoints) {
        var rsp = configureReedsSheppCarPlanner(map, radius, planningTime, turningRadius, distanceBetweenPathPoints);
        generatePath(rsp, initialPose, goalPoses, inversePath);
    }

    private ReedsSheppCarPlanner configureReedsSheppCarPlanner(String map, double radius, double planningTime,
                                                               double turningRadius, double distanceBetweenPathPoints) {
        var rsp = new ReedsSheppCarPlanner(planningAlgorithm);
        rsp.setMap(map);
        rsp.setRadius(radius);
        rsp.setPlanningTimeInSecs(planningTime);
        rsp.setFootprint(getFootPrint());
        rsp.setTurningRadius(turningRadius);
        rsp.setDistanceBetweenPathPoints(distanceBetweenPathPoints);
        return rsp;
    }

    private void generatePath(ReedsSheppCarPlanner rsp, Pose initialPose, Pose[] goalPoses, boolean inversePath) {
        rsp.setStart(initialPose);
        rsp.setGoals(goalPoses);
        rsp.plan();
        var path = rsp.getPath();

        if (path == null) {
            System.err.println("No path found.");
            System.exit(1); //TODO: Handle this better and throw an exception, not exiting the program
        }

        if (inversePath) path = (PoseSteering[]) ArrayUtils.addAll(path, rsp.getPathInv());
        this.setPath(path);
        paths.add(path);
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
                ", trackingPeriod=" + trackingPeriod +
                ", length=" + length +
                ", width=" + width +
                ", initialPose=" + initialPose +
                ", goalPoses=" + Arrays.toString(goalPoses) +
                ", safetyDistance=" + safetyDistance +
                ", pathLength=" + pathLength +
                ", footprint=" + Arrays.toString(footprint) +
                '}';
    }

//    public Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> getPlanSegmentsMap() {
//        return planSegmentsMap;
//    }
    public int getID() {
        return ID;
    }

    public Coordinate[] getFootprint() {
        return footprint;
    } //TODO Each vehicle should be able to have a separate footprint in tec not the default one

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

    public Coordinate[] getFootPrint() {
        return footprint;
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

    public void setPath(PoseSteering[] path) {
        this.path = path;
        setPlanLength(path);
        setSafetyPathPoints();
    }

    public void setLength(double length) {
        this.length = length;
        this.footprint = makeFootprint(length, width);
    }

    public double getLength() {
        return length;
    }

    public void setWidth(double width) {
        this.width = width;
        this.footprint = makeFootprint(length, width);
    }

    public double getWidth() {
        return width;
    }

    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxAcceleration(double maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    public double getMaxAcceleration() {
        return maxAcceleration;
    }

    //TODO: This needs to go from here. Maybe Tracker?
    public void setTrackingPeriod(int trackingPeriod) {
        this.trackingPeriod = trackingPeriod;
    }

    public Integer getTrackingPeriod() {
        return trackingPeriod;
    }

    public String getType() {
        return type;
    }

    public double getSafetyDistance() {
        return safetyDistance;
    }

    public void setSafetyDistance(double safetyDistance) {
        this.safetyDistance = safetyDistance;
    }

    public Pose getInitialPose() {
        return initialPose;
    }

    public void setInitialPose(Pose initialPose) {
        this.initialPose = initialPose;
    }

    public Pose[] getGoalPoses() {
        return goalPoses;
    }

    public void setGoalPoses(Pose[] goalPoses) {
        this.goalPoses = goalPoses;
    }

    public void setGoalPoses(Pose goalPose) {
        this.goalPoses = new Pose[] {goalPose};
    }

    public void setName(String name) {
        this.name = name;
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

    public void setPathLength(double pathLength) {
        this.pathLength = pathLength;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setMissions(Map<Pose, Double> missions) {
        this.missions = missions;
    }

    public Map<Pose, Double> getMissions() {
        return missions;
    }
    public void addMission(Map<Pose, Double> mission) {
        this.missions.put(mission.keySet().iterator().next(), mission.values().iterator().next());
    }

    public void setMissionRepetition(int missionRepetition) {
        this.missionRepetition = missionRepetition;
    }

    public int getMissionRepetition() {
        return missionRepetition;
    }

    public ReedsSheppCarPlanner.PLANNING_ALGORITHM getPlanningAlgorithm() {
        return planningAlgorithm;
    }

    public void setPlanningAlgorithm(ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm) {
        this.planningAlgorithm = planningAlgorithm;
    }

    public int getSafetyPathPoints() {
        return safetyPathPoints;
    }

    public void setSafetyPathPoints() {
        if (this.path != null) this.safetyPathPoints = (int) Math.round(path.length / pathLength * safetyDistance);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addGoal(Pose goalPose) {
    this.tasks.add(new Task(new Pose[] {goalPose}, 0.0));
    }
    public void addGoals(Pose[] goalPoses) {
        this.tasks.add(new Task(goalPoses, 0.0));
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public List<PoseSteering[]> getPaths() {
        return paths;
    }

    public void setPaths(List<PoseSteering[]> paths) {
        this.paths = paths;
    }
}
