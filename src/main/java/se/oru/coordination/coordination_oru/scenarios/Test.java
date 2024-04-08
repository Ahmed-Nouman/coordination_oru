package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.DataStructure.Task;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.motionplanning.VehicleMotionPlanner;
import se.oru.coordination.coordination_oru.motionplanning.VehiclePlanner;

import java.awt.*;

public class Test {
    public static void main(String[] args) {

        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(78.05,24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final String YAML_FILE = "maps/mine-map-test.yaml";
        VehiclePlanner planner = new VehicleMotionPlanner();


        var autonomousVehicle = new AutonomousVehicle("A1",1, Color.YELLOW, 10.0, 1.0,
                0.9, 0.65, drawPoint21, 0, 0);
        autonomousVehicle.setGoals(mainTunnelRight);
//        autonomousVehicle.setGoals(new Pose[] {mainTunnelRight, drawPoint21});
//        autonomousVehicle.addTask(new Task(new Pose[] {mainTunnelRight}, 0.25));
//        autonomousVehicle.addTask(new Task(new Pose[] {mainTunnelLeft}, 0.5));

        var autonomousVehicle1 = new AutonomousVehicle("A2",1, Color.YELLOW, 10.0, 1.0,
                0.9, 0.65, orePass, 0, 0);
        autonomousVehicle1.addTask(new Task(new Pose[] {mainTunnelLeft}, 0.25));
        autonomousVehicle1.addTask(new Task(new Pose[] {orePass}, 0.25));

        autonomousVehicle.generatePlans(YAML_FILE);
        autonomousVehicle1.generatePlans(YAML_FILE);

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle.getFootprint());
        tec.placeRobotsAtStartPoses();
//        tec.placeRobot(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0)[0].getPose()); //FIXME: DO Automatic placing of vehicles
//        tec.placeRobot(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(0)[0].getPose());
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

//        var m1 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0));
//        var m2 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(1));
//        var m3 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(0));
//        var m4 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(1));

//        Missions.enqueueMission(m1);
//        Missions.enqueueMission(m2);
//        Missions.enqueueMission(m3);
//        Missions.enqueueMission(m4);
        Missions.generateMissions();
        Missions.setMap(YAML_FILE);
        Missions.runMissionsOnce(tec);
    }
}