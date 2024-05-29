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
public class Test3 {

    private static final double MAX_ACCEL = 1.0;
    private static final double MAX_VEL = 10.0;

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
        autonomousVehicle.setGoals(new Pose[] {orePass});
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

        //Instantiate a simple motion planner
        ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
        rsp.setMap(map);
        rsp.setRadius(0.1);
        rsp.setFootprint(tec.getDefaultFootprint());
        rsp.setTurningRadius(4.0);
        rsp.setDistanceBetweenPathPoints(0.3);


        //In case deadlocks occur, we make the coordinator capable of re-planning on the fly (experimental, not working properly yet)
        tec.setMotionPlanner(1, rsp);
        tec.setForwardModel(1, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));

        var initialPath = autonomousVehicle.getPaths().get(0);

        int replacementIndex = initialPath.length * 1 / 2;
        Pose replacementPose = initialPath[replacementIndex].getPose();
        rsp.setStart(replacementPose);
        rsp.setGoals(mainTunnelRight);
        rsp.plan();
        if (rsp.getPath() == null) throw new Error("No path found.");
        PoseSteering[] replacementPath = new PoseSteering[replacementIndex+rsp.getPath().length];
        System.arraycopy(initialPath, 0, replacementPath, 0, replacementIndex);
        for (int i = 0; i < rsp.getPath().length; i++) replacementPath[i+replacementIndex] = rsp.getPath()[i];

        Mission m = new Mission(1, initialPath);
        Missions.enqueueMission(m);

        Missions.runTasks(tec, -1);

        int buffer = 20;
        while (tec.getRobotReport(1).getPathIndex() < replacementIndex-buffer) Thread.sleep(50);

        tec.replacePath(1,replacementPath,replacementIndex,false,null);

    }

}
