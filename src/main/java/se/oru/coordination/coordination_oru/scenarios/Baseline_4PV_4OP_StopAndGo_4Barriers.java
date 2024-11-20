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
import java.util.List;
import java.util.function.Function;

public class Baseline_4PV_4OP_StopAndGo_4Barriers {

    public static final String MAP = "maps/Baseline_4PV_4OP_6SV_StopAndGo_4Barriers.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.CLOSEST_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/Baseline_4PV_4OP_StopAndGo_2Barriers/";
    public static final double SAFETY_DISTANCE = 0.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = false;
    public static final double REPORTING_TIME = 0.1;
    public static final int SIMULATION_INTERVAL = 48;
    public static final String CLASS_NAME = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
    public static final String PLANS_FOLDER_NAME = "paths/" + CLASS_NAME + "/";
    public static final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
    public static final VehiclePathPlanner planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 120, 1.5, 0.1);

    public static void main(String[] args) throws InterruptedException {

        final var maxVelocityLHD = 4.17 / SCALE_ADJUSTMENT;
        final var maxAccelerationLHD = 1.0 / SCALE_ADJUSTMENT;
        final var lengthLHD = 10.0 / SCALE_ADJUSTMENT;
        final var widthLHD = 6.0 / SCALE_ADJUSTMENT;

        final var maxVelocityMT = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationMT = 1.0 / SCALE_ADJUSTMENT;
        final var lengthMT = 8.0 / SCALE_ADJUSTMENT;
        final var widthMT = 6.0 / SCALE_ADJUSTMENT;

        final var maxVelocityDR = 4.17 / SCALE_ADJUSTMENT;
        final var maxAccelerationDR = 1.0 / SCALE_ADJUSTMENT;
        final var lengthDR = 8.0 / SCALE_ADJUSTMENT;
        final var widthDR = 5.0 / SCALE_ADJUSTMENT;

        final var maxVelocityC = 4.17 / SCALE_ADJUSTMENT;
        final var maxAccelerationC = 1.0 / SCALE_ADJUSTMENT;
        final var lengthC = 8.0 / SCALE_ADJUSTMENT;
        final var widthC = 5.0 / SCALE_ADJUSTMENT;

        final var maxVelocityS = 11.11 / SCALE_ADJUSTMENT;
        final var maxAccelerationS = 1.0 / SCALE_ADJUSTMENT;
        final var lengthS = 1.0 / SCALE_ADJUSTMENT;
        final var widthS = 1.0 / SCALE_ADJUSTMENT;

        final var maxVelocityHT = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationHT = 1.0 / SCALE_ADJUSTMENT;
        final var lengthHT = 3.0 / SCALE_ADJUSTMENT;
        final var widthHT = 2.0 / SCALE_ADJUSTMENT;

        final var safetyDistance = SAFETY_DISTANCE / SCALE_ADJUSTMENT;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocityLHD, maxAccelerationLHD);
        final Pose drawPoint1 = new Pose(18.45,61.05,-Math.PI/2);
        final Pose drawPoint3 = new Pose(25.55,61.45,Math.PI/2);
        final Pose drawPoint4B = new Pose(39.95,67.05,-Math.PI/2);
        final Pose drawPoint4F = new Pose(39.95,65.05,-Math.PI/2);
        final Pose drawPoint5 = new Pose(39.85,67.15,Math.PI/2);
        final Pose drawPoint6B = new Pose(54.15,74.15,-Math.PI/2);
        final Pose drawPoint6F = new Pose(54.15,71.65,-Math.PI/2);
        final Pose drawPoint10 = new Pose(82.75,74.75,Math.PI/2);
        final Pose drawPoint10A = new Pose(82.65,66.85,Math.PI/2);
        final Pose drawPoint10B = new Pose(82.35,53.15,Math.PI/2);
        final Pose drawPoint8 = new Pose(68.35,75.05,Math.PI/2);
        final Pose drawPoint8B = new Pose(68.35,72.65,Math.PI/2);
        final Pose drawPoint9 = new Pose(75.45,74.15,Math.PI/2);
        final Pose drawPoint11 = new Pose(82.55,73.25,Math.PI/2);
        final Pose drawPoint11B = new Pose(82.75,72.15,Math.PI/2);
        final Pose drawPoint12B = new Pose(96.85,77.35,-Math.PI/2);
        final Pose drawPoint12F = new Pose(96.85,75.45,-Math.PI/2);
        final Pose drawPoint13 = new Pose(96.85,77.05,Math.PI/2);
        final Pose drawPoint14B = new Pose(111.05,83.85,-Math.PI/2);
        final Pose drawPoint14F = new Pose(111.05,81.95,-Math.PI/2);
        final Pose drawPoint15B = new Pose(111.05,84.05,Math.PI/2);
        final Pose drawPoint15F = new Pose(111.05,81.25,Math.PI/2);
        final Pose drawPoint16 = new Pose(118.15,80.25,-Math.PI/2);
        final Pose drawPoint17 = new Pose(125.35,80.05,Math.PI/2);
        final Pose drawPoint18 = new Pose(132.25,72.75,-Math.PI/2);
        final Pose drawPoint19B = new Pose(139.35,79.95,Math.PI/2);
        final Pose drawPoint19F = new Pose(139.35,77.65,Math.PI/2);
        final Pose drawPoint20 = new Pose(146.55,82.85,-Math.PI/2);
        final Pose drawPoint21 = new Pose(153.95,81.95,Math.PI/2);
        final Pose drawPoint21B = new Pose(153.75,49.15,Math.PI/2);
        final Pose orePass1 = new Pose(49.25,17.55,-Math.PI/2);
        final Pose orePass2 = new Pose(58.85,17.75,-Math.PI/2);
        final Pose orePass3 = new Pose(97.95,36.95,-Math.PI/2);
        final Pose orePass4 = new Pose(106.45,33.05,-Math.PI/2);
        final Pose mainTunnelLeft = new Pose(11.35,13.95, Math.PI/2);
        final Pose mainTunnelRight = new Pose(168.05,48.05, -Math.PI/2);

        final Pose barrier1Entry = new Pose(123.45,45.55, Math.PI);
        final Pose barrier1Exit = new Pose(121.45,44.25, Math.PI);
        final Pose barrier2Entry = new Pose(102.05,45.25, Math.PI);
        final Pose barrier2Exit = new Pose(99.95,43.25, Math.PI);
        final Pose barrier3Entry = new Pose(60.85,30.25, Math.PI);
        final Pose barrier3Exit = new Pose(58.65,28.15, Math.PI);
        final Pose barrier4Entry = new Pose(52.15,27.75, Math.PI);
        final Pose barrier4Exit = new Pose(50.45,26.05, Math.PI);

        final Pose serviceWorkshop1 = new Pose(125.15,19.55, Math.PI/2);
        final Pose serviceWorkshop2 = new Pose(125.15,23.75, Math.PI/2);
        final Pose serviceWorkshop3 = new Pose(125.15,27.65, Math.PI/2);
        final Pose serviceWorkshop4 = new Pose(128.25,19.45, Math.PI/2);
        final Pose chargingStation = new Pose(67.85,29.05, -Math.PI/2);

        var lhd1 = new AutonomousVehicle("LHD-1", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint4B, safetyDistance, 1, model);

        var mt1 = new AutonomousVehicle("MT-1", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint4F, safetyDistance, 100, model);
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
//        mt1.generatePlans(planner);
//        mt1.savePlans(CLASS_NAME);
        mt1.loadPlans(PLANS_FOLDER_NAME + "MT-1.path");

        var lhd2 = new AutonomousVehicle("LHD-2", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint6B, safetyDistance, 1, model);

        var mt2 = new AutonomousVehicle("MT-2", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint6F, safetyDistance, 100, model);
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
//        mt2.generatePlans(planner);
//        mt2.savePlans(CLASS_NAME);
        mt2.loadPlans(PLANS_FOLDER_NAME + "MT-2.path");

        var lhd3 = new AutonomousVehicle("LHD-3", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint12B, safetyDistance, 1, model);

        var mt3 = new AutonomousVehicle("MT-3", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint12F, safetyDistance, 100, model);
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 1));
//        mt3.generatePlans(planner);
//        mt3.savePlans(CLASS_NAME);
        mt3.loadPlans(PLANS_FOLDER_NAME + "MT-3.path");

        var lhd4 = new AutonomousVehicle("LHD-4", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint14B, safetyDistance, 1, model);

        var mt4 = new AutonomousVehicle("MT-4", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint14F, safetyDistance, 100, model);
        mt4.addTask(new Task("OreProduction4", 0, new Pose[] {orePass4, drawPoint14F}, 1));
