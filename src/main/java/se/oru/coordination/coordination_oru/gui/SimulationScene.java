package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.io.File;
import java.io.IOException;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class SimulationScene {
    private final se.oru.coordination.coordination_oru.gui.GUI GUI;

    public SimulationScene(se.oru.coordination.coordination_oru.gui.GUI GUI) {
        this.GUI = GUI;
    }

    public Scene get() {

        var root = new BorderPane();

        // Top Pane - Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(GUI));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();

        // Center Pane
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(30, 30, 30, 30));
        BorderPane.setMargin(centerPane, new Insets(30, 30, 30, 30));
        centerPane.setHgap(10);
        centerPane.setVgap(10);
        centerPane.setAlignment(Pos.CENTER);
        root.setCenter(centerPane);

        // heuristics choice-box
        Text heuristicsText = new Text("Heuristics: ");
        GridPane.setConstraints(heuristicsText, 0, 0);
        ChoiceBox<String> heuristicsChoiceBox = new ChoiceBox<String>();
        heuristicsChoiceBox.setPrefWidth(220);
        GridPane.setConstraints(heuristicsChoiceBox, 1, 0);
        heuristicsChoiceBox.getItems().addAll(Heuristics.getAllHeuristicNames());
        heuristicsChoiceBox.setValue(heuristicsChoiceBox.getItems().stream().findFirst().orElse(null));
        heuristicsChoiceBox.setOnAction(e -> {
            String selectedHeuristic = heuristicsChoiceBox.getValue();
            if (selectedHeuristic != null) {
                switch (selectedHeuristic) {
                    case "MOST_DISTANCE_TRAVELLED":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TRAVELLED));
                        break;
                    case "MOST_DISTANCE_TO_TRAVEL":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TO_TRAVEL));
                        break;
                    case "RANDOM":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.RANDOM));
                        break;
                    case "HIGHEST_PRIORITY_FIRST":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST));
                        break;
                    case "HUMAN_FIRST":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST));
                        break;
                    case "AUTONOMOUS_FIRST":
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST));
                        break;
                    default:
                        GUI.getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST));
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
                Boolean validated = Utils.validateInteger(simulationTimeTextField);
                if (validated) {
                    GUI.getDataStatus().setSimulationTime(Integer.parseInt(simulationTimeTextField.getText()));
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
                Boolean validated = Utils.validateInteger(numberOfRunsTextField);
                if (validated) {
                    GUI.getDataStatus().setNumberOfRuns(Integer.parseInt(numberOfRunsTextField.getText()));
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
                GUI.getDataStatus().setReportsFolder(selectedDirectory.getAbsolutePath());
                reportsFolderLocation.setText(GUI.getDataStatus().getReportsFolder());
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
            GUI.getDataStatus().setWriteVehicleReports(saveReportsCheckBox.isSelected());
        });

        centerPane.getChildren().addAll(heuristicsText, heuristicsChoiceBox, simulationTimeText,
                simulationTimeTextField, numberOfRunsText, numberOfRunsTextField, saveReportsText, saveReportsCheckBox,
                reportsFolderText, reportFolderButton, reportsLocationText, reportsFolderLocation);

        // Bottom Pane - Navigation Buttons
        root.setBottom(BottomPane.getBottomPane(GUI.getNavigationBar().getBackButton(), GUI.getNavigationBar().getResetButton(),
                GUI.getNavigationBar().getSaveButton(), GUI.getNavigationBar().getRunButton()));

        return new Scene(root);
    }

    public void resetProject(Stage stage, GUI gui) {
    gui.initializeStage(stage);
    }

    // A method to save the current project
    public void saveProject(GUI gui) {
        try{
            if (gui.getDataStatus().isNewProject()) {
                AlertBox.display("Saving the project", "The project has been saved to: " + gui.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                writeJSON(gui.getDataStatus().getProjectData(), gui.getDataStatus().getProjectFile());
            } else {
                if (gui.getDataStatus().getProjectData().equals(gui.getDataStatus().getOriginalProjectData())) {
                    AlertBox.display("Saving the project", "There are no changes to save in the project: " + gui.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                } else {
                    var selectedFile = createFile(gui, "project", "json");
                    if (selectedFile != null) {
                        gui.getDataStatus().setProjectFile(selectedFile.getAbsolutePath());
                        AlertBox.display("Saving the project", "The project has been saved to: " + gui.getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
                        writeJSON(gui.getDataStatus().getProjectData(), gui.getDataStatus().getProjectFile());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // A method to run the current project
    public void runProject(DataStatus dataStatus) {

        final String YAML_FILE = dataStatus.getProjectData().getMap();
        double mapResolution = dataStatus.getMapData().getResolution();
        double scaleAdjustment = 1 / mapResolution;
        double lookAheadDistance = 45 / scaleAdjustment;
        double reportsTimeIntervalInSeconds = 0.1;

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation();
        tec.setupSolver(0, 100000000);
        tec.startInference();

        // Set Heuristics
        tec.addComparator(dataStatus.getHeuristics().getComparator());

        // Set Local Re-ordering and Local Re-Planning to break Deadlocks
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(YAML_FILE);
        viz.setFontScale(3.5);
        viz.AccessInitialTransform();
        tec.setVisualization(viz);

        dataStatus.getProjectData().getVehicles().forEach((vehicle) -> {

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
            newVehicle.setInitialPose(dataStatus.getProjectData().getPose(vehicle.getInitialPose()));
            newVehicle.setGoalPoses(vehicle.getMission()
                    .stream()
                    .map(ProjectData.MissionStep::getPoseName)
                    .map(poseName -> dataStatus.getProjectData().getPose(poseName))
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
            Missions.startMissionDispatchers(tec, dataStatus.getWriteVehicleReports(), reportsTimeIntervalInSeconds,
                    dataStatus.getSimulationTime(), dataStatus.getHeuristics().getName(), 100, dataStatus.getReportsFolder(), scaleAdjustment);
    }
}