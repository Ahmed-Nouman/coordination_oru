package se.oru.coordination.coordination_oru.forwardModel;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.dataStructue.RobotReport;
import se.oru.coordination.coordination_oru.simulation2D.State;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeTrackerRK4;

import static se.oru.coordination.coordination_oru.utils.RK4.integrate;

public class ConstantAccelerationForwardModel implements ForwardModel {
		
	private final double maxAccel;
    private final double maxVel;
	private final double temporalResolution;
	private final int trackingPeriodInMillis;
	private final int controlPeriodInMillis;
	
	public ConstantAccelerationForwardModel(double maxAccel, double maxVel, double temporalResolution, int controlPeriodInMillis, int trackingPeriodInMillis) {
		this.maxAccel = maxAccel;
		this.maxVel = maxVel;	
		this.temporalResolution = temporalResolution;
		this.controlPeriodInMillis = controlPeriodInMillis;
		this.trackingPeriodInMillis = trackingPeriodInMillis;
	}

	@Override
	public boolean canStop(TrajectoryEnvelope trajectoryEnvelope, RobotReport currentState, int targetPathIndex, boolean useVelocity) {
		if (useVelocity && currentState.getVelocity() <= 0.0) return true;
		double distance = TrajectoryEnvelopeTrackerRK4.computeDistance(trajectoryEnvelope.getTrajectory(), (currentState.getPathIndex() != -1 ? currentState.getPathIndex() : 0), targetPathIndex);
        var state = new State(0.0, currentState.getVelocity());
		double time = 0.0;
		double deltaTime = 0.0001;
		long lookaheadInMillis = this.controlPeriodInMillis + 2L *(TrajectoryEnvelopeCoordinator.MAX_TX_DELAY + trackingPeriodInMillis);
		if (lookaheadInMillis > 0) {
			while (time*this.temporalResolution < lookaheadInMillis) {
				integrate(state, time, deltaTime, false, maxVel, 1.0, maxAccel);
				time += deltaTime;
			}
		}
		while (state.getVelocity() > 0) {
			if (state.getPosition() > distance) return false;
			integrate(state, time, deltaTime, true, maxVel, 1.0, maxAccel);
			time += deltaTime;
		}
		return true;
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
				double ratio = 1.0 - (accumulatedDistance-auxState.getPosition()) / deltaS;
                poses[i].interpolate(poses[i + 1], ratio);
                currentPathIndex = i;
				break;
			}
		}
		if (currentPathIndex == -1) {
			currentPathIndex = poses.length-1;
        }
		return currentPathIndex;
	}
	
	@Override
	public int getEarliestStoppingPathIndex(TrajectoryEnvelope trajectoryEnvelope, RobotReport robotReport) {
        var state = new State(robotReport.getDistanceTraveled(), robotReport.getVelocity());
		double time = 0.0;
		double deltaTime = 0.0001;
		long lookaheadInMillis = this.controlPeriodInMillis + 2L *(TrajectoryEnvelopeCoordinator.MAX_TX_DELAY + trackingPeriodInMillis);
		if (lookaheadInMillis > 0) {
			while (time * temporalResolution < lookaheadInMillis) {
				integrate(state, time, deltaTime, false, maxVel, 1.0, maxAccel * 1.1);
				time += deltaTime;
			}
		}
		while (state.getVelocity() > 0) {
			integrate(state, time, deltaTime, true, maxVel, 1.0, maxAccel * 0.9);
			time += deltaTime;
		}
		return getPathIndex(trajectoryEnvelope, state);
	}

}
