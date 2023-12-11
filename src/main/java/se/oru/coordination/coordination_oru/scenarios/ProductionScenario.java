package se.oru.coordination.coordination_oru.scenarios;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;
import java.io.FileNotFoundException;

public class ProductionScenario {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        String absolutePath = System.getProperty("user.dir");
        String resultsDirectory = absolutePath + "/src/main/java/se/oru/coordination/coordination_oru/results/lookAheadPaper_2023";
        final String YAML_FILE = "maps/map_production_scenario.yaml";
        double drillLookAheadDistance = 30;
        int intervalInSeconds = 1;
        int terminationInMinutes = 15;
        int numOfCallsForLookAheadRobot = 5;
        boolean visualization = true;
        boolean writeRobotReports = false;

        final Pose mainTunnelLeft = new Pose(4.05, 42.95, Math.PI);
        final Pose mainTunnelRight = new Pose(120.55, 40.75, Math.PI);
        final Pose drawPoint1 = new Pose(33.05, 77.35, -Math.PI / 2);
        final Pose drawPoint2 = new Pose(45.85, 77.35, -Math.PI / 2);
        final Pose drawPoint3 = new Pose(56.85, 77.35, -Math.PI / 2);
        final Pose drawPoint4 = new Pose(69.15, 77.35, -Math.PI / 2);
        final Pose drillPoint = new Pose(120.85, 78.65, -Math.PI / 2);
        final Pose drillPoint2 = new Pose(19.25, 9.75, -Math.PI / 2);
        final Pose orePass1 = new Pose(39.15, 9.95, -Math.PI / 2.7);
        final Pose orePass2 = new Pose(45.75, 53.25, -Math.PI / 2);
        final Pose orePass3 = new Pose(62.95, 9.75, -Math.PI / 2);
        final Pose orePass4 = new Pose(69.15, 53.15, -Math.PI / 2);

        var autonomousRobot1 = new AutonomousVehicle("A1", 2, Color.YELLOW, 5, 2, 1000, 2,
                1, drawPoint1, new Pose[] {orePass1}, 0, 0);
        var autonomousRobot2 = new AutonomousVehicle("A2",2, Color.YELLOW, 5, 2, 1000, 2,
                1, drawPoint2, new Pose[] {orePass2}, 0, 0);
        var autonomousRobot3 = new AutonomousVehicle("A3", 2, Color.YELLOW, 5, 2, 1000, 2,
                1, drawPoint3, new Pose[] {orePass3}, 0, 0);
        var autonomousRobot4 = new AutonomousVehicle("A4", 2, Color.YELLOW, 5, 2, 1000, 2,
                1, drawPoint4, new Pose[] {orePass4}, 0, 0);
        var autonomousRobot5 = new AutonomousVehicle("A5", 1, Color.RED, 0.05, 0.02, 1000,
                3.5, 3.5, mainTunnelLeft, new Pose[] {mainTunnelRight}, 0, 0);
        var drillRig = new LookAheadVehicle("drillRig", drillLookAheadDistance, 2, Color.GREEN, 5,
                2, 1000, 2, 1, drillPoint, new Pose[] {drawPoint2}, 0, 0);

