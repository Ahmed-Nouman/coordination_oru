package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.ArrayList;
import java.util.List;

public class EditLocationDialog {
    public static final int WIDTH = 120;
    public static final int GAP = 10;
    public static final int SPACING = 30;
    private static final TextField nameField = new TextField();
    private static final TextField xPositionField = new TextField();
    private static final TextField yPositionField = new TextField();
    private static final TextField orientationField = new TextField();
    private static Button edit = new Button();

    public static List<String> edit(SceneMap scene, String poseName) {
        var pose = scene.getMain().getDataStatus().getProjectData().getPoses().get(poseName);
        return location(poseName, pose);
    }

    private static List<String> location(String poseName, Pose pose) {
        return dialog(poseName, pose).showAndWait().orElse(null);
    }

    private static Dialog<List<String>> dialog(String poseName, Pose pose) {
        var dialog = new Dialog<List<String>>();
        dialog.setTitle("Edit Pose");
        var buttonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(dialog, poseName, pose);
        controller();
        dialogResult(dialog, buttonType);
        edit = (Button) dialog.getDialogPane().lookupButton(buttonType);
        return dialog;
    }

    private static void pane(Dialog<List<String>> dialog, String poseName, Pose pose) {
        var pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(SPACING));
        pane.setHgap(GAP);
        pane.setVgap(GAP);

        var name = new Text("Name:");
        GridPane.setConstraints(name, 0, 0);
        nameField.setPrefWidth(WIDTH);
        GridPane.setConstraints(nameField, 1, 0);
        nameField.setText(poseName);

        var orientation = new Text("Orientation (deg):");
        GridPane.setConstraints(orientation, 0, 1);
        orientationField.setPrefWidth(WIDTH);
        GridPane.setConstraints(orientationField, 1, 1);
        orientationField.setText(String.valueOf(Math.toDegrees(pose.getTheta())));

        var xPosition = new Text("X Position (m):");
        GridPane.setConstraints(xPosition, 0, 2);
        xPositionField.setPrefWidth(WIDTH);
        GridPane.setConstraints(xPositionField, 1, 2);
        xPositionField.setText(String.valueOf(pose.getX()));

        var yPosition = new Text("Y Position (m):");
        GridPane.setConstraints(yPosition, 0, 3);
        yPositionField.setPrefWidth(WIDTH);
        GridPane.setConstraints(yPositionField, 1, 3);
        yPositionField.setText(String.valueOf(pose.getY()));

        pane.getChildren().addAll(name, nameField, orientation, orientationField, xPosition, xPositionField, yPosition, yPositionField);
        dialog.getDialogPane().setContent(pane);
    }

    private static void controller() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> checkNameValidity(newValue));
        xPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkXPositionValidity(newValue));
        yPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkYPositionValidity(newValue));
    }

    private static void checkNameValidity(String newValue) {
        boolean isNameEmpty = newValue.trim().isEmpty();
        boolean isXPositionInvalid = isPositionInvalid(xPositionField.getText());
        boolean isYPositionInvalid = isPositionInvalid(yPositionField.getText());
        edit.setDisable(isNameEmpty || isXPositionInvalid || isYPositionInvalid);
    }

    private static boolean isPositionInvalid(String text) {
        if (text.isEmpty()) return true;
        try {
            return Double.parseDouble(text) <= 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static void checkXPositionValidity(String text) {
        xPositionField.setStyle("-fx-border-color: none");
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isXPositionInvalid = isPositionInvalid(text);
        boolean isYPositionInvalid = isPositionInvalid(yPositionField.getText());
        edit.setDisable(isNameEmpty || isXPositionInvalid || isYPositionInvalid);
        if (isXPositionInvalid) xPositionField.setStyle("-fx-border-color: red");
    }

    private static void checkYPositionValidity(String text) {
        yPositionField.setStyle("-fx-border-color: none");
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isXPositionInvalid = isPositionInvalid(xPositionField.getText());
        boolean isYPositionInvalid = isPositionInvalid(text);
        edit.setDisable(isNameEmpty || isXPositionInvalid || isYPositionInvalid);
        if (isYPositionInvalid) yPositionField.setStyle("-fx-border-color: red");
    }

    private static void dialogResult(Dialog<List<String>> dialog, ButtonType buttonType) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonType) {
                var resultDialog = new ArrayList<String>();
                resultDialog.add(nameField.getText());
                resultDialog.add(orientationField.getText());
                resultDialog.add(xPositionField.getText());
                resultDialog.add(yPositionField.getText());
                return resultDialog;
            }
            return null;
        });
    }
}