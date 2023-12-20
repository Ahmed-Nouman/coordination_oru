package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MapInteract {

    private final List<Pose> markers = new ArrayList<>();
    private final ProjectData projectData;
    private final MapData mapData;
    private final ListView<String> listView;
    private final OccupancyMap occupancyMap;
    private final Canvas canvas;
    private Image image;
    private final Button nextButton;

    public MapInteract(ProjectData projectData, MapData mapData, ListView<String> listView, Button nextButton) {
        this.projectData = projectData;
        this.mapData = mapData;
        this.listView = listView;
        this.nextButton = nextButton;
        this.occupancyMap = new OccupancyMap(projectData.getMap());
        this.canvas = new Canvas(occupancyMap.getPixelWidth(), occupancyMap.getPixelHeight());
        markers.addAll(projectData.getPoses().values());
    }

    public ScrollPane createMapInteractionNode() {

        // Create a ScrollPane to contain the Canvas that will be used to display the map and markers
        ScrollPane scrollPane = new ScrollPane();
        image = new Image("file:" + projectData.getMapImage(mapData));
        drawMap(null);
        scrollPane.setContent(canvas);

        // Setup event handlers and other functionalities
        setupCanvasEventHandlers(canvas, occupancyMap);

        // Set up the list view to display the active marker with red color
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            drawMap(newValue); // Redraw the map with the selected item
        });

        // Return the ScrollPane for integration into the BorderPane
        return scrollPane;
    }

    private void setupCanvasEventHandlers(Canvas canvas, OccupancyMap occupancyMap) {
        canvas.setOnMouseReleased(event -> handleMouseReleased(event, occupancyMap));
        // Additional event handlers can be set up here
    }

    private void handleMouseReleased(MouseEvent event, OccupancyMap occupancyMap) {
        Point point = new Point((int) event.getX(), (int) event.getY());
        Coordinate position = occupancyMap.convertToWorldCoordinates(point.x, point.y);
        position.x = Round.round(position.x, 2);
        position.y = Round.round(position.y, 2);
        boolean occupancy = !occupancyMap.isOccupied(point.x, point.y);

        if (occupancy) {
            List<String> annotatedPose = AddLocationDialogBox.display(position.x, position.y); // Pose is a list of strings of name, orientation, x, y
            if (annotatedPose != null) {
                String poseName = annotatedPose.get(0);
                Pose pose = getPose(annotatedPose);
                projectData.addPose(poseName, pose);
                markers.add(pose); // Marker is still plotted at the point of clicking. This is intentional.
                drawMap(poseName);
                listView.getItems().add(annotatedPose.get(0));
                listView.getSelectionModel().select(annotatedPose.get(0));
                checkSizePoses();
            }
        } else {
            AlertBox.display("Adding Location Error", "You cannot add a location on an occupied cell.", Alert.AlertType.ERROR);
        }
    }

    private void checkSizePoses() {
        nextButton.setDisable(projectData.getPoses().size() < 2);
    }

    private static Pose getPose(List<String> pose) {
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
        return new Pose(Double.parseDouble(pose.get(2)), Double.parseDouble(pose.get(3)), theta);
    }

    private void drawMap(String selectedPoseName) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.drawImage(image, 0, 0);
        drawMarkers(graphicsContext, selectedPoseName);
    }

    private void drawMarkers(GraphicsContext graphicsContext, String selectedPoseName) {
        for (Pose marker : markers) {
            Color color = Color.LIGHTGREEN; // Default color is green
            if (projectData.getPoses().containsKey(selectedPoseName) && marker.equals(projectData.getPoses().get(selectedPoseName))) {
                color = Color.RED; // Change to red if it's the selected pose
            }
            drawMarker(graphicsContext, marker, color);
        }
    }


    private void drawMarker(GraphicsContext graphicsContext, Pose pose, Color color) {
        int markerSize = 10;
        var position = new Coordinate(pose.getX(), pose.getY());
        var pixel = occupancyMap.convertToPixels(position);
        int x = (pixel[0] - markerSize / 2);
        int y = (pixel[1] - markerSize / 2);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(x, y, markerSize, markerSize);
    }
}
