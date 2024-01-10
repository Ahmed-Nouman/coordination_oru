package se.oru.coordination.coordination_oru.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.HashMap;
import java.util.Map;

public class MapDisplayWithMarkers extends StackPane {
    private static final int MARKER_RADIUS = 8;
    private static final int FONT_SIZE = 12;
    private final Canvas canvas;
    private final Image mapImage;
    private final Map<String, Pose> poses;
    private final double scaleImage;
    private final double originalImageWidth;
    private final double originalImageHeight;

    public MapDisplayWithMarkers(String imagePath, Map<String, Pose> poses, double mapResolution, double newWidth, double newHeight) {
        mapImage = new Image(imagePath);
        scaleImage = 1 / mapResolution;
        originalImageWidth = mapImage.getWidth();
        originalImageHeight = mapImage.getHeight();
        canvas = new Canvas(newWidth, newHeight);
        this.getChildren().add(canvas);
        this.poses = poses;
        drawCanvas();
    }

    private void drawCanvas() {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        double scaleX = canvas.getWidth() / originalImageWidth;
        double scaleY = canvas.getHeight() / originalImageHeight;

        int originX = 0;
        int originY = 0;
        graphicsContext.clearRect(originX, originY, canvas.getWidth(), canvas.getHeight());
        graphicsContext.drawImage(mapImage, originX, originY, canvas.getWidth(), canvas.getHeight());

        for (Map.Entry<String, Pose> point : poses.entrySet()) {
            double x = point.getValue().getX() * scaleX * scaleImage;
            double y = (canvas.getHeight() - point.getValue().getY() * scaleY * scaleImage);  // Invert y-axis

            graphicsContext.setFill(Color.LIGHTGREEN);
            graphicsContext.setFont(Font.font("System", FontWeight.BOLD, FONT_SIZE));
            graphicsContext.fillOval(x - (double) (MARKER_RADIUS / 2), (y - (double) MARKER_RADIUS / 2), MARKER_RADIUS, MARKER_RADIUS);
            int textOffset = 2 * point.getKey().length();
            graphicsContext.fillText(point.getKey(), x - textOffset, y - MARKER_RADIUS);
        }
    }
}
