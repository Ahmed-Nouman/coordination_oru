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
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;
import java.util.Arrays;

@DemoDescription(desc = "Example of replacing a path midway.")
public class Test5 {

    public static void main(String[] args) throws InterruptedException {

        double MAX_ACCEL = 3.0;
        double MAX_VEL = 4.0;
        String map = null;
        map = "maps/map-partial-2.yaml";
        final ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);

        //Instantiate a trajectory envelope coordinator.
        final TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, MAX_VEL,MAX_ACCEL);

        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(false, true, true);

        double xl = .01;
        double yl = .01;
        Coordinate footprint1 = new Coordinate(-xl,yl);
        Coordinate footprint2 = new Coordinate(xl,yl);
        Coordinate footprint3 = new Coordinate(xl,-yl);
        Coordinate footprint4 = new Coordinate(-xl,-yl);
        tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);

        //Need to setup infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        //Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        //Setup a simple GUI (null means empty map, otherwise provide yaml file)

        BrowserVisualization viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setInitialTransform(20.0, 9.0, 2.0);
        tec.setVisualization(viz);

        Missions.loadRoadMap("missions/icaps_locations_and_paths_4.txt");


        //Instantiate a simple motion planner
        ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
        rsp.setMap(map);
        rsp.setRadius(0.1);
//        rsp.setFootprint(tec.getDefaultFootprint());
        rsp.setTurningRadius(4.0);
        rsp.setDistanceBetweenPathPoints(0.3);


        //In case deadlocks occur, we make the coordinator capable of re-planning on the fly (experimental, not working properly yet)
        tec.setMotionPlanner(1, rsp);
        tec.setForwardModel(1, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
        tec.placeRobot(1, Missions.getLocationPose("L_0"));

        PoseSteering[] pathL0R0 = null;
        rsp.setStart(Missions.getLocationPose("L_0"));
        rsp.setGoals(Missions.getLocationPose("R_0"));
        var goal = new Pose[] {Missions.getLocationPose("R_0")};
        var task = new Task("", 0.0, goal, 0);

        var vehicle = new AutonomousVehicle(1, "A1", 1, Color.YELLOW, MAX_VEL, MAX_ACCEL, xl, yl, Missions.getLocationPose("L_0"), 0, 1, tec.getForwardModel(1));
        vehicle.addTask(task);
        System.out.println("Vehicle Footprint: " + Arrays.toString(vehicle.getFootprint()));
        vehicle.generatePlans(planner);
//        rsp.plan();
//        if (rsp.getPath() == null) throw new Error("No path found.");
//        pathL0R0 = rsp.getPath();
//
//        Mission m = new Mission(1, pathL0R0);
//        Missions.enqueueMission(m);
//
//        Missions.startMissionDispatcher(tec);
        Missions.generateMissions();
        Missions.setMap(map);
        Missions.runTasks(tec, -1);

    }

}
