package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.HumanVehicle;

import java.util.ArrayList;
import java.util.List;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int WIDTH = 220;

    private Text priorityRule;
    private Text trafficControl;
    private Text triggerVehicle;
    private ChoiceBox<String> priorityRuleField;
    private ChoiceBox<String> trafficControlField;
    private ChoiceBox<String> triggerVehicleField;

    private final Main main;
    private BorderPane pane;
    private final CoordinationController controller = new CoordinationController(this);

    public CoordinationScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        centerPane();
        navigationBar();
        return new Scene(pane);
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.COORDINATION));
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
        priorityRule = text("Priority Rule: ", 0, 0);
        trafficControl = text("Traffic Control: ", 0, 1);
        triggerVehicle = text("Trigger Vehicle: ", 0, 2);
    }

    private Text text(String name, int column, int row) {
        var text = new Text(name);
        GridPane.setConstraints(text, column, row);
        return text;
    }

    private void setupFields() {
        priorityRuleField = choiceBox(Heuristics.getHeuristicNames(), 0);
        priorityRuleField.setValue(Heuristics.getHeuristicNames().stream().findFirst().orElse(null));
        var trafficControlStrategies = new ArrayList<>(List.of(
                "Mixed Traffic",
                "Vehicle Stoppage",
                "Vehicle Speed Change",
                "Priority Rule Change"));
        trafficControlField = choiceBox(trafficControlStrategies, 1);
        trafficControlField.setValue(trafficControlStrategies.stream().findFirst().orElse(null));
        var vehicles = main.getDataStatus().getProjectData().getVehicleNames();
        triggerVehicleField = choiceBox(vehicles, 2);
        triggerVehicleField.setValue(vehicles.stream().findFirst().orElse(null));
    }

    private ChoiceBox<String> choiceBox(List<String> items, int row) {
        var choiceBox = new ChoiceBox<String>();
        choiceBox.getItems().addAll(items);
        choiceBox.setMaxWidth(WIDTH);
        choiceBox.setValue(items.stream().findFirst().orElse(null));
        GridPane.setConstraints(choiceBox, 1, row);
        return choiceBox;
    }

    private void addChildren(GridPane centerPane) {
        centerPane.getChildren().addAll(priorityRule, priorityRuleField, trafficControl, trafficControlField, triggerVehicle, triggerVehicleField);
    }
    
    private void controllers() {
        priorityRuleField.setOnAction(e -> controller.chooseHeuristic());
        trafficControlField.setOnAction(e -> controller.chooseTrafficControl());
        triggerVehicleField.setOnAction(e -> controller.chooseTriggerVehicle());
    }

    public Main getMain() {
        return main;
    }

    public BorderPane getPane() {
        return pane;
    }

    public ChoiceBox<String> getPriorityRuleField() {
        return priorityRuleField;
    }

    public ChoiceBox<String> getTrafficControlField() {
        return trafficControlField;
    }

    public ChoiceBox<String> getTriggerVehicleField() {
        return triggerVehicleField;
    }
}
