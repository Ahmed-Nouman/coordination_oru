package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AddLocationDialogBox {

    public static List<String> display(double x, double y) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Add Pose");

        var addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(addButtonType);

        var gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(30));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        var nameText = new Text("Name:");
        GridPane.setConstraints(nameText, 0, 0);
        var nameTextField = new TextField();
        nameTextField.setPrefWidth(120);
        GridPane.setConstraints(nameTextField, 1, 0);
        nameTextField.setPromptText("Location name");

        var orientationText = new Text("Orientation (rad):");
        GridPane.setConstraints(orientationText, 0, 1);
        ChoiceBox<String> orientationChoiceBox = new ChoiceBox<>();
        orientationChoiceBox.getItems().addAll("UP", "UP_RIGHT", "RIGHT", "DOWN_RIGHT", "DOWN", "DOWN_LEFT", "LEFT", "UP_LEFT");
        orientationChoiceBox.setValue(orientationChoiceBox.getItems().stream().findFirst().orElse(null));
        GridPane.setConstraints(orientationChoiceBox, 1, 1);

        var xPositionText = new Text("X Position (m):");
        GridPane.setConstraints(xPositionText, 0, 2);
        var xPositionTextField = new TextField();
        xPositionTextField.setPrefWidth(nameTextField.getPrefWidth());
        GridPane.setConstraints(xPositionTextField, 1, 2);
        xPositionTextField.setText(String.valueOf(x));
        xPositionTextField.setPromptText("Location name");

        var yPositionText = new Text("Y Position (m):");
        GridPane.setConstraints(yPositionText, 0, 3);
        var yPositionTextField = new TextField();
        yPositionTextField.setPrefWidth(nameTextField.getPrefWidth());
        GridPane.setConstraints(yPositionTextField, 1, 3);
        yPositionTextField.setText(String.valueOf(y));

        gridPane.getChildren().addAll(nameText, nameTextField, orientationText, orientationChoiceBox, xPositionText, xPositionTextField, yPositionText, yPositionTextField);
        dialog.getDialogPane().setContent(gridPane);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> addButton.setDisable(newValue.trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                List<String> resultDialog = new ArrayList<>();
                resultDialog.add(nameTextField.getText());
                resultDialog.add(orientationChoiceBox.getValue());
                resultDialog.add(xPositionTextField.getText());
                resultDialog.add(yPositionTextField.getText());
                return resultDialog;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}