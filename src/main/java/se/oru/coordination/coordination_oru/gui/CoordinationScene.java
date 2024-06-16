package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.gui.ProjectData.TaskStep;
import se.oru.coordination.coordination_oru.gui.ProjectData.Vehicle;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int HEIGHT = 600;
    public static final int WIDTH = 600;

    private Text priorityRule;
    private ChoiceBox<String> priorityRuleField;
    private Text trafficControlStrategy;
    private ChoiceBox<String> trafficControlStrategyField;
    private ListView<String> selectTriggerVehicleField;
    private ListView<String> selectMissionTriggerField;
    private ListView<String> vehiclesToComplyField;
    private TextField velocityChangeRatioField;

    private final Main main;
    private final CoordinationController controller = new CoordinationController(this);
    private BorderPane pane;

    private CoordinationData coordinationData = new CoordinationData();

    public CoordinationScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        centerPane();
        navigationBar();
        return new Scene(pane, WIDTH, HEIGHT);
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
        priorityRule = text("Priority Rule: ", 0, 0);
        trafficControlStrategy = text("Traffic Control Strategy: ", 0, 1);
    }

    private Text text(String name, int column, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, column, row);
        return text;
    }

    private void setupFields() {
        var priorityRules = Heuristics.getHeuristicNames();
        priorityRuleField = choiceBox(priorityRules, 0);
        // Set the default value
        if (!priorityRules.isEmpty()) {
            priorityRuleField.setValue(priorityRules.get(0));
            coordinationData.setPriorityRule(priorityRules.get(0)); // Update CoordinationData
        }

        var controlStrategies = List.of("Mixed Traffic", "Shutdown", "Change in Speeds", "Change in Priority Rule");
        trafficControlStrategyField = choiceBox(controlStrategies, 1);

        // Create additional UI elements
        selectTriggerVehicleField = new ListView<>();
        selectMissionTriggerField = new ListView<>();
        vehiclesToComplyField = new ListView<>();
        velocityChangeRatioField = new TextField();
        velocityChangeRatioField.setMaxWidth(WIDTH);

        selectMissionTriggerField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vehiclesToComplyField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add listeners to update CoordinationData and ProjectData
        trafficControlStrategyField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            coordinationData.setTrafficControlStrategy(newValue);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
            updateVisibility(newValue);
        });

        priorityRuleField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            coordinationData.setPriorityRule(newValue);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
        });

        velocityChangeRatioField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                coordinationData.setVelocityChangeRatio(Double.parseDouble(newValue));
            } catch (NumberFormatException e) {
                // Handle invalid input
                coordinationData.setVelocityChangeRatio(0);
            }
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
        });
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
        centerPane.getChildren().addAll(priorityRule, priorityRuleField, trafficControlStrategy, trafficControlStrategyField);
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
                case "Change in Speeds":
                    handleSpeedChangeVehicles(centerPane);
                    break;
                case "Change in Priority Rule":
                    handlePriorityRuleChange(centerPane);
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
        selectTriggerVehicleField = new ListView<>();
        selectTriggerVehicleField.getItems().addAll(main.getDataStatus().getProjectData().getVehicleNames());
        selectTriggerVehicleField.setMaxWidth(WIDTH);

        Label selectMissionTriggerLabel = new Label("Select Mission Trigger");
        selectMissionTriggerField = new ListView<>();
        selectMissionTriggerField.setMaxWidth(WIDTH);

        Label vehiclesToStopLabel = new Label("Vehicles to Stop");
        vehiclesToComplyField = new ListView<>();
        vehiclesToComplyField.setMaxWidth(WIDTH);

        // Add a listener to update the mission trigger list view and vehicles to stop based on the selected vehicle
        selectTriggerVehicleField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            coordinationData.setTriggerVehicle(newValue);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
            updateMissionTriggerList(newValue);
            updateVehiclesToStopList(newValue);
        });

        centerPane.add(selectTriggerVehicleLabel, 0, 2);
        centerPane.add(selectTriggerVehicleField, 1, 2);
        centerPane.add(selectMissionTriggerLabel, 0, 3);
        centerPane.add(selectMissionTriggerField, 1, 3);
        centerPane.add(vehiclesToStopLabel, 0, 4);
        centerPane.add(vehiclesToComplyField, 1, 4);
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
                selectMissionTriggerField.getItems().setAll(tasks);
                selectMissionTriggerField.getSelectionModel().clearSelection(); // Clear selection by default
                List<Integer> taskIndices = IntStream.range(0, tasks.size()).boxed().collect(Collectors.toList());
                coordinationData.setMissionTriggers(taskIndices);
                main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
            }
        }
    }

    private void updateVehiclesToStopList(String vehicleName) {
        if (vehicleName != null) {
            List<String> otherVehicles = main.getDataStatus().getProjectData().getVehicles().stream()
                    .map(Vehicle::getName)
                    .filter(name -> !name.equals(vehicleName))
                    .collect(Collectors.toList());

            vehiclesToComplyField.getItems().setAll(otherVehicles);
            coordinationData.setVehiclesToComply(otherVehicles);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);

            if (otherVehicles.isEmpty()) {
                vehiclesToComplyField.getItems().add("No other vehicles to stop");
            }
        }
    }

    private void handleSpeedChangeVehicles(GridPane centerPane) {
        Label selectTriggerVehicleLabel = new Label("Select Trigger Vehicle");
        selectTriggerVehicleField = new ListView<>();
        selectTriggerVehicleField.getItems().addAll(main.getDataStatus().getProjectData().getVehicleNames());
        selectTriggerVehicleField.setMaxWidth(WIDTH);

        Label selectMissionTriggerLabel = new Label("Select Mission Trigger");
        selectMissionTriggerField = new ListView<>();
        selectMissionTriggerField.setMaxWidth(WIDTH);

        Label vehiclesToSlowDownLabel = new Label("Vehicles to Slow Down");
        vehiclesToComplyField = new ListView<>();
        vehiclesToComplyField.setMaxWidth(WIDTH);

        // Set selection mode to multiple for the list views
        selectMissionTriggerField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vehiclesToComplyField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add a listener to update the mission trigger list view and vehicles to slow down based on the selected vehicle
        selectTriggerVehicleField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            coordinationData.setTriggerVehicle(newValue);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);
            updateMissionTriggerList(newValue);
            updateVehiclesToSlowDownList(newValue);
        });

        centerPane.add(selectTriggerVehicleLabel, 0, 2);
        centerPane.add(selectTriggerVehicleField, 1, 2);
        centerPane.add(selectMissionTriggerLabel, 0, 3);
        centerPane.add(selectMissionTriggerField, 1, 3);
        centerPane.add(vehiclesToSlowDownLabel, 0, 4);
        centerPane.add(vehiclesToComplyField, 1, 4);
        centerPane.add(new Label("Velocity Change Ratio"), 0, 5);
        centerPane.add(velocityChangeRatioField, 1, 5); // Add text field to the grid
    }

    private void updateVehiclesToSlowDownList(String vehicleName) {
        if (vehicleName != null) {
            List<String> otherVehicles = main.getDataStatus().getProjectData().getVehicles().stream()
                    .map(Vehicle::getName)
                    .filter(name -> !name.equals(vehicleName))
                    .collect(Collectors.toList());

            vehiclesToComplyField.getItems().setAll(otherVehicles);
            coordinationData.setVehiclesToComply(otherVehicles);
            main.getDataStatus().getProjectData().setCoordinationData(coordinationData);

            if (otherVehicles.isEmpty()) {
                vehiclesToComplyField.getItems().add("No other vehicles to slow down");
            }
        }
    }

    private void handlePriorityRuleChange(GridPane centerPane) {
        Label newHeuristicsNameLabel = new Label("New Heuristics Name");
        ChoiceBox<String> newHeuristicsNameField = new ChoiceBox<>();
        newHeuristicsNameField.getItems().addAll("Heuristic A", "Heuristic B", "Heuristic C");
        newHeuristicsNameField.setMaxWidth(WIDTH);

        centerPane.add(newHeuristicsNameLabel, 0, 2);
        centerPane.add(newHeuristicsNameField, 1, 2);
    }

    private void controllers() {
        trafficControlStrategyField.setOnAction(e -> controller.chooseTrafficControlStrategy());
        priorityRuleField.setOnAction(e -> controller.chooseHeuristic());
    }

    public Main getMain() {
        return main;
    }

    public ChoiceBox<String> getPriorityRuleField() {
        return priorityRuleField;
    }

    public ChoiceBox<String> getTrafficControlStrategyField() {
        return trafficControlStrategyField;
    }

    public BorderPane getPane() {
        return pane;
    }

    public CoordinationData getCoordinationData() {
        return coordinationData;
    }
}
