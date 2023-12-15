package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;
import se.oru.coordination.coordination_oru.gui.ProjectData.Vehicle;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.JTSDrawingPanelVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class GUI extends Application {

    protected Stage primaryStage;
    private final Text filePathText = new Text(""); // FIXME: Maybe remove as a field
    protected Boolean isNewProject = false;
    protected Boolean isProjectScene = false;
    protected Boolean isMapScene = false;
    protected Boolean isVehicleScene = false;
    protected Boolean isSimulationScene = false;
    protected String projectFile = "";
    protected ProjectData projectData;
    protected ProjectData orignalProjectData; // The original loaded projectData before any changes
    protected MapData mapData;
    private final Button backButton = new Button("Back");
    protected final Button nextButton = new Button("Next");
    private final Button saveButton = new Button("Save");
    private final Button resetButton =  new Button("Reset");
    private final Button runButton =  new Button("Run");
    private final ListView<String> vehicleListView = new ListView<>();
    private Boolean writeVehicleReports = false;
    private int simulationTime = 5;
    private int numberOfRuns = 1;
    private String reportsFolder = "";
    private final Heuristics heuristics = new Heuristics();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Setting up the primaryStage
        this.primaryStage = primaryStage;
        toggleScene(true, false, false, false);
        Scene projectScene = displayProjectScene();
        primaryStage.setTitle("Coordination_ORU");
        primaryStage.setOnCloseRequest(e -> quitProgram(primaryStage));
        primaryStage.setScene(projectScene);
        primaryStage.show();

        // Setting action for navigation buttons
        nextButton.setOnAction(e -> {
            if (isProjectScene && !isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(false, true, false, false);
                primaryStage.setTitle("Coordination_ORU: Setting up the map");
                primaryStage.setScene(displayMapScene());
                primaryStage.centerOnScreen(); // FIXME: Sometimes centerOnScreen() does not work
            }
            else if (!isProjectScene && isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(false, false, true, false);
                primaryStage.setTitle("Coordination_ORU: Setting up the vehicles");
                primaryStage.setScene(displayVehicleScene());
                primaryStage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && isVehicleScene && !isSimulationScene) {
                toggleScene(false, false, false, true);
                primaryStage.setTitle("Coordination_ORU: Setting up the simulation");
                primaryStage.setScene(displaySimulationScene());
                primaryStage.centerOnScreen();
            }
        });

        backButton.setOnAction(e -> {
            if (!isProjectScene && isMapScene && !isVehicleScene && !isSimulationScene) {
                toggleScene(true, false, false, false);
                primaryStage.setTitle("Coordination_ORU");
                primaryStage.setScene(displayProjectScene());
                primaryStage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && isVehicleScene && !isSimulationScene) {
                toggleScene(false, true, false, false);
                primaryStage.setTitle("Coordination_ORU: Setting up the map");
                primaryStage.setScene(displayMapScene());
                primaryStage.centerOnScreen();
            }
            else if (!isProjectScene && !isMapScene && !isVehicleScene && isSimulationScene) {
                toggleScene(false, false, true, false);
                primaryStage.setTitle("Coordination_ORU: Setting up the vehicles");
                primaryStage.setScene(displayVehicleScene());
                primaryStage.centerOnScreen();
            }
        });

        saveButton.setOnAction(e -> saveProject());

        runButton.setOnAction(e -> runProject());

        resetButton.setOnAction(e -> resetProject(primaryStage));
    }

    /**
     * Creates and returns a scene with a layout for project management.
     *
     * @return A new Scene containing the layout for project management.
     */
    private Scene displayProjectScene() {

        BorderPane borderPane = new BorderPane();
        borderPane.setPrefWidth(400);
        Separator separator = new Separator();
        separator.setVisible(false);
        nextButton.setVisible(false);

        // Top Pane - Menu Bar
        borderPane.setTop(GUIMenuBar.getMenuBar(this));
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableCloseProject();
        GUIMenuBar.disableRunProject();

        // Center Pane
        VBox centerPane = new VBox();
        centerPane.setSpacing(40);
        centerPane.setPadding(new Insets(40, 40, 40, 40));

        Label welcomeMessageLabel = new Label("Welcome to Coordination_ORU!");
        welcomeMessageLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        welcomeMessageLabel.setAlignment(Pos.CENTER);

        // Center - Button Pane
        HBox projectButtonPane = new HBox();
        projectButtonPane.setSpacing(40);
        Button newProjectButton = new Button("New Project");
        Button openProjectButton = new Button("Open Project");
        projectButtonPane.getChildren().addAll(newProjectButton, openProjectButton);
        projectButtonPane.setAlignment(Pos.CENTER);

        centerPane.getChildren().addAll(welcomeMessageLabel, projectButtonPane, filePathText);
        centerPane.setAlignment(Pos.CENTER);

        // Set VBox children to grow equally
        VBox.setVgrow(projectButtonPane, Priority.ALWAYS);

        borderPane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        newProjectButton.setOnAction(e -> newProject());

        openProjectButton.setOnAction(e -> openProject());

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(nextButton));
        return new Scene(borderPane);
    }

    private Scene displayMapScene() {

        BorderPane borderPane = new BorderPane();
        borderPane.setPrefWidth(400);

        // Top Pane - Menu Bar
        borderPane.setTop(GUIMenuBar.getMenuBar(this));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableRunProject();

        // Center Pane
        var centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerPane);
        centerPane.setSpacing(30);
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        BorderPane.setMargin(centerPane, new Insets(20, 0, 20, 0));

        if (isNewProject) {
            nextButton.setDisable(true);
            Text mapMessageText = new Text("Select a map to load: ");
            Button mapBrowseButton = new Button("Browse...");
            Text mapSelectedText = new Text("");
            mapSelectedText.setVisible(false);
            centerPane.getChildren().addAll(mapMessageText, mapBrowseButton, mapSelectedText);

            // Set action for browsing a map
            mapBrowseButton.setOnAction(e -> {
                File file = chooseFile(this, "Select a map file to open: ", "yaml");
                if (file != null) {
                    mapSelectedText.setText("Map selected: " + file.getAbsolutePath());
                    mapSelectedText.setVisible(true);
                    projectData.setMap(file.getAbsolutePath());
                    mapData = parseYAML(projectData.getMap());
                    String title = "Selecting the key locations on the loaded map";
                    String content = "The map has been loaded successfully!\n\n" +
                            "A new window with the loaded map will now open, and you must select at least two locations on the loaded map.\n\n" +
                            "You can select a location by clicking on the loaded map.\n";
                    AlertBox.display(title, content, Alert.AlertType.INFORMATION);
                    new MapInteract(projectData, nextButton);
                }
            });

        } else {
            mapData = parseYAML(projectData.getMap());
            ImageView imageView = getImageView(this);
            centerPane.getChildren().add(imageView);
        }

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(backButton, nextButton));

        return new Scene(borderPane);
    }
    private Scene displayVehicleScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane - Menu Bar
        borderPane.setTop(GUIMenuBar.getMenuBar(this));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableRunProject();

        // Right Pane
        var rightPane = new StackPane();
        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        rightPane.setPadding(new Insets(10, 10, 10, 10));
        rightPane.setAlignment(Pos.TOP_CENTER);
        borderPane.setRight(rightPane);
        ImageView imageView = getImageView(this);
        rightPane.getChildren().add(imageView);

        // Center Pane
        var centerPane = new GridPane();
        BorderPane.setMargin(centerPane, new Insets(10, 10, 10, 10));
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        centerPane.setAlignment(Pos.TOP_CENTER);
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        borderPane.setCenter(centerPane);

        // name text-field
        Text nameText = new Text("Name of Vehicle: ");
        GridPane.setConstraints(nameText, 0, 0);
        TextField nameTextField = new TextField();
        nameTextField.setMaxWidth(180);
        GridPane.setConstraints(nameTextField, 1, 0);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
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
                    projectData.getVehicle(projectData.getVehicleID(oldVehicleName, projectData.getVehicles())).setName(newVehicleName);
                    vehicleListView.getItems().clear();
                    projectData.getVehicles().forEach(vehicle -> vehicleListView.getItems().add(vehicle.getName()));
                    vehicleListView.getSelectionModel().selectFirst();
                }
            }
        });

        // length text-field
        Text lengthText = new Text("Length (m): ");
        GridPane.setConstraints(lengthText, 0, 1);
        TextField lengthTextField = new TextField();
        lengthTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(lengthTextField, 1, 1);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    lengthTextField.setText(String.valueOf(selectedVehicle.getLength()));
                }
            }
        });
        lengthTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(lengthTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLength = Double.parseDouble(lengthTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setLength(newLength);
                }
            }
        });

        // width text-field
        Text widthText = new Text("Width (m): ");
        GridPane.setConstraints(widthText, 0, 2);
        TextField widthTextField = new TextField();
        widthTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(widthTextField, 1, 2);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    widthTextField.setText(String.valueOf(selectedVehicle.getWidth()));
                }
            }
        });
        widthTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(widthTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newWidth = Double.parseDouble(widthTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setWidth(newWidth);
                }
            }
        });

        // maxVelocity text-field
        Text maxVelocityText = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocityText, 0, 3);
        TextField maxVelocityTextField = new TextField();
        maxVelocityTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(maxVelocityTextField, 1, 3);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    maxVelocityTextField.setText(String.valueOf(selectedVehicle.getMaxVelocity()));
                }
            }
        });
        maxVelocityTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(maxVelocityTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxVelocity = Double.parseDouble(maxVelocityTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMaxVelocity(newMaxVelocity);
                }
            }
        });

        // maxAcceleration text-field
        Text maxAccelerationText = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAccelerationText, 0, 4);
        TextField maxAccelerationTextField = new TextField();
        maxAccelerationTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(maxAccelerationTextField, 1, 4);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    maxAccelerationTextField.setText(String.valueOf(selectedVehicle.getMaxAcceleration()));
                }
            }
        });
        maxAccelerationTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(maxAccelerationTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newMaxAcceleration = Double.parseDouble(maxAccelerationTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMaxAcceleration(newMaxAcceleration);
                }
            }
        });

        // safetyDistance text-field
        Text safetyDistanceText = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistanceText, 0, 5);
        TextField safetyDistanceTextField = new TextField();
        safetyDistanceTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(safetyDistanceTextField, 1, 5);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    safetyDistanceTextField.setText(String.valueOf(selectedVehicle.getSafetyDistance()));
                }
            }
        });
        safetyDistanceTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(safetyDistanceTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newSafetyDistance = Double.parseDouble(safetyDistanceTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setSafetyDistance(newSafetyDistance);
                }
            }
        });

        // color choice-box
        Text colorText = new Text("Color: ");
        GridPane.setConstraints(colorText, 0, 6);
        ChoiceBox<String> colorChoiceBox = new ChoiceBox<>();
        colorChoiceBox.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorChoiceBox.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(colorChoiceBox, 1, 6);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    colorChoiceBox.setValue(String.valueOf(selectedVehicle.getColor()));
                }
            }
        });
        colorChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newColor = colorChoiceBox.getValue();
                projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setColor(newColor);
            }
        });

        // initialPose choice-box
        Text initialPoseText = new Text("Start Location: ");
        GridPane.setConstraints(initialPoseText, 0, 7);
        ChoiceBox<String> initialPoseChoiceBox = new ChoiceBox<>();
        initialPoseChoiceBox.getItems().addAll(projectData.getPoses().keySet());
        initialPoseChoiceBox.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(initialPoseChoiceBox, 1, 7);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    initialPoseChoiceBox.setValue(String.valueOf(selectedVehicle.getInitialPose()));
                }
            }
        });
        initialPoseChoiceBox.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
            if (selectedVehicleName != null) {
                String newInitialPose = initialPoseChoiceBox.getValue();
                projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setInitialPose(newInitialPose);
            }
        });

        // missions vbox
        Text missionText = new Text("Mission: ");
        GridPane.setConstraints(missionText, 0, 8);
        VBox missionVBox = new VBox();
        missionVBox.setSpacing(2);
        missionVBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(missionVBox, 1, 8);

        // mission list-view
        ListView<String> missionListView = new ListView<>();
        missionListView.setMaxWidth(nameTextField.getMaxWidth());
        missionListView.setMaxHeight(110);

        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    missionListView.getItems().clear();
                    List<MissionStep> missionSteps = selectedVehicle.getMission();
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
            Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), projectData.getVehicles()));
            if (selectedIndex > 0) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex - 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex - 1);
                if (selectedVehicle != null) {
                    List<MissionStep> missionSteps = selectedVehicle.getMission();
                    MissionStep missionStep = missionSteps.remove(selectedIndex);
                    missionSteps.add(selectedIndex - 1, missionStep);
                }
            }
        });

        // Set action for moving missions down
        downMissionButton.setOnAction(e -> {
            int selectedIndex = missionListView.getSelectionModel().getSelectedIndex();
            Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), projectData.getVehicles()));
            if (selectedIndex < missionListView.getItems().size() - 1) {
                String itemToMove = missionListView.getItems().remove(selectedIndex);
                missionListView.getItems().add(selectedIndex + 1, itemToMove);
                missionListView.getSelectionModel().select(selectedIndex + 1);
                if (selectedVehicle != null) {
                    List<MissionStep> missionSteps = selectedVehicle.getMission();
                    MissionStep missionStep = missionSteps.remove(selectedIndex);
                    missionSteps.add(selectedIndex + 1, missionStep);
                }
            }
        });

        // Set action for adding a mission
        addMissionButton.setOnAction(e -> {
            MissionStep addedMission = AddMissionDialogBox.display("Adding a Mission", projectData.getPoses().keySet(), missionListView);
            Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), projectData.getVehicles()));
            if (selectedVehicle != null && addedMission != null) {
                List<MissionStep> missionSteps = selectedVehicle.getMission();
                missionSteps.add(addedMission);
            }
        });

        // Set action for removing a mission
        deleteMissionButton.setOnAction(e -> {
            Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(vehicleListView.getSelectionModel().getSelectedItem(), projectData.getVehicles()));
            if (selectedVehicle != null) {
                int selectedMissionIndex = missionListView.getSelectionModel().getSelectedIndex();
                missionListView.getItems().remove(selectedMissionIndex);
                selectedVehicle.getMission().remove(selectedMissionIndex);
            }
        });

        // missionRepetition text-field
        Text missionRepetitionText = new Text("Mission Repetition: ");
        GridPane.setConstraints(missionRepetitionText, 0, 9);
        TextField missionRepetitionTextField = new TextField();
        missionRepetitionTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(missionRepetitionTextField, 1, 9);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    missionRepetitionTextField.setText(String.valueOf(selectedVehicle.getMissionRepetition()));
                }
            }
        });
        missionRepetitionTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateInteger(missionRepetitionTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newMissionRepetition = Integer.parseInt(missionRepetitionTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setMissionRepetition(newMissionRepetition);
                }
            }
        });

        // lookAheadDistance text-field
        Text lookAheadDistanceText = new Text("Look Ahead Distance (m): ");
        lookAheadDistanceText.setVisible(false);
        GridPane.setConstraints(lookAheadDistanceText, 0, 11);
        TextField lookAheadDistanceTextField = new TextField();
        lookAheadDistanceTextField.setVisible(false);
        lookAheadDistanceTextField.setMaxWidth(nameTextField.getMaxWidth());
        GridPane.setConstraints(lookAheadDistanceTextField, 1, 11);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
                if (selectedVehicle != null) {
                    lookAheadDistanceTextField.setText(String.valueOf(selectedVehicle.getLookAheadDistance()));
                }
            }
        });
        lookAheadDistanceTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateDouble(lookAheadDistanceTextField);
                String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    double newLookAheadDistance = Double.parseDouble(lookAheadDistanceTextField.getText());
                    projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).setLookAheadDistance(newLookAheadDistance);
                }
            }
        });

        // isHumanVehicle checkbox
        Text isHumanText = new Text("Human Operated: ");
        GridPane.setConstraints(isHumanText, 0, 10);
        CheckBox isHumanCheckBox = new CheckBox();
        GridPane.setConstraints(isHumanCheckBox, 1, 10);
        vehicleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Vehicle selectedVehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));
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
            Vehicle selectedVehicle = selectedVehicleName != null ? projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())) : null;

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
        leftPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setLeft(leftPane);

        Label vehiclesLabel = new Label("List of Vehicles: ");
        vehiclesLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        vehiclesLabel.setPrefWidth(215);
        vehiclesLabel.setAlignment(Pos.CENTER);

        vehicleListView.setMaxWidth(vehiclesLabel.getPrefWidth());
        vehicleListView.setPrefHeight(440);
        vehicleListView.getItems().clear();
        projectData.getVehicles().forEach(vehicle -> vehicleListView.getItems().add(vehicle.getName()));
        vehicleListView.getSelectionModel().selectFirst();

        Button addVehicleButton = new Button("Add Vehicle");
        Button deleteVehicleButton = new Button("Delete Vehicle");

        // Set action for adding a 'default' vehicle
        addVehicleButton.setOnAction(e -> {

            // Setting default values for a vehicle
            String baseNameOfVehicle = "vehicle";
            double lengthOfVehicle = 8;
            double widthOfVehicle = 4;
            double maxVelocityOfVehicle = 10.0;
            double maxAccelerationOfVehicle = 1.0;
            double safetyDistanceOfVehicle = 0.0;
            String colorOfVehicle = "Yellow";
            String initialPoseOfVehicle = projectData.getPoses().keySet().stream().findFirst().orElse(null);
            int missionRepetitionOfVehicle = 1;
            String typeOfVehicle = "Autonomous";
            double lookAheadDistanceOfVehicle = 0.0;

            // Adding a default mission
            List<MissionStep> missionOfVehicle = new ArrayList<>();
            MissionStep missionStep = new MissionStep();
            missionStep.setPoseName(projectData.getPoses().keySet().stream().
                    filter(item -> !item.equals(initialPoseOfVehicle)).
                    findAny().
                    orElse(null));
            missionStep.setDuration(1.0);
            missionOfVehicle.add(missionStep);

            // Handle duplicate names for vehicles
            String nameOfVehicle = baseNameOfVehicle;
            int counter = 1;
            while (projectData.getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(baseNameOfVehicle))) {
                // Append a number to the base name
                nameOfVehicle = baseNameOfVehicle + " (" + counter + ")";
                counter++;
            }

            // Create a new vehicle with default values
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
            vehicle.setType(typeOfVehicle);
            vehicle.setLookAheadDistance(lookAheadDistanceOfVehicle);

            projectData.addVehicle(vehicle);
            vehicleListView.getItems().add(vehicle.getName());
            vehicleListView.getSelectionModel().selectLast();
        });

        // Set action for removing a vehicle
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicleName = vehicleListView.getSelectionModel().getSelectedItem(); // Get the selected item
            projectData.removeVehicle(projectData.getVehicle(projectData.getVehicleID(selectedVehicleName, projectData.getVehicles())).getID()); // Remove from ProjectData
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
                Vehicle vehicle = projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles()));

                // Update the fields in the centerPane with the details of the selected vehicle
                nameTextField.setText(newValue);
                lengthTextField.setText(String.valueOf(vehicle.getLength()));
                widthTextField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityTextField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationTextField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceTextField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorChoiceBox.setValue(vehicle.getColor());
                initialPoseChoiceBox.setValue(projectData.getVehicle(projectData.getVehicleID(newValue, projectData.getVehicles())).getInitialPose());
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
        borderPane.setBottom(BottomPane.getBottomPane(backButton, nextButton));

        return new Scene(borderPane);
    }
    private Scene displaySimulationScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane - Menu Bar
        borderPane.setTop(GUIMenuBar.getMenuBar(this));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();

        // Center Pane
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(30, 30, 30, 30));
        BorderPane.setMargin(centerPane, new Insets(30, 30, 30, 30));
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        centerPane.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerPane);

        // heuristics choice-box
        Text heuristicsText = new Text("Heuristics: ");
        GridPane.setConstraints(heuristicsText, 0, 0);
        ChoiceBox<String> heuristicsChoiceBox = new ChoiceBox<>();
        heuristicsChoiceBox.setPrefWidth(220);
        GridPane.setConstraints(heuristicsChoiceBox, 1, 0);
        heuristicsChoiceBox.getItems().addAll(Heuristics.getAllHeuristicNames());
        heuristicsChoiceBox.setValue(heuristicsChoiceBox.getItems().stream().findFirst().orElse(null));
        heuristicsChoiceBox.setOnAction(e -> {
            String selectedHeuristic = heuristicsChoiceBox.getValue();
            if (selectedHeuristic != null) {
                switch (selectedHeuristic) {
                    case "MOST_DISTANCE_TRAVELLED":
                        heuristics.mostDistanceTravelled();
                        break;
                    case "MOST_DISTANCE_TO_TRAVEL":
                        heuristics.mostDistanceToTravel();
                        break;
                    case "RANDOM":
                        heuristics.random();
                        break;
                    case "HIGHEST_PRIORITY_FIRST":
                        heuristics.highestPriorityFirst();
                        break;
                    case "HUMAN_FIRST":
                        heuristics.humanFirst();
                        break;
                    case "AUTONOMOUS_FIRST":
                        heuristics.autonomousFirst();
                        break;
                    default:
                        heuristics.closestFirst();
                        break;
                }
            }
        });

        // simulationTime text-field
        Text simulationTimeText = new Text("Simulation Time (minutes): ");
        GridPane.setConstraints(simulationTimeText, 0, 1);
        TextField simulationTimeTextField = new TextField();
        simulationTimeTextField.setMaxWidth(heuristicsChoiceBox.getPrefWidth());
        simulationTimeTextField.setText("30");
        GridPane.setConstraints(simulationTimeTextField, 1, 1);
        simulationTimeTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateInteger(simulationTimeTextField);
                if (validated) {
                    simulationTime = Integer.parseInt(simulationTimeTextField.getText());
                }
            }
        });

        Text numberOfRunsText = new Text("No. of Runs: ");
        GridPane.setConstraints(numberOfRunsText, 0, 2);
        TextField numberOfRunsTextField = new TextField();
        numberOfRunsTextField.setMaxWidth(heuristicsChoiceBox.getPrefWidth());
        numberOfRunsTextField.setText("1");
        GridPane.setConstraints(numberOfRunsTextField, 1, 2);
        numberOfRunsTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = validateInteger(numberOfRunsTextField);
                if (validated) {
                    numberOfRuns = Integer.parseInt(numberOfRunsTextField.getText()); // FIXME: How to implement multiple runs of simulation?
                }
            }
        });

        Text reportsLocationText = new Text("Reports will be saved in:");
        GridPane.setConstraints(reportsLocationText, 0, 5);
        reportsLocationText.setVisible(false);
        Text reportsFolderLocation = new Text();
        GridPane.setConstraints(reportsFolderLocation, 1, 5);
        reportsFolderLocation.setVisible(false);

        Text reportsFolderText = new Text("Folder to Save the Reports: ");
        GridPane.setConstraints(reportsFolderText, 0, 4);
        reportsFolderText.setVisible(false);
        Button reportFolderButton = new Button("Browse...");
        GridPane.setConstraints(reportFolderButton, 1, 4);
        reportFolderButton.setVisible(false);
        reportFolderButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(new Stage());
            if (selectedDirectory != null) {
                reportsLocationText.setVisible(true);
                reportsFolderLocation.setVisible(true);
                reportsFolder = selectedDirectory.getAbsolutePath();
                reportsFolderLocation.setText(reportsFolder);
            }
        });

        Text saveReportsText = new Text("Saving Vehicles Reports: ");
        GridPane.setConstraints(saveReportsText, 0, 3);
        CheckBox saveReportsCheckBox = new CheckBox();
        saveReportsCheckBox.setSelected(false);
        GridPane.setConstraints(saveReportsCheckBox, 1, 3);
        saveReportsCheckBox.setOnAction(e -> {
            if (saveReportsCheckBox.isSelected()) {
                reportsFolderText.setVisible(true);
                reportFolderButton.setVisible(true);
            } else {
                reportsFolderText.setVisible(false);
                reportFolderButton.setVisible(false);
                reportsLocationText.setVisible(false);
                reportsFolderLocation.setVisible(false);
            }
            writeVehicleReports = saveReportsCheckBox.isSelected();
        });

        centerPane.getChildren().addAll(heuristicsText, heuristicsChoiceBox, simulationTimeText,
                simulationTimeTextField, numberOfRunsText, numberOfRunsTextField, saveReportsText, saveReportsCheckBox,
                reportsFolderText, reportFolderButton, reportsLocationText, reportsFolderLocation);

        // Bottom Pane - Navigation Buttons
        borderPane.setBottom(BottomPane.getBottomPane(backButton, resetButton,
                saveButton, runButton));

        return new Scene(borderPane);
    }

    // FIXME maybe better to ensure only one scene is true at a time
    // A method to toggle the scenes
    private void toggleScene(Boolean isProjectScene, Boolean isMapScene, Boolean isVehicleScene, Boolean isSimulationScene) {
        this.isProjectScene = isProjectScene;
        this.isMapScene = isMapScene;
        this.isVehicleScene = isVehicleScene;
        this.isSimulationScene = isSimulationScene;
    }

    // A method to create a new project
    protected void newProject() {
        File selectedFile = createFile(this, "newProject", "json");
        if (selectedFile != null) {
            projectFile = selectedFile.getAbsolutePath();
            filePathText.setText("Name of Project: " + selectedFile.getName());
            projectData = new ProjectData();
            mapData = new MapData();
            isNewProject = true;
            nextButton.setVisible(true);

            // Write {} to the new project file
            try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                fileWriter.write("{}");
            } catch (IOException ex) {
                filePathText.setText("Error: Could not save the file.");
            }
        }
    }

    // A method to open an existing project
    protected void openProject() {
        File file = chooseFile(this, "Select a project file to open: ", "json");
        if (file != null) {
            projectFile = file.getAbsolutePath();
            filePathText.setText("Name of Project: " + file.getName());
            isNewProject = false;
            nextButton.setVisible(true);
            try {
                projectData = parseJSON(projectFile);
                orignalProjectData = deepCopy(projectData);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // A method to save the current project
    protected void saveProject() {
        try{
            if (isNewProject) {
                AlertBox.display("Saving the project", "The project has been saved to: " + projectFile, Alert.AlertType.INFORMATION);
                writeJSON(projectData, projectFile);
            } else {
                if (projectData.equals(orignalProjectData)) {
                    AlertBox.display("Saving the project", "There are no changes to save in the project: " + projectFile, Alert.AlertType.INFORMATION);
                } else {
                    var selectedFile = createFile(this, "project", "json");
                    if (selectedFile != null) {
                        projectFile = selectedFile.getAbsolutePath();
                        AlertBox.display("Saving the project", "The project has been saved to: " + projectFile, Alert.AlertType.INFORMATION);
                        writeJSON(projectData, projectFile);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // A method to run the current project
    protected void runProject() {

        final String YAML_FILE = projectData.getMap();
        double mapResolution = mapData.getResolution();
        double scaleAdjustment = 1 / mapResolution;
        double lookAheadDistance = 45 / scaleAdjustment; // FIXME Currently for single vehicle, needs to be changed for multiple vehicles and take value from vehicle objects
        double reportsTimeIntervalInSeconds = 0.1;     // FIXME Fix Time Interval hard coded, maybe give option in GUI

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        tec.addComparator(heuristics.closestFirst()); // FIXME Fix Heuristics Hard Coded

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new JTSDrawingPanelVisualization(); // FIXME Fix Visualization zooming, arrow direction, show consistent area, check maybe use BrowserVisualization, RVizVisualization
        viz.setMap(YAML_FILE);
        viz.setSize();
        tec.setVisualization(viz);

        projectData.getVehicles().forEach((vehicle) -> {

            AbstractVehicle newVehicle;
            if (vehicle.getType().equals("Autonomous")) {
                newVehicle = new AutonomousVehicle();
            } else {
                newVehicle = new LookAheadVehicle();
                ((LookAheadVehicle) newVehicle).setLookAheadDistance(lookAheadDistance);
            }

            newVehicle.setID(vehicle.getID());
            newVehicle.setName(vehicle.getName());
            newVehicle.setLength(vehicle.getLength() / scaleAdjustment);
            newVehicle.setWidth(vehicle.getWidth() / scaleAdjustment);
            newVehicle.setMaxVelocity(vehicle.getMaxVelocity() / scaleAdjustment);
            newVehicle.setMaxAcceleration(vehicle.getMaxAcceleration() / scaleAdjustment);
            newVehicle.setSafetyDistance(vehicle.getSafetyDistance() / scaleAdjustment);
            newVehicle.setColor(stringToColor(vehicle.getColor()));
            newVehicle.setInitialPose(projectData.getPose(vehicle.getInitialPose()));
            newVehicle.setGoalPoses(vehicle.getMission()
                    .stream()
                    .map(MissionStep::getPoseName)
                    .map(poseName -> projectData.getPose(poseName))
                    .toArray(Pose[]::new));
//            newVehicle.setMission(vehicle.getMission()); // FIXME Fix Mission, How to handle multiple missions to GoalPoses, handle stoppages
            newVehicle.setMissionRepetition(vehicle.getMissionRepetition()); //FIXME Handle Mission Repetitions in missionsDispatcher

            newVehicle.getPlan(newVehicle.getInitialPose(),
                    newVehicle.getGoalPoses(), YAML_FILE, true);

            tec.setForwardModel(newVehicle.getID(), new ConstantAccelerationForwardModel(newVehicle.getMaxAcceleration(),
                    newVehicle.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                    tec.getRobotTrackingPeriodInMillis(newVehicle.getID())));
            tec.setDefaultFootprint(newVehicle.getFootprint());

            tec.placeRobot(newVehicle.getID(), newVehicle.getInitialPose());

            var mission = new Mission(newVehicle.getID(), newVehicle.getPath());
            Missions.enqueueMission(mission);
        });
            Missions.setMap(YAML_FILE);
            Missions.startMissionDispatchers(tec, writeVehicleReports, reportsTimeIntervalInSeconds,
                    simulationTime, heuristics.getName(), 100, reportsFolder, scaleAdjustment);
    }

    // A method to reset the GUI interface
    protected void resetProject(Stage stage) {
        stage.setTitle("Coordination_ORU");
        stage.setScene(displayProjectScene());
        stage.centerOnScreen();
        projectData = new ProjectData();
        mapData = new MapData();
        isNewProject = false;
        isProjectScene = true;
        isMapScene = false;
        isVehicleScene = false;
        isSimulationScene = false;
        projectFile = "";
        vehicleListView.getItems().clear();
    }

    // A method to quit the project gracefully and save the project if needed
    protected void quitProgram(Stage stage) {
        if (projectData == null) {
            stage.close();
        } else if (projectData.equals(orignalProjectData)) {
            stage.close();
        } else {
            Optional<ButtonType> answer = AlertBox.display("Saving the project", "Would you like to save the project before exiting?", Alert.AlertType.CONFIRMATION);
            if (answer.isPresent() && answer.get() == ButtonType.YES && projectData != null) saveProject();
            stage.close();
        }
    }
}

