package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.util.*;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GUI extends Application {

    private final TextField simulationTimeField = new TextField();
    private final Stage stage = new Stage();
    private final Label pathLabel = new Label("");  // To display the file path
    private String filenameJSON = "";
    private String filenameYAML = "";
    private final JsonParser jsonParser = new JsonParser();
    private ProjectData projectData;
    private YamlData yamlData;
    private final YamlParser yamlParser = new YamlParser();
    private final Button nextProjectButton = new Button("Next");
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
            stage.setScene(displayMapScene());
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
            stage.setScene(displayMapScene());
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Create Button
        createProject.setOnAction(e -> {

            fileChooser.setInitialFileName("Choose a folder and name of the project file: ");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Ensure the file has .json extension
                if (!file.getName().endsWith(".json")) {
                    filenameJSON = file.getAbsolutePath() + ".json";
                    file = new File(filenameJSON);
                }

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("{}"); // Save empty JSON or default data
                    pathLabel.setText("File created: " + file.getAbsolutePath());
                    nextProjectButton.setVisible(true);
                } catch (IOException ex) {
                    pathLabel.setText("Error: Could not save the file.");
                }
            }
        });

        // Open Button
        openProject.setOnAction(e -> {

            fileChooser.setTitle("Select a project file to open: ");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath())); // TODO Check if required
                    filenameJSON = file.getAbsolutePath();
                    pathLabel.setText("File opened: " + filenameJSON);
                    nextProjectButton.setVisible(true);
                    projectData = jsonParser.parse(filenameJSON);
                } catch (IOException ex) {
                    DialogBox.display("Error", "Could not read the file.");
                }
            }
        });

        return new Scene(borderPane, 400, 300);
    }

    private Scene displayMapScene() {

        // Visual
        BorderPane borderPane = new BorderPane();

        // Center Pane
        VBox centerPane = new VBox();

        yamlData = yamlParser.parse(projectData.getMap());
        ImageView imageView = getImageView();

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

        // Left Pane
        Button deleteVehicleButton = new Button("Delete Vehicle");
        updateVehiclesList(borderPane, deleteVehicleButton);

        // Right Pane
        var rightPane = new VBox();
        ImageView imageView = getImageView();
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
        GridPane.setConstraints(name, 0, 1);
        TextField nameField = new TextField();   // TODO Make Choicebox width same as TextField
        GridPane.setConstraints(nameField, 1, 1);
        Text maxVelocity = new Text("Max. Velocity (m/s): ");
        GridPane.setConstraints(maxVelocity, 0, 2);
        TextField maxVelocityField = new TextField();
        GridPane.setConstraints(maxVelocityField, 1, 2);
        Text maxAcceleration = new Text("Max. Acceleration (m/s^2): ");
        GridPane.setConstraints(maxAcceleration, 0, 3);
        TextField maxAccelerationField = new TextField();
        GridPane.setConstraints(maxAccelerationField, 1, 3);
        Text trackingPeriod = new Text("Tracking Period (ms): ");
        GridPane.setConstraints(trackingPeriod, 0, 4);
        TextField trackingPeriodField = new TextField();
        GridPane.setConstraints(trackingPeriodField, 1, 4);
        Text safetyDistance = new Text("Safety Distance (m): ");
        GridPane.setConstraints(safetyDistance, 0, 5);
        TextField safetyDistanceField = new TextField();
        GridPane.setConstraints(safetyDistanceField, 1, 5);
        Text color = new Text("Color: ");
        GridPane.setConstraints(color, 0, 6);
        ChoiceBox<String> colorField = new ChoiceBox<>();
        colorField.getItems().addAll("Red", "Blue", "Yellow", "Green"); // FIXME Color not as a String
        colorField.setValue("Yellow"); // FIXME Get first value
        GridPane.setConstraints(colorField, 1, 6);
        Text initialPose = new Text("Initial Pose: ");
        GridPane.setConstraints(initialPose, 0, 7);
        ChoiceBox<String> initialPoseField = new ChoiceBox<>();
        getPoses(initialPoseField);
        initialPoseField.setValue(projectData.getListOfAllPoses().keySet().iterator().next());
        GridPane.setConstraints(initialPoseField, 1, 7);
        Text goalPose = new Text("Goal Pose: ");
        GridPane.setConstraints(goalPose, 0, 8);
        ChoiceBox<String> goalPoseField = new ChoiceBox<>();
        getPoses(goalPoseField);
        goalPoseField.setValue(new ArrayList<>(projectData.getListOfAllPoses().values()).size() > 1 ?
                new ArrayList<>(projectData.getListOfAllPoses().keySet()).get(1) : null);
        GridPane.setConstraints(goalPoseField, 1, 8);
        Text isHuman = new Text("Human Operated: ");
        GridPane.setConstraints(isHuman, 0, 9);
        CheckBox isHumanField = new CheckBox();
        GridPane.setConstraints(isHumanField, 1, 9);
        Text lookAheadDistance = new Text("Look Ahead Distance (m): ");
        lookAheadDistance.setVisible(false);
        GridPane.setConstraints(lookAheadDistance, 0, 10);
        TextField lookAheadDistanceField = new TextField();
        GridPane.setConstraints(lookAheadDistanceField, 1, 10);
        lookAheadDistanceField.setVisible(false);
        Button addVehicleButton = new Button("Add Vehicle");
        GridPane.setConstraints(addVehicleButton, 1, 11);

        centerPane.getChildren().addAll(name, nameField, maxVelocity, maxVelocityField, maxAcceleration,
                maxAccelerationField, trackingPeriod, trackingPeriodField, initialPose, initialPoseField,
                safetyDistance, safetyDistanceField, color, colorField, goalPose, goalPoseField,
                isHuman, isHumanField, lookAheadDistance, lookAheadDistanceField, addVehicleButton);
        borderPane.setCenter(centerPane);
        BorderPane.setMargin(centerPane, new Insets(20, 0, 0, 0)); // 20px top spacing

        // Bottom Pane
        var bottomPane = new HBox();
        bottomPane.setAlignment(Pos.BOTTOM_LEFT);
        bottomPane.getChildren().addAll(backVehicleButton, nextVehicleButton);
        bottomPane.setSpacing(500);
        borderPane.setBottom(bottomPane);
        BorderPane.setMargin(bottomPane, new Insets(0, 0, 20, 350)); // 20px top spacing

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
            var vehicle = new Vehicle();
            String vehicleName = nameField.getText();
            vehicle.setMaxVelocity(Double.parseDouble(maxVelocityField.getText()));
            vehicle.setMaxAcceleration(Double.parseDouble(maxAccelerationField.getText()));
            vehicle.setTrackingPeriod(Integer.parseInt(trackingPeriodField.getText()));
            vehicle.setSafetyDistance(Double.parseDouble(safetyDistanceField.getText()));
            vehicle.setColor(colorField.getValue());
            if (isHumanField.isSelected()) {
                vehicle.setType("Human");
                vehicle.setLookAheadDistance(Double.parseDouble(lookAheadDistanceField.getText()));
            } else {
                vehicle.setType("Autonomous");
                vehicle.setLookAheadDistance(0.0);
            }
            vehicle.setInitialPose(initialPoseField.getValue());
            vehicle.setGoalPoses(goalPoseField.getValue());
            projectData.addVehicle(vehicleName, vehicle);
            updateVehiclesList(borderPane, deleteVehicleButton);
        });

        // Delete vehicle button
        deleteVehicleButton.setOnAction(e -> {
            String selectedVehicle = vehiclesList.getSelectionModel().getSelectedItem(); // Get the selected item
            vehiclesList.getItems().remove(selectedVehicle); // Remove from ListView
            projectData.removeVehicle(selectedVehicle); // Remove from ProjectData's map
            updateVehiclesList(borderPane, deleteVehicleButton); // Update your ListView display
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

        Text heuristics = new Text("Heuristics: ");
        GridPane.setConstraints(heuristics, 0, 0);
        ChoiceBox<String> heuristicsField = new ChoiceBox<>();
        heuristicsField.getItems().addAll("Closest First", "Human First", "Autonomous First");
        heuristicsField.setValue("Closest First");
        GridPane.setConstraints(heuristicsField, 1, 0);

        Text noOfRuns = new Text("No. of Runs: ");
        GridPane.setConstraints(noOfRuns, 0, 1);
        TextField noOfRunsField = new TextField();
        GridPane.setConstraints(noOfRunsField, 1, 1);

        Text simulationTime = new Text("Simulation Time (minutes): ");
        GridPane.setConstraints(simulationTime, 0, 2);
        GridPane.setConstraints(simulationTimeField, 1, 2);

        Text reportsSaving = new Text("Saving Vehicles Reports: ");
        GridPane.setConstraints(reportsSaving, 0, 3);
        CheckBox reportsSavingField = new CheckBox();
        reportsSavingField.setSelected(true);
        GridPane.setConstraints(reportsSavingField, 1, 3);

        Text reportsFolder = new Text("Folder to Save the Reports: ");
        GridPane.setConstraints(reportsFolder, 0, 4);
        Button btnBrowse = getBrowseButton();
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

    private Button getBrowseButton() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Save the Vehicle Reports: ");

        // Create the Browse button
        Button btnBrowse = new Button("Browse");

        // Set the action to be performed when the Browse button is clicked
        btnBrowse.setOnAction(e -> {
            // Show the directory chooser and get the selected directory
            File selectedDirectory = directoryChooser.showDialog(stage);

            // Check if a directory is selected
            if (selectedDirectory != null) {
                // Do something with the selected directory
                // For example, print the path to the console or use it in your application
                System.out.println("Folder selected: " + selectedDirectory.getAbsolutePath());
            }
        });
        return btnBrowse;
    }

    private void updateVehiclesList(BorderPane borderPane, Button deleteVehicleButton) {
        VBox leftPane = new VBox();
        Text vehiclesText = new Text("Vehicles: ");
        getVehicles(vehiclesList);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getChildren().addAll(vehiclesText, vehiclesList, deleteVehicleButton);
        leftPane.setSpacing(10);
        vehiclesList.getSelectionModel().select(0);
        borderPane.setLeft(leftPane);
        BorderPane.setMargin(leftPane, new Insets(0, 0, 0, 20)); // 20px top spacing
    }

    private ImageView getImageView() {
        String imageFile = yamlData.getImage();
        Image mapImage = new Image("file:" + imageFile); // FIXME Path problem

        // Set the preferred dimensions for the image
        double preferredWidth = 800; // you can set this value to whatever width you want
        double preferredHeight = 640; // you can set this value to whatever height you want
        ImageView imageView = new ImageView(mapImage);
        imageView.setFitWidth(preferredWidth);
        imageView.setFitHeight(preferredHeight);
        imageView.setPreserveRatio(true); // This will keep the image's aspect ratio
        return imageView;
    }

    private void getPoses(ChoiceBox<String> Poses) {
        for (String pose : projectData.getListOfAllPoses().keySet()) {
            Poses.getItems().add(pose);
        }
    }

    private void getVehicles(ListView<String> vehicles) {
        vehicles.getItems().clear();
        for (String vehicle : projectData.getVehicles().keySet()) {
            vehicles.getItems().add(vehicle);
        }
    }
    private void runSimulation() {

        int runTime = 10; // FIXME Auto Load
        String resultsDirectory = System.getProperty("user.dir"); // FIXME Hard Coded
        final String YAML_FILE = "maps/mine-map-paper-2023.yaml";  // FIXME Hard Coded
        double lookAheadDistance = 6;
        double timeIntervalInSeconds = 0.25;
        int updateCycleTime = 100;
        int numOfCallsForLookAheadRobot = 5;
        boolean visualization = true;
        boolean writeRobotReports = false;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(1000, 10000, 14, 3);
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        var heuristic = new Heuristics();
        tec.addComparator(heuristic.closest());
        String heuristicName = heuristic.getHeuristicName();

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        if (visualization) {
            var viz = new JTSDrawingPanelVisualization();
            viz.setMap(YAML_FILE);
            viz.setSize(1920, 1200);
//            viz.setFontScale(2.5);
//            viz.setInitialTransform(8.6, 30.2, -0.73);
            tec.setVisualization(viz);
        }

        for (Vehicle vehicle : projectData.getVehicles().values()) {
            AbstractVehicle vehicle1;
            if (vehicle.getType().equals("Autonomous")) {
                vehicle1 = new AutonomousVehicle(
                        1,
                        stringToColor(vehicle.getColor()),
                        vehicle.getMaxVelocity(),
                        vehicle.getMaxAcceleration(),
                        vehicle.getTrackingPeriod(),
                        0.9,
                        0.5,
                        getPoseByName(vehicle.getInitialPose()),
                        getPoseArrayByNames(vehicle.getGoalPoses()),
                        0
                );
            }
            else {
                vehicle1 = new LookAheadVehicle(
                        1,
                        vehicle.getLookAheadDistance(),
                        stringToColor(vehicle.getColor()),
                        vehicle.getMaxVelocity(),
                        vehicle.getMaxAcceleration(),
                        vehicle.getTrackingPeriod(),
                        0.9,
                        0.5,
                        getPoseByName(vehicle.getInitialPose()),
                        getPoseArrayByNames(vehicle.getGoalPoses()),
                        0
                );
            }

            vehicle1.getPlan(vehicle1.getInitialPose(),
                    vehicle1.getGoalPoses(), YAML_FILE, true);

            tec.setForwardModel(vehicle1.getID(), new ConstantAccelerationForwardModel(vehicle1.getMaxAcceleration(),
                    vehicle1.getMaxVelocity(), tec.getTemporalResolution(), tec.getControlPeriod(),
                    tec.getRobotTrackingPeriodInMillis(vehicle1.getID())));
            tec.setDefaultFootprint(vehicle1.getFootprint());

            tec.placeRobot(vehicle1.getID(), getPoseByName(vehicle.getInitialPose()));

            var mission = new Mission(vehicle1.getID(), vehicle1.getPath());
            Missions.enqueueMission(mission);
            Missions.setMap(YAML_FILE);
        }

        Missions.startMissionDispatchers(tec, writeRobotReports, timeIntervalInSeconds,
                runTime, heuristicName, updateCycleTime, resultsDirectory, 1);
    }

    private static final Map<String, Pose> poses = createPosesMap();

    private static Map<String, Pose> createPosesMap() {
        Map<String, Pose> poses = new HashMap<>();
        // Initialize static map with predefined Pose objects
        poses.put("mainTunnelLeft", new Pose(14.25, 22.15, Math.PI));
        poses.put("mainTunnelRight", new Pose(114.15, 40.05, Math.PI));
        poses.put("entrance", new Pose(115.35, 3.75, Math.PI));
        poses.put("drawPoint12", new Pose(88.35, 101.05, -Math.PI / 2));
        poses.put("drawPoint13", new Pose(95.75, 100.85, Math.PI));
        poses.put("drawPoint14", new Pose(102.45, 98.05, Math.PI));
        poses.put("drawPoint27", new Pose(17.95, 54.35, Math.PI));
        poses.put("drawPoint28", new Pose(25.05, 58.35, -Math.PI / 2));
        poses.put("drawPoint29", new Pose(31.95, 58.75, Math.PI));
        poses.put("drawPoint29A", new Pose(39.35, 54.15, Math.PI));
        poses.put("drawPoint30", new Pose(46.25, 49.85, -Math.PI / 2));
        poses.put("drawPoint31", new Pose(53.25, 49.25, -Math.PI / 2));
        poses.put("drawPoint32", new Pose(60.35, 53.05, -Math.PI / 2));
        poses.put("drawPoint32A", new Pose(67.55, 55.45, -Math.PI / 2));
        poses.put("drawPoint33", new Pose(74.25, 73.45, -Math.PI / 2));
        poses.put("drawPoint34", new Pose(81.35, 79.45, -Math.PI / 2));
        poses.put("drawPoint35", new Pose(88.45, 81.95, -Math.PI / 2));
        poses.put("orePass1", new Pose(28.45, 15.05, -Math.PI / 2));
        poses.put("orePass2", new Pose(76.35, 31.05, -Math.PI / 2.7));
        poses.put("orePass3", new Pose(92.65, 33.15, -Math.PI / 2));
        return poses;
    }

    public static Pose getPoseByName(String poseName) {
        // Return the requested pose, or null if not found
        return poses.get(poseName);
    }

    public static Pose[] getPoseArrayByNames(String poseNames) {
        String[] names = poseNames.split(","); // Assuming the names are separated by commas
        ArrayList<Pose> poseList = new ArrayList<>();

        for (String name : names) {
            name = name.trim(); // Remove any leading/trailing whitespace
            Pose pose = poses.get(name);
            if (pose != null) {
                poseList.add(pose);
            }
        }

        return poseList.toArray(new Pose[0]); // Convert the List to an array and return
    }

    public static Color stringToColor(String colorStr) {
        if (colorStr == null) {
            return Color.BLACK; // Default color or null, depending on your preference
        }

        // Convert the string to uppercase to match the enum constant naming convention
        String colorUpper = colorStr.toUpperCase();

        // Match the string to the corresponding color constant
        switch (colorUpper) {
            case "YELLOW":
                return Color.YELLOW;
            case "RED":
                return Color.RED;
            case "BLUE":
                return Color.BLUE;
            case "GREEN":
                return Color.GREEN;
            case "BLACK":
                return Color.BLACK;
            case "WHITE":
                return Color.WHITE;
            // Add more cases for other colors as needed
            default:
                throw new IllegalArgumentException("Unknown color: " + colorStr);
        }
    }

}

