package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.scene.control.Alert;

public class DialogBox {
    public static void display(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}