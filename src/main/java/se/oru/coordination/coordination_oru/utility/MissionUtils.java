package se.oru.coordination.coordination_oru.utility;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.robots.RobotHashMap;
import se.oru.coordination.coordination_oru.simulator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.utility.gates.GatedThread;

public class MissionUtils {
    public static final double targetVelocityHumanInitial = 0.1;
    public static double targetVelocityHuman = targetVelocityHumanInitial;
    public static int idHuman = 0;
    public static boolean isWorking = false;

    // TODO: race condition (click/keypress)
    // TODO: crashes on click and then (immediately) keypress
    // TODO: crashes on large initial velocity

    protected static Object pathLock = new Object(); // sentinel

    protected static void removeMissions(int robotID) {
        while (true) {
            var mission = Missions.dequeueMission(robotID);
            if (mission == null) {
                break;
            }
        }
    }

    public static void moveRobot(int robotID, Pose goal) {
        if (isWorking) {
            return;
        }
        isWorking = true;
        try {
            synchronized (pathLock) {
                waitUntilScheduledMissionStarts(robotID); // TODO: Fix the hack.

                TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
                Object stateOrNothingAsLock = getRobotState(robotID);
                if (stateOrNothingAsLock == null) {
                    stateOrNothingAsLock = new Object();
                }

                //synchronized (stateOrNothingAsLock) { // seems like a deadlock with the tracker
                RobotReport rr = tec.getRobotReport(robotID);
                Pose currentPose = rr.getPose();
                var robot = RobotHashMap.getRobot(robotID);

                PoseSteering[] newPath = null;
                try {
                    robot.getPlan(currentPose, new Pose[]{goal}, Missions.getMapYAMLFilename(), false);
                } catch (Error exc) { // TODO: check for NoPathFound only
                    System.out.println("moveRobot: no path found (or another error): " + exc);
                    return;
                }

                PoseSteering[] currentPath = getCurrentPath(robotID);
                if (currentPath == null || rr.getPathIndex() == -1) {
                    targetVelocityHuman = targetVelocityHumanInitial;
                    Missions.enqueueMission(new Mission(robotID, newPath));
                } else {
                    int replacementIndex = getReplacementIndex(robotID);
                    PoseSteering[] replacementPath = computeReplacementPath(currentPath, replacementIndex, newPath);
                    MissionUtils.changePath(robotID, replacementPath, replacementIndex);
                }
            }
        } finally {
            isWorking = false;
        }
    }

    protected static void waitUntilScheduledMissionStarts(int robotID) {
        TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;

        // If there is a scheduled mission, wait until it starts.
        try {
            while (!tec.isMissionsPoolEmpty() || Missions.hasMissions(robotID))
                GatedThread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static State getRobotState(int robotID) {
        TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
        AbstractTrajectoryEnvelopeTracker tracker = tec.getTracker(robotID);
        if (tracker == null) {
            return null;
        }
        return tracker.getState();
    }

    // 0.1->1.1: OK
    // 0.1->1.1->0.1: OK
    // 0.1->1.1->2.1: breaks
    // 0.1->1.1->0.1->1.1: breaks
    public static void changeTargetVelocityHuman(double delta) {
        if (isWorking) {
            return;
        }
        isWorking = true;
        try {
            // TODO: sometimes doesn't get called
            // ("hint": try to pause the websocket thread in the debugger)
            int robotID = idHuman;
            double targetVelocityNew = targetVelocityHuman + delta;
            if (targetVelocityNew > 0) {
                targetVelocityHuman = targetVelocityNew;

                synchronized (pathLock) {
                    PoseSteering[] currentPath = getCurrentPath(robotID);
                    if (currentPath != null) {
                        changePath(robotID, currentPath, getReplacementIndex(robotID));
                    }
                }
            }
        } finally {
            isWorking = false;
        }
    }

    protected static PoseSteering[] getCurrentPath(int robotID) {
        TrajectoryEnvelope te = TrajectoryEnvelopeCoordinatorSimulation.tec.getCurrentTrajectoryEnvelope(robotID);
        if (te == null) {
            return null;
        }
        return te.getSpatialEnvelope().getPath();
    }

    protected static int getReplacementIndex(int robotID) {
        TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
        RobotReport rr = tec.getRobotReport(robotID);
        return Math.max(0, rr.getPathIndex() + 2);
        // TODO: why does `- 10` work better than `+ 10`?
    }

    protected static void changePath(int robotID, PoseSteering[] replacementPath, int replacementIndex) {
        TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;

        tec.replacePath(robotID, replacementPath, replacementIndex, false, null);

        int replacementIndexNew = getReplacementIndex(robotID);
        System.out.println("changePath1: replacementIndex: " + replacementIndex + " -> " + replacementIndexNew);
        // TODO: It changes significantly sometimes.
    }

    public static PoseSteering[] computeReplacementPath(PoseSteering[] initialPath, int replacementIndex, PoseSteering[] newPath) {
        replacementIndex = Math.min(replacementIndex, initialPath.length - 1); // TODO
        PoseSteering[] replacementPath = new PoseSteering[replacementIndex + newPath.length];
        // TODO: replacementIndex: off by one error? (replacementIndex=2 -> preserve 3 points)
        if (replacementIndex >= 0) System.arraycopy(initialPath, 0, replacementPath, 0, replacementIndex);
        System.arraycopy(newPath, 0, replacementPath, replacementIndex, newPath.length);
        return replacementPath;
    }
}
