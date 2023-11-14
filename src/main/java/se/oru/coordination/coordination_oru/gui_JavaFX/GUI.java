package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.Heuristics;
import se.oru.coordination.coordination_oru.util.JTSDrawingPanelVisualization;
import se.oru.coordination.coordination_oru.util.Missions;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import static se.oru.coordination.coordination_oru.gui_JavaFX.Utils.*;

import java.util.ArrayList;

public class GUI extends Application {

    private final TextField simulationTimeField = new TextField();
    final Stage stage = new Stage();
    final Label pathLabel = new Label("");
    String filenameJSON = "";
    final JsonParser jsonParser = new JsonParser();
    protected ProjectData projectData;
    protected MapData mapData;
    private final YamlParser yamlParser = new YamlParser();
    final Button nextProjectButton = new Button("Next");
    private final Button backMapButton = new Button("Back");
    private final Button nextMapButton = new Button("Next");
    private final Button nextVehicleButton = new Button("Next");
    private final Button backVehicleButton = new Button("Back");
    private final Button runSimulationButton = new Button("Run");
    private final Button saveSimulationButton = new Button("Save");
    private final Button resetSimulationButton =  new Button("Reset");
    private final Button backSimulationButton = new Button("Back");
    private final ListView<String> vehiclesList = new ListView<>();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Scene projectScene = displayProjectScene();
        stage.setTitle("Coordination_ORU");
        stage.setScene(projectScene);
        stage.show();

        nextProjectButton.setOnAction( e -> {
            stage.setTitle("Coordination_ORU: Setting the map");
            stage.setScene(displayOpenMapScene());
            stage.centerOnScreen();
        });

        nextMapButton.setOnAction( e -> {
            stage.setTitle("Coordination_ORU: Setting the vehicles");
            stage.setScene(displayVehicleScene());
            stage.centerOnScreen();
        });

        backMapButton.setOnAction( e -> {
            // FIXME Centering Issue
            stage.setTitle("Coordination_ORU");
            stage.setScene(displayProjectScene());
            stage.centerOnScreen();
            nextProjectButton.setVisible(true);
        });

        nextVehicleButton.setOnAction( e -> {
            stage.setTitle("Coordination_ORU: Setting the simulation");
            stage.setScene(displaySimulationScene());
            stage.centerOnScreen();
        });

        backVehicleButton.setOnAction( e -> {
            // FIXME Centering Issue
            stage.setTitle("Coordination_ORU: Setting the map");
            stage.setScene(displayOpenMapScene());
            stage.centerOnScreen();
        });

        backSimulationButton.setOnAction( e -> {
            stage.setTitle("Coordination_ORU: Setting the vehicles");
            stage.setScene(displayVehicleScene());
            stage.centerOnScreen();
        });

        saveSimulationButton.setOnAction( e -> System.out.println("File saved"));

        runSimulationButton.setOnAction( e -> {
            runSimulation();
        });

