package se.oru.coordination.coordination_oru.tests;

import java.awt.Color;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;

import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.RobotReport;
import se.oru.coordination.coordination_oru.code.*;
import se.oru.coordination.coordination_oru.simulation2D.EmergencyBreaker;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeTrackerRK4;
import se.oru.coordination.coordination_oru.util.*;
import se.oru.coordination.coordination_oru.util.gates.GatedThread;

import static se.oru.coordination.coordination_oru.util.Printer.print;


public class GridTest {
    enum Scenario {
        BASELINE_IDEAL_DRIVER,
        FORCING,
    }

    public static void main(String[] args) {
        Printer.resetTime();
        print("started");

        BrowserVisualization.isStatusText = true;
        GatedThread.enable();

        Scenario scenario = Scenario.BASELINE_IDEAL_DRIVER;
        AbstractVehicle.scenarioId = String.valueOf(scenario);

        new GatedThread("runDemo") {
            @Override
            public void runCore() {
                try {
                    runDemo(scenario);
                } catch (NoPathFound e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

        GatedThread.runGatekeeper();
    }

    protected static void runDemo(Scenario scenario) throws NoPathFound {
        final double loopMinutes = 1;
        final long loopTime = System.currentTimeMillis() + Math.round(loopMinutes * 60 * 1000);

        final String YAML_FILE = "maps/map-grid.yaml";

        final Pose column1Top = new Pose(14.5,57.4, -Math.PI/2);
        final Pose column2Top = new Pose(30.0,57.4, -Math.PI/2);
        final Pose column3Top = new Pose(45.7,57.4,-Math.PI/2);
        final Pose column1Bottom = new Pose(14.5,3.0, -Math.PI);
        final Pose column2Bottom = new Pose(30.0,3.0, Math.PI);
        final Pose column3Bottom = new Pose(45.7,3.0,-Math.PI/2);
        final Pose row1Left = new Pose(4.0,44.0,-Math.PI/2);
        final Pose row2Left = new Pose(4.0,30.0,-Math.PI/2);
        final Pose row3Left = new Pose(4.0,15.5,-Math.PI/2);
        final Pose row1Right = new Pose(57.0,44.0,-Math.PI/2);
        final Pose row2Right = new Pose(52.0,30.0,-Math.PI/2);
        final Pose row3Right = new Pose(57.0,15.5,-Math.PI/2);
        final Pose center = new Pose(30.0,30.0,-Math.PI/2);

        final Pose humStart = column2Top;
        final Pose humFinish = column2Bottom;

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

        AutonomousVehicle aut1 = null;
        AutonomousVehicle aut2 = null;
        AutonomousVehicle aut3 = null;
        AutonomousVehicle aut4 = null;
        AutonomousVehicle aut5 = null;

        double xLength = 2.0;
        double yLength = 1.5;
        double xLengthInner = 1.5;
        double yLengthInner = 1.0;

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
        tec.addComparator(heuristics.highestIDNumber());

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

        boolean isForcing;
        switch (scenario) {
            case BASELINE_IDEAL_DRIVER:
                isForcing = false;
                break;
            case FORCING:
                isForcing = true;
                break;
            default:
                throw new RuntimeException(String.valueOf(scenario));
        };

        if (isForcing) {
            AutonomousVehicle finalAut1 = aut1;

            new GatedThread("new mission") {
                @Override
                public void runCore() {
                    TrajectoryEnvelopeCoordinatorSimulation tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
                    // wait until `hum1` gives way to `aut1`
                    while (true) {
                        RobotReport rr = tec.getRobotReport(finalAut1.getID());
                        if (rr.getPathIndex() >= 200) {
                            break;
                        }
                        GatedThread.skipCycles(1);
                    }

                    Printer.print("calling forceDriving");
                    MissionUtils.forceDriving(hum0.getID());
                }
            }.start();
        }
    }
}
