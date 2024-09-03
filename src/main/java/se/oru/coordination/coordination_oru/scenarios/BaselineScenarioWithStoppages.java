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

public class BaselineScenarioWithStoppages {

    public static final String MAP = "maps/12-1051_batteryChange.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/BaseLineScenarioWithStoppages";
    public static final double SAFETY_DISTANCE = 20.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = true;
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
        final var widthMT = 5.0 / SCALE_ADJUSTMENT;

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
        final var lengthS = 5.0 / SCALE_ADJUSTMENT;
        final var widthS = 3.0 / SCALE_ADJUSTMENT;

        final var maxVelocityHT = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationHT = 1.0 / SCALE_ADJUSTMENT;
        final var lengthHT = 8.0 / SCALE_ADJUSTMENT;
        final var widthHT = 5.0 / SCALE_ADJUSTMENT;

        final var safetyDistance = SAFETY_DISTANCE / SCALE_ADJUSTMENT;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocityLHD, maxAccelerationLHD);
        final Pose drawPoint1 = new Pose(11.45,61.65,Math.PI/2);
        final Pose drawPoint2 = new Pose(18.65,56.45,-Math.PI/2);
        final Pose drawPoint3 = new Pose(25.55,61.45,Math.PI/2);
        final Pose drawPoint4 = new Pose(32.65,66.65,-Math.PI/2);
        final Pose drawPoint5 = new Pose(39.85,67.15,Math.PI/2);
        final Pose drawPoint6 = new Pose(47.05,67.95,-Math.PI/2);
        final Pose drawPoint7 = new Pose(54.05,73.85,Math.PI/2);
        final Pose drawPoint7A = new Pose(54.15,62.45,Math.PI/2);
        final Pose drawPoint7B= new Pose(54.15,46.35,Math.PI/2);
        final Pose drawPoint8 = new Pose(61.25,72.75,-Math.PI/2);
        final Pose drawPoint9 = new Pose(68.55,74.05,Math.PI/2);
        final Pose drawPoint10 = new Pose(75.55,74.45,-Math.PI/2);
        final Pose drawPoint11 = new Pose(82.55,73.25,Math.PI/2);
        final Pose drawPoint12 = new Pose(89.75,75.25,-Math.PI/2);
        final Pose drawPoint13 = new Pose(96.85,77.05,Math.PI/2);
        final Pose drawPoint14 = new Pose(103.95,79.65,-Math.PI/2);
        final Pose drawPoint15B = new Pose(111.05,84.05,Math.PI/2);
        final Pose drawPoint15F = new Pose(111.05,81.25,Math.PI/2);
        final Pose drawPoint16 = new Pose(118.15,80.25,-Math.PI/2);
        final Pose drawPoint17 = new Pose(125.35,80.05,Math.PI/2);
        final Pose drawPoint18 = new Pose(132.25,72.75,-Math.PI/2);
        final Pose drawPoint19B = new Pose(139.35,79.95,Math.PI/2);
        final Pose drawPoint19F = new Pose(139.35,77.65,Math.PI/2);
        final Pose drawPoint20 = new Pose(146.55,82.85,-Math.PI/2);
        final Pose drawPoint21 = new Pose(153.95,81.95,Math.PI/2);
        final Pose orePass1 = new Pose(48.75,15.75,Math.PI/2);
        final Pose orePass2 = new Pose(106.55,32.95,Math.PI/2);
        final Pose orePass3 = new Pose(134.95,34.05,Math.PI/2);
        final Pose mainTunnelLeft = new Pose(11.35,13.95, Math.PI/2);
        final Pose mainTunnelRight = new Pose(168.05,48.05, -Math.PI/2);
        final Pose barrier1Entry = new Pose(21.35,20.35, Math.PI);
        final Pose barrier1Exit = new Pose(25.35,21.35, Math.PI);
        final Pose barrier2Entry = new Pose(151.65,38.95, -Math.PI);
        final Pose barrier2Exit = new Pose(147.85,38.55, 0);
        final Pose serviceWorkshop1 = new Pose(125.15,19.55, Math.PI/2);
        final Pose serviceWorkshop2 = new Pose(125.15,23.75, Math.PI/2);
        final Pose serviceWorkshop3 = new Pose(125.15,27.65, Math.PI/2);
        final Pose chargingStation = new Pose(67.85,29.05, Math.PI/2);

        var lhd1 = new AutonomousVehicle("LHD-1", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint15B, safetyDistance, 1, model);

        var mt1 = new AutonomousVehicle("MT-1", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint15F, safetyDistance, 50, model);
        mt1.addTask(new Task("toOrePass2", 0.5, new Pose[] {orePass2}, 1));
        mt1.addTask(new Task("toDrawPoint15", 0.5, new Pose[] {drawPoint15F}, 1));
//        mt1.generatePlans(planner);
//        mt1.savePlans(CLASS_NAME);
        mt1.loadPlans(PLANS_FOLDER_NAME + "MT-1.path");

        var lhd2 = new AutonomousVehicle("LHD-2", 1, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint19B, safetyDistance, 1, model);

        var mt2 = new AutonomousVehicle("MT-2", 1, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint19F, safetyDistance, 50, model);
        mt2.addTask(new Task("toOrePass3", 0.5, new Pose[] {orePass3}, 1));
        mt2.addTask(new Task("toDrawPoint19", 0.5, new Pose[] {drawPoint19F}, 1));
