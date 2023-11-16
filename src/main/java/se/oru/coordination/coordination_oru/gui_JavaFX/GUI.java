package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import se.oru.coordination.coordination_oru.gui_JavaFX.ProjectData.Vehicle;

import java.util.ArrayList;
import java.util.Optional;

public class GUI extends Application {

    private final TextField simulationTimeField = new TextField();
    final Stage stage = new Stage();
    final Label pathLabel = new Label("");
    private Boolean isNewProject = false;
    protected String filenameJSON = "";
    final JsonParser jsonParser = new JsonParser();
    protected ProjectData projectData;
    protected MapData mapData;
    private final YamlParser yamlParser = new YamlParser();
    protected final Button nextProjectButton = new Button("Next");
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
            Button browseMapButton = getBrowseButton(stage);
            centerPane.getChildren().addAll(mapMessage, browseMapButton);
            // FIXME

//        mapData = yamlParser.parse(projectData.getMap());
//        ImageView imageView = getImageView(this);

//        centerPane.getChildren().add(imageView);

        }
        else {

            // Center Pane
            mapData = yamlParser.parse(projectData.getMap());
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

    private Scene displayVehicleScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableSaveProject();
        MenuBar.disableNewProject();
        MenuBar.disableOpenProject();

        listViewCentering(vehiclesList);

        // Right Pane
        var rightPane = new VBox();
        ImageView imageView = getImageView(this);
        rightPane.getChildren().addAll(imageView);
        rightPane.setSpacing(10);
        rightPane.setAlignment(Pos.CENTER);
        borderPane.setRight(rightPane);
//        BorderPane.setMargin(rightPane, new Insets(0, 20, 0, 0));

        // Center Pane
        var centerPane = new GridPane();
//        centerPane.setPadding(new Insets(10, 10, 10, 10));
        centerPane.setAlignment(Pos.CENTER);
//        centerPane.setHgap(10);
        centerPane.setVgap(10);

        Text name = new Text("Name of Vehicle: ");
        GridPane.setConstraints(name, 0, 0);
        TextField nameField = new TextField();
        nameField.setEditable(false);
        nameField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(nameField, 1, 0);

        Text length = new Text("Length (m): ");
        GridPane.setConstraints(length, 0, 1);
        TextField lengthField = new TextField();
        lengthField.setEditable(false);
        lengthField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(lengthField, 1, 1);

        Text width = new Text("Width (m): ");
        GridPane.setConstraints(width, 0, 2);
        TextField widthField = new TextField();
        widthField.setEditable(false);
        widthField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(widthField, 1, 2);

        Text maxVelocity = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocity, 0, 3);
        TextField maxVelocityField = new TextField();
        maxVelocityField.setEditable(false);
        maxVelocityField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(maxVelocityField, 1, 3);

        Text maxAcceleration = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAcceleration, 0, 4);
        TextField maxAccelerationField = new TextField();
        maxAccelerationField.setEditable(false);
        maxAccelerationField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(maxAccelerationField, 1, 4);

        Text safetyDistance = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistance, 0, 5);
        TextField safetyDistanceField = new TextField();
        safetyDistanceField.setEditable(false);
        safetyDistanceField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(safetyDistanceField, 1, 5);

        Text color = new Text("Color: ");
        GridPane.setConstraints(color, 0, 6);
        TextField colorField = new TextField();
        colorField.setEditable(false);
        colorField.setPrefWidth(200);
        colorField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(colorField, 1, 6);

        Text initialPose = new Text("Start Location: ");
        GridPane.setConstraints(initialPose, 0, 7);
        TextField initialPoseField = new TextField();
        initialPoseField.setEditable(false);
        getPoses(this, initialPoseField);
        initialPoseField.setPrefWidth(200);
        initialPoseField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(initialPoseField, 1, 7);

        Text goalPose = new Text("Goals: ");
        GridPane.setConstraints(goalPose, 0, 8);
        ListView<String> goalPoseField = new ListView<>();
        goalPoseField.setPrefWidth(200);
        goalPoseField.setPrefHeight(120);
        GridPane.setConstraints(goalPoseField, 1, 8);

        Text isHuman = new Text("Human Operated: ");
        GridPane.setConstraints(isHuman, 0, 9);
        CheckBox isHumanField = new CheckBox();
        isHumanField.setDisable(true);
        GridPane.setValignment(isHumanField, VPos.CENTER);
        GridPane.setHalignment(isHumanField, HPos.CENTER);
        GridPane.setConstraints(isHumanField, 1, 9);

        Text lookAheadDistance = new Text("Look Ahead Distance (m): ");
        lookAheadDistance.setVisible(false);
        GridPane.setConstraints(lookAheadDistance, 0, 10);
        TextField lookAheadDistanceField = new TextField();
        lookAheadDistanceField.setEditable(false);
        lookAheadDistanceField.setAlignment(Pos.CENTER);
        GridPane.setConstraints(lookAheadDistanceField, 1, 10);
        lookAheadDistanceField.setVisible(false);

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
                lookAheadDistance, lookAheadDistanceField);
        borderPane.setCenter(centerPane);
