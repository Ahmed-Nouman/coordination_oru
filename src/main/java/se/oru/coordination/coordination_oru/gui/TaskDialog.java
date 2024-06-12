package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.regex.Pattern;

import static se.oru.coordination.coordination_oru.gui.ProjectData.TaskStep;
import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;
import static se.oru.coordination.coordination_oru.gui.Utils.validateInteger;

public class TaskDialog {
    private static final int WIDTH = 150;
    private static final int GAP = 10;
    private static boolean isEdit = false;
    private static TextField nameField = new TextField();
    private static ChoiceBox<String> locationField = new ChoiceBox<>();
    private static TextField durationField = new TextField();
    private static TextField priorityField = new TextField();
    private static Button ok = new Button();
    private static int editIndex = -1; // To keep track of the index of the task being edited

    public static void add(VehicleScene scene) {
        isEdit = false;
        var taskStep = taskStep(scene);
        taskStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var taskSteps = vehicle.getTask();
            taskSteps.add(step);
        });
    }

    public static void edit(VehicleScene scene) {
        isEdit = true;
        editIndex = scene.getTasks().getSelectionModel().getSelectedIndex();
        var taskStep = taskStep(scene);
        taskStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var taskSteps = vehicle.getTask();
            taskSteps.set(editIndex, step); // Update the existing task
        });
    }

    private static ProjectData.Vehicle getVehicle(VehicleScene scene) {
        return scene.getMain().getDataStatus().getProjectData().getVehicle(
                scene.getMain().getDataStatus().getProjectData().getVehicleID(
                        scene.getVehicles().getSelectionModel().getSelectedItem(),
                        scene.getMain().getDataStatus().getProjectData().getVehicles()));
    }

    private static Optional<TaskStep> taskStep(VehicleScene scene) {
        var dialog = new Dialog<TaskStep>();
        dialog.setTitle(isEdit ? "Edit task" : "Add task");
        var buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(scene, dialog);
        controller();
        ok = (Button) dialog.getDialogPane().lookupButton(buttonType);
        ok.setDisable(!isEdit);
        return dialogResult(scene, dialog, buttonType);
    }

    private static void pane(VehicleScene scene, Dialog<TaskStep> dialog) {
        var pane = new GridPane();
        pane.setHgap(GAP);
        pane.setVgap(GAP);

        nameField = new TextField();
        nameField.setPrefWidth(WIDTH);

        locationField = new ChoiceBox<>();
        locationField.setPrefWidth(WIDTH);
        var choices = scene.getMain().getDataStatus().getProjectData().getPoses().keySet();
        locationField.getItems().addAll(choices);

        durationField = new TextField();
        durationField.setPrefWidth(WIDTH);

        priorityField = new TextField();
        priorityField.setPrefWidth(WIDTH);

        if (isEdit) {
            var selectedTask = scene.getTasks().getSelectionModel().getSelectedItem();
            var pattern = Pattern.compile("^(.*?) \\((.*?), (.*?), (.*?)\\)$");
            var matcher = pattern.matcher(selectedTask);

            if (matcher.matches()) {
                nameField.setText(matcher.group(1).trim());
                locationField.setValue(matcher.group(2).trim());
                durationField.setText(matcher.group(3).trim());
                priorityField.setText(matcher.group(4).trim());
            }
        } else {
            locationField.setValue(choices.iterator().next());
        }

        pane.add(new Text("Name: "), 0, 0);
        pane.add(nameField, 1, 0);
        nameField.setPrefWidth(WIDTH);
        pane.add(new Text("Duration (min): "), 0, 1);
        pane.add(durationField, 1, 1);
        pane.add(new Text("Pose: "), 0, 2);
        pane.add(locationField, 1, 2);
        pane.add(new Text("Priority: "), 0, 3);
        pane.add(priorityField, 1, 3);

        dialog.getDialogPane().setContent(pane);
    }

    private static void controller() {
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(!validateDouble(durationField) || !validateInteger(priorityField));
        });

        priorityField.textProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(!validateDouble(durationField) || !validateInteger(priorityField));
        });
    }

    private static Optional<TaskStep> dialogResult(VehicleScene scene, Dialog<TaskStep> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && validateDouble(durationField) && validateInteger(priorityField)) {
                var taskStep = new TaskStep();
                taskStep.setTaskName(nameField.getText());
                taskStep.setPoseName(locationField.getValue());
                taskStep.setDuration(Double.parseDouble(durationField.getText()));
                taskStep.setPriority(Integer.parseInt(priorityField.getText()));
                return taskStep;
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent()) {
            var taskStep = result.get();
            if (isEdit) {
                scene.getTasks().getItems().set(editIndex, formatTaskStep(taskStep));
            } else {
                scene.getTasks().getItems().add(formatTaskStep(taskStep));
            }
        }
        return result;
    }

    private static String formatTaskStep(TaskStep taskStep) {
        return taskStep.getTaskName() + " (" + taskStep.getPoseName() + ", " + taskStep.getDuration() + ", " + taskStep.getPriority() + ")";
    }
}

