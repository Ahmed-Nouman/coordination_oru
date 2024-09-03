package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.AdaptiveTrackerRK4;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReportWriter;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length - 1].getFileName().split("\\.")[0];
        String pathsFolderName = "paths/" + className + "/";
        var map = navigationController.getMain().getDataStatus().getProjectData().getMap();
        var model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
        var mapResolution = navigationController.getMain().getDataStatus().getMapData().getResolution();
        var writeReports = navigationController.getMain().getDataStatus().getWriteVehicleReports();
        var simulationTime = navigationController.getMain().getDataStatus().getSimulationTime();
        var heuristicsName = navigationController.getMain().getDataStatus().getHeuristics().getName();
        var reportsFolder = navigationController.getMain().getDataStatus().getReportsFolder();
        String folderName = "results-";
        var scaleAdjustment = 1 / mapResolution;
        var reportsTimeIntervalInSeconds = 0.1;

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

            var filePath = navigationController.getMain().getDataStatus().getProjectFile();
            var parts = filePath.split("/");
            var lastPart = parts[parts.length - 1];
            var projectName = lastPart.split("\\.")[0];

            // Determine the highest numbered path file
            AbstractVehicle finalNewVehicle = newVehicle;
            String highestNumberedPathFile = Arrays.stream(new File(pathsFolderName).listFiles())
                    .map(File::getName)
                    .filter(name -> name.startsWith(finalNewVehicle.getName() + "(") && name.endsWith(").path"))
                    .max(Comparator.comparingInt(name -> {
                        Matcher m = Pattern.compile("\\((\\d+)\\)\\.path$").matcher(name);
                        return m.find() ? Integer.parseInt(m.group(1)) : -1;
                    }))
                    .orElse(newVehicle.getName() + ".path");

            if (!(newVehicle.getTasks().isEmpty()))
                newVehicle.loadPlans(pathsFolderName + projectName + "/" + highestNumberedPathFile);
        }

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(10.0, 1.0);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setBreakDeadlocks(true, false, false);
        tec.setFootprints(); //FIXME: This needs to be checked in the GUI
        tec.addComparator(navigationController.getMain().getDataStatus().getHeuristics().getComparator());

        for (var vehicle : navigationController.getMain().getDataStatus().getVehicles()) {
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

        // Shutdown Logic
        var trafficControl = navigationController.getMain().getDataStatus().getProjectData().getTrafficControl();
        var triggers = navigationController.getMain().getDataStatus().getProjectData().getTriggers();
        var originalHeuristics = navigationController.getMain().getDataStatus().getHeuristics();
        var newHeuristics = navigationController.getMain().getDataStatus().getNewHeuristics();

        Function<Integer, AbstractTrajectoryEnvelopeTracker> trackerRetriever = vehicleId -> tec.trackers.get(vehicleId);

        for (var trigger : triggers) {
            var triggerVehicleID = navigationController.getMain().getDataStatus().getProjectData().getVehicleID(trigger.getVehicle(), navigationController.getMain().getDataStatus().getProjectData().getVehicles());
            var triggerVehicle = (AutonomousVehicle) navigationController.getMain().getDataStatus().getVehicles().get(triggerVehicleID - 1); // Vehicle ID starts from 1
            var taskNames = trigger.getTask(); // Get the task names from the trigger

            List<Integer> triggerTasks = taskNames.stream()
                    .map(taskName -> {
                        List<Task> tasks = triggerVehicle.getTasks();
                        for (int i = 0; i < tasks.size(); i++) {
                            if (tasks.get(i).getName().equals(taskName)) { // Use the getName() method to get the task name
                                return i; // Return the index of the task
                            }
                        }
                        return -1; // Return -1 if not found (you may handle this case as needed)
                    })
                    .collect(Collectors.toList());

            List<Integer> complyVehicles = trigger.getVehicleToComply().stream()
                    .map(vehicleName -> navigationController.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, navigationController.getMain().getDataStatus().getProjectData().getVehicles()))
                    .collect(Collectors.toList());

            switch (trafficControl) {
                case "Mixed Traffic":
                    System.out.println("Mixed Traffic");
                    break;
                case "Shutdown":
                    System.out.println("Shutdown");
                    AdaptiveTrackerRK4.scheduleVehiclesStop(triggerVehicle, triggerTasks, complyVehicles, trackerRetriever);
                    break;
                case "Vehicle Speed Change":
                    System.out.println("Vehicle Speed Change");
                    AdaptiveTrackerRK4.scheduleVehicleSlow(triggerVehicle, triggerTasks, complyVehicles, trackerRetriever, triggerVehicle.getMaxVelocity(), triggerVehicle.getMaxVelocity() * 0.5); // example speed reduction
                    break;
                case "Priority Rule Change":
                    System.out.println("Priority Rule Change");
                    AdaptiveTrackerRK4.scheduleVehiclesPriorityChange(triggerVehicle, triggerTasks, tec, originalHeuristics, newHeuristics);
                    break;
            }
        }
    }
}
