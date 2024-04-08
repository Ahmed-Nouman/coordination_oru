package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.DataStructure.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.motionplanning.VehicleMotionPlanner;
import se.oru.coordination.coordination_oru.motionplanning.VehiclePlanner;

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
        VehiclePlanner planner = new VehicleMotionPlanner();

        var lookAheadVehicle = new LookAheadVehicle("H1",predictableDistance,1,  Color.CYAN, 5, 2,
                0.5, 0.5, mainTunnelLeft, 0, 0);
        lookAheadVehicle.setGoals(mainTunnelRight);
        var autonomousVehicle1 = new AutonomousVehicle("A1", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint17, 0, 0);
        autonomousVehicle1.setGoals(orePass);
        var autonomousVehicle2 = new AutonomousVehicle("A2", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint19, 0, 0);
        autonomousVehicle2.setGoals(orePass);
        var autonomousVehicle3 = new AutonomousVehicle("A3", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint20, 0, 0);
        autonomousVehicle3.setGoals(orePass);
        var autonomousVehicle4 = new AutonomousVehicle("A4", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint21, 0, 0);
        autonomousVehicle4.setGoals(orePass);
        var autonomousVehicle5 = new AutonomousVehicle("A5", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint22, 0, 0);
        autonomousVehicle5.setGoals(orePass);
        var autonomousVehicle6 = new AutonomousVehicle("A6", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint23, 0, 0);
        autonomousVehicle6.setGoals(orePass);
        var autonomousVehicle7 = new AutonomousVehicle("A7", 1, Color.YELLOW, 10.0, 1.0, 9.0, 6.0,
                drawPoint24, 0, 0);
        autonomousVehicle7.setGoals(orePass);
        lookAheadVehicle.generatePlans(YAML_FILE);
        autonomousVehicle1.generatePlans(YAML_FILE);
        autonomousVehicle2.generatePlans(YAML_FILE);
        autonomousVehicle3.generatePlans(YAML_FILE);
        autonomousVehicle4.generatePlans(YAML_FILE);
        autonomousVehicle5.generatePlans(YAML_FILE);
        autonomousVehicle6.generatePlans(YAML_FILE);
        autonomousVehicle7.generatePlans(YAML_FILE);

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
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST).getComparator());
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
        Missions.startMissionDispatcher(tec);

    }
}
