package se.oru.coordination.coordination_oru.tests.clean;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.utils.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.CriticalSection;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReport;

import java.util.Comparator;

@DemoDescription(desc = "Example showing coordination in opposing directions (following should not happen here).")
public class TwoRobotsIdle {

	public static void main(String[] args) throws InterruptedException {

		double MAX_ACCEL = 2.0;
		double MAX_VEL = 3.0;
		//Instantiate a trajectory envelope coordinator.
		//The TrajectoryEnvelopeCoordinatorSimulation implementation provides
		// -- the factory method getNewTracker() which returns a trajectory envelope tracker
		// -- the getCurrentTimeInMillis() method, which is used by the coordinator to keep time
		//You still need to add one or more comparators to determine robot orderings thru critical sections (comparators are evaluated in the order in which they are added)
		final TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(MAX_VEL,MAX_ACCEL);
		tec.addComparator(new Comparator<RobotAtCriticalSection> () {
			@Override
			public int compare(RobotAtCriticalSection o1, RobotAtCriticalSection o2) {
				CriticalSection cs = o1.getCriticalSection();
				RobotReport robotReport1 = o1.getRobotReport();
				RobotReport robotReport2 = o2.getRobotReport();
				return ((cs.getTrajectoryEnvelopeStart1()-robotReport1.getPathIndex())-(cs.getTrajectoryEnvelopeStart2()-robotReport2.getPathIndex()));
			}
		});
		tec.addComparator(new Comparator<RobotAtCriticalSection> () {
			@Override
			public int compare(RobotAtCriticalSection o1, RobotAtCriticalSection o2) {
				return (o2.getRobotReport().getRobotID()-o1.getRobotReport().getRobotID());
			}
		});
		tec.setUseInternalCriticalPoints(false);
		
		Coordinate footprint1 = new Coordinate(-0.5,0.5);
		Coordinate footprint2 = new Coordinate(-0.5,-0.5);
		Coordinate footprint3 = new Coordinate(0.7,-0.5);
		Coordinate footprint4 = new Coordinate(0.7,0.5);
		tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);
		
		//You probably also want to provide a non-trivial forward model
		//(the default assumes that robots can always stop)
		tec.setForwardModel(1, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
		tec.setForwardModel(2, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(2)));

		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();

		//JTSDrawingPanelVisualization viz = new JTSDrawingPanelVisualization();
		//viz.setSize(1800, 450);
		BrowserVisualization viz = new BrowserVisualization();
		viz.setInitialTransform(63, 0, 0);
		tec.setVisualization(viz);

		Pose startRobot1 = new Pose(5.0,5.0,0.0);	
		Pose startRobot2 = new Pose(25.0,9.0,Math.PI);
	
		//Place robots in their initial locations (looked up in the data file that was loaded above)
		// -- creates a trajectory envelope for each location, representing the fact that the robot is parked
		// -- each trajectory envelope has a path of one pose (the pose of the location)
		// -- each trajectory envelope is the footprint of the corresponding robot in that pose
		tec.placeRobot(1, startRobot1);
		tec.placeRobot(2, startRobot2);

		Missions.startMissionDispatcher(tec);
				
	}

}
