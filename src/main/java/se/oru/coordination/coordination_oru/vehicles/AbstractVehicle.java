package se.oru.coordination.coordination_oru.vehicles;

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.lang.ArrayUtils;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.util.Arrays;

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
    private final int priorityID;
    private final String type = this.getClass().getSimpleName();
    private double maxVelocity;
    private double maxAcceleration;
    private int trackingPeriod;
    private double length;
    private double width;
    private Coordinate[] footprint;
    private Color color;
    private Pose initialPose;
    private Pose[] goalPoses;
    private double safetyDistance;
    private PoseSteering[] path;
    private double pathLength;
    private Mission mission;
    private int missionRepetition;
    public static ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;
//    private Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> planSegmentsMap;
    public AbstractVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                           int trackingPeriod, double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance, int missionRepetition) {
        this.ID = ID;
        this.name = name;
        this.priorityID = priorityID;
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

        // Adjusted to handle both a single Pose and an array of Poses
//        if (goalPoses instanceof Pose) {
//            this.goalPoses = new Pose[]{(Pose) goalPoses};
//        } else if (goalPoses instanceof Pose[]) {
//            this.goalPoses = (Pose[]) goalPoses;
//        } else {
//            throw  new IllegalArgumentException("Invalid type for goal poses");
//        }

        AbstractVehicle existingVehicle = VehiclesHashMap.getVehicle(ID);
        if (existingVehicle != null) {
            throw new IllegalStateException("ID " + ID + " already exists.");
        }

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

    /**
     * Generates a path for the vehicle using the default planning algorithm and parameters.
     *
     * @param initial     The initial pose of the vehicle.
     * @param goalPoses   An array of goal poses.
     * @param map         The map used for planning.
     * @param inversePath A flag indicating whether the inverse path should also be computed.
     */
    public void getPlan(Pose initial, Pose[] goalPoses, String map, Boolean inversePath) {
        getPlan(initial, goalPoses, map, inversePath, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect, 0.01,
                60, 0.01, 0.1);
    }

    /**
     * Generates a path for the vehicle using the specified planning algorithm and parameters.
     *
     * @param initialPose               The initialPose pose of the vehicle.
     * @param goalPoses                 An array of goal poses.
     * @param map                       The map used for planning.
     * @param inversePath               A flag indicating whether the inverse path should also be computed.
     * @param planningAlgorithm         The planning algorithm to be used.
     * @param radius                    The radius used for planning.
     * @param planningTime              The maximum planning time in seconds.
     * @param turningRadius             The turning radius of the vehicle.
     * @param distanceBetweenPathPoints The distance between path points in the generated path.
     */
    public void getPlan(Pose initialPose, Pose[] goalPoses, String map, Boolean inversePath, ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm,
                        double radius, double planningTime, double turningRadius, double distanceBetweenPathPoints) {

        var rsp = configureReedsSheppCarPlanner(planningAlgorithm, map, radius, planningTime, turningRadius,
                distanceBetweenPathPoints);
        generatePath(rsp, initialPose, goalPoses, inversePath);
    }

    /**
     * Configures a ReedsSheppCarPlanner instance with the specified parameters.
     *
     * @param planningAlgorithm         The planning algorithm to be used.
     * @param map                       The map used for planning.
     * @param radius                    The radius used for planning.
     * @param planningTime              The maximum planning time in seconds.
     * @param turningRadius             The turning radius of the vehicle.
     * @param distanceBetweenPathPoints The distance between path points in the generated path.
     * @return A configured ReedsSheppCarPlanner instance.
     */
    private ReedsSheppCarPlanner configureReedsSheppCarPlanner(ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm, String map, double radius,
                                                               double planningTime, double turningRadius, double distanceBetweenPathPoints) {
        var rsp = new ReedsSheppCarPlanner(planningAlgorithm);
        rsp.setMap(map);
        rsp.setRadius(radius);
        rsp.setPlanningTimeInSecs(planningTime);
        rsp.setFootprint(getFootPrint());
        rsp.setTurningRadius(turningRadius);
        rsp.setDistanceBetweenPathPoints(distanceBetweenPathPoints);
        return rsp;
    }

    /**
     * Generates a path for the vehicle using the provided ReedsSheppCarPlanner instance.
     * If no path is found, the program prints an error message and exits.
     *
     * @param rsp         The ReedsSheppCarPlanner instance used for planning.
     * @param initialPose The initialPose pose of the vehicle.
     * @param goalPoses   Varargs parameter that can take one or more goal poses.
     * @param inversePath A flag indicating whether the inverse path should also be computed.
     *                    This parameter is optional and defaults to false.
     */
    private void generatePath(ReedsSheppCarPlanner rsp, Pose initialPose, Pose[] goalPoses, boolean inversePath) {
        rsp.setStart(initialPose);
        rsp.setGoals(goalPoses);
        rsp.plan();
        var path = rsp.getPath();

        if (path == null) {
            System.err.println("No path found.");
            System.exit(1); // Exit the program
        }

        if (inversePath) {
            ArrayUtils.addAll(path, rsp.getPathInv());
        }
        this.setPath(path);
    }

    @Override
    public String toString() {
        return "AbstractVehicle{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", priorityID=" + priorityID +
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPriorityID() {
        return priorityID;
    }

    public double getPathLength() {
        return pathLength;
    }

    public void setPathLength(double pathLength) {
        this.pathLength = pathLength;
    }

    public LookAheadVehicle convertToLookAheadVehicle(AutonomousVehicle autonomousVehicle) {
        VehiclesHashMap.removeVehicle(autonomousVehicle.getID());
        return new LookAheadVehicle(
                autonomousVehicle.getID(),
                autonomousVehicle.getName(),
                20,
                autonomousVehicle.getPriorityID(),
                (Color) autonomousVehicle.getColor("color"),
                autonomousVehicle.getMaxVelocity(),
                autonomousVehicle.getMaxAcceleration(),
                autonomousVehicle.getTrackingPeriod(),
                autonomousVehicle.getLength(),
                autonomousVehicle.getWidth(),
                autonomousVehicle.getInitialPose(),
                autonomousVehicle.getGoalPoses(),
                autonomousVehicle.getSafetyDistance(),
                autonomousVehicle.getMissionRepetition()
        );
    }

    public AutonomousVehicle convertToAutonomousVehicle(LookAheadVehicle lookAheadVehicle) {
        VehiclesHashMap.removeVehicle(lookAheadVehicle.getID());
        return new AutonomousVehicle(
                lookAheadVehicle.getID(),
                lookAheadVehicle.getName(),
                lookAheadVehicle.getPriorityID(),
                (Color) lookAheadVehicle.getColor("color"),
                lookAheadVehicle.getMaxVelocity(),
                lookAheadVehicle.getMaxAcceleration(),
                lookAheadVehicle.getTrackingPeriod(),
                lookAheadVehicle.getLength(),
                lookAheadVehicle.getWidth(),
                lookAheadVehicle.getInitialPose(),
                lookAheadVehicle.getGoalPoses(),
                lookAheadVehicle.getSafetyDistance(),
                lookAheadVehicle.getMissionRepetition()
        );
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setMissionRepetition(int missionRepetition) {
        this.missionRepetition = missionRepetition;
    }

    public int getMissionRepetition() {
        return missionRepetition;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }
}
