package se.oru.coordination.coordination_oru.coordinator;

import aima.core.util.datastructure.Pair;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.metacsp.framework.Constraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope.SpatialEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.logging.MetaCSPLogging;
import se.oru.coordination.coordination_oru.utils.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.tracker.TrackingCallback;
import se.oru.coordination.coordination_oru.utils.CriticalSection;
import se.oru.coordination.coordination_oru.utils.Dependency;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.AbstractMotionPlanner;
import se.oru.coordination.coordination_oru.tracker.AbstractTrajectoryEnvelopeTracker;
import se.oru.coordination.coordination_oru.tracker.TrajectoryEnvelopeTrackerDummy;
import se.oru.coordination.coordination_oru.simulation.FleetVisualization;
import se.oru.coordination.coordination_oru.utils.StringUtils;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * This class provides coordination for a fleet of robots. An instantiatable {@link AbstractTrajectoryEnvelopeCoordinator}
 * must provide an implementation of the {@link #updateDependencies()} function and of a time keeping method, a {@link TrajectoryEnvelope} tracker factory, and
 * a criteria with which robots are to be prioritized.
 * 
 * @author fpa
 *
 */
public abstract class AbstractTrajectoryEnvelopeCoordinator {

	public static String TITLE = "coordination_oru - Robot-agnostic online coordination for multiple robots";
	public static String COPYRIGHT = "Copyright \u00a9 2017-" + Calendar.getInstance().get(Calendar.YEAR) + " Federico Pecora";
	public static String[] CONTRIBUTORS = {"Anna Mannucci", "Franziska Klügl", "Ahmed Nouman", "Olga Mironenko"};

	//null -> public (GPL3) license
	public static String LICENSE = null;

	public static String PUBLIC_LICENSE = "This program comes with ABSOLUTELY NO WARRANTY. "
			+ "This program is free software: you can redistribute it and/or modify it under the "
			+ "terms of the GNU General Public License as published by the Free Software Foundation, "
			+ "either version 3 of the License, or (at your option) any later version. see LICENSE for details.";
	public static String PRIVATE_LICENSE = "This program comes with ABSOLUTELY NO WARRANTY. "
			+ "This program has been licensed to " + LICENSE + ". The licensee may "
			+ "redistribute it under certain conditions; see LICENSE for details.";

	//Force printing of (c) and license upon class loading
	static { printLicense(); }

	public static final int PARKING_DURATION = 3000;
	protected static final int DEFAULT_STOPPING_TIME = 5000;
	protected int DEFAULT_ROBOT_TRACKING_PERIOD = 30;
	protected int CONTROL_PERIOD;
	protected double TEMPORAL_RESOLUTION;
	public static int EFFECTIVE_CONTROL_PERIOD = 0;

	protected boolean overlay = false;
	protected boolean quiet = true;

	//For statistics
	protected AtomicInteger totalMsgsSent = new AtomicInteger(0);
	protected AtomicInteger totalMsgsReTx = new AtomicInteger(0);
	protected AtomicInteger criticalSectionCounter =  new AtomicInteger(0);

	protected TrajectoryEnvelopeSolver solver = null;
	protected Thread inference = null;
	protected volatile Boolean stopInference = Boolean.TRUE;

	//protected JTSDrawingPanel panel = null;
	protected FleetVisualization viz = null;
	protected TreeSet<Pair<TrajectoryEnvelope,Long>> missionsPool = new TreeSet<>((te1, te2) -> te1.getSecond() < te2.getSecond() ? 1 : -1);
	protected ArrayList<TrajectoryEnvelope> envelopesToTrack = new ArrayList<>();
	protected ArrayList<TrajectoryEnvelope> currentParkingEnvelopes = new ArrayList<>();
	public final HashSet<CriticalSection> allCriticalSections = new HashSet<>();
	protected HashMap<CriticalSection,Pair<Integer,Integer>> CSToDepsOrder = new HashMap<>();
	HashMap<Dependency,CriticalSection> depsToCS = new HashMap<>();
	protected HashMap<CriticalSection,Pair<Integer,Integer>> escapingCSToWaitingRobotIDandCP = new HashMap<CriticalSection, Pair<Integer,Integer>>();

	protected HashMap<Integer,ArrayList<Integer>> stoppingPoints = new HashMap<Integer,ArrayList<Integer>>();

	protected HashMap<Integer,ArrayList<Integer>> stoppingTimes = new HashMap<Integer,ArrayList<Integer>>();
	protected HashMap<Integer,Thread> stoppingPointTimers = new HashMap<Integer,Thread>();
	public HashMap<Integer,AbstractTrajectoryEnvelopeTracker> trackers = new HashMap<>();

	protected HashMap<Integer, Dependency> currentDependencies = new HashMap<Integer, Dependency>();
	protected static Logger metaCSPLogger = MetaCSPLogging.getLogger(TrajectoryEnvelopeCoordinator.class);

	protected String logDirName = null;
	protected HashMap<AbstractTrajectoryEnvelopeTracker,Pair<Integer,Long>> communicatedCPs = new HashMap<AbstractTrajectoryEnvelopeTracker, Pair<Integer,Long>>();

	protected HashMap<AbstractTrajectoryEnvelopeTracker,Integer> externalCPCounters = new HashMap<AbstractTrajectoryEnvelopeTracker, Integer>();
	protected ComparatorChain comparators = new ComparatorChain();

	protected HashMap<Integer,ForwardModel> forwardModels = new HashMap<Integer, ForwardModel>();
	protected HashMap<Integer,Coordinate[]> footprints = new HashMap<Integer, Coordinate[]>();

	protected HashMap<Integer,Double> maxFootprintDimensions = new HashMap<Integer, Double>();
	protected HashMap<Integer, Integer> robotTrackingPeriodInMillis = new HashMap<Integer, Integer>();

	protected HashMap<Integer, Double> robotMaxVelocity = new HashMap<Integer, Double>();
	protected HashMap<Integer, Double> robotMaxAcceleration = new HashMap<Integer, Double>();
	protected HashSet<Integer> muted = new HashSet<Integer>();

	protected boolean yieldIfParking = true;

	protected boolean checkEscapePoses = true;
	protected HashMap<Integer, TrackingCallback> trackingCallbacks = new HashMap<Integer, TrackingCallback>();

	protected Callback inferenceCallback = null;
	protected HashMap<Integer,AbstractMotionPlanner> motionPlanners = new HashMap<Integer, AbstractMotionPlanner>();

	//Network knowledge

	protected double packetLossProbability = NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS;
	public static int MAX_TX_DELAY = NetworkConfiguration.getMaximumTxDelay();
	protected double maxFaultsProbability = NetworkConfiguration.PROBABILITY_OF_PACKET_LOSS;
	protected int numberOfReplicas = 1;
	//State knowledge

	protected HashMap<Integer,Boolean> isDriving = new HashMap<Integer, Boolean>();

    /**
	 * Get the envelopes representing robots that are not idle.
	 * @return Envelopes representing robots that are not idle.
	 */
	public ArrayList<SpatialEnvelope> getDrivingEnvelopes() {
		//Collect all driving envelopes and current pose indices
		ArrayList<SpatialEnvelope> drivingEnvelopes = new ArrayList<SpatialEnvelope>();
		for (AbstractTrajectoryEnvelopeTracker atet : trackers.values()) {
			if (!(atet instanceof TrajectoryEnvelopeTrackerDummy)) {
				drivingEnvelopes.add(atet.getTrajectoryEnvelope().getSpatialEnvelope());
				//metaCSPLogger.info(atet.getRobotReport().getRobotID() + " is driving.");
			}
		}
		return drivingEnvelopes;
	}

	@Deprecated
	public ArrayList<SpatialEnvelope> getDrivingEnvelope() {
		//Collect all driving envelopes and current pose indices
		ArrayList<SpatialEnvelope> drivingEnvelopes = new ArrayList<>();
		for (AbstractTrajectoryEnvelopeTracker atet : trackers.values()) {
			if (!(atet instanceof TrajectoryEnvelopeTrackerDummy)) {
				drivingEnvelopes.add(atet.getTrajectoryEnvelope().getSpatialEnvelope());
				//metaCSPLogger.info(atet.getRobotReport().getRobotID() + " is driving.");
			}
		}
		return drivingEnvelopes;
	}

	/**
	 * Check if a robot is known to the coordinator and parked.
	 * @param robotID The ID of a robot.
	 * @return <code>true</code> iff the given robot is known to the coordinator and is parked.
	 */
	public boolean isParked(int robotID) {
		return (isDriving.containsKey(robotID) && !isDriving.get(robotID));
	}

	/**
	 * Check if a robot is known to the coordinator and driving.
	 * @param robotID The ID of a robot.
	 * @return <code>true</code> iff the given robot is known to the coordinator and is driving.
	 */
	public boolean isDriving(int robotID) {
		return (isDriving.containsKey(robotID) && isDriving.get(robotID));
	}

	/**
	 * Get the current set of idle robots
	 * @return The current set of idle robots
	 */
	public ArrayList<Integer> getIdleRobots() {
		if (solver == null) {
			metaCSPLogger.severe("Solver not initialized, please call method setupSolver() first!");
			throw new Error("Solver not initialized, please call method setupSolver() first!");
		}
		synchronized(solver) {
			ArrayList<Integer> idleRobots = new ArrayList<Integer>();
			for (int robotID : this.trackers.keySet()){
				if (isFree(robotID)) idleRobots.add(robotID);
			}
			return idleRobots;
		}
	}

	/**
	 * Return all robotIDs.
	 * @return The set of robotIDs.
	 */
	public Set<Integer> getAllRobotIDs() {
		return trackers.keySet();
	}


	/**
	 * Returning the number of messages required by each send to be effective
	 * (i.e. the probability of unsuccessful delivery will be lower than the threshold maxFaultsProbability)
	 */
	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	/**
	 * Return the temporal resolution of the control period.
	 * @return The temporal resolution of the control period, e.g., 1000 for milliseconds.
	 */
	public double getTemporalResolution() {
		return TEMPORAL_RESOLUTION;
	}

	/**
	 * Get the tracking period of a given robot in millis.
	 * @param robotID The ID of the robot.
	 * @return The tracking period of the robot (or the default value if not specified).
	 */
	public Integer getRobotTrackingPeriodInMillis(int robotID) {
		if (this.robotTrackingPeriodInMillis.containsKey(robotID))
			return this.robotTrackingPeriodInMillis.get(robotID);
		metaCSPLogger.warning("Tracking period of Robot" + robotID + " is not specified. Returning the default value.");
		return DEFAULT_ROBOT_TRACKING_PERIOD;
	}

	/**
	 * Utility method to treat internal resources from this library as filenames.
	 * @param resource The internal resource to be loaded.
	 * @return The absolute path of a temporary file which contains a copy of the resource.
	 */
	public static String getResourceAsFileName(String resource) {
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());
		ClassLoader classLoader = TrajectoryEnvelopeCoordinator.class.getClassLoader();
		File source = new File(classLoader.getResource(resource).getFile());
		File dest = new File("." + 1+rand.nextInt(1000) + ".tempfile");
		try { Files.copy(source.toPath(), dest.toPath()); }
		catch (IOException e) { e.printStackTrace(); }
		return dest.getAbsolutePath();
	}

	/**
	 * Set whether this {@link TrajectoryEnvelopeCoordinator} should print info at every period.
	 * @param value Set to <code>true</code> if this {@link TrajectoryEnvelopeCoordinator} should print info at every period.
	 */
	public void setQuiet(boolean value) {
		this.quiet = value;
	}

	/**
	 * Set the network parameters (packet loss probability, max delay and max faults probability).
	 * This allows to compute the number of messages <code>numberOfReplicas</code> required for each send to be effective
	 * (the probability of receiving a message after <code>numberOfReplicas</code> trials is assumed to have a geometric distribution).
	 * @param packetLossProbability The probability of a message being lost.
	 * @param max_tx_delay The maximum transmission delay in milliseconds.
	 * @param maxFaultsProbability The maximum probability of a piece of information (e.g., a precedence constraint)
	 * being lost that the coordinator should tolerate. This is assumed to be zero if <code>packetLossProbability</code> is zero.
	 */
	public void setNetworkParameters(double packetLossProbability, int max_tx_delay, double maxFaultsProbability) {
		this.packetLossProbability = packetLossProbability;
		MAX_TX_DELAY = max_tx_delay;
		this.maxFaultsProbability = maxFaultsProbability;
		this.numberOfReplicas =  (packetLossProbability > 0 && maxFaultsProbability > 0) ? (int)Math.ceil(Math.log(1-Math.sqrt(1-maxFaultsProbability))/Math.log(packetLossProbability)) : 1;
		metaCSPLogger.info("Number of replicas for each send: " + numberOfReplicas);
	}

	/**
	 * Set a {@link Callback} that will be called at every cycle.
	 * @param cb A {@link Callback} that will be called at every cycle.
	 */
	public void setInferenceCallback(Callback cb) {
		this.inferenceCallback = cb;
	}

	/**
	 * Get the control period of this {@link TrajectoryEnvelopeCoordinator}.
	 * @return the control period (in milliseconds) of this {@link TrajectoryEnvelopeCoordinator}.
	 */
	public int getControlPeriod() {
		return this.CONTROL_PERIOD;
	}

	/**
	 * Set whether robots that will park in a critical section should yield to other robots.
	 * @param value <code>true</code> if robots that will park in a critical section should yield to other robots.
	 */
	public void setYieldIfParking(boolean value) {
		this.yieldIfParking = value;
	}

	/**
	 * Set whether completely overlapping paths should lead to a warning.
	 * @param value <code>true</code> if completely overlapping paths should lead to a warning.
	 */
	public void setCheckEscapePoses(boolean value) {
		this.checkEscapePoses = value;
	}

	/**
	 * Toggle mute/unmute communication with a given robot.
	 * @param robotID The robot to toggle mute/unmute communication with.
	 */
	public void toggleMute(int robotID) {
		if (muted.contains(robotID)) muted.remove(robotID);
		else muted.add(robotID);
	}

	/**
	 * Mute communication with a given robot.
	 * @param robotID The robot to mute communication with.
	 */
	public void mute(int robotID) {
		muted.add(robotID);
	}

	/**
	 * Get the IDs of robots that are muted.
	 * @return The IDs of robots that are muted.
	 */
	public int[] getMuted() {
		int[] ret = new int[muted.size()];
		int counter = 0;
		for (Integer m : muted) ret[counter++] = m;
		return ret;
	}

	/**
	 * Unmute communication with a given robot.
	 * @param robotID The robot to unmute communication with.
	 */
	public void unMute(int robotID) {
		muted.remove(robotID);
	}

	protected Double getMaxFootprintDimension(int robotID) {
		if (this.footprints.containsKey(robotID)) return maxFootprintDimensions.get(robotID);
		return null;
	}

	/**
	 * Get the {@link Coordinate}s defining the footprint of a given robot.
	 * @param robotID the ID of the robot
	 * @return The {@link Coordinate}s defining the footprint of a given robot.
	 */
	public Coordinate[] getFootprint(int robotID) {
		if (this.footprints.containsKey(robotID)) return this.footprints.get(robotID);
		return null;
	}

	/**
	 * Set the {@link ForwardModel} of a given robot.
	 * @param robotID The ID of the robot.
	 * @param fm The robot's {@link ForwardModel}.
	 */
	public void setForwardModel(int robotID, ForwardModel fm) {
		this.forwardModels.put(robotID, fm);
	}

	/**
	 * Get the {@link ForwardModel} of a given robot.
	 * @param robotID The ID of the robot.
	 * @return The {@link ForwardModel} of the robot.
	 */
	public ForwardModel getForwardModel(int robotID) {
		if (forwardModels.containsKey(robotID)) return forwardModels.get(robotID);
		System.out.println("Returning default FM for " + robotID);
		return new ForwardModel() {
//			@Override
//			public boolean canStop(TrajectoryEnvelope te, RobotReport currentState, int targetPathIndex, boolean useVelocity) {
//				return true;
//			}
			@Override
			public int getEarliestStoppingPathIndex(TrajectoryEnvelope te, RobotReport currentState) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	/**
	 * Set the tracking period of a given robot in millis.
	 * @param robotID The ID of the robot.
	 * @param trackingPeriodInMillis The tracking period of the robot.
	 */
	public void setRobotTrackingPeriodInMillis(int robotID, int trackingPeriodInMillis) {
		this.robotTrackingPeriodInMillis.put(robotID, trackingPeriodInMillis);
	}

	/**
	 * Set the maximum velocity of a given robot in m/s.
	 * @param robotID The ID of the robot.
	 * @param maxVelocity The maximum velocity of the robot.
	 */
	public void setRobotMaxVelocity(int robotID, double maxVelocity) {
		this.robotMaxVelocity.put(robotID, maxVelocity);
	}

	/**
	 * Set the maximum acceleration of a given robot in m/s^2.
	 * @param robotID The ID of the robot.
	 * @param maxAcceleration The maximum acceleration of the robot.
	 */
	public void setRobotMaxAcceleration(int robotID, double maxAcceleration) {
		this.robotMaxAcceleration.put(robotID, maxAcceleration);
	}

	/**
	 * Get the maximum velocity of a given robot (m/s).
	 * @param robotID The ID of the robot.
	 * @return The maximum velocity of the robot (null if not specified --- this should never happen).
	 */
	public Double getRobotMaxVelocity(int robotID) {
		if (this.robotMaxVelocity.containsKey(robotID))
			return this.robotMaxVelocity.get(robotID);
		return null;
	}

	/**
	 * Get the maximum acceleration of a given robot (m/s^2).
	 * @param robotID The ID of the robot.
	 * @return The maximum acceleration of the robot (null if not specified --- this should never happen).
	 */
	public Double getRobotMaxAcceleration(int robotID) {
		if (this.robotMaxAcceleration.containsKey(robotID))
			return this.robotMaxAcceleration.get(robotID);
		return null;
	}

	protected void setupLogging() {
		//logDirName = "log-" + Calendar.getInstance().getTimeInMillis();
		logDirName = "logs";
		File dir = new File(logDirName);
		dir.mkdir();
		MetaCSPLogging.setLogDir(logDirName);

	}

	protected void writeStat(String fileName, String stat) {
		try {
			//Append to file
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(fileName), true));
			writer.println(stat);
			writer.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	protected void initStat(String fileName, String stat) {
		try {
			//Append to file
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(fileName), false));
			writer.println(stat);
			writer.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Get the {@link TrajectoryEnvelopeSolver} underlying this coordinator. This solver maintains the {@link TrajectoryEnvelope}s and temporal constraints
	 * among them.
	 * @return The {@link TrajectoryEnvelopeSolver} underlying this coordinator.
	 */
	public TrajectoryEnvelopeSolver getSolver() {
		return this.solver;
	}

	/**
	 * Instruct a given robot's tracker that it may not navigate beyond a given
	 * path index.
	 * @param robotID The ID of the robot.
	 * @param criticalPoint The index of the path pose beyond which the robot should not navigate.
	 * @param retransmitt True if the message should be send once again.
	 */
	public void setCriticalPoint(int robotID, int criticalPoint, boolean retransmitt) {

		synchronized (trackers) {
			AbstractTrajectoryEnvelopeTracker tracker = trackers.get(robotID);

			//If the robot is not muted
			if (tracker != null && !muted.contains(robotID) && !(tracker instanceof TrajectoryEnvelopeTrackerDummy)) {

				if (!communicatedCPs.containsKey(tracker) || communicatedCPs.containsKey(tracker) && communicatedCPs.get(tracker).getFirst() != criticalPoint || retransmitt ) {
					communicatedCPs.put(tracker, new Pair<Integer,Long>(criticalPoint, Calendar.getInstance().getTimeInMillis()));
					externalCPCounters.replace(tracker,externalCPCounters.get(tracker)+1);
					tracker.setCriticalPoint(criticalPoint, externalCPCounters.get(tracker)%Integer.MAX_VALUE);

					//for statistics
					totalMsgsSent.incrementAndGet();
					if (retransmitt) totalMsgsReTx.incrementAndGet();

					//metaCSPLogger.info("Sent critical point " + criticalPoint + " to Robot" + robotID +".");
				}
			}
		}
	}

	/**
	 * Get the current state of a given robot.
	 * @param robotID The ID of the robot of which the state should be returned.
	 * @return The current state of a given robot.
	 */
	public RobotReport getRobotReport(int robotID) {

		//Read the last message received
		synchronized (trackers) {
			if (!trackers.containsKey(robotID)) return null;
			return trackers.get(robotID).getLastRobotReport();
		}

	}

	/**
	 * Set the footprint of a given robot, which is used for computing spatial envelopes.
	 * Provide the bounding polygon of the machine assuming its reference point is in (0,0), and its
	 * orientation is aligned with the x-axis. The coordinates must be in CW or CCW order.
	 * @param robotID The ID of the robot.
	 * @param coordinates The coordinates delimiting bounding polygon of the footprint.
	 */
	public void setFootprint(int robotID, Coordinate ... coordinates) {
		this.footprints.put(robotID, coordinates);
		maxFootprintDimensions.put(robotID, computeMaxFootprintDimension(coordinates));
	}

	protected double computeMaxFootprintDimension(Coordinate[] coords) {
		ArrayList<Double> fpX = new ArrayList<Double>();
		ArrayList<Double> fpY = new ArrayList<Double>();
		for (Coordinate coord : coords) {
			fpX.add(coord.x);
			fpY.add(coord.y);
		}
		Collections.sort(fpX);
		Collections.sort(fpY);
		return Math.max(fpX.get(fpX.size()-1)-fpX.get(0), fpY.get(fpY.size()-1)-fpY.get(0));
	}

	/**
	 * Call this method to set up the solvers that manage the {@link TrajectoryEnvelope} representation
	 * underlying the coordinator.
	 * @param origin The origin of time (milliseconds).
	 * @param horizon The maximum time (milliseconds).
	 */
	public void setupSolver(long origin, long horizon) {
		//Create meta solver
		solver = new TrajectoryEnvelopeSolver(origin, horizon);
	}

	/**
	 * Call this method to start the thread that dispatches trajectories and critical points to robots,
	 * checking and enforcing dependencies at every clock tick.
	 */
	public void startInference() {
		if (solver == null) {
			metaCSPLogger.severe("Solver not initialized, please call method setupSolver() first!");
			throw new Error("Solver not initialized, please call method setupSolver() first!");
		}

		if (!stopInference) {
			metaCSPLogger.info("Inference is already started.");
			return;
		}

		//Start the thread that checks and enforces dependencies at every clock tick
		this.setupInferenceCallback();
	}

	/**
	 * Call this method to stop the thread that checks and enforces dependencies at every clock tick.
	 * This will also cause the robots to no longer receive new trajectories or updates of critical points,
	 * so the fleet will come to a (safe) stop once this method is called.
	 */
	public void stopInference() {

		if (solver == null) {
			metaCSPLogger.severe("Solver not initialized, please call method setupSolver() first!");
			throw new Error("Solver not initialized, please call method setupSolver() first!");
		}

		//Stop the thread that checks and enforces dependencies at every clock tick
		if (stopInference) metaCSPLogger.severe("Inference thread is not alive.");
		stopInference = true;
	}

	/**
	 * Call this method to check if the thread that checks and enforces dependencies at every clock tick is alive.
	 */
	public boolean isStartedInference() {
		return !stopInference;
	}

	/**
	 * Get the current time of the system, in milliseconds.
	 * @return The current time of the system, in milliseconds.
	 */
	public abstract long getCurrentTimeInMillis();

	/**
	 * Place a robot with a given ID in a given {@link Pose}.
	 * @param robotID The ID of the robot.
	 * @param currentPose The {@link Pose} in which to place the robot.
	 */
	public void placeRobot(final int robotID, Pose currentPose) {
		this.placeRobot(robotID, currentPose, null, currentPose.toString());
	}

	/**
	 * Place a robot with a given ID in the first {@link Pose} of a given {@link TrajectoryEnvelope}.
	 * @param robotID The ID of the robot.
	 * @param parking The {@link TrajectoryEnvelope} in which the robot is parked.
	 */
	public void placeRobot(final int robotID, TrajectoryEnvelope parking) {
		this.placeRobot(robotID, null, parking, null);
	}

	/**
	 * Place a robot with a given ID in a given {@link Pose} of a given {@link TrajectoryEnvelope},
	 * labeled with a given string.
	 * @param robotID The ID of the robot.
	 * @param currentPose The {@link Pose} of the robot.
	 * @param parking The {@link TrajectoryEnvelope} in which the robot is parked.
	 * @param location A label representing the {@link TrajectoryEnvelope}.
	 */
	public void placeRobot(final int robotID, Pose currentPose, TrajectoryEnvelope parking, String location) {

		if (solver == null) {
			metaCSPLogger.severe("Solver not initialized, please call method setupSolver() first!");
			throw new Error("Solver not initialized, please call method setupSolver() first!");
		}

		synchronized (solver) {
			//Create a new parking envelope
			long time = getCurrentTimeInMillis();

			//Can provide null parking or null currentPose, but not both
			if (parking == null) parking = solver.createParkingEnvelope(robotID, PARKING_DURATION, currentPose, location, getFootprint(robotID));
			else currentPose = parking.getTrajectory().getPose()[0];

			this.isDriving.put(robotID,false);

			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(time, time));
			release.setFrom(parking);
			release.setTo(parking);
			if (!solver.addConstraint(release)) {
				metaCSPLogger.severe("Could not release " + parking + " with constraint " + release);
				throw new Error("Could not release " + parking + " with constraint " + release);
			}
			metaCSPLogger.info("Placed " + parking.getComponent() + " in pose " + currentPose + ": " + parking);

			TrackingCallback cb = new TrackingCallback(parking) {
				@Override
				public void beforeTrackingStart() {
					if (trackingCallbacks.containsKey(robotID)) {
						trackingCallbacks.get(robotID).myTE = this.myTE;
						trackingCallbacks.get(robotID).beforeTrackingStart();
					}
				}
				@Override
				public void onTrackingStart() {
					if (trackingCallbacks.containsKey(robotID)) trackingCallbacks.get(robotID).onTrackingStart();
				}
				@Override
				public void onNewGroundEnvelope() {
					if (trackingCallbacks.containsKey(robotID)) trackingCallbacks.get(robotID).onNewGroundEnvelope();
				}
				@Override
				public void beforeTrackingFinished() {
					if (trackingCallbacks.containsKey(robotID)) trackingCallbacks.get(robotID).beforeTrackingFinished();
				}
				@Override
				public void onTrackingFinished() {
					if (trackingCallbacks.containsKey(robotID)) trackingCallbacks.get(robotID).onTrackingFinished();
				}
				@Override
				public String[] onPositionUpdate() {
					if (trackingCallbacks.containsKey(robotID)) return trackingCallbacks.get(robotID).onPositionUpdate();
					return null;
				}
			};

			//Now start the tracker for this parking (will be ended by call to addMissions for this robot)
			final TrajectoryEnvelopeTrackerDummy tracker = new TrajectoryEnvelopeTrackerDummy(parking, 300, TEMPORAL_RESOLUTION, this, cb) {
				@Override
				public long getCurrentTimeInMillis() {
					return tec.getCurrentTimeInMillis();
				}
			};

			currentParkingEnvelopes.add(tracker.getTrajectoryEnvelope());

			synchronized (trackers) {
				externalCPCounters.remove(trackers.get(robotID));
				trackers.remove(robotID);

				trackers.put(robotID, tracker);
				externalCPCounters.put(tracker, -1);
			}
		}

	}

	/**
	 * Get the {@link FleetVisualization} that is used for displaying the current fleet.
	 * @return The {@link FleetVisualization} that is used for displaying the current fleet.
	 */
	public FleetVisualization getVisualization() {
		return this.viz;
	}

	/**
	 * Get the list of current dependencies between robots.
	 * @return A list of {@link Dependency} objects.
	 */
	public HashMap<Integer, Dependency> getCurrentDependencies() {
		synchronized (this.currentDependencies) {
			return this.currentDependencies;
		}
	}

	/**
	 * Get the path index beyond which a robot should not navigate, given the {@link TrajectoryEnvelope} of another robot.
	 * @return The path index beyond which a robot should not navigate, given the {@link TrajectoryEnvelope} of another robot.
	 */
	protected int getCriticalPoint(int yieldingRobotID, CriticalSection criticalSection, int leadingRobotCurrentPathIndex) {

        int trailingPathPoints = 3;
        if (!VehiclesHashMap.getList().isEmpty())
            trailingPathPoints = VehiclesHashMap.getVehicle(yieldingRobotID).getSafetyPathPoints();

        var leadingRobotStart = -1;
        var yieldingRobotStart = -1;
        var leadingRobotEnd = -1;
        var yieldingRobotEnd = -1;
		TrajectoryEnvelope leadingRobotTE;
		TrajectoryEnvelope yieldingRobotTE;
		if (criticalSection.getTrajectoryEnvelope1().getRobotID() == yieldingRobotID) {
			leadingRobotStart = criticalSection.getTrajectoryEnvelopeStart2();
			yieldingRobotStart = criticalSection.getTrajectoryEnvelopeStart1();
			leadingRobotEnd = criticalSection.getTrajectoryEnvelopeEnd2();
			yieldingRobotEnd = criticalSection.getTrajectoryEnvelopeEnd1();
			leadingRobotTE = criticalSection.getTrajectoryEnvelope2();
			yieldingRobotTE = criticalSection.getTrajectoryEnvelope1();
		}
		else {
			leadingRobotStart = criticalSection.getTrajectoryEnvelopeStart1();
			yieldingRobotStart = criticalSection.getTrajectoryEnvelopeStart2();
			leadingRobotEnd = criticalSection.getTrajectoryEnvelopeEnd1();
			yieldingRobotEnd = criticalSection.getTrajectoryEnvelopeEnd2();
			leadingRobotTE = criticalSection.getTrajectoryEnvelope1();
			yieldingRobotTE = criticalSection.getTrajectoryEnvelope2();
		}

		if (leadingRobotCurrentPathIndex < leadingRobotStart) {
			return Math.max(0, yieldingRobotStart- trailingPathPoints);
		}

		//Compute sweep of robot 1's footprint from current position to LOOKAHEAD
        var leadingRobotPose = leadingRobotTE.getTrajectory().getPose()[leadingRobotCurrentPathIndex];
        var leadingRobotInPose = TrajectoryEnvelope.getFootprint(leadingRobotTE.getFootprint(), leadingRobotPose.getX(), leadingRobotPose.getY(), leadingRobotPose.getTheta());
		if (leadingRobotCurrentPathIndex <= leadingRobotEnd) {
			for (int i = leadingRobotCurrentPathIndex+1; i <= leadingRobotEnd; i++) {
                var leadingRobotNextPose = leadingRobotTE.getTrajectory().getPose()[i];
				try {
					leadingRobotInPose = leadingRobotInPose.union(TrajectoryEnvelope.getFootprint(leadingRobotTE.getFootprint(), leadingRobotNextPose.getX(), leadingRobotNextPose.getY(), leadingRobotNextPose.getTheta()));
				} catch (Exception e) { e.printStackTrace(); }
			}
		}

		//Return pose at which yielding robot should stop given driving robot's projected sweep
		for (int i = yieldingRobotStart; i <= yieldingRobotEnd; i++) {
            var yieldingRobotPose = yieldingRobotTE.getTrajectory().getPose()[i];
            var yieldingRobotInPose = TrajectoryEnvelope.getFootprint(yieldingRobotTE.getFootprint(), yieldingRobotPose.getX(), yieldingRobotPose.getY(), yieldingRobotPose.getTheta());
			if (leadingRobotInPose.intersects(yieldingRobotInPose)) {
				return Math.max(0, i- trailingPathPoints);
			}
		}

		//The only situation where the above has not returned is when robot 2 should
		//stay "parked", therefore wait at index 0
		return Math.max(0, yieldingRobotStart- trailingPathPoints);
	}

	/**
	 * Returns <code>true</code> iff the given robot is at a stopping point.
	 * @param robotID The ID of a robot.
	 * @return <code>true</code> iff the given robot is at a stopping point.
	 */
	public boolean atStoppingPoint(int robotID) {
		return stoppingPointTimers.containsKey(robotID);
	}

	/** Spawn a waiting thread at this stopping point.
	 * @param robotID Which robot should wait.
	 * @param index The stopping point.
	 * @param duration Duration of the stopping.
	 */
	protected void spawnWaitingThread(final int robotID, final int index, final int duration) {
		Thread stoppingPointTimer = new Thread() {
			private final long startTime = Calendar.getInstance().getTimeInMillis();
			@Override
			public void run() {
				metaCSPLogger.info("Waiting thread starts for " + robotID);
				while (Calendar.getInstance().getTimeInMillis()-startTime < duration) {
					try { Thread.sleep(100); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				metaCSPLogger.info("Waiting thread finishes for " + robotID);
				synchronized(solver) {
					synchronized(stoppingPoints) {
						stoppingPoints.get(robotID).remove(index);
						stoppingTimes.get(robotID).remove(index);
						stoppingPointTimers.remove(robotID);
					}
					updateDependencies();
				}
			}
		};
		stoppingPointTimers.put(robotID,stoppingPointTimer);
		stoppingPointTimer.start();
	}

	/**
	 * Return true if the robot is in its parking pose.
	 * @param robotID The robot to be checked.
	 * @return True if the robot is in its parking pose.
	 */
	protected boolean inParkingPose(int robotID) {
		return this.getRobotReport(robotID).getPathIndex() == -1;
	}

	protected Geometry[] getObstaclesInCriticalPoints(int ... robotIDs) {
		//Compute one obstacle per given robot, placed in the robot's waiting pose
        var ret = new ArrayList<Geometry>();
		for (int robotID : robotIDs) {
			AbstractTrajectoryEnvelopeTracker tracker = null;
			synchronized(trackers) {
				tracker = trackers.get(robotID);
			}
			Geometry currentFP = null;
			if (tracker instanceof TrajectoryEnvelopeTrackerDummy) { // the robot is parked
				Pose currentPose = this.getRobotReport(robotID).getPose();
				currentFP = makeObstacles(robotID, currentPose)[0];
			}
			else {
				HashMap<Integer, Dependency> currentDeps = getCurrentDependencies();
				Dependency dep = currentDeps.containsKey(robotID) ? getCurrentDependencies().get(robotID) : null;
				Pose waitingPose = (dep == null) ? tracker.getTrajectoryEnvelope().getTrajectory().getPose()[tracker.getTrajectoryEnvelope().getTrajectory().getPose().length-1] : dep.getWaitingPose();
				currentFP = makeObstacles(robotID, waitingPose)[0];

				//In case the robot has stopped a little beyond the critical point
				int currentPoint = this.getRobotReport(robotID).getPathIndex();
				if (currentPoint != -1 && dep != null && currentPoint > dep.getWaitingPoint()) {
					Pose currentPose = dep.getWaitingTrajectoryEnvelope().getTrajectory().getPose()[currentPoint];
					currentFP = makeObstacles(robotID, currentPose)[0];
					System.out.println("Oops: " + dep.getWaitingPoint() + " < " + currentPoint);
				}
			}
			ret.add(currentFP);
		}
		return ret.toArray(new Geometry[ret.size()]);
	}

	protected Geometry[] getObstaclesFromWaitingRobots(int robotID) {
		//Compute one obstacle per robot that is waiting for this robot, placed in the waiting robot's waiting pose
		ArrayList<Geometry> ret = new ArrayList<>();
		HashMap<Integer, Dependency> currentDeps = getCurrentDependencies();
		Dependency dep = currentDeps.containsKey(robotID) ? currentDeps.get(robotID) : null;
		if (dep != null) {
			Pose waitingPose  = dep.getWaitingTrajectoryEnvelope().getTrajectory().getPose()[dep.getWaitingPoint()];
			ret.add(makeObstacles(robotID, waitingPose)[0]);
		}
		return ret.toArray(new Geometry[ret.size()]);
	}

	/**
	 * Plan a new path from a given starting pose to a target one, calling the defined motion planner.
	 * The given set of obstacles is added to the map used for planning.
	 * @param mp The motion planner to use.
	 * @param fromPose Starting pose.
	 * @param toPose Target pose.
	 * @param obstaclesToConsider Obstacles to be added to the map used for planning.
	 * @return
	 */
	protected PoseSteering[] doReplanning(AbstractMotionPlanner mp, Pose fromPose, Pose toPose, Geometry... obstaclesToConsider) {
		if (mp == null) return null;
		synchronized (mp) {
			mp.setStart(fromPose);
			mp.setGoals(toPose);
			//mp.clearObstacles();
			if (obstaclesToConsider != null && obstaclesToConsider.length > 0) mp.addObstacles(obstaclesToConsider);
			boolean replanningSuccessful = mp.plan();
			if (!replanningSuccessful) {
				try { mp.writeDebugImage(); }
				catch (NullPointerException nex) { System.out.println("Failed to write debug image"); }
			}
			if (obstaclesToConsider != null && obstaclesToConsider.length > 0) mp.clearObstacles();
			if (replanningSuccessful) return mp.getPath();
			return null;
		}
	}

	/**
	 * Generate obstacles representing the placement(s) of a given robot in given poses.
	 * @param robotID The ID of the robot whose footprint should be used.
	 * @param obstaclePoses The poses of the footprint.
	 * @return A {@link Geometry} that has the shape of the given robot's footprint, placed in each of the the given {@link Pose}s.
	 */
	public Geometry[] makeObstacles(int robotID, Pose ... obstaclePoses) {
		ArrayList<Geometry> ret = new ArrayList<Geometry>();
		for (Pose p : obstaclePoses) {
			GeometryFactory gf = new GeometryFactory();
			Coordinate[] footprint = this.getFootprint(robotID);
			Coordinate[] newFoot = new Coordinate[footprint.length+1];
            System.arraycopy(footprint, 0, newFoot, 0, footprint.length);
			newFoot[footprint.length] = footprint[0];
			Geometry obstacle = gf.createPolygon(newFoot);
			AffineTransformation at = new AffineTransformation();
			at.rotate(p.getTheta());
			at.translate(p.getX(), p.getY());
			obstacle = at.transform(obstacle);
			ret.add(obstacle);
			metaCSPLogger.fine("Made obstacle for Robot" + robotID + " in pose " + p);
		}
		return ret.toArray(new Geometry[ret.size()]);
	}

	/**
	 * The key function of all the coordination algorithm,
	 * defining how dependencies should be updated every coordination cycle.
	 */
	protected abstract void updateDependencies();

	/**
	 * Return if a robot, which is starting from a critical sections,
	 * is able to exit safely from it.
	 */
	protected boolean canExitCriticalSection(int drivingCurrentIndex, int waitingCurrentIndex, TrajectoryEnvelope drivingTE, TrajectoryEnvelope waitingTE, int lastIndexOfCSDriving) {
		drivingCurrentIndex = Math.max(drivingCurrentIndex,0);
		waitingCurrentIndex = Math.max(waitingCurrentIndex,0);
		Geometry placementWaiting = waitingTE.makeFootprint(waitingTE.getTrajectory().getPoseSteering()[waitingCurrentIndex]);
		for (int i = drivingCurrentIndex; i <= lastIndexOfCSDriving; i++) {
			Geometry placementDriving = drivingTE.makeFootprint(drivingTE.getTrajectory().getPoseSteering()[i]);
			if (placementWaiting.intersects(placementDriving)) return false;
		}
		return true;
	}


	/**
	 * Set a motion planner to be used for re-planning for a specific
	 * robot.
	 * @param robotID The robot for which the given motion planner should be used.
	 * @param mp The motion planner that will be called for re-planning.
	 */
	public void setMotionPlanner(int robotID, AbstractMotionPlanner mp) {
		this.motionPlanners.put(robotID, mp);
	}


	/**
	 * Get the motion planner used for re-planning for a specific robot.
	 * @param robotID The ID of a robot.
	 * @return The motion planner used for re-planning for the given robot.
	 */
	public AbstractMotionPlanner getMotionPlanner(int robotID) {
		return this.motionPlanners.get(robotID);
	}


	/**
	 * Add a criterion for determining the order of robots through critical sections
	 * (comparator of {@link AbstractTrajectoryEnvelopeTracker}s).
	 * Comparators are considered in the order in which they are added.
	 * @param c A new comparator for determining robot ordering through critical sections.
	 */
	public void addComparator(Comparator<RobotAtCriticalSection> c) {
		this.comparators.addComparator(c);
	}

	public void clearComparator() {
		this.comparators = new ComparatorChain();
	}

	protected void computeCriticalSections() {

		int numberOfCriticalSections = 0;

		synchronized(allCriticalSections) {
			synchronized (trackers) {
				var currentReports = getCurrentReports();
				var drivingEnvelopes = getDrivingEnvelops();
				getCSDrivingEnvelopes(drivingEnvelopes, envelopesToTrack, currentReports);
				getCSNewEnvelopes(currentReports);
				getCSDrivingEnvelopes(drivingEnvelopes, currentParkingEnvelopes, currentReports);
				getCSDrivingEnvelopes(envelopesToTrack, currentParkingEnvelopes, currentReports);
			}
			filterCriticalSections();
			numberOfCriticalSections = this.allCriticalSections.size();
			metaCSPLogger.info("There are now " + numberOfCriticalSections + " critical sections");
		}
		onCriticalSectionUpdate();
	}

	private HashMap<Integer, RobotReport> getCurrentReports() {
		var currentReports = new HashMap<Integer, RobotReport>();
		for (int robotID : trackers.keySet()) currentReports.put(robotID, this.getRobotReport(robotID));
		return currentReports;
	}

	private ArrayList<TrajectoryEnvelope> getDrivingEnvelops() {
		var drivingEnvelopes = new ArrayList<TrajectoryEnvelope>();
		for (AbstractTrajectoryEnvelopeTracker atet : trackers.values())
			if (!(atet instanceof TrajectoryEnvelopeTrackerDummy))
				drivingEnvelopes.add(atet.getTrajectoryEnvelope());
		return drivingEnvelopes;
	}

	private void getCSDrivingEnvelopes(ArrayList<TrajectoryEnvelope> drivingEnvelopes, ArrayList<TrajectoryEnvelope> envelopesToTrack, HashMap<Integer, RobotReport> currentReports) {
		for (TrajectoryEnvelope drivingEnvelope : drivingEnvelopes) {
			for (TrajectoryEnvelope trajectoryEnvelope : envelopesToTrack) {
				if (drivingEnvelope.getRobotID() != trajectoryEnvelope.getRobotID()) {
					int minStart1 = currentReports.containsKey(drivingEnvelope.getRobotID()) ? currentReports.get(drivingEnvelope.getRobotID()).getPathIndex() : -1;
					int minStart2 = currentReports.containsKey(trajectoryEnvelope.getRobotID()) ? currentReports.get(trajectoryEnvelope.getRobotID()).getPathIndex() : -1;
					double maxDimensionOfSmallestRobot = Math.min(getMaxFootprintDimension(drivingEnvelope.getRobotID()), getMaxFootprintDimension(trajectoryEnvelope.getRobotID()));
					Collections.addAll(this.allCriticalSections, getCriticalSections(null, null, drivingEnvelope, minStart1, trajectoryEnvelope, minStart2, this.checkEscapePoses, maxDimensionOfSmallestRobot));
				}
			}
		}
	}

	private void getCSNewEnvelopes(HashMap<Integer, RobotReport> currentReports) {
		for (int i = 0; i < envelopesToTrack.size(); i++) {
			for (int j = i+1; j < envelopesToTrack.size(); j++) {
				if (envelopesToTrack.get(i).getRobotID() != envelopesToTrack.get(j).getRobotID()) {
					int minStart1 = currentReports.containsKey(envelopesToTrack.get(i).getRobotID()) ? currentReports.get(envelopesToTrack.get(i).getRobotID()).getPathIndex() : -1;
					int minStart2 = currentReports.containsKey(envelopesToTrack.get(j).getRobotID()) ? currentReports.get(envelopesToTrack.get(j).getRobotID()).getPathIndex() : -1;
					double maxDimensionOfSmallestRobot = Math.min(getMaxFootprintDimension(envelopesToTrack.get(i).getRobotID()), getMaxFootprintDimension(envelopesToTrack.get(j).getRobotID()));
Collections.addAll(this.allCriticalSections, getCriticalSections(null, null, envelopesToTrack.get(i), minStart1, envelopesToTrack.get(j), minStart2, this.checkEscapePoses, maxDimensionOfSmallestRobot));
				}
			}
		}
	}

	/**
	 * Utility that is called at the end of each critical section update.
	 */
	protected void onCriticalSectionUpdate() {}

    protected void filterCriticalSections() {
        var toRemove = new ArrayList<CriticalSection>();
        var allCriticalSectionsList = new ArrayList<CriticalSection>();
		for (CriticalSection cs : this.allCriticalSections) allCriticalSectionsList.add(cs);

		for (int i = 0; i < allCriticalSectionsList.size(); i++) {
			for (int j = i+1; j < allCriticalSectionsList.size(); j++) {
                var cs1 = allCriticalSectionsList.get(i);
                var cs2 = allCriticalSectionsList.get(j);
				if (cs1.equals(cs2)) {
					toRemove.add(cs1);
					metaCSPLogger.finest("Removed one of " + cs1 + " and " + cs2);
				}
			}
		}
		for (CriticalSection cs : toRemove) this.allCriticalSections.remove(cs);
	}

	public static CriticalSection[] getCriticalSections(SpatialEnvelope se1, SpatialEnvelope se2, TrajectoryEnvelope te1, int minStart1, TrajectoryEnvelope te2, int minStart2, boolean checkEscapePoses, double maxDimensionOfSmallestRobot) {

		ArrayList<CriticalSection> css = new ArrayList<CriticalSection>();

		//		GeometricShapeVariable poly1 = te1.getEnvelopeVariable();
		//		GeometricShapeVariable poly2 = te2.getEnvelopeVariable();
		//		Geometry shape1 = ((GeometricShapeDomain)poly1.getDomain()).getGeometry();
		//		Geometry shape2 = ((GeometricShapeDomain)poly2.getDomain()).getGeometry();

		if (te1 != null) se1 = te1.getSpatialEnvelope();
		if (te2 != null) se2 = te2.getSpatialEnvelope();

		Geometry shape1 = se1.getPolygon();
		Geometry shape2 = se2.getPolygon();

		if (shape1.intersects(shape2)) {
			//			PoseSteering[] path1 = te1.getTrajectory().getPoseSteering();
			//			PoseSteering[] path2 = te2.getTrajectory().getPoseSteering();

			PoseSteering[] path1 = se1.getPath();
			PoseSteering[] path2 = se2.getPath();

			if (checkEscapePoses) {
				//Check that there is an "escape pose" along the paths
				boolean safe = false;
				for (int j = 0; j < path1.length; j++) {
					//Geometry placement1 = te1.makeFootprint(path1[j]);
					Geometry placement1 = TrajectoryEnvelope.getFootprint(se1.getFootprint(), path1[j].getPose().getX(), path1[j].getPose().getY(), path1[j].getPose().getTheta());
					if (!placement1.intersects(shape2)) {
						safe = true;
						break;
					}
				}
				if (path1.length == 1 || path2.length == 1) safe = true;
				if (!safe) {
					metaCSPLogger.severe("** WARNING ** Cannot coordinate as one envelope is completely overlapped by the other!");
					metaCSPLogger.severe("** " + te1 + " <--> " + te2);
					//throw new Error("Cannot coordinate as one envelope is completely overlapped by the other!");
				}

				safe = false;
				for (int j = 0; j < path2.length; j++) {
					//Geometry placement2 = te2.makeFootprint(path2[j]);
					Geometry placement2 = TrajectoryEnvelope.getFootprint(se2.getFootprint(), path2[j].getPose().getX(), path2[j].getPose().getY(), path2[j].getPose().getTheta());
					if (!placement2.intersects(shape1)) {
						safe = true;
						break;
					}
				}
				if (path1.length == 1 || path2.length == 1) safe = true;
				if (!safe) {
					metaCSPLogger.severe("** WARNING ** Cannot coordinate as one envelope is completely overlapped by the other!");
					metaCSPLogger.severe("** " + te1 + " <--> " + te2);
					//throw new Error("Cannot coordinate as one envelope is completely overlapped by the other!");
				}
			}

			Geometry gc = shape1.intersection(shape2);
			ArrayList<Geometry> allIntersections = new ArrayList<Geometry>();
			if (gc.getNumGeometries() == 1) {
				allIntersections.add(gc);
			}
			else {
				for (int i = 1; i < gc.getNumGeometries(); i++) {
					Geometry prev = gc.getGeometryN(i-1);
					Geometry next = gc.getGeometryN(i);
					if (prev.distance(next) < maxDimensionOfSmallestRobot) {
						allIntersections.add(prev.union(next).convexHull());
					}
					else {
						allIntersections.add(prev);
						if (i == gc.getNumGeometries()-1) allIntersections.add(next);
					}
				}
			}

			for (int i = 0; i < allIntersections.size(); i++) {
				ArrayList<CriticalSection> cssOneIntersectionPiece = new ArrayList<CriticalSection>();
				ArrayList<Integer> te1Starts = new ArrayList<Integer>();
				ArrayList<Integer> te1Ends = new ArrayList<Integer>();
				ArrayList<Integer> te2Starts = new ArrayList<Integer>();
				ArrayList<Integer> te2Ends = new ArrayList<Integer>();

				Geometry g = allIntersections.get(i);
				boolean started = false;
				for (int j = 0; j < path1.length; j++) {
					//Geometry placement1 = te1.makeFootprint(path1[j]);
					Geometry placement1 = TrajectoryEnvelope.getFootprint(se1.getFootprint(), path1[j].getPose().getX(), path1[j].getPose().getY(), path1[j].getPose().getTheta());
					if (!started && placement1.intersects(g)) {
						started = true;
						te1Starts.add(j);
					}
					else if (started && !placement1.intersects(g)) {
						te1Ends.add(j-1 > 0 ? j-1 : 0);
						started = false;
					}
					if (started && j == path1.length-1) {
						te1Ends.add(path1.length-1);
					}
				}
				started = false;
				for (int j = 0; j < path2.length; j++) {
					//Geometry placement2 = te2.makeFootprint(path2[j]);
					Geometry placement2 = TrajectoryEnvelope.getFootprint(se2.getFootprint(), path2[j].getPose().getX(), path2[j].getPose().getY(), path2[j].getPose().getTheta());
					if (!started && placement2.intersects(g)) {
						started = true;
						te2Starts.add(j);
					}
					else if (started && !placement2.intersects(g)) {
						te2Ends.add(j-1 > 0 ? j-1 : 0);
						started = false;
					}
					if (started && j == path2.length-1) {
						te2Ends.add(path2.length-1);
					}
				}
				for (int k1 = 0; k1 < te1Starts.size(); k1++) {
					for (int k2 = 0; k2 < te2Starts.size(); k2++) {
						if (te1Ends.get(k1) >= Math.max(0, minStart1) && te2Ends.get(k2) >= Math.max(0, minStart2)) {
							CriticalSection oneCS = new CriticalSection(te1, te2, te1Starts.get(k1), te2Starts.get(k2), te1Ends.get(k1), te2Ends.get(k2));
							//css.add(oneCS);
							cssOneIntersectionPiece.add(oneCS);
						}

					}
				}

				//pre-filter obsolete critical sections to avoid merging them with the new computed.
				te1Starts.clear();
				te2Starts.clear();
				te1Ends.clear();
				te2Ends.clear();
				for (CriticalSection cs : cssOneIntersectionPiece) {
					te1Starts.add(cs.getTrajectoryEnvelopeStart1());
					te2Starts.add(cs.getTrajectoryEnvelopeStart2());
					te1Ends.add(cs.getTrajectoryEnvelopeEnd1());
					te2Ends.add(cs.getTrajectoryEnvelopeEnd2());
				}


				// SPURIOUS INTERSECTIONS (can ignore)
				if (te1Starts.size() == 0 || te2Starts.size() == 0) {
					cssOneIntersectionPiece.clear();
				}

				// ASYMMETRIC INTERSECTIONS OF ENVELOPES
				// There are cases in which there are more starts along one envelope than along the other
				// (see the Epiroc underground mining example).
				// These "holes" may or may not be big enough to accommodate a robot. Those that are not
				// should be filtered, as they falsely indicate that the critical section ends for a little bit
				// before restarting. Because of this, such situations may lead to collision.
				// Here, we take a conservative approach: instead of verifying whether
				// the "hole" is big enough to really accommodate a robot so that it does not collide with
				// the other envelope, we simply filter out all of these cases. We do this by joining the
				// critical sections around holes.
				else if (te1Starts.size() != te2Starts.size()) {
					if (te1Starts.size() == 0 || te2Starts.size() == 0) System.out.println("CRAP: te1Starts is " + te1Starts + " and te2Starts is " + te2Starts);
					metaCSPLogger.info("Asymmetric intersections of envelopes for Robot" + te1.getRobotID() + ", Robot" + te2.getRobotID() + ":");
					metaCSPLogger.info("   Original : " + cssOneIntersectionPiece);
					CriticalSection oldCSFirst = cssOneIntersectionPiece.get(0);
					CriticalSection oldCSLast = cssOneIntersectionPiece.get(cssOneIntersectionPiece.size()-1);
					CriticalSection newCS = new CriticalSection(te1, te2, oldCSFirst.getTrajectoryEnvelopeStart1(), oldCSFirst.getTrajectoryEnvelopeStart2(), oldCSLast.getTrajectoryEnvelopeEnd1(), oldCSLast.getTrajectoryEnvelopeEnd2());
					cssOneIntersectionPiece.clear();
					cssOneIntersectionPiece.add(newCS);
					metaCSPLogger.info("   Refined  : " + cssOneIntersectionPiece);
				}

				css.addAll(cssOneIntersectionPiece);

			}
		}

		return css.toArray(new CriticalSection[css.size()]);
	}


	public static CriticalSection[] getCriticalSections(SpatialEnvelope se1, SpatialEnvelope se2, TrajectoryEnvelope te1, TrajectoryEnvelope te2, boolean checkEscapePoses, double maxDimensionOfSmallestRobot) {
		return getCriticalSections(se1, se2, te1, -1, te2, -1, checkEscapePoses, maxDimensionOfSmallestRobot);
	}

	public static CriticalSection[] getCriticalSections(SpatialEnvelope se1, SpatialEnvelope se2, boolean checkEscapePoses, double maxDimensionOfSmallestRobot) {
		return getCriticalSections(se1, se2, null, -1, null, -1, checkEscapePoses, maxDimensionOfSmallestRobot);
	}

	public static CriticalSection[] getCriticalSections(SpatialEnvelope se1, SpatialEnvelope se2, double maxDimensionOfSmallestRobot) {
		return getCriticalSections(se1, se2, null, -1, null, -1, true, maxDimensionOfSmallestRobot);
	}

	protected void cleanUp(TrajectoryEnvelope te) {
		synchronized (solver) {
			metaCSPLogger.info("Cleaning up " + te);
			Constraint[] consToRemove = solver.getConstraintNetwork().getIncidentEdgesIncludingDependentVariables(te);
			solver.removeConstraints(consToRemove);
			solver.removeVariable(te);
		}
	}

	protected void cleanUpRobotCS(int robotID, int lastWaitingPoint) {
		synchronized (allCriticalSections) {
			metaCSPLogger.info("Cleaning up critical sections of Robot" + robotID);
			ArrayList<CriticalSection> toRemove = new ArrayList<CriticalSection>();
			//Clear the critical sections for which we have stored a dependency ...
			for (CriticalSection cs : CSToDepsOrder.keySet()) {
				if (cs.getTrajectoryEnvelope1().getRobotID() == robotID || cs.getTrajectoryEnvelope2().getRobotID() == robotID) toRemove.add(cs);
			}
			//... and all the critical sections which are currently alive.
			for (CriticalSection cs : allCriticalSections) {
				if ((cs.getTrajectoryEnvelope1().getRobotID() == robotID || cs.getTrajectoryEnvelope2().getRobotID() == robotID) && !toRemove.contains(cs)) {
					metaCSPLogger.severe("<<<<<<<< WARNING: Cleaning up a critical section which was not associated to a dependency: " + cs + ".");
					toRemove.add(cs);
					//increment the counter
					if (cs.getTrajectoryEnvelope1().getRobotID() == robotID && (cs.getTrajectoryEnvelopeStart1() <= lastWaitingPoint || lastWaitingPoint == -1) ||
							cs.getTrajectoryEnvelope2().getRobotID() == robotID && (cs.getTrajectoryEnvelopeStart2() <= lastWaitingPoint || lastWaitingPoint == -1))
						this.criticalSectionCounter.incrementAndGet();
				}
			}
			for (CriticalSection cs : toRemove) {
				CSToDepsOrder.remove(cs);
				allCriticalSections.remove(cs);
				escapingCSToWaitingRobotIDandCP.remove(cs);
			}
		}
	}

	/**
	 * Get the {@link TrajectoryEnvelope} currently being tracked for a given robot.
	 * @param robotID The ID of the robot for which to retrieve the {@link TrajectoryEnvelope} currently
	 * being tracked.
	 * @return The {@link TrajectoryEnvelope} currently being tracked for the given robot.
	 */
	public TrajectoryEnvelope getCurrentSuperEnvelope(int robotID) {
		return trackers.get(robotID).getTrajectoryEnvelope();
	}

	/**
	 * Add a {@link TrackingCallback} that will be called by the tracker of a given robot.
	 * @param robotID The ID of the robot to which the callback should be attached.
	 * @param cb A callback object.
	 */
	public void addTrackingCallback(int robotID, TrackingCallback cb) {
		trackingCallbacks.put(robotID, cb);
	}

	/**
	 * Start the trackers associated to the last batch of {@link Mission}s that has been added.
	 */
	protected void startTrackingAddedMissions() {

		//FIXME: if this is not placed into the control loop (), then a robot can pass from (P) to (D) without
		// affecting the set of dependencies.

		synchronized (solver) {
			for (final TrajectoryEnvelope te : envelopesToTrack) {
				TrajectoryEnvelopeTrackerDummy startParkingTracker = null;
				synchronized (trackers) {
					startParkingTracker = (TrajectoryEnvelopeTrackerDummy)trackers.get(te.getRobotID());
				}
//				startParkingTracker.checkPause();
				final TrajectoryEnvelope startParking = startParkingTracker.getTrajectoryEnvelope();
				//Create end parking envelope
				final TrajectoryEnvelope endParking = solver.createParkingEnvelope(te.getRobotID(), PARKING_DURATION, te.getTrajectory().getPose()[te.getTrajectory().getPose().length-1], "whatever", getFootprint(te.getRobotID()));

				//Driving meets final parking
				AllenIntervalConstraint meets1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meets1.setFrom(te);
				meets1.setTo(endParking);
				//System.out.println("Made end parking: " + endParking + " with con: " + meets1);

				if (!solver.addConstraints(meets1)) {
					metaCSPLogger.severe("ERROR: Could not add constraints " + meets1);
					throw new Error("Could not add constraints " + meets1);
				}

				//Add onStart call back that cleans up parking tracker
				//Note: onStart is triggered only when earliest start of this tracker's envelope is < current time
				TrackingCallback cb = new TrackingCallback(te) {

					private long lastEnvelopeRefresh = Calendar.getInstance().getTimeInMillis();
					private boolean trackingFinished = false;

					@Override
					public void beforeTrackingStart() {

						if (trackingCallbacks.containsKey(myTE.getRobotID())) {
							trackingCallbacks.get(myTE.getRobotID()).myTE = this.myTE;
							trackingCallbacks.get(myTE.getRobotID()).beforeTrackingStart();
						}

						//canStartTracking becomes true when setCriticalPoint is called once
						while (!trackers.containsKey(myTE.getRobotID()) || !trackers.get(myTE.getRobotID()).canStartTracking()) {
							try { Thread.sleep(100); }
							catch (InterruptedException e) { e.printStackTrace(); }
						}

						//						//Sleep for one control period
						//						//(allows to impose critical points before tracking actually starts)
						//						try { Thread.sleep(CONTROL_PERIOD); }
						//						catch (InterruptedException e) { e.printStackTrace(); }
					}

					@Override
					public void onTrackingStart() {
						if (trackingCallbacks.containsKey(myTE.getRobotID())) trackingCallbacks.get(myTE.getRobotID()).onTrackingStart();
						if (viz != null) viz.addEnvelope(myTE);
					}

					@Override
					public void onNewGroundEnvelope() {
						if (trackingCallbacks.containsKey(myTE.getRobotID())) trackingCallbacks.get(myTE.getRobotID()).onNewGroundEnvelope();
					}

					@Override
					public void beforeTrackingFinished() {
						this.trackingFinished = true;
						if (trackingCallbacks.containsKey(myTE.getRobotID())) trackingCallbacks.get(myTE.getRobotID()).beforeTrackingFinished();
					}

					@Override
					public void onTrackingFinished() {

						synchronized (solver) {
							metaCSPLogger.info("Tracking finished for " + myTE);

							if (trackingCallbacks.containsKey(myTE.getRobotID())) trackingCallbacks.get(myTE.getRobotID()).onTrackingFinished();

							if (viz != null) viz.removeEnvelope(myTE);

							//reset stopping points
							synchronized(stoppingPoints) {
								stoppingPoints.remove(myTE.getRobotID());
								stoppingTimes.remove(myTE.getRobotID());
							}

							//remove critical sections in which this robot is involved
							cleanUpRobotCS(myTE.getRobotID(), -1);

							//clean up the old parking envelope
							cleanUp(startParking);
							currentParkingEnvelopes.remove(startParking);

							//Find end parking...
							TrajectoryEnvelope myEndParking = null;
							for (Constraint con : solver.getConstraintNetwork().getOutgoingEdges(myTE)) {
								if (con instanceof AllenIntervalConstraint) {
									AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
									if (aic.getTypes()[0].equals(AllenIntervalConstraint.Type.Meets)) {
										myEndParking = (TrajectoryEnvelope)aic.getTo();
										break;
									}
								}
							}

							//clean up this tracker's TE
							cleanUp(myTE);

							//remove communicatedCP entry
							communicatedCPs.remove(trackers.get(myTE.getRobotID()));

							//Make a new parking tracker for the found end parking (park the robot)
							placeRobot(myTE.getRobotID(), null, myEndParking, null);

							computeCriticalSections();
							updateDependencies();
						}

					}

					@Override
					public String[] onPositionUpdate() {
						if (viz != null && !trackingFinished && viz.periodicEnvelopeRefreshInMillis() > 0) {
							long timeNow = Calendar.getInstance().getTimeInMillis();
							if (timeNow-lastEnvelopeRefresh > viz.periodicEnvelopeRefreshInMillis()) {
								viz.addEnvelope(myTE);
								lastEnvelopeRefresh = timeNow;
							}
						}
						if (trackingCallbacks.containsKey(myTE.getRobotID())) return trackingCallbacks.get(myTE.getRobotID()).onPositionUpdate();
						return null;
					}

				};

				synchronized (trackers) {
					externalCPCounters.remove(trackers.get(te.getRobotID()));

					ArrayList<AutonomousVehicle> recievedSTop = trackers.get(te.getRobotID()).getPauseCounter();
					trackers.remove(te.getRobotID());

					//Make a new tracker for the driving trajectory envelope
					AbstractTrajectoryEnvelopeTracker tracker = getNewTracker(te, cb);  //TODO: Add the tracker to vehicle here
//					tracker.checkPause();
					tracker.setPauseCounter(recievedSTop);

					trackers.put(te.getRobotID(), tracker);
					externalCPCounters.put(tracker, -1);
				}

				//Now we can signal the parking that it can end (i.e., its deadline will no longer be prolonged)
				//Note: the parking tracker will anyway wait to exit until earliest end time has been reached
				startParkingTracker.finishParking();

				this.isDriving.put(te.getRobotID(), true);
			}
			envelopesToTrack.clear();
		}
	}

	/**
	 * Add one or more missions for one or more robots. If more than one mission is specified for
	 * a robot <code>r</code>, then all the robot's missions are concatenated.
	 * NOTE: For each robot <code>r</code>, all missions should be either defined with a path file or with
	 * an array of {@link PoseSteering}s (pathfile- and path-specified missions cannot be mixed for one robot).
	 *
	 * @param missions One or more {@link Mission}s, for one or more robots.
	 * @return <code>true</code> iff for all {@link Mission}s the relevant robot is not already
	 * engaged in another mission.
	 */
	public boolean addMissions(Mission ... missions) {

		if (solver == null) {
			metaCSPLogger.severe("Solvers not initialized, please call method setupSolver()");
			throw new Error("Solvers not initialized, please call method setupSolver()");
		}

		HashMap<Integer,ArrayList<Mission>> robotsToMissions = new HashMap<Integer,ArrayList<Mission>>();
		for (Mission m : missions) {
			if (robotsToMissions.get(m.getRobotID()) == null) robotsToMissions.put(m.getRobotID(), new ArrayList<Mission>());
			robotsToMissions.get(m.getRobotID()).add(m);
		}

		for (Entry<Integer,ArrayList<Mission>> e : robotsToMissions.entrySet()) {
			int robotID = e.getKey();
			if (!isFree(robotID)) return false;
		}

		for (Entry<Integer,ArrayList<Mission>> e : robotsToMissions.entrySet()) {
			int robotID = e.getKey();
			synchronized (solver) {
				//Get start parking tracker and envelope
				TrajectoryEnvelopeTrackerDummy startParkingTracker = (TrajectoryEnvelopeTrackerDummy)trackers.get(robotID);
				TrajectoryEnvelope startParking = startParkingTracker.getTrajectoryEnvelope();
				ArrayList<Constraint> consToAdd = new ArrayList<Constraint>();
				//				String finalDestLocation = "";
				ArrayList<PoseSteering> overallPath = new ArrayList<PoseSteering>();
				for (Mission m : e.getValue()) {
                    Collections.addAll(overallPath, m.getPath());
				}

				//Create a big overall driving envelope
				TrajectoryEnvelope te = null;
				te = solver.createEnvelopeNoParking(robotID, overallPath.toArray(new PoseSteering[overallPath.size()]), "Driving", getFootprint(robotID));

				//Add mission stopping points
				synchronized(stoppingPoints) {
					for (int i = 0; i < e.getValue().size(); i++) {
						Mission m = e.getValue().get(i);
						for (Entry<Pose,Integer> entry : m.getStoppingPoints().entrySet()) {
							Pose stoppingPose = entry.getKey();
							int stoppingPoint = te.getSequenceNumber(new Coordinate(stoppingPose.getX(), stoppingPose.getY()));
							if (stoppingPoint == te.getPathLength()-1) stoppingPoint -= 2;
							int duration = entry.getValue();
							if (!stoppingPoints.containsKey(robotID)) {
								stoppingPoints.put(robotID, new ArrayList<Integer>());
								stoppingTimes.put(robotID, new ArrayList<Integer>());
							}
							if (!stoppingPoints.get(robotID).contains(stoppingPoint)) {
								stoppingPoints.get(robotID).add(stoppingPoint);
								stoppingTimes.get(robotID).add(duration);
							}
						}
					}

					//If many missions, add destinations as stopping points
					for (int i = 0; i < e.getValue().size()-1; i++) {
						Mission m = e.getValue().get(i);
						Pose destPose = m.getToPose();
						int stoppingPoint = te.getSequenceNumber(new Coordinate(destPose.getX(), destPose.getY()));
						if (!stoppingPoints.containsKey(robotID)) {
							stoppingPoints.put(robotID, new ArrayList<Integer>());
							stoppingTimes.put(robotID, new ArrayList<Integer>());
						}
						if (!stoppingPoints.get(robotID).contains(stoppingPoint)) {
							stoppingPoints.get(robotID).add(stoppingPoint);
							stoppingTimes.get(robotID).add(DEFAULT_STOPPING_TIME);
						}
					}
					if (stoppingPoints.get(robotID) != null) metaCSPLogger.info("Stopping points along trajectory for Robot" + robotID + ": " + stoppingPoints.get(robotID));
				}

				//Put in more realistic DTs computed with the RK4 integrator
				//				System.out.println(">> Computing DTs...");
				//				double dts[] = TrajectoryEnvelopeTrackerRK4.computeDTs(te.getTrajectory(), this.getMaxVelocity(), this.getMaxAcceleration());
				//				te.getTrajectory().setDTs(dts);
				//				te.updateDuration();
				//				System.out.println("<< done computing DTs");

				//Start parking meets driving, so that driving does not start before parking is finished
				AllenIntervalConstraint meets = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meets.setFrom(startParking);
				meets.setTo(te);
				consToAdd.add(meets);

				if (!solver.addConstraints(consToAdd.toArray(new Constraint[consToAdd.size()]))) {
					metaCSPLogger.severe("ERROR: Could not add constraints " + consToAdd);
					throw new Error("Could not add constraints " + consToAdd);
				}

				missionsPool.add(new Pair<TrajectoryEnvelope,Long>(te, Calendar.getInstance().getTimeInMillis()));
			}
		}

		return true;
	}

	/**
	 * Sets up a GUI which shows the current status of robots.
	 */
	public void setVisualization(FleetVisualization viz) {
		this.viz = viz;
	}

	protected void setPriorityOfEDT(final int prio) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Thread.currentThread().setPriority(prio);
				}});
		}
		catch (InvocationTargetException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	/**
	 * Get the current {@link TrajectoryEnvelope} of a robot.
	 * @param robotID The robotID.
	 * @return The current {@link TrajectoryEnvelope} of a robot.
	 */
	public TrajectoryEnvelope getCurrentTrajectoryEnvelope(int robotID) {
		return trackers.get(robotID).getTrajectoryEnvelope();
	}

	protected String[] getStatistics() {
		synchronized (trackers) {
			String CONNECTOR_BRANCH = (char)0x251C + "" + (char)0x2500 + " ";
			String CONNECTOR_LEAF = (char)0x2514 + "" + (char)0x2500 + " ";
			ArrayList<String> ret = new ArrayList<String>();
			int numVar = solver.getConstraintNetwork().getVariables().length;
			int numCon = solver.getConstraintNetwork().getConstraints().length;
			ret.add("Status @ "  + getCurrentTimeInMillis() + " ms");

			ret.add(CONNECTOR_BRANCH + "Eff period ..... " + EFFECTIVE_CONTROL_PERIOD + " ms");
			ret.add(CONNECTOR_BRANCH + "Network ........ " + numVar + " variables, " + numCon + " constriants");
			HashSet<Integer> allRobots = new HashSet<Integer>();
			for (Integer robotID : trackers.keySet()) {
				allRobots.add(robotID);
			}
			String st = CONNECTOR_BRANCH + "Robots ......... ";
			for (Integer robotID : allRobots) {
				AbstractTrajectoryEnvelopeTracker tracker = trackers.get(robotID);
				RobotReport rr = tracker.getRobotReport();
				int currentPP = rr.getPathIndex();
				st += tracker.te.getComponent();
				if (tracker instanceof TrajectoryEnvelopeTrackerDummy) st += " (P)";
				else st += " (D)";
				st += ": " + currentPP + "   ";
			}
			ret.add(st);
			synchronized (currentDependencies) {
				ret.add(CONNECTOR_LEAF + "Dependencies ... " + currentDependencies);
				return ret.toArray(new String[ret.size()]);
			}
		}
	}

	protected void overlayStatistics() {
		String[] stats = getStatistics();
		System.out.printf(((char) 0x1b) + "[H");
		System.out.printf(((char) 0x1b) + "[1m");
        for (String stat : stats) {
            System.out.printf(((char) 0x1b) + "[1B" + ((char) 0x1b) + "[2K\r" + stat);
        }
		System.out.printf(((char) 0x1b) + "[0m");
		System.out.printf(((char) 0x1b) + "[200B\r");
	}

	//Print some statistics
	protected void printStatistics() {
		for (String s : getStatistics()) {
			metaCSPLogger.info(s);
		}
	}

    protected void setupInferenceCallback() {

		this.stopInference = false;
		this.inference = new Thread("Abstract Coordinator inference") {

			@Override
			public void run() {
				long threadLastUpdate = Calendar.getInstance().getTimeInMillis();
				int MAX_ADDED_MISSIONS = 1;

				while (!stopInference) {
					int numberNewAddedMissions = 0;
					synchronized (solver) {
						if (!missionsPool.isEmpty()) {
							while (!missionsPool.isEmpty() && numberNewAddedMissions < MAX_ADDED_MISSIONS) {
								Pair<TrajectoryEnvelope, Long> te = missionsPool.pollFirst();
								envelopesToTrack.add(te.getFirst());
								numberNewAddedMissions++;
							}
							computeCriticalSections();
							startTrackingAddedMissions();
						}
						updateDependencies();

						if (!quiet) printStatistics();
						if (overlay) overlayStatistics();
					}

					//Sleep a little...
					if (CONTROL_PERIOD > 0) {
						try { Thread.sleep(Math.max(0, CONTROL_PERIOD-Calendar.getInstance().getTimeInMillis()+threadLastUpdate)); }
						catch (InterruptedException e) { e.printStackTrace(); }
					}

					long threadCurrentUpdate = Calendar.getInstance().getTimeInMillis();
					EFFECTIVE_CONTROL_PERIOD = (int)(threadCurrentUpdate-threadLastUpdate);
					threadLastUpdate = threadCurrentUpdate;

					if (inferenceCallback != null) inferenceCallback.performOperation();

				}
			}
		};
		inference.setPriority(Thread.MAX_PRIORITY);
		inference.start();
	}

	/**
	 * Try to restore the order of traversing each active critical section.
	 * If both the current reports assert that the robots should still enter the critical section,
	 * then the order cannot be reconstructed by looking to reciprocal positions in case of network delays.
	 * @param cs The active critical section for which we are trying to restore the order.
	 * @param rr1 Last report of the first robot.
	 * @param rr2 Last report of the second robot.
	 * @return which robot is ahead (1 if Robot 1 is ahead, -1 if Robot 2 is ahead, 0 if nothing can be stated.)
	 * Returning -2 if the critical section is no more active.
	 */
	protected int isAhead(CriticalSection cs, RobotReport rr1, RobotReport rr2) {
		//FIXME
		//1) add code for the checking the network delay.
		//2) check the correctness of the function with asymmetric intersections.
		if (!allCriticalSections.contains(cs) || rr1.getPathIndex() > cs.getTrajectoryEnvelopeEnd1() || rr2.getPathIndex() > cs.getTrajectoryEnvelopeEnd2()) {
			metaCSPLogger.info("isAhead: the critical sections is no more active.");
			return -2;
		}
		if (rr1.getPathIndex() >= cs.getTrajectoryEnvelopeStart1() && rr2.getPathIndex() >= cs.getTrajectoryEnvelopeStart2()) {
			//Robot 1 is ahead --> return true
			PoseSteering[] pathRobot1 = cs.getTrajectoryEnvelope1().getTrajectory().getPoseSteering();
			PoseSteering[] pathRobot2 = cs.getTrajectoryEnvelope2().getTrajectory().getPoseSteering();
			double dist1 = 0.0;
			double dist2 = 0.0;
			for (int i = cs.getTrajectoryEnvelopeStart1(); i < rr1.getPathIndex()-1; i++) {
				dist1 += pathRobot1[i].getPose().getPosition().distance(pathRobot1[i+1].getPose().getPosition());
			}
			for (int i = cs.getTrajectoryEnvelopeStart2(); i < rr2.getPathIndex()-1; i++) {
				dist2 += pathRobot2[i].getPose().getPosition().distance(pathRobot2[i+1].getPose().getPosition());
			}
			//metaCSPLogger.finest("Dist R" + rr1.getRobotID() + " = " + dist1 + "; Dist R" + rr2.getRobotID() + " = " + dist2);
			return dist1 > dist2 ? 1 : -1;
		}
		return -1;
	}

	/**
	 * Factory method that returns a trajectory envelope tracker, which is the class implementing the
	 * interface with real or simulated robots.
	 * @param te The reference {@link TrajectoryEnvelope} that should be driven.
	 * @param cb A callback that is called every tracking period.
	 * @return An instance of a trajectory envelope tracker.
	 */
	public abstract AbstractTrajectoryEnvelopeTracker getNewTracker(TrajectoryEnvelope te, TrackingCallback cb);

	/**
	 * Determine if a robot is free to accept a new mission (that is, the robot is in state WAITING_FOR_TASK).
	 * @param robotID The ID of the robot.
	 * @return <code>true</code> iff the robot is free to accept a new mission (that is, the robot is in state WAITING_FOR_TASK).
	 */
	public boolean isFree(int robotID) {
		if (solver == null) {
			metaCSPLogger.severe("Solver not initialized, please call method setupSolver() first!");
			throw new Error("Solver not initialized, please call method setupSolver() first!");
		}
		synchronized (solver) {
			if (muted.contains(robotID)) return false;
			for (Pair<TrajectoryEnvelope,Long> te : missionsPool) if (te.getFirst().getRobotID() == robotID) return false;
			synchronized (trackers) {
				AbstractTrajectoryEnvelopeTracker tracker = trackers.get(robotID);
				if (!(tracker instanceof TrajectoryEnvelopeTrackerDummy)) return false;
				TrajectoryEnvelopeTrackerDummy trackerDummy = (TrajectoryEnvelopeTrackerDummy)tracker;
				return (!trackerDummy.isParkingFinished());
			}
		}
	}

	public static void printLicense() {
		System.out.println("\n"+TrajectoryEnvelopeCoordinator.TITLE);
		String cpr = TrajectoryEnvelopeCoordinator.COPYRIGHT;
		for (String cont : TrajectoryEnvelopeCoordinator.CONTRIBUTORS) cpr += ", " + cont;
		List<String> cprJust = StringUtils.fitWidth(cpr, 77, 0);
		for (String st : cprJust) System.out.println(st);
		System.out.println();
		if (TrajectoryEnvelopeCoordinator.LICENSE != null) {
			List<String> lic = StringUtils.fitWidth(TrajectoryEnvelopeCoordinator.PRIVATE_LICENSE, 72, 5);
			for (String st : lic) System.out.println(st);
		}
		else {
			List<String> lic = StringUtils.fitWidth(TrajectoryEnvelopeCoordinator.PUBLIC_LICENSE, 72, 5);
			for (String st : lic) System.out.println(st);
		}
		System.out.println();
	}

}

