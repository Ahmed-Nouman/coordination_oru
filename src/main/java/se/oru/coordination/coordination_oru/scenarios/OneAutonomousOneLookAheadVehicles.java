package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
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
import se.oru.coordination.coordination_oru.vehicles.HumanVehicle;

import java.awt.*;
import java.util.function.Function;

public class OneAutonomousOneLookAheadVehicles {
    public static void main(String[] args) {

        TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);
        double predictableDistance = 25.0;
        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(78.05,24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final String map = "maps/mine-map-test.yaml";
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);
        ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);

        var autonomousVehicle = new AutonomousVehicle("A1",1, Color.YELLOW, 10.0, 1.0,
                0.9, 0.65, drawPoint21, 5, 0, model);
        autonomousVehicle.setGoals(new Pose[] {orePass, drawPoint21});
        autonomousVehicle.generatePlans(planner);

        var humanVehicle = new HumanVehicle("H1",1, Color.BLUE, 10.0, 1.0,
                0.9, 0.65, mainTunnelLeft, 5, 0, model, tec);
        humanVehicle.setGoals(new Pose[] {mainTunnelRight, mainTunnelLeft});
        humanVehicle.generatePlans(planner);

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(humanVehicle.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        var mission1 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0));
        var mission2 = new Mission(humanVehicle.getID(), humanVehicle.getPaths().get(0));
        Missions.enqueueMission(mission1);
        Missions.enqueueMission(mission2);
        Missions.setMap(map);
        Missions.startMissionDispatcher(tec);
    }
}
