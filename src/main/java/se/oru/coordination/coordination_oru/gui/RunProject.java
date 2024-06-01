package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReportWriter;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RunProject {
    private final ControllerNavigation controllerNavigation;

    public RunProject(ControllerNavigation controllerNavigation) {
        this.controllerNavigation = controllerNavigation;
    }

    public void clickRun() {
        var executorService = Executors.newScheduledThreadPool(1);
        var runCount = new AtomicInteger(0);
        int simulationTime = controllerNavigation.getMain().getDataStatus().getSimulationTime();
        int numberOfRuns = controllerNavigation.getMain().getDataStatus().getNumberOfRuns();
        var future = executorService.scheduleAtFixedRate(() -> {
            if (runCount.incrementAndGet() <= numberOfRuns) {
                run();
            } else {
                executorService.shutdown();
            }
        }, 0, simulationTime, TimeUnit.MINUTES);

        executorService.schedule(() -> {
            future.cancel(true);
        }, simulationTime, TimeUnit.MINUTES);
    }

    public void run() {

        var map = controllerNavigation.getMain().getDataStatus().getProjectData().getMap();
        var model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30); //FIXME: HARD CODED
        var mapResolution = controllerNavigation.getMain().getDataStatus().getMapData().getResolution();
        var writeReports = controllerNavigation.getMain().getDataStatus().getWriteVehicleReports();
        var simulationTime = controllerNavigation.getMain().getDataStatus().getSimulationTime();
        var heuristicsName = controllerNavigation.getMain().getDataStatus().getHeuristics().getName();
        var reportsFolder = controllerNavigation.getMain().getDataStatus().getReportsFolder();
        String folderName = "results-";
        var scaleAdjustment = 1 / mapResolution;
        var reportsTimeIntervalInSeconds = 0.1; //FIXME: HARD CODED

        for (var vehicle : controllerNavigation.getMain().getDataStatus().getProjectData().getVehicles()) {
            AbstractVehicle newVehicle;
            if ("Human".equals(vehicle.getType()))
                newVehicle = new LookAheadVehicle(vehicle.getID(),
                        vehicle.getName(),
                        vehicle.getLookAheadDistance() / scaleAdjustment,
                        vehicle.getPriority(),
                        Utils.stringToColor(vehicle.getColor()),
                        vehicle.getMaxVelocity() / scaleAdjustment,
                        vehicle.getMaxAcceleration() / scaleAdjustment,
                        vehicle.getLength() / scaleAdjustment,
                        vehicle.getWidth() / scaleAdjustment,
                        controllerNavigation.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                        vehicle.getSafetyDistance() / scaleAdjustment,
                        vehicle.getTaskRepetition(), model);
            else newVehicle = new AutonomousVehicle(vehicle.getID(),
                    vehicle.getName(),
                    vehicle.getPriority(),
                    Utils.stringToColor(vehicle.getColor()),
                    vehicle.getMaxVelocity() / scaleAdjustment,
                    vehicle.getMaxAcceleration() / scaleAdjustment,
                    vehicle.getLength() / scaleAdjustment,
                    vehicle.getWidth() / scaleAdjustment,
                    controllerNavigation.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                    vehicle.getSafetyDistance() / scaleAdjustment,
                    vehicle.getTaskRepetition(), model);

            newVehicle.setGoals(vehicle.getTask()
                    .stream()
                    .map(ProjectData.TaskStep::getPoseName)
                    .map(poseName -> controllerNavigation.getMain().getDataStatus().getProjectData().getPose(poseName))
                    .toArray(Pose[]::new));

            controllerNavigation.getMain().getDataStatus().getVehicles().add(newVehicle);
        }

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(10.0, 1.0);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setBreakDeadlocks(true, false, false);
        tec.setDefaultFootprint(controllerNavigation.getMain().getDataStatus().getVehicles().get(0).getFootprint());
        tec.addComparator(controllerNavigation.getMain().getDataStatus().getHeuristics().getComparator());

        for (var vehicle : controllerNavigation.getMain().getDataStatus().getVehicles()) {
            vehicle.loadPlans(controllerNavigation.getMain().getDataStatus().getProjectData().getVehicle(vehicle.getID()).getPathFile());
        }

        tec.placeRobotsAtStartPoses();

        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setInitialTransform(9.0, 23.75, 43.40);
        viz.setFontScale(3.5);
        tec.setVisualization(viz);

        Missions.setMap(map);
        Missions.generateMissions();

        if (writeReports)
            RobotReportWriter.writeReports(tec, reportsTimeIntervalInSeconds, simulationTime, heuristicsName, reportsFolder, folderName, scaleAdjustment);
        Missions.runTasks(tec, simulationTime);
    }
}