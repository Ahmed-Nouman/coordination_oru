package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class GUI extends Application {

    private Stage primaryStage;
    private final NavigationBar navigationBar = new NavigationBar();
    private final DataStatus dataStatus = new DataStatus();
    private final HomeScene homeScene = new HomeScene(this);
    private final MapScene mapScene = new MapScene(this);
    private final VehicleScene vehicleScene = new VehicleScene(this);
    private final SimulationScene simulationScene = new SimulationScene(this);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        initializeStage(primaryStage);
        navigationBar.getNavigationController(primaryStage, this);
    }

    public void initializeStage(Stage primaryStage) {
        this.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Coordination_ORU");
        primaryStage.setOnCloseRequest(e -> closeProgram(primaryStage));
        primaryStage.setScene(getHomeScene().get());
        primaryStage.show();
        getNavigationBar().setCurrentScene(NavigationBar.SceneState.PROJECT);
    }

    protected void closeProgram(Stage stage) {
        if (getDataStatus().getProjectData() == null) {
            stage.close();
        } else if (getDataStatus().getProjectData().equals(getDataStatus().getOriginalProjectData())) {
            stage.close();
        } else {
            Optional<ButtonType> answer = AlertBox.display("Saving the project", "Would you like to save the project before exiting?", Alert.AlertType.CONFIRMATION);
            if (answer.isPresent() && answer.get() == ButtonType.YES && getDataStatus().getProjectData() != null) getSimulationScene().saveProject(this);
            stage.close();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public NavigationBar getNavigationBar() {
        return navigationBar;
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

    public SimulationScene getSimulationScene() {
        return simulationScene;
    }
}

