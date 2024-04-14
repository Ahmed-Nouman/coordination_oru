package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Missions;
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
            tec.setForwardModel(vehicle.getID(), new ConstantAcceleration(vehicle.getMaxAcceleration(),
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
//        viz.setInitialTransform(9.0, 11.0, 5.0);
        viz.setInitialTransform(9.0, 23.75, 43.40);
        viz.setFontScale(3.5);
        tec.setVisualization(viz);

        Missions.setMap(YAML_FILE);
        String fileName = "HEHE"; //FIXME:
        Missions.startMissionDispatcher(tec, controllerNavigation.getMain().getDataStatus().getWriteVehicleReports(), reportsTimeIntervalInSeconds,
                controllerNavigation.getMain().getDataStatus().getSimulationTime(), controllerNavigation.getMain().getDataStatus().getHeuristics().getName(),
                controllerNavigation.getMain().getDataStatus().getReportsFolder(), fileName, scaleAdjustment);
    }
}