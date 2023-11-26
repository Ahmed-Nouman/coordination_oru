package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.awt.*;

/**
 * The AutonomousVehicle class extends AbstractVehicle and represents an autonomous vehicle with advanced
 * path planning capabilities. It uses the ReedsSheppCarPlanner for sophisticated path planning and
 * navigation functionalities, making it suitable for complex environments and tasks.
 *
 * <p>Key features of this class include:</p>
 * <ul>
 *     <li>Advanced path planning using the ReedsSheppCarPlanner.</li>
 *     <li>Generation of paths with consideration for various parameters like radius, planning time, turning radius, and distance between path points.</li>
 *     <li>Ability to handle inverse paths and segment-based planning.</li>
 * </ul>
 *
 * <p>This class is ideal for scenarios requiring precise and adaptable navigation strategies in autonomous vehicles.</p>
 *
 * @author anm
 */

public class AutonomousVehicle extends AbstractVehicle {

    /**
     * Constructs a new AutonomousVehicle with the specified parameters.
     * This constructor provides full control over all vehicle attributes.
     *
     * @param ID              Unique identifier of the vehicle.
     * @param name            Name of the vehicle.
     * @param priorityID      Priority identifier of the vehicle, used for handling conflicts or scheduling.
     * @param color           Color of the vehicle for visual representation.
     * @param maxVelocity     Maximum velocity the vehicle can attain.
     * @param maxAcceleration Maximum acceleration rate of the vehicle.
     * @param trackingPeriod  Duration over which the vehicle's state is monitored.
     * @param length          Physical length of the vehicle.
     * @param width           Physical width of the vehicle.
     * @param initialPose     Starting pose of the vehicle in the environment.
     * @param goalPoses       Array of target poses for the vehicle to reach.
     * @param safetyDistance  Minimum distance to maintain from obstacles for safety.
     */
    public AutonomousVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, int trackingPeriod,
                             double length, double width, Pose initialPose, Object goalPoses, double safetyDistance) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, trackingPeriod, length, width, initialPose, goalPoses, safetyDistance
        );
    }

    /**
     * Constructs a new AutonomousVehicle with generated ID and specified parameters.
     * Useful for quickly creating a vehicle with unique ID and custom attributes.
     *
     * @param name            Name of the vehicle.
     * @param priorityID      Priority identifier of the vehicle.
     * @param color           Color of the vehicle.
     * @param maxVelocity     Maximum velocity of the vehicle.
     * @param maxAcceleration Maximum acceleration rate of the vehicle.
     * @param trackingPeriod  Tracking period for the vehicle's state.
     * @param length          Physical length of the vehicle.
     * @param width           Physical width of the vehicle.
     * @param initialPose     Initial pose of the vehicle.
     * @param goalPoses       Target poses for the vehicle.
     * @param safetyDistance  Safety distance to maintain from obstacles.
     */
    public AutonomousVehicle(String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, int trackingPeriod, double length,
                             double width, Pose initialPose, Object goalPoses, double safetyDistance) {
        this(vehicleNumber, name, priorityID, color, maxVelocity, maxAcceleration, trackingPeriod, length, width, initialPose, goalPoses, safetyDistance
        );
    }

    /**
     * Constructs a new AutonomousVehicle with default settings, initial pose, and goal poses.
     * This constructor is useful for quick instantiation with minimal parameters.
     *
     * @param initialPose       Starting pose of the vehicle.
     * @param goalPoses         Array of target poses for the vehicle.
     */
    public AutonomousVehicle(Pose initialPose, Object goalPoses) {
        this(vehicleNumber, null, 1, Color.YELLOW, 5.0, 1.0, 30, 0.5, 0.5,
                initialPose, goalPoses, 0);
    }

    /**
     * Constructs a new AutonomousVehicle with default settings.
     * This constructor initializes the vehicle with default values and no initial or goal poses.
     */
    public AutonomousVehicle() {
        this(vehicleNumber, null, 1, Color.YELLOW, 5.0, 1.0, 30, 0.5, 0.5,
                null, null, 0);
    }
}
