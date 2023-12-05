package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.Heuristics;
import se.oru.coordination.coordination_oru.util.JTSDrawingPanelVisualization;
import se.oru.coordination.coordination_oru.util.MapInspector;
import se.oru.coordination.coordination_oru.util.Missions;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.gui.ProjectData.Vehicle;
import se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class GUI extends Application {

    private final TextField simulationTimeField = new TextField();
    final Stage stage = new Stage();
    final Separator separator = new Separator();
    final Label pathLabel = new Label("");
    protected Boolean isNewProject = false;
    protected Boolean isProjectScene = false;
    protected Boolean isMapScene = false;
    protected Boolean isVehicleScene = false;
    protected Boolean isSimulationScene = false;
    protected String projectFile = "";
    protected ProjectData projectData;
    protected MapData mapData;
    private final Button backButton = new Button("Back");
    protected final Button nextButton = new Button("Next");
    private final Button saveButton = new Button("Save");
    private final Button resetButton =  new Button("Reset");
    private final Button runButton =  new Button("Run");
    private final ListView<String> vehiclesListView = new ListView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        toggleScene(true, false, false, false);
        Scene projectScene = displayProjectScene();
        stage.setTitle("Coordination_ORU");
        stage.setScene(projectScene);
        stage.show();

        nextButton.setOnAction(e -> {
            if (isProjectScene && !isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(false, true, false, false);
                stage.setTitle("Coordination_ORU: Setting up the map");
                stage.setScene(displayMapScene());
                stage.centerOnScreen();
            }
            else if (!isProjectScene && isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(false, false, true, false);
                stage.setTitle("Coordination_ORU: Setting up the vehicles");
                stage.setScene(displayVehicleScene());
                stage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && isVehicleScene && !isSimulationScene) {
                toggleScene(false, false, false, true);
                stage.setTitle("Coordination_ORU: Setting up the simulation");
                stage.setScene(displaySimulationScene());
                stage.centerOnScreen();
            }
        });

        backButton.setOnAction(e -> {
            if (!isProjectScene && isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(true, false, false, false);
                stage.setTitle("Coordination_ORU");
                stage.setScene(displayProjectScene());
                stage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && isVehicleScene && !isSimulationScene) {
                toggleScene(false, true, false, false);
                stage.setTitle("Coordination_ORU: Setting up the map");
                stage.setScene(displayMapScene());
                stage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && !isVehicleScene && isSimulationScene) {
                toggleScene(false, false, true, false);
                stage.setTitle("Coordination_ORU: Setting up the vehicles");
                stage.setScene(displayVehicleScene());
                stage.centerOnScreen();
            }
        });

        saveButton.setOnAction(e -> System.out.println("File saved")); //TODO

        runButton.setOnAction(e -> System.out.println("Run Simulation")); //TODO

        resetButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU");
            stage.setScene(displayProjectScene());
            stage.centerOnScreen();
        });
    }

    // This method changes the scene based on the Next and Back buttons
    private void toggleScene(Boolean isProjectScene, Boolean isMapScene, Boolean isVehicleScene, Boolean isSimulationScene) {
        this.isProjectScene = isProjectScene;
        this.isMapScene = isMapScene;
        this.isVehicleScene = isVehicleScene;
        this.isSimulationScene = isSimulationScene;
    }

    /**
     * Creates and returns a scene with a layout for project management.
     *
     * @return A new Scene containing the layout for project management.
     */
    private Scene displayProjectScene() {

        BorderPane borderPane = new BorderPane();
        separator.setVisible(false);
        nextButton.setVisible(false);

        // Top Pane - Menu Bar
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableSaveProject();

        // Center Pane
        VBox centerPane = new VBox();
        centerPane.setSpacing(40);
        centerPane.setPadding(new Insets(40, 0, 40, 0));

        Text welcomeMessage = new Text("Welcome to Coordination_ORU!");
        welcomeMessage.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));

        // Center - Button Pane
        HBox buttonPane = new HBox();
        buttonPane.setSpacing(40);
        Button newProjectButton = new Button("New Project");
        Button openProjectButton = new Button("Open Project");
        buttonPane.getChildren().addAll(newProjectButton, openProjectButton);
        buttonPane.setAlignment(Pos.CENTER);

        centerPane.getChildren().addAll(welcomeMessage, buttonPane, pathLabel);
        centerPane.setAlignment(Pos.CENTER);

        // Set VBox children to grow equally
        VBox.setVgrow(buttonPane, Priority.ALWAYS);

        borderPane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        newProjectButton.setOnAction(e -> {
            File selectedFile = fileCreator(this, "Name of Project: ", "json");
            if (selectedFile != null) {
                projectFile = selectedFile.getAbsolutePath();
                pathLabel.setText("Name of Project: " + selectedFile.getName());
                isNewProject = true;
                separator.setVisible(true);
                nextButton.setVisible(true);

                // Write to the file
                try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                    fileWriter.write("{}");
                } catch (IOException ex) {
                    pathLabel.setText("Error: Could not save the file.");
                }
            }
        });

        openProjectButton.setOnAction(e -> {
            File file = fileChooser(this, "Select a project file to open: ", "json");
            if (file != null) {
                projectFile = file.getAbsolutePath();
                pathLabel.setText("Name of Project: " + file.getName());
                isNewProject = false;
                separator.setVisible(true);
                nextButton.setVisible(true);
                try {
                    projectData = parseJSON(projectFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(separator, nextButton));
        borderPane.setPrefWidth(400);
        return new Scene(borderPane);
    }

    private Scene displayMapScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane - Menu Bar
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableSaveProject();
        MenuBar.disableNewProject();
        MenuBar.disableOpenProject();

        VBox centerPane = new VBox();

        // Center Pane
        if (isNewProject) {
            Text mapMessage = new Text("Please select a map: ");
            Button browseMapButton = new Button("Browse...");
            browseMapButton.setOnAction(e -> {
                File file = fileChooser(this, "Select a map file to open: ", "yaml");

                OccupancyMap om = new OccupancyMap(file.getAbsolutePath());

                // Create an instance of MapInspector and pass the OccupancyMap to it
                MapInspector mapInspector = new MapInspector(om);

//                if (file != null) {
//                    projectData.setMap(file.getAbsolutePath());
//                    mapData = parseYAML(projectData.getMap());
//                    ImageView imageView = getImageView(this);
//                    centerPane.getChildren().add(imageView);
//                }
            });
            centerPane.getChildren().addAll(mapMessage, browseMapButton);
            // TODO: Add a map preview

//        mapData = yamlParser.parse(projectData.getMap());
//        ImageView imageView = getImageView(this);

//        centerPane.getChildren().add(imageView);

        }
        else {
            mapData = parseYAML(projectData.getMap());
            ImageView imageView = getImageView(this);
            centerPane.getChildren().add(imageView);
        }
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setSpacing(10);
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setCenter(centerPane);

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(separator, backButton, nextButton));

        return new Scene(borderPane);
    }

    private Scene displayVehicleScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane - Menu Bar
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableSaveProject();
        MenuBar.disableNewProject();
        MenuBar.disableOpenProject();

        // Right Pane
        var rightPane = new StackPane();
        ImageView imageView = getImageView(this);
        rightPane.getChildren().add(imageView);
        rightPane.setAlignment(Pos.CENTER);
        borderPane.setRight(rightPane);
//        BorderPane.setMargin(rightPane, new Insets(0, 20, 0, 0));

        // Center Pane
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setHgap(10);
        centerPane.setVgap(10);

        // name text-field
        Text name = new Text("Name of Vehicle: ");
        GridPane.setConstraints(name, 0, 0);
        TextField nameField = new TextField();
        nameField.setMaxWidth(220);
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    nameField.setText(String.valueOf(selectedVehicle.getName()));
                }
            }
        });
        nameField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newVehicleName = nameField.getText();
                String oldVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (!Objects.equals(newVehicleName, "") && newVehicleName != null) {
                    projectData.getVehicle(projectData.getVehicleID(oldVehicleName, projectData.getVehicles())).setName(newVehicleName);
                    vehiclesListView.getItems().clear();
                    projectData.getVehicles().forEach(vehicle -> vehiclesListView.getItems().add(vehicle.getName()));
                }
            }
        });
        GridPane.setConstraints(nameField, 1, 0);

        // length text-field
        Text length = new Text("Length (m): ");
        GridPane.setConstraints(length, 0, 1);
        TextField lengthField = new TextField();
        lengthField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    lengthField.setText(String.valueOf(selectedVehicle.getLength()));
                }
            }
        });
        lengthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(lengthField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLength = Double.parseDouble(lengthField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setLength(newLength);
                }
            }
        });
        GridPane.setConstraints(lengthField, 1, 1);

        // width text-field
        Text width = new Text("Width (m): ");
        GridPane.setConstraints(width, 0, 2);
        TextField widthField = new TextField();
        widthField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    widthField.setText(String.valueOf(selectedVehicle.getWidth()));
                }
            }
        });
        widthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(widthField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newWidth = Double.parseDouble(widthField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setWidth(newWidth);
                }
            }
        });
        GridPane.setConstraints(widthField, 1, 2);

        // maxVelocity text-field
        Text maxVelocity = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocity, 0, 3);
        TextField maxVelocityField = new TextField();
        maxVelocityField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    maxVelocityField.setText(String.valueOf(selectedVehicle.getMaxVelocity()));
                }
            }
        });
        maxVelocityField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(maxVelocityField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxVelocity = Double.parseDouble(maxVelocityField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMaxVelocity(newMaxVelocity);
                }
            }
        });
        GridPane.setConstraints(maxVelocityField, 1, 3);

        // maxAcceleration text-field
        Text maxAcceleration = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAcceleration, 0, 4);
        TextField maxAccelerationField = new TextField();
        maxAccelerationField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    maxAccelerationField.setText(String.valueOf(selectedVehicle.getMaxAcceleration()));
                }
            }
        });
        maxAccelerationField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(maxAccelerationField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxAcceleration = Double.parseDouble(maxAccelerationField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMaxAcceleration(newMaxAcceleration);
                }
            }
        });
        GridPane.setConstraints(maxAccelerationField, 1, 4);

        // safetyDistance text-field
        Text safetyDistance = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistance, 0, 5);
        TextField safetyDistanceField = new TextField();
        safetyDistanceField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    safetyDistanceField.setText(String.valueOf(selectedVehicle.getSafetyDistance()));
                }
            }
        });
        safetyDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(safetyDistanceField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newSafetyDistance = Double.parseDouble(safetyDistanceField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setSafetyDistance(newSafetyDistance);
                }
            }
        });
        GridPane.setConstraints(safetyDistanceField, 1, 5);

        // color combo-box
        Text color = new Text("Color: ");
        GridPane.setConstraints(color, 0, 6);
        ComboBox<String> colorField = new ComboBox<>();
        colorField.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    colorField.setValue(String.valueOf(selectedVehicle.getColor()));
                }
            }
        });
        colorField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (selectedVehicleName != null) {
                    String newColor = colorField.getValue();
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setColor(newColor);
                }
            }
        });
        GridPane.setConstraints(colorField, 1, 6);

        // initialPose combo-box
        Text initialPose = new Text("Start Location: ");
        GridPane.setConstraints(initialPose, 0, 7);
        ComboBox<String> initialPoseField = new ComboBox<>();
        initialPoseField.getItems().addAll(projectData.getPoses().keySet());
        initialPoseField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    initialPoseField.setValue(String.valueOf(selectedVehicle.getInitialPose()));
                }
            }
        });
        initialPoseField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (selectedVehicleName != null) {
                    String newInitialPose = initialPoseField.getValue();
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setInitialPose(newInitialPose);
                }
            }
        });
        GridPane.setConstraints(initialPoseField, 1, 7);

        // missions vbox
        Text mission = new Text("Missions: ");
        GridPane.setConstraints(mission, 0, 8);
        VBox missionField = new VBox();
        ListView<String> missionListView = new ListView<>();
        missionListView.setMaxWidth(nameField.getMaxWidth());
        missionListView.setMaxHeight(110);
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    missionListView.getItems().clear();
                    List<MissionStep> missionSteps = selectedVehicle.getMission();
                    missionSteps.forEach(missionStep -> missionListView.getItems().add(missionStep.toString()));
                }
            }
        });

        GridPane missionButtons = new GridPane();
        missionButtons.setHgap(5);
        missionButtons.setVgap(5);
        missionButtons.setAlignment(Pos.CENTER_LEFT);
        Button upMission = new Button("Move Up");
        GridPane.setConstraints(upMission, 0, 0);
        Button downMission = new Button("Move Down");
        GridPane.setConstraints(downMission, 1, 0);
        Button addMission = new Button("Add Mission");
        GridPane.setConstraints(addMission, 0, 1);
        Button deleteMission = new Button("Delete Mission");
        GridPane.setConstraints(deleteMission, 1, 1);
        missionButtons.setMaxWidth(nameField.getPrefWidth());

        // Set action for upMission button
        upMission.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex > 0) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex - 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex - 1);
            }
            // TODO: Move the mission up in the vehicle
        });

        // Set action for downMission button
        downMission.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex < missionListView.getItems().size() - 1) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex + 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex + 1);
            }
            // TODO: Move the mission down in the vehicle
        });

        addMission.setOnAction(e -> {
            AddMissionDialogBox.display("Adding a Mission", projectData.getPoses().keySet(), missionListView);
            // TODO: Add the mission to the vehicle
        });
        deleteMission.setOnAction(e -> {
            missionListView.getItems().remove(missionListView.getSelectionModel().getSelectedItem());
            // TODO: Delete the mission from the vehicle
        });
        missionButtons.getChildren().addAll(upMission, downMission, addMission, deleteMission);
        missionField.setAlignment(Pos.CENTER);
        missionField.setSpacing(5);
        missionField.setMaxWidth(nameField.getMaxWidth());
        missionField.getChildren().addAll(missionListView, missionButtons);
        GridPane.setConstraints(missionField, 1, 8);

        // missionRepetition text-field
        Text missionRepetition = new Text("Mission Repetition: ");
        GridPane.setConstraints(missionRepetition, 0, 9);
        TextField missionRepetitionField = new TextField();
        missionRepetitionField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    missionRepetitionField.setText(String.valueOf(selectedVehicle.getMissionRepetition()));
                }
            }
        });
        missionRepetitionField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateInteger(missionRepetitionField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newMissionRepetition = Integer.parseInt(missionRepetitionField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMissionRepetition(newMissionRepetition);
                }
            }
        });
        GridPane.setConstraints(missionRepetitionField, 1, 9);

        // lookAheadDistance text-field
        Text lookAheadDistance = new Text("Look Ahead Distance (m): ");
        lookAheadDistance.setVisible(false);
        GridPane.setConstraints(lookAheadDistance, 0, 11);
        TextField lookAheadDistanceField = new TextField();
        lookAheadDistanceField.setVisible(false);
        lookAheadDistanceField.setMaxWidth(nameField.getMaxWidth());
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    lookAheadDistanceField.setText(String.valueOf(selectedVehicle.getLookAheadDistance()));
                }
            }
        });
        lookAheadDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(lookAheadDistanceField);
                String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLookAheadDistance = Double.parseDouble(lookAheadDistanceField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setLookAheadDistance(newLookAheadDistance);
                }
            }
        });
        GridPane.setConstraints(lookAheadDistanceField, 1, 11);

        // isHumanVehicle checkbox
        Text isHuman = new Text("Human Operated: ");
        GridPane.setConstraints(isHuman, 0, 10);
        CheckBox isHumanField = new CheckBox();
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    boolean ifHuman = "Human".equals(selectedVehicle.getType());
                    isHumanField.setSelected(ifHuman);

                    lookAheadDistance.setVisible(ifHuman);
                    lookAheadDistanceField.setVisible(ifHuman);
                }
            }
        });
        isHumanField.setOnAction(e -> {
            String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem();
            Vehicle selectedVehicle = selectedVehicleName != null ? projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())) : null;

            if (isHumanField.isSelected()) {
                lookAheadDistance.setVisible(true);
                lookAheadDistanceField.setVisible(true);
                if (selectedVehicle != null) {
                    selectedVehicle.setType("Human");
                }
            } else {
                lookAheadDistance.setVisible(false);
                lookAheadDistanceField.setVisible(false);
                if (selectedVehicle != null) {
                    selectedVehicle.setType("Autonomous");
                }
            }
        });
        GridPane.setConstraints(isHumanField, 1, 10);

        centerPane.getChildren().addAll(name, nameField,
                length, lengthField,
                width, widthField,
                maxVelocity, maxVelocityField,
                maxAcceleration, maxAccelerationField,
                safetyDistance, safetyDistanceField,
                color, colorField,
                initialPose, initialPoseField,
                mission, missionField,
                missionRepetition, missionRepetitionField,
                isHuman, isHumanField,
                lookAheadDistance, lookAheadDistanceField);
        borderPane.setCenter(centerPane);
