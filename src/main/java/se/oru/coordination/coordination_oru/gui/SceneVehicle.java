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
import java.util.List;

public class SceneVehicle {
    public static final int PADDING = 10;
    private final Main main;
    private final ListView<String> vehicleListView = new ListView<>();
    private final ControllerVehicle controllerVehicle = new ControllerVehicle(this);

    public SceneVehicle(Main main) {
        this.main = main;
    }

    public Scene get() {

        var pane = new BorderPane();
        getMenuBar(pane);
        getRightPane(pane);
        getNavigationBar(pane);

        // Center Pane
        var centerPane = new GridPane();
        BorderPane.setMargin(centerPane, new Insets(10));
        centerPane.setPadding(new Insets(PADDING));
        centerPane.setAlignment(Pos.TOP_CENTER);
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        pane.setCenter(centerPane);

        // name text-field
        var name = new Text("Name of Vehicle: ");
        GridPane.setConstraints(name, 0, 0);
        var nameField = new TextField();
        nameField.setMaxWidth(180);
        GridPane.setConstraints(nameField, 1, 0);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    nameField.setText(String.valueOf(selectedVehicle.getName()));
                }
            }
        });
        controllerVehicle.getName(nameField);

        // priority text-field
        var priorityText = new Text("Priority: ");
        GridPane.setConstraints(priorityText, 0, 1);
        var priorityTextField = new TextField();
        priorityTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(priorityTextField, 1, 1);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    priorityTextField.setText(String.valueOf(selectedVehicle.getPriority()));
                }
            }
        });
        controllerVehicle.getPriority(priorityTextField);

        // length text-field
        Text lengthText = new Text("Length (m): ");
        GridPane.setConstraints(lengthText, 0, 2);
        TextField lengthTextField = new TextField();
        lengthTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(lengthTextField, 1, 2);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    lengthTextField.setText(String.valueOf(selectedVehicle.getLength()));
                }
            }
        });
        lengthTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(lengthTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLength = Double.parseDouble(lengthTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setLength(newLength);
                }
            }
        });

        // width text-field
        Text widthText = new Text("Width (m): ");
        GridPane.setConstraints(widthText, 0, 3);
        TextField widthTextField = new TextField();
        widthTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(widthTextField, 1, 3);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    widthTextField.setText(String.valueOf(selectedVehicle.getWidth()));
                }
            }
        });
        widthTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(widthTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newWidth = Double.parseDouble(widthTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setWidth(newWidth);
                }
            }
        });

        // maxVelocity text-field
        Text maxVelocityText = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocityText, 0, 4);
        TextField maxVelocityTextField = new TextField();
        maxVelocityTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(maxVelocityTextField, 1, 4);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    maxVelocityTextField.setText(String.valueOf(selectedVehicle.getMaxVelocity()));
                }
            }
        });
        maxVelocityTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(maxVelocityTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxVelocity = Double.parseDouble(maxVelocityTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setMaxVelocity(newMaxVelocity);
                }
            }
        });

        // maxAcceleration text-field
        Text maxAccelerationText = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAccelerationText, 0, 5);
        TextField maxAccelerationTextField = new TextField();
        maxAccelerationTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(maxAccelerationTextField, 1, 5);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    maxAccelerationTextField.setText(String.valueOf(selectedVehicle.getMaxAcceleration()));
                }
            }
        });
        maxAccelerationTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(maxAccelerationTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxAcceleration = Double.parseDouble(maxAccelerationTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setMaxAcceleration(newMaxAcceleration);
                }
            }
        });

        // safetyDistance text-field
        Text safetyDistanceText = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistanceText, 0, 6);
        TextField safetyDistanceTextField = new TextField();
        safetyDistanceTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(safetyDistanceTextField, 1, 6);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    safetyDistanceTextField.setText(String.valueOf(selectedVehicle.getSafetyDistance()));
                }
            }
        });
        safetyDistanceTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(safetyDistanceTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newSafetyDistance = Double.parseDouble(safetyDistanceTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setSafetyDistance(newSafetyDistance);
                }
            }
        });

        // color choice-box
        Text colorText = new Text("Color: ");
        GridPane.setConstraints(colorText, 0, 7);
        ChoiceBox<String> colorChoiceBox = new ChoiceBox<String>();
        colorChoiceBox.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorChoiceBox.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(colorChoiceBox, 1, 7);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    colorChoiceBox.setValue(String.valueOf(selectedVehicle.getColor()));
                }
            }
        });
        colorChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newColor = colorChoiceBox.getValue();
                main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setColor(newColor);
            }
        });

        // initialPose choice-box
        Text initialPoseText = new Text("Start Location: ");
        GridPane.setConstraints(initialPoseText, 0, 8);
        ChoiceBox<String> initialPoseChoiceBox = new ChoiceBox<String>();
        initialPoseChoiceBox.getItems().addAll(main.getDataStatus().getProjectData().getPoses().keySet());
        initialPoseChoiceBox.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(initialPoseChoiceBox, 1, 8);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    initialPoseChoiceBox.setValue(String.valueOf(selectedVehicle.getInitialPose()));
                }
            }
        });
        initialPoseChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newInitialPose = initialPoseChoiceBox.getValue();
                main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setInitialPose(newInitialPose);
            }
        });

        // missions vbox
        Text missionText = new Text("Mission: ");
        GridPane.setConstraints(missionText, 0, 9);
        VBox missionVBox = new VBox();
        missionVBox.setSpacing(2);
        missionVBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(missionVBox, 1, 9);

        // mission list-view
        ListView<String> missionListView = new ListView<String>();
        missionListView.setMaxWidth(nameField.getMaxWidth());
        missionListView.setMaxHeight(110);

        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    missionListView.getItems().clear();
                    List<ProjectData.MissionStep> missionSteps = selectedVehicle.getMission();
                    missionSteps.forEach(missionStep -> missionListView.getItems().add(missionStep.toString()));
                }
            }
        });

        // mission buttons
        HBox missionButtons = new HBox();
        missionButtons.setSpacing(5);
        missionButtons.setAlignment(Pos.CENTER);
        Button addMissionButton = new Button("Add");
        Button deleteMissionButton = new Button("Delete");
        Button upMissionButton = new Button("↑");
        Button downMissionButton = new Button("↓");
        missionButtons.getChildren().addAll(addMissionButton, deleteMissionButton, upMissionButton, downMissionButton);
        missionVBox.setMaxWidth(nameField.getMaxWidth());
        missionVBox.getChildren().addAll(missionListView, missionButtons);

        // Set action for moving missions up
        upMissionButton.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), main.getDataStatus().getProjectData().getVehicles()));
            if (selectedIndex > 0) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex - 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex - 1);
                if (selectedVehicle != null) {
                    List<ProjectData.MissionStep> missionSteps = selectedVehicle.getMission();
                    ProjectData.MissionStep missionStep = missionSteps.remove(selectedIndex);
                    missionSteps.add(selectedIndex - 1, missionStep);
                }
            }
        });

        // Set action for moving missions down
        downMissionButton.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), main.getDataStatus().getProjectData().getVehicles()));
            if (selectedIndex < missionListView.getItems().size() - 1) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex + 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex + 1);
                if (selectedVehicle != null) {
                    List<ProjectData.MissionStep> missionSteps = selectedVehicle.getMission();
                    ProjectData.MissionStep missionStep = missionSteps.remove(selectedIndex);
                    missionSteps.add(selectedIndex + 1, missionStep);
                }
            }
        });

        // Set action for adding a mission
        addMissionButton.setOnAction(e -> {
            ProjectData.MissionStep addedMission = AddMissionDialogBox.display("Adding a Mission", main.getDataStatus().getProjectData().getPoses().keySet(), missionListView);
            ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), main.getDataStatus().getProjectData().getVehicles()));
            if (selectedVehicle != null && addedMission != null) {
                List<ProjectData.MissionStep> missionSteps = selectedVehicle.getMission();
                missionSteps.add(addedMission);
            }
        });

        // Set action for removing a mission
        deleteMissionButton.setOnAction(e -> {
            ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), main.getDataStatus().getProjectData().getVehicles()));
            if (selectedVehicle != null) {
                int selectedMissionIndex = missionListView.getSelectionModel().getSelectedIndex();
                missionListView.getItems().remove(selectedMissionIndex);
                selectedVehicle.getMission().remove(selectedMissionIndex);
            }
        });

        // missionRepetition text-field
        Text missionRepetitionText = new Text("Mission Repetition: ");
        GridPane.setConstraints(missionRepetitionText, 0, 10);
        TextField missionRepetitionTextField = new TextField();
        missionRepetitionTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(missionRepetitionTextField, 1, 10);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    missionRepetitionTextField.setText(String.valueOf(selectedVehicle.getMissionRepetition()));
                }
            }
        });
        missionRepetitionTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(missionRepetitionTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newMissionRepetition = Integer.parseInt(missionRepetitionTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setMissionRepetition(newMissionRepetition);
                }
            }
        });

        // lookAheadDistance text-field
        Text lookAheadDistanceText = new Text("Look Ahead Distance (m): ");
        lookAheadDistanceText.setVisible(false);
        GridPane.setConstraints(lookAheadDistanceText, 0, 12);
        TextField lookAheadDistanceTextField = new TextField();
        lookAheadDistanceTextField.setVisible(false);
        lookAheadDistanceTextField.setMaxWidth(nameField.getMaxWidth());
        GridPane.setConstraints(lookAheadDistanceTextField, 1, 12);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    lookAheadDistanceTextField.setText(String.valueOf(selectedVehicle.getLookAheadDistance()));
                }
            }
        });
        lookAheadDistanceTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateDouble(lookAheadDistanceTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLookAheadDistance = Double.parseDouble(lookAheadDistanceTextField.getText());
                    main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).setLookAheadDistance(newLookAheadDistance);
                }
            }
        });

        // isHumanVehicle checkbox
        Text isHumanText = new Text("Human Operated: ");
        GridPane.setConstraints(isHumanText, 0, 11);
        CheckBox isHumanCheckBox = new CheckBox();
        GridPane.setConstraints(isHumanCheckBox, 1, 11);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    boolean ifHuman = "Human".equals(selectedVehicle.getType());
                    isHumanCheckBox.setSelected(ifHuman);

                    lookAheadDistanceText.setVisible(ifHuman);
                    lookAheadDistanceTextField.setVisible(ifHuman);
                }
            }
        });
        isHumanCheckBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            ProjectData.Vehicle selectedVehicle = selectedVehicleName != null ? main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())) : null;

            if (isHumanCheckBox.isSelected()) {
                lookAheadDistanceText.setVisible(true);
                lookAheadDistanceTextField.setVisible(true);
                if (selectedVehicle != null) {
                    selectedVehicle.setType("Human");
                }
            } else {
                lookAheadDistanceText.setVisible(false);
                lookAheadDistanceTextField.setVisible(false);
                if (selectedVehicle != null) {
                    selectedVehicle.setType("Autonomous");
                }
            }
        });

        centerPane.getChildren().addAll(name, nameField,
                priorityText, priorityTextField,
                lengthText, lengthTextField,
                widthText, widthTextField,
                maxVelocityText, maxVelocityTextField,
                maxAccelerationText, maxAccelerationTextField,
                safetyDistanceText, safetyDistanceTextField,
                colorText, colorChoiceBox,
                initialPoseText, initialPoseChoiceBox,
                missionText, missionVBox,
                missionRepetitionText, missionRepetitionTextField,
                isHumanText, isHumanCheckBox,
                lookAheadDistanceText, lookAheadDistanceTextField);

        // Left Pane - VehicleList
        var leftPane = new VBox();
        leftPane.setSpacing(10);
        leftPane.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(leftPane, new Insets(10, 0, 10, 10));
        leftPane.setPadding(new Insets(10));
        pane.setLeft(leftPane);

        Label vehiclesLabel = new Label("List of Vehicles: ");
        vehiclesLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        vehiclesLabel.setPrefWidth(215);
        vehiclesLabel.setAlignment(Pos.CENTER);

        vehicleListView.setMaxWidth(vehiclesLabel.getPrefWidth());
        vehicleListView.setPrefHeight(475);
        vehicleListView.getItems().clear();
        main.getDataStatus().getProjectData().getVehicles().forEach(vehicle -> vehicleListView.getItems().add(vehicle.getName()));
        vehicleListView.getSelectionModel().selectFirst();

        Button addVehicleButton = new Button("Add Vehicle");
        Button deleteVehicleButton = new Button("Delete Vehicle");

        // Set action for adding a 'default' vehicle
        addVehicleButton.setOnAction(e -> {

            // Setting default values for a vehicle
            String baseNameOfVehicle = "vehicle";
            int priorityOfVehicle = 1;
            double lengthOfVehicle = 8.0;
            double widthOfVehicle = 4.0;
            double maxVelocityOfVehicle = 10.0;
            double maxAccelerationOfVehicle = 1.0;
            double safetyDistanceOfVehicle = 0.0;
            String colorOfVehicle = "Yellow";
            String initialPoseOfVehicle = main.getDataStatus().getProjectData().getPoses().keySet().stream().findFirst().orElse(null);
            int missionRepetitionOfVehicle = 1;
            String typeOfVehicle = "Autonomous";
            double lookAheadDistanceOfVehicle = 0.0;

            // Adding a default mission
            List<ProjectData.MissionStep> missionOfVehicle = new ArrayList<ProjectData.MissionStep>();
            ProjectData.MissionStep missionStep = new ProjectData.MissionStep();
            missionStep.setPoseName(main.getDataStatus().getProjectData().getPoses().keySet().stream().
                    filter(item -> !item.equals(initialPoseOfVehicle)).
                    findAny().
                    orElse(null));
            missionStep.setDuration(1.0);
            missionOfVehicle.add(missionStep);

            // Handle duplicate names for vehicles
            String nameOfVehicle = baseNameOfVehicle;
            if (main.getDataStatus().getProjectData().getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(baseNameOfVehicle))) {
                main.getDataStatus().setVehicleCounter(main.getDataStatus().getVehicleCounter() + 1);
                nameOfVehicle = baseNameOfVehicle + " (" + main.getDataStatus().getVehicleCounter() + ")";
            }

            // Create a new vehicle with default values
            var vehicle = new ProjectData.Vehicle();
            vehicle.setName(nameOfVehicle);
            vehicle.setPriority(priorityOfVehicle);
            vehicle.setLength(lengthOfVehicle);
            vehicle.setWidth(widthOfVehicle);
            vehicle.setMaxVelocity(maxVelocityOfVehicle);
            vehicle.setMaxAcceleration(maxAccelerationOfVehicle);
            vehicle.setSafetyDistance(safetyDistanceOfVehicle);
            vehicle.setColor(colorOfVehicle);
            vehicle.setInitialPose(initialPoseOfVehicle);
            vehicle.setMission(missionOfVehicle);
            vehicle.setMissionRepetition(missionRepetitionOfVehicle);
            vehicle.setType(typeOfVehicle);
            vehicle.setLookAheadDistance(lookAheadDistanceOfVehicle);

            main.getDataStatus().getProjectData().addVehicle(vehicle);
            vehicleListView.getItems().add(vehicle.getName());
            vehicleListView.getSelectionModel().selectLast();
        });

        // Set action for removing a vehicle
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem(); // Get the selected item
            main.getDataStatus().getProjectData().removeVehicle(main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, main.getDataStatus().getProjectData().getVehicles())).getID()); // Remove from ProjectData
            vehicleListView.getItems().remove(selectedVehicleName); // Remove from ListView
            vehicleListView.getSelectionModel().selectFirst();
        });

        HBox buttons = new HBox(addVehicleButton, deleteVehicleButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(5);
        buttons.setMaxWidth(vehiclesLabel.getPrefWidth());
        leftPane.getChildren().addAll(vehiclesLabel, vehicleListView, buttons);

        // Listener for list selection changes
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get the selected vehicle's details
                ProjectData.Vehicle vehicle = main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles()));

                // Update the fields in the centerPane with the details of the selected vehicle
                nameField.setText(newValue);
                lengthTextField.setText(String.valueOf(vehicle.getLength()));
                widthTextField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityTextField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationTextField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceTextField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorChoiceBox.setValue(vehicle.getColor());
                initialPoseChoiceBox.setValue(main.getDataStatus().getProjectData().getVehicle(main.getDataStatus().getProjectData().getVehicleID(newValue, main.getDataStatus().getProjectData().getVehicles())).getInitialPose());
                lookAheadDistanceTextField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                missionRepetitionTextField.setText(String.valueOf(vehicle.getMissionRepetition()));

                // Update the mission list view with the mission of the selected vehicle
                missionListView.getItems().clear();
                vehicle.getMission().forEach(missionStep -> missionListView.getItems().add(missionStep.toString()));

                // Update the view according to the type of the vehicle
                if (vehicle.getType().equals("Human")) {
                    isHumanCheckBox.setSelected(true);
                    lookAheadDistanceText.setVisible(true);
                    lookAheadDistanceTextField.setVisible(true);
                } else {
                    isHumanCheckBox.setSelected(false);
                    lookAheadDistanceText.setVisible(false);
                    lookAheadDistanceTextField.setVisible(false);
                }

            }
        });
        vehicleListView.getSelectionModel().selectFirst();


        return new Scene(pane);
    }

    private void getNavigationBar(BorderPane pane) {
        pane.setBottom(NavigationBar.update(main, SceneState.VEHICLE));
    }

    private void getRightPane(BorderPane pane) {
        var rightPane = new StackPane();
        int mapWidth = 680;
        int mapHeight = 538;
        var mapDisplay = new MapDisplayWithMarkers("file:" + main.getDataStatus().getProjectData().getMapImage(main.getDataStatus().getMapData()), main.getDataStatus().getProjectData().getPoses(),
                main.getDataStatus().getMapData().getResolution(), mapWidth, mapHeight);
        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        rightPane.setPadding(new Insets(PADDING));
        rightPane.setAlignment(Pos.TOP_CENTER);
        pane.setRight(rightPane);
        rightPane.getChildren().add(mapDisplay);
    }

    private void getMenuBar(BorderPane pane) {
        pane.setTop(MenuBar.update(main, SceneState.VEHICLE));
    }

    public ListView<String> getVehicleListView() {
        return vehicleListView;
    }

    public Main getMain() {
        return main;
    }
}