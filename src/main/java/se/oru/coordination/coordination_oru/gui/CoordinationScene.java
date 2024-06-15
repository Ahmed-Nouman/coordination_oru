package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.gui.ProjectData.TaskStep;
import se.oru.coordination.coordination_oru.gui.ProjectData.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int WIDTH = 220;

    private Text trafficControlStrategy;
    private ChoiceBox<String> trafficControlStrategyField;
    private ChoiceBox<String> selectTriggerVehicleChoiceBox;
    private ListView<String> selectMissionTriggerListView;
    private ChoiceBox<String> vehiclesToStopChoiceBox;

    private final Main main;
    private BorderPane pane;

    public CoordinationScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        centerPane();
        navigationBar();
        return new Scene(pane);
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.COORDINATION));
    }

    private void centerPane() {
        var centerPane = initializeCenterPane();
        setupCenterPane(centerPane);
        controllers();
    }

    private GridPane initializeCenterPane() {
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(PADDING));
        BorderPane.setMargin(centerPane, new Insets(PADDING));
        centerPane.setHgap(SPACING);
        centerPane.setVgap(SPACING);
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        return centerPane;
    }

    private void setupCenterPane(GridPane centerPane) {
        setupTexts();
        setupFields();
        addChildren(centerPane);
    }

    private void setupTexts() {
        trafficControlStrategy = text("Traffic Control Strategy: ", 0, 0);
    }

    private Text text(String name, int column, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, column, row);
        return text;
    }

    private void setupFields() {
        var controlStrategies = List.of("Mixed Traffic", "Shutdown", "Slow Down Vehicles");
        trafficControlStrategyField = choiceBox(controlStrategies, 0);

        // Create additional UI elements
        selectTriggerVehicleChoiceBox = new ChoiceBox<>();
        selectMissionTriggerListView = new ListView<>();
        vehiclesToStopChoiceBox = new ChoiceBox<>();

        // Add a listener to the ChoiceBox to update the UI based on the selected item
        trafficControlStrategyField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateVisibility(newValue));
    }

    private ChoiceBox<String> choiceBox(List<String> items, int row) {
        var choiceBox = new ChoiceBox<String>();
        choiceBox.getItems().addAll(items);
        choiceBox.setMaxWidth(WIDTH);
        choiceBox.setValue(items.stream().findFirst().orElse(null));
        GridPane.setConstraints(choiceBox, 1, row);
        return choiceBox;
    }

    private void addChildren(GridPane centerPane) {
        centerPane.getChildren().addAll(trafficControlStrategy, trafficControlStrategyField);
    }

    private void updateVisibility(String selectedOption) {
        // Clear previous dynamic elements
        GridPane centerPane = (GridPane) pane.getCenter();
        centerPane.getChildren().clear();
        addChildren(centerPane); // Add the initial children back

        // Show the selected element
        if (selectedOption != null) {
            switch (selectedOption) {
                case "Mixed Traffic":
                    handleMixedTraffic();
                    break;
                case "Shutdown":
                    handleShutdown(centerPane);
                    break;
                case "Slow Down Vehicles":
                    handleSlowDownVehicles(centerPane);
                    break;
            }
        }
    }

    private void handleMixedTraffic() {
        // Handle the "Mixed Traffic" option
        // Add your code here if you want to do something specific for "Mixed Traffic"
    }

    private void handleShutdown(GridPane centerPane) {
        Label selectTriggerVehicleLabel = new Label("Select Trigger Vehicle");
        selectTriggerVehicleChoiceBox = new ChoiceBox<>();
        selectTriggerVehicleChoiceBox.getItems().addAll(main.getDataStatus().getProjectData().getVehicleNames());
        selectTriggerVehicleChoiceBox.setMaxWidth(WIDTH);

        Label selectMissionTriggerLabel = new Label("Select Mission Trigger");
        selectMissionTriggerListView = new ListView<>();
        selectMissionTriggerListView.setMaxWidth(WIDTH);

        Label vehiclesToStopLabel = new Label("Vehicles to Stop");
        vehiclesToStopChoiceBox = new ChoiceBox<>();
        vehiclesToStopChoiceBox.setMaxWidth(WIDTH);

        // Add a listener to update the mission trigger list view and vehicles to stop based on the selected vehicle
        selectTriggerVehicleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateMissionTriggerList(newValue);
            updateVehiclesToStopList(newValue);
        });

        centerPane.add(selectTriggerVehicleLabel, 0, 1);
        centerPane.add(selectTriggerVehicleChoiceBox, 1, 1);
        centerPane.add(selectMissionTriggerLabel, 0, 2);
        centerPane.add(selectMissionTriggerListView, 1, 2);
        centerPane.add(vehiclesToStopLabel, 0, 3);
        centerPane.add(vehiclesToStopChoiceBox, 1, 3);
    }

    private void updateMissionTriggerList(String vehicleName) {
        if (vehicleName != null) {
            Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicles().stream()
                    .filter(vehicle -> vehicleName.equals(vehicle.getName()))
                    .findFirst()
                    .orElse(null);

            if (selectedVehicle != null) {
                List<String> tasks = selectedVehicle.getTasks().stream()
                        .map(TaskStep::getTaskName)
                        .collect(Collectors.toList());
                selectMissionTriggerListView.getItems().setAll(tasks);
            }
        }
    }

    private void updateVehiclesToStopList(String vehicleName) {
        if (vehicleName != null) {
            List<String> otherVehicles = main.getDataStatus().getProjectData().getVehicles().stream()
                    .map(Vehicle::getName)
                    .filter(name -> !name.equals(vehicleName))
                    .collect(Collectors.toList());

            vehiclesToStopChoiceBox.getItems().setAll(otherVehicles);

            if (otherVehicles.isEmpty()) {
                vehiclesToStopChoiceBox.getItems().add("No other vehicles to stop");
            }
        }
    }

    private void handleSlowDownVehicles(GridPane centerPane) {
        Label slowDownLabel = new Label("Slow Down Vehicles Option");
        centerPane.add(slowDownLabel, 0, 1);
    }

    private Button createOptionButton() {
        var button = new Button("Option 2 Button");
        return button;
    }

    private ChoiceBox<String> createOptionChoiceBox() {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Sub-option 1", "Sub-option 2", "Sub-option 3");
        choiceBox.setMaxWidth(WIDTH);
        return choiceBox;
    }

    private void controllers() {
        // Implement your controller logic here
    }

    public Main getMain() {
        return main;
    }

    public BorderPane getPane() {
        return pane;
    }
}
