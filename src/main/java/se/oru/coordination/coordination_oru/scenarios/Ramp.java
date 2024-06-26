package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;

public class Ramp {
    public static void main(String[] args) {

        final int simulationTimeMinutes = 2;
        double predictableDistance = 25.0;
        long simulationTime = System.currentTimeMillis() + (simulationTimeMinutes * 60 * 1000);
        final Pose mainTunnelLeft = new Pose(4.25, 15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(80.05, 24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95, 87.75, -Math.PI / 2);
        final Pose orePass = new Pose(54.35, 11.25, -Math.PI / 2);
        final String map = "maps/mine-map-test.yaml";
        final ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);

        var autonomousVehicle = new AutonomousVehicle("A1", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint21, 0, 0, model);
        autonomousVehicle.setGoals(orePass);
        var lookAheadVehicle = new LookAheadVehicle("L1", predictableDistance, 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                mainTunnelLeft, 0, 0, model);
        autonomousVehicle.generatePlans(planner);
        lookAheadVehicle.generatePlans(planner);

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle.getFootprint());
        tec.placeRobot(autonomousVehicle.getID(), autonomousVehicle.getInitialPose());
        tec.placeRobot(lookAheadVehicle.getID(), lookAheadVehicle.getInitialPose());
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization(map);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        var m1 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPath());
        var m2 = new Mission(lookAheadVehicle.getID(), lookAheadVehicle.getPath());

        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
        Missions.setMap(map);
        Missions.startMissionDispatcher(tec);
    }
}
