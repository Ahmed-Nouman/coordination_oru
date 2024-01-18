package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;

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
        var future = executorService.scheduleAtFixedRate(() -> {
            if (runCount.incrementAndGet() <= controllerNavigation.getMain().getDataStatus().getNumberOfRuns()) {
                runProject();
            } else {
                executorService.shutdown();
            }
        }, 0, controllerNavigation.getMain().getDataStatus().getSimulationTime(), TimeUnit.MINUTES);

        executorService.schedule(() -> {
            future.cancel(true);
        }, controllerNavigation.getMain().getDataStatus().getSimulationTime(), TimeUnit.MINUTES);
    }

    public void runProject() {

        final var YAML_FILE = controllerNavigation.getMain().getDataStatus().getProjectData().getMap();
        var mapResolution = controllerNavigation.getMain().getDataStatus().getMapData().getResolution();
        var scaleAdjustment = 1 / mapResolution;
        var reportsTimeIntervalInSeconds = 0.1;

        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();
        tec.addComparator(controllerNavigation.getMain().getDataStatus().getHeuristics().getComparator());
        tec.setBreakDeadlocks(true, false, false);

        controllerNavigation.getMain().getDataStatus().getVehicles().forEach((vehicle) -> {
            tec.setForwardModel(vehicle.getID(), new ConstantAccelerationForwardModel(vehicle.getMaxAcceleration(),
                    vehicle.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                    tec.getRobotTrackingPeriodInMillis(vehicle.getID())));
            tec.setDefaultFootprint(vehicle.getFootprint());

            tec.placeRobot(vehicle.getID(), vehicle.getInitialPose());

            var mission = new Mission(vehicle.getID(), vehicle.getPath());
            Missions.enqueueMission(mission);
        });

        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
//        viz.AccessInitialTransform();
        viz.setInitialTransform(9.0, 11.0, 5.0);
        viz.setFontScale(3.5);
        tec.setVisualization(viz);

        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec, controllerNavigation.getMain().getDataStatus().getWriteVehicleReports(), reportsTimeIntervalInSeconds,
                controllerNavigation.getMain().getDataStatus().getSimulationTime(), controllerNavigation.getMain().getDataStatus().getHeuristics().getName(), 100,
                controllerNavigation.getMain().getDataStatus().getReportsFolder(), scaleAdjustment);
    }
}