package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import java.util.Optional;
import static se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;
import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;

public class MissionDialog {
    private static final int WIDTH = 150;
    private static final int GAP = 10;
    private static boolean isEdit = false;
    private static ChoiceBox<String> locationField = new ChoiceBox<>();
    private static TextField durationField = new TextField();
    private static Button ok = new Button();

    public static void add(SceneVehicle scene) {
        isEdit = false;
        var missionStep = missionStep(scene);
        missionStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var missionSteps = vehicle.getMission();
            missionSteps.add(step);
        });
    }

    public static void edit(SceneVehicle scene) {
        isEdit = true;
        var missionStep = missionStep(scene);
        missionStep.ifPresent(step -> {
            var vehicle = getVehicle(scene);
            var missionSteps = vehicle.getMission();
            missionSteps.add(step);
        });
    }

    private static ProjectData.Vehicle getVehicle(SceneVehicle scene) {
        return scene.getMain().getDataStatus().getProjectData().getVehicle(
                scene.getMain().getDataStatus().getProjectData().getVehicleID(
                        scene.getVehicles().getSelectionModel().getSelectedItem(),
                        scene.getMain().getDataStatus().getProjectData().getVehicles()));
    }

    private static Optional<MissionStep> missionStep(SceneVehicle scene) {
        var dialog = new Dialog<MissionStep>();
        dialog.setTitle(isEdit ? "Edit a mission step" : "Add a mission step");
        var buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(scene, dialog);
        controller();
        ok = (Button) dialog.getDialogPane().lookupButton(buttonType);
        ok.setDisable(!isEdit);
        return dialogResult(scene, dialog, buttonType);
    }

    private static void pane(SceneVehicle scene, Dialog<MissionStep> dialog) {
        var pane = new GridPane();
        pane.setHgap(GAP);
        pane.setVgap(GAP);

        locationField = new ChoiceBox<>();
        locationField.setPrefWidth(WIDTH);
        var choices = scene.getMain().getDataStatus().getProjectData().getPoses().keySet();
        locationField.getItems().addAll(choices);

        if (isEdit) {
            var selectedMission = scene.getMissions().getSelectionModel().getSelectedItem();
            locationField.setValue(selectedMission.replaceAll("[()]", "").split(", ")[0]);
        } else locationField.setValue(choices.iterator().next());

        durationField = new TextField();
        durationField.setPrefWidth(WIDTH);
        if (isEdit) {
            var selectedMission = scene.getMissions().getSelectionModel().getSelectedItem();
            durationField.setText(selectedMission.replaceAll("[()]", "").split(", ")[1]);
        }

        pane.add(new Text("Location: "), 0, 0);
        pane.add(locationField, 1, 0);
        pane.add(new Text("Duration (min): "), 0, 1);
        pane.add(durationField, 1, 1);

        dialog.getDialogPane().setContent(pane);
    }

    private static void controller() {
        durationField.textProperty().addListener((observable, oldValue, newValue) -> ok.setDisable(!validateDouble(durationField)));
    }

    private static Optional<MissionStep> dialogResult(SceneVehicle scene, Dialog<MissionStep> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType && validateDouble(durationField)) {
                var missionStep = new MissionStep();
                missionStep.setPoseName(locationField.getValue());
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
