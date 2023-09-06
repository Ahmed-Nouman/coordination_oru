package se.oru.coordination.coordination_oru.tests;

import java.awt.Color;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;

import se.oru.coordination.coordination_oru.code.*;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation2D.EmergencyBreaker;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeTrackerRK4;
import se.oru.coordination.coordination_oru.util.*;
import se.oru.coordination.coordination_oru.util.gates.GatedThread;

import static se.oru.coordination.coordination_oru.util.Printer.print;


public class GridTest {
    enum Scenario {
        BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST_COL1,
        BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST,
        BASELINE_IDEAL_DRIVER_HUMAN_FIRST,
        BASELINE_IDEAL_DRIVER_FIRST_COME,

        FORCING_CS1_PRIORITIES_CHANGE,
        FORCING_CS1_WITH_STOPS,

        FORCING_CS1_CS2_PRIORITIES_CHANGE,
        FORCING_CS1_CS2_WITH_STOPS,

        FORCING_UPCOMING_PRIORITIES_CHANGE,
        FORCING_UPCOMING_WITH_STOPS,

        FORCING_GLOBAL_STOP,
        // The same as FORCING_UPCOMING_WITH_STOPS, but other robots stop even they have already passed,
        // the intersection (so there are no critical sections for them to stop before).

        FORCING_GLOBAL_STOP_11,
        FORCING_GLOBAL_STOP_12,
        FORCING_UPCOMING_PRIORITIES_CHANGE_21,
        // FORCING_UPCOMING_PRIORITIES_CHANGE_22, // would be the same as BASELINE_IDEAL_DRIVER_HUMAN_FIRST
    }

