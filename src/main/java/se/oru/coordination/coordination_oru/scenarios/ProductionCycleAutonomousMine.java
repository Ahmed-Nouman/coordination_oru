package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;

public class ProductionCycleAutonomousMine {
    public static void main(String[] args) throws InterruptedException {

        final int loopMinutes = 5;
        final long loopTime = System.currentTimeMillis() + (loopMinutes * 60 * 1000);
        final String YAML_FILE = "maps/mine-map-new.yaml";
        final Pose mainTunnelLeft = new Pose(4.25, 15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(80.05, 24.75, Math.PI);
        final Pose drawPoint15 = new Pose(9.65, 84.35, -Math.PI / 2);
        final Pose drawPoint16 = new Pose(16.75, 87.15, -Math.PI / 2);
        final Pose drawPoint17 = new Pose(24.15, 84.95, -Math.PI / 2);
        final Pose drawPoint18 = new Pose(31.35, 85.65, -Math.PI / 2);
        final Pose drawPoint19 = new Pose(38.85, 85.35, -Math.PI / 2);
        final Pose drawPoint20 = new Pose(45.85, 84.65, -Math.PI / 2);
        final Pose drawPoint21 = new Pose(53.05, 87.45, -Math.PI / 2);
        final Pose drawPoint22 = new Pose(60.25, 87.55, -Math.PI / 2);
        final Pose drawPoint23 = new Pose(67.75, 86.95, -Math.PI / 2);
        final Pose drawPoint24 = new Pose(75.05, 84.65, -Math.PI / 2);
        final Pose drawPoint36 = new Pose(7.25, 45.75, -Math.PI / 2);
        final Pose drawPoint37 = new Pose(19.55, 34.85, -Math.PI / 2);
        final Pose drawPoint38 = new Pose(19.55, 26.25, -Math.PI / 2);
        final Pose orePass = new Pose(54.35, 11.25, -Math.PI / 2);
        final Pose workStation1 = new Pose(23.75, 8.95, -Math.PI / 2);
        final Pose workStation2 = new Pose(20.15, 9.05, -Math.PI / 2);
        final Pose workStation3 = new Pose(17.35, 9.65, -Math.PI / 2);

        // FIXME Maybe try smaller motion plans
        final Pose[] drillRigGoal = {drawPoint38, drawPoint18, drawPoint24, workStation1};
        final Pose[] chargingVehicleGoal = {drawPoint38, drawPoint18, drawPoint24, workStation2};
        final Pose[] waterVehicleGoal = {drawPoint24, drawPoint23, drawPoint22, drawPoint21,
                drawPoint20, drawPoint19, drawPoint18, drawPoint17, drawPoint16, drawPoint15,
                drawPoint36, drawPoint37, drawPoint38, workStation3};

        var drillVehicle = new AutonomousVehicle("drillRig", 1, Color.MAGENTA, 5, 2, 1000,
                0.5, 0.5, mainTunnelLeft, drillRigGoal, 0, 0);
        var chargingVehicle = new AutonomousVehicle("chargingVehicle", 1, Color.PINK, 5, 2, 1000,
                0.5, 0.5, mainTunnelLeft, chargingVehicleGoal, 0, 0);
        var waterVehicle = new AutonomousVehicle("waterVehicle", 1, Color.BLUE, 5, 2, 1000,
                0.5, 0.5, mainTunnelLeft, waterVehicleGoal, 0, 0);

        var autonomousVehicle1 = new AutonomousVehicle(drawPoint16, new Pose[] {orePass});
        var autonomousVehicle2 = new AutonomousVehicle(drawPoint23, new Pose[] {orePass});
        autonomousVehicle1.getPlan(autonomousVehicle1, YAML_FILE, true);
        autonomousVehicle2.getPlan(autonomousVehicle2, YAML_FILE, true);

        // Instantiate a trajectory envelope coordinator.
        final var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(autonomousVehicle1.getFootprint());
        tec.placeRobot(autonomousVehicle1.getID(), autonomousVehicle1.getInitialPose());
        tec.placeRobot(autonomousVehicle2.getID(), autonomousVehicle2.getInitialPose());
        tec.setForwardModel(autonomousVehicle1.getID(), new ConstantAccelerationForwardModel(autonomousVehicle1.getMaxAcceleration(), autonomousVehicle1.getMaxVelocity(), tec.getTemporalResolution(),
                tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(autonomousVehicle1.getID())));
        tec.setForwardModel(autonomousVehicle2.getID(), new ConstantAccelerationForwardModel(autonomousVehicle2.getMaxAcceleration(), autonomousVehicle2.getMaxVelocity(), tec.getTemporalResolution(),
                tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(autonomousVehicle2.getID())));

        tec.addComparator(new Heuristics().closestFirst());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
//        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

        var m1 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPath());
        var m2 = new Mission(autonomousVehicle2.getID(), autonomousVehicle2.getPath());
//        m1.setStoppingPoint(orePass, 5000);
//        m2.setStoppingPoint(orePass, 5000);

        Missions.enqueueMission(m1);
        Missions.enqueueMission(m2);
        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec);

        tec.setForwardModel(drillVehicle.getID(), new ConstantAccelerationForwardModel(drillVehicle.getMaxAcceleration(), drillVehicle.getMaxVelocity(), tec.getTemporalResolution(),
                tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(drillVehicle.getID())));
        tec.setForwardModel(chargingVehicle.getID(), new ConstantAccelerationForwardModel(chargingVehicle.getMaxAcceleration(), chargingVehicle.getMaxVelocity(), tec.getTemporalResolution(),
                tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(chargingVehicle.getID())));
        tec.setForwardModel(waterVehicle.getID(), new ConstantAccelerationForwardModel(waterVehicle.getMaxAcceleration(), waterVehicle.getMaxVelocity(), tec.getTemporalResolution(),
                tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(waterVehicle.getID())));

        drillVehicle.getPlan(drillVehicle, YAML_FILE, false);
        chargingVehicle.getPlan(chargingVehicle, YAML_FILE, false);
        waterVehicle.getPlan(waterVehicle.getInitialPose(), waterVehicleGoal, YAML_FILE, false,
                0.01, 120,
                0.01, 0.1);

        Thread.sleep(5000);
        tec.placeRobot(drillVehicle.getID(), mainTunnelLeft);
        var m3 = new Mission(drillVehicle.getID(), drillVehicle.getPath());
        tec.addMissions(m3);

        Thread.sleep(10000);
        tec.placeRobot(chargingVehicle.getID(), mainTunnelRight);
        var m4 = new Mission(chargingVehicle.getID(), chargingVehicle.getPath());
        tec.addMissions(m4);

        Thread.sleep(20000);
        tec.placeRobot(waterVehicle.getID(), mainTunnelRight);
        var m5 = new Mission(waterVehicle.getID(), waterVehicle.getPath());
        tec.addMissions(m5);

    }
}