        autonomousRobot1.getPlan(autonomousRobot1.getInitialPose(), autonomousRobot1.getGoalPoses(),
                YAML_FILE, true);
        autonomousRobot2.getPlan(autonomousRobot2.getInitialPose(), autonomousRobot2.getGoalPoses(),
                YAML_FILE, true);
        autonomousRobot3.getPlan(autonomousRobot3.getInitialPose(), autonomousRobot3.getGoalPoses(),
                YAML_FILE, true);
        autonomousRobot4.getPlan(autonomousRobot4.getInitialPose(), autonomousRobot4.getGoalPoses(),
                YAML_FILE, true);
        autonomousRobot5.getPlan(autonomousRobot5.getInitialPose(), autonomousRobot5.getGoalPoses(),
                YAML_FILE, true);
        drillRig.getPlan(drillRig.getInitialPose(), drillRig.getGoalPoses(), YAML_FILE, false);

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setForwardModel(autonomousRobot1.getID(), new ConstantAccelerationForwardModel(autonomousRobot1.getMaxAcceleration(),
                autonomousRobot1.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot1.getID())));
        tec.setForwardModel(autonomousRobot2.getID(), new ConstantAccelerationForwardModel(autonomousRobot2.getMaxAcceleration(),
                autonomousRobot2.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot2.getID())));
        tec.setForwardModel(autonomousRobot3.getID(), new ConstantAccelerationForwardModel(autonomousRobot3.getMaxAcceleration(),
                autonomousRobot3.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot3.getID())));
        tec.setForwardModel(autonomousRobot4.getID(), new ConstantAccelerationForwardModel(autonomousRobot4.getMaxAcceleration(),
                autonomousRobot4.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot4.getID())));
        tec.setForwardModel(autonomousRobot5.getID(), new ConstantAccelerationForwardModel(autonomousRobot5.getMaxAcceleration(),
                autonomousRobot5.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot5.getID())));

        Coordinate[] fp5 = new Coordinate[]{
                new Coordinate(-4.5, 4.5),
                new Coordinate(4.5, 4.5),
                new Coordinate(4.5, -4.5),
                new Coordinate(-4.5, -4.5)
        };
        tec.setDefaultFootprint(drillRig.getFootprint());
        tec.setFootprint(autonomousRobot5.getID(), fp5);
        tec.placeRobot(autonomousRobot1.getID(), autonomousRobot1.getInitialPose());
        tec.placeRobot(autonomousRobot2.getID(), autonomousRobot2.getInitialPose());
        tec.placeRobot(autonomousRobot3.getID(), autonomousRobot3.getInitialPose());
        tec.placeRobot(autonomousRobot4.getID(), autonomousRobot4.getInitialPose());
        tec.placeRobot(autonomousRobot5.getID(), autonomousRobot5.getInitialPose());
        tec.placeRobot(drillRig.getID(), drillRig.getInitialPose());

        // Set Heuristics
        var heuristic = new Heuristics();
        tec.addComparator(heuristic.mostDistanceToTravel());
        String heuristicName = heuristic.getName();

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(0.5);
        viz.setInitialTransform(9.6, 30.2, -0.73);
        tec.setVisualization(viz);

        var m1 = new Mission(autonomousRobot1.getID(), autonomousRobot1.getPath());
        var m2 = new Mission(autonomousRobot2.getID(), autonomousRobot2.getPath());
        var m3 = new Mission(autonomousRobot3.getID(), autonomousRobot3.getPath());
        var m4 = new Mission(autonomousRobot4.getID(), autonomousRobot4.getPath());
        var m5 = new Mission(autonomousRobot5.getID(), autonomousRobot5.getPath());
        var m6 = new Mission(drillRig.getID(), drillRig.getLimitedPath(drillRig.getID(), drillLookAheadDistance, tec));
//        m4.setStoppingPoint(orePass3, 10000); //FIXME I think it does not work.

//        var randomRobotCaller = new RandomRobotCaller(numOfCallsForLookAheadRobot, terminationInMinutes);
//        randomRobotCaller.scheduleRandomCalls(m4);

        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
        Missions.enqueueMission(m3);
        Missions.enqueueMission(m4);
        Missions.enqueueMission(m5);
        Missions.enqueueMission(m6);
        Missions.setMap(YAML_FILE);

//        Missions.startMissionDispatchers(tec, drillLookAheadDistance, writeRobotReports,
//                intervalInSeconds, terminationInMinutes, heuristicName, resultsDirectory);

//        Thread.sleep(10000);
//        tec.placeRobot(drillRig.getID(), mainTunnelLeft);
//        drillRig.getPlan(mainTunnelLeft, drillRigGoal, YAML_FILE, false);
//        PoseSteering[] drillInitialPath = drillRig.getLimitedPath(drillRig.getID(), drillRig.getLookAheadDistance(), tec);
//        var m6 = new Mission(drillRig.getID(), drillInitialPath);
//        tec.addMissions(m6);
    }
}

