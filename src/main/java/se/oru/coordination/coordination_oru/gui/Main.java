package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private final DataStatus dataStatus = new DataStatus();
    private final HomeScene homeScene = new HomeScene(this);
    private final MapScene mapScene = new MapScene(this);
    private final VehicleScene vehicleScene = new VehicleScene(this);
    //TODO: Add the coordination scene
    private final SetupScene setupScene = new SetupScene(this);
    public final NavigationController navigationController = new NavigationController(this);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeStage();
        navigationController.getNavigationController();
    }

    private void initializeStage() {
        primaryStage.setTitle("Coordination_ORU");
        primaryStage.setOnCloseRequest(e -> navigationController.closeProgram(this));
        primaryStage.setScene(homeScene.get());
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public NavigationController getNavigationButton() {
        return navigationController;
    }

    public DataStatus getDataStatus() {
        return dataStatus;
    }

    public HomeScene getHomeScene() {
        return homeScene;
    }

    public MapScene getMapScene() {
        return mapScene;
    }

    public VehicleScene getVehicleScene() {
        return vehicleScene;
    }

    public SetupScene getSetupScene() {
        return setupScene;
    }
}

