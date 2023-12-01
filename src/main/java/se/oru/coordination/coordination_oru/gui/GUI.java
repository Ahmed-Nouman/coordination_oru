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

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class GUI extends Application {

    private final TextField simulationTimeField = new TextField();
    final Stage stage = new Stage();
    final Label pathLabel = new Label("");
    protected Boolean isNewProject = false;
    protected String projectFile = "";
    protected ProjectData projectData;
    protected MapData mapData;
    protected Button nextProjectButton = new Button("Next");
    private final Button backMapButton = new Button("Back");
    private final Button nextMapButton = new Button("Next");
    private final Button nextVehicleButton = new Button("Next");
    private final Button backVehicleButton = new Button("Back");
    private final Button runSimulationButton = new Button("Run");
    private final Button saveSimulationButton = new Button("Save");
    private final Button resetSimulationButton =  new Button("Reset");
    private final Button backSimulationButton = new Button("Back");
    private final ListView<String> vehiclesListView = new ListView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Scene projectScene = displayProjectScene();
        stage.setTitle("Coordination_ORU");
        stage.setScene(projectScene);
        stage.show();

        nextProjectButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU: Setting up the map");
            stage.setScene(displayMapScene());
            stage.centerOnScreen();
        });

        nextMapButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU: Setting the vehicles");
            stage.setScene(displayVehicleScene());
            stage.centerOnScreen();
        });

        backMapButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU");
            stage.setScene(displayProjectScene());
            stage.centerOnScreen();
            nextProjectButton.setVisible(true);
        });

        nextVehicleButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU: Setting the simulation");
            stage.setScene(displaySimulationScene());
            stage.centerOnScreen();
        });

        backVehicleButton.setOnAction(e -> {
            // FIXME Centering Issue
            stage.setTitle("Coordination_ORU: Setting the map");
            stage.setScene(displayMapScene());
            stage.centerOnScreen();
        });

        backSimulationButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU: Setting the vehicles");
            stage.setScene(displayVehicleScene());
            stage.centerOnScreen();
        });

        saveSimulationButton.setOnAction(e -> System.out.println("File saved"));

        runSimulationButton.setOnAction(e -> runSimulation());

        resetSimulationButton.setOnAction(e -> {
            stage.setTitle("Coordination_ORU");
            stage.setScene(displayProjectScene());
            stage.centerOnScreen();
        });

    }

    private Scene displaySimulationScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane
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

        // Bottom Pane
        borderPane.setBottom(BottomPane.getBottomPane(backSimulationButton, resetSimulationButton,
                saveSimulationButton, runSimulationButton));

        return new Scene(borderPane, 600, 300);
    }

    private Scene displayVehicleScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane
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

        // name text-field FIXME
        Text name = new Text("Name of Vehicle: ");
        GridPane.setConstraints(name, 0, 0);
        TextField nameField = new TextField();
        nameField.setMaxWidth(220);
        nameField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            System.out.println("Vehicle Name Changed");
//            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
//            projectData.getVehicle(projectData.getVehicle(selectedVehicle).getID()).setName(nameField.getText());
        });
        GridPane.setConstraints(nameField, 1, 0);

        // length text-field
        Text length = new Text("Length (m): ");
        GridPane.setConstraints(length, 0, 1);
        TextField lengthField = new TextField();
        lengthField.setMaxWidth(nameField.getMaxWidth());
        lengthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            Boolean validated = validateDouble(lengthField);
            if (validated) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
