package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.BrowserVisualization;
import se.oru.coordination.coordination_oru.util.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

public class TwoAutonomousRobots {
    public static void main(String[] args) {

        final String YAML_FILE = "maps/mine-map-test.yaml";

        final Pose mainTunnelLeft = new Pose(4.25, 15.35, -Math.PI + Math.PI);
        final Pose mainTunnelRight = new Pose(80.05, 24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95, 87.75, -Math.PI / 2);
        final Pose orePass = new Pose(54.35, 11.25, -Math.PI / 2);

        var autonomousRobot1 = new AutonomousVehicle(drawPoint21, new Pose[] {orePass, drawPoint21});
        autonomousRobot1.setMaxVelocity(10);
//        int[] waitingTimes = {100};
        var autonomousRobot2 = new AutonomousVehicle(mainTunnelLeft, new Pose[] {mainTunnelRight, mainTunnelLeft});
        autonomousRobot2.setMaxVelocity(10);
        // TODO Include time delays
//        autonomousRobot2.getPlanSegments(autonomousRobot2.getInitialPose(),
//                autonomousRobot2.getGoalPoses(), waitingTimes, YAML_FILE);
        autonomousRobot1.getPlan(autonomousRobot1.getInitialPose(), autonomousRobot1.getGoalPoses(), YAML_FILE, false); // FIXME Why?
        autonomousRobot2.getPlan(autonomousRobot2.getInitialPose(), autonomousRobot2.getGoalPoses(), YAML_FILE, false);
        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();

        // Sets up MetaCSP solver
        tec.setupSolver(0, 100000000);

        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

//        var heuristics = new Heuristics().closest();
//        tec.addComparator(heuristics);
//        tec.setDefaultFootprint(autonomousRobot1.getFootPrint());
        tec.placeRobot(autonomousRobot1.getID(), autonomousRobot1.getInitialPose());
        tec.placeRobot(autonomousRobot2.getID(), autonomousRobot2.getInitialPose());

//        tec.addComparator(new Heuristics().closest());
//        tec.setUseInternalCriticalPoints(false);
//        tec.setYieldIfParking(true);
//        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(1.5);
        viz.setInitialTransform(9, 45, -3.5);
        tec.setVisualization(viz);


        Missions.setMap(YAML_FILE);
        // Create a Pair for the stopping point and its duration
//        Pair<Pose, Integer> stoppingPoint = new Pair<>(orePass, 50000);

        // Create a list of such pairs
//        List<Pair<Pose, Integer>> stoppingPoints = new ArrayList<>();
//        stoppingPoints.add(stoppingPoint);

        // Now you can call the Mission constructor
        Mission m1 = new Mission(1, autonomousRobot1.getPath());
        m1.setStoppingPoint(orePass, 20000);   //FIXME check stoppage implementation. Works for larger durations
//        Mission m1 = new Mission(1, autonomousRobot1.getPath(), stoppingPoints);
        var m2 = new Mission(autonomousRobot2.getID(), autonomousRobot2.getPath());
        m2.setStoppingPoint(mainTunnelRight, 20000);
        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
//        tec.addMissions(m1);

//        Missions.enqueueMissionsFromMap(autonomousRobot2);

        Missions.startMissionDispatchers(tec, 1, 2);
    }
}