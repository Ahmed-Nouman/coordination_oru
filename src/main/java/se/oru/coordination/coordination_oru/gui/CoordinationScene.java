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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinationScene {

    private static final int PADDING = 30;
    private static final int SPACING = 10;
    public static final int WIDTH = 220;

    private Text priorityRule;
    private Text trafficControl;
    private Text trigger;
    private Text transientVelocity;
    private Text transientPriorityRule;
    private final VBox triggerField = new VBox();
    private final Button add = new Button("Add");
    private final Button delete = new Button("Delete");
    private final ListView<String> triggerList = new ListView<>();
    private ChoiceBox<String> priorityRuleField;
    private ChoiceBox<String> trafficControlField;
    private TextField transientVelocityField;
    private ChoiceBox<String> transientPriorityRuleField;
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

        return new Scene(pane);
    }

    private void initializeTrigger() {
        triggerField.getChildren().clear();
        var triggerButtons = new HBox();
        triggerButtons.setSpacing(10);
        triggerButtons.setPadding(new Insets(5));
        triggerButtons.setAlignment(Pos.CENTER_LEFT);
        triggerButtons.setMaxWidth(WIDTH);
        HBox.setHgrow(add, Priority.ALWAYS);
        HBox.setHgrow(delete, Priority.ALWAYS);
        add.setMaxWidth(Double.MAX_VALUE);
        delete.setMaxWidth(Double.MAX_VALUE);
        triggerButtons.getChildren().addAll(add, delete);
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

        triggerList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                int selectedIndex = triggerList.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    TriggerDialog.edit(this);
                }
            }
        });
    }

    public void updateTriggerList() {
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
        controller.updateVisibilityBasedOnTrafficControl();
    }

    private void setupTexts() {
        priorityRule = text("Overall Priority Rule: ", 0, 0);
        trafficControl = text("Traffic Control: ", 0, 1);
        trigger = text("Control States: ", 0, 2);
        transientVelocity = text("Transient Velocity: ", 0, 3);
        transientVelocity.setVisible(false);
        transientPriorityRule = text("Transient Priority Rule: ", 0, 4);
        transientPriorityRule.setVisible(false);
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

        transientVelocityField = new TextField();
        transientVelocityField.setMaxWidth(WIDTH);
        transientVelocityField.setVisible(false);
        GridPane.setConstraints(transientVelocityField, 1, 3);

        transientPriorityRuleField = choiceBox(Heuristics.getHeuristicNames(), 4);
        transientPriorityRuleField.setValue(Heuristics.getHeuristicNames().stream().findFirst().orElse(null));
        transientPriorityRuleField.setVisible(false);
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
                transientVelocity, transientVelocityField, transientPriorityRule, transientPriorityRuleField);
    }

    private void controllers() {
        priorityRuleField.setOnAction(e -> controller.chooseHeuristic());
        trafficControlField.setOnAction(e -> controller.chooseTrafficControl());
        transientPriorityRuleField.setOnAction(e -> controller.chooseTransientHeuristic());
        add.setOnAction(e -> controller.addTrigger());
        delete.setOnAction(e -> controller.deleteTrigger());
        controller.chooseTrafficControl();
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

    public TextField getTransientVelocityField() {
        return transientVelocityField;
    }

    public ChoiceBox<String> getTransientPriorityRuleField() {
        return transientPriorityRuleField;
    }

    public Text getTransientVelocity() {
        return transientVelocity;
    }

    public Text getTransientPriorityRule() {
        return transientPriorityRule;
    }

    public ListView<String> getTriggerList() {
        return triggerList;
    }

    public VBox getTriggerField() {
        return triggerField;
    }

    public Text getTrigger() {
        return trigger;
    }
}