//        BorderPane.setMargin(centerPane, new Insets(20, 20, 20, 20));

        // Left Pane - VehicleList
        VBox leftPane = new VBox();

        Text vehiclesText = new Text("List of Vehicles: ");
        vehiclesText.setFont(Font.font("System", FontWeight.BOLD, 12));

        vehiclesListView.setMaxWidth(220);
        vehiclesListView.getItems().clear();
        projectData.getVehicles().forEach(vehicle -> vehiclesListView.getItems().add(vehicle.getName()));
        Button addVehicleButton = new Button("Add Vehicle");
        Button deleteVehicleButton = new Button("Delete Vehicle");

        addVehicleButton.setOnAction(e -> {
            String baseNameOfVehicle = "vehicle";
            double lengthOfVehicle = 9;
            double widthOfVehicle = 5;
            double maxVelocityOfVehicle = 10.0;
            double maxAccelerationOfVehicle = 1.0;
            double safetyDistanceOfVehicle = 0.0;
            String colorOfVehicle = "Yellow";
            String initialPoseOfVehicle = projectData.getPoses().keySet().stream().findFirst().orElse(null);
            List<MissionStep> missionOfVehicle = new ArrayList<>();
            int missionRepetitionOfVehicle = 1;
            isHumanField.setSelected(false);
            double lookAheadDistanceOfVehicle = 0.0;

            String nameOfVehicle = baseNameOfVehicle;
            int counter = 1;
            while (vehicleExists(nameOfVehicle, projectData)) {
                // Append a number to the base name
                nameOfVehicle = baseNameOfVehicle + " (" + counter + ")";
                counter++;
            }

            var vehicle = new Vehicle();
            vehicle.setName(nameOfVehicle);
            vehicle.setLength(lengthOfVehicle);
            vehicle.setWidth(widthOfVehicle);
            vehicle.setMaxVelocity(maxVelocityOfVehicle);
            vehicle.setMaxAcceleration(maxAccelerationOfVehicle);
            vehicle.setSafetyDistance(safetyDistanceOfVehicle);
            vehicle.setColor(colorOfVehicle);
            vehicle.setInitialPose(initialPoseOfVehicle);
            vehicle.setMission(missionOfVehicle);
            vehicle.setMissionRepetition(missionRepetitionOfVehicle);
            vehicle.setType("Autonomous");
            vehicle.setLookAheadDistance(lookAheadDistanceOfVehicle);

            projectData.addVehicle(vehicle);
            vehiclesListView.getItems().add(vehicle.getName());
        });

        // Delete vehicle button
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicleName = vehiclesListView.getSelectionModel().getSelectedItem(); // Get the selected item
            projectData.removeVehicle(projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).getID()); // Remove from ProjectData
            vehiclesListView.getItems().remove(selectedVehicleName); // Remove from ListView
        });

        leftPane.setAlignment(Pos.CENTER);
        HBox buttons = new HBox(addVehicleButton, deleteVehicleButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        buttons.setMaxWidth(vehiclesListView.getMaxWidth());
        leftPane.getChildren().addAll(vehiclesText, vehiclesListView, buttons);
        leftPane.setSpacing(10);
        borderPane.setLeft(leftPane);
        BorderPane.setMargin(leftPane, new Insets(0, 0, 0, 20));

        // Listener for list selection changes
        vehiclesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get the selected vehicle's details
                ProjectData.Vehicle vehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));

                // Update the fields in the centerPane with the details of the selected vehicle
                nameField.setText(newValue); // TODO
                lengthField.setText(String.valueOf(vehicle.getLength()));
                lengthField.setStyle("-fx-border-color: green");
                widthField.setText(String.valueOf(vehicle.getWidth()));
                widthField.setStyle("-fx-border-color: green");
                maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxVelocityField.setStyle("-fx-border-color: green");
                maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                maxAccelerationField.setStyle("-fx-border-color: green");
                safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
                safetyDistanceField.setStyle("-fx-border-color: green");
                colorField.setValue(vehicle.getColor());
                initialPoseField.setValue(projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles())).getInitialPose());
