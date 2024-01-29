package se.oru.coordination.coordination_oru.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class InteractiveMap {

    private static final int DECIMAL_PLACES = 2;
    private final OccupancyMap map;
    private final Canvas canvas;
    private Image image;
    private final ControllerMap controller;
    private final Main main;

    public InteractiveMap(Main main, ControllerMap controller) {
        this.main = main;
        this.controller = controller;
        this.map = new OccupancyMap(main.getDataStatus().getProjectData().getMap());
        this.canvas = new Canvas(map.getPixelWidth(), map.getPixelHeight());
    }

    public ScrollPane createInteractiveMapNode() {
        var scrollPane = createScrollablePane("file:" + main.getDataStatus().getProjectData().getMapImage(main.getDataStatus().getMapData()));
        UpdateMapImage.drawMapMarkers(this);
        scrollPane.setContent(canvas);
        setupCanvasEventHandlers();
        controller.getPoseList(this);
        return scrollPane;
    }

    private ScrollPane createScrollablePane(String imagePath) {
        var scrollPane = new ScrollPane();
        image = new Image(imagePath);
        return scrollPane;
    }

    private void setupCanvasEventHandlers() {
        canvas.setOnMouseReleased(this::handleMouseReleased);
        // Additional event handlers can be set up here
    }

    private void handleMouseReleased(MouseEvent event) {
        var point = new Point((int) event.getX(), (int) event.getY());
        var position = map.convertToWorldCoordinates(point.x, point.y);
        position.x = Round.round(position.x, DECIMAL_PLACES);
        position.y = Round.round(position.y, DECIMAL_PLACES);
        boolean occupancy = !map.isOccupied(point.x, point.y);

        if (occupancy) { //FIXME: Simplify this if statement. Maybe?
            var annotatedPose = LocationDialog.add(position.x, position.y);
            if (annotatedPose != null) {
                var poseName = annotatedPose.get(0);
                var pose = parsePose(annotatedPose);
                main.getDataStatus().getProjectData().addPose(poseName, pose);
                UpdateMapImage.drawMapMarkersWithSelection(this, poseName);
                controller.addPose(poseName);
                controller.updateLocations();
            }
        } else {
            AlertBox.display("Adding Location Error", "You cannot add a location on an occupied cell.", Alert.AlertType.ERROR);
        }
    }

    private static Pose parsePose(List<String> annotatedPose) {
        var theta = Math.toRadians(Double.parseDouble(annotatedPose.get(1)));
        var x = Double.parseDouble(annotatedPose.get(2));
        var y = Double.parseDouble(annotatedPose.get(3));
        return new Pose(x, y, theta);
    }

    public OccupancyMap getMap() {
        return map;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Image getImage() {
        return image;
    }

    public Main getMain() {
        return main;
    }
}
