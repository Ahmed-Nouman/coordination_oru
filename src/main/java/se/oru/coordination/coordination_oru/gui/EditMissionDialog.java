package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.Optional;

import static se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;
import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;

public class EditMissionDialog {
    private static final int WIDTH = 150;
    private static final int GAP = 10;
    private static ChoiceBox<String> locationField = new ChoiceBox<>();
    private static TextField durationField = new TextField();
    private static Button ok = new Button();

    public static void edit(SceneVehicle scene) {
        var missionStep =  missionStep(scene).orElse(null);
        var vehicle = scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(scene.getVehicles().getSelectionModel().getSelectedItem(), scene.getMain().getDataStatus().getProjectData().getVehicles()));
        var missionSteps = vehicle.getMission();
        missionSteps.add(missionStep);
    }

    private static Optional<MissionStep> missionStep(SceneVehicle scene) {
        return dialog(scene);
    }

    private static Optional<MissionStep> dialog(SceneVehicle scene) {
        var dialog = new Dialog<MissionStep>();
        dialog.setTitle("Edit a mission step");
        var buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(scene, dialog);
        controller();
        ok = (Button) dialog.getDialogPane().lookupButton(buttonType);
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
        locationField.setValue(scene.getMissions().getSelectionModel().getSelectedItem().replaceAll("[()]", "").split(", ")[0]);

        durationField = new TextField();
        durationField.setPrefWidth(WIDTH);
        durationField.setText(scene.getMissions().getSelectionModel().getSelectedItem().replaceAll("[()]", "").split(", ")[1]);

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
        result.ifPresent(missionStep -> {
            scene.getMissions().getItems().remove(scene.getMissions().getSelectionModel().getSelectedItem());
            scene.getMissions().getItems().add("(" + missionStep.getPoseName() + ", " + missionStep.getDuration() + ")");
        });
        return result;
    }
}