package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneVehicle {
    private static final int PADDING = 10;
    private static final int MAP_WIDTH = 680;
    private static final int MAP_HEIGHT = 538;
    private static final int TEXT_WIDTH = 300;
    private TextField nameField;
    private TextField priorityField;
    private TextField lengthField;
    private TextField widthField;
    private TextField maxVelocityField;
    private TextField maxAccelerationField;
    private TextField safetyDistanceField;
    private ChoiceBox<String> colorField;
    private ChoiceBox<String> initialPoseField;
    private VBox taskField;
    private TextField taskRepetitionField;
    private CheckBox isHumanField;
    private Text lookAheadDistance;
    private TextField lookAheadDistanceField;
    private final ListView<String> vehicles = new ListView<>();
    private final Button add = new Button("Add");
    private final Button delete = new Button("Delete");
    private final Button down = new Button("↑");
    private final Button up = new Button("↓");
    private final Button loadPath = new Button("Load Path");
    private ListView<String> tasks = new ListView<>();
    private final Button addVehicle = new Button("Add Vehicle");
    private final Button deleteVehicle = new Button("Delete Vehicle");
    private final Main main;
    private BorderPane pane;
    private final ControllerVehicle controller = new ControllerVehicle(this);

    public SceneVehicle(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        menuBar();
        rightPane();
        navigationBar();
        centerPane();
        fieldController();

        vehicles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                var vehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (vehicle != null) {
                    nameField.setText(String.valueOf(vehicle.getName()));
                    priorityField.setText(String.valueOf(vehicle.getPriority()));
                    lengthField.setText(String.valueOf(vehicle.getLength()));
                    widthField.setText(String.valueOf(vehicle.getWidth()));
                    maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                    maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                    safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
                    colorField.setValue(String.valueOf(vehicle.getColor()));
                    initialPoseField.setValue(String.valueOf(vehicle.getInitialPose()));
                    taskRepetitionField.setText(String.valueOf(vehicle.getTaskRepetition()));
                    isHumanField.setSelected("Human".equals(vehicle.getType()));
                    lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                }
            }
        });

        tasks = initializeTaskList();
        initializeTasks();

        vehicles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                var selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    tasks.getItems().clear();
                    var taskSteps = selectedVehicle.getTask();
                    taskSteps.forEach(taskStep -> tasks.getItems().add(taskStep.toString()));
                }
            }
        });

        // Left Pane - VehicleList
        var leftPane = new VBox();
        leftPane.setSpacing(10);
        leftPane.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(leftPane, new Insets(10, 0, 10, 10));
        leftPane.setPadding(new Insets(10));
        pane.setLeft(leftPane);

        var label = label();

        vehicles.setMaxWidth(label.getPrefWidth());
        vehicles.setPrefHeight(475);
        vehicles.getItems().clear();
        main.getDataStatus().getProjectData().getVehicles().forEach(vehicle -> vehicles.getItems().add(vehicle.getName()));
        vehicles.getSelectionModel().selectFirst();

        vehicleController();
        var buttons = new HBox(addVehicle, deleteVehicle);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(5);
        buttons.setMaxWidth(label.getPrefWidth());
        leftPane.getChildren().addAll(label, vehicles, buttons);

        vehicles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                var vehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));

                nameField.setText(newValue);
                lengthField.setText(String.valueOf(vehicle.getLength()));
                widthField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorField.setValue(vehicle.getColor());
                initialPoseField.setValue(main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles())).getInitialPose());
                lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                taskRepetitionField.setText(String.valueOf(vehicle.getTaskRepetition()));

                tasks.getItems().clear();
                vehicle.getTask().forEach(missionStep -> tasks.getItems().add(missionStep.toString()));

                if (vehicle.getType().equals("Human")) {
                    isHumanField.setSelected(true);
                    lookAheadDistance.setVisible(true);
                    lookAheadDistanceField.setVisible(true);
                } else {
                    isHumanField.setSelected(false);
                    lookAheadDistance.setVisible(false);
                    lookAheadDistanceField.setVisible(false);
                }

            }
        });
        vehicles.getSelectionModel().selectFirst();
        return new Scene(pane);
    }

    private void vehicleController() {
        addVehicle.setOnAction(e -> controller.clickAddVehicle());
        deleteVehicle.setOnAction(e -> controller.clickDeleteVehicle());
    }

    private Label label() {
        var label = new Label("List of Vehicles: ");
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        label.setPrefWidth(215);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private ListView<String> initializeTaskList() {
        var missions = new ListView<String>();
        missions.setMaxWidth(TEXT_WIDTH);
        missions.setMaxHeight(110);
        return missions;
    }

    private void initializeTasks() {
        var taskButtons = new HBox();
        taskButtons.setSpacing(5);
        taskButtons.setAlignment(Pos.CENTER);
        taskButtons.getChildren().addAll(add, delete, down, up, loadPath);
        taskField.setMaxWidth(TEXT_WIDTH);
        taskField.getChildren().addAll(tasks, taskButtons);
        taskController();
    }

    private void taskController() {
        add.setOnAction(e -> controller.clickAdd());
        delete.setOnAction(e -> controller.clickDelete());
        down.setOnAction(e -> controller.clickDown());
        up.setOnAction(e -> controller.clickUp());
        loadPath.setOnAction(e -> controller.clickLoadPath());
        tasks.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) controller.doubleClickTask();
        });
    }

    private void fieldController() {
        nameField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeName();
        });
        priorityField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changePriority();
        });
        lengthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeLength();
        });
        widthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeWidth();
        });
        maxVelocityField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeMaxVelocity();
        });
        maxAccelerationField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeMaxAcceleration();
        });
        safetyDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeSafetyDistance();
        });
        taskRepetitionField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeTaskRepetition();
        });
        lookAheadDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeLookAhead();
        });
        colorField.setOnAction(e -> controller.chooseColor());
        initialPoseField.setOnAction(e -> controller.chooseStartPose());
        isHumanField.setOnAction(e -> controller.checkIsHuman());
    }

    private void centerPane() {
        var centerPane = initializeCenterPane();

        var name = text("Name of Vehicle: ", 0);
        var priority = text("Priority: ", 1);
        var length = text("Length (m): ", 2);
        var width = text("Width (m): ", 3);
        var maxVelocity = text("Max. Velocity (m/s): ", 4);
        var maxAcceleration = text("Max. Acceleration (m/s^2): ", 5);
        var safetyDistance = text("Safety Distance (m): ", 6);
        var color = text("Color: ", 7);
        var initialPose = text("Start Pose: ", 8);
        var task = text("Tasks :", 9);
        var taskRepetition = text("Tasks Repetition: ", 10);
        var isHuman = text("Human Operated: ", 11);
        lookAheadDistance = text("Look Ahead Distance (m): ", 12);
        lookAheadDistance.setVisible(false);

        initializeFields();

        centerPane.getChildren().addAll(name, nameField,
                priority, priorityField,
                length, lengthField,
                width, widthField,
                maxVelocity, maxVelocityField,
                maxAcceleration, maxAccelerationField,
                safetyDistance, safetyDistanceField,
                color, colorField,
                initialPose, initialPoseField,
                task, taskField,
                taskRepetition, taskRepetitionField,
                isHuman, isHumanField,
                lookAheadDistance, lookAheadDistanceField);
    }

    private GridPane initializeCenterPane() {
        var centerPane = new GridPane();
        BorderPane.setMargin(centerPane, new Insets(PADDING));
        centerPane.setPadding(new Insets(PADDING));
        centerPane.setAlignment(Pos.TOP_CENTER);
        centerPane.setHgap(PADDING);
        centerPane.setVgap(PADDING);
        pane.setCenter(centerPane);
        return centerPane;
    }

    private void initializeFields() {
        nameField = textField(0);
        priorityField = textField(1);
        lengthField = textField(2);
        widthField = textField(3);
        maxVelocityField = textField(4);
        maxAccelerationField = textField(5);
        safetyDistanceField = textField(6);
        var colors = Arrays.asList("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorField = choiceBox(colors, 7);
        var poses = new ArrayList<>(main.getDataStatus().getProjectData().getPoses().keySet());
        initialPoseField = choiceBox(poses, 8);
        taskField = vBox(9);
        taskRepetitionField = textField(10);
        isHumanField = checkBox(11);
        lookAheadDistanceField = textField(12);
        lookAheadDistanceField.setVisible(false);
    }

    private CheckBox checkBox(int row) {
        var checkBox = new CheckBox();
        GridPane.setConstraints(checkBox, 1, row);
        return checkBox;
    }

    private VBox vBox(int row) {
        var taskVBox = new VBox();
        taskVBox.setSpacing(2);
        taskVBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(taskVBox, 1, row);
        return taskVBox;
    }

    private ChoiceBox<String> choiceBox(List<String> items, int row) {
        var choiceBox = new ChoiceBox<String>();
        choiceBox.getItems().addAll(items);
        choiceBox.setMaxWidth(TEXT_WIDTH);
        GridPane.setConstraints(choiceBox, 1, row);
        return choiceBox;
    }

    private Text text(String name, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, 0, row);
        return text;
    }

    private TextField textField(int row) {
        var textField = new TextField();
        textField.setMaxWidth(TEXT_WIDTH);
        GridPane.setConstraints(textField, 1, row);
        return textField;
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.VEHICLE));
    }

    private void rightPane() {
        var rightPane = new StackPane();
        rightPane.setAlignment(Pos.TOP_CENTER);
        rightPane.setPadding(new Insets(PADDING));
        pane.setRight(rightPane);
        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        var mapDisplay = new StaticMap("file:" + main.getDataStatus().getProjectData().getMapImage(main.getDataStatus().getMapData()), main.getDataStatus().getProjectData().getPoses(),
                main.getDataStatus().getMapData().getResolution(), MAP_WIDTH, MAP_HEIGHT);
        rightPane.getChildren().add(mapDisplay);
    }

    private void menuBar() {
        pane.setTop(MenuBar.update(main, SceneState.VEHICLE));
    }

    public ListView<String> getVehicles() {
        return vehicles;
    }

    public Main getMain() {
        return main;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getPriorityField() {
        return priorityField;
    }

    public TextField getLengthField() {
        return lengthField;
    }

    public TextField getWidthField() {
        return widthField;
    }

    public TextField getMaxVelocityField() {
        return maxVelocityField;
    }

    public TextField getMaxAccelerationField() {
        return maxAccelerationField;
    }

    public TextField getSafetyDistanceField() {
        return safetyDistanceField;
    }

    public ChoiceBox<String> getColorField() {
        return colorField;
    }

    public ChoiceBox<String> getStartPoseField() {
        return initialPoseField;
    }

    public TextField getTaskRepetitionField() {
        return taskRepetitionField;
    }

    public CheckBox getIsHumanField() {
        return isHumanField;
    }

    public TextField getLookAheadDistanceField() {
        return lookAheadDistanceField;
    }

    public Button getAddVehicle() {
        return addVehicle;
    }

    public Button getDeleteVehicle() {
        return deleteVehicle;
    }

    public Button getAdd() {
        return add;
    }

    public Button getDelete() {
        return delete;
    }

    public Button getDown() {
        return down;
    }

    public Button getUp() {
        return up;
    }

    public ListView<String> getTasks() {
        return tasks;
    }

    public Text getLookAheadDistance() {
        return lookAheadDistance;
    }
}