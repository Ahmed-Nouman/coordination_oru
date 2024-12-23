package se.oru.coordination.coordination_oru.utils;

import com.vividsolutions.jts.geom.Coordinate;
import se.oru.coordination.coordination_oru.motionPlanning.OccupancyMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MapInspector extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	private static final long serialVersionUID = 906863784669776526L;
	private OccupancyMap om = null;
	private boolean occ = false;
	private Point p;

	public MapInspector(OccupancyMap om) {
		super();
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseListener(this);
		setFocusable(true);
		this.om = om;
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(om.getPixelWidth(), om.getPixelHeight()));
		createFrame();
	}

	private void createFrame() {
		// Create a JFrame to hold the MapInspector panel
		JFrame f = new JFrame("Map inspector");

		// Create a JScrollPane and set the MapInspector as its viewport view
		JScrollPane sp = new JScrollPane(this);

		// Add the JScrollPane to the JFrame
		f.setContentPane(sp);
		f.setSize(1920, 1200);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (!occ) g.drawImage(om.asBufferedImage(), 0, 0, null);
		else g.drawImage(om.asThresholdedBufferedImage(), 0, 0, null);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		p = new Point(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		p = new Point(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {
		if (arg0.getKeyChar() == 'c') {
			try {
				Color color = new Color(om.asBufferedImage().getRGB(p.x, p.y));
				Coordinate position = om.convertToWorldCoordinates(p.x, p.y);
				System.out.println("--");
				System.out.println("Pixel (x,y) = (" + p.x + "," + p.y + ")");
				System.out.println("Position (x,y) = (" + position.x + "," + position.y + ")");
				System.out.println("Color (r,g,b,a) = (" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + ")");
				System.out.println("Occupancy map bit: " + om.asByteArray()[(p.y) * om.getPixelWidth() / 8 + (p.x) / 8]);
				System.out.println("Occupancy value: " + om.getOccupancyValue(p.x, p.y));
				System.out.println("State: " + (om.isOccupied(p.x, p.y) ? "occupied" : "free") + " (threshold is " + om.getThreshold() + ")");
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} else if (arg0.getKeyChar() == 'o') {
			occ = !occ;
			System.out.println("Showing " + (occ ? "thresholded (" + om.getThreshold() + ")" : "original") + " map");
		}
		repaint();
	}

	public static void main(String[] args) {
		// String map = "maps/map-empty-circle.yaml";
		// String map = "/home/fpa/gitroot.github/coordination_oru/maps/map-partial-2.yaml";
		// String map = "maps/paolo/icra2016_basement.yaml";
		// String map = "maps/map-partial-2.yaml";
//		String map = "maps/mine-map-full.yaml";
		// String map = "maps/mine-map-heuristic-paper.yaml";
		// String map = "maps/mine-map-test.yaml";
//		 String map = "maps/12-1051_batteryChange.yaml";
//		 String map = "maps/Baseline_4PV_2OP_MixedTraffic.yaml";
//		 String map = "maps/Baseline_4PV_4OP_StopAndGo.yaml";
//		 String map = "maps/12-1051_Simulation_MAP_BatteryBay.yaml";
		 String map = "maps/Baseline_4PV_4OP_6SV_StopAndGo_4Barriers.yaml";
		OccupancyMap om = new OccupancyMap(map);
		// "/home/fpa/gitroot.gitlab/iqmobility/maps/leipzig-lindenau/leipzig-lindenau.yaml"
		MapInspector p = new MapInspector(om);
	}
}