//        mt2.generatePlans(planner);
//        mt2.savePlans(CLASS_NAME);
        mt2.loadPlans(PLANS_FOLDER_NAME + "MT-2.path");

        var dr = new AutonomousVehicle("DR", 1, Color.GREEN, maxVelocityDR, maxAccelerationDR,
                lengthDR, widthDR, drawPoint5, safetyDistance, 1, model);
        dr.addTask(new Task("toDrawPoint11", 3.0, new Pose[] {drawPoint11}, 1));
//        dr.generatePlans(planner);
//        dr.savePlans(CLASS_NAME);
        dr.loadPlans(PLANS_FOLDER_NAME + "DR.path");

        var c = new AutonomousVehicle("C", 1, Color.RED, maxVelocityC, maxAccelerationC,
                lengthC, widthC, chargingStation, safetyDistance, 1, model);
        c.addTask(new Task("toDrawPoint10", 9.0, new Pose[] {drawPoint5}, 1));
        c.addTask(new Task("toChargingStation", 1.0, new Pose[] {chargingStation}, 1));
//        c.generatePlans(planner);
//        c.savePlans(CLASS_NAME);
        c.loadPlans(PLANS_FOLDER_NAME + "C.path");

        var s1 = new AutonomousVehicle("S-1", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, mainTunnelRight, safetyDistance, 1, model);
        s1.addTask(new Task("toBarrier2Entry", 5.0, new Pose[] {barrier2Entry}, 1));
        s1.addTask(new Task("toServiceWorkshop", 0.5, new Pose[] {serviceWorkshop1}, 1));
        s1.addTask(new Task("toBarrier2Exit", 1.0, new Pose[] {barrier2Exit}, 1));
        s1.addTask(new Task("toMainTunnelRight", 0.5, new Pose[] {mainTunnelRight}, 1));
//        s1.generatePlans(planner);
//        s1.savePlans(CLASS_NAME);
        s1.loadPlans(PLANS_FOLDER_NAME + "S-1.path");

        var s2 = new AutonomousVehicle("S-2", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, drawPoint1, safetyDistance, 1, model);
        s2.addTask(new Task("toBarrier1Entry", 15.0, new Pose[] {barrier1Entry}, 1));
        s2.addTask(new Task("toDrawPoint5", 0.5, new Pose[] {drawPoint5}, 1));
        s2.addTask(new Task("toBarrier1Exit", 1.0, new Pose[] {barrier1Exit}, 1));
        s2.addTask(new Task("toDrawPoint1", 0.5, new Pose[] {drawPoint1}, 1));
//        s2.generatePlans(planner);
//        s2.savePlans(CLASS_NAME);
        s2.loadPlans(PLANS_FOLDER_NAME + "S-2.path");

        var ht = new AutonomousVehicle("HT", 1, Color.LIGHT_GRAY, maxVelocityHT, maxAccelerationHT,
                lengthHT, widthHT, mainTunnelLeft, safetyDistance, 1, model);
        ht.addTask(new Task("toBarrier1Entry", 12.0, new Pose[] {barrier1Entry}, 1)); //12
        ht.addTask(new Task("toDrawPoint7B", 1.0, new Pose[] {drawPoint7B}, 1)); //1
        ht.addTask(new Task("toDrawPoint7A", 1.0, new Pose[] {drawPoint7A}, 1));
        ht.addTask(new Task("toDrawPoint7", 1.0, new Pose[] {drawPoint7}, 1));
        ht.addTask(new Task("toBarrier1Exit", 1.0, new Pose[] {barrier1Exit}, 1));
        ht.addTask(new Task("toMainTunnelLeft", 1.0, new Pose[] {mainTunnelLeft}, 1));
        ht.addTask(new Task("toBarrier1Entry", 35.0, new Pose[] {barrier1Entry}, 1));
        ht.addTask(new Task("toDrawPoint7B", 1.0, new Pose[] {drawPoint7B}, 1));
        ht.addTask(new Task("toDrawPoint7A", 1.0, new Pose[] {drawPoint7A}, 1));
        ht.addTask(new Task("toDrawPoint7", 1.0, new Pose[] {drawPoint7}, 1));
        ht.addTask(new Task("toBarrier1Exit", 1.0, new Pose[] {barrier1Exit}, 1));
        ht.addTask(new Task("toMainTunnelLeft", 1.0, new Pose[] {mainTunnelLeft}, 1));
//        ht.generatePlans(planner);
//        ht.savePlans(CLASS_NAME);
        ht.loadPlans(PLANS_FOLDER_NAME + "HT.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setFootprints();
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var heuristic = new Heuristics(HEURISTIC_TYPE);
        tec.addComparator(heuristic.getComparator());
        var heuristicName = heuristic.getName();

        CollisionDetector collisionChecker = new CollisionDetector(tec, 2.0);
        collisionChecker.start();

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
        AdaptiveTrackerRK4.scheduleVehiclesStop(s1, new ArrayList<>(List.of(1, 2)), new ArrayList<>(List.of(2, 4)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(s2, new ArrayList<>(List.of(1, 2)), new ArrayList<>(List.of(2, 4)), trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(ht, new ArrayList<>(List.of(1, 2, 3, 4)), new ArrayList<>(List.of(2, 4)), trackerRetriever);

    }

}
