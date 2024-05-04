package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private final DataStatus dataStatus = new DataStatus();
    private final SceneHome sceneHome = new SceneHome(this);
    private final SceneMap sceneMap = new SceneMap(this);
    private final VehicleScene vehicleScene = new VehicleScene(this);
    private final SceneSetup sceneSetup = new SceneSetup(this);
    final ControllerNavigation controllerNavigation = new ControllerNavigation(this);

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
        primaryStage.setOnCloseRequest(e -> controllerNavigation.closeProgram(this));
        primaryStage.setScene(getHomeScene().get());
        primaryStage.show();
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

    public VehicleScene getVehicleScene() {
        return vehicleScene;
    }

    public SceneSetup getSetupScene() {
        return sceneSetup;
    }
}

