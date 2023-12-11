package se.oru.coordination.coordination_oru.gui;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.motionplanning.OccupancyMap;
import se.oru.coordination.coordination_oru.utils.Round;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MapInteract extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private final List<Point> markers = new ArrayList<>();
    private final OccupancyMap occupancyMap;
    private Point point;
    private final ProjectData projectData;
    private final Button button;

    public MapInteract(String mapYaml, ProjectData projectData, Button button) {
        this.projectData = projectData;
        this.button = button;
        this.occupancyMap = new OccupancyMap(mapYaml);
        addMouseMotionListener(this);
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(occupancyMap.getPixelWidth(), occupancyMap.getPixelHeight()));
        JFrame jFrame = new JFrame("Map inspector");
        jFrame.setContentPane(this);
        jFrame.setSize(1920,1200);  // FIXME: hardcoded, Can make Adjustable on the image size and scrollable
        jFrame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        boolean isBinaryImage = false;
        if (!isBinaryImage) graphics.drawImage(occupancyMap.asBufferedImage(), 0, 0, null);
        else graphics.drawImage(occupancyMap.asThresholdedBufferedImage(), 0, 0, null);

        // Draw markers
        for (Point marker : markers) {
            drawMarker(graphics, marker);
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        point = new Point(mouseEvent.getX(),mouseEvent.getY());
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        point = new Point(mouseEvent.getX(),mouseEvent.getY());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void keyPressed(KeyEvent mouseEvent) {

    }

    @Override
    public void keyReleased(KeyEvent mouseEvent) {

    }

    @Override
    public void keyTyped(KeyEvent mouseEvent) {
        if (mouseEvent.getKeyChar() == 'c') {
            Coordinate position = occupancyMap.toWorldCoordiantes(point.x, point.y);
            position.x = Round.round(position.x, 2);
            position.y = Round.round(position.y, 2);
            boolean occupancy = occupancyMap.asByteArray()[(point.y) * occupancyMap.getPixelWidth() / 8 + (point.x) / 8] == 0;

            if (occupancy) {
                // Execute this part on the JavaFX Application thread. It is required because the class uses swing library.
                Platform.runLater(() -> {
                    String poseName = AddLocationDialogBox.display();
                    if (poseName != null) {
                        markers.add(new Point(point.x, point.y));
                        projectData.addPose(poseName, new Pose(position.x, position.y, 0)); // FIXME theta can be set according to UP, DOWN, LEFT, RIGHT, maybe UPLeft, UPRight, DownLeft, DownRight
                        repaint();
                        if (projectData.getPoses().size() >= 2) button.setDisable(false);
                    }
                });
            } else {
                Platform.runLater(() -> {
                    AlertBox.display("Error", "You cannot add a location on an occupied cell.", Alert.AlertType.ERROR);
                });
            }
        }
    }

    private void drawMarker(Graphics graphics, Point point) {
        int markerSize = 10;
        int x = point.x - markerSize / 2;
        graphics.setColor(Color.GREEN);
        int y = point.y - markerSize / 2;
        graphics.fillRect(x, y, markerSize, markerSize);
    }
}