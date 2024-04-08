package se.oru.coordination.coordination_oru.tests.clean;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.*;
import se.oru.coordination.coordination_oru.DataStructure.CriticalSection;
import se.oru.coordination.coordination_oru.DataStructure.Mission;
import se.oru.coordination.coordination_oru.DataStructure.RobotReport;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.utils.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Missions;

import java.util.Comparator;
import java.util.HashMap;

public class Diameter {
	
	public static void main(String[] args) throws InterruptedException {
		
		double MAX_ACCEL = 1.0;
		double MAX_VEL = 4.0;
		double radius = 40;
		int NUMBER_ROBOTS = 80;
		
		//Instantiate a trajectory envelope coordinator.
		final TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(1000,1000,MAX_VEL,MAX_ACCEL);
		
		//Provide a heuristic (here, closest to critical section goes first)
		tec.addComparator(new Comparator<RobotAtCriticalSection> () {
			@Override
			public int compare(RobotAtCriticalSection o1, RobotAtCriticalSection o2) {
				CriticalSection cs = o1.getCriticalSection();
				RobotReport robotReport1 = o1.getRobotReport();
				RobotReport robotReport2 = o2.getRobotReport();
				return ((cs.getTrajectoryEnvelopeStart1()-robotReport1.getPathIndex())-(cs.getTrajectoryEnvelopeStart2()-robotReport2.getPathIndex()));
			}
		});
		
		//Provide a conservative forward model for each robot
		int[] robotIDs =  new int[NUMBER_ROBOTS];
		for (int i = 0; i < NUMBER_ROBOTS; i++) {
			robotIDs[i] = i+1;
			tec.setForwardModel(i+1, new ConstantAccelerationForwardModel(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(i+1)));
		}

		//Define the footprints of the robots (here they are all the same)
		Coordinate footprint1 = new Coordinate(-0.25,0.25);
		Coordinate footprint2 = new Coordinate(0.25,0.25);
		Coordinate footprint3 = new Coordinate(0.25,-0.25);
		Coordinate footprint4 = new Coordinate(-0.25,-0.25);
		tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);
		
		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		
		//Setup a simple GUI (null means empty map, otherwise provide yaml file)
		BrowserVisualization viz = new BrowserVisualization();
		//viz.setInitialTransform(10, 85, 48);
		viz.setInitialTransform(30, 27, 15);
		tec.setVisualization(viz);
		
		//MetaCSPLogging.setLevel(tec.getClass().getSuperclass(), Level.FINEST);

		//Instantiate a simple motion planner
		ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
		rsp.setRadius(0.2);
		rsp.setFootprint(tec.getDefaultFootprint());
		rsp.setTurningRadius(4.0);
		rsp.setDistanceBetweenPathPoints(0.5);
			
		HashMap<Integer,Pose> startPoses = new HashMap<Integer,Pose>();
		HashMap<Integer,Pose> goalPoses = new HashMap<Integer,Pose>();
		
		double theta = 0.0;
		for (final int robotID : robotIDs) {

			//Place robots
			double alpha = theta + robotID*Math.PI/NUMBER_ROBOTS;
			startPoses.put(robotID, new Pose(radius*Math.cos(alpha), radius*Math.sin(alpha), alpha));
			goalPoses.put(robotID, new Pose(radius*Math.cos(alpha+Math.PI), radius*Math.sin(alpha+Math.PI), alpha));
			tec.placeRobot(robotID, startPoses.get(robotID));
			
			//Plan the path and enqueue the mission
			rsp.setStart(startPoses.get(robotID));
			rsp.setGoals(goalPoses.get(robotID));
			if (!rsp.plan()) throw new Error ("No path between " + startPoses.get(robotID) + " and " + goalPoses.get(robotID));
			
			Missions.enqueueMission(new Mission(robotID, rsp.getPath()));
		}	
		
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();
		
		//Add robots to the mission dispatching thread 
		Missions.startMissionDispatcher(tec);
	
	}
}
