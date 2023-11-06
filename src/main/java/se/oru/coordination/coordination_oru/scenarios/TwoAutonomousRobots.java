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

        var autonomousRobot1 = new AutonomousVehicle(drawPoint21, new Pose[] {orePass});
        autonomousRobot1.setMaxVelocity(10);
        System.out.println(autonomousRobot1.getTrackingPeriod());
        var autonomousRobot2 = new AutonomousVehicle(mainTunnelLeft, new Pose[] {mainTunnelRight, mainTunnelLeft});
        autonomousRobot2.setMaxVelocity(1);
        int[] waitingTimes = {100};
        autonomousRobot1.getPlan(autonomousRobot1.getInitialPose(), autonomousRobot1.getGoalPoses(), YAML_FILE,
                true);
        // TODO Include time delays
        autonomousRobot2.getPlanSegments(autonomousRobot2.getInitialPose(),
                autonomousRobot2.getGoalPoses(), waitingTimes, YAML_FILE);

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
        viz.setFontScale(2);
        viz.setInitialTransform(9, 45, -3.5);
        tec.setVisualization(viz);


        Missions.setMap(YAML_FILE);
        var m1 = new Mission(autonomousRobot1.getID(), autonomousRobot1.getPath());
        Missions.enqueueMission(m1);

        Missions.enqueueMissionsFromMap(autonomousRobot2);

        Missions.startMissionDispatchers(tec, 1, 2);
    }
}