        resetSimulationButton.setOnAction( e -> {
            stage.setTitle("Coordination_ORU");
            stage.setScene(displayProjectScene());
            stage.centerOnScreen();
        });

    }

    private Scene displayProjectScene() {

        // Visual
        BorderPane borderPane = new BorderPane();

        nextProjectButton.setVisible(false);

        // Center Pane
        VBox centerPane = new VBox();
        centerPane.setSpacing(40);

        Text welcomeMessage = new Text("Welcome to Coordination_ORU!");

        // Center - Button Pane
        HBox buttonPane = new HBox();
        buttonPane.setSpacing(40);
        Button createProject = new Button("Create Project");
        Button openProject = new Button("Open Project");
        buttonPane.getChildren().addAll(createProject, openProject); // Add buttons to HBox
        buttonPane.setAlignment(Pos.CENTER); // Center alignment for all children of HBox

        centerPane.getChildren().addAll(welcomeMessage, buttonPane, pathLabel, nextProjectButton); // Add HBox (with buttons) to VBox below the text
        centerPane.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        // Working

        // Create Button
        createProject.setOnAction(e -> fileJSONCreate(this));

        // Open Button
        openProject.setOnAction(e -> fileJSONOpen(this));

        return new Scene(borderPane, 400, 300);
    }

    private Scene displayOpenMapScene() {

        // Visual
        BorderPane borderPane = new BorderPane();

        // Center Pane
        VBox centerPane = new VBox();

        mapData = yamlParser.parse(projectData.getMap());
        ImageView imageView = getImageView(this);

//        Button changeMapButton = new Button("Change Map");

        centerPane.getChildren().add(imageView);
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setSpacing(10);
        borderPane.setCenter(centerPane);

        // Bottom Pane
        var bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setSpacing(350);
        bottomPane.getChildren().addAll(backMapButton, nextMapButton);
        borderPane.setBottom(bottomPane);
        BorderPane.setMargin(bottomPane, new Insets(0, 0, 20, 0)); // 20px top spacing

        // Working
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
//
//        changeMapButton.setOnAction(e -> {
//            fileChooser.setTitle("Choose Map File");
//            fileChooser.getExtensionFilters().clear();
//            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml"));
//            File file = fileChooser.showOpenDialog(stage);
//
//            if (file != null) {
                // TODO Update projectData
                // TODO Show the Updated map
//                filenameYAML = file.getAbsolutePath();
//                yamlData = yamlParser.parse(filenameYAML);
//                projectData.setMap(filenameYAML);
//                ImageView imageViewNew = getImageView();
//                    pathLabel.setText("File opened: " + filenameYAML);
//                    projectData = jsonParser.parse(filenameYAML);
//            }
//        });

        return new Scene(borderPane, 800, 800);
    }

    private Scene displayVehicleScene() {

        // Visual
        BorderPane borderPane = new BorderPane();
        listViewCentering(vehiclesList);

        // Right Pane
        var rightPane = new VBox();
        ImageView imageView = getImageView(this);
        rightPane.getChildren().addAll(imageView);
        rightPane.setSpacing(10);
        rightPane.setAlignment(Pos.CENTER);
        borderPane.setRight(rightPane);
        BorderPane.setMargin(rightPane, new Insets(0, 20, 0, 0));

        // Center Pane
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setHgap(10);
        centerPane.setVgap(10);

        Text name = new Text("Name of Vehicle: ");
        GridPane.setConstraints(name, 0, 0);
        TextField nameField = new TextField();
        nameField.setPromptText("vehicle");
        nameField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(nameField, 1, 0);

        Text length = new Text("Length (m): ");
        GridPane.setConstraints(length, 0, 1);
        TextField lengthField = new TextField();
        lengthField.setPromptText("0.9");
        lengthField.setAlignment(Pos.CENTER);
        lengthField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(lengthField));
        GridPane.setConstraints(lengthField, 1, 1);

        Text width = new Text("Width (m): ");
        GridPane.setConstraints(width, 0, 2);
        TextField widthField = new TextField();
        widthField.setPromptText("0.6");
        widthField.setAlignment(Pos.CENTER);
        widthField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(widthField));
        GridPane.setConstraints(widthField, 1, 2);

        Text maxVelocity = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocity, 0, 3);
        TextField maxVelocityField = new TextField();
        maxVelocityField.setPromptText("10.0");
        maxVelocityField.setAlignment(Pos.CENTER);
        maxVelocityField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(maxVelocityField));
        GridPane.setConstraints(maxVelocityField, 1, 3);

        Text maxAcceleration = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAcceleration, 0, 4);
        TextField maxAccelerationField = new TextField();
        maxAccelerationField.setPromptText("1.0");
        maxAccelerationField.setAlignment(Pos.CENTER);
        maxAccelerationField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(maxAccelerationField));
        GridPane.setConstraints(maxAccelerationField, 1, 4);

        Text safetyDistance = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistance, 0, 5);
        TextField safetyDistanceField = new TextField();
        safetyDistanceField.setPromptText("0.5");
        safetyDistanceField.setAlignment(Pos.CENTER);
        safetyDistanceField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(safetyDistanceField));
        GridPane.setConstraints(safetyDistanceField, 1, 5);

        Text color = new Text("Color: ");  // TODO Centering Issue
        GridPane.setConstraints(color, 0, 6);
        ComboBox<String> colorField = new ComboBox<>();
        colorField.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
        colorField.getSelectionModel().selectFirst();
        colorField.setPrefWidth(200);
        listComboBoxCentering(colorField);
        GridPane.setConstraints(colorField, 1, 6);

        Text initialPose = new Text("Initial Pose: ");
        GridPane.setConstraints(initialPose, 0, 7);
        ComboBox<String> initialPoseField = new ComboBox<>();
        getPoses(this, initialPoseField);
        initialPoseField.getSelectionModel().selectFirst();
        initialPoseField.setPrefWidth(200);
        listComboBoxCentering(initialPoseField);
        GridPane.setConstraints(initialPoseField, 1, 7);

        Text goalPose = new Text("Goal Pose: ");
        GridPane.setConstraints(goalPose, 0, 8);
        ComboBox<String> goalPoseField = new ComboBox<>();
        getPoses(this, goalPoseField);
        goalPoseField.setValue(new ArrayList<>(projectData.getListOfAllPoses().values()).size() > 1 ?
                new ArrayList<>(projectData.getListOfAllPoses().keySet()).get(1) : null);
        goalPoseField.setPrefWidth(200);
        listComboBoxCentering(goalPoseField);
        GridPane.setConstraints(goalPoseField, 1, 8);

        Text isHuman = new Text("Human Operated: ");
        GridPane.setConstraints(isHuman, 0, 9);
        CheckBox isHumanField = new CheckBox();
        GridPane.setValignment(isHumanField, VPos.CENTER);
        GridPane.setHalignment(isHumanField, HPos.CENTER);
        GridPane.setConstraints(isHumanField, 1, 9);

        Text lookAheadDistance = new Text("Look Ahead Distance (m): ");
        lookAheadDistance.setVisible(false);
        GridPane.setConstraints(lookAheadDistance, 0, 10);
        TextField lookAheadDistanceField = new TextField();
        lookAheadDistanceField.setPromptText("20.0");
        lookAheadDistanceField.textProperty().addListener((observable, oldValue, newValue) ->
                validateDouble(lookAheadDistanceField));
        lookAheadDistanceField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(lookAheadDistanceField, 1, 10);
        lookAheadDistanceField.setVisible(false);

        Button addVehicleButton = new Button("Add Vehicle");
        GridPane.setConstraints(addVehicleButton, 1, 11);

        centerPane.getChildren().addAll(name, nameField,
                length, lengthField,
                width, widthField,
                maxVelocity, maxVelocityField,
                maxAcceleration, maxAccelerationField,
                initialPose, initialPoseField,
                safetyDistance, safetyDistanceField,
                color, colorField,
                goalPose, goalPoseField,
                isHuman, isHumanField,
                lookAheadDistance, lookAheadDistanceField,
                addVehicleButton);
        borderPane.setCenter(centerPane);
        BorderPane.setMargin(centerPane, new Insets(20, 0, 0, 0)); // 20px top spacing

        // Bottom Pane
        var bottomPane = new HBox();
        bottomPane.setAlignment(Pos.BOTTOM_LEFT);
        bottomPane.getChildren().addAll(backVehicleButton, nextVehicleButton);
        bottomPane.setSpacing(500);
        borderPane.setBottom(bottomPane);
        BorderPane.setMargin(bottomPane, new Insets(0, 0, 20, 350)); // 20px top spacing

        // Left Pane
        Button deleteVehicleButton = new Button("Delete Vehicle");

        updateVehiclesList(vehiclesList, borderPane, deleteVehicleButton, projectData, nameField, lengthField, widthField,
                maxVelocityField, maxAccelerationField, safetyDistanceField, colorField, initialPoseField,
                goalPoseField, isHumanField, lookAheadDistanceField);

        // Working

        // isHuman checkbox
        isHumanField.setOnAction( e -> {
            if (isHumanField.isSelected()) {
                lookAheadDistance.setVisible(true);
                lookAheadDistanceField.setVisible(true);
            }
            else {
                lookAheadDistance.setVisible(false);
                lookAheadDistanceField.setVisible(false);
            }
        });

        // Add vehicle button
        addVehicleButton.setOnAction( e -> {
            var vehicle = getAddedVehicle(isHumanField, lookAheadDistanceField, maxVelocityField, maxAccelerationField,
                    safetyDistanceField, colorField, lengthField, widthField, initialPoseField, goalPoseField);
            projectData.addVehicle(nameField.getText(), vehicle);
            updateVehiclesList(vehiclesList, borderPane, deleteVehicleButton, projectData, nameField, lengthField, widthField,
                    maxVelocityField, maxAccelerationField, safetyDistanceField, colorField, initialPoseField,
                    goalPoseField, isHumanField, lookAheadDistanceField);
        });

        // Delete vehicle button
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicle = vehiclesList.getSelectionModel().getSelectedItem(); // Get the selected item
            vehiclesList.getItems().remove(selectedVehicle); // Remove from ListView
            projectData.removeVehicle(selectedVehicle); // Remove from ProjectData
            updateVehiclesList(vehiclesList, borderPane, deleteVehicleButton, projectData, nameField, lengthField, widthField,
                    maxVelocityField, maxAccelerationField, safetyDistanceField, colorField, initialPoseField,
                    goalPoseField, isHumanField, lookAheadDistanceField);
        });

        return new Scene(borderPane, 1420, 800);
    }

    private Scene displaySimulationScene() {

        // Visual
        BorderPane borderPane = new BorderPane();

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
        Button btnBrowse = getBrowseButton(stage);
        GridPane.setConstraints(btnBrowse, 1, 4);

        settingsPane.getChildren().addAll(heuristics, heuristicsField, simulationTime,
                simulationTimeField, noOfRuns, noOfRunsField, reportsSaving, reportsSavingField,
                reportsFolder, btnBrowse);

        centerPane.setAlignment(Pos.CENTER);
        centerPane.getChildren().add(settingsPane);
        borderPane.setCenter(centerPane);
        BorderPane.setMargin(centerPane, new Insets(10, 0, 0, 0));

        // Bottom Pane
        var bottomPane = new HBox();
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().addAll(backSimulationButton, runSimulationButton, saveSimulationButton,
                resetSimulationButton);
        bottomPane.setSpacing(50);
        borderPane.setBottom(bottomPane);
        BorderPane.setMargin(bottomPane, new Insets(0, 0, 20, 0)); // 20px top spacing

        // Working

        return new Scene(borderPane, 600, 300);
    }

    private void runSimulation() {

        int runTime = 10; // FIXME Auto Load
        String resultsDirectory = System.getProperty("user.dir"); // FIXME Hard Coded
        final String YAML_FILE = projectData.getMap();
        double lookAheadDistance = 6;
        double timeIntervalInSeconds = 0.25;
        int updateCycleTime = 100;
        int numOfCallsForLookAheadRobot = 5;
        boolean visualization = true;
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
        if (visualization) {
            var viz = new JTSDrawingPanelVisualization();
            viz.setMap(YAML_FILE); // TODO Fix Zooming of the Map
            viz.setSize(1920, 1200);
//            viz.setFontScale(2.5);
//            viz.setInitialTransform(8.6, 30.2, -0.73);
            tec.setVisualization(viz);
        }

        projectData.getVehicles().forEach((key, vehicle) -> {

            AbstractVehicle newVehicle;
            if (vehicle.getType().equals("Autonomous")) {
                newVehicle = new AutonomousVehicle();
            } else {
                newVehicle = new LookAheadVehicle();
                ((LookAheadVehicle) newVehicle).setLookAheadDistance(lookAheadDistance);
            }

        newVehicle.setName(key);
        newVehicle.setMaxVelocity(vehicle.getMaxVelocity());
        newVehicle.setMaxAcceleration(vehicle.getMaxAcceleration());
        newVehicle.setSafetyDistance(vehicle.getSafetyDistance());
        newVehicle.setColor(stringToColor(vehicle.getColor()));
        newVehicle.setLength(vehicle.getLength());
        newVehicle.setWidth(vehicle.getWidth());
        newVehicle.setInitialPose(getPosesByName(this, vehicle.getInitialPose())[0]);
        newVehicle.setGoalPoses(getPosesByName(this, vehicle.getGoalPoses()));

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
            runTime, heuristicName, updateCycleTime, resultsDirectory, mapData.getResolution());
        });
    }
}

