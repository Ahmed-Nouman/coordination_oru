package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.VehicleState;

public class PausedState implements VehicleState {
    @Override
    public void handlePause(AbstractTrajectoryEnvelopeTracker tracker) {
        // Already paused, do nothing
    }

    @Override
    public void handleResume(AbstractTrajectoryEnvelopeTracker tracker) {
        if (tracker instanceof AdaptiveTrackerRK4) {
            ((AdaptiveTrackerRK4) tracker).resume();
        }
        tracker.setState(new RunningState());
    }

}