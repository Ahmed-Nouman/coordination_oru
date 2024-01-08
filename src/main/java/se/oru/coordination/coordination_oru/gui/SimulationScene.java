package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;

public class SimulationScene {
    public static final int PADDING = 30;
    public static final int SPACING = 10;
    private final Main main;
    private final SimulationController simulationController = new SimulationController(this);

    public SimulationScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        var pane = new BorderPane();
        getMenuBar(pane);
        getNavigationBar(pane);
        getCenterPane(pane);
        return new Scene(pane);
    }

    private void getMenuBar(BorderPane pane) {
        pane.setTop(MenuBar.update(main, SceneState.SIMULATION));
    }

    private void getNavigationBar(BorderPane pane) {
        pane.setBottom(NavigationBar.update(main, SceneState.SIMULATION));
    }

    private void getCenterPane(BorderPane pane) {
        // Center Pane
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(PADDING));
        BorderPane.setMargin(centerPane, new Insets(PADDING));
        centerPane.setHgap(SPACING);
        centerPane.setVgap(SPACING);
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);

        // heuristics choice-box
        var heuristicsText = new Text("Heuristics: ");
        GridPane.setConstraints(heuristicsText, 0, 0);
        ChoiceBox<String> heuristicsChoiceBox = new ChoiceBox<>();
        heuristicsChoiceBox.setPrefWidth(220);
        GridPane.setConstraints(heuristicsChoiceBox, 1, 0);
        heuristicsChoiceBox.getItems().addAll(Heuristics.getAllHeuristicNames());
        heuristicsChoiceBox.setValue(heuristicsChoiceBox.getItems().stream().findFirst().orElse(null));
        simulationController.getHeuristics(heuristicsChoiceBox);

        // simulationTime text-field
        var simulationTime = new Text("Simulation Time (minutes): ");
        GridPane.setConstraints(simulationTime, 0, 1);
        var simulationTimeField = new TextField();
        simulationTimeField.setMaxWidth(heuristicsChoiceBox.getPrefWidth());
        simulationTimeField.setText("30");
        GridPane.setConstraints(simulationTimeField, 1, 1);
        simulationController.getSimulationTime(simulationTimeField);

        var numberOfRun = new Text("No. of Runs: ");
        GridPane.setConstraints(numberOfRun, 0, 2);
        var numberOfRunField = new TextField();
        numberOfRunField.setMaxWidth(heuristicsChoiceBox.getPrefWidth());
        numberOfRunField.setText("1");
        GridPane.setConstraints(numberOfRunField, 1, 2);
        simulationController.getNumberOfRun(numberOfRunField);

        var reportsLocationText = new Text("Reports will be saved in:");
        GridPane.setConstraints(reportsLocationText, 0, 5);
        reportsLocationText.setVisible(false);
        var reportsFolderLocation = new Text();
        GridPane.setConstraints(reportsFolderLocation, 1, 5);
        reportsFolderLocation.setVisible(false);

        var reportsFolderText = new Text("Folder to Save the Reports: ");
        GridPane.setConstraints(reportsFolderText, 0, 4);
        reportsFolderText.setVisible(false);
        var reportFolderButton = new Button("Browse...");
        GridPane.setConstraints(reportFolderButton, 1, 4);
        reportFolderButton.setVisible(false);
        simulationController.getReportFolder(reportFolderButton, reportsLocationText, reportsFolderLocation);

        var saveReports = new Text("Saving Vehicles Reports: ");
        GridPane.setConstraints(saveReports, 0, 3);
        var saveReportField = new CheckBox();
        saveReportField.setSelected(false);
        GridPane.setConstraints(saveReportField, 1, 3);
        simulationController.getIfSavingReports(saveReportField, reportsFolderText, reportFolderButton, reportsLocationText, reportsFolderLocation);

        centerPane.getChildren().addAll(heuristicsText, heuristicsChoiceBox, simulationTime,
                simulationTimeField, numberOfRun, numberOfRunField, saveReports, saveReportField,
                reportsFolderText, reportFolderButton, reportsLocationText, reportsFolderLocation);
    }

    public Main getMain() {
        return main;
    }
}