//                projectData.getVehicle(projectData.getVehicle(selectedVehicle).getID()).setLength(Double.parseDouble(lengthField.getText()));
                projectData.getVehicle(selectedVehicle).setLength(Double.parseDouble(lengthField.getText()));
            }
        });
        GridPane.setConstraints(lengthField, 1, 1);

        // width text-field
        Text width = new Text("Width (m): ");
        GridPane.setConstraints(width, 0, 2);
        TextField widthField = new TextField();
        widthField.setMaxWidth(nameField.getMaxWidth());
        widthField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            Boolean validated  = validateDouble(widthField);
            if (validated) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
                projectData.getVehicle(selectedVehicle).setWidth(Double.parseDouble(widthField.getText()));
            }
        });
        GridPane.setConstraints(widthField, 1, 2);

        // maxVelocity text-field
        Text maxVelocity = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocity, 0, 3);
        TextField maxVelocityField = new TextField();
        maxVelocityField.setMaxWidth(nameField.getMaxWidth());
        maxVelocityField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            Boolean validated = validateDouble(maxVelocityField);
            if (validated) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
                projectData.getVehicle(selectedVehicle).setMaxVelocity(Double.parseDouble(maxVelocityField.getText()));
            }
        });
        GridPane.setConstraints(maxVelocityField, 1, 3);

        // maxAcceleration text-field
        Text maxAcceleration = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAcceleration, 0, 4);
        TextField maxAccelerationField = new TextField();
        maxAccelerationField.setMaxWidth(nameField.getMaxWidth());
        maxAccelerationField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (validateDouble(maxAccelerationField)) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
                projectData.getVehicle(selectedVehicle).setMaxAcceleration(Double.parseDouble(maxAccelerationField.getText()));
            }
        });
        GridPane.setConstraints(maxAccelerationField, 1, 4);

        // safetyDistance text-field
        Text safetyDistance = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistance, 0, 5);
        TextField safetyDistanceField = new TextField();
        safetyDistanceField.setMaxWidth(nameField.getMaxWidth());
        safetyDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (validateDouble(safetyDistanceField)) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
                projectData.getVehicle(selectedVehicle).setSafetyDistance(Double.parseDouble(safetyDistanceField.getText()));
            }
        });
        GridPane.setConstraints(safetyDistanceField, 1, 5);

        // color combo-box
        Text color = new Text("Color: ");
        GridPane.setConstraints(color, 0, 6);
        ComboBox<String> colorField = new ComboBox<>();
        colorField.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorField.getSelectionModel().selectFirst();
        colorField.setMaxWidth(nameField.getMaxWidth());
        colorField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
            projectData.getVehicle(selectedVehicle).setColor(colorField.getValue());
        });
        GridPane.setConstraints(colorField, 1, 6);

        // initialPose combo-box
        Text initialPose = new Text("Start Location: ");
        GridPane.setConstraints(initialPose, 0, 7);
        ComboBox<String> initialPoseField = new ComboBox<>();
        getPoses(this, initialPoseField);
        initialPoseField.getSelectionModel().selectFirst();
        initialPoseField.setMaxWidth(nameField.getMaxWidth());
        initialPoseField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
            projectData.getVehicle(selectedVehicle).setInitialPose(projectData.getPose(initialPoseField.getValue()));
        });
        GridPane.setConstraints(initialPoseField, 1, 7);

        // missions vbox
        Text mission = new Text("Missions: ");
        GridPane.setConstraints(mission, 0, 8);
        VBox missionField = new VBox();
        ListView<String> missionListView = new ListView<>();
        missionListView.setMaxWidth(nameField.getMaxWidth());
        missionListView.setMaxHeight(110);
        // FIXME
        missionListView.getItems().addAll("(drawPoint29, 2.5)", "(drawPoint30, 5.0)", "(entrance, 0.0)", "(mainTunnelLeft, 4.5)", "(mainTunnelRight, 10.0)");
        HBox missionButtons = new HBox();
        Button addMission = new Button("Add Mission");
        Button deleteMission = new Button("Delete Mission");
        missionButtons.setAlignment(Pos.CENTER);
        missionButtons.setSpacing(10);
        missionButtons.setMaxWidth(nameField.getPrefWidth());
        addMission.setOnAction(e -> {
            AddMissionDialogBox.display("Adding a Mission", projectData.getPoses().keySet(), missionListView);
        });
        deleteMission.setOnAction(e -> {
            missionListView.getItems().remove(missionListView.getSelectionModel().getSelectedItem());
        });
        missionButtons.getChildren().addAll(addMission, deleteMission);
        missionField.setAlignment(Pos.CENTER);
        missionField.setSpacing(5);
        missionField.setMaxWidth(nameField.getMaxWidth());
        missionField.getChildren().addAll(missionListView, missionButtons);
        GridPane.setConstraints(missionField, 1, 8);

        // missionRepeat text-field
        Text missionRepeat = new Text("Mission Repetition: ");
        GridPane.setConstraints(missionRepeat, 0, 9);
        TextField missionRepeatField = new TextField();
        missionRepeatField.setMaxWidth(nameField.getMaxWidth());
        missionRepeatField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (validateInteger(missionRepeatField)) {
                String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
                projectData.getVehicle(selectedVehicle).setMissionRepetition(Integer.parseInt(missionRepeatField.getText()));
            }
        });
        GridPane.setConstraints(missionRepeatField, 1, 9);

        // lookAheadDistance text-field
        Text lookAheadDistance = new Text("Look Ahead Distance (m): ");
        lookAheadDistance.setVisible(false);
        GridPane.setConstraints(lookAheadDistance, 0, 11);
        TextField lookAheadDistanceField = new TextField();
        lookAheadDistanceField.setMaxWidth(nameField.getMaxWidth());
        lookAheadDistanceField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
        if (validateDouble(lookAheadDistanceField)) {
            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
            ((LookAheadVehicle) projectData.getVehicle(selectedVehicle)).setLookAheadDistance(Double.parseDouble(lookAheadDistanceField.getText()));
        }
        });
        GridPane.setConstraints(lookAheadDistanceField, 1, 11);
        lookAheadDistanceField.setVisible(false);

        // isHumanVehicle checkbox
        Text isHuman = new Text("Human Operated: ");
        GridPane.setConstraints(isHuman, 0, 10);
        CheckBox isHumanField = new CheckBox();
        isHumanField.selectedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem();
            if (isHumanField.isSelected()) {
                int ID = projectData.getVehicle(selectedVehicle).getID();
                AutonomousVehicle autonomousVehicle = (AutonomousVehicle) projectData.getVehicle(selectedVehicle);
                LookAheadVehicle lookAheadVehicle = autonomousVehicle.convertToLookAheadVehicle(autonomousVehicle);
                projectData.removeVehicle(ID);
                projectData.addVehicle(lookAheadVehicle);
            }
            else {
                int ID = projectData.getVehicle(selectedVehicle).getID();
                LookAheadVehicle lookAheadVehicle = (LookAheadVehicle) projectData.getVehicle(selectedVehicle);
                AutonomousVehicle autonomousVehicle = lookAheadVehicle.convertToAutonomousVehicle(lookAheadVehicle);
                projectData.removeVehicle(ID);
                projectData.addVehicle(autonomousVehicle);
            }
        });
        GridPane.setConstraints(isHumanField, 1, 10);

        isHumanField.setOnAction( v -> {
            if (isHumanField.isSelected()) {
                lookAheadDistance.setVisible(true);
                lookAheadDistanceField.setVisible(true);
            }
            else {
                lookAheadDistance.setVisible(false);
                lookAheadDistanceField.setVisible(false);
            }
        });

        centerPane.getChildren().addAll(name, nameField,
                length, lengthField,
                width, widthField,
                maxVelocity, maxVelocityField,
                maxAcceleration, maxAccelerationField,
                safetyDistance, safetyDistanceField,
                color, colorField,
                initialPose, initialPoseField,
                mission, missionField,
                missionRepeat, missionRepeatField,
                isHuman, isHumanField,
                lookAheadDistance, lookAheadDistanceField);
        borderPane.setCenter(centerPane);
