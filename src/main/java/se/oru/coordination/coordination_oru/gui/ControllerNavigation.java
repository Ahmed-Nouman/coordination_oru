package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static se.oru.coordination.coordination_oru.gui.Utils.*;
import static se.oru.coordination.coordination_oru.gui.Utils.writeJSON;

public class ControllerNavigation {
    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    private final Button saveButton = new Button("Save");
    private final Button resetButton = new Button("Reset");
    private final Button runButton = new Button("Run");
    private final Main main;
    private SceneState currentSceneState = SceneState.HOME;

    public ControllerNavigation(Main main) {
        this.main = main;
    }

    public void getNavigationController() {
        main.getNavigationButton().clickNext();
        main.getNavigationButton().clickBack();
        main.getNavigationButton().clickSaved();
        main.getNavigationButton().clickReset();
        main.getNavigationButton().clickRun();
    }

    public void updateScene(SceneState newScene) {
        setScene(newScene);
        newScene.update(main);
    }

    public void clickBack() {
        getBackButton().setOnAction(e -> {
            var currentScene = getCurrentScene();
            var backState = currentScene.getBackState();
            if (backState != null) {
                updateScene(backState);
            }
        });
    }

    public void clickNext() {
        getNextButton().setOnAction(e -> {
            var currentScene = getCurrentScene();
            var nextState = currentScene.getNextState();
            if (nextState != null) {
                updateScene(nextState);
            }
        });
    }

    private void verifySavePlans() {
        System.out.println("Saving plans...");
        for (var vehicle : main.getDataStatus().getProjectData().getVehicles()) {
            var autonomousVehicle = new AutonomousVehicle();
            autonomousVehicle.setName(vehicle.getName());
            autonomousVehicle.setInitialPose(main.getDataStatus().getProjectData().getPose(vehicle.getInitialPose()));
            autonomousVehicle.setGoalPoses(vehicle.getMission()
                .stream()
                .map(ProjectData.MissionStep::getPoseName)
                .map(poseName -> main.getDataStatus().getProjectData().getPose(poseName))
                .toArray(Pose[]::new));
            autonomousVehicle.getPlan(autonomousVehicle.getInitialPose(),
                autonomousVehicle.getGoalPoses(), main.getDataStatus().getProjectData().getMap(), true);
        }
    }

    public void clickSaved() {
        getSaveButton().setOnAction(e -> trySaveProject());
    }

    public void trySaveProject() {
        try {
            boolean isProjectUnchanged = main.getDataStatus().getProjectData().equals(main.getDataStatus().getOriginalProjectData());
            if (isProjectUnchanged) doNotSaveProject();
            else saveProject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doNotSaveProject() {
        AlertBox.display("Saving the Project", "There are no changes to save in the project: " + main.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
    }

    private void saveProject() throws IOException {
        var file = createFile(main, "project", "json");
        if (file != null) {
            main.getDataStatus().setProjectFile(file.getAbsolutePath());
            writeJSON(main.getDataStatus().getProjectData(), main.getDataStatus().getProjectFile());
            AlertBox.display("Saving the Project", "The project has been saved to: " + main.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
        }
    }


    public void clickReset() {
        getResetButton().setOnAction(e -> {
            updateScene(SceneState.HOME);
            main.getNavigationButton().nextButton.setDisable(true);
            main.getHomeScene().getFilePath().setText("");
        });
    }

    public void clickRun() {
        getRunButton().setOnAction(e -> {
            // Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            // Schedule the task to run n times with an interval of one minute
            var runCount = new AtomicInteger(0);
            ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() -> {
                if (runCount.incrementAndGet() <= main.getDataStatus().getNumberOfRuns()) {
                    runProject(); // Run your task
                } else {
                    executorService.shutdown(); // Shutdown the executor after n executions
                }
            }, 0, main.getDataStatus().getSimulationTime(), TimeUnit.MINUTES);

            // Optional: If you want to cancel the execution after 1 minute
            executorService.schedule(() -> {
                future.cancel(true); // This will interrupt the running task
            }, main.getDataStatus().getSimulationTime(), TimeUnit.MINUTES);
        });
    }

    public void runProject() {

        final String YAML_FILE = main.getDataStatus().getProjectData().getMap();
        double mapResolution = main.getDataStatus().getMapData().getResolution();
        double scaleAdjustment = 1 / mapResolution;
        double lookAheadDistance = 45 / scaleAdjustment;
        double reportsTimeIntervalInSeconds = 0.1;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        tec.addComparator(main.getDataStatus().getHeuristics().getComparator());

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(3.5);
        viz.AccessInitialTransform();
        tec.setVisualization(viz);

        main.getDataStatus().getProjectData().getVehicles().forEach((vehicle) -> {

            AbstractVehicle newVehicle;
            if (vehicle.getType().equals("Autonomous")) {
                newVehicle = new AutonomousVehicle();
            } else {
                newVehicle = new LookAheadVehicle();
                ((LookAheadVehicle) newVehicle).setLookAheadDistance(lookAheadDistance);
            }

            newVehicle.setID(vehicle.getID());
            newVehicle.setName(vehicle.getName());
            newVehicle.setLength(vehicle.getLength() / scaleAdjustment);
            newVehicle.setWidth(vehicle.getWidth() / scaleAdjustment);
            newVehicle.setMaxVelocity(vehicle.getMaxVelocity() / scaleAdjustment);
            newVehicle.setMaxAcceleration(vehicle.getMaxAcceleration() / scaleAdjustment);
            newVehicle.setSafetyDistance(vehicle.getSafetyDistance() / scaleAdjustment);
            newVehicle.setColor(stringToColor(vehicle.getColor()));
            newVehicle.setInitialPose(main.getDataStatus().getProjectData().getPose(vehicle.getInitialPose()));
            newVehicle.setGoalPoses(vehicle.getMission()
                    .stream()
                    .map(ProjectData.MissionStep::getPoseName)
                    .map(poseName -> main.getDataStatus().getProjectData().getPose(poseName))
                    .toArray(Pose[]::new));
//            newVehicle.setMission(vehicle.getMission()); //FIXME Fix Mission, How to handle multiple missions to GoalPoses, handle stoppages
            newVehicle.setMissionRepetition(vehicle.getMissionRepetition()); //FIXME Handle Mission Repetitions in missionsDispatcher

            newVehicle.getPlan(newVehicle.getInitialPose(),
                    newVehicle.getGoalPoses(), YAML_FILE, true);

            tec.setForwardModel(newVehicle.getID(), new ConstantAccelerationForwardModel(newVehicle.getMaxAcceleration(),
                    newVehicle.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                    tec.getRobotTrackingPeriodInMillis(newVehicle.getID())));
            tec.setDefaultFootprint(newVehicle.getFootprint());

            tec.placeRobot(newVehicle.getID(), newVehicle.getInitialPose());

            var mission = new Mission(newVehicle.getID(), newVehicle.getPath());
            Missions.enqueueMission(mission);
        });
        Missions.setMap(YAML_FILE);
        Missions.startMissionDispatchers(tec, main.getDataStatus().getWriteVehicleReports(), reportsTimeIntervalInSeconds,
                main.getDataStatus().getSimulationTime(), main.getDataStatus().getHeuristics().getName(), 100,
                main.getDataStatus().getReportsFolder(), scaleAdjustment);
    }

    public SceneState getCurrentScene() {
        return currentSceneState;
    }

    public void setScene(SceneState sceneState) {
        this.currentSceneState = sceneState;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getRunButton() {
        return runButton;
    }
}
