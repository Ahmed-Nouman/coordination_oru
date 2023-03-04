package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.code.AutonomousVehicle;
import se.oru.coordination.coordination_oru.code.Heuristics;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.BrowserVisualization;
import se.oru.coordination.coordination_oru.util.Missions;

import java.awt.*;

public class Test1 {
    public static void main(String[] args) {

        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(80.05,24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final String YAML_FILE = "maps/mine-map-test.yaml";

        final Pose[] goal = {orePass};

        var autonomousVehicle = new AutonomousVehicle(YAML_FILE);
        var autonomousVehiclePath = autonomousVehicle.getPlan(drawPoint21, goal, YAML_FILE, true);

        // Instantiate a trajectory envelope coordinator.
        final var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle.getFootPrint());
        tec.placeRobot(autonomousVehicle.getID(), drawPoint21);
        tec.addComparator(new Heuristics().closest());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(4);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        var m1 = new Mission(autonomousVehicle.getID(), autonomousVehiclePath);

        Missions.enqueueMission(m1);
        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec, false, 1);

    }
}