//        BorderPane.setMargin(centerPane, new Insets(20, 20, 20, 20));

        // Bottom Pane
        borderPane.setBottom(BottomPane.getBottomPane(backVehicleButton, nextVehicleButton));

        // Left Pane
        Button addVehicleButton = new Button("Add Vehicle");
        Button deleteVehicleButton = new Button("Delete Vehicle");

        updateVehiclesListView(vehiclesListView, borderPane, addVehicleButton, deleteVehicleButton, projectData, nameField,
                lengthField, widthField, maxVelocityField, maxAccelerationField, safetyDistanceField, colorField,
                initialPoseField, missionField, isHumanField, lookAheadDistanceField, missionRepeatField);

        addVehicleButton.setOnAction(e -> {
            String baseNameOfVehicle = "vehicle";
            double lengthOfVehicle = 0.9;
            double widthOfVehicle = 0.5;
            double maxVelocityOfVehicle = 10.0;
            double maxAccelerationOfVehicle = 1.0;
            double safetyDistanceOfVehicle = 0.0;
            Color colorOfVehicle = Color.YELLOW;
            Pose initialPoseOfVehicle = projectData.getPose(projectData.getPoses().keySet().iterator().next());
            // ArrayList<String> goalPoseOfVehicle = projectData.getListOfAllPoses().keySet().stream().skip(1).findFirst().orElse(null);

            String nameOfVehicle = baseNameOfVehicle;
            int counter = 1;
            while (vehicleExists(nameOfVehicle, projectData)) {
                // Append a number to the base name
                nameOfVehicle = baseNameOfVehicle + " (" + counter + ")";
                counter++;
            }

            var vehicle = new AutonomousVehicle();
            vehicle.setName(nameOfVehicle);
            vehicle.setLength(lengthOfVehicle);
            vehicle.setWidth(widthOfVehicle);
            vehicle.setMaxVelocity(maxVelocityOfVehicle);
            vehicle.setMaxAcceleration(maxAccelerationOfVehicle);
            vehicle.setSafetyDistance(safetyDistanceOfVehicle);
            vehicle.setColor(colorOfVehicle);
            vehicle.setInitialPose(initialPoseOfVehicle);
            // vehicle.setGoalPoses(goalPoseOfVehicle.split(","));
            vehicle.setMissionRepetition(99);

            projectData.addVehicle(vehicle);
            vehiclesListView.getItems().add(vehicle.getName());
        });

        // Delete vehicle button
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicle = vehiclesListView.getSelectionModel().getSelectedItem(); // Get the selected item
            vehiclesListView.getItems().remove(selectedVehicle); // Remove from ListView
            projectData.removeVehicle(projectData.getVehicle(selectedVehicle).getID()); // Remove from ProjectData
        });

        return new Scene(borderPane, 1440, 800);
    }

    private boolean vehicleExists(String name, ProjectData projectData) {
        return projectData.getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(name));
    }

    private Scene displayMapScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableSaveProject();
        MenuBar.disableNewProject();
        MenuBar.disableOpenProject();

        VBox centerPane = new VBox();

        if (isNewProject) {

            // Center Pane
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

            // Center Pane
            mapData = parseYAML(projectData.getMap());
            ImageView imageView = getImageView(this);

            centerPane.getChildren().add(imageView);

        }
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setSpacing(10);
        borderPane.setCenter(centerPane);

        // Bottom Pane
        borderPane.setBottom(BottomPane.getBottomPane(backMapButton, nextMapButton));

        return new Scene(borderPane, 800, 800);
    }

    /**
     * Creates and returns a scene with a layout for project management.
     *
     * @return A new Scene containing the layout for project management.
     */
    private Scene displayProjectScene() {

        BorderPane borderPane = new BorderPane();
        nextProjectButton.setVisible(false);

        // Top Pane
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
        Button newProject = new Button("New Project");
        Button openProject = new Button("Open Project");
        buttonPane.getChildren().addAll(newProject, openProject);
        buttonPane.setAlignment(Pos.CENTER);

        centerPane.getChildren().addAll(welcomeMessage, buttonPane, pathLabel);
        centerPane.setAlignment(Pos.CENTER);

        // Set VBox children to grow equally
        VBox.setVgrow(buttonPane, Priority.ALWAYS);

        borderPane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        newProject.setOnAction(e -> {
            File selectedFile = fileCreator(this, "Name of Project: ", "json");
            if (selectedFile != null) {
                this.projectFile = selectedFile.getAbsolutePath();
                this.pathLabel.setText("Name of Project: " + selectedFile.getName());
                this.nextProjectButton.setVisible(true);

                // Write to the file
                try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                    fileWriter.write("{}");
                } catch (IOException ex) {
                    pathLabel.setText("Error: Could not save the file.");
                }
            }

            isNewProject = true;
            nextProjectButton.setVisible(true);
        });

        openProject.setOnAction(e -> {
            File file = fileChooser(this, "Select a project file to open: ", "json");
            if (file != null) {
                this.projectFile = file.getAbsolutePath();
                this.pathLabel.setText("Name of Project: " + file.getName());
                this.nextProjectButton.setVisible(true);
                try {
                    this.projectData = parseJSON(this.projectFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            nextProjectButton.setVisible(true);
        });

        // Bottom Pane
        borderPane.setBottom(BottomPane.getBottomPane(nextProjectButton));

        return new Scene(borderPane, 400, 300);
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

