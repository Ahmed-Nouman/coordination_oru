package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReportWriter;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RunProject {
    private final NavigationController navigationController;

    public RunProject(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public void clickRun() {
        var executorService = Executors.newScheduledThreadPool(1);
        var runCount = new AtomicInteger(0);
        int simulationTime = navigationController.getMain().getDataStatus().getSimulationTime();
        int numberOfRuns = navigationController.getMain().getDataStatus().getNumberOfRuns();
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

        var map = navigationController.getMain().getDataStatus().getProjectData().getMap();
        var model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30); //FIXME: HARD CODED
        var mapResolution = navigationController.getMain().getDataStatus().getMapData().getResolution();
        var writeReports = navigationController.getMain().getDataStatus().getWriteVehicleReports();
        var simulationTime = navigationController.getMain().getDataStatus().getSimulationTime();
        var heuristicsName = navigationController.getMain().getDataStatus().getHeuristics().getName();
        var reportsFolder = navigationController.getMain().getDataStatus().getReportsFolder();
        String folderName = "results-";
        var scaleAdjustment = 1 / mapResolution;
        var reportsTimeIntervalInSeconds = 0.1; //FIXME: HARD CODED

        for (var vehicle : navigationController.getMain().getDataStatus().getProjectData().getVehicles()) {
            AbstractVehicle newVehicle = VehiclesHashMap.getVehicle(vehicle.getID());

            if (newVehicle == null) {
                if ("Human".equals(vehicle.getType())) {
                    newVehicle = new LookAheadVehicle(
                            vehicle.getID(),
                            vehicle.getName(),
                            vehicle.getLookAheadDistance() / scaleAdjustment,
                            vehicle.getPriority(),
                            Utils.stringToColor(vehicle.getColor()),
                            vehicle.getMaxVelocity() / scaleAdjustment,
                            vehicle.getMaxAcceleration() / scaleAdjustment,
                            vehicle.getLength() / scaleAdjustment,
                            vehicle.getWidth() / scaleAdjustment,
                            navigationController.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                            vehicle.getSafetyDistance() / scaleAdjustment,
                            vehicle.getTasksRepetition(),
                            model
                    );
                } else {
                    newVehicle = new AutonomousVehicle(
                            vehicle.getID(),
                            vehicle.getName(),
                            vehicle.getPriority(),
                            Utils.stringToColor(vehicle.getColor()),
                            vehicle.getMaxVelocity() / scaleAdjustment,
                            vehicle.getMaxAcceleration() / scaleAdjustment,
                            vehicle.getLength() / scaleAdjustment,
                            vehicle.getWidth() / scaleAdjustment,
                            navigationController.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                            vehicle.getSafetyDistance() / scaleAdjustment,
                            vehicle.getTasksRepetition(),
                            model
                    );
                }
                VehiclesHashMap.getList().put(vehicle.getID(), newVehicle);
                navigationController.getMain().getDataStatus().getVehicles().add(newVehicle);
            }

            for (var task : vehicle.getTasks()) {
                var poses = Arrays.stream(task.getPoseName().split(" -> "))
                        .map(poseName -> navigationController.getMain().getDataStatus().getProjectData().getPose(poseName.trim()))
                        .toArray(Pose[]::new);
                newVehicle.addTask(new Task(task.getTaskName(), task.getDuration(), poses, task.getPriority()), task.getRepetition());
            }
        }

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(10.0, 1.0);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setBreakDeadlocks(true, false, false);
        tec.setDefaultFootprint(navigationController.getMain().getDataStatus().getVehicles().get(0).getFootprint());
        tec.addComparator(navigationController.getMain().getDataStatus().getHeuristics().getComparator());

        for (var vehicle : navigationController.getMain().getDataStatus().getVehicles()){
            String pathFile = navigationController.getMain().getDataStatus().getProjectData().getVehicle(vehicle.getID()).getPathFile();
            if (pathFile != null) vehicle.loadPlans(pathFile);
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
