package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.utils.Round;

import java.awt.*;
import java.util.List;

public class InteractiveMapDisplayWithMarkers {

    private final ListView<String> listView;
    protected final Button nextButton;
    protected final ProjectData projectData;
    protected final MapData mapData;
    protected final OccupancyMap occupancyMap;
    protected final Canvas canvas;
    protected Image image;

    public InteractiveMapDisplayWithMarkers(ProjectData projectData, MapData mapData, ListView<String> listView, Button nextButton) {
        this.projectData = projectData;
        this.mapData = mapData;
        this.listView = listView;
        this.nextButton = nextButton;
        this.occupancyMap = new OccupancyMap(projectData.getMap());
        this.canvas = new Canvas(occupancyMap.getPixelWidth(), occupancyMap.getPixelHeight());
    }

    public ScrollPane createMapInteractionNode() {
        var scrollPane = createScrollablePane("file:" + projectData.getMapImage(mapData));
        UpdateMapImage.drawMapMarkers(this);
        scrollPane.setContent(canvas);
        setupCanvasEventHandlers();

        getPoseList();
        return scrollPane;
    }

    private ScrollPane createScrollablePane(String imagePath) {
        var scrollPane = new ScrollPane();
        image = new Image(imagePath);
        return scrollPane;
    }

    private void setupCanvasEventHandlers() {
        canvas.setOnMouseReleased(event -> handleMouseReleased(event, occupancyMap));
        // Additional event handlers can be set up here
    }

    private void handleMouseReleased(MouseEvent event, OccupancyMap occupancyMap) {
        Point point = new Point((int) event.getX(), (int) event.getY());
        Coordinate position = occupancyMap.convertToWorldCoordinates(point.x, point.y);
        int decimalPlaces = 2;
        position.x = Round.round(position.x, decimalPlaces);
        position.y = Round.round(position.y, decimalPlaces);
        boolean occupancy = !occupancyMap.isOccupied(point.x, point.y);

        if (occupancy) { // FIXME: Simplify this if statement
            List<String> annotatedPose = AddLocationDialogBox.display(position.x, position.y);
            if (annotatedPose != null) {
                String poseName = annotatedPose.get(0);
                Pose pose = parsePose(annotatedPose);
                projectData.addPose(poseName, pose);
                UpdateMapImage.drawMapMarkersWithSelection(this, poseName);
                addPose(poseName);
            }
        } else {
            AlertBox.display("Adding Location Error", "You cannot add a location on an occupied cell.", Alert.AlertType.ERROR);
        }
    }

    private void addPose(String poseName) {
        listView.getItems().add(poseName); // FIXME: These three lines belong somewhere else
        listView.getSelectionModel().select(poseName);
        nextButton.setDisable(!(projectData.noOfPoses() >= 2));
    }

    private static Pose parsePose(List<String> annotatedPose) {
        var orientation = annotatedPose.get(1);
        var x = Double.parseDouble(annotatedPose.get(2));
        var y = Double.parseDouble(annotatedPose.get(3));
        double theta;
        switch (orientation) {
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
        return new Pose(x, y, theta);
    }

    public void getPoseList() {
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { // FIXME: listview does not belong here
            UpdateMapImage.drawMapMarkersWithSelection(this, newValue);
        });
    }
}
