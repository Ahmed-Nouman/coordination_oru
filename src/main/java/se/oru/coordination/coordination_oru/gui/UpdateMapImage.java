package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;

import java.util.ArrayList;

public class UpdateMapImage {
    protected static void drawMapMarkers(InteractiveMapDisplayWithMarkers interactiveMapDisplayWithMarkers) {
        drawMapMarkersWithSelection(interactiveMapDisplayWithMarkers, null);
    }

    protected static void drawMapMarkersWithSelection(InteractiveMapDisplayWithMarkers interactiveMapDisplayWithMarkers, String selectedPoseName) {
        GraphicsContext graphicsContext = interactiveMapDisplayWithMarkers.canvas.getGraphicsContext2D();
        int originX = 0;
        int originY = 0;
        graphicsContext.drawImage(interactiveMapDisplayWithMarkers.image, originX, originY);
        drawMarkers(graphicsContext, interactiveMapDisplayWithMarkers.projectData, interactiveMapDisplayWithMarkers.occupancyMap, selectedPoseName);
    }

    private static void drawMarkers(GraphicsContext graphicsContext, ProjectData projectData, OccupancyMap occupancyMap, String selectedPoseName) {
        for (Pose pose : new ArrayList<>(projectData.getPoses().values())) {
            boolean isSelectedPose = selectedPoseName != null && pose.equals(projectData.getPose(selectedPoseName));
            Color markerColor = isSelectedPose ? Color.RED : Color.LIGHTGREEN;
            drawMarker(graphicsContext, occupancyMap, pose, markerColor);
        }
    }

    private static void drawMarker(GraphicsContext graphicsContext, OccupancyMap occupancyMap, Pose pose, Color color) {
        int markerSize = 10;
        var position = new Coordinate(pose.getX(), pose.getY());
        var pixel = occupancyMap.convertToPixels(position);
        int x = (pixel[0] - markerSize / 2);
        int y = (pixel[1] - markerSize / 2);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(x, y, markerSize, markerSize);
    }
}
