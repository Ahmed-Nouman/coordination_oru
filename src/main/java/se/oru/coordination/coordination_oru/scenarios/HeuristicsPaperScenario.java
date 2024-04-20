package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;

public class HeuristicsPaperScenario {

    public static final String MAP = "maps/mine-map-heuristic-paper.yaml";
    public static final double MAP_RESOLUTION = new MapResolution().getMapResolution(MAP);
    public static final double SCALE_ADJUSTMENT = 1 / MAP_RESOLUTION;
    public static final Heuristics.HeuristicType HEURISTIC_TYPE = Heuristics.HeuristicType.HIGHEST_PRIORITY_AND_CLOSEST_FIRST;
    public static final String REPORT_ADDRESS = System.getProperty("user.dir") +
            "/src/main/java/se/oru/coordination/coordination_oru/results/HeuristicsPaperScenario";
    public static final double LENGTH = 9.0;
    public static final double WIDTH = 7.0;
    public static final double MAX_VELOCITY = 15.0;
    public static final double MAX_ACCELERATION = 1.0;
    public static final double PRODUCTION_SAFETY_DISTANCE = 10.0;
    public static final double SERVICE_SAFETY_DISTANCE = 10.0;
    public static final boolean VISUALIZATION = true;
    public static final boolean WRITE_VEHICLE_REPORTS = true;
    public static final double REPORTING_TIME = 0.1;
    public static final int SIMULATION_INTERVAL = 30;
    public static final String CLASS_NAME = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
    public static final String PLANS_FOLDER_NAME = "paths/" + CLASS_NAME + "/";
    public static final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
    public static final VehiclePathPlanner planner = new VehiclePathPlanner(MAP, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 30, 2.0, 0.1);

    public static final double UP = Math.PI / 2;
    public static final double UP_RIGHT = Math.PI / 6;
    public static final double DOWN = 3 * Math.PI / 2;
    public static final double DOWN_LEFT = 5 * Math.PI / 4;
    public static final double DOWN_RIGHT = 7 * Math.PI / 4;
    public static final double RIGHT = 0;
    public static final double LEFT = Math.PI;;
    public static void main(String[] args) throws Exception {

        final var maxVelocity = MAX_VELOCITY / SCALE_ADJUSTMENT;
        final var maxAcceleration = MAX_ACCELERATION / SCALE_ADJUSTMENT;
        final var length = LENGTH / SCALE_ADJUSTMENT;
        final var width = WIDTH / SCALE_ADJUSTMENT;
        final var productionSafetyDistance = PRODUCTION_SAFETY_DISTANCE / SCALE_ADJUSTMENT;
        final var serviceSafetyDistance = SERVICE_SAFETY_DISTANCE / SCALE_ADJUSTMENT;

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

        var productionVehicle1 = new AutonomousVehicle("P1",0, Color.YELLOW, maxVelocity, maxAcceleration,
                length, width, drawPoint1, productionSafetyDistance, 100, model);
        productionVehicle1.setGoals(new Pose[] {orePass, drawPoint1});
        var productionVehicle2 = new AutonomousVehicle("P2", 0, Color.YELLOW, maxVelocity, maxAcceleration,
                length, width, drawPoint3, productionSafetyDistance, 100, model);
        productionVehicle2.setGoals(new Pose[] {orePass, drawPoint3});
        var productionVehicle3 = new AutonomousVehicle("P3", 0, Color.YELLOW, maxVelocity, maxAcceleration,
                length, width, drawPoint5, productionSafetyDistance, 100, model);
        productionVehicle3.setGoals(new Pose[] {orePass, drawPoint5});
        var productionVehicle4 = new AutonomousVehicle("P4", 0, Color.YELLOW, maxVelocity, maxAcceleration,
                length, width, drawPoint7, productionSafetyDistance, 100, model);
        productionVehicle4.setGoals(new Pose[] {orePass, drawPoint7});
        var serviceVehicle = new AutonomousVehicle("S", 1,  Color.GREEN, maxVelocity, maxAcceleration,
                length, width, mainTunnelLeft, serviceSafetyDistance, 100, model);
        serviceVehicle.addTask(new Task(0.25, new Pose[] {mainTunnelRight}, ));
        serviceVehicle.addTask(new Task(0.25, new Pose[] {mainTunnelLeft}, ));

        productionVehicle1.loadPlans(PLANS_FOLDER_NAME + "P1.path");
        productionVehicle2.loadPlans(PLANS_FOLDER_NAME + "P2.path");
        productionVehicle3.loadPlans(PLANS_FOLDER_NAME + "P3.path");
        productionVehicle4.loadPlans(PLANS_FOLDER_NAME + "P4.path");
        serviceVehicle.loadPlans(PLANS_FOLDER_NAME + "S.path");

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(1000, 1000, maxVelocity, maxAcceleration);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setForwardModelsForRobots();
        tec.setDefaultFootprint(productionVehicle1.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var heuristic = new Heuristics(HEURISTIC_TYPE);
        tec.addComparator(heuristic.getComparator());
        var heuristicName = heuristic.getName();

        if (VISUALIZATION) {
            var viz = new BrowserVisualization();
            viz.setMap(MAP);
            viz.setFontScale(2.75);
            viz.setInitialTransform(11.0, 46.94, 11.27);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(MAP);

        String fileName = "S" + "_" + "S" + "_" + productionVehicle1.getSafetyDistance() * SCALE_ADJUSTMENT + "_"
                + "V" + "_" + productionVehicle1.getMaxVelocity() * SCALE_ADJUSTMENT + "_";
        if (WRITE_VEHICLE_REPORTS)
            RobotReportWriter.writeReports(tec, REPORTING_TIME, SIMULATION_INTERVAL, heuristicName, REPORT_ADDRESS, fileName, SCALE_ADJUSTMENT);
        Missions.runTasks(tec, SIMULATION_INTERVAL);
    }

}
