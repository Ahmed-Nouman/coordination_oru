package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HumanVehicle extends AutonomousVehicle {

    private static final Random random = new Random();
    private static final int MAX_NO_OF_STOPS = 3;
    private static final double MIN_VELOCITY = 2.0;
    private static final long STOP_TIME = 5 + random.nextInt(16);
    private static final long RESUME_TIME = 5 + random.nextInt(16);
    private static final long SLOWDOWN_TIME = 5 + random.nextInt(11);
    private final TrajectoryEnvelopeCoordinatorSimulation tec;

    public HumanVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                        double length, double width, Pose initialPose, double safetyDistance, int missionRepetition, ForwardModel model,
                        TrajectoryEnvelopeCoordinatorSimulation tec) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition, model);
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
            var behavior = Behavior.fromIntToBehavior(random.nextInt(Behavior.values().length));
            switch (behavior) {
                case NORMAL:
                    System.out.println("No stopping or slowing for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    break;
                case SLOWING:
                    System.out.println("Slowing behavior for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    scheduleVehicleSlow(getID(), trackerRetriever, MIN_VELOCITY);
                    break;
                case STOPPING:
                    System.out.println("Stopping and resuming behavior for vehicle " + getID() + " on task " + getCurrentTaskIndex());
                    int numberOfStops = random.nextInt(MAX_NO_OF_STOPS) + 1;
                    scheduleMultipleStops(getID(), trackerRetriever, numberOfStops);
                    break;
            }
        }
    }

    private static void scheduleMultipleStops(Integer vehicleIDToStop, Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever, int numberOfStops) {
        final var scheduler = Executors.newScheduledThreadPool(1);
        long currentTime = 0;

        for (int i = 0; i < numberOfStops; i++) {
            long stopTime = currentTime + STOP_TIME;
            long resumeTime = RESUME_TIME;

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

    private static void stopVehicle(AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            if (tracker instanceof AdaptiveTrackerRK4) {
//                ((AdaptiveTrackerRK4) tracker).pause(this);
            }
        }
    }

    private static void resumeVehicle(AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            if (tracker instanceof AdaptiveTrackerRK4) {
//                ((AdaptiveTrackerRK4) tracker).resume(this);
            }
        }
    }

    public static void scheduleVehicleSlow(
            Integer vehicleIDToSlow,
            Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever,
            double minVelocity) {

        final var scheduler = Executors.newScheduledThreadPool(1);

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

        scheduler.schedule(slowdownRunnable, SLOWDOWN_TIME, TimeUnit.SECONDS);
    }

    public enum Behavior {
        STOPPING(0),
        SLOWING(1),
        NORMAL(2);

        private final int value;

        Behavior(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Behavior fromIntToBehavior(int value) {
            for (var behavior : Behavior.values())
                if (behavior.getValue() == value) return behavior;
            throw new IllegalArgumentException("Invalid behavior value: " + value);
        }
    }

}
