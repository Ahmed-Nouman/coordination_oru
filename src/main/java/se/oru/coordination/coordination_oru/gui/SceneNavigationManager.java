package se.oru.coordination.coordination_oru.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigationManager {
    private Stage primaryStage;

    public SceneNavigationManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void switchScene(Scene newScene, SceneController controller) {
        controller.setupScene();  // Call setup for the new scene's controller
        primaryStage.setScene(newScene);  // Switch to the new scene
        primaryStage.show();
    }
}
