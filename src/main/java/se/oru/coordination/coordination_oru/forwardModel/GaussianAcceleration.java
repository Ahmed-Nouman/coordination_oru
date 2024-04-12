package se.oru.coordination.coordination_oru.forwardModel;

import java.util.Random;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.utils.State;

public class GaussianAcceleration implements ForwardModel {

    public static final double MAX_ACCELERATION_VARIANCE = 0.25;
    private final double maxAccel;
    private final double temporalResolution;
    private final int trackingPeriodInMillis;
    private final int controlPeriodInMillis;
    private final Random random;

    public GaussianAcceleration(double maxAccel, double temporalResolution, int controlPeriodInMillis, int trackingPeriodInMillis) {
        this.maxAccel = maxAccel;
        this.temporalResolution = temporalResolution;
        this.controlPeriodInMillis = controlPeriodInMillis;
        this.trackingPeriodInMillis = trackingPeriodInMillis;
        this.random = new Random();
    }

    @Override
    public int getEarliestStoppingPathIndex(TrajectoryEnvelope trajectoryEnvelope, RobotReport robotReport) {
        State state = new State(robotReport.getDistanceTraveled(), robotReport.getVelocity());
        double time = 0.0;
        double deltaTime = 0.0001;
        long lookaheadInMillis = this.controlPeriodInMillis + 2L * (TrajectoryEnvelopeCoordinator.MAX_TX_DELAY + trackingPeriodInMillis);
        if (lookaheadInMillis > 0) {
            while (time * temporalResolution < lookaheadInMillis) {
                gaussianIntegrate(state, time, deltaTime, false);
                time += deltaTime;
            }
        }
        while (state.getVelocity() > 0) {
            gaussianIntegrate(state, time, deltaTime, true);
            time += deltaTime;
        }
        return getPathIndex(trajectoryEnvelope, state);
    }

    private void gaussianIntegrate(State state, double time, double deltaTime, boolean braking) {
        double accelVariance = MAX_ACCELERATION_VARIANCE * maxAccel;
        double acceleration = (braking ? -maxAccel : maxAccel) + accelVariance * random.nextGaussian();
        acceleration = Math.max(Math.min(acceleration, maxAccel), -maxAccel);

        state.setVelocity(state.getVelocity() + acceleration * deltaTime);
        state.setPosition(state.getPosition() + state.getVelocity() * deltaTime);
    }

    private int getPathIndex(TrajectoryEnvelope trajectoryEnvelope, State auxState) {
        if (auxState == null) return -1;
        int currentPathIndex = -1;
        double accumulatedDistance = 0.0;
        var trajectory = trajectoryEnvelope.getTrajectory();
        var poses = trajectory.getPose();
        for (int i = 0; i < poses.length - 1; i++) {
            double deltaS = poses[i].distanceTo(poses[i + 1]);
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
