package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;

public class HeuristicsPaperScenario {

    public static final String YAML_FILE = "maps/mine-map-heuristic-paper.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(YAML_FILE);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.HIGHEST_PRIORITY_AND_CLOSEST_FIRST;
    public static final ReedsSheppCarPlanner.PLANNING_ALGORITHM PLANNING_ALGORITHM = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;
    public static final String reportAddress = "/src/main/java/se/oru/coordination/coordination_oru/results/heuristicsPaper_2024";
    public static final String REPORT_ADDRESS = reportAddress;
    public static final double LENGTH = 9.0;
    public static final double WIDTH = 7.0;
    public static final double MAX_VELOCITY = 30.0;
    public static final double PRODUCTION_SAFETY_DISTANCE = 10.0;
    public static final double SERVICE_SAFETY_DISTANCE = 1.0;
    public static final double MAX_ACCELERATION = 5.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = false;
    public static final double REPORTING_TIME = 0.1;
    public static final int REPORTING_INTERVAL = 30;
    public static final int TRACKING_PERIOD = 30;
    public static final double UP = Math.PI / 2;
    public static final double UP_RIGHT = Math.PI / 6;
    public static final double DOWN = 3 * Math.PI / 2;
    public static final double DOWN_LEFT = 5 * Math.PI / 4;
    public static final double DOWN_RIGHT = 7 * Math.PI / 4;
    public static final double RIGHT = 0;
    public static final double LEFT = Math.PI;;
    public static void main(String[] args) {

        var absolutePath = System.getProperty("user.dir");
        var reportsFolder = absolutePath + REPORT_ADDRESS;

        final var maxVelocity = MAX_VELOCITY / SCALE_ADJUSTMENT;
        final var maxAcceleration = MAX_ACCELERATION / SCALE_ADJUSTMENT;
        final var length = LENGTH / SCALE_ADJUSTMENT;
        final var width = WIDTH / SCALE_ADJUSTMENT;

        final var mainTunnelLeft = new Pose(3.35, 13.85, UP_RIGHT);
        final var mainTunnelRight = new Pose(80.05, 26.25, UP_RIGHT);
        final var drawPoint1 = new Pose(10.35, 56.75, DOWN);
        final var drawPoint2 = new Pose(20.55, 50.75, LEFT);
        final var drawPoint3 = new Pose(26.95, 56.15, DOWN);
        final var drawPoint4 = new Pose(35.45, 61.05, DOWN);
        final var drawPoint5 = new Pose(43.95, 61.15, DOWN);
        final var drawPoint6 = new Pose(54.65, 55.95, LEFT);
        final var drawPoint7 = new Pose(61.35, 50.65, DOWN);
        final var drawPoint8 = new Pose(69.55, 48.95, DOWN);
        final var orePass = new Pose(39.95, 9.15, DOWN);

        var productionVehicle1 = new AutonomousVehicle("P1",0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
                length, width, drawPoint1, new Pose[] {orePass}, PRODUCTION_SAFETY_DISTANCE, 0);
        productionVehicle1.setPlanningAlgorithm(PLANNING_ALGORITHM);
//        var productionVehicle2 = new AutonomousVehicle("P2", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
//                length, width, drawPoint2, new Pose[] {orePass}, 0, 0);
        var productionVehicle3 = new AutonomousVehicle("P3", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
                length, width, drawPoint3, new Pose[] {orePass}, PRODUCTION_SAFETY_DISTANCE, 0);
        productionVehicle1.setPlanningAlgorithm(PLANNING_ALGORITHM);
//        var productionVehicle4 = new AutonomousVehicle("P4", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
//                length, width, drawPoint4, new Pose[] {orePass}, 0, 0);
        var productionVehicle5 = new AutonomousVehicle("P5", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
                length, width, drawPoint5, new Pose[] {orePass}, PRODUCTION_SAFETY_DISTANCE, 0);
        productionVehicle5.setPlanningAlgorithm(PLANNING_ALGORITHM);
//        var productionVehicle6 = new AutonomousVehicle("P6", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
//                length, width, drawPoint6, new Pose[] {orePass}, 0, 0);
        var productionVehicle7 = new AutonomousVehicle("P7", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
                length, width, drawPoint7, new Pose[] {orePass}, PRODUCTION_SAFETY_DISTANCE, 0);
        productionVehicle7.setPlanningAlgorithm(PLANNING_ALGORITHM);
//        var productionVehicle8 = new AutonomousVehicle("P8", 0, Color.YELLOW, maxVelocity, maxAcceleration, TRACKING_PERIOD,
//                length, width, drawPoint8, new Pose[] {orePass}, 0, 0);
        var serviceVehicle = new AutonomousVehicle("S1", 1,  Color.GREEN, maxVelocity, maxAcceleration,
                TRACKING_PERIOD, length, width, mainTunnelLeft, new Pose[] {mainTunnelRight}, SERVICE_SAFETY_DISTANCE, 0);
        serviceVehicle.setPlanningAlgorithm(PLANNING_ALGORITHM);

        productionVehicle1.getPlan(productionVehicle1, YAML_FILE, true);
//        productionVehicle2.getPlan(productionVehicle2, YAML_FILE, false);
        productionVehicle3.getPlan(productionVehicle3, YAML_FILE,true);
//        productionVehicle4.getPlan(productionVehicle4, YAML_FILE, false);
        productionVehicle5.getPlan(productionVehicle5, YAML_FILE,true);
//        productionVehicle6.getPlan(productionVehicle6, YAML_FILE, false);
        productionVehicle7.getPlan(productionVehicle7, YAML_FILE, true);
//        productionVehicle8.getPlan(productionVehicle8, YAML_FILE, false);
        serviceVehicle.getPlan(serviceVehicle, YAML_FILE, true);

        // Instantiate a trajectory envelope coordinator. TODO Velocity and acceleration are hard coded for tec.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(1000, 1000, maxVelocity, maxAcceleration);
        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setForwardModel(productionVehicle1.getID(), new ConstantAccelerationForwardModel(productionVehicle1.getMaxAcceleration(),
                productionVehicle1.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(productionVehicle1.getID())));
//        tec.setForwardModel(productionVehicle2.getID(), new ConstantAccelerationForwardModel(productionVehicle2.getMaxAcceleration(),
//                productionVehicle2.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
//                tec.getRobotTrackingPeriodInMillis(productionVehicle2.getID())));
        tec.setForwardModel(productionVehicle3.getID(), new ConstantAccelerationForwardModel(productionVehicle3.getMaxAcceleration(),
                productionVehicle3.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(productionVehicle3.getID())));
//        tec.setForwardModel(productionVehicle4.getID(), new ConstantAccelerationForwardModel(productionVehicle4.getMaxAcceleration(),
//                productionVehicle4.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
//                tec.getRobotTrackingPeriodInMillis(productionVehicle4.getID())));
        tec.setForwardModel(productionVehicle5.getID(), new ConstantAccelerationForwardModel(productionVehicle5.getMaxAcceleration(),
                productionVehicle5.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(productionVehicle5.getID())));
//        tec.setForwardModel(productionVehicle6.getID(), new ConstantAccelerationForwardModel(productionVehicle6.getMaxAcceleration(),
//                productionVehicle6.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
//                tec.getRobotTrackingPeriodInMillis(productionVehicle6.getID())));
        tec.setForwardModel(productionVehicle7.getID(), new ConstantAccelerationForwardModel(productionVehicle7.getMaxAcceleration(),
                productionVehicle7.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(productionVehicle7.getID())));
//        tec.setForwardModel(productionVehicle8.getID(), new ConstantAccelerationForwardModel(productionVehicle8.getMaxAcceleration(),
//                productionVehicle8.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
//                tec.getRobotTrackingPeriodInMillis(productionVehicle8.getID())));
        tec.setForwardModel(serviceVehicle.getID(), new ConstantAccelerationForwardModel(serviceVehicle.getMaxAcceleration(),
                serviceVehicle.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(serviceVehicle.getID())));

        tec.setDefaultFootprint(productionVehicle1.getFootprint());
        tec.placeRobot(productionVehicle1.getID(), productionVehicle1.getInitialPose());
//        tec.placeRobot(productionVehicle2.getID(), productionVehicle2.getInitialPose());
        tec.placeRobot(productionVehicle3.getID(), productionVehicle3.getInitialPose());
//        tec.placeRobot(productionVehicle4.getID(), productionVehicle4.getInitialPose());
        tec.placeRobot(productionVehicle5.getID(), productionVehicle5.getInitialPose());
//        tec.placeRobot(productionVehicle6.getID(), productionVehicle6.getInitialPose());
        tec.placeRobot(productionVehicle7.getID(), productionVehicle7.getInitialPose());
//        tec.placeRobot(productionVehicle8.getID(), productionVehicle8.getInitialPose());
        tec.placeRobot(serviceVehicle.getID(), serviceVehicle.getInitialPose());

        var heuristic = new Heuristics(HEURISTIC_TYPE);
        tec.addComparator(heuristic.getComparator());
        var heuristicName = heuristic.getName();

        tec.setBreakDeadlocks(true, false, false);

        if (VISUALIZATION) {
            var viz = new BrowserVisualization();
            viz.setMap(YAML_FILE);
            viz.setFontScale(2.75);
            viz.setInitialTransform(11.0, 16.18, 22.50);
            tec.setVisualization(viz);
        }
        var m1 = new Mission(productionVehicle1.getID(), productionVehicle1.getPath());
//        var m1Inv = new ArrayList<>(Arrays.asList(productionVehicle1.getPath()));
//        Collections.reverse(m1Inv);
//        var m1Back = new Mission(productionVehicle1.getID(), m1Inv.toArray(new PoseSteering[0]));
//        var m2 = new Mission(productionVehicle2.getID(), productionVehicle2.getPath());
        var m3 = new Mission(productionVehicle3.getID(), productionVehicle3.getPath());
//        var m3Inv = new ArrayList<>(Arrays.asList(productionVehicle3.getPath()));
//        Collections.reverse(m3Inv);
//        var m3Back = new Mission(productionVehicle3.getID(), m3Inv.toArray(new PoseSteering[0]));
//        var m4 = new Mission(productionVehicle4.getID(), productionVehicle4.getPath());
        var m5 = new Mission(productionVehicle5.getID(), productionVehicle5.getPath());
//        var m5Inv = new ArrayList<>(Arrays.asList(productionVehicle5.getPath()));
//        Collections.reverse(m5Inv);
//        var m5Back = new Mission(productionVehicle5.getID(), m5Inv.toArray(new PoseSteering[0]));
//        var m6 = new Mission(productionVehicle6.getID(), productionVehicle6.getPath());
        var m7 = new Mission(productionVehicle7.getID(), productionVehicle7.getPath());
//        var m7Inv = new ArrayList<>(Arrays.asList(productionVehicle7.getPath()));
//        Collections.reverse(m7Inv);
//        var m7Back = new Mission(productionVehicle7.getID(), m7Inv.toArray(new PoseSteering[0]));
//        var m8 = new Mission(productionVehicle8.getID(), productionVehicle8.getPath());
        var m9 = new Mission(serviceVehicle.getID(), serviceVehicle.getPath());
//        var m9Inv = new ArrayList<>(Arrays.asList(serviceVehicle.getPath()));
//        Collections.reverse(m9Inv);
//        var m9Back = new Mission(serviceVehicle.getID(), m9Inv.toArray(new PoseSteering[0]));
//        Missions.saveRoadMap("missions/gg");

        Missions.enqueueMission(m1);
//        Missions.enqueueMission(m1Back);
//        Missions.enqueueMission(m2);
        Missions.enqueueMission(m3);
//        Missions.enqueueMission(m3Back);
//        Missions.enqueueMission(m4);
        Missions.enqueueMission(m5);
//        Missions.enqueueMission(m5Back);
//        Missions.enqueueMission(m6);
        Missions.enqueueMission(m7);
//        Missions.enqueueMission(m7Back);
//        Missions.enqueueMission(m8);
        Missions.enqueueMission(m9);
//        Missions.enqueueMission(m9Back);
        Missions.setMap(YAML_FILE);
//        Missions.runMissionsOnce(tec);
//        Missions.runMissionsIndefinitely(tec);
//        Missions.startMissionDispatchers(tec);
        Missions.startMissionDispatcher(tec, WRITE_VEHICLE_REPORTS, REPORTING_TIME, REPORTING_INTERVAL, heuristicName, reportsFolder, SCALE_ADJUSTMENT);
    }

}
