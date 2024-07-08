package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class MultipleTriggers {

    public static final String MAP = "maps/12-1051_batteryChange.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/ProductionCycleBatteryChange";
    public static final double LENGTH = 10.0;
    public static final double WIDTH = 6.0;
    public static final double MAX_VELOCITY = 10.0;
    public static final double MAX_ACCELERATION = 1.0;
    public static final double PRODUCTION_SAFETY_DISTANCE = 50.0;
    public static final double SERVICE_SAFETY_DISTANCE = 10.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = false;
    public static final double REPORTING_TIME = 0.1;
    public static final int SIMULATION_INTERVAL = 30;
    public static final String CLASS_NAME = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
    public static final String PLANS_FOLDER_NAME = "paths/" + CLASS_NAME + "/";
    public static final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
    public static final VehiclePathPlanner planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 120, 2.0, 0.1);

    public static void main(String[] args) throws InterruptedException {

        final var maxVelocity = MAX_VELOCITY / SCALE_ADJUSTMENT;
        final var maxAcceleration = MAX_ACCELERATION / SCALE_ADJUSTMENT;
        final var length = LENGTH / SCALE_ADJUSTMENT;
        final var width = WIDTH / SCALE_ADJUSTMENT;
        final var productionSafetyDistance = PRODUCTION_SAFETY_DISTANCE / SCALE_ADJUSTMENT;
        final var serviceSafetyDistance = SERVICE_SAFETY_DISTANCE / SCALE_ADJUSTMENT;
        final var goalRepetition = 100;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocity, maxAcceleration);
        final Pose drawPoint1 = new Pose(11.45,61.65,-Math.PI/2);
        final Pose drawPoint2 = new Pose(18.65,56.45,-Math.PI/2);
        final Pose drawPoint3 = new Pose(25.55,61.45,-Math.PI/2);
        final Pose drawPoint4 = new Pose(32.75,66.15,-Math.PI/2);
        final Pose drawPoint7 = new Pose(54.15,73.65,-Math.PI/2);
        final Pose drawPoint12 = new Pose(89.75,75.25,-Math.PI/2);
        final Pose drawPoint16 = new Pose(118.15,80.25,-Math.PI/2);
        final Pose drawPoint18 = new Pose(132.45,77.35,-Math.PI/2);
        final Pose drawPoint20 = new Pose(146.55,82.85,-Math.PI/2);
        final Pose drawPoint21 = new Pose(153.95,81.95,-Math.PI/2);
        final Pose orePass1 = new Pose(48.75,15.75,-Math.PI/2);
        final Pose orePass2 = new Pose(106.55,32.95,-Math.PI/2);
        final Pose orePass3 = new Pose(134.95,34.05,-Math.PI/2);
        final Pose mainTunnelLeft = new Pose(11.35,13.95, -Math.PI/2);
        final Pose mainTunnelRight = new Pose(168.05,84.75, -Math.PI/2);
        final Pose barrier1Entry = new Pose(21.35,20.35, Math.PI);
        final Pose barrier1Exit = new Pose(25.35,21.35, Math.PI);
        final Pose barrier2Entry = new Pose(151.65,38.95, -Math.PI);
        final Pose barrier2Exit = new Pose(147.85,38.55, 0);
        final Pose serviceWorkshop1 = new Pose(115.85,33.95, -Math.PI/2);
        final Pose serviceWorkshop2A = new Pose(125.25,29.65, Math.PI/2);
        final Pose serviceWorkshop2B = new Pose(128.05,30.15, Math.PI/2);

        var productionVehicle1 = new AutonomousVehicle("P1",1, Color.YELLOW, 10.0, 1.0,
                length, width, drawPoint7, productionSafetyDistance, goalRepetition, model);
        productionVehicle1.addTask(new Task("", 0.0, new Pose[] {orePass1}, 1));
        productionVehicle1.addTask(new Task("", 0.1, new Pose[] {drawPoint7}, 1));
