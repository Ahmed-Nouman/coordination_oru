package se.oru.coordination.coordination_oru.scenarios;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.HumanVehicle;

import java.awt.*;

@DemoDescription(desc = "Example of replacing a path midway.")
public class Test4 {

    public static void main(String[] args) throws InterruptedException {

        TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);
        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(78.05,24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final String map = "maps/mine-map-test.yaml";
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);
        ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);

        var autonomousVehicle = new AutonomousVehicle("A1",1, Color.YELLOW, 10.0, 1.0,
                0.9, 0.5, drawPoint21, 5, 5, model);
        autonomousVehicle.setGoals(new Pose[] {mainTunnelRight, drawPoint21});
        autonomousVehicle.generatePlans(planner);

        var humanVehicle = new AutonomousVehicle("A2",1, Color.BLUE, 10.0, 1.0,
                0.9, 0.65, mainTunnelLeft, 5, 5, model);
        humanVehicle.setGoals(new Pose[] {mainTunnelRight, mainTunnelLeft});
        humanVehicle.generatePlans(planner);

        tec.setupSolver(0, 100000000);
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle.getFootprint());
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

        Missions.generateMissions();
        Missions.setMap(map);
        Missions.runTasks(tec, -1);

    }

}
