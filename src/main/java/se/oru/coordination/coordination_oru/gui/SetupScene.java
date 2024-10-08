package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.List;

public class SetupScene {
    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int WIDTH = 220;
    public static final int PANE_WIDTH = 800;

    private Text pathPlanner;
    private ChoiceBox<String> pathPlannerField;
    private Text simulationTime;
    private TextField simulationTimeField;
    private Text numberOfRun;
    private TextField numberOfRunField;
    private Text reportFolder;
    private Button reportFolderField;
    private Text reportLocation;
    private Text reportLocationField;
    private Text saveReport;
    private CheckBox saveReportField;

    private BorderPane pane;
    private final Main main;
    private final SetupController controller = new SetupController(this);

    public SetupScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        pane.setPrefWidth(PANE_WIDTH);
        centerPane();
        menuBar();
        navigationBar();
        return new Scene(pane);
    }

    private void menuBar() {
        pane.setTop(MenuBar.update(main, SceneState.SETUP));
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.SETUP));
    }

    private void centerPane() {
        var centerPane = initializeCenterPane();
        setupCenterPane(centerPane);
        controllers();
    }

    private GridPane initializeCenterPane() {
        var centerPane = new GridPane();
        centerPane.setPadding(new Insets(PADDING));
        BorderPane.setMargin(centerPane, new Insets(PADDING));
        centerPane.setHgap(SPACING);
        centerPane.setVgap(SPACING);
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        return centerPane;
    }

    private void setupCenterPane(GridPane centerPane) {
        setupTexts();
        setupFields();
        addChildren(centerPane);
    }

    private void setupTexts() {
        pathPlanner = text("Path Planner: ", 0, 0);
        simulationTime = text("Simulation Time (minutes): ", 0, 1);
        numberOfRun = text("No. of Runs: ", 0, 2);
        saveReport = text("Saving Vehicles Reports: ", 0, 3);
        reportFolder = text("Folder to Save the Reports: ", 0, 4);
        reportFolder.setVisible(false);
        reportLocation = text("Reports will be saved in:", 0, 5);
        reportLocation.setVisible(false);
    }

    private void setupFields() {
        var pathPlanners = List.of("Fast (RRTConnect)", "Optimal (RRT*)");
        pathPlannerField = choiceBox(pathPlanners, 0);
        simulationTimeField = textField(1);
        simulationTimeField.setText("30");
        numberOfRunField = textField(2);
        numberOfRunField.setText("1");
        saveReportField = checkBox(3);
        saveReportField.setSelected(false);
        reportFolderField = button("Browse...", 4);
        reportFolderField.setVisible(false);
        reportLocationField = text("", 1, 5);
        reportLocationField.setVisible(false);
    }

    private void addChildren(GridPane centerPane) {
        centerPane.getChildren().addAll(pathPlanner, pathPlannerField, simulationTime,
                simulationTimeField, numberOfRun, numberOfRunField, saveReport, saveReportField,
                reportFolder, reportFolderField, reportLocation, reportLocationField);
    }

    private ChoiceBox<String> choiceBox(List<String> items, int row) {
        var choiceBox = new ChoiceBox<String>();
        choiceBox.getItems().addAll(items);
        choiceBox.setMaxWidth(WIDTH);
        choiceBox.setValue(items.stream().findFirst().orElse(null));
        GridPane.setConstraints(choiceBox, 1, row);
        return choiceBox;
    }

    private TextField textField(int row) {
        var textField = new TextField();
        textField.setMaxWidth(WIDTH);
        GridPane.setConstraints(textField, 1, row);
        return textField;
    }

    private CheckBox checkBox(int row) {
        var checkBox = new CheckBox();
        GridPane.setConstraints(checkBox, 1, row);
        return checkBox;
    }

    private Button button(String name, int row) {
        var button = new Button(name);
        GridPane.setConstraints(button, 1, row);
        return button;
    }

    private Text text(String name, int column, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, column, row);
        return text;
    }

    private void controllers() {
        simulationTimeField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeSimulationTime();
        });
        numberOfRunField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) controller.changeNumberOfRun();
        });
        pathPlannerField.setOnAction(e -> controller.choosePathPlanner());
        saveReportField.setOnAction(e -> controller.checkSavingReport());
        reportFolderField.setOnAction(e -> controller.clickReportFolder());
    }
    public Main getMain() {
        return main;
    }

    public ChoiceBox<String> getPathPlannerField() {
        return pathPlannerField;
    }

    public TextField getSimulationTimeField() {
        return simulationTimeField;
    }

    public TextField getNumberOfRunField() {
        return numberOfRunField;
    }

    public Button getReportFolderField() {
        return reportFolderField;
    }

    public Text getReportLocationField() {
        return reportLocationField;
    }

    public CheckBox getSaveReportField() {
        return saveReportField;
    }

    public Text getReportLocation() {
        return reportLocation;
    }

    public Text getReportFolder() {
        return reportFolder;
    }

    public BorderPane getPane() {
        return pane;
    }
}
