package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private final DataStatus dataStatus = new DataStatus();
    private final SceneHome sceneHome = new SceneHome(this);
    private final SceneMap sceneMap = new SceneMap(this);
    private final SceneVehicle sceneVehicle = new SceneVehicle(this);
    private final SceneSetup sceneSetup = new SceneSetup(this);
    private final ControllerNavigation controllerNavigation = new ControllerNavigation(this);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeStage();
        controllerNavigation.getNavigationController();
    }

    private void initializeStage() {
        primaryStage.setTitle("Coordination_ORU");
        primaryStage.setOnCloseRequest(e -> closeProgram());
        primaryStage.setScene(getHomeScene().get());
        primaryStage.show();
    }

    protected void closeProgram() {
        if (getDataStatus().getProjectData() == null) {
            primaryStage.close();
        } else if (getDataStatus().getProjectData().equals(getDataStatus().getOriginalProjectData())) {
            primaryStage.close();
        } else {
            var answer = AlertBox.display("Saving the project", "Would you like to save the project before exiting?", Alert.AlertType.CONFIRMATION);
            if (answer.isPresent() && answer.get() == ButtonType.YES && getDataStatus().getProjectData() != null) getNavigationButton().trySaveProject();
            primaryStage.close();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ControllerNavigation getNavigationButton() {
        return controllerNavigation;
    }

    public DataStatus getDataStatus() {
        return dataStatus;
    }

    public SceneHome getHomeScene() {
        return sceneHome;
    }

    public SceneMap getMapScene() {
        return sceneMap;
    }

    public SceneVehicle getVehicleScene() {
        return sceneVehicle;
    }

    public SceneSetup getSimulationScene() {
        return sceneSetup;
    }
}

