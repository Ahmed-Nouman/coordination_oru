package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;

import java.util.Set;

public class CollisionDetector {
    private final TrajectoryEnvelopeCoordinatorSimulation tec;
    private final double collisionThreshold;
    private boolean running;

    public CollisionDetector(TrajectoryEnvelopeCoordinatorSimulation tec, double collisionThreshold) {
        this.tec = tec;
        this.collisionThreshold = collisionThreshold;
        this.running = false;
    }

    public void start() {
        if (running) {
            System.out.println("Collision detection is already running.");
            return;
        }

        running = true;
        Thread collisionThread = new Thread(() -> {
            while (running) {
                if (checkForCollisions()) {
                    stopThread();  // Automatically stop the thread when a collision is detected
                    break;
                }
                try {
                    Thread.sleep(500); // Check every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        collisionThread.setDaemon(true); // Set the thread as a daemon thread
        collisionThread.start();
        System.out.println("Collision detection started.");
    }

    private boolean checkForCollisions() {
        Set<Integer> robotIDs = tec.getAllRobotIDs();
        Integer[] robotIdArray = robotIDs.toArray(new Integer[0]);

        for (int i = 0; i < robotIdArray.length; i++) {
            for (int j = i + 1; j < robotIdArray.length; j++) {
                Integer robot1ID = robotIdArray[i];
                Integer robot2ID = robotIdArray[j];

                var report1 = tec.getRobotReport(robot1ID);
                var report2 = tec.getRobotReport(robot2ID);

                if (report1 != null && report2 != null) {
                    double distance = computeDistance(report1.getPose(), report2.getPose());
                    if (distance < collisionThreshold) {
                        System.out.println("Collision Warning: Robots " + robot1ID + " and " + robot2ID + " are too close! Distance: " + distance);
                        notifyCollision(robot1ID, robot2ID, distance);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double computeDistance(Pose pose1, Pose pose2) {
        double x1 = pose1.getX();
        double y1 = pose1.getY();
        double x2 = pose2.getX();
        double y2 = pose2.getY();

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void notifyCollision(Integer robot1ID, Integer robot2ID, double distance) {
        // Implement your notification logic here (e.g., logging, UI update, etc.)
        System.out.println("Collision detected between " + robot1ID + " and " + robot2ID + " with distance: " + distance);
        tec.stopInference();
    }

    private void stopThread() {
        running = false;
        System.out.println("Collision detection thread stopped.");
    }
}