//        mt4.generatePlans(planner);
//        mt4.savePlans(CLASS_NAME);
        mt4.loadPlans(PLANS_FOLDER_NAME + "MT-4.path");

        var s1 = new AutonomousVehicle("S-1", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop1, safetyDistance, 1, model);
        s1.addTask(new Task("toBarrierEntry", 0.0, new Pose[] {barrier1Entry}, 1));
        s1.addTask(new Task("toBarrier2Entry", 0.0, new Pose[] {barrier2Entry}, 1));
        s1.addTask(new Task("toBarrier2Entry", 0.0, new Pose[] {barrier3Entry}, 1));
        s1.addTask(new Task("toBarrier2Entry", 0.0, new Pose[] {barrier4Entry}, 1));
        s1.addTask(new Task("toDrawPoint1", 0.0, new Pose[] {drawPoint1}, 1));
        s1.addTask(new Task("toBarrier2Exit", 0.0, new Pose[] {barrier4Exit}, 1));
        s1.addTask(new Task("toBarrier2Exit", 0.0, new Pose[] {barrier3Exit}, 1));
        s1.addTask(new Task("toBarrier2Exit", 0.0, new Pose[] {barrier2Exit}, 1));
        s1.addTask(new Task("toBarrierExit", 0.0, new Pose[] {barrier1Exit}, 1));
        s1.addTask(new Task("toServiceWorkshop1", 0.0, new Pose[] {serviceWorkshop1}, 1));
