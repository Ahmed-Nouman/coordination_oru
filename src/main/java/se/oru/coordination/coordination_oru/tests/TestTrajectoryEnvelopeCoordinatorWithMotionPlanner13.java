package se.oru.coordination.coordination_oru.tests;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.dataStructue.CriticalSection;
import se.oru.coordination.coordination_oru.dataStructue.Mission;
import se.oru.coordination.coordination_oru.dataStructue.RobotReport;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.JTSDrawingPanelVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;

import java.util.Comparator;

@DemoDescription(desc = "Example that throws an error due to coordination being impossible.")
public class TestTrajectoryEnvelopeCoordinatorWithMotionPlanner13 {

	public static void main(String[] args) throws InterruptedException {

		double MAX_ACCEL = 1.0;
		double MAX_VEL = 5.0;
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
		
		Coordinate footprint1 = new Coordinate(-0.5,0.5);
		Coordinate footprint2 = new Coordinate(-0.5,-0.5);
		Coordinate footprint3 = new Coordinate(0.7,-0.5);
		Coordinate footprint4 = new Coordinate(0.7,0.5);
		tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);
		
		//You probably also want to provide a non-trivial forward model
		//(the default assumes that robots can always stop)
		tec.setForwardModel(1, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
		tec.setForwardModel(2, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(2)));

		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();
		tec.setCheckEscapePoses(true);
		tec.setBreakDeadlocks(false, true, true);

		//Setup a simple GUI (null means empty map, otherwise provide yaml file)
		JTSDrawingPanelVisualization viz = new JTSDrawingPanelVisualization();
		tec.setVisualization(viz);

		Pose startRobot1 = new Pose(5.0,1.0,0.0);
		Pose goalRobot1 = new Pose(5.0,10.0,Math.PI);

		Pose startRobot2 = new Pose(1.0,10.0,0.0);
		Pose goalRobot2 = new Pose(6.0,10.0,Math.PI);
		
		Pose startRobot2Next = new Pose(6.0,10.0,Math.PI);
		Pose goalRobot2Next = new Pose(12.0,10.0,Math.PI);

		//Place robots in their initial locations (looked up in the data file that was loaded above)
		// -- creates a trajectory envelope for each location, representing the fact that the robot is parked
		// -- each trajectory envelope has a path of one pose (the pose of the location)
		// -- each trajectory envelope is the footprint of the corresponding robot in that pose
		tec.placeRobot(1, startRobot1);
		tec.placeRobot(2, startRobot2);

		String yamlFile = "maps/map-empty.yaml";
		final ReedsSheppCarPlanner rsp1 = new ReedsSheppCarPlanner();
		tec.setMotionPlanner(1, rsp1);
		rsp1.setMap(yamlFile);
		rsp1.setRadius(0.2);
		rsp1.setFootprint(footprint1,footprint2,footprint3,footprint4);
		rsp1.setTurningRadius(4.0);
		rsp1.setDistanceBetweenPathPoints(0.1);
		
		rsp1.setStart(startRobot1);
		rsp1.setGoals(goalRobot1);
		if (!rsp1.plan()) throw new Error("No path found");
		Missions.enqueueMission(new Mission(1,rsp1.getPath()));
		
		final ReedsSheppCarPlanner rsp2 = (ReedsSheppCarPlanner) rsp1.getCopy(false);
		tec.setMotionPlanner(2, rsp2);
		rsp2.setStart(startRobot2);
		rsp2.setGoals(goalRobot2);
		if (!rsp2.plan()) throw new Error("No path found");
		Missions.enqueueMission(new Mission(2,rsp2.getPath()));

		rsp2.setStart(startRobot2Next);
		rsp2.setGoals(goalRobot2Next);
		if (!rsp2.plan()) throw new Error("No path found");
		Missions.enqueueMission(new Mission(2,rsp1.getPath()));

		System.out.println("Added missions " + Missions.getMissions());

		tec.addMissions(Missions.getMission(1, 0), Missions.getMission(2, 0));
		
		while (!tec.isFree(2)) {
			Thread.sleep(100);
		}
		
		Thread.sleep(5000);
		
		tec.addMissions(Missions.getMission(2, 1));

	}

}
