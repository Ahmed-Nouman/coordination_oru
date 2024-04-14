package se.oru.coordination.coordination_oru.forwardModel;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.utils.State;

import static se.oru.coordination.coordination_oru.utils.RungeKutta4.integrate;

public class AlternatingAccelerationModel implements ForwardModel {

    private final double maxAccel;
    private final double maxVel;
    private final double temporalResolution;
    private final int controlPeriodInMillis;
    private final int trackingPeriodInMillis;
    private boolean accelerating = true;

    public AlternatingAccelerationModel(double maxAccel, double maxVel, double temporalResolution, int controlPeriodInMillis, int trackingPeriodInMillis) {
        this.maxAccel = maxAccel;
        this.maxVel = maxVel;
        this.temporalResolution = temporalResolution;
        this.controlPeriodInMillis = controlPeriodInMillis;
        this.trackingPeriodInMillis = trackingPeriodInMillis;
    }

    @Override
    public int getEarliestStoppingPathIndex(TrajectoryEnvelope trajectoryEnvelope, RobotReport robotReport) {
        State state = new State(robotReport.getDistanceTraveled(), robotReport.getVelocity());
        double time = 0.0;
        double deltaTime = 0.0001;
        long periodInMillis = this.controlPeriodInMillis;
        double acceleration = this.accelerating ? 0.5 * maxAccel : -maxAccel; // Toggle acceleration

        while (time * temporalResolution < periodInMillis) {
            integrate(state, time, deltaTime, false, maxVel, 1.0, acceleration);
            time += deltaTime;
        }

        // Toggle the acceleration state for the next period
        this.accelerating = !this.accelerating;

        return getPathIndex(trajectoryEnvelope, state);
    }

    private int getPathIndex(TrajectoryEnvelope trajectoryEnvelope, State auxState) {
        if (auxState == null) return -1;
        int currentPathIndex = -1;
        double accumulatedDistance = 0.0;
        var trajectory = trajectoryEnvelope.getTrajectory();
        var poses = trajectory.getPose();
        for (int i = 0; i < poses.length-1; i++) {
            double deltaS = poses[i].distanceTo(poses[i+1]);
            accumulatedDistance += deltaS;
            if (accumulatedDistance > auxState.getPosition()) {
                double ratio = 1.0 - (accumulatedDistance - auxState.getPosition()) / deltaS;
                poses[i].interpolate(poses[i + 1], ratio);
                currentPathIndex = i;
                break;
            }
        }
        if (currentPathIndex == -1) {
            currentPathIndex = poses.length - 1;
        }
        return currentPathIndex;
    }

}
