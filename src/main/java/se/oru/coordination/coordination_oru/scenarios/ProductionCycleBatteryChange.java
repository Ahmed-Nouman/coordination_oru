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
import java.util.function.Function;

public class ProductionCycleBatteryChange {

    public static final String MAP = "maps/12-1051_batteryChange.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.CLOSEST_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/ProductionCycleBatteryChange";
    public static final double LENGTH = 9.0;
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
        final var missionRepetition = 100;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocity, maxAcceleration);
        final Pose drawPoint1 = new Pose(11.45,61.65,-Math.PI/2);
        final Pose drawPoint2 = new Pose(18.65,56.45,-Math.PI/2);
        final Pose drawPoint3 = new Pose(25.55,61.45,-Math.PI/2);
        final Pose drawPoint4 = new Pose(32.75,66.15,-Math.PI/2);
        final Pose drawPoint12 = new Pose(89.75,75.25,-Math.PI/2);
        final Pose drawPoint20 = new Pose(146.55,82.85,-Math.PI/2);
        final Pose drawPoint21 = new Pose(153.95,81.95,-Math.PI/2);
        final Pose orePass1 = new Pose(48.75,15.75,-Math.PI/2);
        final Pose orePass2 = new Pose(106.55,32.95,-Math.PI/2);
        final Pose orePass3 = new Pose(134.95,34.05,-Math.PI/2);
        final Pose endrawPoint3trance = new Pose(11.55,18.15, -Math.PI/2);
        final Pose barrierEntry = new Pose(21.45,20.65, 0);
        final Pose barrierExit = new Pose(25.45,21.35, 0);
        final Pose serviceWorkshop1 = new Pose(115.85,33.95, -Math.PI/2);
        final Pose serviceWorkshop2A = new Pose(125.25,29.65, Math.PI/2);
        final Pose serviceWorkshop2B = new Pose(128.05,30.15, Math.PI/2);
        final var planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
                0.09, 120, 2.0, 0.1);
        ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);

        var autonomousVehicle1 = new AutonomousVehicle("A1",1, Color.YELLOW, 10.0, 1.0,
                length, width, drawPoint3, productionSafetyDistance, 1, model);
        autonomousVehicle1.addTask(new Task(0.0, new Pose[] {orePass1}, 1));
        autonomousVehicle1.addTask(new Task(0.1, new Pose[] {drawPoint3}, 1));
        autonomousVehicle1.addTask(new Task(0.0, new Pose[] {orePass1}, 1));
        autonomousVehicle1.addTask(new Task(0.1, new Pose[] {serviceWorkshop2A}, 1));
        autonomousVehicle1.addTask(new Task(1.0, new Pose[] {drawPoint3}, 1));
//        autonomousVehicle1.generatePlans(planner);
//        autonomousVehicle1.savePlans(CLASS_NAME);
        autonomousVehicle1.loadPlans(PLANS_FOLDER_NAME + "A1.path");

        var autonomousVehicle2 = new AutonomousVehicle("A2",1, Color.YELLOW, 10.0, 1.0,
                length, width, drawPoint12, productionSafetyDistance, missionRepetition, model);
        autonomousVehicle2.addTask(new Task(0, new Pose[] {orePass2}, 1));
        autonomousVehicle2.addTask(new Task(0.1, new Pose[] {drawPoint12}, 1));
        autonomousVehicle2.loadPlans(PLANS_FOLDER_NAME + "A2.path");

        var autonomousVehicle3 = new AutonomousVehicle("A3",1, Color.YELLOW, 10.0, 1.0,
                length, width, drawPoint20, productionSafetyDistance, missionRepetition, model);
        autonomousVehicle3.addTask(new Task(0, new Pose[] {orePass3}, 1));
        autonomousVehicle3.addTask(new Task(0.1, new Pose[] {drawPoint20}, 1));
        autonomousVehicle3.loadPlans(PLANS_FOLDER_NAME + "A3.path");

        var serviceVehicle1 = new AutonomousVehicle("S1",1, Color.GREEN, 10.0, 1.0,
                length, width, serviceWorkshop1, serviceSafetyDistance, 1, model);
        serviceVehicle1.addTask(new Task(1.40, new Pose[] {serviceWorkshop2B}, 1));
        serviceVehicle1.addTask(new Task(0.25, new Pose[] {serviceWorkshop1}, 1));
//        serviceVehicle1.generatePlans(planner);
//        serviceVehicle1.savePlans(CLASS_NAME);
        serviceVehicle1.loadPlans(PLANS_FOLDER_NAME + "S1.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle1.getFootprint());
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

        String fileName = "C" + "_" + "S" + "_" + autonomousVehicle1.getSafetyDistance() * SCALE_ADJUSTMENT + "_"
                + "V" + "_" + autonomousVehicle1.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
        if (WRITE_VEHICLE_REPORTS)
            RobotReportWriter.writeReports(tec, REPORTING_TIME, SIMULATION_INTERVAL, heuristicName, REPORT_ADDRESS, fileName, SCALE_ADJUSTMENT);
        Missions.runTasks(tec, SIMULATION_INTERVAL);

        ArrayList<Integer> missionIDsToStop = new ArrayList<>(Arrays.asList(1, 2));
        ArrayList<Integer> vehicleIDsToStop = new ArrayList<>(Arrays.asList(1, 2, 3));
        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);

//        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle, missionIDsToStop, vehicleIDsToStop, trackerRetriever);
//        AdaptiveTrackerRK4.scheduleVehicleSlow(serviceVehicle, missionIDsToStop, vehicleIDsToStop, trackerRetriever, 10.0, 5.0);
//        AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(serviceVehicle, missionIDsToStop, tec, heuristic, newheuristics);

    }

}
