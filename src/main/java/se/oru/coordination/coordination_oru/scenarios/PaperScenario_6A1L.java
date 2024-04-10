package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.dataStructue.Mission;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;
import java.io.IOException;

public class PaperScenario_6A1L {
    public static void main(String[] args) throws IOException {

        String absolutePath = System.getProperty("user.dir");
        String reportsFolder = absolutePath + "/src/main/java/se/oru/coordination/coordination_oru/results/lookAheadPaper_2023";
        final String YAML_FILE = "maps/mine-map-paper-2023.yaml";
        double mapResolution = new MapResolution().getMapResolution(YAML_FILE);
        double scaleAdjustment = 1.0 / mapResolution;
        double lookAheadDistance = 95 / scaleAdjustment; // use separate look ahead distance for all robots
        double timeIntervalInSeconds = 0.1;
        int inferenceCycleTime = 100;
        int terminationInMinutes = 5;
        int numOfCallsForLookAheadRobot = 1;
        boolean visualization = true;
        boolean writeVehicleReports = true;

        // Everything including velocity, acceleration, lookahead, length and width should be scaled
        final double MAX_VELOCITY = 10.0 / scaleAdjustment;
        final double MAX_ACCELERATION = 1.0 / scaleAdjustment;
        final double LENGTH = 9.0 / scaleAdjustment;
        final double WIDTH = 6.0 / scaleAdjustment;
        final ForwardModel model = new ConstantAccelerationForwardModel(MAX_ACCELERATION, MAX_VELOCITY, 1000, 1000, 30);

        final Pose mainTunnelLeft = new Pose(14.25, 22.15, Math.PI);
        final Pose mainTunnelRight = new Pose(113.25, 40.85, Math.PI);
        final Pose entrance = new Pose(115.35, 3.75, Math.PI);
        final Pose drawPoint12 = new Pose(88.35, 101.05, -Math.PI / 2);
        final Pose drawPoint13 = new Pose(95.75, 100.85, Math.PI);
        final Pose drawPoint14 = new Pose(102.45, 98.05, Math.PI);
        final Pose drawPoint27 = new Pose(17.95, 54.35, Math.PI);
        final Pose drawPoint28 = new Pose(25.05, 58.35, -Math.PI / 2);
        final Pose drawPoint29 = new Pose(31.95, 58.75, Math.PI);
        final Pose drawPoint29A = new Pose(39.35, 54.15, Math.PI);
        final Pose drawPoint30 = new Pose(46.25, 49.85, -Math.PI / 2);
        final Pose drawPoint31 = new Pose(53.25, 49.25, -Math.PI / 2);
        final Pose drawPoint32 = new Pose(60.35, 53.05, -Math.PI / 2);
        final Pose drawPoint32A = new Pose(67.55, 55.45, -Math.PI / 2);
        final Pose drawPoint33 = new Pose(74.25, 73.45, -Math.PI / 2);
        final Pose drawPoint34 = new Pose(81.35, 79.45, -Math.PI / 2);
        final Pose drawPoint35 = new Pose(88.45, 81.95, -Math.PI / 2);
        final Pose orePass1 = new Pose(28.45, 15.05, -Math.PI / 2);
        final Pose orePass2 = new Pose(76.35, 31.05, -Math.PI / 2.7);
        final Pose orePass3 = new Pose(92.65, 33.15, -Math.PI / 2);

        var autonomousRobot1 = new AutonomousVehicle("A1",1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint28, 0, 0, model);
        var autonomousRobot2 = new AutonomousVehicle("A2", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint30, 0, 0, model);
        var autonomousRobot3 = new AutonomousVehicle("A3", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint32A, 0, 0, model);
        var autonomousRobot4 = new AutonomousVehicle("A4", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint34, 0, 0, model);
        var autonomousRobot5 = new AutonomousVehicle("A5", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint35, 0, 0, model);
        var autonomousRobot6 = new AutonomousVehicle("A6", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint12, 0, 0, model);
        var lookAheadRobot = new LookAheadVehicle("H1", lookAheadDistance, 1,  Color.GREEN, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, entrance, 0, 0, model);

        autonomousRobot1.generatePlans(YAML_FILE);
        autonomousRobot2.generatePlans(YAML_FILE);
        autonomousRobot3.generatePlans(YAML_FILE);
        autonomousRobot4.generatePlans(YAML_FILE);
        autonomousRobot5.generatePlans(YAML_FILE);
        autonomousRobot6.generatePlans(YAML_FILE);
        lookAheadRobot.generatePlans(YAML_FILE);

        // Instantiate a trajectory envelope coordinator. TODO Velocity and acceleration are hard coded for tec.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(1000, 1000, MAX_VELOCITY, MAX_ACCELERATION);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setInferenceSleepingTime(inferenceCycleTime);

        tec.setDefaultFootprint(lookAheadRobot.getFootprint());
        tec.placeRobot(autonomousRobot1.getID(), autonomousRobot1.getInitialPose());
        tec.placeRobot(autonomousRobot2.getID(), autonomousRobot2.getInitialPose());
        tec.placeRobot(autonomousRobot3.getID(), autonomousRobot3.getInitialPose());
        tec.placeRobot(autonomousRobot4.getID(), autonomousRobot4.getInitialPose());
        tec.placeRobot(autonomousRobot5.getID(), autonomousRobot5.getInitialPose());
        tec.placeRobot(autonomousRobot6.getID(), autonomousRobot6.getInitialPose());
        tec.placeRobot(lookAheadRobot.getID(), lookAheadRobot.getInitialPose());

        // Set Heuristics
        var heuristic = new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST);
        tec.addComparator(heuristic.getComparator());
        String heuristicName = heuristic.getName();

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        if (visualization) {
            var viz = new BrowserVisualization();
            viz.setMap(YAML_FILE);
            viz.setFontScale(2.5);
            viz.setInitialTransform(8.6, 30.2, -0.73);
            tec.setVisualization(viz);
        }

        var m1 = new Mission(autonomousRobot1.getID(), autonomousRobot1.getPath());
        var m2 = new Mission(autonomousRobot2.getID(), autonomousRobot2.getPath());
        var m3 = new Mission(autonomousRobot3.getID(), autonomousRobot3.getPath());
        var m4 = new Mission(autonomousRobot4.getID(), autonomousRobot4.getPath());
        var m5 = new Mission(autonomousRobot5.getID(), autonomousRobot5.getPath());
        var m6 = new Mission(autonomousRobot6.getID(), autonomousRobot6.getPath());
        var m7 = new Mission(lookAheadRobot.getID(), lookAheadRobot.getPath(lookAheadDistance, tec));

        var randomRobotCaller = new RandomRobotCaller(numOfCallsForLookAheadRobot, terminationInMinutes);
        randomRobotCaller.scheduleRandomCalls(m7);

        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
        Missions.enqueueMission(m3);
        Missions.enqueueMission(m4);
        Missions.enqueueMission(m5);
        Missions.enqueueMission(m6);
        Missions.setMap(YAML_FILE);

        Missions.startMissionDispatcher(tec, writeVehicleReports,
                timeIntervalInSeconds, terminationInMinutes, heuristicName,
                reportsFolder, scaleAdjustment);
    }
}