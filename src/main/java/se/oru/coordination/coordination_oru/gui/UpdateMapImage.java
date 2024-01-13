package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;

import java.util.ArrayList;

public class UpdateMapImage {
    protected static void drawMapMarkers(InteractiveMap interactiveMap) {
        drawMapMarkersWithSelection(interactiveMap, null);
    }

    protected static void drawMapMarkersWithSelection(InteractiveMap interactiveMap, String selectedPoseName) {
        var graphicsContext = interactiveMap.getCanvas().getGraphicsContext2D();
        var originX = 0;
        var originY = 0;
        graphicsContext.drawImage(interactiveMap.getImage(), originX, originY);
        drawMarkers(graphicsContext, interactiveMap.getMain().getDataStatus().getProjectData(), interactiveMap.getMap(), selectedPoseName);
    }

    private static void drawMarkers(GraphicsContext graphicsContext, ProjectData projectData, OccupancyMap map, String poseName) {
        for (var pose : new ArrayList<>(projectData.getPoses().values())) {
            var isSelectedPose = poseName != null && pose.equals(projectData.getPose(poseName));
            var markerColor = isSelectedPose ? Color.RED : Color.LIGHTGREEN;
            drawMarker(graphicsContext, map, pose, markerColor);
        }
    }

    private static void drawMarker(GraphicsContext graphicsContext, OccupancyMap map, Pose pose, Color color) {
        var markerSize = 10;
        var position = new Coordinate(pose.getX(), pose.getY());
        var pixel = map.convertToPixels(position);
        var x = (pixel[0] - markerSize / 2);
        var y = (pixel[1] - markerSize / 2);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(x, y, markerSize, markerSize);
    }
}
