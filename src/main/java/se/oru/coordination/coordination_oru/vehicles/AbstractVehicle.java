package se.oru.coordination.coordination_oru.vehicles;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;

import java.awt.*;
import java.util.Arrays;

/**
 * AbstractRobot is an abstract class representing a generic robot with common attributes and methods.
 * It should be extended by concrete robot classes with specific implementations.
 * The class keeps track of the robot's ID, priorityID, color,
 * maxVelocity, maxAcceleration, and footprint of the robot.
 *
 * @author anm
 */
public abstract class AbstractVehicle {
    public static int vehicleNumber = 1;
    private final int ID;
    private final int priorityID;
    private final String type = this.getClass().getSimpleName();
    private double maxVelocity;
    private double maxAcceleration;
    private int trackingPeriod;
    private final double length;
    private final double width;
    private final Coordinate[] footprint;
    private Color color;
    private Pose initialPose;
    private Pose[] goalPoses;
    private double safetyDistance;
    private PoseSteering[] path;
    private double pathLength;
    /**
     * Constructs an AbstractRobot object with the specified parameters.
     * It initializes the robot's ID, priorityID, color, maxVelocity, maxAcceleration, and footprint.
     * If a robot with the same ID already exists, an IllegalStateException is thrown.
     *
     * @param ID              The unique identifier of the robot.
     * @param priorityID      The priority identifier of the robot.
     * @param color           The color of the robot when stationary.
     * @param maxVelocity     The maximum velocity of the robot.
     * @param maxAcceleration The maximum acceleration of the robot.
     * @param trackingPeriod  The tracking period of the robot.
     * @param length          The length of the robot.
     * @param width           The length of the robot.
     * @param initialPose     The initial Pose of the robot.
     * @param goalPoses       The goal Poses of the robot.
     * @param safetyDistance  The safety distance of the robot.
     * @throws IllegalStateException if a robot with the same ID already exists.
     */
    public AbstractVehicle(int ID, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                           int trackingPeriod, double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance) {
        this.ID = ID;
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
        this.footprint = makeFootprint(length, width);

        AbstractVehicle existingVehicle = VehiclesHashMap.getVehicle(ID);
        if (existingVehicle != null) {
            throw new IllegalStateException("ID " + ID + " already exists.");
        }

        VehiclesHashMap.getList().put(this.ID, this);
        vehicleNumber++;
    }
    public AbstractVehicle(int priorityID, Color color, double maxVelocity, double maxAcceleration, int trackingPeriod,
                           double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance) {
        this(vehicleNumber, priorityID, color, maxVelocity, maxAcceleration, trackingPeriod, length, width, initialPose,
                goalPoses, safetyDistance);
    }
    public static Coordinate[] makeFootprint(double length, double width) {
        return new Coordinate[]{               // FIXME Currently allows four sided vehicles only
                new Coordinate(-length, width),        //back left
                new Coordinate(length, width),         //back right
                new Coordinate(length, -width),        //front right
                new Coordinate(-length, -width)        //front left
        };
    }
    @Override
    public String toString() {
        return "AbstractVehicle{" +
                "ID=" + ID +
                ", priorityID=" + priorityID +
                ", type='" + type + '\'' +
                ", color=" + color +
                ", maxVelocity=" + maxVelocity +
                ", maxAcceleration=" + maxAcceleration +
                ", trackingPeriod=" + trackingPeriod +
                ", xLength=" + length +
                ", yLength=" + width +
                ", footprint=" + Arrays.toString(footprint) +
                "}";
    }

    /**
     * Generates the plan for the robot given the initial pose, goal poses, map, and whether the path
     * should be reversed.
     * The subclasses should provide the concrete implementation for this method.
     *
     * @param initial     The initial pose of the robot.
     * @param goals       An array of goal poses.
     * @param map         The map used for planning.
     * @param inversePath A flag indicating if the path should be reversed.
     */
    public abstract void getPlan(Pose initial, Pose[] goals, String map, Boolean inversePath);

    protected static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public int getID() {
        return ID;
    }

    public Coordinate[] getFootprint() {
        return footprint;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getColorCode() {
        return "#" + String.format("%06x", 0xFFFFFF & getColor().getRGB());
    }

    public Coordinate[] getFootPrint() {
        return footprint;
    }

    public double getPlanLength() {
        return pathLength;
    }

    public void setPlanLength(PoseSteering[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            double deltaS = path[i].getPose().distanceTo(path[i + 1].getPose());
            pathLength += deltaS;
        }
        pathLength = round(pathLength * 10.0) / 10.0;
        VehiclesHashMap.getVehicle(this.getID()).pathLength = pathLength;
    }

    public PoseSteering[] getPath() {
        return path;
    }

    public void setPath(PoseSteering[] path) {
        this.path = path;
        setPlanLength(path);
    }

    public double getLength() {
        return length;
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
}
