package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.scene.control.Alert;

public class AlertBox {
    public static void display(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}