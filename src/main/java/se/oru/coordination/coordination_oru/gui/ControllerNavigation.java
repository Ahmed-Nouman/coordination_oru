package se.oru.coordination.coordination_oru.gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    private final Button back = new Button("Back");
    private final Button next = new Button("Next");
    private final Button save = new Button("Save");
    private final Button reset = new Button("Reset");
    private final Button verify = new Button("Verify");
    private final Button run = new Button("Run");
    private final Main main;
    private SceneState currentSceneState = SceneState.HOME;
    public ControllerNavigation(Main main) {
        this.main = main;
    }

    public void getNavigationController() {
        main.getNavigationButton().next.setOnAction(e -> clickNext());
        main.getNavigationButton().back.setOnAction(e -> clickBack());
        main.getNavigationButton().save.setOnAction(e -> clickSave());
        main.getNavigationButton().reset.setOnAction(e -> clickReset());
        main.getNavigationButton().verify.setOnAction(e -> clickVerify());
        main.getNavigationButton().run.setOnAction(e -> clickRun());
    }

    public void updateScene(SceneState newScene) {
        setScene(newScene);
        newScene.update(main);
    }

    public void clickBack() {
        var currentScene = getCurrentScene();
        var backState = currentScene.getBackState();
        if (backState != null) {
            updateScene(backState);
        }
    }

    public void clickNext() {
        var currentScene = getCurrentScene();
        var nextState = currentScene.getNextState();
        if (nextState != null) {
            updateScene(nextState);
        }
    }

    private void clickVerify() {

        var progressDialog = progressDialog();
        var task = new Task<>() {
            @Override
            protected Void call() {
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
                    main.getDataStatus().getVehicles().add(autonomousVehicle);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("Verification succeeded");
                progressDialog.close();
                updateNavigationBar();
            }

            @Override
            protected void failed() {
                super.failed();
                System.out.println("Verification failed");
                progressDialog.close();
            }
        };
        progressDialog.show();
        new Thread(task).start();
    }

    private Stage progressDialog() {
        var progressDialog = new Stage();
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        progressDialog.setTitle("Verifying and Saving Plans");

        var progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);

        var dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.getChildren().add(progressBar);

        var dialogScene = new Scene(dialogVbox);
        progressDialog.setScene(dialogScene);
        return progressDialog;
    }

    private void updateNavigationBar() {
        main.getDataStatus().setPlansVerified(true);
        main.getSetupScene().getPane().setBottom(NavigationBar.getBar(main, SceneState.EXPERIMENT));
    }

    public void clickSave() {
        trySaveProject();
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
        updateScene(SceneState.HOME);
        main.getNavigationButton().next.setDisable(true);
        main.getHomeScene().getFilePath().setText("");
    }

    public void clickRun() {
        // Create a ScheduledExecutorService with a single thread
        var executorService = Executors.newScheduledThreadPool(1);

        // Schedule the task to run n times with an interval of one minute
        var runCount = new AtomicInteger(0);
        var future = executorService.scheduleAtFixedRate(() -> {
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
    }

    public void runProject() {

        final var YAML_FILE = main.getDataStatus().getProjectData().getMap();
        var mapResolution = main.getDataStatus().getMapData().getResolution();
        var scaleAdjustment = 1 / mapResolution;
        var lookAheadDistance = 45 / scaleAdjustment;
        var reportsTimeIntervalInSeconds = 0.1;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        tec.addComparator(main.getDataStatus().getHeuristics().getComparator());

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

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

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(3.5);
        viz.AccessInitialTransform();
        tec.setVisualization(viz);

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

    public Button getBack() {
        return back;
    }

    public Button getNext() {
        return next;
    }

    public Button getSave() {
        return save;
    }

    public Button getReset() {
        return reset;
    }

    public Button getVerify() {
        return verify;
    }

    public Button getRun() {
        return run;
    }
}