//        productionVehicle1.generatePlans(planner);
//        productionVehicle1.savePlans(CLASS_NAME);
        productionVehicle1.loadPlans(PLANS_FOLDER_NAME + "P1.path");

        var productionVehicle2 = new AutonomousVehicle("P2",1, Color.YELLOW, 10.0, 1.0,
                length, width, drawPoint12, productionSafetyDistance, goalRepetition, model);
        productionVehicle2.addTask(new Task("", 0, new Pose[] {orePass2}, 1));
        productionVehicle2.addTask(new Task("", 0.1, new Pose[] {drawPoint12}, 1));
//        productionVehicle2.generatePlans(planner);
//        productionVehicle2.savePlans(CLASS_NAME);
        productionVehicle2.loadPlans(PLANS_FOLDER_NAME + "P2.path");

        var serviceVehicle1 = new AutonomousVehicle("S1",2, Color.GREEN, 10.0, 1.0,
                length, width, mainTunnelLeft, serviceSafetyDistance, 1, model);
        serviceVehicle1.addTask(new Task("", 0.1, new Pose[] {barrier1Entry}, 1));
        serviceVehicle1.addTask(new Task("", 0.1, new Pose[] {drawPoint3}, 2));
        serviceVehicle1.addTask(new Task("", 0.1, new Pose[] {barrier1Exit}, 2));
        serviceVehicle1.addTask(new Task("", 0.1, new Pose[] {mainTunnelLeft}, 1));
//        serviceVehicle1.generatePlans(planner);
//        serviceVehicle1.savePlans(CLASS_NAME);
        serviceVehicle1.loadPlans(PLANS_FOLDER_NAME + "S1.path");

        var serviceVehicle2 = new AutonomousVehicle("S2",2, Color.GREEN, 10.0, 1.0,
                length, width, mainTunnelRight, serviceSafetyDistance, 1, model);
        serviceVehicle2.addTask(new Task("", 0.2, new Pose[] {barrier2Entry}, 1));
        serviceVehicle2.addTask(new Task("", 0.2, new Pose[] {drawPoint20}, 2));
        serviceVehicle2.addTask(new Task("", 0.2, new Pose[] {barrier2Exit}, 2));
        serviceVehicle2.addTask(new Task("", 0.2, new Pose[] {mainTunnelRight}, 1));
//        serviceVehicle2.generatePlans(planner);
//        serviceVehicle2.savePlans(CLASS_NAME);
        serviceVehicle2.loadPlans(PLANS_FOLDER_NAME + "S2.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(productionVehicle1.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var heuristic = new Heuristics(HEURISTIC_TYPE);
        tec.addComparator(heuristic.getComparator());
        var heuristicName = heuristic.getName();
        var newheuristics = new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST);

        if(VISUALIZATION) {
            var viz = new BrowserVisualization();
            viz.setMap(MAP);
            viz.setFontScale(4);
            viz.setInitialTransform(10.0, 5.0, 0.0);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(MAP);

        String fileName = "FA" + "_" + "C" + "_" + "S" + "_" + productionVehicle1.getSafetyDistance() * SCALE_ADJUSTMENT + "_"
                + "V" + "_" + productionVehicle1.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
        if (WRITE_VEHICLE_REPORTS)
            RobotReportWriter.writeReports(tec, REPORTING_TIME, SIMULATION_INTERVAL, heuristicName, REPORT_ADDRESS, fileName, SCALE_ADJUSTMENT);
        Missions.runTasks(tec, SIMULATION_INTERVAL);

        ArrayList<Integer> missionIDsToStop = new ArrayList<>(Arrays.asList(1, 2));
        ArrayList<Integer> vehicleIDsToStop = new ArrayList<>(Arrays.asList(1, 2));
        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);

        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle1, new ArrayList<>(List.of(1)), vehicleIDsToStop, trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle1, new ArrayList<>(List.of(2)), vehicleIDsToStop, trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle2, new ArrayList<>(List.of(1)), vehicleIDsToStop, trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle2, new ArrayList<>(List.of(2)), vehicleIDsToStop, trackerRetriever);
//        AdaptiveTrackerRK4.scheduleVehicleSlow(serviceVehicle, missionIDsToStop, vehicleIDsToStop, trackerRetriever, 10.0, 5.0);
//        AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(serviceVehicle, missionIDsToStop, tec, heuristic, newheuristics);

    }

}
