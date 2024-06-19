package se.oru.coordination.coordination_oru.gui;

import aima.core.logic.common.Token;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.control.SelectionMode.MULTIPLE;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int HEIGHT = 100;
    public static final int WIDTH = 220;

    private Text priorityRule;
    private Text trafficControl;
    private Text triggerVehicle;
    private Text triggerTasks;
    private Text triggerVelocityRatio;
    private Text newPriorityRule;
    private ChoiceBox<String> priorityRuleField;
    private ChoiceBox<String> trafficControlField;
    private ChoiceBox<String> triggerVehicleField;
    private ListView<String> triggerTasksField;
    private TextField triggerVelocityRatioField;
    private ChoiceBox<String> newPriorityRuleField;

    private final Main main;
    private BorderPane pane;
    private final CoordinationController controller = new CoordinationController(this);

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
        priorityRule = text("Priority Rule: ", 0, 0);
        trafficControl = text("Traffic Control: ", 0, 1);
        triggerVehicle = text("Trigger Vehicle: ", 0, 2);
        triggerTasks = text("Trigger Tasks: ", 0, 3);
        triggerVelocityRatio = text("Slowdown Velocity Ratio: ", 0, 4);
        newPriorityRule = text("New Priority Rule: ", 0, 5);
    }

    private Text text(String name, int column, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, column, row);
        return text;
    }

    private void setupFields() {
        priorityRuleField = choiceBox(Heuristics.getHeuristicNames(), 0);
        priorityRuleField.setValue(Heuristics.getHeuristicNames().stream().findFirst().orElse(null));

        var trafficControlStrategies = new ArrayList<>(List.of(
                "Mixed Traffic",
                "Vehicle Stoppage",
                "Vehicle Speed Change",
                "Priority Rule Change"));
        trafficControlField = choiceBox(trafficControlStrategies, 1);
        trafficControlField.setValue(trafficControlStrategies.stream().findFirst().orElse(null));
        this.getMain().getDataStatus().setTrafficControl(this.getTrafficControlField().getValue());  // Set the default traffic control strategy in DataStatus

        var vehicles = main.getDataStatus().getProjectData().getVehicleNames();
        triggerVehicleField = choiceBox(vehicles, 2);
        triggerVehicleField.setValue(vehicles.stream().findFirst().orElse(null));
        triggerVehicleField.setDisable(true);  // Initially disable for the mixed traffic
        var triggerVehicle = this.getTriggerVehicleField().getValue();
        if (triggerVehicle != null) this.getMain().getDataStatus().setTriggerVehicle(triggerVehicle);  // Set the default trigger vehicle in DataStatus

        triggerTasksField = new ListView<>();
        triggerTasksField.getSelectionModel().setSelectionMode(MULTIPLE);
        triggerTasksField.setDisable(true);
        triggerTasksField.setMaxHeight(HEIGHT);
        triggerTasksField.setMaxWidth(WIDTH);
        GridPane.setConstraints(triggerTasksField, 1, 3);

        // Add a listener to the vehicle choice box to update missions and other vehicles when a different vehicle is selected
        triggerVehicleField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateTasks(newValue);
            updateOtherVehicles(newValue);
        });

        // Add a listener to triggerMissionsField to print selected mission indices to the terminal
        triggerTasksField.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    getSelectedTaskIndices();
                }
            }
        });

        // Initially load tasks and other vehicles for the first selected vehicle
        updateTasks(triggerVehicleField.getValue());
        updateOtherVehicles(triggerVehicleField.getValue());

        triggerVelocityRatioField = new TextField();
        triggerVelocityRatioField.setDisable(true);
        triggerVelocityRatioField.setMaxWidth(WIDTH);
        GridPane.setConstraints(triggerVelocityRatioField, 1, 4);

        newPriorityRuleField = choiceBox(Heuristics.getHeuristicNames(), 5);
        newPriorityRuleField.setValue(Heuristics.getHeuristicNames().stream().findFirst().orElse(null));
        newPriorityRuleField.setDisable(true);
    }

    private void getSelectedTaskIndices() {
        ObservableList<String> selectedTasks = triggerTasksField.getSelectionModel().getSelectedItems();
        ObservableList<String> allTasks = triggerTasksField.getItems();
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        for (String task : selectedTasks) {
            int index = allTasks.indexOf(task);
            selectedIndices.add(index);
        }
        this.main.getDataStatus().setTriggerTasks(selectedIndices);
    }

    private void updateTasks(String selectedVehicleInGUI) {
        var selectedVehicle = main.getDataStatus().getProjectData().getVehicle(
                main.getDataStatus().getProjectData().getVehicleID(selectedVehicleInGUI, main.getDataStatus().getProjectData().getVehicles()));
        var tasks = selectedVehicle.getTasks();
        ObservableList<String> taskItems = FXCollections.observableArrayList();
        tasks.forEach(task -> taskItems.add(task.getTaskName()));
        triggerTasksField.setItems(taskItems);
    }

    private void updateOtherVehicles(String selectedVehicleInGUI) {
        var vehicles = main.getDataStatus().getProjectData().getVehicleNames();
        ObservableList<String> otherVehicles = FXCollections.observableArrayList();
        vehicles.stream().filter(vehicle -> !vehicle.equals(selectedVehicleInGUI)).forEach(otherVehicles::add);
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
        centerPane.getChildren().addAll(priorityRule, priorityRuleField, trafficControl, trafficControlField, triggerVehicle, triggerVehicleField,
                triggerTasks, triggerTasksField, triggerVelocityRatio, triggerVelocityRatioField, newPriorityRule, newPriorityRuleField);
    }

    private void controllers() {
        priorityRuleField.setOnAction(e -> controller.chooseHeuristic());
        trafficControlField.setOnAction(e -> controller.chooseTrafficControl());
        triggerVehicleField.setOnAction(e -> controller.chooseTriggerVehicle());
        triggerVelocityRatioField.textProperty().addListener((observable, oldValue, newValue) -> controller.chooseTriggerVelocityRatio());
        newPriorityRuleField.setOnAction(e -> controller.chooseNewHeuristic());
    }

    public Main getMain() {
        return main;
    }

    public BorderPane getPane() {
        return pane;
    }

    public ChoiceBox<String> getPriorityRuleField() {
        return priorityRuleField;
    }

    public ChoiceBox<String> getTrafficControlField() {
        return trafficControlField;
    }

    public ChoiceBox<String> getTriggerVehicleField() {
        return triggerVehicleField;
    }

    public ListView<String> getTriggerTasksField() {
        return triggerTasksField;
    }

    public TextField getTriggerVelocityRatioField() {
        return triggerVelocityRatioField;
    }

    public ChoiceBox<String> getNewPriorityRuleField() {
        return newPriorityRuleField;
    }
}
