package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ImageViewExample extends Application {

    @Override
    public void start(Stage stage) {
        // Load an image
        Image image = new Image("/home/ra2/mine-map-paper-2023.png"); // Replace with your image path
        ImageView imageView = new ImageView(image);

        // Create a StackPane to overlay the Circle on the ImageView
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(imageView);

        // Create a Circle and position it
        Circle circle = new Circle(100, 100, 20, Color.TRANSPARENT);
        circle.setStroke(Color.RED);

        // Add the Circle to the StackPane
        stackPane.getChildren().add(circle);

        // Create a Scene with the StackPane and show it on the stage
        Scene scene = new Scene(stackPane, image.getWidth(), image.getHeight());
        stage.setScene(scene);
        stage.setTitle("ImageView with Circle Overlay");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
