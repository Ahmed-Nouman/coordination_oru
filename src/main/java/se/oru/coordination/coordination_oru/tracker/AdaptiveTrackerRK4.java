package se.oru.coordination.coordination_oru.tracker;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.coordinator.NetworkConfiguration;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.RungeKutta4;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.utils.State;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class AdaptiveTrackerRK4 extends AbstractTrajectoryEnvelopeTracker implements Runnable {
    protected static final long WAIT_AMOUNT_AT_END = 3000;

    public double maxVelocity;
    protected final double maxAcceleration;
    protected double overallDistance = 0.0;
    protected double totalDistance = 0.0;
    protected double positionToSlowDown = -1.0;
    protected double elapsedTrackingTime = 0.0;
    private Thread th = null;
    protected State state = null;
    protected double[] curvatureDampening = null;
    private final ArrayList<Integer> internalCriticalPoints = new ArrayList<>();
    private int numberOfReplicas = 1;
    private final Random rand = new Random(Calendar.getInstance().getTimeInMillis());
    private TreeMap<Double, Double> slowDownProfile = null;
    private boolean slowingDown = false;
    private boolean useInternalCPs = true;
    protected ArrayList<RobotReport> reportsList = new ArrayList<>();
    protected ArrayList<Long> reportTimeLists = new ArrayList<>();
    private HashMap<Integer, Integer> userCPReplacements = null;

    public AdaptiveTrackerRK4(TrajectoryEnvelope te, int timeStep, double temporalResolution, double maxVelocity, double maxAcceleration, TrajectoryEnvelopeCoordinator tec, TrackingCallback cb) {
        super(te, temporalResolution, tec, timeStep, cb);
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.state = new State(0.0, 0.0);
        this.totalDistance = this.computeDistance(0, trajectory.getPose().length - 1);
        this.overallDistance = totalDistance;
        this.computeInternalCriticalPoints();
        this.slowDownProfile = this.getSlowdownProfile();
        this.positionToSlowDown = this.computePositionToSlowDown();
        this.th = new Thread(this, "Adaptive RK4 tracker " + te.getComponent());
        this.th.setPriority(Thread.MAX_PRIORITY);
    }

    public static void scheduleVehiclesStop(
            AutonomousVehicle priorityVehicle,
            AbstractTrajectoryEnvelopeTracker priorityVehicleTracker,
            List<Integer> missionIDsToTrigger,
            List<Integer> vehicleIDsToComply,
            Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever) {

        final var scheduler = Executors.newScheduledThreadPool(5);
        final var pausedTrackers = new HashSet<AbstractTrajectoryEnvelopeTracker>();

        var shutdown = new Runnable() {
            @Override
            public void run() {
                boolean shouldPause = missionIDsToTrigger.contains(priorityVehicle.getCurrentTaskIndex());
                var trackers = new ArrayList<AbstractTrajectoryEnvelopeTracker>();
                for (Integer vehicleId : vehicleIDsToComply) {
                    AbstractTrajectoryEnvelopeTracker tracker = trackerRetriever.apply(vehicleId);
                    trackers.add(tracker);
                }
                if (shouldPause) {
                    if (!(pausedTrackers.size() == trackers.size())) {
                        if (!priorityVehicle.isStopped()){
                            stopVehicle(priorityVehicle, priorityVehicleTracker); // This does not work.
                        }
                    } else {
                        if (priorityVehicle.isStopped()) {
                            resumeVehicle(priorityVehicle, priorityVehicleTracker);
                        }
                    }
                    for (AbstractTrajectoryEnvelopeTracker tobeTrackerPaused : trackers) {
                        if (!pausedTrackers.contains(tobeTrackerPaused)) {
                            if (vehicleCanBeStopped(priorityVehicle, tobeTrackerPaused)) {
                                stopVehicle(priorityVehicle, tobeTrackerPaused);
                                pausedTrackers.add(tobeTrackerPaused);
                            }
                        }
                    }
                } else {
                    if (!pausedTrackers.isEmpty()) {
                        resumeVehicles(priorityVehicle, trackers);
                        pausedTrackers.clear();
                    }
                }
                scheduler.schedule(this, 100, TimeUnit.MILLISECONDS);
            }
        };

        scheduler.schedule(shutdown, 5, TimeUnit.SECONDS);
    }

    public static void stopVehicles(AutonomousVehicle triggerVehicle, List<AbstractTrajectoryEnvelopeTracker> trackers) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            stopVehicle(triggerVehicle, tracker);
        }
    }

    private static void stopVehicle(AutonomousVehicle triggerVehicle, AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            tracker.pause(triggerVehicle);
        }
    }

    public static boolean vehicleCanBeStopped(AutonomousVehicle triggerVehicle, AbstractTrajectoryEnvelopeTracker trackerToBeStopped) {
        PoseSteering[] triggerVehiclePath = triggerVehicle.getPaths().get(triggerVehicle.getCurrentTaskIndex());
        Pose vehicleToBeStoppedPose = trackerToBeStopped.getRobotReport().getPose();
        for (PoseSteering poseSteering : triggerVehiclePath) {
            if (computeDistance(poseSteering.getPose(), vehicleToBeStoppedPose) < 2.0) {
                return false;
            }
        } return true;
    }

    private static double computeDistance(Pose pose1, Pose pose2) {
        double x1 = pose1.getX();
        double y1 = pose1.getY();
        double x2 = pose2.getX();
        double y2 = pose2.getY();

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private static void resumeVehicles(AutonomousVehicle triggerVehicle, List<AbstractTrajectoryEnvelopeTracker> trackers) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            resumeVehicle(triggerVehicle, tracker);
        }
    }

    private static void resumeVehicle(AutonomousVehicle triggerVehicle, AbstractTrajectoryEnvelopeTracker tracker) {
        synchronized (tracker) {
            tracker.resume(triggerVehicle);
        }
    }

    private static final Map<AbstractTrajectoryEnvelopeTracker, String> slowDownTasks = new HashMap<>();

    public static void scheduleVehicleSlow(
            AutonomousVehicle priorityVehicle,
            List<Integer> missionIDsToTrigger,
            List<Integer> vehicleIDsToComply,
            Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever,
            double maxVelocity, double minVelocity) {

        final var scheduler = Executors.newScheduledThreadPool(1);

        var slowdown = new Runnable() {
            String taskId = UUID.randomUUID().toString();

            @Override
            public void run() {
                boolean shouldSlowDown = missionIDsToTrigger.contains(priorityVehicle.getCurrentTaskIndex());
                var trackers = new ArrayList<AbstractTrajectoryEnvelopeTracker>();
                for (Integer vehicleId : vehicleIDsToComply) {
                    AbstractTrajectoryEnvelopeTracker tracker = trackerRetriever.apply(vehicleId);
                    trackers.add(tracker);
                }
                if (shouldSlowDown) {
                    if (slowDownTasks.isEmpty()) {
                        slowDownVehicles(trackers, minVelocity, taskId);
                    }
                } else {
                    if (!slowDownTasks.isEmpty()) {
                        speedUpVehicles(trackers, maxVelocity, taskId);
                    }
                }
                scheduler.schedule(this, 100, TimeUnit.MILLISECONDS);
            }
        };

        scheduler.schedule(slowdown, 5, TimeUnit.SECONDS);
    }

    private static void slowDownVehicles(List<AbstractTrajectoryEnvelopeTracker> trackers, double targetVelocity, String taskId) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            synchronized (tracker) {
                if (!slowDownTasks.containsKey(tracker)) {
                    tracker.slowDown(targetVelocity);
                    slowDownTasks.put(tracker, taskId);
                }
            }
        }
    }

    private static void speedUpVehicles(List<AbstractTrajectoryEnvelopeTracker> trackers, double targetVelocity, String taskId) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            synchronized (tracker) {
                if (slowDownTasks.containsKey(tracker) && slowDownTasks.get(tracker).equals(taskId)) {
                    tracker.speedUp(targetVelocity);
                    slowDownTasks.remove(tracker);
                }
            }
        }
    }

    @Override
    public void slowDown(double targetVelocity) {
        super.slowDown(targetVelocity);
    }

    @Override
    public void speedUp(double targetVelocity) {
        super.speedUp(targetVelocity);
    }

    private static final Map<AbstractTrajectoryEnvelopeTracker, String> priorityChangeTasks = new HashMap<>();

    public static void scheduleVehiclesPriorityChange(
            AutonomousVehicle priorityVehicle,
            List<Integer> missionIDs,
            TrajectoryEnvelopeCoordinatorSimulation tec,
            Heuristics originalHeuristics,
            Heuristics newHeuristics) {

        final var scheduler = Executors.newScheduledThreadPool(1);

        var updateHeuristics = new Runnable() {
            String taskId = UUID.randomUUID().toString();

            @Override
            public void run() {
                synchronized (tec) {
                    boolean missionMatchFound = missionIDs.contains(priorityVehicle.getCurrentTaskIndex());
                    var trackers = new ArrayList<AbstractTrajectoryEnvelopeTracker>(tec.trackers.values());

                    if (missionMatchFound) {
                        changePriorityOfVehicles(trackers, newHeuristics, taskId);
                    } else {
                        resetPriorityOfVehicles(trackers, originalHeuristics, taskId);
                    }
                }

                scheduler.schedule(this, 100, TimeUnit.MILLISECONDS);
            }
        };

        scheduler.schedule(updateHeuristics, 5, TimeUnit.SECONDS);
    }

    private static void changePriorityOfVehicles(List<AbstractTrajectoryEnvelopeTracker> trackers, Heuristics newHeuristics, String taskId) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            synchronized (tracker) {
                if (!priorityChangeTasks.containsKey(tracker)) {
                    tracker.changePriority(newHeuristics);
                    priorityChangeTasks.put(tracker, taskId);
                }
            }
        }
    }

    private static void resetPriorityOfVehicles(List<AbstractTrajectoryEnvelopeTracker> trackers, Heuristics originalHeuristics, String taskId) {
        for (AbstractTrajectoryEnvelopeTracker tracker : trackers) {
            synchronized (tracker) {
                if (priorityChangeTasks.containsKey(tracker) && priorityChangeTasks.get(tracker).equals(taskId)) {
                    tracker.resetPriority(originalHeuristics);
                    priorityChangeTasks.remove(tracker);
                }
            }
        }
    }

    @Override
    public void changePriority(Heuristics newHeuristics) {
        super.changePriority(newHeuristics);
    }

    @Override
    public void resetPriority(Heuristics newHeuristics) {
        super.resetPriority(newHeuristics);
    }

    @Override
    public void run() {
        this.elapsedTrackingTime = 0.0;
        double deltaTime = 0.0;
        boolean atCP = false;
        int myRobotID = te.getRobotID();
        int myTEID = te.getID();

        while (true) {
            checkPause();
            checkSlowdown();
            checkPriorityChange();

            boolean skipIntegration = false;
            if (state.getPosition() >= this.positionToSlowDown && state.getVelocity() < 0.0) {
                if (criticalPoint == -1 && !atCP) {
                    state = new State(totalDistance, 0.0);
                    onPositionUpdate();
                    break;
                }

                if (!atCP) {
                    int pathIndex = getRobotReport().getPathIndex();
                    metaCSPLogger.info("At critical point (" + te.getComponent() + "): " + criticalPoint + " (" + pathIndex + ")");
                    if (pathIndex > criticalPoint) metaCSPLogger.severe("* ATTENTION! STOPPED AFTER!! *");
                    atCP = true;
                }

                skipIntegration = true;
            }

            checkPause();
            checkSlowdown();
            checkPriorityChange();

            long timeStart = Calendar.getInstance().getTimeInMillis();

            if (!skipIntegration) {
                if (atCP) {
                    metaCSPLogger.info("Resuming from critical point (" + te.getComponent() + ")");
                    atCP = false;
                }
                slowingDown = state.getPosition() >= positionToSlowDown;
                double dampening = getCurvatureDampening(getRobotReport().getPathIndex(), false);
                RungeKutta4.integrate(state, elapsedTrackingTime, deltaTime, slowingDown, maxVelocity, dampening, maxAcceleration);
            }

            onPositionUpdate();
            enqueueOneReport();

            int delay = trackingPeriodInMillis;
            if (NetworkConfiguration.getMaximumTxDelay() > 0) delay += rand.nextInt(NetworkConfiguration.getMaximumTxDelay());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long deltaTimeInMillis = Calendar.getInstance().getTimeInMillis() - timeStart;
            deltaTime = deltaTimeInMillis / this.temporalResolution;
            elapsedTrackingTime += deltaTime;
        }

        while (tec.getRobotReport(te.getRobotID()).getPathIndex() != -1) {
            enqueueOneReport();
            try {
                Thread.sleep(trackingPeriodInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long timerStart = getCurrentTimeInMillis();
        while (getCurrentTimeInMillis() - timerStart < WAIT_AMOUNT_AT_END) {
            try {
                Thread.sleep(trackingPeriodInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        metaCSPLogger.info("RK4 tracking thread terminates (Robot " + myRobotID + ", TrajectoryEnvelope " + myTEID + ")");
    }

    public void setUseInternalCriticalPoints(boolean value) {
        this.useInternalCPs = value;
    }

    private void computeInternalCriticalPoints() {
        this.curvatureDampening = new double[te.getTrajectory().getPose().length];
        this.curvatureDampening[0] = 1.0;
        Pose[] poses = this.trajectory.getPose();
        double prevTheta = poses[0].getTheta();
        if (poses.length > 1) prevTheta = Math.atan2(poses[1].getY() - poses[0].getY(), poses[1].getX() - poses[0].getX());
        for (int i = 0; i < poses.length - 1; i++) {
            double theta = Math.atan2(poses[i + 1].getY() - poses[i].getY(), poses[i + 1].getX() - poses[i].getX());
            double deltaTheta = (theta - prevTheta);
            prevTheta = theta;
            if (Math.abs(deltaTheta) > Math.PI / 2 && Math.abs(deltaTheta) < 1.9 * Math.PI) {
                internalCriticalPoints.add(i);
                metaCSPLogger.info("Found internal critical point (" + te.getComponent() + "): " + (i));
            }
            this.curvatureDampening[i + 1] = 1.0;
        }
    }

    public double getCurvatureDampening(int index, boolean backwards) {
        if (!backwards) return curvatureDampening[index];
        return curvatureDampening[this.trajectory.getPose().length - 1 - index];
    }

    @Override
    protected void onTrajectoryEnvelopeUpdate() {
        synchronized (reportsList) {
            this.totalDistance = this.computeDistance(0, trajectory.getPose().length - 1);
            this.overallDistance = totalDistance;
            this.internalCriticalPoints.clear();
            this.computeInternalCriticalPoints();
            this.slowDownProfile = this.getSlowdownProfile();
            this.positionToSlowDown = this.computePositionToSlowDown();
            reportsList.clear();
            reportTimeLists.clear();
        }
    }

    @Override
    public void startTracking() {
        while (this.th == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.th.start();
        if (useInternalCPs) this.startInternalCPThread();
    }

    public static double computeDistance(Trajectory traj, int startIndex, int endIndex) {
        double ret = 0.0;
        for (int i = startIndex; i < Math.min(endIndex, traj.getPoseSteering().length - 1); i++) {
            ret += traj.getPose()[i].distanceTo(traj.getPose()[i + 1]);
        }
        return ret;
    }

    private double computeDistance(int startIndex, int endIndex) {
        return computeDistance(this.trajectory, startIndex, endIndex);
    }

    private void enqueueOneReport() {
        synchronized (reportsList) {
            if (reportsList.isEmpty()) {
                if (getRobotReport() != null) {
                    reportsList.add(0, getRobotReport());
                    reportTimeLists.add(0, Calendar.getInstance().getTimeInMillis());
                }
                return;
            }

            long timeNow = Calendar.getInstance().getTimeInMillis();
            final int numberOfReplicasReceiving = this.numberOfReplicas;

            timeNow = Calendar.getInstance().getTimeInMillis();
            long timeOfArrival = timeNow;
            if (NetworkConfiguration.getMaximumTxDelay() > 0) {
                int delay = (NetworkConfiguration.getMaximumTxDelay() - NetworkConfiguration.getMinimumTxDelay() > 0) ? rand.nextInt(NetworkConfiguration.getMaximumTxDelay() - NetworkConfiguration.getMinimumTxDelay()) : 0;
                timeOfArrival = timeOfArrival + NetworkConfiguration.getMinimumTxDelay() + delay;
            }

            boolean received = !(NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS > 0);
            int trial = 0;
            while (!received && trial < numberOfReplicasReceiving) {
                if (rand.nextDouble() < (1 - NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS))
                    received = true;
                trial++;
            }
            if (received) {
                ArrayList<Long> reportTimeToRemove = new ArrayList<>();
                ArrayList<RobotReport> reportToRemove = new ArrayList<>();

                for (int index = 0; index < reportTimeLists.size(); index++) {
                    if (reportTimeLists.get(index) < timeOfArrival) break;
                    if (reportTimeLists.get(index) >= timeOfArrival) {
                        reportToRemove.add(reportsList.get(index));
                        reportTimeToRemove.add(reportTimeLists.get(index));
                    }
                }

                for (Long time : reportTimeToRemove) reportTimeLists.remove(time);
                for (RobotReport report : reportToRemove) reportsList.remove(report);

                reportsList.add(0, getRobotReport());
                reportTimeLists.add(0, timeOfArrival);
            }

            if (reportTimeLists.get(reportTimeLists.size() - 1) > timeNow) {
                metaCSPLogger.severe("* ERROR * Unknown status Robot" + te.getRobotID());
            } else {
                ArrayList<Long> reportTimeToRemove = new ArrayList<Long>();
                ArrayList<RobotReport> reportToRemove = new ArrayList<RobotReport>();

                for (int index = reportTimeLists.size() - 1; index > 0; index--) {
                    if (reportTimeLists.get(index) > timeNow) break;
                    if (reportTimeLists.get(index) < timeNow && reportTimeLists.get(index - 1) <= timeNow) {
                        reportToRemove.add(reportsList.get(index));
                        reportTimeToRemove.add(reportTimeLists.get(index));
                    }
                }

                for (Long time : reportTimeToRemove) reportTimeLists.remove(time);
                for (RobotReport report : reportToRemove) reportsList.remove(report);
            }

            if (timeNow - reportTimeLists.get(reportTimeLists.size() - 1) > tec.getControlPeriod() + TrajectoryEnvelopeCoordinator.MAX_TX_DELAY) {
                metaCSPLogger.severe("* ERROR * Status of Robot" + te.getRobotID() + " is too old.");
            }
        }
    }

    @Override
    public RobotReport getLastRobotReport() {
        synchronized (reportsList) {
            if (reportsList.isEmpty()) return getRobotReport();
            return reportsList.get(reportsList.size() - 1);
        }
    }

    private void startInternalCPThread() {
        Thread t = new Thread() {
            @Override
            public void run() {
                userCPReplacements = new HashMap<>();

                while (th.isAlive()) {
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (Integer i : internalCriticalPoints) {
                        if (getRobotReport().getPathIndex() >= i) {
                            toRemove.add(i);
                            setCriticalPoint(userCPReplacements.get(i));
                            metaCSPLogger.info("Restored critical point (" + te.getComponent() + "): " + userCPReplacements.get(i) + " which was masked by internal critical point " + i);
                            break;
                        } else {
                            if (criticalPoint == -1 || criticalPoint > i) {
                                userCPReplacements.put(i, criticalPoint);
                                metaCSPLogger.info("Set internal critical point (" + te.getComponent() + "): " + i + " replacing critical point " + criticalPoint);
                                setCriticalPoint(i);
                                break;
                            }
                        }
                    }
                    for (Integer i : toRemove) {
                        internalCriticalPoints.remove(i);
                    }

                    try {
                        Thread.sleep(trackingPeriodInMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private TreeMap<Double, Double> getSlowdownProfile() {
        TreeMap<Double, Double> ret = new TreeMap<Double, Double>(Collections.reverseOrder());
        State tempStateBW = new State(0.0, 0.0);
        ret.put(tempStateBW.getVelocity(), tempStateBW.getPosition());

        double time = 0.0;
        double deltaTime = 0.5 * (this.trackingPeriodInMillis / this.temporalResolution);

        while (tempStateBW.getVelocity() < maxVelocity * 1.1) {
            double dampeningBW = getCurvatureDampening(getRobotReport(tempStateBW).getPathIndex(), true);
            RungeKutta4.integrate(tempStateBW, time, deltaTime, false, maxVelocity * 1.1, dampeningBW, maxAcceleration);
            time += deltaTime;
            ret.put(tempStateBW.getVelocity(), tempStateBW.getPosition());
        }

        return ret;
    }

    private double computePositionToSlowDown() {
        State tempStateFW = new State(state.getPosition(), state.getVelocity());
        double time = 0.0;
        double deltaTime = 0.5 * (this.trackingPeriodInMillis / this.temporalResolution);

        while (tempStateFW.getPosition() < this.totalDistance) {
            double prevSpeed = -1.0;
            boolean firstTime = true;
            for (Double speed : this.slowDownProfile.keySet()) {
                if (tempStateFW.getVelocity() > speed) {
                    double landingPosition = tempStateFW.getPosition() + (firstTime ? 0.0 : slowDownProfile.get(prevSpeed));
                    if (landingPosition > totalDistance) {
                        return tempStateFW.getPosition();
                    }
                    break;
                }
                firstTime = false;
                prevSpeed = speed;
            }

            double dampeningFW = getCurvatureDampening(getRobotReport(tempStateFW).getPathIndex(), true);
            RungeKutta4.integrate(tempStateFW, time, deltaTime, false, maxVelocity, dampeningFW, maxAcceleration);

            time += deltaTime;
        }
        return -this.totalDistance;
    }

    @Override
    public void setCriticalPoint(int criticalPointToSet, int extCPCounter) {

        final int criticalPoint = criticalPointToSet;
        final int externalCPCount = extCPCounter;
        final int numberOfReplicas = this.numberOfReplicas;

        Thread waitToTXThread = new Thread("Wait to TX thread for robot " + te.getRobotID()) {
            public void run() {

                int delayTx = 0;
                if (NetworkConfiguration.getMaximumTxDelay() > 0) {
                    int delay = (NetworkConfiguration.getMaximumTxDelay() - NetworkConfiguration.getMinimumTxDelay() > 0) ? rand.nextInt(NetworkConfiguration.getMaximumTxDelay() - NetworkConfiguration.getMinimumTxDelay()) : 0;
                    delayTx = NetworkConfiguration.getMinimumTxDelay() + delay;
                }

                try {
                    Thread.sleep(delayTx);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (externalCPCounter) {
                    boolean send = false;
                    int trial = 0;
                    while (trial < numberOfReplicas) {
                        if (rand.nextDouble() < (1 - NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS))
                            send = true;
                        else {
                            TrajectoryEnvelopeCoordinatorSimulation tc = (TrajectoryEnvelopeCoordinatorSimulation) tec;
                            tc.incrementLostPacketsCounter();
                        }
                        trial++;
                    }
                    if (send) {
                        if (
                                (externalCPCount < externalCPCounter && externalCPCount - externalCPCounter > Integer.MAX_VALUE / 2.0) ||
                                        (externalCPCounter > externalCPCount && externalCPCounter - externalCPCount < Integer.MAX_VALUE / 2.0)) {
                            metaCSPLogger.info("Ignored critical point " + criticalPoint + " related to counter " + externalCPCount + " because counter is already at " + externalCPCounter + ".");
                        } else {
                            setCriticalPoint(criticalPoint);
                            externalCPCounter = externalCPCount;
                        }

                        if (!canStartTracking()) {
                            setCanStartTracking();
                        }
                    } else {
                        TrajectoryEnvelopeCoordinatorSimulation tc = (TrajectoryEnvelopeCoordinatorSimulation) tec;
                        tc.incrementLostMessagesCounter();
                        metaCSPLogger.info("PACKET to Robot" + te.getRobotID() + " LOST, criticalPoint: " + criticalPoint + ", externalCPCounter: " + externalCPCount);
                    }
                }
            }
        };
        waitToTXThread.start();
    }

    @Override
    public void setCriticalPoint(int criticalPointToSet) {

        if (this.criticalPoint != criticalPointToSet) {
            if (criticalPointToSet != -1 && criticalPointToSet > getRobotReport().getPathIndex()) {
                double totalDistanceBKP = this.totalDistance;
                int criticalPointBKP = this.criticalPoint;
                double positionToSlowDownBKP = this.positionToSlowDown;

                this.criticalPoint = criticalPointToSet;
                this.totalDistance = computeDistance(0, criticalPointToSet);
                this.positionToSlowDown = computePositionToSlowDown();

                if (this.positionToSlowDown < state.getPosition()) {
                    metaCSPLogger.warning("Ignored critical point (" + te.getComponent() + "): " + criticalPointToSet + " because slowdown distance (" + this.positionToSlowDown + ") < current distance (" + state.getPosition() + ")");
                    this.criticalPoint = criticalPointBKP;
                    this.totalDistance = totalDistanceBKP;
                    this.positionToSlowDown = positionToSlowDownBKP;
                } else {
                    metaCSPLogger.finest("Set critical point (" + te.getComponent() + "): " + criticalPointToSet + ", currently at point " + this.getRobotReport().getPathIndex() + ", distance " + state.getPosition() + ", will slow down at distance " + this.positionToSlowDown);
                }
            } else if (criticalPointToSet != -1 && criticalPointToSet <= getRobotReport().getPathIndex()) {
                metaCSPLogger.warning("Ignored critical point (" + te.getComponent() + "): " + criticalPointToSet + " because robot is already at " + getRobotReport().getPathIndex() + " (and current CP is " + this.criticalPoint + ")");
            } else if (criticalPointToSet == -1) {
                this.criticalPoint = criticalPointToSet;
                this.totalDistance = computeDistance(0, trajectory.getPose().length - 1);
                this.positionToSlowDown = computePositionToSlowDown();
                metaCSPLogger.finest("Set critical point (" + te.getComponent() + "): " + criticalPointToSet);
            }
        } else {
            metaCSPLogger.warning("Critical point (" + te.getComponent() + ") " + criticalPointToSet + " was already set!");
        }
    }

    @Override
    public RobotReport getRobotReport() {
        if (state == null) return null;
        if (!this.th.isAlive()) return new RobotReport(te.getRobotID(), trajectory.getPose()[0], -1, 0.0, 0.0, -1);
        synchronized (state) {
            Pose pose = null;
            int currentPathIndex = -1;
            double accumulatedDist = 0.0;
            Pose[] poses = trajectory.getPose();
            for (int i = 0; i < poses.length - 1; i++) {
                double deltaS = poses[i].distanceTo(poses[i + 1]);
                accumulatedDist += deltaS;
                if (accumulatedDist > state.getPosition()) {
                    double ratio = 1.0 - (accumulatedDist - state.getPosition()) / deltaS;
                    pose = poses[i].interpolate(poses[i + 1], ratio);
                    currentPathIndex = i;
                    break;
                }
            }
            if (currentPathIndex == -1) {
                currentPathIndex = poses.length - 1;
                pose = poses[currentPathIndex];
            }
            return new RobotReport(te.getRobotID(), pose, currentPathIndex, state.getVelocity(), state.getPosition(), this.criticalPoint);
        }
    }

    public RobotReport getRobotReport(State auxState) {
        if (auxState == null) return null;
        Pose pose = null;
        int currentPathIndex = -1;
        double accumulatedDist = 0.0;
        Pose[] poses = trajectory.getPose();
        for (int i = 0; i < poses.length - 1; i++) {
            double deltaS = poses[i].distanceTo(poses[i + 1]);
            accumulatedDist += deltaS;
            if (accumulatedDist > auxState.getPosition()) {
                double ratio = 1.0 - (accumulatedDist - auxState.getPosition()) / deltaS;
                pose = poses[i].interpolate(poses[i + 1], ratio);
                currentPathIndex = i;
                break;
            }
        }
        if (currentPathIndex == -1) {
            currentPathIndex = poses.length - 1;
            pose = poses[currentPathIndex];
        }
        return new RobotReport(te.getRobotID(), pose, currentPathIndex, auxState.getVelocity(), auxState.getPosition(), -1);
    }
}
