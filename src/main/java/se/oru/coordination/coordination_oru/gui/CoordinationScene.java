package se.oru.coordination.coordination_oru.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int HEIGHT = 100;
    public static final int WIDTH = 220;
    private static final int TEXT_WIDTH = 180;

    private Text priorityRule;
    private Text trafficControl;
    private Text trigger;
    private VBox triggerField = new VBox();
    private final Button add = new Button("Add");
    private final Button delete = new Button("Delete");
    private final Button down = new Button("↓");
    private final Button up = new Button("↑");
    private ListView<String> triggerList = new ListView<>();
    private Text adaptiveVelocity;
    private Text adaptivePriorityRule;
    private ChoiceBox<String> priorityRuleField;
    private ChoiceBox<String> trafficControlField;
    private TextField adaptiveVelocityField;
    private ChoiceBox<String> adaptivePriorityRuleField;
    private final Main main;

    private BorderPane pane;
    private final CoordinationController controller = new CoordinationController(this);

    public CoordinationScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = new BorderPane();
        menuBar();
        centerPane();
        navigationBar();

        initializeTriggerList();
        initializeTrigger();
        triggerController();

        return new Scene(pane);
    }

    private void triggerController() {
        add.setOnAction(e -> controller.addTrigger());
        delete.setOnAction(e -> controller.deleteTrigger());
        down.setOnAction(e -> controller.moveDown());
        up.setOnAction(e -> controller.moveUp());
    }

    private void initializeTrigger() {
        var triggerButtons = new HBox();
        triggerButtons.setSpacing(10);
        triggerButtons.setAlignment(Pos.CENTER);
        triggerButtons.getChildren().addAll(add, delete, down, up);
        triggerField.setMaxWidth(WIDTH);
        triggerField.getChildren().addAll(triggerList, triggerButtons);
        GridPane.setConstraints(triggerField, 1, 2);
    }

    private void initializeTriggerList() {
        triggerList.setMaxWidth(WIDTH);
        triggerList.setMaxHeight(110);

        // Load triggers from the main project data
        ObservableList<String> triggers = FXCollections.observableArrayList(
                main.getDataStatus().getProjectData().getTriggers().stream()
                        .map(trigger -> String.format("(%s, %s, %s)",
                                trigger.getVehicle(),
                                trigger.getTask().toString(),
                                trigger.getVehicleToComply().toString()))
                        .collect(Collectors.toList())
        );

        triggerList.setItems(triggers);
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
        trigger = text("Triggers: ", 0, 2);
        adaptiveVelocity = text("Adaptive Velocity: ", 0, 3);
        adaptivePriorityRule = text("Adaptive Priority Rule: ", 0, 4);
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
                "Shutdown",
                "Velocity Adaptation",
                "Priority Rule Adaptation"));
        trafficControlField = choiceBox(trafficControlStrategies, 1);
        trafficControlField.setValue(main.getDataStatus().getProjectData().getTrafficControl());
        this.getMain().getDataStatus().getProjectData().setTrafficControl(this.getTrafficControlField().getValue());  // Set the default traffic control strategy in DataStatus

        adaptiveVelocityField = new TextField();
        adaptiveVelocityField.setMaxWidth(WIDTH);
        GridPane.setConstraints(adaptiveVelocityField, 1, 3);

        adaptivePriorityRuleField = choiceBox(Heuristics.getHeuristicNames(), 4);
        adaptivePriorityRuleField.setValue(Heuristics.getHeuristicNames().stream().findFirst().orElse(null));
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
        centerPane.getChildren().addAll(priorityRule, priorityRuleField, trafficControl, trafficControlField, trigger, triggerField,
                adaptiveVelocity, adaptiveVelocityField, adaptivePriorityRule, adaptivePriorityRuleField);
    }

    private void controllers() {
        priorityRuleField.setOnAction(e -> controller.chooseHeuristic());
        trafficControlField.setOnAction(e -> controller.chooseTrafficControl());
        adaptivePriorityRuleField.setOnAction(e -> controller.chooseNewHeuristic());
    }

    private void menuBar() {
        pane.setTop(MenuBar.update(main, SceneState.COORDINATION));
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

    public TextField getAdaptiveVelocityField() {
        return adaptiveVelocityField;
    }

    public ChoiceBox<String> getAdaptivePriorityRuleField() {
        return adaptivePriorityRuleField;
    }

    public Text getAdaptiveVelocity() {
        return adaptiveVelocity;
    }

    public Text getAdaptivePriorityRule() {
        return adaptivePriorityRule;
    }

    public ListView<String> getTriggerList() {
        return triggerList;
    }
}
