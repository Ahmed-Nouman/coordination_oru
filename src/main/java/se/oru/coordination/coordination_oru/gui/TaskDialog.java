package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;
import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;

public class TaskDialog {
    private static final int WIDTH = 150;
    private static final int GAP = 10;
    private static boolean isEdit = false;
    private static final TextField nameField = new TextField();
    private static ChoiceBox<String> goalField1 = new ChoiceBox<>();
    private static ChoiceBox<String> goalField2 = new ChoiceBox<>();
    private static ChoiceBox<String> goalField3 = new ChoiceBox<>();
    private static ChoiceBox<String> goalField4 = new ChoiceBox<>();
    private static ChoiceBox<String> goalField5 = new ChoiceBox<>();
    private static TextField durationField = new TextField();
    private static final TextField priority = new TextField();
    private static Button ok = new Button();

    public static void add(VehicleScene scene) {
        isEdit = false;
        var missionStep = taskStep(scene);
        missionStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var missionSteps = vehicle.getMission();
            missionSteps.add(step);
        });
    }

    public static void edit(VehicleScene scene) {
        isEdit = true;
        var taskStep = taskStep(scene);
        taskStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var missionSteps = vehicle.getMission();
            missionSteps.add(step);
        });
    }

    private static ProjectData.Vehicle getVehicle(VehicleScene scene) {
        return scene.getMain().getDataStatus().getProjectData().getVehicle(
                scene.getMain().getDataStatus().getProjectData().getVehicleID(
                        scene.getVehicles().getSelectionModel().getSelectedItem(),
                        scene.getMain().getDataStatus().getProjectData().getVehicles()));
    }

    private static Optional<MissionStep> taskStep(VehicleScene scene) {
        var dialog = new Dialog<MissionStep>();
        dialog.setTitle(isEdit ? "Edit the tasks" : "Add the tasks");
        var buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(scene, dialog);
        controller();
        ok = (Button) dialog.getDialogPane().lookupButton(buttonType);
        ok.setDisable(!isEdit);
        return dialogResult(scene, dialog, buttonType);
    }

    private static void pane(VehicleScene scene, Dialog<MissionStep> dialog) {
        var pane = new GridPane();
        pane.setHgap(GAP);
        pane.setVgap(GAP);

        goalField1 = new ChoiceBox<>();
        goalField1.setPrefWidth(WIDTH);
        var choices = scene.getMain().getDataStatus().getProjectData().getPoses().keySet();
        List<String> choicesWithNull = new ArrayList<>(choices);
        choicesWithNull.add(0, null);
        goalField1.getItems().addAll(choices);

        goalField2 = new ChoiceBox<>();
        goalField2.setPrefWidth(WIDTH);
        goalField2.getItems().addAll(choicesWithNull);

        goalField3 = new ChoiceBox<>();
        goalField3.setPrefWidth(WIDTH);
        goalField3.getItems().addAll(choicesWithNull);

        goalField4 = new ChoiceBox<>();
        goalField4.setPrefWidth(WIDTH);
        goalField4.getItems().addAll(choicesWithNull);

        goalField5 = new ChoiceBox<>();
        goalField5.setPrefWidth(WIDTH);
        goalField5.getItems().addAll(choicesWithNull);

        if (isEdit) {
            var selectedMission = scene.getMissions().getSelectionModel().getSelectedItem();
            goalField1.setValue(selectedMission.replaceAll("[()]", "").split(", ")[0]);
        } else goalField1.setValue(choices.iterator().next());

        durationField = new TextField();
        durationField.setPrefWidth(WIDTH);
        if (isEdit) {
            var selectedMission = scene.getMissions().getSelectionModel().getSelectedItem();
            durationField.setText(selectedMission.replaceAll("[()]", "").split(", ")[1]);
        }

        pane.add(new Text("Name: "), 0, 0);
        pane.add(nameField, 1, 0);
        nameField.setPrefWidth(WIDTH);
        pane.add(new Text("Duration (min): "), 0, 1);
        pane.add(durationField, 1, 1);
        pane.add(new Text("Goal 1: "), 0, 2);
        pane.add(goalField1, 1, 2);
        pane.add(new Text("Goal 2: "), 0, 3);
        pane.add(goalField2, 1, 3);
        pane.add(new Text("Goal 3: "), 0, 4);
        pane.add(goalField3, 1, 4);
        pane.add(new Text("Goal 4: "), 0, 5);
        pane.add(goalField4, 1, 5);
        pane.add(new Text("Goal 5: "), 0, 6);
        pane.add(goalField5, 1, 6);
        pane.add(new Text("Priority: "), 0, 7);
        pane.add(priority, 1, 7);
        dialog.getDialogPane().setContent(pane);
    }

    private static void controller() {
        durationField.textProperty().addListener((observable, oldValue, newValue) -> ok.setDisable(!validateDouble(durationField)));
    }

    private static Optional<MissionStep> dialogResult(VehicleScene scene, Dialog<MissionStep> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && validateDouble(durationField)) {
                var missionStep = new MissionStep();
                missionStep.setPoseName(goalField1.getValue());
                missionStep.setDuration(Double.parseDouble(durationField.getText()));
                return missionStep;
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (isEdit) result.ifPresent(missionStep -> scene.getMissions().getItems().replaceAll(item ->
                item.equals(scene.getMissions().getSelectionModel().getSelectedItem()) ? formatMissionStep(missionStep) : item));
        else result.ifPresent(missionStep -> scene.getMissions().getItems().add(formatMissionStep(missionStep)));
        return result;
    }

    private static String formatMissionStep(MissionStep missionStep) {
        return "(" + missionStep.getPoseName() + ", " + missionStep.getDuration() + ")";
    }
}
