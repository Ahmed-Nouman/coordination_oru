package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.kinematicModel.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.dataStructue.Task;
import se.oru.coordination.coordination_oru.dataStructue.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.motionPlanning.VehicleMotionPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePlanner;

import java.awt.*;
import java.io.IOException;

public class DelayTest {
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
        boolean writeVehicleReports = false;
        VehiclePlanner planner = new VehicleMotionPlanner();

        // Everything including velocity, acceleration, lookahead, length and width should be scaled
        final double MAX_VELOCITY = 100.0 / scaleAdjustment;
        final double MAX_ACCELERATION = 10.0 / scaleAdjustment;
        final double LENGTH = 9.0 / scaleAdjustment;
        final double WIDTH = 6.0 / scaleAdjustment;

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
                LENGTH, WIDTH, drawPoint28, 0, 0);
        autonomousRobot1.setGoals(new Pose[] {orePass1, drawPoint28});
        var autonomousRobot2 = new AutonomousVehicle("A2", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint32A, 0, 0);
        autonomousRobot2.setGoals(new Pose[] {orePass2, drawPoint32A});

        var chargingRobot = new AutonomousVehicle("E", 1, Color.RED, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint34, 0, 0);
        chargingRobot.addTask(new Task(0.25, new Pose[] {orePass3}));
//        autonomousRobot3.addTask(new Task(new Pose[] {mainTunnelRight}, 0.25));

        autonomousRobot1.generatePlans(YAML_FILE);
        autonomousRobot2.generatePlans(YAML_FILE);
        chargingRobot.generatePlans(YAML_FILE);

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(1000, 1000, MAX_VELOCITY, MAX_ACCELERATION);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setInferenceSleepingTime(inferenceCycleTime);

        tec.setForwardModel(autonomousRobot1.getID(), new ConstantAccelerationForwardModel(autonomousRobot1.getMaxAcceleration(),
                autonomousRobot1.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot1.getID())));
        tec.setForwardModel(autonomousRobot2.getID(), new ConstantAccelerationForwardModel(autonomousRobot2.getMaxAcceleration(),
                autonomousRobot2.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(autonomousRobot2.getID())));
        tec.setForwardModel(chargingRobot.getID(), new ConstantAccelerationForwardModel(chargingRobot.getMaxAcceleration(),
                chargingRobot.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(chargingRobot.getID())));

        tec.setDefaultFootprint(autonomousRobot1.getFootprint());
        tec.placeRobot(autonomousRobot1.getID(), autonomousRobot1.getInitialPose());
        tec.placeRobot(autonomousRobot2.getID(), autonomousRobot2.getInitialPose());
        tec.placeRobot(chargingRobot.getID(), chargingRobot.getInitialPose());
//        tec.placeRobotsAtStartPoses();

        // Set Heuristics
        var heuristic = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        tec.addComparator(heuristic.getComparator());
        String heuristicName = heuristic.getName();

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        if (visualization) {
            var viz = new BrowserVisualization();
            viz.setMap(YAML_FILE);
            viz.setFontScale(3.5);
            viz.setInitialTransform(8.6, 30.2, -0.73);
            tec.setVisualization(viz);
        }

        Missions.enqueueMission(new Mission(autonomousRobot1.getID(), autonomousRobot1.getPaths().get(0)));
        Missions.enqueueMission(new Mission(autonomousRobot2.getID(), autonomousRobot2.getPaths().get(0)));
        Missions.enqueueMission(new Mission(chargingRobot.getID(), chargingRobot.getPaths().get(0)));
        Missions.enqueueMission(new Mission(chargingRobot.getID(), chargingRobot.getPaths().get(1)));
        Missions.setMap(YAML_FILE);

        for (int i = 1; i <= 3; i++) {
            Thread t = runMissionsForEachRobot(i, tec);
            t.start();
        }
    }

    private static Thread runMissionsForEachRobot(int i, TrajectoryEnvelopeCoordinatorSimulation tec) {
        final int robotID = i;
        return new Thread() {
            int iteration = 0;
            @Override
            public void run() {
                while (true) {
                    Mission m = Missions.getMission(robotID, iteration%Missions.getMissions(robotID).size());
                    synchronized(tec) {
                        //addMission returns true iff the robot was free to accept a new mission
                        if (tec.addMissions(m)) iteration++;
                    }
                    try { Thread.sleep(2000); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        };
    }
}