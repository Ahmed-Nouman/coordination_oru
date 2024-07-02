package se.oru.coordination.coordination_oru.gui;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import se.oru.coordination.coordination_oru.gui.ProjectData.Trigger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TriggerDialog {
    private static boolean isEdit = false;
    private static ChoiceBox<String> triggerVehicleField = new ChoiceBox<>();
    private static ChoiceBox<String> triggerTaskField = new ChoiceBox<>();
    private static ListView<String> vehicleToComplyField = new ListView<>();
    private static Button ok = new Button();
    private static int editIndex = -1;

    public static void add(CoordinationScene scene) {
        isEdit = false;
        var trigger = trigger(scene);
        trigger.ifPresent(t -> {
            scene.getMain().getDataStatus().getProjectData().getTriggers().add(t);
            scene.updateTriggerList();
        });
    }

    public static void edit(CoordinationScene scene) {
        isEdit = true;
        editIndex = scene.getTriggerList().getSelectionModel().getSelectedIndex();
        var trigger = trigger(scene);
        trigger.ifPresent(t -> {
            scene.getMain().getDataStatus().getProjectData().getTriggers().set(editIndex, t);
            scene.updateTriggerList();
        });
    }

    private static Optional<Trigger> trigger(CoordinationScene scene) {
        var dialog = new Dialog<Trigger>();
        dialog.setTitle(isEdit ? "Edit Trigger" : "Add Trigger");
        var buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(scene, dialog);
        controller(scene);
        ok = (Button) dialog.getDialogPane().lookupButton(buttonType);
        ok.setDisable(!isEdit);
        return dialogResult(scene, dialog, buttonType);
    }

    private static void pane(CoordinationScene scene, Dialog<Trigger> dialog) {
        var pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);

        triggerVehicleField = new ChoiceBox<>();
        triggerVehicleField.setItems(FXCollections.observableArrayList(scene.getMain().getDataStatus().getProjectData().getVehicleNames()));

        triggerTaskField = new ChoiceBox<>();
        vehicleToComplyField = new ListView<>();
        vehicleToComplyField.setPrefHeight(100);
        vehicleToComplyField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        if (isEdit) {
            var trigger = scene.getMain().getDataStatus().getProjectData().getTriggers().get(editIndex);
            triggerVehicleField.setValue(trigger.getVehicle());
            updateTaskField(scene); // Update task field based on selected vehicle
            updateVehicleToComplyField(scene, trigger.getVehicle()); // Update comply vehicles based on selected vehicle
            triggerTaskField.setValue(trigger.getTask().get(0));
            vehicleToComplyField.getSelectionModel().clearSelection();
            trigger.getVehicleToComply().forEach(vehicle -> {
                int index = vehicleToComplyField.getItems().indexOf(vehicle);
                if (index >= 0) {
                    vehicleToComplyField.getSelectionModel().select(index);
                }
            });
        } else {
            updateVehicleToComplyField(scene, null); // Initialize comply vehicles
        }

        pane.add(new Text("Vehicle: "), 0, 0);
        pane.add(triggerVehicleField, 1, 0);
        pane.add(new Text("Task: "), 0, 1);
        pane.add(triggerTaskField, 1, 1);
        pane.add(new Text("Effected Vehicles: "), 0, 2);
        pane.add(new VBox(vehicleToComplyField), 1, 2);

        dialog.getDialogPane().setContent(pane);
    }

    private static void controller(CoordinationScene scene) {
        triggerVehicleField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateTaskField(scene); // Update task field based on selected vehicle
                updateVehicleToComplyField(scene, newValue); // Update comply vehicles based on selected vehicle
            }
            ok.setDisable(newValue == null || triggerTaskField.getValue() == null || vehicleToComplyField.getSelectionModel().getSelectedItems().isEmpty());
        });

        triggerTaskField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(triggerVehicleField.getValue() == null || newValue == null || vehicleToComplyField.getSelectionModel().getSelectedItems().isEmpty());
        });

        vehicleToComplyField.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    ok.setDisable(triggerVehicleField.getValue() == null || triggerTaskField.getValue() == null || vehicleToComplyField.getSelectionModel().getSelectedItems().isEmpty());
                }
            }
        });
    }

    private static void updateTaskField(CoordinationScene scene) {
        var selectedVehicle = triggerVehicleField.getValue();
        var vehicle = scene.getMain().getDataStatus().getProjectData().getVehicles().stream()
                .filter(v -> v.getName().equals(selectedVehicle))
                .findFirst().orElse(null);
        if (vehicle != null) {
            var taskOptions = FXCollections.observableArrayList(
                    vehicle.getTasks().stream()
                            .map(ProjectData.TaskStep::getTaskName)
                            .collect(Collectors.toList())
            );
            triggerTaskField.setItems(taskOptions);
            triggerTaskField.setValue(taskOptions.isEmpty() ? null : taskOptions.get(0));
        } else {
            triggerTaskField.setItems(FXCollections.observableArrayList());
            triggerTaskField.setValue(null);
        }
    }

    private static void updateVehicleToComplyField(CoordinationScene scene, String triggerVehicle) {
        var vehicles = scene.getMain().getDataStatus().getProjectData().getVehicleNames();
        var filteredVehicles = vehicles.stream()
                .filter(vehicle -> !vehicle.equals(triggerVehicle))
                .collect(Collectors.toList());
        vehicleToComplyField.setItems(FXCollections.observableArrayList(filteredVehicles));
    }

    private static Optional<Trigger> dialogResult(CoordinationScene scene, Dialog<Trigger> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && triggerVehicleField.getValue() != null && triggerTaskField.getValue() != null && !vehicleToComplyField.getSelectionModel().getSelectedItems().isEmpty()) {
                var trigger = new Trigger();
                trigger.setVehicle(triggerVehicleField.getValue());
                trigger.setTask(List.of(triggerTaskField.getValue()));
                trigger.setVehicleToComply(vehicleToComplyField.getSelectionModel().getSelectedItems());
                return trigger;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
