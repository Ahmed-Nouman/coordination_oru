package se.oru.coordination.coordination_oru.gui;

import javafx.application.Platform;
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

    public static List<String> add(double x, double y) {
        return new LocationDialogInstance("Add Pose", "Add", "Name", x, y, 0).showAndWait();
    }

    public static List<String> edit(MapScene scene, String poseName) {
        var pose = scene.getMain().getDataStatus().getProjectData().getPoses().get(poseName);
        return new LocationDialogInstance("Edit Pose", "OK", poseName, pose.getX(), pose.getY(), Math.toDegrees(pose.getTheta())).showAndWait();
    }

    private static class LocationDialogInstance {
        private final String title;
        private final String buttonText;
        private final TextField nameField = new TextField();
        private final TextField xPositionField = new TextField();
        private final TextField yPositionField = new TextField();
        private final TextField orientationField = new TextField();
        private Button action;

        public LocationDialogInstance(String title, String buttonText, String poseName, double x, double y, double theta) {
            this.title = title;
            this.buttonText = buttonText;
            setupFields(poseName, x, y, theta);
        }

        private void setupFields(String poseName, double x, double y, double theta) {
            nameField.setText(poseName);
            nameField.selectAll();
            xPositionField.setText(String.valueOf(x));
            yPositionField.setText(String.valueOf(y));
            orientationField.setText(String.valueOf(theta));
        }

        public List<String> showAndWait() {
            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle(title);
            ButtonType buttonType = new ButtonType(buttonText, ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(buttonType);

            var pane = createPane();
            dialog.getDialogPane().setContent(pane);
            setupController();

            action = (Button) dialog.getDialogPane().lookupButton(buttonType);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonType) {
                    return getResult();
                }
                return null;
            });
            Platform.runLater(nameField::requestFocus);
            Platform.runLater(nameField::selectAll);
            return dialog.showAndWait().orElse(null);
        }

        private GridPane createPane() {
            GridPane pane = new GridPane();
            pane.setAlignment(Pos.CENTER);
            pane.setPadding(new Insets(SPACING));
            pane.setHgap(GAP);
            pane.setVgap(GAP);

            addField(pane, "Name:", nameField, 0);
            addField(pane, "Orientation (deg):", orientationField, 1);
            addField(pane, "X Position (m):", xPositionField, 2);
            addField(pane, "Y Position (m):", yPositionField, 3);

            return pane;
        }

        private void addField(GridPane pane, String labelText, TextField field, int row) {
            Text label = new Text(labelText);
            GridPane.setConstraints(label, 0, row);
            field.setPrefWidth(WIDTH);
            GridPane.setConstraints(field, 1, row);
            pane.getChildren().addAll(label, field);
        }

        private void setupController() {
            nameField.textProperty().addListener((observable, oldValue, newValue) -> checkValidity());
            orientationField.textProperty().addListener((observable, oldValue, newValue) -> checkValidity());
            xPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkValidity());
            yPositionField.textProperty().addListener((observable, oldValue, newValue) -> checkValidity());
        }

        private void checkValidity() {
            boolean isNameEmpty = nameField.getText().trim().isEmpty();
            boolean isOrientationValid = isValid(orientationField.getText());
            boolean isXPositionValid = isValid(xPositionField.getText());
            boolean isYPositionValid = isValid(yPositionField.getText());
            action.setDisable(isNameEmpty || !isOrientationValid || !isXPositionValid || !isYPositionValid);
        }

        private boolean isValid(String text) {
            if (text.isEmpty()) return false;
            try {
                return Double.parseDouble(text) >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private List<String> getResult() {
            List<String> result = new ArrayList<>();
            result.add(nameField.getText());
            result.add(orientationField.getText());
            result.add(xPositionField.getText());
            result.add(yPositionField.getText());
            return result;
        }
    }
}
