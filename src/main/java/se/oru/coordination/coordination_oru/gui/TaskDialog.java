package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import static se.oru.coordination.coordination_oru.gui.ProjectData.TaskStep;
import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;
import static se.oru.coordination.coordination_oru.gui.Utils.validateInteger;

public class TaskDialog {
    private static final int HEIGHT = 100;
    private static final int WIDTH = 150;
    private static final int GAP = 10;
    private static boolean isEdit = false;
    private static TextField nameField = new TextField();
    private static ListView<String> locationField = new ListView<>();
    private static TextField durationField = new TextField();
    private static TextField priorityField = new TextField();
    private static TextField repetitionField = new TextField(); // New field for repetition
    private static Button ok = new Button();
    private static int editIndex = -1;

    public static void add(VehicleScene scene) {
        isEdit = false;
        var taskStep = taskStep(scene);
        taskStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var taskSteps = vehicle.getTasks();
            taskSteps.add(step);
        });
    }

    public static void edit(VehicleScene scene) {
        isEdit = true;
        editIndex = scene.getTaskList().getSelectionModel().getSelectedIndex();
        var taskStep = taskStep(scene);
        taskStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var taskSteps = vehicle.getTasks();
            taskSteps.set(editIndex, step);
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

        locationField = new ListView<>();
        locationField.setPrefHeight(HEIGHT);
        locationField.setPrefWidth(WIDTH);
        locationField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        durationField = new TextField();
        durationField.setPrefWidth(WIDTH);

        priorityField = new TextField();
        priorityField.setPrefWidth(WIDTH);

        repetitionField = new TextField();
        repetitionField.setPrefWidth(WIDTH);

        if (isEdit) {
            var selectedTask = scene.getTaskList().getSelectionModel().getSelectedItem();
            var pattern = Pattern.compile("^(.*?) \\((.*?), (.*?), (.*?), (.*?)\\)$");
            var matcher = pattern.matcher(selectedTask);

            if (matcher.matches()) {
                nameField.setText(matcher.group(1).trim());
                var selectedPoses = matcher.group(2).split(" -> ");
                locationField.getItems().setAll(selectedPoses);  // Show only the selected poses
                durationField.setText(matcher.group(3).trim());
                priorityField.setText(matcher.group(4).trim());
                repetitionField.setText(matcher.group(5).trim());
            }
        }

        locationField.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showPoseSelectionDialog(scene, locationField.getSelectionModel().getSelectedItem());
            }
        });

        var vbox = selectPose(scene);

        pane.add(new Text("Name: "), 0, 0);
        pane.add(nameField, 1, 0);
        nameField.setPrefWidth(WIDTH);
        pane.add(new Text("StartUp Time (min): "), 0, 1);
        pane.add(durationField, 1, 1);
        pane.add(new Text("Pose: "), 0, 2);
        pane.add(vbox, 1, 2);
        pane.add(new Text("Priority: "), 0, 3);
        pane.add(priorityField, 1, 3);
        pane.add(new Text("Repeat: "), 0, 4);  // New field for repetition
        pane.add(repetitionField, 1, 4);

        dialog.getDialogPane().setContent(pane);
    }

    private static VBox selectPose(VehicleScene scene) {
        var addButton = getAddButton(scene);
        var deleteButton = getDeleteButton();
        var upButton = getUpButton();
        var downButton = getDownButton();

        HBox buttonBox = new HBox(10, addButton, deleteButton, upButton, downButton);
        return new VBox(10, locationField, buttonBox);
    }

    private static Button getAddButton(VehicleScene scene) {
        var addButton = new Button("Add Pose");
        addButton.setOnAction(e -> {
            var randomPose = getRandomPose(scene);
            locationField.getItems().add(randomPose);
        });
        return addButton;
    }

    private static Button getDeleteButton() {
        var deleteButton = new Button("Delete Pose");
        deleteButton.setOnAction(e -> {
            int selectedIdx = locationField.getSelectionModel().getSelectedIndex();
            if (selectedIdx >= 0) {
                locationField.getItems().remove(selectedIdx);
            }
        });
        return deleteButton;
    }

    private static Button getUpButton() {
        var upButton = new Button("↑");
        upButton.setOnAction(e -> {
            int selectedIdx = locationField.getSelectionModel().getSelectedIndex();
            if (selectedIdx > 0) {
                String pose = locationField.getItems().remove(selectedIdx);
                locationField.getItems().add(selectedIdx - 1, pose);
                locationField.getSelectionModel().select(selectedIdx - 1);
            }
        });
        return upButton;
    }

    private static Button getDownButton() {
        var downButton = new Button("↓");
        downButton.setOnAction(e -> {
            int selectedIdx = locationField.getSelectionModel().getSelectedIndex();
            if (selectedIdx < locationField.getItems().size() - 1) {
                String pose = locationField.getItems().remove(selectedIdx);
                locationField.getItems().add(selectedIdx + 1, pose);
                locationField.getSelectionModel().select(selectedIdx + 1);
            }
        });
        return downButton;
    }

    private static void controller() {
        durationField.textProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(!validateDouble(durationField) || !validateInteger(priorityField) || !validateInteger(repetitionField));
        });

        priorityField.textProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(!validateDouble(durationField) || !validateInteger(priorityField) || !validateInteger(repetitionField));
        });

        repetitionField.textProperty().addListener((observable, oldValue, newValue) -> {
            ok.setDisable(!validateInteger(repetitionField));
        });
    }

    private static Optional<TaskStep> dialogResult(VehicleScene scene, Dialog<TaskStep> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && validateDouble(durationField) && validateInteger(priorityField) && validateInteger(repetitionField)) {
                var taskStep = new TaskStep();
                taskStep.setTaskName(nameField.getText());
                var selectedPoses = locationField.getItems();
                var poses = String.join(" -> ", selectedPoses);
                taskStep.setPoseName(poses);
                taskStep.setDuration(Double.parseDouble(durationField.getText()));
                taskStep.setPriority(Integer.parseInt(priorityField.getText()));
                taskStep.setRepetition(Integer.parseInt(repetitionField.getText()));
                return taskStep;
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent()) {
            var taskStep = result.get();
            if (isEdit) {
                scene.getTaskList().getItems().set(editIndex, formatTaskStep(taskStep));
            } else {
                scene.getTaskList().getItems().add(formatTaskStep(taskStep));
            }
        }
        return result;
    }

    private static String formatTaskStep(TaskStep taskStep) {
        return taskStep.getTaskName() + " (" + taskStep.getPoseName() + ", " + taskStep.getDuration() + ", " + taskStep.getPriority() + ", " + taskStep.getRepetition() + ")";
    }

    private static void showPoseSelectionDialog(VehicleScene scene, String selectedPose) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Pose");

        var buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

        var comboBox = new ComboBox<String>();
        comboBox.getItems().addAll(scene.getMain().getDataStatus().getProjectData().getPoses().keySet());
        comboBox.getSelectionModel().select(selectedPose);

        var vbox = new VBox(new Label("Select new pose:"), comboBox);
        vbox.setSpacing(10);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                return comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPose -> {
            int selectedIdx = locationField.getSelectionModel().getSelectedIndex();
            locationField.getItems().set(selectedIdx, newPose);
        });
    }

    private static String getRandomPose(VehicleScene scene) {
        var poses = scene.getMain().getDataStatus().getProjectData().getPoses().keySet();
        int randomIndex = new Random().nextInt(poses.size());
        return poses.stream().skip(randomIndex).findFirst().orElse(null);
    }
}
