package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.utils.MapResolution;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.HumanVehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ProductionCycleShutdown {

    public static final TrajectoryEnvelopeCoordinatorSimulation TEC = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);

    public static void main(String[] args) throws Exception {
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
        String folderName = "paths/" + className + "/";
        String absolutePath = System.getProperty("user.dir");
        String reportsFolder = absolutePath + "/src/main/java/se/oru/coordination/coordination_oru/results/lookAheadPaper_2023";
        final String map = "maps/mine-map-shutdown.yaml";
        double mapResolution = new MapResolution().getMapResolution(map);
        double scaleAdjustment = 1.0 / mapResolution;
        double lookAheadDistance = 95 / scaleAdjustment; // use separate look ahead distance for all robots
        double timeIntervalInSeconds = 0.1;
        int terminationInMinutes = 5;
        int numOfCallsForLookAheadRobot = 1;
        boolean visualization = true;
        boolean writeVehicleReports = true;

        // Everything including velocity, acceleration, lookahead, length and width should be scaled
        final double MAX_VELOCITY = 100.0 / scaleAdjustment;
        final double MAX_ACCELERATION = 10.0 / scaleAdjustment;
        final double LENGTH = 9.0 / scaleAdjustment;
        final double WIDTH = 6.0 / scaleAdjustment;
        final ForwardModel model = new ConstantAcceleration(MAX_ACCELERATION, MAX_VELOCITY, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
                0.09, 60, 2.0, 0.1);

        final Pose mainTunnelLeft = new Pose(14.25, 22.05, Math.PI);
        final Pose mainTunnelRight = new Pose(113.25, 40.85, Math.PI);
        final Pose entrance = new Pose(115.35, 3.75, Math.PI);
        final Pose drawPoint12 = new Pose(88.35, 101.05, -Math.PI / 2);
        final Pose drawPoint13 = new Pose(95.75, 100.85, Math.PI);
        final Pose drawPoint14 = new Pose(102.45, 98.05, Math.PI);
        final Pose drawPoint27 = new Pose(17.95, 54.35, Math.PI);
        final Pose drawPoint28 = new Pose(25.05, 58.35, -Math.PI / 2);
        final Pose drawPoint29 = new Pose(32.05, 56.95, Math.PI);
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
        final Pose barrier1Start = new Pose(20.25, 22.05, Math.PI);
        final Pose barrier1End = new Pose(24.15, 22.05, Math.PI);
        final Pose barrier2Start = new Pose(95.55, 41.05, Math.PI);
        final Pose barrier2End = new Pose(100.75, 40.75, Math.PI);
        final Pose parking = new Pose(100.55, 62.65, 0);

        var autonomousVehicle1 = new AutonomousVehicle("A1",1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint30, 5, 5, model);
        autonomousVehicle1.addTask(new Task(0, new Pose[] {orePass1}, 1));
        autonomousVehicle1.addTask(new Task(0.1, new Pose[] {drawPoint30}, 0));
        var autonomousVehicle2 = new AutonomousVehicle("A2", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint32A, 5, 5, model);
        autonomousVehicle2.addTask(new Task(0, new Pose[] {orePass2}, 1));
        autonomousVehicle2.addTask(new Task(0.1, new Pose[] {drawPoint32A}, 0));
        var autonomousVehicle3 = new AutonomousVehicle("A3", 1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint12, 5, 5, model);
        autonomousVehicle3.addTask(new Task(0, new Pose[] {parking}, 1));
        autonomousVehicle3.addTask(new Task(0.1, new Pose[] {drawPoint12}, 0));
        var serviceVehicle = new HumanVehicle("S", 2,  Color.GREEN, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, entrance, 5, 1, model, TEC);
        serviceVehicle.addTask(new Task(0, new Pose[] {barrier2End}, 0));
        serviceVehicle.addTask(new Task(0.1, new Pose[] {barrier1End}, 0));
        serviceVehicle.addTask(new Task(0, new Pose[] {mainTunnelLeft}, 0));
        serviceVehicle.addTask(new Task(0.1, new Pose[] {barrier1Start}, 0));
        serviceVehicle.addTask(new Task(0, new Pose[] {barrier2Start}, 0));
        serviceVehicle.addTask(new Task(0.1, new Pose[] {entrance}, 0));

        autonomousVehicle1.loadPlans(folderName + "A1.path");
        autonomousVehicle2.loadPlans(folderName + "A2.path");
        autonomousVehicle3.loadPlans(folderName + "A3.path");
        serviceVehicle.loadPlans(folderName + "S.path");

        TEC.setupSolver(0, 100000000);
        TEC.startInference();
        var heuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        var newheuristics = new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST);
        TEC.addComparator(heuristics.getComparator());
        TEC.setDefaultFootprint(autonomousVehicle1.getFootprint());
        TEC.placeRobotsAtStartPoses();
        TEC.setUseInternalCriticalPoints(false);
        TEC.setYieldIfParking(true);
        TEC.setBreakDeadlocks(true, false, false);

        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(4);
        viz.setInitialTransform(9, 28.53, 1.0);
        TEC.setVisualization(viz);

        Missions.generateMissions();
        Missions.setMap(map);
        Missions.runTasks(TEC, -1);
        ArrayList<Integer> missionIDsToStop = new ArrayList<>(Arrays.asList(1, 4));
        ArrayList<Integer> vehicleIDsToStop = new ArrayList<>(Arrays.asList(1, 2));

//        AdaptiveTrackerRK4.scheduleVehicleStop(serviceVehicle, 10, 10, trackerRetriever);
//        AdaptiveTrackerRK4.scheduleVehicleSlow(serviceVehicle, missionIDsToStop, vehicleIDsToStop, trackerRetriever, 10.0, 1.0); // FIXME: Vehicles Jump
//        AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(serviceVehicle, missionIDsToStop, TEC, heuristics, newheuristics);
    }

}