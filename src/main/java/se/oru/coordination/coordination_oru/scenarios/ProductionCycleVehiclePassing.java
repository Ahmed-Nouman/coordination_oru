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
import java.util.List;
import java.util.function.Function;

public class ProductionCycleVehiclePassing {

    public static final double OPERATING_TIME = 5.0;
    public static final double SAFETY_DISTANCE = 5.0;

    public static void main(String[] args) throws Exception {

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000);
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
        String folderName = "paths/" + className + "/";
        String absolutePath = System.getProperty("user.dir");
        String reportsFolder = absolutePath + "/src/main/java/se/oru/coordination/coordination_oru/results/productionVehiclePassing";
        final String map = "maps/mine-map-shutdown.yaml";
        double mapResolution = new MapResolution().getMapResolution(map);
        double scaleAdjustment = 1 / mapResolution;
        double reportingTime = 0.1;
        int simulationTime = 60;
        boolean visualization = true;
        boolean writeVehicleReports = false;

        final double productionMaxVelocity = 4.17 * 100/ scaleAdjustment;
        final double productionMaxAcceleration = 10 / scaleAdjustment;
        final double productionVehicleLength = 5.0 / scaleAdjustment;
        final double productionVehicleWidth = 5.0 / scaleAdjustment;

        final double serviceVehicleMaxVelocity = 11.12  * 100/ scaleAdjustment;
        final double serviceVehicleMaxAcceleration = 10 / scaleAdjustment;
        final double serviceVehicleLength = 10.0 / scaleAdjustment;
        final double serviceVehicleWidth = 6.0 / scaleAdjustment;

        final ForwardModel model = new ConstantAcceleration(productionMaxAcceleration, productionMaxVelocity, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);

        final Pose mainTunnelLeft = new Pose(14.25, 22.05, Math.PI);
        final Pose mainTunnelRight = new Pose(113.25, 40.85, Math.PI);
        final Pose entrance = new Pose(115.35, 3.75, Math.PI);
        final Pose drawPoint12 = new Pose(88.35, 101.05, -Math.PI / 2);
        final Pose drawPoint13 = new Pose(95.75, 100.85, Math.PI);
        final Pose drawPoint14 = new Pose(102.45, 98.05, Math.PI);
        final Pose drawPoint27 = new Pose(17.95, 54.35, Math.PI);
        final Pose drawPoint28 = new Pose(25.05, 58.35, -Math.PI / 2);
        final Pose drawPoint29 = new Pose(32.05, 56.95, Math.PI);
        final Pose drawPoint29A = new Pose(39.35, 54.15, Math.PI);
        final Pose drawPoint30 = new Pose(46.25, 49.85, -Math.PI / 2);
        final Pose drawPoint31 = new Pose(53.25, 49.25, -Math.PI / 2);
        final Pose drawPoint32 = new Pose(60.35, 53.05, -Math.PI / 2);
        final Pose drawPoint32A = new Pose(67.55, 55.45, -Math.PI / 2);
        final Pose drawPoint33 = new Pose(74.25, 73.45, -Math.PI / 2);
        final Pose drawPoint34 = new Pose(81.35, 79.45, -Math.PI / 2);
        final Pose drawPoint35 = new Pose(88.45, 81.95, -Math.PI / 2);
        final Pose orePass1 = new Pose(28.45, 15.05, -Math.PI / 2);
        final Pose orePass2 = new Pose(76.35, 31.05, -Math.PI / 2);
        final Pose orePass3 = new Pose(92.65, 33.15, -Math.PI / 2);
        final Pose barrier1Start = new Pose(20.25, 22.05, Math.PI);
        final Pose barrier1End = new Pose(24.15, 22.05, Math.PI);
        final Pose barrier2Start = new Pose(95.55, 41.05, Math.PI);
        final Pose barrier2End = new Pose(100.75, 40.75, Math.PI);
        final Pose parking = new Pose(100.55, 62.65, 0);

