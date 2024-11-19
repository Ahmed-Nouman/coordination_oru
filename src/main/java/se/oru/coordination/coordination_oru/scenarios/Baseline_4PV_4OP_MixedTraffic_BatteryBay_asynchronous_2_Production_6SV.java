package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;

public class Baseline_4PV_4OP_MixedTraffic_BatteryBay_asynchronous_2_Production_6SV {

    public static final String MAP = "maps/12-1051_Simulation_MAP_BatteryBay.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.CLOSEST_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/Baseline_4PV_4OP_MixedTraffic_BatteryBay_asynchronous_2/";
    public static final double SAFETY_DISTANCE = 0.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = false;
    public static final double REPORTING_TIME = 0.1;
    public static final int SIMULATION_INTERVAL = 48;
    public static final String CLASS_NAME = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
    public static final String PLANS_FOLDER_NAME = "paths/" + CLASS_NAME + "/";
    public static final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
    public static final VehiclePathPlanner planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 60, 1.5, 0.1);

    public static void main(String[] args) throws InterruptedException {

        final var maxVelocityLHD = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationLHD = 1.0 / SCALE_ADJUSTMENT;
        final var lengthLHD = 6.0 / SCALE_ADJUSTMENT;
        final var widthLHD = 4.0 / SCALE_ADJUSTMENT;

        final var maxVelocityMT = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationMT = 1.0 / SCALE_ADJUSTMENT;
        final var lengthMT = 6.0 / SCALE_ADJUSTMENT;
        final var widthMT = 4.0 / SCALE_ADJUSTMENT;

        final var maxVelocityDR = 4.17 / SCALE_ADJUSTMENT;
        final var maxAccelerationDR = 1.0 / SCALE_ADJUSTMENT;
        final var lengthDR = 8.0 / SCALE_ADJUSTMENT;
        final var widthDR = 6.0 / SCALE_ADJUSTMENT;

        final var maxVelocityC = 4.17 / SCALE_ADJUSTMENT;
        final var maxAccelerationC = 1.0 / SCALE_ADJUSTMENT;
        final var lengthC = 8.0 / SCALE_ADJUSTMENT;
        final var widthC = 6.0 / SCALE_ADJUSTMENT;

        final var maxVelocityS = 11.11 / SCALE_ADJUSTMENT;
        final var maxAccelerationS = 1.0 / SCALE_ADJUSTMENT;
        final var lengthS = 3.0 / SCALE_ADJUSTMENT;
        final var widthS = 2.0 / SCALE_ADJUSTMENT;

        final var maxVelocityHT = 8.34 / SCALE_ADJUSTMENT;
        final var maxAccelerationHT = 1.0 / SCALE_ADJUSTMENT;
        final var lengthHT = 8.0 / SCALE_ADJUSTMENT;
        final var widthHT = 6.0 / SCALE_ADJUSTMENT;

        final var safetyDistance = SAFETY_DISTANCE / SCALE_ADJUSTMENT;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(maxVelocityLHD, maxAccelerationLHD);
        final Pose drawPoint1 = new Pose(18.45,61.05,-Math.PI/2);
        final Pose drawPoint3 = new Pose(25.55,61.45,Math.PI/2);
        final Pose drawPoint3B = new Pose(32.75,67.05,Math.PI/2);
        final Pose drawPoint4B = new Pose(39.95,67.05,-Math.PI/2);
        final Pose drawPoint4F = new Pose(39.95,65.05,-Math.PI/2);
        final Pose drawPoint5 = new Pose(39.85,67.15,Math.PI/2);
        final Pose drawPoint6B = new Pose(54.15,74.15,-Math.PI/2);
        final Pose drawPoint6F = new Pose(54.15,71.65,-Math.PI/2);
        final Pose drawPoint8B = new Pose(68.35,72.65,Math.PI/2);
        final Pose drawPoint10 = new Pose(82.75,74.75,Math.PI/2);
        final Pose drawPoint10A = new Pose(82.65,66.85,Math.PI/2);
        final Pose drawPoint10B = new Pose(82.35,53.15,Math.PI/2);
        final Pose drawPoint8 = new Pose(68.35,75.05,Math.PI/2);
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
        final Pose orePass1 = new Pose(48.75,15.75,-Math.PI/2);
        final Pose orePass2 = new Pose(59.05,17.85,-Math.PI/2);
        final Pose orePass3 = new Pose(98.75,36.45,-Math.PI/2);
        final Pose orePass4 = new Pose(106.75,33.05,-Math.PI/2);
        final Pose mainTunnelLeft = new Pose(11.35,13.95, Math.PI/2);
        final Pose mainTunnelRight = new Pose(168.05,48.05, -Math.PI/2);
        final Pose barrier1Entry = new Pose(21.35,20.35, Math.PI);
        final Pose barrier1Exit = new Pose(25.35,21.35, Math.PI);
        final Pose barrier2Entry = new Pose(151.65,38.95, -Math.PI);
        final Pose barrier2Exit = new Pose(147.85,38.55, 0);
        final Pose serviceWorkshop1 = new Pose(125.15,19.55, Math.PI/2);
        final Pose serviceWorkshop2 = new Pose(125.15,23.75, Math.PI/2);
        final Pose serviceWorkshop3 = new Pose(125.15,27.65, Math.PI/2);
        final Pose serviceWorkshop4 = new Pose(128.25,19.45, Math.PI/2);
        final Pose chargingStation = new Pose(11.75,17.95, -Math.PI/2);
        final Pose batteryStation1 = new Pose(73.55,13.05, -Math.PI/2);
        final Pose batteryStation2 = new Pose(75.55,12.85, -Math.PI/2);
        final Pose batteryStation3 = new Pose(80.15,12.5585, -Math.PI/2);
        final Pose batteryStation4 = new Pose(83.95,12.75, -Math.PI/2);
        final Pose batteryStation5 = new Pose(72.25,18.35, -Math.PI/2);
        final Pose batteryStation6 = new Pose(74.35,20.05, -Math.PI/2);
        final Pose batteryStation7 = new Pose(77.95,21.65, -Math.PI/2);
        final Pose batteryStation8 = new Pose(81.85,21.95, -Math.PI/2);

        final Pose serviceWorkshop5 = new Pose(133.75,34.15, Math.PI/2);
        final Pose serviceWorkshop6 = new Pose(136.25,34.45, Math.PI/2);

        var lhd1 = new AutonomousVehicle("LHD-1", 10, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint4B, safetyDistance, 1, model);
        lhd1.addTask(new Task("batteryStation5", 5.0, new Pose[] {batteryStation1}, 1));
        lhd1.addTask(new Task("drawPoint4B", 2.25, new Pose[] {drawPoint4B}, 1));
        lhd1.addTask(new Task("batteryStation5", 11.75, new Pose[] {batteryStation1}, 1));
        lhd1.addTask(new Task("drawPoint4B", 2.25, new Pose[] {drawPoint4B}, 1));
        lhd1.addTask(new Task("batteryStation5", 11.75, new Pose[] {batteryStation1}, 1));
        lhd1.addTask(new Task("drawPoint4B", 2.25, new Pose[] {drawPoint4B}, 1));
        lhd1.addTask(new Task("batteryStation5", 11.75, new Pose[] {batteryStation1}, 1));
        lhd1.addTask(new Task("drawPoint4B", 2.25, new Pose[] {drawPoint4B}, 1));

//        lhd1.generatePlans(planner);
//        lhd1.savePlans(CLASS_NAME);
        lhd1.loadPlans(PLANS_FOLDER_NAME + "LHD-1.path");

        var mt1 = new AutonomousVehicle("MT-1", 10, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint4F, safetyDistance, 1, model);     //Cycle Time 2.5
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("batteryStation", 0.0, new Pose[] {batteryStation2}, 1));
        mt1.addTask(new Task("drawPoint4F", 2.5, new Pose[] {drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt1.addTask(new Task("drawPoint4F", 2.5, new Pose[] {drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt1.addTask(new Task("drawPoint4F", 2.5, new Pose[] {drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("oreProduction1", 0, new Pose[] {orePass1, drawPoint4F}, 1));
        mt1.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt1.addTask(new Task("drawPoint4F", 2.5, new Pose[] {drawPoint4F}, 1));

//        mt1.generatePlans(planner);
//        mt1.savePlans(CLASS_NAME);
        mt1.loadPlans(PLANS_FOLDER_NAME + "MT-1.path");

        var lhd2 = new AutonomousVehicle("LHD-2", 10, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint6B, safetyDistance, 1, model); // These plans need to be generated again
        lhd2.addTask(new Task("batteryStation6", 8.25, new Pose[] {batteryStation1}, 1));
        lhd2.addTask(new Task("drawPoint6B", 2.25, new Pose[] {drawPoint6B}, 1));
        lhd2.addTask(new Task("batteryStation2", 12.0, new Pose[] {batteryStation1}, 1));
        lhd2.addTask(new Task("drawPoint6B", 2.25, new Pose[] {drawPoint6B}, 1));
        lhd2.addTask(new Task("batteryStation6", 12.0, new Pose[] {batteryStation1}, 1));
        lhd2.addTask(new Task("drawPoint6B", 2.25, new Pose[] {drawPoint6B}, 1));
        lhd2.addTask(new Task("batteryStation2", 12.0, new Pose[] {batteryStation1}, 1));
        lhd2.addTask(new Task("drawPoint6B", 2.25, new Pose[] {drawPoint6B}, 1));

//        lhd2.generatePlans(planner);
//        lhd2.savePlans(CLASS_NAME);
        lhd2.loadPlans(PLANS_FOLDER_NAME + "LHD-2.path");

        var mt2 = new AutonomousVehicle("MT-2", 10, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint6F, safetyDistance, 1, model);  // Cycle Time 2.75
        mt2.addTask(new Task("oreProduction2", 0.75, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0.75, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0.75, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("batteryStation", 1.0, new Pose[] {batteryStation2}, 1));
        mt2.addTask(new Task("drawPoint6F", 2.5, new Pose[] {drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt2.addTask(new Task("drawPoint6F", 2.5, new Pose[] {drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt2.addTask(new Task("drawPoint6F", 2.5, new Pose[] {drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("oreProduction2", 0, new Pose[] {orePass2, drawPoint6F}, 1));
        mt2.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt2.addTask(new Task("drawPoint6F", 2.5, new Pose[] {drawPoint6F}, 1));

//        mt2.generatePlans(planner);
//        mt2.savePlans(CLASS_NAME);
        mt2.loadPlans(PLANS_FOLDER_NAME + "MT-2.path");

        var lhd3 = new AutonomousVehicle("LHD-3", 10, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint12B, safetyDistance, 1, model);
        lhd3.addTask(new Task("batteryStation7", 0.0, new Pose[] {batteryStation1}, 1));
        lhd3.addTask(new Task("drawPoint12B", 2.25, new Pose[] {drawPoint12B}, 1));
        lhd3.addTask(new Task("batteryStation7", 11.5, new Pose[] {batteryStation1}, 1));
        lhd3.addTask(new Task("drawPoint12B", 2.25, new Pose[] {drawPoint12B}, 1));
        lhd3.addTask(new Task("batteryStation7", 11.5, new Pose[] {batteryStation1}, 1));
        lhd3.addTask(new Task("drawPoint12B", 2.25, new Pose[] {drawPoint12B}, 1));
        lhd3.addTask(new Task("batteryStation7", 11.5, new Pose[] {batteryStation1}, 1));
        lhd3.addTask(new Task("drawPoint12B", 2.25, new Pose[] {drawPoint12B}, 1));

//        lhd3.generatePlans(planner);
//        lhd3.savePlans(CLASS_NAME);
        lhd3.loadPlans(PLANS_FOLDER_NAME + "LHD-3.path");

        var mt3 = new AutonomousVehicle("MT-3", 10, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint12F, safetyDistance, 1, model); // Cycle Time 2.0
        mt3.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt3.addTask(new Task("drawPoint12F", 2.5, new Pose[] {drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt3.addTask(new Task("drawPoint12F", 2.5, new Pose[]  {drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt3.addTask(new Task("drawPoint12F", 2.5, new Pose[]  {drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("oreProduction3", 0, new Pose[] {orePass3, drawPoint12F}, 10));
        mt3.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt3.addTask(new Task("drawPoint12F", 2.5, new Pose[]  {drawPoint12F}, 10));

//        mt3.generatePlans(planner);
//        mt3.savePlans(CLASS_NAME);
        mt3.loadPlans(PLANS_FOLDER_NAME + "MT-3.path");
//
        var lhd4 = new AutonomousVehicle("LHD-4", 10, Color.YELLOW, maxVelocityLHD, maxAccelerationLHD,
                lengthLHD, widthLHD, drawPoint14B, safetyDistance, 1, model);
        lhd4.addTask(new Task("batteryStation8", 10.50, new Pose[] {batteryStation1}, 1));
        lhd4.addTask(new Task("drawPoint14B", 2.25, new Pose[] {drawPoint14B}, 1));
        lhd4.addTask(new Task("batteryStation8", 11.50, new Pose[] {batteryStation1}, 1));
        lhd4.addTask(new Task("drawPoint14B", 2.25, new Pose[] {drawPoint14B}, 1));
        lhd4.addTask(new Task("batteryStation8", 11.50, new Pose[] {batteryStation1}, 1));
        lhd4.addTask(new Task("drawPoint14B", 2.25, new Pose[] {drawPoint14B}, 1));
        lhd4.addTask(new Task("batteryStation8", 11.50, new Pose[] {batteryStation1}, 1));
        lhd4.addTask(new Task("drawPoint14B", 2.25, new Pose[] {drawPoint14B}, 1));

//        lhd4.generatePlans(planner);
//        lhd4.savePlans(CLASS_NAME);
        lhd4.loadPlans(PLANS_FOLDER_NAME + "LHD-4.path");
//
        var mt4 = new AutonomousVehicle("MT-4", 10, Color.CYAN, maxVelocityMT, maxAccelerationMT,
                lengthMT, widthMT, drawPoint14F, safetyDistance, 1, model); // Cycle Time 2.40
        mt4.addTask(new Task("toOrePass2", 1, new Pose[] {orePass4, drawPoint14F}, 1));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 1));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt4.addTask(new Task("drawPoint14F", 2.75, new Pose[] {drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt4.addTask(new Task("drawPoint14F", 2.5, new Pose[] {drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt4.addTask(new Task("drawPoint14F", 2.5, new Pose[] {drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("toOrePass2", 0, new Pose[] {orePass4, drawPoint14F}, 10));
        mt4.addTask(new Task("batteryStation", 0, new Pose[] {batteryStation2}, 1));
        mt4.addTask(new Task("drawPoint14F", 2.5, new Pose[] {drawPoint14F}, 10));

//        mt4.generatePlans(planner);
//        mt4.savePlans(CLASS_NAME);
        mt4.loadPlans(PLANS_FOLDER_NAME + "MT-4.path");

        var s1 = new AutonomousVehicle("S-1", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop1, safetyDistance, 1, model);
        s1.addTask(new Task("toDrawPoint1", 5.0, new Pose[] {drawPoint1}, 1)); // This plan is not correct
        s1.addTask(new Task("toServiceWorkshop1", 1.0, new Pose[] {serviceWorkshop1}, 1));
//        s1.generatePlans(planner);
//        s1.savePlans(CLASS_NAME);
        s1.loadPlans(PLANS_FOLDER_NAME + "S-1.path");

        var s2 = new AutonomousVehicle("S-2", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop3, safetyDistance, 1, model);
        s2.addTask(new Task("toDrawPoint9", 15.0, new Pose[] {drawPoint9}, 1));
        s2.addTask(new Task("toServiceWorkshop3", 1.0, new Pose[] {serviceWorkshop3}, 1));
//        s2.generatePlans(planner);
//        s2.savePlans(CLASS_NAME);
        s2.loadPlans(PLANS_FOLDER_NAME + "S-2.path");

        var s3 = new AutonomousVehicle("S-3", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop2, safetyDistance, 1, model);
        s3.addTask(new Task("toDrawPoint8B", 10.0, new Pose[] {drawPoint8B}, 1));
        s3.addTask(new Task("toServiceWorkshop2", 1.0, new Pose[] {serviceWorkshop2}, 1));
//        s5.generatePlans(planner);
//        s5.savePlans(CLASS_NAME);
        s3.loadPlans(PLANS_FOLDER_NAME + "S-3.path");

        var s4 = new AutonomousVehicle("S-4", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop5, safetyDistance, 1, model);
        s4.addTask(new Task("toOrePass2", 11.0, new Pose[] {orePass2}, 1));
        s4.addTask(new Task("toServiceWorkshop5", 2.0, new Pose[] {serviceWorkshop5}, 1));
//        s5.generatePlans(planner);
//        s5.savePlans(CLASS_NAME);
        s4.loadPlans(PLANS_FOLDER_NAME + "S-4.path");

        var s5 = new AutonomousVehicle("S-5", 1, Color.BLUE, maxVelocityS, maxAccelerationS,
                lengthS, widthS, serviceWorkshop6, safetyDistance, 1, model);
        s5.addTask(new Task("toDrawPoint3", 14.0, new Pose[] {drawPoint3B}, 1));
        s5.addTask(new Task("toDrawPoint11", 1.0, new Pose[] {drawPoint11}, 1));
        s5.addTask(new Task("toServiceWorkshop6", 1.0, new Pose[] {serviceWorkshop6}, 1));
//        s5.generatePlans(planner);
//        s5.savePlans(CLASS_NAME);
        s5.loadPlans(PLANS_FOLDER_NAME + "S-5.path");

        var ht = new AutonomousVehicle("HT", 1, Color.LIGHT_GRAY, maxVelocityHT, maxAccelerationHT,
                lengthHT, widthHT, serviceWorkshop4, safetyDistance, 1, model);
        ht.addTask(new Task("toDrawPoint10B", 12.0, new Pose[] {drawPoint10B}, 1)); //12
        ht.addTask(new Task("toDrawPoint10A", 1.0, new Pose[] {drawPoint10A}, 1));
        ht.addTask(new Task("toDrawPoint10", 1.0, new Pose[] {drawPoint10}, 1));
        ht.addTask(new Task("toServiceWorkshop4", 1.0, new Pose[] {serviceWorkshop4}, 1));
        ht.addTask(new Task("toDrawPoint10B", 12.0, new Pose[] {drawPoint10B}, 1)); //35
        ht.addTask(new Task("toDrawPoint10A", 1.0, new Pose[] {drawPoint10A}, 1));
        ht.addTask(new Task("toDrawPoint10", 1.0, new Pose[] {drawPoint10}, 1));
        ht.addTask(new Task("toServiceWorkshop4", 1.0, new Pose[] {serviceWorkshop4}, 1));
//        ht.generatePlans(planner);
//        ht.savePlans(CLASS_NAME);
        ht.loadPlans(PLANS_FOLDER_NAME + "HT.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setFootprints();
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(false);
        tec.setBreakDeadlocks(true, false, false);

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
                + "V" + "_" + mt4.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
        if (WRITE_VEHICLE_REPORTS)
            RobotReportWriter.writeReports(tec, REPORTING_TIME, SIMULATION_INTERVAL, heuristicName, REPORT_ADDRESS, fileName, SCALE_ADJUSTMENT);
        Missions.runTasks(tec, SIMULATION_INTERVAL);

    }

}
