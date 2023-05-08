package se.oru.coordination.coordination_oru.util;

import edu.uci.ics.jung.algorithms.importance.AbstractRanker;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.*;
import se.oru.coordination.coordination_oru.code.Heuristics;
import se.oru.coordination.coordination_oru.code.VehiclesHashMap;
import se.oru.coordination.coordination_oru.simulation2D.State;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.gates.GatedThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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
                //waitUntilScheduledMissionStarts(robotID);

                TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
                Object stateOrNothingAsLock = getRobotState(robotID);
                if (stateOrNothingAsLock == null) {
                    stateOrNothingAsLock = new Object();
                }

                //synchronized (stateOrNothingAsLock) { // seems like a deadlock with the tracker
                RobotReport rr = tec.getRobotReport(robotID);
                Pose currentPose = rr.getPose();
                var vehicle = VehiclesHashMap.getVehicle(robotID);

                PoseSteering[] newPath = null;
                try {
                    newPath = vehicle.getPlan(currentPose, new Pose[]{goal}, Missions.getMapYAMLFilename(), false);
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
            while (! tec.isMissionsPoolEmpty() || Missions.hasMissions(robotID))
                GatedThread.sleep(50);
        }
        catch (InterruptedException e) {
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
        }
        finally {
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
        for (int i = 0; i < replacementIndex; i++) replacementPath[i] = initialPath[i];
        for (int i = 0; i < newPath.length; i++) replacementPath[i + replacementIndex] = newPath[i];
        return replacementPath;
    }

    public static void forceDriving(int robotID) {
        TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;

        tec.resetComparators();
        tec.addComparator(new Heuristics().lowestIDNumber());
        tec.updateDependencies();

        final ArrayList<CriticalSection> criticalSectionsWithHighPriority = findCriticalSectionsWithHighPriority(robotID, tec.allCriticalSections);

        TrackingCallback cb = new TrackingCallback(null) {

            @Override
            public void onTrackingStart() { }

            @Override
            public void onTrackingFinished() { }

            @Override
            public String[] onPositionUpdate() {
                if (criticalSectionsWithHighPriority.isEmpty()) {
                    return null;
                }

                var cs = tec.allCriticalSections;
                if (areAllCriticalSectionsWithHighPriorityGone(tec.allCriticalSections, criticalSectionsWithHighPriority)) {
                    tec.resetComparators();
                    tec.addComparator(new Heuristics().highestIDNumber());
                    criticalSectionsWithHighPriority.clear();
                }
                return null;
            }

            @Override
            public void onNewGroundEnvelope() { }

            @Override
            public void beforeTrackingStart() { }

            @Override
            public void beforeTrackingFinished() { }
        };

        tec.addTrackingCallback(robotID, cb);
    }

    protected static ArrayList<CriticalSection> findCriticalSectionsWithHighPriority(
            int robotID,
            HashSet<CriticalSection> allCriticalSections) {
        ArrayList<CriticalSection> criticalSectionsSorted =
                CriticalSection.sortCriticalSectionsForRobotID(allCriticalSections, robotID);

        assert robotID != -1;
        for (CriticalSection cs : criticalSectionsSorted) {
            int id1 = cs.getTe1() == null ? -1 : cs.getTe1().getRobotID();
            int id2 = cs.getTe2() == null ? -1 : cs.getTe2().getRobotID();
            assert id1 == robotID || id2 == robotID;
            return new ArrayList<>(Arrays.asList(cs));
        }
        return new ArrayList<>();
    }

    protected static boolean areAllCriticalSectionsWithHighPriorityGone(
            HashSet<CriticalSection> allCriticalSections,
            ArrayList<CriticalSection> criticalSectionsWithHighPriority) {
        for (CriticalSection cs : criticalSectionsWithHighPriority) {
            if (allCriticalSections.contains(cs)) {
                return false;
            }
        }
        return true;
    }
}