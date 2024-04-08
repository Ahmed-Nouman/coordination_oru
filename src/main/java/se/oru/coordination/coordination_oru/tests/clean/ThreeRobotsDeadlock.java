package se.oru.coordination.coordination_oru.tests.clean;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.ConstantAccelerationForwardModel;
import se.oru.coordination.coordination_oru.DataStructure.Mission;
import se.oru.coordination.coordination_oru.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;

import java.util.Comparator;

@DemoDescription(desc = "Coordination with deadlock-inducing ordering heuristic (paths obtained with the ReedsSheppCarPlanner).")
public class ThreeRobotsDeadlock {
	
	public static void main(String[] args) throws InterruptedException {
		
		double MAX_ACCEL = 1.0;
		double MAX_VEL = 2.5;
		//Instantiate a trajectory envelope coordinator.
		//The TrajectoryEnvelopeCoordinatorSimulation implementation provides
		// -- the factory method getNewTracker() which returns a trajectory envelope tracker
		// -- the getCurrentTimeInMillis() method, which is used by the coordinator to keep time
		//You still need to add one or more comparators to determine robot orderings thru critical sections (comparators are evaluated in the order in which they are added)
		final TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(MAX_VEL,MAX_ACCEL);
		tec.addComparator(new Comparator<RobotAtCriticalSection> () {
			@Override
			public int compare(RobotAtCriticalSection o1, RobotAtCriticalSection o2) {
				int robot1ID = o1.getRobotReport().getRobotID();
				int robot2ID = o2.getRobotReport().getRobotID();
				if (robot1ID == 1 && robot2ID == 2) return -1;
				if (robot1ID == 2 && robot2ID == 1) return 1;
				if (robot1ID == 1 && robot2ID == 3) return 1;
				if (robot1ID == 3 && robot2ID == 1) return -1;
				if (robot1ID == 2 && robot2ID == 3) return -1;
				if (robot1ID == 3 && robot2ID == 2) return 1;
				return 0;
			}
		});
		//You probably also want to provide a non-trivial forward model
		//(the default assumes that robots can always stop)
		tec.setForwardModel(1, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
        tec.setForwardModel(2, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(2)));
		tec.setForwardModel(3, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(3)));
		//comment out following (or set first arg to true) to make the coordinator attempt to break the deadlock
		tec.setBreakDeadlocks(false, false, false);

		Coordinate footprint1 = new Coordinate(-0.25,0.25);
		Coordinate footprint2 = new Coordinate(0.25,0.25);
		Coordinate footprint3 = new Coordinate(0.25,-0.25);
		Coordinate footprint4 = new Coordinate(-0.25,-0.25);
		tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);

		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();
		
		//Setup a simple GUI (null means empty map, otherwise provide yaml file)
		//JTSDrawingPanelVisualization viz = new JTSDrawingPanelVisualization();
		BrowserVisualization viz = new BrowserVisualization();
		viz.setInitialTransform(73, 6, 0);
		tec.setVisualization(viz);
				
		//MetaCSPLogging.setLevel(tec.getClass().getSuperclass(), Level.FINEST);
	
		Pose[] starts = new Pose[3];
		Pose[] goals = new Pose[3];
		starts[0] = new Pose(3.0,3.0,Math.PI/4);
		goals[0] = new Pose(10.0,10.0,Math.PI/4);
		starts[1] = new Pose(10.0,3.0,3*Math.PI/4);
		goals[1] = new Pose(3.0,10.0,3*Math.PI/4);
		starts[2] = new Pose(3.0,6.5,0.0);
		goals[2] = new Pose(10.0,6.5,0.0);
		
		//Place robots in their initial locations (looked up in the data file that was loaded above)
		// -- creates a trajectory envelope for each location, representing the fact that the robot is parked
		// -- each trajectory envelope has a path of one pose (the pose of the location)
		// -- each trajectory envelope is the footprint of the corresponding robot in that pose
		for (int i = 0; i < 3; i++) {
			
			//Place the robot in the environment
			tec.placeRobot(i+1, starts[i]);
			
			//Instantiate a simple motion planner
			ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
			rsp.setRadius(0.2);
			rsp.setFootprint(tec.getDefaultFootprint());
			rsp.setTurningRadius(4.0);
			rsp.setDistanceBetweenPathPoints(0.5);

			rsp.setStart(starts[i]);
			rsp.setGoals(goals[i]);
			if (!rsp.plan()) throw new Error ("No path between " + starts[i] + " and " + goals[i]);
			Missions.enqueueMission(new Mission(i+1,rsp.getPath()));
			Missions.enqueueMission(new Mission(i+1,rsp.getPathInv()));
			tec.setMotionPlanner(i+1, rsp);
		}
		
		//Thread.sleep(6000);

		Missions.startMissionDispatcher(tec);

	}
	
}