//        s1.generatePlans(planner);
//        s1.savePlans(CLASS_NAME);
        s1.loadPlans(PLANS_FOLDER_NAME + "S-1.path");

        var s2 = new AutonomousVehicle("S-2", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop3, safetyDistance, 1, model);
        s2.addTask(new Task("toBarrierEntry", 2.0, new Pose[] {barrier1Entry}, 1));
        s2.addTask(new Task("toBarrier2Entry", 0.0, new Pose[] {barrier2Entry}, 1));
        s2.addTask(new Task("toDrawPoint9", 0.0, new Pose[] {drawPoint9}, 1));
        s2.addTask(new Task("toBarrier2Exit", 0.0, new Pose[] {barrier2Exit}, 1));
        s2.addTask(new Task("toBarrierExit", 0.0, new Pose[] {barrier1Exit}, 1));
        s2.addTask(new Task("toServiceWorkshop3", 0.0, new Pose[] {serviceWorkshop3}, 1));
//        s2.generatePlans(planner);
//        s2.savePlans(CLASS_NAME);
        s2.loadPlans(PLANS_FOLDER_NAME + "S-2.path");

        var s3 = new AutonomousVehicle("S-3", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop2, safetyDistance, 1, model);
        s3.addTask(new Task("toBarrierEntry", 5.0, new Pose[] {barrier1Entry}, 1));
        s3.addTask(new Task("toBarrier2Entry", 0.0, new Pose[] {barrier2Entry}, 1));
        s3.addTask(new Task("toDrawPoint8B", 0.0, new Pose[] {drawPoint8B}, 1));
        s3.addTask(new Task("toBarrier2Exit", 0.0, new Pose[] {barrier2Exit}, 1));
        s3.addTask(new Task("toBarrierExit", 0.0, new Pose[] {barrier1Exit}, 1));
        s3.addTask(new Task("toServiceWorkshop2", 0.0, new Pose[] {serviceWorkshop2}, 1));
        s3.generatePlans(planner);
        s3.savePlans(CLASS_NAME);
//        s3.loadPlans(PLANS_FOLDER_NAME + "S-3.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setFootprints();
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(true);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, true, false);

        var heuristic = new Heuristics(HEURISTIC_TYPE);
        tec.addComparator(heuristic.getComparator());
        var heuristicName = heuristic.getName();

//        CollisionDetector collisionChecker = new CollisionDetector(tec, 1.0);
//        collisionChecker.start();

        if(VISUALIZATION) {
            var viz = new BrowserVisualization();
            viz.setMap(MAP);
            viz.setFontScale(3.0);
            viz.setInitialTransform(10.0, 5.0, 0.0);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(MAP);

        String fileName = "FA" + "_" + "C" + "_" + "S" + "_" + safetyDistance * SCALE_ADJUSTMENT + "_"
                + "V" + "_" + lhd1.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
        if (WRITE_VEHICLE_REPORTS)
            RobotReportWriter.writeReports(tec, REPORTING_TIME, SIMULATION_INTERVAL, heuristicName, REPORT_ADDRESS, fileName, SCALE_ADJUSTMENT);
        Missions.runTasks(tec, SIMULATION_INTERVAL);

        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s1, tec.trackers.get(s1.getID()), new ArrayList<>(List.of(1, 8)), new ArrayList<>(List.of(8)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s1, tec.trackers.get(s1.getID()), new ArrayList<>(List.of(2, 7)), new ArrayList<>(List.of(6)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s1, tec.trackers.get(s1.getID()), new ArrayList<>(List.of(3, 6)), new ArrayList<>(List.of(4)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s1, tec.trackers.get(s1.getID()), new ArrayList<>(List.of(4, 5)), new ArrayList<>(List.of(2)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s2, tec.trackers.get(s2.getID()), new ArrayList<>(List.of(1, 4)), new ArrayList<>(List.of(8)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s2, tec.trackers.get(s2.getID()), new ArrayList<>(List.of(2, 3)), new ArrayList<>(List.of(6)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s3, tec.trackers.get(s3.getID()), new ArrayList<>(List.of(1, 4)), new ArrayList<>(List.of(8)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s3, tec.trackers.get(s3.getID()), new ArrayList<>(List.of(2, 3)), new ArrayList<>(List.of(6)), trackerRetriever);
    }

}