//                if (vehicle.getGoalPoses() != null && vehicle.getGoalPoses().length > 0) {
//                    goalPoseField.setValue(vehicle.getGoalPoses()[0]);
//                }
                isHumanField.setSelected(isHumanField.isSelected()); // FIXME
                lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                lookAheadDistanceField.setStyle("-fx-border-color: green");
                missionRepetitionField.setText(String.valueOf(vehicle.getMissionRepetition()));
            }
        });
        vehiclesListView.getSelectionModel().selectFirst();

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(separator, backButton, nextButton));

        return new Scene(borderPane, 1440, 800);
    }

    private Scene displaySimulationScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane - Menu Bar
        borderPane.setTop(MenuBar.getMenuBar(this));

        // Center Pane
        var centerPane = new VBox();
        centerPane.setSpacing(10);

        var settingsPane = new GridPane();
        settingsPane.setPadding(new Insets(10, 10, 10, 10));
        settingsPane.setHgap(10);
        settingsPane.setVgap(10);
        settingsPane.setAlignment(Pos.CENTER);

        Text heuristics = new Text("Heuristics: "); // TODO
        GridPane.setConstraints(heuristics, 0, 0);
        ChoiceBox<String> heuristicsField = new ChoiceBox<>();
        heuristicsField.getItems().addAll("Closest First", "Human First", "Autonomous First");
        heuristicsField.setValue(heuristicsField.getItems().get(0));
        GridPane.setConstraints(heuristicsField, 1, 0);

        Text noOfRuns = new Text("No. of Runs: "); // TODO
        GridPane.setConstraints(noOfRuns, 0, 1);
        TextField noOfRunsField = new TextField();
        GridPane.setConstraints(noOfRunsField, 1, 1);

        Text simulationTime = new Text("Simulation Time (minutes): "); // TODO
        GridPane.setConstraints(simulationTime, 0, 2);
        GridPane.setConstraints(simulationTimeField, 1, 2);

        Text reportsSaving = new Text("Saving Vehicles Reports: "); //TODO
        GridPane.setConstraints(reportsSaving, 0, 3);
        CheckBox reportsSavingField = new CheckBox();
        reportsSavingField.setSelected(true);
        GridPane.setConstraints(reportsSavingField, 1, 3);

        Text reportsFolder = new Text("Folder to Save the Reports: "); //TODO
        GridPane.setConstraints(reportsFolder, 0, 4);
        Button btnBrowse = new Button("Browse...");

        GridPane.setConstraints(btnBrowse, 1, 4);

        settingsPane.getChildren().addAll(heuristics, heuristicsField, simulationTime,
                simulationTimeField, noOfRuns, noOfRunsField, reportsSaving, reportsSavingField,
                reportsFolder, btnBrowse);

        centerPane.setAlignment(Pos.CENTER);
        centerPane.getChildren().add(settingsPane);
        borderPane.setCenter(centerPane);
        BorderPane.setMargin(centerPane, new Insets(10, 0, 0, 0));

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(separator, backButton, resetButton,
                saveButton, runButton));

        return new Scene(borderPane, 600, 300);
    }

    private boolean vehicleExists(String name, ProjectData projectData) {
        return projectData.getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(name));
    }

    private void runSimulation() {

        int runTime = 10; // FIXME Auto Load
        String resultsDirectory = System.getProperty("user.dir"); // FIXME Hard Coded
        final String YAML_FILE = projectData.getMap();
        double mapResolution = mapData.getResolution();
        double scaleAdjustment = 1 / mapResolution;
        double lookAheadDistance = 45 / scaleAdjustment;
        double timeIntervalInSeconds = 0.25;
        int updateCycleTime = 100;
        boolean writeRobotReports = false;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics TODO Fix Heuristics
        var heuristic = new Heuristics();
        tec.addComparator(heuristic.closest());
        String heuristicName = heuristic.getHeuristicName();

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new JTSDrawingPanelVisualization();
        viz.setMap(YAML_FILE); // TODO Fix Zooming of the Map
        viz.setSize(1920, 1200);
        tec.setVisualization(viz);

        projectData.getVehicles().forEach((vehicle) -> {

            AbstractVehicle newVehicle;
            if (vehicle.getType().equals("Autonomous")) {
                newVehicle = new AutonomousVehicle();
            } else {
                newVehicle = new LookAheadVehicle();
                ((LookAheadVehicle) newVehicle).setLookAheadDistance(lookAheadDistance);
            }

        newVehicle.setName(vehicle.getName());
        newVehicle.setMaxVelocity(vehicle.getMaxVelocity());
        newVehicle.setMaxAcceleration(vehicle.getMaxAcceleration());
        newVehicle.setSafetyDistance(vehicle.getSafetyDistance());
//        newVehicle.setColor(stringToColor(vehicle.getColor()));
        newVehicle.setLength(vehicle.getLength());
        newVehicle.setWidth(vehicle.getWidth());
//        newVehicle.setInitialPose(getPosesByName(this, vehicle.getInitialPose())[0]); // FIXME Fix Initial Pose
//        newVehicle.setGoalPoses(getPosesByName(this, vehicle.getGoals().get(0)));
        // FIXME Fix Goal Poses

        newVehicle.getPlan(newVehicle.getInitialPose(),
                newVehicle.getGoalPoses(), YAML_FILE, true);

        tec.setForwardModel(newVehicle.getID(), new ConstantAccelerationForwardModel(newVehicle.getMaxAcceleration(),
                newVehicle.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                tec.getRobotTrackingPeriodInMillis(newVehicle.getID())));
        tec.setDefaultFootprint(newVehicle.getFootprint());

        tec.placeRobot(newVehicle.getID(), newVehicle.getInitialPose());

        var mission = new Mission(newVehicle.getID(), newVehicle.getPath());
        Missions.enqueueMission(mission);
        Missions.setMap(YAML_FILE);

    Missions.startMissionDispatchers(tec, writeRobotReports, timeIntervalInSeconds,
            runTime, heuristicName, updateCycleTime, resultsDirectory, scaleAdjustment);
        });
    }
}

