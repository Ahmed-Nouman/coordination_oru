package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;

import java.awt.*;
import java.util.Arrays;

/**
 * The LookAheadVehicle class represents a type of vehicle that has the capability to predict and plan
 * its path up to a specified look-ahead distance. This class extends AbstractVehicle, inheriting
 * its basic attributes and functionalities while adding specialized features for look-ahead path planning.
 *
 * <p>This class is particularly useful in scenarios where dynamic path adjustment is required based on
 * real-time conditions or constraints, allowing for more efficient and safe navigation.</p>
 *
 * <p>Key functionalities:</p>
 * <ul>
 *     <li>Path updating in accordance with the vehicle's current state and look-ahead distance.</li>
 *     <li>Generation of limited paths considering the look-ahead distance for precise movement planning.</li>
 * </ul>
 *
 * @author anm
 */

public class LookAheadVehicle extends AutonomousVehicle {
    private final double lookAheadDistance;

    /**
     * Constructs a new LookAheadVehicle with the specified parameters.
     *
     * @param ID                Unique identifier of the vehicle.
     * @param name              Name of the vehicle.
     * @param lookAheadDistance The maximum distance ahead on the path that the vehicle considers for planning.
     * @param priorityID        Priority identifier of the vehicle.
     * @param color             Visual representation color of the vehicle.
     * @param maxVelocity       Maximum velocity that the vehicle can achieve.
     * @param maxAcceleration   Maximum acceleration rate of the vehicle.
     * @param length            Physical length of the vehicle.
     * @param width             Physical width of the vehicle.
     * @param initialPose       Initial pose of the vehicle in the environment.
     * @param safetyDistance    Minimum safe distance to be maintained from other objects.
     * @param model             Forward model for the vehicle.
     */
    public LookAheadVehicle(int ID, String name, double lookAheadDistance, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                            double length, double width, Pose initialPose, double safetyDistance, int taskRepetition, ForwardModel model) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                taskRepetition, model);
        this.lookAheadDistance = lookAheadDistance;
    }

    public LookAheadVehicle(String name, double lookAheadDistance, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                            double length, double width, Pose initialPose, double safetyDistance, int taskRepetition, ForwardModel model) {
        this(vehicleNumber, name, lookAheadDistance, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                taskRepetition, model);
    }

    /**
     * Updates the path of all LookAheadVehicles in the TrajectoryEnvelopeCoordinator.
     *
     * @param tec The TrajectoryEnvelopeCoordinator containing the vehicles.
     */
    public synchronized void updatePath(TrajectoryEnvelopeCoordinator tec) {
        if (tec.isDriving(this.getID()) && this.getLookAheadDistance() != -1) {
            var newPath = this.getLimitedPath(tec);
            tec.changePath(this.getID(), newPath, 0);
        }
    }

    /**
     * Retrieves the path for the LookAheadVehicle.
     *
     * @param tec               The TrajectoryEnvelopeCoordinator containing the vehicles.
     * @return An array of PoseSteering objects representing the path.
     */
    public PoseSteering[] getPath(TrajectoryEnvelopeCoordinator tec) {
        return getLimitedPath(tec);
    }

    /**
     * Retrieves a limited path for the LookAheadVehicle up to a specified look-ahead distance.
     *
     * @param tec The TrajectoryEnvelopeCoordinator containing the vehicles.
     * @return An array of PoseSteering objects representing the limited path.
     */
    public synchronized PoseSteering[] getLimitedPath(TrajectoryEnvelopeCoordinator tec) {
        if (lookAheadDistance < 0) return getPaths().get(0);

        var distance = 0.0;
        var vehicleReport = tec.getRobotReport(getID());
        var fullPath = getPaths().get(0);

        if (vehicleReport == null) {
            System.err.println("Error: RobotReport for vehicleID " + getID() + " not found.");
            return new PoseSteering[0];
        }

        var currentDistance = vehicleReport.getDistanceTraveled();
        var totalDistance = getPlanLength();
        int pathIndex = Math.max(vehicleReport.getPathIndex(), 0);

        for (; pathIndex < fullPath.length - 1 && distance <= lookAheadDistance; pathIndex++) {
            if ((currentDistance + lookAheadDistance) >= totalDistance) {
                return fullPath;
            } else {
                distance += fullPath[pathIndex].getPose().distanceTo(fullPath[pathIndex + 1].getPose());
            }
        }
        return Arrays.copyOfRange(fullPath, 0, pathIndex);
    }

    public double getLookAheadDistance() {
        return lookAheadDistance;
    }

}
