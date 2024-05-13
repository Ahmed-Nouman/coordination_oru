package se.oru.coordination.coordination_oru.tests;

import se.oru.coordination.coordination_oru.utils.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.tracker.TrackingCallback;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.CriticalSection;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.utils.RobotReport;

import java.util.Comparator;
import java.util.HashMap;

@DemoDescription(desc = "Simple test showing the use of pre-planned paths stored in files, plus ability to replan paths on external request.")
public class TestTrajectoryEnvelopeThreeRobotsReplanOnRequest {
	public static void main(String[] args) throws InterruptedException {

		double MAX_ACCEL = 1.0;
		double MAX_VEL = 4.0;
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
		
		//You probably also want to provide a non-trivial forward model
		//(the default assumes that robots can always stop)
		tec.setForwardModel(1, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
		tec.setForwardModel(2, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(2)));
		tec.setForwardModel(3, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(3)));

		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();

		//Setup a simple GUI (null means empty map, otherwise provide yaml file)
		//JTSDrawingPanelVisualization viz = new JTSDrawingPanelVisualization();
		//RVizVisualization.writeRVizConfigFile(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26);
		//RVizVisualization viz = new RVizVisualization();
		BrowserVisualization viz = new BrowserVisualization();
		viz.setInitialTransform(19, 56.5, 35.17);
		tec.setVisualization(viz);
		
		//Example of how you can add extra info Strings to the visualization of robot status
		TrackingCallback cb = new TrackingCallback(null) {
			
			@Override
			public void onTrackingStart() { }
			
			@Override
			public void onTrackingFinished() { }
			
			@Override
			public String[] onPositionUpdate() {
				return new String[] {"a","b","c"};
			}
			
			@Override
			public void onNewGroundEnvelope() { }
			
			@Override
			public void beforeTrackingStart() { }
			
			@Override
			public void beforeTrackingFinished() { }
		};
		tec.addTrackingCallback(1, cb);
		tec.addTrackingCallback(2, cb);
		tec.addTrackingCallback(3, cb);
		
		//Load data file with locations and pointers to files containing paths between locations
		Missions.loadLocationAndPathData("paths/test_poses_and_path_data.txt");
		
		//Instantiate a simple motion planner (for re-planning)
		ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
		rsp.setRadius(0.1);
		rsp.setFootprint(tec.getDefaultFootprint());
		rsp.setTurningRadius(4.0);
		rsp.setDistanceBetweenPathPoints(0.3);

		//Place robots in their initial locations (looked up in the data file that was loaded above)
		// -- creates a trajectory envelope for each location, representing the fact that the robot is parked
		// -- each trajectory envelope has a path of one pose (the pose of the location)
		// -- each trajectory envelope is the footprint of the corresponding robot in that pose
		tec.placeRobot(1, Missions.getLocation("r1p"));
		tec.placeRobot(2, Missions.getLocation("r2p"));
		tec.setMotionPlanner(2, rsp);
		tec.placeRobot(3, Missions.getLocation("r3p"));

		//Make a mission for each robot, and store it in the global hashmap:
		// -- from parking location of robot i (rip)
		// -- to destination location of robot i (desti)
		Missions.enqueueMission(new Mission(1, Missions.getShortestPath("r1p", "dest1")));
		Missions.enqueueMission(new Mission(2, Missions.getShortestPath("r2p", "dest2")));
		Missions.enqueueMission(new Mission(3, Missions.getShortestPath("r3p", "dest3")));

		//Make another mission for each robot, and store it in the global hashmap:
		// -- from destination location of robot i (desti)
		// -- to parking location of robot i (rip)
		Missions.enqueueMission(new Mission(1, Missions.getShortestPath("dest1", "r1p")));
		Missions.enqueueMission(new Mission(2, Missions.getShortestPath("dest2", "r2p")));
		Missions.enqueueMission(new Mission(3, Missions.getShortestPath("dest3", "r3p")));
				
		System.out.println("Added missions " + Missions.getMissions());

		final HashMap<Integer,Boolean> isPaused = new HashMap<Integer, Boolean>();
		//Start a mission dispatching thread for each robot, which will run forever
		for (int i = 1; i <= 3; i++) {
			final int robotID = i;
			//For each robot, create a thread that dispatches the "next" mission when the robot is free 
			Thread t = new Thread() {
				int iteration = 0;
				@Override
				public void run() {
					isPaused.put(robotID,false);
					while (true) {
						//Mission to dispatch alternates between (rip -> desti) and (desti -> rip)
						Mission m = Missions.getMission(robotID, iteration%2);
						synchronized(tec) {
							//addMission returns true iff the robot was free to accept a new mission
							if (!isPaused.get(robotID) && tec.addMissions(m)) iteration++;
						}
						//Sleep for a little (2 sec)
						try { Thread.sleep(2000); }
						catch (InterruptedException e) { e.printStackTrace(); }
					}
				}
			};
			//Start the thread!
			t.start();
		}
		
		Thread.sleep(12000);
		isPaused.put(2, true);
		
		boolean result = tec.replanEnvelope(2);
		System.out.println("Replanning path for Robot2. Spawned thread succeeded: " + result + ".");

	}
}
