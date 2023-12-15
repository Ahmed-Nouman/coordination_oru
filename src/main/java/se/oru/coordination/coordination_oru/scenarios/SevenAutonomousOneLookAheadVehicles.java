package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;

public class SevenAutonomousOneLookAheadVehicles {
    public static void main(String[] args) {

        final int simulationTimeMinutes = 10;
        final long simulationTime = System.currentTimeMillis() + (simulationTimeMinutes * 60 * 1000);
        double predictableDistance = 10.0 ;
        final String YAML_FILE = "maps/mine-map-test.yaml";
        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(80.05,24.75, Math.PI);
        final Pose drawPoint17 = new Pose(24.15,85.55,-Math.PI/2);
        final Pose drawPoint19 = new Pose(38.75,86.35,-Math.PI/2);
        final Pose drawPoint20 = new Pose(45.85,86.15,-Math.PI/2);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose drawPoint22 = new Pose(60.35,87.85,-Math.PI/2);
        final Pose drawPoint23 = new Pose(67.75,86.95,-Math.PI/2);
        final Pose drawPoint24 = new Pose(74.85,84.45,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);

        var lookAheadVehicle = new LookAheadVehicle("H1",predictableDistance,1,  Color.CYAN, 5, 2,
                30, 0.5, 0.5, mainTunnelLeft, new Pose[] {mainTunnelRight}, 0, 0);
        var autonomousVehicle1 = new AutonomousVehicle(drawPoint17, new Pose[] {orePass});
        var autonomousVehicle2 = new AutonomousVehicle(drawPoint19, new Pose[] {orePass});
        var autonomousVehicle3 = new AutonomousVehicle(drawPoint20, new Pose[] {orePass});
        var autonomousVehicle4 = new AutonomousVehicle(drawPoint21, new Pose[] {orePass});
        var autonomousVehicle5 = new AutonomousVehicle(drawPoint22, new Pose[] {orePass});
        var autonomousVehicle6 = new AutonomousVehicle(drawPoint23, new Pose[] {orePass});
        var autonomousVehicle7 = new AutonomousVehicle(drawPoint24, new Pose[] {orePass});
        lookAheadVehicle.getPlan(lookAheadVehicle.getInitialPose(), lookAheadVehicle.getGoalPoses(),
                YAML_FILE, true);
        autonomousVehicle1.getPlan(autonomousVehicle1.getInitialPose(), autonomousVehicle1.getGoalPoses(),
                YAML_FILE, true);
        autonomousVehicle2.getPlan(autonomousVehicle2.getInitialPose(), autonomousVehicle2.getGoalPoses(),
                YAML_FILE, true);
        autonomousVehicle3.getPlan(autonomousVehicle3.getInitialPose(), autonomousVehicle3.getGoalPoses(),
                YAML_FILE,true);
        autonomousVehicle4.getPlan(autonomousVehicle4.getInitialPose(), autonomousVehicle4.getGoalPoses(),
                YAML_FILE, true);
        autonomousVehicle5.getPlan(autonomousVehicle5.getInitialPose(), autonomousVehicle5.getGoalPoses(),
                YAML_FILE,true);
        autonomousVehicle6.getPlan(autonomousVehicle6.getInitialPose(), autonomousVehicle6.getGoalPoses(),
                YAML_FILE, true);
        autonomousVehicle7.getPlan(autonomousVehicle7.getInitialPose(), autonomousVehicle7.getGoalPoses(),
                YAML_FILE, true);

        // Instantiate a trajectory envelope coordinator.
        final var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000,
                5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle1.getFootprint());
        tec.placeRobot(autonomousVehicle1.getID(), autonomousVehicle1.getInitialPose());
        tec.placeRobot(autonomousVehicle2.getID(), autonomousVehicle2.getInitialPose());
        tec.placeRobot(autonomousVehicle3.getID(), autonomousVehicle3.getInitialPose());
        tec.placeRobot(autonomousVehicle4.getID(), autonomousVehicle4.getInitialPose());
        tec.placeRobot(autonomousVehicle5.getID(), autonomousVehicle5.getInitialPose());
        tec.placeRobot(autonomousVehicle6.getID(), autonomousVehicle6.getInitialPose());
        tec.placeRobot(autonomousVehicle7.getID(), autonomousVehicle7.getInitialPose());
        tec.placeRobot(lookAheadVehicle.getID(), lookAheadVehicle.getInitialPose());
        tec.addComparator(new Heuristics().closestFirst());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(0);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        var m1 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPath());
        var m2 = new Mission(autonomousVehicle2.getID(), autonomousVehicle2.getPath());
        var m3 = new Mission(autonomousVehicle3.getID(), autonomousVehicle3.getPath());
        var m4 = new Mission(autonomousVehicle4.getID(), autonomousVehicle4.getPath());
        var m5 = new Mission(autonomousVehicle5.getID(), autonomousVehicle5.getPath());
        var m6 = new Mission(autonomousVehicle6.getID(), autonomousVehicle6.getPath());
        var m7 = new Mission(autonomousVehicle7.getID(), autonomousVehicle7.getPath());
        var m8 = new Mission(lookAheadVehicle.getID(), lookAheadVehicle.getPath());

        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
        Missions.enqueueMission(m3);
        Missions.enqueueMission(m4);
        Missions.enqueueMission(m5);
        Missions.enqueueMission(m6);
        Missions.enqueueMission(m7);
        Missions.enqueueMission(m8);
        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec);

    }
}
