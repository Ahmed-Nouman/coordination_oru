package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;

public interface VehicleState {
    void handlePause(AbstractTrajectoryEnvelopeTracker tracker);
    void handleResume(AbstractTrajectoryEnvelopeTracker tracker);
}
