package se.oru.coordination.coordination_oru.simulation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.utility.UI.JTSDrawingPanel;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class JTSDrawingPanelVisualization implements FleetVisualization {

	private JTSDrawingPanel panel = null;

	public JTSDrawingPanelVisualization() {
		this(null);
	}
	
	public JTSDrawingPanelVisualization(String mapYAMLFile) {
		this.setupGUI(mapYAMLFile);
	}
	
	public JTSDrawingPanel getPanel() {
		return this.panel;
	}
	
	public void setSize(int width, int height) {
		JFrame jFrame = (JFrame) SwingUtilities.getWindowAncestor(this.getPanel());
		jFrame.setSize(width, height);
	}

	// Set the size of screen accordingly to the display size of the running machine
	public void setSize() {
		JFrame jFrame = (JFrame) SwingUtilities.getWindowAncestor(this.getPanel());
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		jFrame.setSize(screenSize.width, screenSize.height);
	}

	private static String getExtraData(TrajectoryEnvelope te, RobotReport rr, String[] extraStatusInfo, String representation) {
		if (Objects.equals(representation, "Name")) {
			return "";
		} else if (Objects.equals(representation, "PathIndex")) {
			return ":" + (extraStatusInfo == null || extraStatusInfo.length == 0 ? "" : " ") + rr.getPathIndex();
		}
		else {
			String extraData = "";
			if (rr.getPathIndex() != -1 && te.getPathLength() != 0) {
				double percentage = ((double) rr.getPathIndex() / te.getPathLength()) * 100;
				String roundedPercentage = String.format("%.1f", percentage);
				if (percentage <= 100) extraData = ":" + (extraStatusInfo == null || extraStatusInfo.length == 0 ? "" : " ") +
						roundedPercentage + "%";
			}
			return extraData;
		}
	}
	@Override
	public void displayRobotState(TrajectoryEnvelope te, RobotReport rr, String ... extraStatusInfo) {
		double x = rr.getPose().getX();
		double y = rr.getPose().getY();
		double theta = rr.getPose().getTheta();

		var name = "R" + te.getRobotID();
		var color = "#ff0000";
		if (VehiclesHashMap.getVehicle(te.getRobotID()) != null) {
			name = VehiclesHashMap.getVehicle(te.getRobotID()).getName();
			color = (String) VehiclesHashMap.getVehicle(rr.getRobotID()).getColor("code");
		}

		// Show Representation
		String representation = "Name";
		String extraData = getExtraData(te, rr, extraStatusInfo, representation);
		name += extraData;

		if (rr.getPathIndex() != -1) panel.addGeometry(name, TrajectoryEnvelope.getFootprint(te.getFootprint(), x, y, theta), false,
				true, false, color);
		else panel.addGeometry(name, TrajectoryEnvelope.getFootprint(te.getFootprint(), te.getTrajectory().getPose()[0].getX(),
				te.getTrajectory().getPose()[0].getY(), te.getTrajectory().getPose()[0].getTheta()), false, true,
				false, "#FF0000");
	}

	@Override
	public void displayDependency(RobotReport rrWaiting, RobotReport rrDriving, String dependencyDescriptor) {
		panel.addArrow(dependencyDescriptor, rrWaiting.getPose(), rrDriving.getPose());
	}
	
	private void setupGUI(String mapYAMLFile) {
		//Show everything in a GUI (the trackers update vehicle positions in real time, see below)
		this.panel = JTSDrawingPanel.makeEmpty("Simulation of the scenario");
		//setPriorityOfEDT(Thread.MIN_PRIORITY);
		//setPriorityOfEDT(Thread.MAX_PRIORITY);
		panel.setSmoothTransitions(true);
		panel.setArrowHeadSizeInMeters(0.6*TrajectoryEnvelopeCoordinatorSimulation.MAX_DEFAULT_FOOTPRINT_DIMENSION);
		panel.setTextSizeInMeters(0.8*TrajectoryEnvelopeCoordinatorSimulation.MAX_DEFAULT_FOOTPRINT_DIMENSION);
		//System.out.println("TEXT SIZE IN METERS IS " + 0.5*getMaxFootprintDimension(1));
		if (mapYAMLFile != null) panel.setMap(mapYAMLFile);
		panel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				String fileName = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS").format(new Date());
				if (e.getKeyChar() == 's') {
					fileName += ".svg";
					dumpSVG(fileName);
					System.out.println("Saved screenshot " + fileName);
				}
				else if (e.getKeyChar() == 'p') {
					fileName += ".pdf";
					dumpPDF(fileName);
					System.out.println("Saved screenshot " + fileName);
				}
				else if (e.getKeyChar() == 'e') {
					fileName += ".eps";
					dumpEPS(fileName);
					System.out.println("Saved screenshot " + fileName);
				}
//				else if (e.getKeyChar() == 'm') {
//					System.out.println("Muted robots: " + Arrays.toString(tec.getMuted()));
//				}
//				else {
//					try {
//						int robotID = Integer.parseInt(""+e.getKeyChar());
//						tec.toggleMute(robotID);
//					}
//					catch(NumberFormatException e1) {}
//				}

			}

			@Override
			public void keyReleased(KeyEvent e) { }

			@Override
			public void keyPressed(KeyEvent e) { }
		});
		panel.setFocusable(true);
	}
	
	/**
	 * Dump a vector graphics file with the contents of the GUI. Note: this operation is slow, so this method
	 * should not be called within the control loop.
	 * @param fileName Name of the PDF file to write to.
	 */
	public void dumpPDF(String fileName) {
		panel.writePDF(fileName);
	}

	/**
	 * Dump a vector graphics file with the contents of the GUI. Note: this operation is slow, so this method
	 * should not be called within the control loop.
	 * @param fileName Name of the SVG file to write to.
	 */
	public void dumpSVG(String fileName) {
		panel.writeSVG(fileName);
	}

	/**
	 * Dump a vector graphics file with the contents of the GUI. Note: this operation is slow, so this method
	 * should not be called within the control loop.
	 * @param fileName Name of the EPS file to write to.
	 */
	public void dumpEPS(String fileName) {
		panel.writeEPS(fileName);
	}

	/**
	 * Scale and translate the view of the GUI to include all geometries. 
	 */
	public void centerView() {
		//		this.panel.centerView();
		this.panel.reinitVisualization();
	}

	@Override
	public void updateVisualization() {
		panel.removeOldGeometries(1000);
		panel.updatePanel();
	}

	@Override
	public void addEnvelope(TrajectoryEnvelope te) {
		GeometricShapeDomain dom = (GeometricShapeDomain)te.getEnvelopeVariable().getDomain();
		panel.addGeometry("_"+te.getID(), dom.getGeometry(), true, false);
		panel.setPermanent("_"+te.getID());
	}

	@Override
	public void removeEnvelope(TrajectoryEnvelope te) {
		panel.removeGeometry("_"+te.getID());
	}
	
	public void setMinimumVisibleFrame(double minX, double minY, double maxX, double maxY) {
		GeometryFactory gf = new GeometryFactory();
		Geometry frame = gf.createPolygon(new Coordinate[] {
				new Coordinate(minX,minY),
				new Coordinate(minX,maxY),
				new Coordinate(maxX,maxY),
				new Coordinate(maxX,minY),
				new Coordinate(minX,minY)
		});	
		panel.removeGeometry("_frame");
		panel.addGeometry("_frame", frame, true, false, true, "#000000");
		panel.setPermanent("_frame");
	}

	@Override
	public void setMap(String yamlFile) {
		panel.setMap(yamlFile);
	}

	@Override
	public int periodicEnvelopeRefreshInMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

}
