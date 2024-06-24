package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.PausedState;
import se.oru.coordination.coordination_oru.utils.VehicleState;

public class RunningState implements VehicleState {
    @Override
    public void handlePause(AbstractTrajectoryEnvelopeTracker tracker) {
        if (tracker instanceof AdaptiveTrackerRK4) {
            ((AdaptiveTrackerRK4) tracker).pause();
        }
        tracker.setState(new PausedState());
    }

    @Override
    public void handleResume(AbstractTrajectoryEnvelopeTracker tracker) {
        // Already running, do nothing
    }
    
}
