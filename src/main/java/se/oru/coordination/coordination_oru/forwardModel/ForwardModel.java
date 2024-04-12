package se.oru.coordination.coordination_oru.forwardModel;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.utils.RobotReport;

public interface ForwardModel {

//	boolean canStop(TrajectoryEnvelope te, RobotReport currentState, int targetPathIndex, boolean useVelocity);

	int getEarliestStoppingPathIndex(TrajectoryEnvelope te, RobotReport currentState);

}