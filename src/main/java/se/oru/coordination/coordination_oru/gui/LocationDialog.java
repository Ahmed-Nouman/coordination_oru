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

import java.util.ArrayList;
import java.util.List;

public class LocationDialog {
    private static final int WIDTH = 120;
    private static final int GAP = 10;
    private static final int SPACING = 30;
    private static String title = "";
    private static String buttonText = "";
    private static final TextField nameField = new TextField();
    private static final TextField xPositionField = new TextField();
    private static final TextField yPositionField = new TextField();
    private static final TextField orientationField = new TextField();
    private static Button action = new Button();

    public static List<String> add(double x, double y) {
        title = "Add Pose";
        buttonText = "Add";
        return showLocationDialog("PoseName", x, y, 0);
    }

    public static List<String> edit(SceneMap scene, String poseName) {
        title = "Edit Pose";
        buttonText = "OK";
        var pose = scene.getMain().getDataStatus().getProjectData().getPoses().get(poseName);
        return showLocationDialog(poseName, pose.getX(), pose.getY(), Math.toDegrees(pose.getTheta()));
    }

    private static List<String> showLocationDialog(String poseName, double x, double y, double theta) {
        var dialog = new Dialog<List<String>>();
        dialog.setTitle(title);
        var buttonType = new ButtonType(buttonText, ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonType);
        pane(dialog, poseName, x, y, theta);
        controller();
        dialogResult(dialog, buttonType);
        action = (Button) dialog.getDialogPane().lookupButton(buttonType);
        return dialog.showAndWait().orElse(null);
    }

    private static void pane(Dialog<List<String>> dialog, String poseName, double x, double y, double theta) {
        var pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(SPACING));
        pane.setHgap(GAP);
        pane.setVgap(GAP);

        field(nameField, "Name:", poseName, 0, pane);
        field(orientationField, "Orientation (deg):", String.valueOf(theta), 1, pane);
        field(xPositionField, "X Position (m):", String.valueOf(x), 2, pane);
        field(yPositionField, "Y Position (m):", String.valueOf(y), 3, pane);

        dialog.getDialogPane().setContent(pane);
    }

    private static void field(TextField field, String labelText, String value, int row, GridPane pane) {
        var label = new Text(labelText);
        GridPane.setConstraints(label, 0, row);
        field.setPrefWidth(WIDTH);
        GridPane.setConstraints(field, 1, row);
        field.setText(value);
        pane.getChildren().addAll(label, field);
    }

    private static void controller() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> checkNameValidity(newValue));
        orientationField.textProperty().addListener((observable, oldValue, newValue) -> checkOrientationValidity(newValue));
        xPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkXPositionValidity(newValue));
        yPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkYPositionValidity(newValue));
    }

    private static void checkNameValidity(String newValue) {
        boolean isNameEmpty = newValue.trim().isEmpty();
        boolean isOrientationValid = isValid(orientationField.getText());
        boolean isXPositionValid = isValid(xPositionField.getText());
        boolean isYPositionValid = isValid(yPositionField.getText());
        action.setDisable(isNameEmpty || !isOrientationValid || !isXPositionValid || !isYPositionValid);
    }

    public static boolean isValid(String text) {
        if (text.isEmpty()) return false;
        try {
            return Double.parseDouble(text) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void checkOrientationValidity(String text) {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isOrientationValid = isValid(text);
        boolean isXPositionValid = isValid(xPositionField.getText());
        boolean isYPositionValid = isValid(yPositionField.getText());
        action.setDisable(isNameEmpty || !isOrientationValid || !isXPositionValid || !isYPositionValid);
    }

    private static void checkXPositionValidity(String text) {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isOrientationValid = isValid(orientationField.getText());
        boolean isXPositionValid = isValid(text);
        boolean isYPositionValid = isValid(yPositionField.getText());
        action.setDisable(isNameEmpty || !isOrientationValid || !isXPositionValid || !isYPositionValid);
    }

    private static void checkYPositionValidity(String text) {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isOrientationValid = isValid(orientationField.getText());
        boolean isXPositionValid = isValid(xPositionField.getText());
        boolean isYPositionValid = isValid(text);
        action.setDisable(isNameEmpty || isOrientationValid || isXPositionValid || isYPositionValid);
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