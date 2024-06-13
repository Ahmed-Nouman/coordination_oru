package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.*;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class ProductionCycleDrilling {

    public static void main(String[] args) throws Exception {

        final var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
        String pathsFolderName = "paths/" + className + "/";
        String absolutePath = System.getProperty("user.dir");
        String reportsFolder = absolutePath + "/src/main/java/se/oru/coordination/coordination_oru/results/productionCycleDrilling";
        final String map = "maps/map-full.yaml";
        double mapResolution = new MapResolution().getMapResolution(map);
        double scaleAdjustment = 1.0 / mapResolution;
        double reportingTime = 0.1;
        int simulationTime = 15;
        boolean visualization = true;
        boolean writeVehicleReports = false;

        final double productionMaxVelocity = 4.17 / scaleAdjustment;
        final double productionMaxAcceleration = 1.0 / scaleAdjustment;
        final double productionVehicleLength = 12.0 / scaleAdjustment;
        final double productionVehicleWidth = 8.0 / scaleAdjustment;

        final double drillRigMaxVelocity = 11.12 / scaleAdjustment;
        final double drillRigMaxAcceleration = 1.0 / scaleAdjustment;
        final double drillRigLength = 10.0 / scaleAdjustment;
        final double drillRigWidth = 6.0 / scaleAdjustment;

        final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
        final VehiclePathPlanner planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar,
                0.09, 300, 2.0, 0.1);

        final Pose mainTunnelLeft = new Pose(14.25, 22.05, Math.PI);
        final Pose mainTunnelRight = new Pose(113.25, 40.85, Math.PI);
        final Pose entrance = new Pose(159.95,5.05, Math.PI);
        final Pose drawPoint10 = new Pose(106.95,128.35, -Math.PI / 2);
        final Pose drawPoint12 = new Pose(123.95,124.45, -Math.PI / 2);
        final Pose drawPoint13 = new Pose(95.75, 100.85, Math.PI);
        final Pose drawPoint14 = new Pose(102.45, 98.05, Math.PI);
        final Pose drawPoint17 = new Pose(162.25,120.85, -Math.PI / 2);
        final Pose drawPoint18 = new Pose(169.75,121.65, Math.PI);
        final Pose drawPoint21 = new Pose(191.15,123.65, -Math.PI / 2);
        final Pose drawPoint25 = new Pose(4.55,51.65, -Math.PI / 2);
        final Pose drawPoint26 = new Pose(21.75,67.95, -Math.PI / 2);
        final Pose drawPoint27 = new Pose(17.95, 54.35, Math.PI);
        final Pose drawPoint28 = new Pose(47.25,73.25, Math.PI / 2);
        final Pose drawPoint29 = new Pose(32.05, 56.95, Math.PI);
        final Pose drawPoint29A = new Pose(39.35, 54.15, Math.PI);
        final Pose drawPoint30 = new Pose(73.15,62.35, -Math.PI / 2);
        final Pose drawPoint31 = new Pose(53.25, 49.25, -Math.PI / 2);
        final Pose drawPoint32 = new Pose(89.85,66.45, -Math.PI / 2);
        final Pose drawPoint32A = new Pose(67.55, 55.45, -Math.PI / 2);
        final Pose drawPoint33 = new Pose(74.25, 73.45, -Math.PI / 2);
        final Pose drawPoint34 = new Pose(81.35, 79.45, -Math.PI / 2);
        final Pose drawPoint35 = new Pose(88.45, 81.95, -Math.PI / 2);
        final Pose orePass1 = new Pose(109.05,40.55, Math.PI / 2);
        final Pose orePass2 = new Pose(128.75,43.95, -Math.PI / 2.7);
        final Pose orePass3 = new Pose(191.35,51.25, -Math.PI / 2);
        final Pose orePass4 = new Pose(203.05,54.25, -Math.PI / 2);
        final Pose barrierStart = new Pose(148.15,44.95, Math.PI / 2);
        final Pose barrierEnd = new Pose(148.15,50.15, Math.PI / 2);
        final Pose parking = new Pose(100.55, 62.65, 0);

        var productionVehicle1 = new AutonomousVehicle("A1",1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint32, 5, 100, model);
        productionVehicle1.addTask(new Task("", 0.1, new Pose[] {orePass1}, 1));
        productionVehicle1.addTask(new Task("", 0.1, new Pose[] {drawPoint32}, 0));
//        productionVehicle1.generatePlans(planner);
//        productionVehicle1.savePlans(className);
        productionVehicle1.loadPlans(pathsFolderName + "A1.path");

        var productionVehicle2 = new AutonomousVehicle("A2",1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint12, 5, 100, model);
        productionVehicle2.addTask(new Task("", 0.1, new Pose[] {orePass2}, 1));
        productionVehicle2.addTask(new Task("", 0.1, new Pose[] {drawPoint12}, 0));
        productionVehicle2.loadPlans(pathsFolderName + "A2.path");

        var productionVehicle3 = new AutonomousVehicle("A3",1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint17, 5, 100, model);
        productionVehicle3.addTask(new Task("", 0.1, new Pose[] {orePass3}, 1));
        productionVehicle3.addTask(new Task("", 0.1, new Pose[] {drawPoint17}, 0));
        productionVehicle3.loadPlans(pathsFolderName + "A3.path");

        var productionVehicle4 = new AutonomousVehicle("A4",1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint21, 5, 100, model);
        productionVehicle4.addTask(new Task("", 0.1, new Pose[] {orePass4}, 1));
        productionVehicle4.addTask(new Task("", 0.1, new Pose[] {drawPoint21}, 0));
        productionVehicle4.loadPlans(pathsFolderName + "A4.path");

        var drillRig = new AutonomousVehicle("DR",1, Color.RED, drillRigMaxVelocity, drillRigMaxAcceleration,
                drillRigLength, drillRigWidth, entrance, 0, 1, model);
        drillRig.addTask(new Task("", 0.25, new Pose[] {barrierStart}, 1));
        drillRig.addTask(new Task("", 0.25, new Pose[] {drawPoint25}, 1));
        drillRig.addTask(new Task("", 0.25, new Pose[] {drawPoint26}, 0));
        drillRig.addTask(new Task("", 0.25, new Pose[] {drawPoint28}, 0));
        drillRig.addTask(new Task("", 0.25, new Pose[] {barrierEnd}, 0));
        drillRig.addTask(new Task("", 0.25, new Pose[] {entrance}, 0));
        drillRig.loadPlans(pathsFolderName + "DR.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();
        var heuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        var newheuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        tec.addComparator(heuristics.getComparator());
        tec.setDefaultFootprint(productionVehicle1.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        if (visualization) {
            var viz = new BrowserVisualization();
            viz.setMap(map);
            viz.setFontScale(4);
            viz.setInitialTransform(6.43, 36.35, 3.07);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(map);

        String fileName = "SS" + "_" + "C" + "_" + "S" + "_" + productionVehicle1.getSafetyDistance() * scaleAdjustment + "_"
                + "V" + "_" + productionVehicle1.getMaxVelocity() * scaleAdjustment + "_";
        if (writeVehicleReports)
            RobotReportWriter.writeReports(tec, reportingTime, simulationTime, heuristics.getName(), reportsFolder, fileName, scaleAdjustment);
        Missions.runTasks(tec, simulationTime);

        ArrayList<Integer> missionIDsToStop = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        ArrayList<Integer> vehicleIDsToStop = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);

//        AdaptiveTrackerRK4.scheduleVehiclesStop(drillRig, missionIDsToStop, vehicleIDsToStop, trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehicleSlow(drillRig, missionIDsToStop, vehicleIDsToStop, trackerRetriever, productionMaxVelocity, productionMaxVelocity / 4);
//        AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(drillRig, missionIDsToStop, tec, heuristic, newheuristics);
    }
}