//        BorderPane.setMargin(centerPane, new Insets(20, 20, 20, 20));

        // Bottom Pane
        borderPane.setBottom(BottomPane.getBottomPane(backVehicleButton, nextVehicleButton));

        // Left Pane
        Button addVehicleButton = new Button("Add Vehicle");
        Button deleteVehicleButton = new Button("Delete Vehicle");

        updateVehiclesList(vehiclesList, borderPane, addVehicleButton, deleteVehicleButton, projectData, nameField,
                lengthField, widthField, maxVelocityField, maxAccelerationField, safetyDistanceField, colorField,
                initialPoseField, goalPoseField, isHumanField, lookAheadDistance, lookAheadDistanceField);

//        // isHuman checkbox
//        isHumanField.setOnAction( e -> {
//            if (isHumanField.isSelected()) {
//                System.out.println("Enabled");
//                lookAheadDistance.setVisible(true);
//                lookAheadDistanceField.setVisible(true);
//            }
//            else {
//                System.out.println("Disabled");
//                lookAheadDistance.setVisible(false);
//                lookAheadDistanceField.setVisible(false);
//            }
//        });

        // Add vehicle button
        addVehicleButton.setOnAction(e -> {

            Dialog<Vehicle> dialogBox = new Dialog<>();
            dialogBox.setTitle("Add Vehicle");

            var addVehiclePane = new GridPane();
            addVehiclePane.setAlignment(Pos.CENTER);
            addVehiclePane.setVgap(10);
            addVehiclePane.setPadding(new Insets(10, 10, 10, 10));

            Text nameVehicle = new Text("Name of Vehicle: ");
            GridPane.setConstraints(nameVehicle, 0, 0);
            TextField nameVehicleField = new TextField();
            nameVehicleField.setPromptText("vehicle");
            nameVehicleField.setAlignment(Pos.CENTER);
            GridPane.setConstraints(nameVehicleField, 1, 0);

            Text lengthVehicle = new Text("Length (m): ");
            GridPane.setConstraints(lengthVehicle, 0, 1);
            TextField lengthVehicleField = new TextField();
            lengthVehicleField.setPromptText("0.9");
            lengthVehicleField.setAlignment(Pos.CENTER);
            lengthVehicleField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(lengthVehicleField));
            GridPane.setConstraints(lengthVehicleField, 1, 1);

            Text widthVehicle = new Text("Width (m): ");
            GridPane.setConstraints(widthVehicle, 0, 2);
            TextField widthVehicleField = new TextField();
            widthVehicleField.setPromptText("0.6");
            widthVehicleField.setAlignment(Pos.CENTER);
            widthVehicleField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(widthVehicleField));
            GridPane.setConstraints(widthVehicleField, 1, 2);

            Text maxVelocityVehicle = new Text("Max. Velocity (m/s): ");
            GridPane.setConstraints(maxVelocityVehicle, 0, 3);
            TextField maxVelocityVehicleField = new TextField();
            maxVelocityVehicleField.setPromptText("10.0");
            maxVelocityVehicleField.setAlignment(Pos.CENTER);
            maxVelocityVehicleField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(maxVelocityVehicleField));
            GridPane.setConstraints(maxVelocityVehicleField, 1, 3);

            Text maxAccelerationVehicle = new Text("Max. Acceleration (m/s^2): ");
            GridPane.setConstraints(maxAccelerationVehicle, 0, 4);
            TextField maxAccelerationVehicleField = new TextField();
            maxAccelerationVehicleField.setPromptText("1.0");
            maxAccelerationVehicleField.setAlignment(Pos.CENTER);
            maxAccelerationVehicleField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(maxAccelerationVehicleField));
            GridPane.setConstraints(maxAccelerationVehicleField, 1, 4);

            Text safetyDistanceVehicle = new Text("Safety Distance (m): ");
            GridPane.setConstraints(safetyDistanceVehicle, 0, 5);
            TextField safetyDistanceVehicleField = new TextField();
            safetyDistanceVehicleField.setPromptText("0.5");
            safetyDistanceVehicleField.setAlignment(Pos.CENTER);
            safetyDistanceVehicleField.textProperty().addListener((observable, oldValue, newValue) -> validateDouble(safetyDistanceVehicleField));
            GridPane.setConstraints(safetyDistanceVehicleField, 1, 5);

            Text colorVehicle = new Text("Color: ");
            GridPane.setConstraints(colorVehicle, 0, 6);
            ComboBox<String> colorVehicleField = new ComboBox<>();
            colorVehicleField.getItems().addAll("Yellow", "Red", "Blue", "Green", "Black", "White", "Cyan", "Orange");
            colorVehicleField.getSelectionModel().selectFirst();
            colorVehicleField.setPrefWidth(200);
            listComboBoxCentering(colorVehicleField);
            GridPane.setConstraints(colorVehicleField, 1, 6);

            Text initialPoseVehicle = new Text("Start Location: ");
            GridPane.setConstraints(initialPoseVehicle, 0, 7);
            ComboBox<String> initialPoseVehicleField = new ComboBox<>();
            getPoses(this, initialPoseVehicleField);
            initialPoseVehicleField.getSelectionModel().selectFirst();
            initialPoseVehicleField.setPrefWidth(200);
            listComboBoxCentering(initialPoseVehicleField);
            GridPane.setConstraints(initialPoseVehicleField, 1, 7);

            Text goalPoseVehicle = new Text("Goals: ");
            GridPane.setConstraints(goalPoseVehicle, 0, 8);
            ComboBox<String> goalPoseVehicleField = new ComboBox<>();
            getPoses(this, goalPoseVehicleField);
            goalPoseVehicleField.setValue(new ArrayList<>(projectData.getListOfAllPoses().values()).size() > 1 ?
                    new ArrayList<>(projectData.getListOfAllPoses().keySet()).get(1) : null);
            goalPoseVehicleField.setPrefWidth(200);
            listComboBoxCentering(goalPoseVehicleField);
            GridPane.setConstraints(goalPoseVehicleField, 1, 8);

            Text isHumanVehicle = new Text("Human Operated: ");
            GridPane.setConstraints(isHumanVehicle, 0, 9);
            CheckBox isHumanVehicleField = new CheckBox();
            GridPane.setValignment(isHumanVehicleField, VPos.CENTER);
            GridPane.setHalignment(isHumanVehicleField, HPos.CENTER);
            GridPane.setConstraints(isHumanVehicleField, 1, 9);

            Text lookAheadDistanceVehicle = new Text("Look Ahead Distance (m): ");
            lookAheadDistanceVehicle.setVisible(false);
            GridPane.setConstraints(lookAheadDistanceVehicle, 0, 10);
            TextField lookAheadDistanceVehicleField = new TextField();
            lookAheadDistanceVehicleField.setPromptText("20.0");
            lookAheadDistanceVehicleField.textProperty().addListener((observable, oldValue, newValue) ->
                    validateDouble(lookAheadDistanceVehicleField));
            lookAheadDistanceVehicleField.setAlignment(Pos.CENTER);
            GridPane.setConstraints(lookAheadDistanceVehicleField, 1, 10);
            lookAheadDistanceVehicleField.setVisible(false);

            // isHumanVehicle checkbox
            isHumanVehicleField.setOnAction( v -> {
                if (isHumanVehicleField.isSelected()) {
                    lookAheadDistanceVehicle.setVisible(true);
                    lookAheadDistanceVehicleField.setVisible(true);
                }
                else {
                    lookAheadDistanceVehicle.setVisible(false);
                    lookAheadDistanceVehicleField.setVisible(false);
                }
            });

            addVehiclePane.getChildren().addAll(nameVehicle, nameVehicleField,
                    lengthVehicle, lengthVehicleField,
                    widthVehicle, widthVehicleField,
                    maxVelocityVehicle, maxVelocityVehicleField,
                    maxAccelerationVehicle, maxAccelerationVehicleField,
                    initialPoseVehicle, initialPoseVehicleField,
                    safetyDistanceVehicle, safetyDistanceVehicleField,
                    colorVehicle, colorVehicleField,
                    goalPoseVehicle, goalPoseVehicleField,
                    isHumanVehicle, isHumanVehicleField,
                    lookAheadDistanceVehicle, lookAheadDistanceVehicleField);

            // Create the Add and Cancel buttons
            ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogBox.getDialogPane().getButtonTypes().addAll(addButton, cancelButton);

            // Set the dialogBox's content
            dialogBox.getDialogPane().setContent(addVehiclePane);

            // Handle the result from the dialogBox
            dialogBox.setResultConverter(dialogButton -> {
                if (dialogButton == addButton) {
                    // Collect the data from the input fields and create a new vehicle
                    return getAddedVehicle(isHumanVehicleField, lookAheadDistanceVehicleField,
                            maxVelocityVehicleField, maxAccelerationVehicleField,
                            safetyDistanceVehicleField, colorVehicleField,
                            lengthVehicleField, widthVehicleField, initialPoseVehicleField,
                            goalPoseVehicleField);
                }
                return null;
            });

            // Show the dialogBox and wait for the user's response
            Optional<Vehicle> result = dialogBox.showAndWait();

            // Process the result
            result.ifPresent(vehicle -> {
                projectData.addVehicle(nameVehicleField.getText(), vehicle);
                updateVehiclesList(vehiclesList, borderPane, addVehicleButton, deleteVehicleButton, projectData, nameVehicleField,
                        lengthVehicleField, widthVehicleField, maxVelocityVehicleField, maxAccelerationVehicleField, safetyDistanceVehicleField, colorVehicleField,
                        initialPoseVehicleField, goalPoseVehicleField, isHumanVehicleField, lookAheadDistance,
                        lookAheadDistanceVehicleField);
            });
        });


        // Delete vehicle button
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicle = vehiclesList.getSelectionModel().getSelectedItem(); // Get the selected item
            vehiclesList.getItems().remove(selectedVehicle); // Remove from ListView
            projectData.removeVehicle(selectedVehicle); // Remove from ProjectData
            updateVehiclesList(vehiclesList, borderPane, addVehicleButton, deleteVehicleButton, projectData, nameField,
                    lengthField, widthField, maxVelocityField, maxAccelerationField, safetyDistanceField, colorField,
                    initialPoseField, goalPoseField, isHumanField, lookAheadDistance, lookAheadDistanceField);
        });

        return new Scene(borderPane, 1420, 800);
    }

    private Scene displaySimulationScene() {

        BorderPane borderPane = new BorderPane();

        // Top Pane
        borderPane.setTop(MenuBar.getMenuBar(this));
        MenuBar.disableNewProject();
        MenuBar.disableOpenProject();

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
        borderPane.setBottom(BottomPane.getBottomPane(backSimulationButton, resetSimulationButton,
                saveSimulationButton, runSimulationButton));

        return new Scene(borderPane, 600, 300);
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
            fileJSONCreate(this);
            isNewProject = true;
            nextProjectButton.setVisible(true);
        });
        openProject.setOnAction(e -> {
            fileJSONOpen(this);
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
            runTime, heuristicName, updateCycleTime, resultsDirectory, scaleAdjustment);
        });
    }
}

