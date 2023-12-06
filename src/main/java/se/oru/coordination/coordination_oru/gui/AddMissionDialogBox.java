package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.Set;

import static se.oru.coordination.coordination_oru.gui.Utils.validateDouble;
import static se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;

public class AddMissionDialogBox {

    public static MissionStep display(String title, Set<String> choices, ListView<String> listView) {

        // Create the custom dialog
        Dialog<MissionStep> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Select a location and stopping duration: ");

        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create the choice box and text field
        ChoiceBox<String> location = new ChoiceBox<>();
        location.setPrefWidth(150);
        TextField duration = new TextField();
        duration.setPrefWidth(location.getPrefWidth());

        // Add choices to the choice box
        location.getItems().addAll(choices);
        location.setValue(choices.iterator().next());
        location.setPrefWidth(duration.getPrefWidth());

        // Layout the dialog components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Location: "), 0, 0);
        grid.add(location, 1, 0);
        grid.add(new Label("Duration (min): "), 0, 1);
        grid.add(duration, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Validation for the duration field
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);
        duration.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!validateDouble(duration));
        });

        // Convert the result to a pair when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType && validateDouble(duration)) {
                MissionStep missionStep = new MissionStep();
                missionStep.setPoseName(location.getValue());
                missionStep.setDuration(Double.parseDouble(duration.getText()));
                return missionStep;
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<MissionStep> result = dialog.showAndWait();
        result.ifPresent(choiceAndText -> {
            listView.getItems().add("(" + choiceAndText.getPoseName() + ", " + choiceAndText.getDuration() + ")");
        });
        return result.orElse(null);
    }
}
