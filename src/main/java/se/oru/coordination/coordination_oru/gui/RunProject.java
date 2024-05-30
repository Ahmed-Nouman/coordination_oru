package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReportWriter;

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
        var mapResolution = controllerNavigation.getMain().getDataStatus().getMapData().getResolution();
        var writeReports = controllerNavigation.getMain().getDataStatus().getWriteVehicleReports();
        var simulationTime = controllerNavigation.getMain().getDataStatus().getSimulationTime();
        var heuristicsName = controllerNavigation.getMain().getDataStatus().getHeuristics().getName();
        var reportsFolder = controllerNavigation.getMain().getDataStatus().getReportsFolder();
        String folderName = "results-";
        var scaleAdjustment = 1 / mapResolution;
        var reportsTimeIntervalInSeconds = 0.1;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation(10.0, 1.0);
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.setBreakDeadlocks(true, false, false);
        tec.setDefaultFootprint(controllerNavigation.getMain().getDataStatus().getVehicles().get(0).getFootprint());
        tec.addComparator(controllerNavigation.getMain().getDataStatus().getHeuristics().getComparator());
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