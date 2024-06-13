package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.PathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.MapResolution;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;

public class PlannedInterruptions {

    private static final String map = "maps/mine-map-scenario.yaml";
    private static final double MAP_RESOLUTION = new MapResolution().getMapResolution(map);
    private static final double SCALE_ADJUSTMENT = 1.0 / MAP_RESOLUTION;
    private static final double LENGTH = 8.0 / SCALE_ADJUSTMENT;
    private static final double WIDTH = 5.0 / SCALE_ADJUSTMENT;
    private static final double MAX_VELOCITY = 50.0 / SCALE_ADJUSTMENT;
    private static final double MAX_ACCELERATION = 10.0 / SCALE_ADJUSTMENT;
    private static final double SAFETY_DISTANCE = 2400.0 / SCALE_ADJUSTMENT; //FIXME: Why is this so large? 2400 instead of 24? Not working
    private static final double ORE_LOADING_TIME = 0.15;
    private static final double ORE_UNLOADING_TIME = 0.10;
    private static final double PLANTING_TIME = 0.25;
    private static final double CLEANING_TIME = 1.4;
    private static final ForwardModel model = new ConstantAcceleration(MAX_ACCELERATION, MAX_VELOCITY, 1000, 1000, 30);
    private static final PathPlanner planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
            0.09, 60, 1.0, 0.1);
    public static void main(String[] args) throws Exception {

        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
        String folderName = "paths/" + className + "/";

        final Pose mainTunnelLeft = new Pose(14.25, 22.15, 0);
        final Pose mainTunnelRight = new Pose(113.25, 40.85, Math.PI);
        final Pose entrance = new Pose(115.35, 3.75, Math.PI);
        final Pose drawPoint12 = new Pose(88.35, 101.05, Math.PI / 2);
        final Pose drawPoint13 = new Pose(95.75, 100.85, Math.PI);
        final Pose drawPoint14 = new Pose(102.45, 98.05, Math.PI);
        final Pose drawPoint27 = new Pose(17.95, 54.35, Math.PI);
        final Pose drawPoint28 = new Pose(25.05, 58.35, Math.PI / 2);
        final Pose drawPoint29 = new Pose(31.95, 58.75, Math.PI / 2);
        final Pose drawPoint29A = new Pose(39.35, 54.15, Math.PI);
        final Pose drawPoint30 = new Pose(46.25, 49.85, Math.PI / 2);
        final Pose drawPoint31 = new Pose(53.25, 49.25, -Math.PI / 2);
        final Pose drawPoint32 = new Pose(60.35, 53.05, -Math.PI / 2);
        final Pose drawPoint32A = new Pose(67.55, 55.45, -Math.PI / 2);
        final Pose drawPoint33 = new Pose(74.25, 73.45, Math.PI / 2);
        final Pose drawPoint34 = new Pose(81.35, 79.45, -Math.PI / 2);
        final Pose drawPoint35 = new Pose(88.45, 81.95, -Math.PI / 2);
        final Pose orePass1 = new Pose(28.45, 15.05, Math.PI / 2);
        final Pose orePass2 = new Pose(76.35, 31.05, Math.PI / 2.7);
        final Pose orePass3 = new Pose(92.65, 33.15, Math.PI / 2);

        var explosivesVehicle = new AutonomousVehicle("E",10, Color.RED, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint34, SAFETY_DISTANCE, 1, model);
        explosivesVehicle.addTask(new Task("", PLANTING_TIME, new Pose[] {drawPoint34}, 0));
        explosivesVehicle.addTask(new Task("", PLANTING_TIME, new Pose[] {drawPoint30}, 0));
        explosivesVehicle.addTask(new Task("", PLANTING_TIME, new Pose[] {entrance}, 0));
        explosivesVehicle.savePlans(folderName + "E.path");
        explosivesVehicle.loadPlans(folderName + "E.path");

        var cleaningVehicle = new AutonomousVehicle("C",10, Color.BLUE, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint30, SAFETY_DISTANCE, 1, model);
        cleaningVehicle.addTask(new Task("", CLEANING_TIME, new Pose[] {drawPoint30}, 0));
        cleaningVehicle.addTask(new Task("", CLEANING_TIME, new Pose[] {drawPoint34}, 0));
        cleaningVehicle.addTask(new Task("", CLEANING_TIME, new Pose[] {mainTunnelLeft}, 0));
        cleaningVehicle.loadPlans(folderName + "C.path");

        var autonomousVehicle1 = new AutonomousVehicle("A1",1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint29, SAFETY_DISTANCE, 10, model);
        autonomousVehicle1.addTask(new Task("", ORE_LOADING_TIME, new Pose[] {orePass1}, 0));
        autonomousVehicle1.addTask(new Task("", ORE_UNLOADING_TIME, new Pose[] {drawPoint29}, 0));
        autonomousVehicle1.loadPlans(folderName + "A1.path");

        var autonomousVehicle2 = new AutonomousVehicle("A2",1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint33, SAFETY_DISTANCE, 10, model);
        autonomousVehicle2.addTask(new Task("", ORE_LOADING_TIME, new Pose[] {orePass2}, 0));
        autonomousVehicle2.addTask(new Task("", ORE_UNLOADING_TIME, new Pose[] {drawPoint33}, 0));
        autonomousVehicle2.loadPlans(folderName + "A2.path");

        var autonomousVehicle3 = new AutonomousVehicle("A3",1, Color.YELLOW, MAX_VELOCITY, MAX_ACCELERATION,
                LENGTH, WIDTH, drawPoint12, SAFETY_DISTANCE, 10, model);
        autonomousVehicle3.addTask(new Task("", ORE_LOADING_TIME, new Pose[] {orePass3}, 0));
        autonomousVehicle3.addTask(new Task("", ORE_UNLOADING_TIME, new Pose[] {drawPoint12}, 0));
        autonomousVehicle3.loadPlans(folderName + "A3.path");

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle1.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.setForwardModelsForRobots();
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(3.5);
        viz.setInitialTransform(8.6, 31.58, 5.42);
        tec.setVisualization(viz);

        Missions.setMap(map);
        Missions.generateMissions();

        Missions.runTasks(tec, -1);
    }

}
