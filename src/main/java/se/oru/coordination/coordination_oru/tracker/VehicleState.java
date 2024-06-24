package se.oru.coordination.coordination_oru.tracker;

public interface VehicleState {
    void handlePause(AbstractTrajectoryEnvelopeTracker tracker);
    void handleResume(AbstractTrajectoryEnvelopeTracker tracker);
}
