package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapInteract extends StackPane {

    private final List<Point> markers = new ArrayList<>();
    private final OccupancyMap occupancyMap;
    private final ProjectData projectData;
    private final Button button;
    private final Button saveButton;
    private final Canvas canvas;

    public MapInteract(ProjectData projectData, Button button) {
        this.projectData = projectData;
        this.button = button;
        this.occupancyMap = new OccupancyMap(projectData.getMap());

        // Canvas to show the map
        this.canvas = new Canvas(occupancyMap.getPixelWidth(), occupancyMap.getPixelHeight());

        // Save and close button
        saveButton = new Button("Save and Close");
        saveButton.setDisable(true);
        VBox layout = new VBox(saveButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        // No need to save data as it is being handled in the AddLocationDialogBox, so just close the window
        saveButton.setOnAction(e -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });

        this.getChildren().addAll(canvas, layout);

        // Allow the user interaction with the map outside the bound of the save button
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (!saveButton.getBoundsInParent().contains(event.getX(), event.getY())) {
                handleMouseReleased(event);
            }
        });

        setupEventHandlers();

        Stage stage = new Stage();
        stage.setTitle("Select the key location in the map: " + projectData.getMap());
        stage.setScene(new Scene(this));
        stage.show();

        // Add a handler for the close request
        String title = "Closing the Map";
        String message = "Are you sure you want to close the map? All the saved locations will be lost.";
        stage.setOnCloseRequest(event -> {
            Optional<ButtonType> result = AlertBox.display(title, message, Alert.AlertType.CONFIRMATION);
            if (result.isPresent() && result.get() == ButtonType.NO) {
                event.consume();
            }
            projectData.getPoses().clear();
        });

        drawMap();
    }

    private void setupEventHandlers() {
        canvas.setOnMouseReleased(this::handleMouseReleased);
        // Add other event handlers if needed
    }

    private Image convertBufferedImageToFXImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;
                Color color = Color.rgb(r, g, b, a / 255.0);
                pixelWriter.setColor(x, y, color);
            }
        }

        return writableImage;
    }

    private void drawMap() {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        Image javaFXImage;

        // Draw the map
        boolean isBinaryImage = false;
        if (!isBinaryImage) {
            javaFXImage = convertBufferedImageToFXImage(occupancyMap.asBufferedImage());
        } else {
            javaFXImage = convertBufferedImageToFXImage(occupancyMap.asThresholdedBufferedImage());
        }

        graphicsContext.drawImage(javaFXImage, 0, 0);

        // Draw markers
        for (Point marker : markers) {
            drawMarker(graphicsContext, marker);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        Point point = new Point((int) event.getX(), (int) event.getY());
        Coordinate position = occupancyMap.convertToWorldCoordinates(point.x, point.y);
        position.x = Round.round(position.x, 2);
        position.y = Round.round(position.y, 2);
        boolean occupancy = !occupancyMap.isOccupied(point.x, point.y);

        System.out.println("--");
        System.out.println("Pixel (x,y) = (" + point.x + "," + point.y + ")");
        System.out.println("Position (x,y) = (" + position.x + "," + position.y + ")");
        System.out.println("Occupancy map bit: " + occupancyMap.asByteArray()[(point.y)*occupancyMap.getPixelWidth()/8+(point.x)/8]);
        System.out.println("Occupancy value: " + occupancyMap.getOccupancyValue(point.x, point.y));
        System.out.println("State: " + (occupancyMap.isOccupied(point.x,point.y) ? "occupied" : "free") + " (threshold is " + occupancyMap.getThreshold() + ")");

        if (occupancy) {
            List<String> pose = AddLocationDialogBox.display(position.x, position.y); // Pose is a list of strings of name, orientation, x, y
            if (pose != null) {
                double theta;
                switch (pose.get(1)) {
                    case "DOWN":
                        theta = 3 * Math.PI / 2;
                        break;
                    case "DOWN_RIGHT":
                        theta = 7 * Math.PI / 4;
                        break;
                    case "DOWN_LEFT":
                        theta = 5 * Math.PI / 4;
                        break;
                    case "LEFT":
                        theta = Math.PI;
                        break;
                    case "UP_LEFT":
                        theta = 3 * Math.PI / 4;
                        break;
                    case "UP":
                        theta = Math.PI / 2;
                        break;
                    case "UP_RIGHT":
                        theta = Math.PI / 4;
                        break;
                    default:
                        theta = 0;
                        break;
                }
                projectData.addPose(pose.get(0), new Pose(Double.parseDouble(pose.get(2)), Double.parseDouble(pose.get(3)), theta));
                markers.add(point); // Marker is still plotted at the point of clicking. This is intentional.
                drawMap();
                if (projectData.getPoses().size() >= 2) saveButton.setDisable(false); // We can allow the user to save and close if there are at least two poses
                if (projectData.getPoses().size() >= 2) button.setDisable(false); // We can stop fetching points if there are at least two poses
            }
        } else {
            AlertBox.display("Adding Location Error", "You cannot add a location on an occupied cell.", Alert.AlertType.ERROR);
        }
    }

    private void drawMarker(GraphicsContext graphicsContext, Point point) {
        int markerSize = 10;
        int x = point.x - markerSize / 2;
        int y = point.y - markerSize / 2;
        graphicsContext.setFill(Color.LIGHTGREEN);
        graphicsContext.fillRect(x, y, markerSize, markerSize);
    }

}
