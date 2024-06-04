package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class EpirocScenario {

    public static final String MAP = "maps/mine-map-full.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.CLOSEST_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/EpirocScenario/";
    public static final double MAX_VELOCITY = 10.0;
    public static final double MAX_ACCELERATION = 1.0;
    public static final double SAFETY_DISTANCE = 30.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = false;
    public static final double REPORTING_TIME = 0.1;
    public static final int SIMULATION_INTERVAL = 30;
    public static final String CLASS_NAME = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
    public static final String PLANS_FOLDER_NAME = "paths/" + CLASS_NAME + "/";
    public static final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
    public static final VehiclePathPlanner planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 300, 2.0, 0.1);

    public static void main(String[] args) throws InterruptedException {

        final var maxAcceleration = MAX_ACCELERATION / SCALE_ADJUSTMENT;
        final var maxVelocity = MAX_VELOCITY / SCALE_ADJUSTMENT;
        final var safetyDistance = SAFETY_DISTANCE / SCALE_ADJUSTMENT;

        final var loaderLength = 9.0 / SCALE_ADJUSTMENT;
        final var loaderWidth = 6.0 / SCALE_ADJUSTMENT;
        final var loaderMaxVelocity = 4.17 * 10 / SCALE_ADJUSTMENT;
        final var loaderMaxAcceleration = 1.0 * 10  / SCALE_ADJUSTMENT;

        final var drillRigLength = 9.0 / SCALE_ADJUSTMENT;
        final var drillRigWidth = 6.0 / SCALE_ADJUSTMENT;
        final var drillRigMaxVelocity = 11.11 / SCALE_ADJUSTMENT;
        final var drillRigMaxAcceleration = 11.11 / SCALE_ADJUSTMENT;

        final var serviceVehicleLength = 9.0 / SCALE_ADJUSTMENT;
        final var serviceVehicleWidth = 6.0 / SCALE_ADJUSTMENT;
        final var serviceVehicleMaxVelocity = 11.11 / SCALE_ADJUSTMENT;
        final var serviceVehicleMaxAcceleration = 11.11 / SCALE_ADJUSTMENT;

        final var mineTruckLength = 9.0 / SCALE_ADJUSTMENT;
        final var mineTruckWidth = 6.0 / SCALE_ADJUSTMENT;
        final var mineTRuckMaxVelocity = 11.11 / SCALE_ADJUSTMENT;
        final var mineTruckMaxAcceleration = 11.11 / SCALE_ADJUSTMENT;

        TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocity, maxAcceleration);
        final Pose drawPoint1 = new Pose(11.45,61.65,-Math.PI/2);
        final Pose drawPoint2 = new Pose(29.75,106.95,-Math.PI/2);
        final Pose drawPoint3 = new Pose(36.35,107.55,-Math.PI/2);
        final Pose drawPoint4 = new Pose(32.75,66.15,-Math.PI/2);
        final Pose drawPoint6 = new Pose(72.85,138.35,-Math.PI/2);
        final Pose drawPoint7 = new Pose(47.05,78.25,-Math.PI/2);
        final Pose drawPoint8 = new Pose(89.85,132.85,-Math.PI/2);
        final Pose drawPoint12 = new Pose(89.75,75.25,-Math.PI/2);
        final Pose drawPoint18 = new Pose(169.55,121.95,-Math.PI/2);
        final Pose drawPoint20 = new Pose(146.55,82.85,-Math.PI/2);
        final Pose drawPoint21 = new Pose(153.95,81.95,-Math.PI/2);
        final Pose drawPoint24 = new Pose(213.25,120.85,-Math.PI/2);
        final Pose drawPoint27 = new Pose(38.55,66.95,-Math.PI/2);
        final Pose drawPoint30 = new Pose(73.05,61.65,-Math.PI/2);
        final Pose drawPoint32 = new Pose(90.15,66.05,-Math.PI/2);
        final Pose drawPoint32A = new Pose(98.35,69.25,-Math.PI/2);
        final Pose drawPoint34 = new Pose(115.25,97.25,-Math.PI/2);
        final Pose drawPoint37 = new Pose(157.75,71.55,-Math.PI/2);
        final Pose drawPoint38 = new Pose(158.35,63.05,-Math.PI/2);
        final Pose orePass1 = new Pose(109.25,40.15,-Math.PI/2);
        final Pose orePass2 = new Pose(128.95,44.25,-Math.PI/2);
        final Pose orePass3 = new Pose(191.75,50.55,-Math.PI/2);
        final Pose orePass4 = new Pose(202.85,54.95,-Math.PI/2);
        final Pose entrance = new Pose(159.75,4.95, -Math.PI/2);
        final Pose serviceWorkshop1 = new Pose(51.65,22.65, 0);
        final Pose serviceWorkshop2 = new Pose(175.55,47.55, 0);

        var loader1 = new AutonomousVehicle("L1",1, Color.YELLOW, loaderMaxVelocity, loaderMaxAcceleration,
                loaderLength, loaderWidth, drawPoint6, safetyDistance, 20, model);
        loader1.addTask(new Task(0, new Pose[] {orePass2}, 1));
        loader1.addTask(new Task(0, new Pose[] {drawPoint6}, 1));
//        loader1.generatePlans(planner);
//        loader1.savePlans(CLASS_NAME);
        loader1.loadPlans(PLANS_FOLDER_NAME + "L1.path");

        var loader2 = new AutonomousVehicle("L2",1, Color.YELLOW, loaderMaxVelocity, loaderMaxAcceleration,
                loaderLength, loaderWidth, drawPoint32, safetyDistance, 50, model);
        loader2.addTask(new Task(0, new Pose[] {orePass2}, 1));
        loader2.addTask(new Task(0, new Pose[] {drawPoint32}, 1));
