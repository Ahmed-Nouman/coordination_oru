package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.io.File;

public class SimulationController {
    private final SimulationScene simulationScene;

    public SimulationController(SimulationScene simulationScene) {
        this.simulationScene = simulationScene;
    }

    public void getHeuristics(ChoiceBox<String> heuristicsChoiceBox) {
        heuristicsChoiceBox.setOnAction(e -> {
            String selectedHeuristic = heuristicsChoiceBox.getValue();
            if (selectedHeuristic != null) {
                switch (selectedHeuristic) {
                    case "MOST_DISTANCE_TRAVELLED":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TRAVELLED));
                        break;
                    case "MOST_DISTANCE_TO_TRAVEL":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TO_TRAVEL));
                        break;
                    case "RANDOM":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.RANDOM));
                        break;
                    case "HIGHEST_PRIORITY_FIRST":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST));
                        break;
                    case "HUMAN_FIRST":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST));
                        break;
                    case "AUTONOMOUS_FIRST":
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST));
                        break;
                    default:
                        simulationScene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST));
                        break;
                }
            }
        });
    }

    public void getSimulationTime(TextField simulationTimeField) {
        simulationTimeField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(simulationTimeField);
                if (validated) {
                    simulationScene.getMain().getDataStatus().setSimulationTime(Integer.parseInt(simulationTimeField.getText()));
                }
            }
        });
    }

    public void getIfSavingReports(CheckBox saveReportField, Text reportsFolderText, Button reportFolderButton, Text reportsLocationText, Text reportsFolderLocation) {
        saveReportField.setOnAction(e -> {
            if (saveReportField.isSelected()) {
                reportsFolderText.setVisible(true);
                reportFolderButton.setVisible(true);
            } else {
                reportsFolderText.setVisible(false);
                reportFolderButton.setVisible(false);
                reportsLocationText.setVisible(false);
                reportsFolderLocation.setVisible(false);
            }
            simulationScene.getMain().getDataStatus().setWriteVehicleReports(saveReportField.isSelected());
        });
    }

    public void getNumberOfRun(TextField numberOfRunField) {
        numberOfRunField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(numberOfRunField);
                if (validated) {
                    simulationScene.getMain().getDataStatus().setNumberOfRuns(Integer.parseInt(numberOfRunField.getText()));
                }
            }
        });
    }

    public void getReportFolder(Button reportFolderButton, Text reportsLocationText, Text reportsFolderLocation) {
        reportFolderButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(new Stage());
            if (selectedDirectory != null) {
                reportsLocationText.setVisible(true);
                reportsFolderLocation.setVisible(true);
                simulationScene.getMain().getDataStatus().setReportsFolder(selectedDirectory.getAbsolutePath());
                reportsFolderLocation.setText(simulationScene.getMain().getDataStatus().getReportsFolder());
            }
        });
    }
}