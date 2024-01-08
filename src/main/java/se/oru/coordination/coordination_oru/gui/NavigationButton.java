package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

public class NavigationButton {

    public enum SceneState {
        PROJECT, MAP, VEHICLE, SIMULATION
    }

    private SceneState currentScene = SceneState.PROJECT;
    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    private final Button saveButton = new Button("Save");
    private final Button resetButton = new Button("Reset");
    private final Button runButton = new Button("Run");

    public void getNavigationController(Stage primaryStage, Main main) {
        main.getNavigationButton().nextClicked(primaryStage, main);
        main.getNavigationButton().backClicked(primaryStage, main);
        main.getNavigationButton().saveClicked(main);
        main.getNavigationButton().resetClicked(primaryStage, main);
        main.getNavigationButton().runClicked(main);
    }

    public void updateScene(SceneState newScene, Stage primaryStage, Main main) {
        setCurrentScene(newScene);
        switch (newScene) {
            case PROJECT:
                primaryStage.setTitle("Coordination_ORU");
                primaryStage.setScene(main.getHomeScene().get());
                primaryStage.centerOnScreen();
                nextButton.setVisible(true); //FIXME: Handle logic in controllers
                nextButton.setDisable(false);
                break;
            case MAP:
                primaryStage.setTitle("Coordination_ORU: Setting up the map");
                primaryStage.setScene(main.getMapScene().get());
                primaryStage.centerOnScreen();
                break;
            case VEHICLE:
                primaryStage.setTitle("Coordination_ORU: Setting up the vehicles");
                primaryStage.setScene(main.getVehicleScene().get());
                primaryStage.centerOnScreen();
                break;
            case SIMULATION:
                primaryStage.setTitle("Coordination_ORU: Setting up the simulation");
                primaryStage.setScene(main.getSimulationScene().get());
                primaryStage.centerOnScreen();
                break;
            default:
                break;
        }
    }

    public void backClicked(Stage primaryStage, Main main) {
        getBackButton().setOnAction(e -> {
            switch (getCurrentScene()) {
                case MAP:
                    updateScene(SceneState.PROJECT, primaryStage, main);
                    break;
                case VEHICLE:
                    updateScene(SceneState.MAP, primaryStage, main);
                    break;
                case SIMULATION:
                    updateScene(SceneState.VEHICLE, primaryStage, main);
                    break;
                default:
                    break;
            }
        });
    }

    public void nextClicked(Stage primaryStage, Main main) {
        getNextButton().setOnAction(e -> {
            switch (getCurrentScene()) {
                case PROJECT:
                    updateScene(SceneState.MAP, primaryStage, main);
                    break;
                case MAP:
                    updateScene(SceneState.VEHICLE, primaryStage, main);
                    break;
                case VEHICLE:
                    updateScene(SceneState.SIMULATION, primaryStage, main);
                    break;
                default:
                    break;
            }
        });
    }

    public void saveClicked(Main main) {
        getSaveButton().setOnAction(e -> saveProject(main));
    }

    public void saveProject(Main main) {
        try{
            if (main.getDataStatus().isNewProject()) {
                AlertBox.display("Saving the project", "The project has been saved to: " + main.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                writeJSON(main.getDataStatus().getProjectData(), main.getDataStatus().getProjectFile());
            } else {
                if (main.getDataStatus().getProjectData().equals(main.getDataStatus().getOriginalProjectData())) {
                    AlertBox.display("Saving the project", "There are no changes to save in the project: " + main.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                } else {
                    var selectedFile = createFile(main, "project", "json");
                    if (selectedFile != null) {
                        main.getDataStatus().setProjectFile(selectedFile.getAbsolutePath());
                        AlertBox.display("Saving the project", "The project has been saved to: " + main.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                        writeJSON(main.getDataStatus().getProjectData(), main.getDataStatus().getProjectFile());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetClicked(Stage primaryStage, Main main) {
        getResetButton().setOnAction(e -> main.initializeStage(primaryStage));
    }

    public void runClicked(Main main) {
        getRunButton().setOnAction(e -> {
            // Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            // Schedule the task to run n times with an interval of one minute
            AtomicInteger runCount = new AtomicInteger(0);
            ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() -> {
                if (runCount.incrementAndGet() <= main.getDataStatus().getNumberOfRuns()) {
                    runProject(main.getDataStatus()); // Run your task
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

    public void runProject(DataStatus dataStatus) {

        final String YAML_FILE = dataStatus.getProjectData().getMap();
        double mapResolution = dataStatus.getMapData().getResolution();
        double scaleAdjustment = 1 / mapResolution;
        double lookAheadDistance = 45 / scaleAdjustment;
        double reportsTimeIntervalInSeconds = 0.1;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        tec.addComparator(dataStatus.getHeuristics().getComparator());

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(3.5);
        viz.AccessInitialTransform();
        tec.setVisualization(viz);

        dataStatus.getProjectData().getVehicles().forEach((vehicle) -> {

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
            newVehicle.setInitialPose(dataStatus.getProjectData().getPose(vehicle.getInitialPose()));
            newVehicle.setGoalPoses(vehicle.getMission()
                    .stream()
                    .map(ProjectData.MissionStep::getPoseName)
                    .map(poseName -> dataStatus.getProjectData().getPose(poseName))
                    .toArray(Pose[]::new));
//            newVehicle.setMission(vehicle.getMission()); // FIXME Fix Mission, How to handle multiple missions to GoalPoses, handle stoppages
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
        Missions.startMissionDispatchers(tec, dataStatus.getWriteVehicleReports(), reportsTimeIntervalInSeconds,
                dataStatus.getSimulationTime(), dataStatus.getHeuristics().getName(), 100, dataStatus.getReportsFolder(), scaleAdjustment);
    }

    public SceneState getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(SceneState scene) {
        this.currentScene = scene;
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
