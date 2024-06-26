package se.oru.coordination.coordination_oru.tests.clean;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.demo.DemoDescription;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Missions;

@DemoDescription(desc = "Example of replacing a path midway.")
public class ContinuePathExample {

	public static void main(String[] args) throws InterruptedException {

		double MAX_ACCEL = 3.0;
		double MAX_VEL = 4.0;
		
		//Instantiate a trajectory envelope coordinator.
		final TrajectoryEnvelopeCoordinatorSimulation tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, MAX_VEL,MAX_ACCEL);
		
		tec.setUseInternalCriticalPoints(false);
		tec.setYieldIfParking(true);
		tec.setBreakDeadlocks(false, true, true);
		//MetaCSPLogging.setLevel(TrajectoryEnvelopeCoordinator.class, Level.FINEST);
		
		//Setup the network parameters
//		NetworkConfiguration.setDelays(10, 500);
//		NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS = 0;
//		tec.setNetworkParameters(NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS, NetworkConfiguration.getMaximumTxDelay(), 1e-2);

		double xl = 1.0;
		double yl = .5;
		Coordinate footprint1 = new Coordinate(-xl,yl);
		Coordinate footprint2 = new Coordinate(xl,yl);
		Coordinate footprint3 = new Coordinate(xl,-yl);
		Coordinate footprint4 = new Coordinate(-xl,-yl);
		tec.setDefaultFootprint(footprint1, footprint2, footprint3, footprint4);

		//Need to setup infrastructure that maintains the representation
		tec.setupSolver(0, 100000000);
		//Start the thread that checks and enforces dependencies at every clock tick
		tec.startInference();

		//Setup a simple GUI (null means empty map, otherwise provide yaml file)
		String yamlFile = null;
		yamlFile = "maps/map-partial-2.yaml";	
		
		BrowserVisualization viz = new BrowserVisualization();
		viz.setMap(yamlFile);
		viz.setInitialTransform(20.0, 9.0, 2.0);
		tec.setVisualization(viz);
		
		Missions.loadRoadMap("missions/icaps_locations_and_paths_4.txt");

		//MetaCSPLogging.setLevel(tec.getClass().getSuperclass(), Level.FINEST);
	
		//Instantiate a simple motion planner
		ReedsSheppCarPlanner rsp = new ReedsSheppCarPlanner();
		rsp.setMap(yamlFile);
		rsp.setRadius(0.1);
		rsp.setFootprint(tec.getDefaultFootprint());
		rsp.setTurningRadius(4.0);
		rsp.setDistanceBetweenPathPoints(0.3);
		
		
		//In case deadlocks occur, we make the coordinator capable of re-planning on the fly (experimental, not working properly yet)
		tec.setMotionPlanner(1, rsp);
		tec.setForwardModel(1, new ConstantAcceleration(MAX_ACCEL, MAX_VEL, tec.getTemporalResolution(), tec.getControlPeriod(), tec.getRobotTrackingPeriodInMillis(1)));
		tec.placeRobot(1, Missions.getLocationPose("L_0"));

		PoseSteering[] pathL0R0 = null;		
		rsp.setStart(Missions.getLocationPose("L_0"));
		rsp.setGoals(Missions.getLocationPose("R_0"));
		rsp.plan();
		if (rsp.getPath() == null) throw new Error("No path found.");
		pathL0R0 = rsp.getPath();
	
		int replaceIndex = pathL0R0.length-1;
		Pose replacePose = pathL0R0[replaceIndex].getPose();
		rsp.setStart(replacePose);
		rsp.setGoals(Missions.getLocationPose("L_6"));
		rsp.plan();
		if (rsp.getPath() == null) throw new Error("No path found.");
		PoseSteering[] pathR0L6 = new PoseSteering[replaceIndex+rsp.getPath().length];
        System.arraycopy(pathL0R0, 0, pathR0L6, 0, replaceIndex);
		for (int i = 0; i < rsp.getPath().length; i++) pathR0L6[i+replaceIndex] = rsp.getPath()[i];

		Mission m = new Mission(1, pathL0R0);
		Missions.enqueueMission(m);

		Missions.startMissionDispatcher(tec);

		Thread.sleep(7000);

		tec.replacePath(1,pathR0L6,replaceIndex,false,null);

	}

}
