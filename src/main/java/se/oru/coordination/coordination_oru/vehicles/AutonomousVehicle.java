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

    public AutonomousVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, int trackingPeriod,
                             double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance, int missionRepetition) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, trackingPeriod, length, width, initialPose, goalPoses, safetyDistance,
                missionRepetition);
    }

    public AutonomousVehicle(String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, int trackingPeriod, double length,
                             double width, Pose initialPose, Pose[] goalPoses, double safetyDistance, int missionRepetition) {
        this(vehicleNumber, name, priorityID, color, maxVelocity, maxAcceleration, trackingPeriod, length, width, initialPose, goalPoses, safetyDistance,
                missionRepetition);
    }

    public AutonomousVehicle(Pose initialPose, Pose[] goalPoses) {
        this(vehicleNumber, null, 1, Color.YELLOW, 5.0, 1.0, 30, 0.5, 0.5,
                initialPose, goalPoses, 0, 0);
    }

    public AutonomousVehicle() {
        this(vehicleNumber, null, 1, Color.YELLOW, 5.0, 1.0, 30, 0.5, 0.5,
                null, null, 0, 0);
    }
}
