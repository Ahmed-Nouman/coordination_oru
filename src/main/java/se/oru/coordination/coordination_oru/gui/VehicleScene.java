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
import java.util.Objects;

public class VehicleScene {
    private final se.oru.coordination.coordination_oru.gui.GUI GUI;
    private final ListView<String> vehicleListView = new ListView<>();


    public VehicleScene(se.oru.coordination.coordination_oru.gui.GUI GUI) {
        this.GUI = GUI;
    }

    public Scene get() {

        var root = new BorderPane();

        // Top Pane - Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(GUI));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableRunProject();

        // Right Pane
        var rightPane = new StackPane();
        int mapWidth = 680;
        int mapHeight = 538;
        var mapDisplay = new MapDisplayWithMarkers("file:" + GUI.getDataStatus().getProjectData().getMapImage(GUI.getDataStatus().getMapData()), GUI.getDataStatus().getProjectData().getPoses(),
                GUI.getDataStatus().getMapData().getResolution(), mapWidth, mapHeight);
        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        rightPane.setPadding(new Insets(10));
        rightPane.setAlignment(Pos.TOP_CENTER);
        root.setRight(rightPane);
        rightPane.getChildren().add(mapDisplay);

        // Center Pane
        var centerPane = new GridPane();
        BorderPane.setMargin(centerPane, new Insets(10));
        centerPane.setPadding(new Insets(10));
        centerPane.setAlignment(Pos.TOP_CENTER);
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        root.setCenter(centerPane);

        // name text-field
        var nameText = new Text("Name of Vehicle: ");
        GridPane.setConstraints(nameText, 0, 0);
        TextField nameTextField = new TextField();
        nameTextField.setMaxWidth(180);
        GridPane.setConstraints(nameTextField, 1, 0);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    nameTextField.setText(String.valueOf(selectedVehicle.getName()));
                }
            }
        });
        nameTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newVehicleName = nameTextField.getText();
                String oldVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (!Objects.equals(newVehicleName, "") && newVehicleName != null) {
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(oldVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setName(newVehicleName);
                    vehicleListView.getItems().clear();
                    GUI.getDataStatus().getProjectData().getVehicles().forEach(vehicle -> vehicleListView.getItems().add(vehicle.getName()));
                    vehicleListView.getSelectionModel().selectFirst();
                }
            }
        });

        // priority text-field
        Text priorityText = new Text("Priority: ");
        GridPane.setConstraints(priorityText, 0, 1);
        TextField priorityTextField = new TextField();
        priorityTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(priorityTextField, 1, 1);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    priorityTextField.setText(String.valueOf(selectedVehicle.getPriority()));
                }
            }
        });
        priorityTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(priorityTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newPriority = Integer.parseInt(priorityTextField.getText());
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setPriority(newPriority);
                }
            }
        });

        // length text-field
        Text lengthText = new Text("Length (m): ");
        GridPane.setConstraints(lengthText, 0, 2);
        TextField lengthTextField = new TextField();
        lengthTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(lengthTextField, 1, 2);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setLength(newLength);
                }
            }
        });

        // width text-field
        Text widthText = new Text("Width (m): ");
        GridPane.setConstraints(widthText, 0, 3);
        TextField widthTextField = new TextField();
        widthTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(widthTextField, 1, 3);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setWidth(newWidth);
                }
            }
        });

        // maxVelocity text-field
        Text maxVelocityText = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocityText, 0, 4);
        TextField maxVelocityTextField = new TextField();
        maxVelocityTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(maxVelocityTextField, 1, 4);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setMaxVelocity(newMaxVelocity);
                }
            }
        });

        // maxAcceleration text-field
        Text maxAccelerationText = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAccelerationText, 0, 5);
        TextField maxAccelerationTextField = new TextField();
        maxAccelerationTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(maxAccelerationTextField, 1, 5);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setMaxAcceleration(newMaxAcceleration);
                }
            }
        });

        // safetyDistance text-field
        Text safetyDistanceText = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistanceText, 0, 6);
        TextField safetyDistanceTextField = new TextField();
        safetyDistanceTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(safetyDistanceTextField, 1, 6);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setSafetyDistance(newSafetyDistance);
                }
            }
        });

        // color choice-box
        Text colorText = new Text("Color: ");
        GridPane.setConstraints(colorText, 0, 7);
        ChoiceBox<String> colorChoiceBox = new ChoiceBox<String>();
        colorChoiceBox.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorChoiceBox.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(colorChoiceBox, 1, 7);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    colorChoiceBox.setValue(String.valueOf(selectedVehicle.getColor()));
                }
            }
        });
        colorChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newColor = colorChoiceBox.getValue();
                GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setColor(newColor);
            }
        });

        // initialPose choice-box
        Text initialPoseText = new Text("Start Location: ");
        GridPane.setConstraints(initialPoseText, 0, 8);
        ChoiceBox<String> initialPoseChoiceBox = new ChoiceBox<String>();
        initialPoseChoiceBox.getItems().addAll(GUI.getDataStatus().getProjectData().getPoses().keySet());
        initialPoseChoiceBox.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(initialPoseChoiceBox, 1, 8);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
                if (selectedVehicle != null) {
                    initialPoseChoiceBox.setValue(String.valueOf(selectedVehicle.getInitialPose()));
                }
            }
        });
        initialPoseChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newInitialPose = initialPoseChoiceBox.getValue();
                GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setInitialPose(newInitialPose);
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
        missionListView.setMaxWidth(nameTextField.getMaxWidth());
        missionListView.setMaxHeight(110);

        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
        missionVBox.setMaxWidth(nameTextField.getMaxWidth());
        missionVBox.getChildren().addAll(missionListView, missionButtons);

        // Set action for moving missions up
        upMissionButton.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), GUI.getDataStatus().getProjectData().getVehicles()));
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
            ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), GUI.getDataStatus().getProjectData().getVehicles()));
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
            ProjectData.MissionStep addedMission = AddMissionDialogBox.display("Adding a Mission", GUI.getDataStatus().getProjectData().getPoses().keySet(), missionListView);
            ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), GUI.getDataStatus().getProjectData().getVehicles()));
            if (selectedVehicle != null && addedMission != null) {
                List<ProjectData.MissionStep> missionSteps = selectedVehicle.getMission();
                missionSteps.add(addedMission);
            }
        });

        // Set action for removing a mission
        deleteMissionButton.setOnAction(e -> {
            ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), GUI.getDataStatus().getProjectData().getVehicles()));
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
        missionRepetitionTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(missionRepetitionTextField, 1, 10);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setMissionRepetition(newMissionRepetition);
                }
            }
        });

        // lookAheadDistance text-field
        Text lookAheadDistanceText = new Text("Look Ahead Distance (m): ");
        lookAheadDistanceText.setVisible(false);
        GridPane.setConstraints(lookAheadDistanceText, 0, 12);
        TextField lookAheadDistanceTextField = new TextField();
        lookAheadDistanceTextField.setVisible(false);
        lookAheadDistanceTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(lookAheadDistanceTextField, 1, 12);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
                    GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).setLookAheadDistance(newLookAheadDistance);
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
                ProjectData.Vehicle selectedVehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));
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
            ProjectData.Vehicle selectedVehicle = selectedVehicleName != null ? GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())) : null;

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

        centerPane.getChildren().addAll(nameText, nameTextField,
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
        VBox leftPane = new VBox();
        leftPane.setSpacing(10);
        leftPane.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(leftPane, new Insets(10, 0, 10, 10));
        leftPane.setPadding(new Insets(10));
        root.setLeft(leftPane);

        Label vehiclesLabel = new Label("List of Vehicles: ");
        vehiclesLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        vehiclesLabel.setPrefWidth(215);
        vehiclesLabel.setAlignment(Pos.CENTER);

        vehicleListView.setMaxWidth(vehiclesLabel.getPrefWidth());
        vehicleListView.setPrefHeight(475);
        vehicleListView.getItems().clear();
        GUI.getDataStatus().getProjectData().getVehicles().forEach(vehicle -> vehicleListView.getItems().add(vehicle.getName()));
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
            String initialPoseOfVehicle = GUI.getDataStatus().getProjectData().getPoses().keySet().stream().findFirst().orElse(null);
            int missionRepetitionOfVehicle = 1;
            String typeOfVehicle = "Autonomous";
            double lookAheadDistanceOfVehicle = 0.0;

            // Adding a default mission
            List<ProjectData.MissionStep> missionOfVehicle = new ArrayList<ProjectData.MissionStep>();
            ProjectData.MissionStep missionStep = new ProjectData.MissionStep();
            missionStep.setPoseName(GUI.getDataStatus().getProjectData().getPoses().keySet().stream().
                    filter(item -> !item.equals(initialPoseOfVehicle)).
                    findAny().
                    orElse(null));
            missionStep.setDuration(1.0);
            missionOfVehicle.add(missionStep);

            // Handle duplicate names for vehicles
            String nameOfVehicle = baseNameOfVehicle;
            if (GUI.getDataStatus().getProjectData().getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(baseNameOfVehicle))) {
                GUI.getDataStatus().setVehicleCounter(GUI.getDataStatus().getVehicleCounter() + 1);
                nameOfVehicle = baseNameOfVehicle + " (" + GUI.getDataStatus().getVehicleCounter() + ")";
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

            GUI.getDataStatus().getProjectData().addVehicle(vehicle);
            vehicleListView.getItems().add(vehicle.getName());
            vehicleListView.getSelectionModel().selectLast();
        });

        // Set action for removing a vehicle
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem(); // Get the selected item
            GUI.getDataStatus().getProjectData().removeVehicle(GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(selectedVehicleName, GUI.getDataStatus().getProjectData().getVehicles())).getID()); // Remove from ProjectData
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
                ProjectData.Vehicle vehicle = GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles()));

                // Update the fields in the centerPane with the details of the selected vehicle
                nameTextField.setText(newValue);
                lengthTextField.setText(String.valueOf(vehicle.getLength()));
                widthTextField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityTextField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationTextField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceTextField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorChoiceBox.setValue(vehicle.getColor());
                initialPoseChoiceBox.setValue(GUI.getDataStatus().getProjectData().getVehicle(GUI.getDataStatus().getProjectData().getVehicleID(newValue, GUI.getDataStatus().getProjectData().getVehicles())).getInitialPose());
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

        // Bottom Pane - Navigation Buttons
        root.setBottom(BottomPane.getBottomPane(GUI.getNavigationBar().getBackButton(), GUI.getNavigationBar().getNextButton()));

        return new Scene(root);
    }
}