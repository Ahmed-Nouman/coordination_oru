package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NavigationBar {
    public enum SceneState {
        PROJECT, MAP, VEHICLE, SIMULATION, NONE
    }

    private SceneState currentScene = SceneState.NONE;
    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    private final Button saveButton = new Button("Save");
    private final Button resetButton = new Button("Reset");
    private final Button runButton = new Button("Run");

    public void getNavigationController(Stage primaryStage, GUI gui) {
        gui.getNavigationBar().nextClicked(primaryStage, gui);
        gui.getNavigationBar().backClicked(primaryStage, gui);
        gui.getNavigationBar().saveClicked(gui);
        gui.getNavigationBar().runClicked(gui);
        gui.getNavigationBar().resetClicked(primaryStage, gui);
    }

    public void updateScene(SceneState newScene, Stage primaryStage, GUI gui) {
        setCurrentScene(newScene);
        switch (newScene) {
            case PROJECT:
                primaryStage.setTitle("Coordination_ORU");
                primaryStage.setScene(gui.getHomeScene().get());
                primaryStage.centerOnScreen();
                break;
            case MAP:
                primaryStage.setTitle("Coordination_ORU: Setting up the map");
                primaryStage.setScene(gui.getMapScene().get());
                primaryStage.centerOnScreen();
                break;
            case VEHICLE:
                primaryStage.setTitle("Coordination_ORU: Setting up the vehicles");
                primaryStage.setScene(gui.getVehicleScene().get());
                primaryStage.centerOnScreen();
                break;
            case SIMULATION:
                primaryStage.setTitle("Coordination_ORU: Setting up the simulation");
                primaryStage.setScene(gui.getSimulationScene().get());
                primaryStage.centerOnScreen();
                break;
            case NONE:
            default:
                break;
        }
    }

    public void backClicked(Stage primaryStage, GUI gui) {
        getBackButton().setOnAction(e -> {
            switch (getCurrentScene()) {
                case MAP:
                    updateScene(SceneState.PROJECT, primaryStage, gui);
                    break;
                case VEHICLE:
                    updateScene(SceneState.MAP, primaryStage, gui);
                    break;
                case SIMULATION:
                    updateScene(SceneState.VEHICLE, primaryStage, gui);
                    break;
                default:
                    break;
            }
        });
    }

    public void nextClicked(Stage primaryStage, GUI gui) {
        getNextButton().setOnAction(e -> {
            switch (getCurrentScene()) {
                case PROJECT:
                    updateScene(SceneState.MAP, primaryStage, gui);
                    break;
                case MAP:
                    updateScene(SceneState.VEHICLE, primaryStage, gui);
                    break;
                case VEHICLE:
                    updateScene(SceneState.SIMULATION, primaryStage, gui);
                    break;
                default:
                    break;
            }
        });
    }

    public void saveClicked(GUI gui) {
        getSaveButton().setOnAction(e -> gui.getSimulationScene().saveProject(gui));
    }

    public void resetClicked(Stage primaryStage, GUI gui) {
        getResetButton().setOnAction(e -> gui.getSimulationScene().resetProject(primaryStage, gui));
    }

    public void runClicked(GUI gui) {
        getRunButton().setOnAction(e -> {
            // Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            // Schedule the task to run n times with an interval of one minute
            AtomicInteger runCount = new AtomicInteger(0);
            ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() -> {
                if (runCount.incrementAndGet() <= gui.getDataStatus().getNumberOfRuns()) {
                    gui.getSimulationScene().runProject(gui.getDataStatus()); // Run your task
                } else {
                    executorService.shutdown(); // Shutdown the executor after n executions
                }
            }, 0, gui.getDataStatus().getSimulationTime(), TimeUnit.MINUTES);

            // Optional: If you want to cancel the execution after 1 minute
            executorService.schedule(() -> {
                future.cancel(true); // This will interrupt the running task
            }, gui.getDataStatus().getSimulationTime(), TimeUnit.MINUTES);
        });
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
