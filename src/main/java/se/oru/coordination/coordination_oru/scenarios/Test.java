package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.CollisionDetector;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;

public class Test {
    public static void main(String[] args) {

        final int simulationTimeMinutes = 10;
        final long simulationTime = System.currentTimeMillis() + (simulationTimeMinutes * 60 * 1000);
        double predictableDistance = 30.0 ;
        final String map = "maps/mine-map-test.yaml";
        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(80.05,24.75, Math.PI);
        final Pose drawPoint17 = new Pose(24.15,85.55,-Math.PI/2);
        final Pose drawPoint19 = new Pose(38.75,86.35,-Math.PI/2);
        final Pose drawPoint20 = new Pose(45.85,85.25,-Math.PI/2);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose drawPoint22 = new Pose(60.35,87.85,-Math.PI/2);
        final Pose drawPoint23 = new Pose(67.75,86.95,-Math.PI/2);
        final Pose drawPoint24 = new Pose(74.85,84.45,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final ForwardModel model = new ConstantAcceleration(1.0, 2.0, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);

        var lookAheadVehicle = new LookAheadVehicle("H1", predictableDistance, 1, Color.CYAN, 5, 1,
                0.4, 0.5, mainTunnelLeft, 5, 5, model);
        lookAheadVehicle.addTask(new Task("M1", 0.0, new Pose[] {mainTunnelRight}, 0));
        lookAheadVehicle.addTask(new Task("M2", 0.0, new Pose[] {mainTunnelRight}, 0));
        lookAheadVehicle.setGoals(new Pose[] {mainTunnelRight, mainTunnelLeft});
        var autonomousVehicle1 = new AutonomousVehicle("A1", 1, Color.YELLOW, 10.0, 1.0, 0.6, 0.6,
                drawPoint23, 5, 5, model);
        autonomousVehicle1.addTask(new Task("M1", 0.0, new Pose[] {mainTunnelRight, drawPoint23}, new int[] {10000, 0}, 5));
//        autonomousVehicle1.addTask(new Task("M2", 0.0, new Pose[] {drawPoint23}, 5));
//        autonomousVehicle1.setGoals(new Pose[] {mainTunnelRight, drawPoint23});
        lookAheadVehicle.generatePlans(planner);
        autonomousVehicle1.generatePlans(planner);

        // Instantiate a trajectory envelope coordinator.
        final var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        CollisionDetector checker = new CollisionDetector(tec, 2.0);
        checker.start();

        tec.setDefaultFootprint(autonomousVehicle1.getFootprint());
        tec.placeRobot(autonomousVehicle1.getID(), drawPoint23);
        tec.placeRobot(lookAheadVehicle.getID(), mainTunnelLeft);
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(true);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(0);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        Missions.generateMissions();
        Missions.setMap(map);
        Missions.runTasks(tec, -2);

    }
}