        var productionVehicle1 = new AutonomousVehicle("A1",1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint30, SAFETY_DISTANCE, 100, model);
        productionVehicle1.addTask(new Task("", 0.1, new Pose[] {orePass1}, 1));
        productionVehicle1.addTask(new Task("", 0.1, new Pose[] {drawPoint30}, 0));
        var productionVehicle2 = new AutonomousVehicle("A2", 1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint32A, SAFETY_DISTANCE, 100, model);
        productionVehicle2.addTask(new Task("", 0.1, new Pose[] {orePass2}, 1));
        productionVehicle2.addTask(new Task("", 0.1, new Pose[] {drawPoint32A}, 0));
        var productionVehicle3 = new AutonomousVehicle("A3", 1, Color.YELLOW, productionMaxVelocity, productionMaxAcceleration,
                productionVehicleLength, productionVehicleWidth, drawPoint12, SAFETY_DISTANCE, 100, model);
        productionVehicle3.addTask(new Task("", 0.1, new Pose[] {parking}, 1));
        productionVehicle3.addTask(new Task("", 0.1, new Pose[] {drawPoint12}, 0));
        var serviceVehicle = new AutonomousVehicle("S", 2,  Color.GREEN, serviceVehicleMaxVelocity, serviceVehicleMaxAcceleration,
                serviceVehicleLength, serviceVehicleWidth, entrance, SAFETY_DISTANCE, 100, model);
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {barrier2End}, 0));
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {barrier1End}, 0));
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {mainTunnelLeft}, 0));
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {barrier1Start}, 0));
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {barrier2Start}, 0));
        serviceVehicle.addTask(new Task("", 0.1, new Pose[] {entrance}, 0));

        productionVehicle1.generatePlans(planner);
        productionVehicle1.loadPlans(folderName + "A1.path");
        productionVehicle2.loadPlans(folderName + "A2.path");
        productionVehicle3.loadPlans(folderName + "A3.path");
        serviceVehicle.loadPlans(folderName + "S.path");

        tec.setupSolver(0, 100000000);
        tec.startInference();
        var heuristics = new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST);
        var newheuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        tec.addComparator(heuristics.getComparator());
        tec.setDefaultFootprint(productionVehicle1.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        if(visualization) {
            var viz = new BrowserVisualization();
            viz.setMap(map);
            viz.setFontScale(4);
            viz.setInitialTransform(9, 28.53, 1.0);
            tec.setVisualization(viz);
        }

        Missions.generateMissions();
        Missions.setMap(map);

        String fileName = "CS_OP" + "_" + "C" + "_" + "S" + "_" + productionVehicle1.getSafetyDistance() + "_"
                + "V" + "_" + productionVehicle1.getMaxVelocity() * scaleAdjustment + "_";
        if (writeVehicleReports)
            RobotReportWriter.writeReports(tec, reportingTime, simulationTime, heuristics.getName(), reportsFolder, fileName, scaleAdjustment);
        Missions.runTasks(tec, simulationTime);

        ArrayList<Integer> P_missionIDsToStop = new ArrayList<>(Arrays.asList(1));
        ArrayList<Integer> S_missionIDsToStop = new ArrayList<>(Arrays.asList(0, 4));
        ArrayList<Integer> vehicleIDsToStop = new ArrayList<>(Arrays.asList(1, 2, 3));
        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);

//        AdaptiveTrackerRK4.scheduleVehiclesStop(productionVehicle2, P_missionIDsToStop, vehicleIDsToStop, trackerRetriever);
        AdaptiveTrackerRK4.scheduleVehiclesStop(serviceVehicle, S_missionIDsToStop, vehicleIDsToStop, trackerRetriever);
//        AdaptiveTrackerRK4.scheduleVehicleSlow(serviceVehicle, missionIDsToStop, vehicleIDsToStop, trackerRetriever, productionMaxVelocity, productionMaxVelocity / 8); // FIXME: Vehicles may Jump if difference is too much
//        AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(serviceVehicle, missionIDsToStop, TEC, heuristics, newheuristics);
    }

}