package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.Task;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HumanVehicle extends AutonomousVehicle {

    private final Random random;
    private final TrajectoryEnvelopeCoordinatorSimulation tec;

    public HumanVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                        double length, double width, Pose initialPose, double safetyDistance, int missionRepetition, ForwardModel model,
                        TrajectoryEnvelopeCoordinatorSimulation tec) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition, model);
        this.random = new Random();
        this.tec = tec;
    }

    public HumanVehicle(String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, double length,
                        double width, Pose initialPose, double safetyDistance, int missionRepetition, ForwardModel model,
                        TrajectoryEnvelopeCoordinatorSimulation tec) {
        this(vehicleNumber, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition, model, tec);
    }

    @Override
    public void setCurrentTaskIndex(int currentTaskIndex) {
        super.setCurrentTaskIndex(currentTaskIndex);
        setupBehaviorForCurrentTask();
    }

    private void setupBehaviorForCurrentTask() {
        if (getCurrentTaskIndex() >= 0 && getCurrentTaskIndex() < getTasks().size()) {
            Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);
            int behavior = random.nextInt(3);
            switch (behavior) {
                case 0:
                    System.out.println("No stopping or slowing for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    break;
                case 1:
                    System.out.println("Slowing behavior for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    scheduleVehicleSlow(getID(), trackerRetriever, 1.0);
                    break;
                case 2:
                    System.out.println("Stopping and resuming behavior for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    int numberOfStops = random.nextInt(3) + 1;
                    scheduleMultipleStops(getID(), trackerRetriever, numberOfStops);
                    break;
            }
        }
    }

    private void scheduleMultipleStops(Integer vehicleIDToStop, Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever, int numberOfStops) {
        final var scheduler = Executors.newScheduledThreadPool(1);
        long currentTime = 0;

        for (int i = 0; i < numberOfStops; i++) {
            long stopTime = currentTime + 5 + random.nextInt(16);
            long resumeTime = 5 + random.nextInt(16);

            var stopRunnable = new Runnable() {
                @Override
                public void run() {
                    AbstractTrajectoryEnvelopeTracker tracker = trackerRetriever.apply(vehicleIDToStop);
                    stopVehicle(tracker);
                    scheduler.schedule(() -> resumeVehicle(tracker), resumeTime, TimeUnit.SECONDS);
                }
            };

            scheduler.schedule(stopRunnable, stopTime, TimeUnit.SECONDS);
            currentTime = stopTime + resumeTime;
        }
    }

    private void stopVehicle(AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            if (tracker instanceof AdaptiveTrackerRK4) {
                ((AdaptiveTrackerRK4) tracker).pause();
            }
        }
    }

    private void resumeVehicle(AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            if (tracker instanceof AdaptiveTrackerRK4) {
                ((AdaptiveTrackerRK4) tracker).resume();
            }
        }
    }

    public static void scheduleVehicleSlow(
            Integer vehicleIDToSlow,
            Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever,
            double minVelocity) {

        final var scheduler = Executors.newScheduledThreadPool(1);
        Random random = new Random();
        long delay = 5 + random.nextInt(11);

        var slowdownRunnable = new Runnable() {
            @Override
            public void run() {
                AbstractTrajectoryEnvelopeTracker tracker = trackerRetriever.apply(vehicleIDToSlow);
                if (tracker instanceof AdaptiveTrackerRK4) {
                    AdaptiveTrackerRK4 adaptiveTracker = (AdaptiveTrackerRK4) tracker;
                    adaptiveTracker.maxVelocity = minVelocity;
                }
            }
        };

        scheduler.schedule(slowdownRunnable, delay, TimeUnit.SECONDS);
    }
}
