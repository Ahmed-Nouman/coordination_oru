package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.TrajectoryEnvelopeCoordinator;

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
    private double lookAheadDistance;

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
     */
    public LookAheadVehicle(int ID, String name, double lookAheadDistance, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                            double length, double width, Pose initialPose, double safetyDistance, int missionRepetition) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition);
        this.lookAheadDistance = lookAheadDistance;
    }

    public LookAheadVehicle(String name, double lookAheadDistance, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                            double length, double width, Pose initialPose, double safetyDistance, int missionRepetition) {
        this(vehicleNumber, name, lookAheadDistance, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition);
    }

    //FIXME: Remove as a constructor
    public LookAheadVehicle(double lookAheadDistance) {
        this(vehicleNumber, null,  lookAheadDistance,  1, Color.YELLOW, 5.0, 1.0,
                0.5, 0.5, null, 0, 0);
    }

    /**
     * Updates the path of all LookAheadVehicles in the TrajectoryEnvelopeCoordinator.
     *
     * @param tec The TrajectoryEnvelopeCoordinator containing the vehicles.
     * @param lookAheadVehicle The LookAheadVehicle for which the path needs to be updated.
     */
    public synchronized void updateLookAheadRobotPath(TrajectoryEnvelopeCoordinator tec, LookAheadVehicle lookAheadVehicle) {
        if (tec.isDriving(lookAheadVehicle.getID()) && lookAheadVehicle.getLookAheadDistance() != -1) {
            var newPath = lookAheadVehicle.getLimitedPath(lookAheadVehicle.getID(), lookAheadVehicle.getLookAheadDistance(), tec);
            tec.updatePath(lookAheadVehicle.getID(), newPath, 0);
        }
    }

    /**
     * Retrieves the path for the LookAheadVehicle.
     *
     * @param lookAheadDistance The look-ahead distance to consider for path generation.
     * @param tec               The TrajectoryEnvelopeCoordinator containing the vehicles.
     * @return An array of PoseSteering objects representing the path.
     */
    public PoseSteering[] getPath(double lookAheadDistance, TrajectoryEnvelopeCoordinator tec) {
        return getLimitedPath(getID(), lookAheadDistance, tec);
    }

    /**
     * Retrieves a limited path for the LookAheadVehicle up to a specified look-ahead distance.
     *
     * @param vehicleID         The ID of the vehicle for which the path is being generated.
     * @param lookAheadDistance The maximum distance ahead in the path to consider.
     * @param tec               The TrajectoryEnvelopeCoordinator containing the vehicles.
     * @return An array of PoseSteering objects representing the limited path.
     */
    public synchronized PoseSteering[] getLimitedPath(int vehicleID, double lookAheadDistance, TrajectoryEnvelopeCoordinator tec) {
        if (lookAheadDistance < 0) return getPath();

        var distance = 0.0;
        var vehicleReport = tec.getRobotReport(vehicleID);
        var fullPath = getPath();

        if (vehicleReport == null) {
            System.err.println("Error: RobotReport for vehicleID " + vehicleID + " not found.");
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

    public void setLookAheadDistance(double lookAheadDistance) {
        this.lookAheadDistance = lookAheadDistance;
    }
    public double getLookAheadDistance() {
        return lookAheadDistance;
    }

}