//        loader2.generatePlans(planner);
//        loader2.savePlans(CLASS_NAME);
        loader2.loadPlans(PLANS_FOLDER_NAME + "L2.path");

        var loader3 = new AutonomousVehicle("L3",1, Color.YELLOW, loaderMaxVelocity, loaderMaxAcceleration,
                loaderLength, loaderWidth, drawPoint30, safetyDistance, 35, model);
        loader3.addTask(new Task(0, new Pose[] {orePass1}, 1));
        loader3.addTask(new Task(0, new Pose[] {drawPoint30}, 1));
//        loader3.generatePlans(planner);
//        loader3.savePlans(CLASS_NAME);
        loader3.loadPlans(PLANS_FOLDER_NAME + "L3.path");

        var loader4 = new AutonomousVehicle("L4",1, Color.YELLOW, loaderMaxVelocity, loaderMaxAcceleration,
                loaderLength, loaderWidth, drawPoint27, safetyDistance, 25, model);
        loader4.addTask(new Task(0, new Pose[] {orePass1}, 1));
        loader4.addTask(new Task(0, new Pose[] {drawPoint27}, 1));
//        loader4.generatePlans(planner);
//        loader4.savePlans(CLASS_NAME);
        loader4.loadPlans(PLANS_FOLDER_NAME + "L4.path");

        var drillRig1 = new AutonomousVehicle("DR1",1, Color.GREEN, drillRigMaxVelocity, drillRigMaxAcceleration,
                drillRigLength, drillRigWidth, orePass4, safetyDistance, 1, model);
        drillRig1.addTask(new Task(60, new Pose[] {drawPoint8}, 1));
        drillRig1.addTask(new Task(120, new Pose[] {orePass4}, 1));
//        drillRig1.generatePlans(planner);
//        drillRig1.savePlans(CLASS_NAME);
        drillRig1.loadPlans(PLANS_FOLDER_NAME + "DR1.path");

        var drillRig2 = new AutonomousVehicle("DR2",1, Color.GREEN, drillRigMaxVelocity, drillRigMaxAcceleration,
                drillRigLength, drillRigWidth, drawPoint24, safetyDistance, 1, model);
        drillRig2.addTask(new Task(60, new Pose[] {drawPoint18}, 1));
        drillRig2.addTask(new Task(120, new Pose[] {drawPoint24}, 1));
//        drillRig2.generatePlans(planner);
//        drillRig2.savePlans(CLASS_NAME);
        drillRig2.loadPlans(PLANS_FOLDER_NAME + "DR2.path");

        var drillRig3 = new AutonomousVehicle("DR3",1, Color.GREEN, drillRigMaxVelocity, drillRigMaxAcceleration,
                drillRigLength, drillRigWidth, entrance, safetyDistance, 1, model);
        drillRig3.addTask(new Task(60, new Pose[] {drawPoint37}, 1));
        drillRig3.addTask(new Task(120, new Pose[] {entrance}, 1));
//        drillRig3.generatePlans(planner);
//        drillRig3.savePlans(CLASS_NAME);
        drillRig3.loadPlans(PLANS_FOLDER_NAME + "DR3.path");

        var serviceVehicle1 = new AutonomousVehicle("S1",1, Color.BLUE, serviceVehicleMaxVelocity, serviceVehicleMaxAcceleration,
                serviceVehicleLength, serviceVehicleWidth, serviceWorkshop1, safetyDistance, 1, model);
        serviceVehicle1.addTask(new Task(60, new Pose[] {drawPoint34}, 1));
        serviceVehicle1.addTask(new Task(120, new Pose[] {serviceWorkshop1}, 1));
//        serviceVehicle1.generatePlans(planner);
//        serviceVehicle1.savePlans(CLASS_NAME);
        serviceVehicle1.loadPlans(PLANS_FOLDER_NAME + "S1.path");

        var serviceVehicle2 = new AutonomousVehicle("S2",1, Color.BLUE, serviceVehicleMaxVelocity, serviceVehicleMaxAcceleration,
                serviceVehicleLength, serviceVehicleWidth, serviceWorkshop2, safetyDistance, 1, model);
        serviceVehicle2.addTask(new Task(60, new Pose[] {drawPoint32A}, 1));
        serviceVehicle2.addTask(new Task(240, new Pose[] {serviceWorkshop2}, 1));
//        serviceVehicle2.generatePlans(planner);
//        serviceVehicle2.savePlans(CLASS_NAME);
        serviceVehicle2.loadPlans(PLANS_FOLDER_NAME + "S2.path");

        var mineTruck = new AutonomousVehicle("MT1",1, Color.RED, mineTRuckMaxVelocity, mineTruckMaxAcceleration,
                mineTruckLength, mineTruckWidth, drawPoint38, safetyDistance, 1, model);
        mineTruck.addTask(new Task(120, new Pose[] {orePass3}, 1));
        mineTruck.addTask(new Task(0, new Pose[] {drawPoint38}, 1));
//        mineTruck.generatePlans(planner);
//        mineTruck.savePlans(CLASS_NAME);
        mineTruck.loadPlans(PLANS_FOLDER_NAME + "MT1.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(loader1.getFootprint());
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
            viz.setInitialTransform(5.77, 26.12, 31.23);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(MAP);

        String fileName = "C" + "_" + "S" + "_" + loader1.getSafetyDistance() * SCALE_ADJUSTMENT + "_"
                + "V" + "_" + loader1.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
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