    public static void main(String[] args) {
        Printer.resetTime();
        print("started");

        BrowserVisualization.isStatusText = true;
        GatedThread.enable();


        new GatedThread("runDemo") {
            @Override
            public void runCore() {
                try {
                    runDemo();
                } catch (NoPathFound e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

        GatedThread.runGatekeeper();
    }

    protected static void runDemo() throws NoPathFound {
        final String scenarioString = System.getenv().get("SCENARIO");
        final Scenario scenario = scenarioString == null ? Scenario.FORCING_GLOBAL_STOP_12 :
                Scenario.valueOf(scenarioString);

        AbstractVehicle.scenarioId = String.valueOf(scenario);

        final double loopMinutes = 60;
        final long loopTime = System.currentTimeMillis() + Math.round(loopMinutes * 60 * 1000);

        final String YAML_FILE = "maps/map-grid.yaml";

        final double xLeft = 4.0;
        final double xRight = 56.0;
        final double yTop = 56.5;
        final double yBottom = 3.0;

        final double xColumn1 = 14.5;
        final double xColumn2 = 30.0;
        final double xColumn3 = 45.7;
        final double yRow1 = 44.0;
        final double yRow2 = 30.0;
        final double yRow3 = 15.5;

        final double thetaDown = -Math.PI/2;
        final double thetaUp = Math.PI/2;
        final double thetaRight = 0;
        final double thetaLeft = Math.PI;

        final Pose column1Top = new Pose(xColumn1, yTop, thetaDown);
        final Pose column2Top = new Pose(xColumn2, yTop, thetaDown);
        final Pose column3Top = new Pose(xColumn3, yTop, thetaDown);

        final Pose column1Bottom = new Pose(xColumn1, yBottom, thetaUp);
        final Pose column2Bottom = new Pose(xColumn2, yBottom, thetaLeft); // left is for the robot 0 to look down
        final Pose column3Bottom = new Pose(xColumn3, yBottom, thetaUp);

        final Pose row1Left = new Pose(xLeft, yRow1, thetaRight);
        final Pose row2Left = new Pose(xLeft, yRow2, thetaRight);
        final Pose row3Left = new Pose(xLeft, yRow3, thetaRight);

        final Pose row1Right = new Pose(xRight, yRow1, thetaLeft);
        final Pose row2Right = new Pose(xRight, yRow2, thetaLeft);
        final Pose row3Right = new Pose(xRight, yRow3, thetaLeft);

        final Pose centerDownward = new Pose(xColumn2, yRow2, thetaDown);

        final Pose humStart = scenario == Scenario.BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST_COL1 ? column1Top : column2Top;
        final Pose humFinish = scenario == Scenario.BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST_COL1 ? column2Bottom : column2Bottom;

        final boolean ishumLoop = true;

        final Pose aut1Start = row1Left;
        final Pose aut1Finish = row1Right;

        final Pose aut2Start = row2Left;
        final Pose aut2Finish = row2Right;

        final Pose aut3Start = row3Left;
        final Pose aut3Finish = row3Right;

        final Pose aut4Start = column1Top;
        final Pose aut4Finish = column1Bottom;

        final Pose aut5Start = row3Left;
        final Pose aut5Finish = row1Right;

        final double precisionCoefficient = 1;
        final double maxVelocity = 5.0 * precisionCoefficient;
        final double maxAcceleration = 2.0 * precisionCoefficient;
        final int trackingPeriod = (int) Math.round(100 / precisionCoefficient);

        double xLength = 2.5;
        double yLength = 1.5;
        double xLengthInner = 1.5;
        double yLengthInner = 1.0;

        MissionUtils.targetVelocityHumanInitial = maxVelocity;
        MissionUtils.targetVelocityHuman = maxVelocity;

        AutonomousVehicle.planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect; // default
        //AutonomousVehicle.planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.PRMstar; // too slow
        //AutonomousVehicle.planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.SPARS; // too slow

        AutonomousVehicle aut1 = null;
        AutonomousVehicle aut2 = null;
        AutonomousVehicle aut3 = null;
        AutonomousVehicle aut4 = null;
        AutonomousVehicle aut5 = null;

        // TODO: `maxAcceleration` passed here is not used by `tec`.
        AutonomousVehicle hum0 = new HumanDrivenVehicle(0, Color.GREEN, Color.BLUE, maxVelocity, maxAcceleration, xLength, yLength);
        aut1 = new AutonomousVehicle(1, 0, Color.YELLOW, Color.YELLOW, maxVelocity, maxAcceleration, xLength, yLength);
        aut2 = new AutonomousVehicle(2, 0, Color.YELLOW, Color.YELLOW, maxVelocity, maxAcceleration, xLength, yLength);
        aut3 = new AutonomousVehicle(3, 0, Color.YELLOW, Color.YELLOW, maxVelocity, maxAcceleration, xLength, yLength);
        //aut4 = new AutonomousVehicle(4, 0, Color.YELLOW, Color.YELLOW, maxVelocity, maxAcceleration, xLength, yLength);
        //aut5 = new AutonomousVehicle(5, 0, Color.YELLOW, Color.YELLOW, maxVelocity, maxAcceleration, xLength, yLength);

        TrajectoryEnvelopeTrackerRK4.emergencyBreaker = new EmergencyBreaker(false, false);

        TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, maxVelocity, maxAcceleration, trackingPeriod);
        tec.setupSolver(0, 100000000);
        tec.startInference();

        Coordinate[] innerFootprint = AbstractVehicle.makeFootprint(xLengthInner, yLengthInner);
        for (AbstractVehicle vehicle : new AbstractVehicle[] { hum0, aut1, aut2, aut3, aut4, aut5 }) {
            if (vehicle != null) {
                vehicle.innerFootprint = innerFootprint;
                tec.setFootprint(vehicle.getID(), vehicle.getFootprint());
                tec.setInnerFootprint(vehicle.getID(), vehicle.innerFootprint);
            }
        }

        tec.placeRobot(hum0.getID(), humStart);
        if (aut1 != null) tec.placeRobot(aut1.getID(), aut1Start);
        if (aut2 != null) tec.placeRobot(aut2.getID(), aut2Start);
        if (aut3 != null) tec.placeRobot(aut3.getID(), aut3Start);
        if (aut4 != null) tec.placeRobot(aut4.getID(), aut4Start);
        if (aut5 != null) tec.placeRobot(aut5.getID(), aut5Start);

        Heuristics heuristics = new Heuristics();
        switch (scenario) {
            case BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST_COL1:
            case BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST:
            default:
                tec.addComparator(heuristics.highestIDNumber());
                break;
            case BASELINE_IDEAL_DRIVER_HUMAN_FIRST:
                tec.addComparator(heuristics.lowestIDNumber());
                break;
            case BASELINE_IDEAL_DRIVER_FIRST_COME:
                tec.addComparator(heuristics.closest());
                break;
        }

        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setInitialTransform(7.0, 5.0, 5.0);
        tec.setVisualization(viz);

        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec, loopTime);
        Missions.loopMissions.put(hum0.getID(), ishumLoop);

        final boolean isInverse = false;
        Missions.enqueueMissions(hum0, humStart, humFinish, isInverse);
        if (aut1 != null) Missions.enqueueMissions(aut1, aut1Start, aut1Finish, isInverse);
        if (aut2 != null) Missions.enqueueMissions(aut2, aut2Start, aut2Finish, isInverse);
        if (aut3 != null) Missions.enqueueMissions(aut3, aut3Start, aut3Finish, isInverse);
        if (aut4 != null) Missions.enqueueMissions(aut4, aut4Start, aut4Finish, isInverse);
        if (aut5 != null) Missions.enqueueMissions(aut5, aut5Start, aut5Finish, isInverse);

        AutonomousVehicle finalAut1 = aut1;
        new GatedThread("scenario creator") {
            @Override
            public void runCore() {
                boolean isForcing = true;
                assert(MissionUtils.priorityDistance == Double.NEGATIVE_INFINITY);
                assert(MissionUtils.stopDistance == Double.NEGATIVE_INFINITY);
                assert(! MissionUtils.isGlobalTemporaryStop);
                assert(MissionUtils.isRestorePrioritiesAfterTheNearestIntersection);

                double[] ysDownwardsForcing = new double[] { humStart.getY() - 6.5 };
                double[] ysDownwardsResumingRobots = new double[] { humStart.getY() - 16.5 };
                double[] ysDownwardsRestoringPriorities = new double[] {};

                double[] ysUpwardsForcing = new double[] {};
                double[] ysUpwardsResumingRobots = new double[] {};
                double[] ysUpwardsRestoringPriorities = new double[] {};

                switch (scenario) {
                    case BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST_COL1:
                    case BASELINE_IDEAL_DRIVER_AUTOMATED_FIRST:
                    case BASELINE_IDEAL_DRIVER_HUMAN_FIRST:
                    case BASELINE_IDEAL_DRIVER_FIRST_COME:
                        isForcing = false;
                        // For manual use:
                        MissionUtils.priorityDistance = 10.0;
                        MissionUtils.stopDistance = 10.0;
                        break;

                    case FORCING_CS1_PRIORITIES_CHANGE:
                        MissionUtils.priorityDistance = 10.0;
                        break;
                    case FORCING_CS1_WITH_STOPS:
                        MissionUtils.priorityDistance = 10.0;
                        MissionUtils.stopDistance = 10.0;
                        break;

                    case FORCING_CS1_CS2_PRIORITIES_CHANGE:
                        MissionUtils.priorityDistance = 20.0;
                        break;
                    case FORCING_CS1_CS2_WITH_STOPS:
                        MissionUtils.priorityDistance = 20.0;
                        MissionUtils.stopDistance = 20.0;
                        break;

                    case FORCING_UPCOMING_PRIORITIES_CHANGE:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        break;
                    case FORCING_UPCOMING_WITH_STOPS:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        MissionUtils.stopDistance = Double.POSITIVE_INFINITY;
                        break;
                    case FORCING_GLOBAL_STOP:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        MissionUtils.isGlobalTemporaryStop = true;
                        break;

                    case FORCING_GLOBAL_STOP_11:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        MissionUtils.isGlobalTemporaryStop = true;
                        MissionUtils.isRestorePrioritiesAfterTheNearestIntersection = false;
                        ysDownwardsResumingRobots = ysDownwardsRestoringPriorities = new double[] { humFinish.getY() };

                        ysDownwardsForcing = new double[] { humStart.getY() - 4.0 }; // TODO: temporary
                        break;

                    case FORCING_GLOBAL_STOP_12:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        MissionUtils.isGlobalTemporaryStop = true;
                        MissionUtils.isRestorePrioritiesAfterTheNearestIntersection = false;
                        ysDownwardsResumingRobots = ysDownwardsRestoringPriorities = new double[] { humFinish.getY() };
                        ysUpwardsForcing = new double[] { humFinish.getY() + 4.0 };
                        ysUpwardsResumingRobots = ysUpwardsRestoringPriorities = new double[] { humStart.getY() };

                        ysDownwardsForcing = new double[] { humStart.getY() - 4.0 }; // TODO: temporary
                        break;

                    case FORCING_UPCOMING_PRIORITIES_CHANGE_21:
                        MissionUtils.priorityDistance = Double.POSITIVE_INFINITY;
                        MissionUtils.isRestorePrioritiesAfterTheNearestIntersection = false;
                        ysDownwardsResumingRobots = new double[] {};
                        ysDownwardsRestoringPriorities = new double[] { humFinish.getY() };

                        ysDownwardsForcing = new double[] { humStart.getY() - 4.0 }; // TODO: temporary
                        break;
                        
                    default:
                        throw new RuntimeException(String.valueOf(scenario));
                }

                if (! isForcing) {
                    return;
                }

                KnobsAfterForcing knobsAfterForcing = null;
                while (true) {
                    boolean isForcingNow = false;
                    boolean isResumingNow = false;
                    boolean isRestoringNow = false;

                    for (double y : ysDownwardsForcing) {
                        if (hum0.isYPassedDownwards(y)) {
                            isForcingNow = true;
                        }
                    }
                    for (double y : ysUpwardsForcing) {
                        if (hum0.isYPassedUpwards(y)) {
                            isForcingNow = true;
                        }
                    }

                    for (double y : ysDownwardsResumingRobots) {
                        if (hum0.isYPassedDownwards(y)) {
                            isResumingNow = true;
                        }
                    }
                    for (double y : ysUpwardsResumingRobots) {
                        if (hum0.isYPassedUpwards(y)) {
                            isResumingNow = true;
                        }
                    }

                    for (double y : ysDownwardsRestoringPriorities) {
                        if (hum0.isYPassedDownwards(y)) {
                            isRestoringNow = true;
                        }
                    }
                    for (double y : ysUpwardsRestoringPriorities) {
                        if (hum0.isYPassedUpwards(y)) {
                            isRestoringNow = true;
                        }
                    }

                    if (isForcingNow) {
                        assert ! isResumingNow;
                        assert ! isRestoringNow;
                    }

                    if (isForcingNow) {
                        knobsAfterForcing = MissionUtils.forceDriving(hum0.getID());
                    }
                    if (isResumingNow) {
                        knobsAfterForcing.resumeRobots();
                    }
                    if (isRestoringNow) {
                        knobsAfterForcing.restorePriorities();
                    }

                    GatedThread.skipCycles(1);
                }
            }
        }.start();
    }
}
