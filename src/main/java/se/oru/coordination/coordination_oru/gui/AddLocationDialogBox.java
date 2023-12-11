package se.oru.coordination.coordination_oru.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class AddLocationDialogBox {

    public static String display() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Location");

        ButtonType addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(addButtonType);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);

        TextField nameTextField = new TextField();
        nameTextField.setPromptText("Location name");

        hBox.getChildren().addAll(new Text("Name:"), nameTextField);
        dialog.getDialogPane().setContent(hBox);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return nameTextField.getText();
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}