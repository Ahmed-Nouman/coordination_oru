package se.oru.coordination.coordination_oru.coordinator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.tracker.*;
import se.oru.coordination.coordination_oru.utils.CollisionEvent;
import se.oru.coordination.coordination_oru.utils.CriticalSection;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class TrajectoryEnvelopeCoordinatorSimulation extends TrajectoryEnvelopeCoordinator {

	public static TrajectoryEnvelopeCoordinatorSimulation trajectoryEnvelopeCoordinatorSimulation = null;
	protected static final long START_TIME = Calendar.getInstance().getTimeInMillis();
	protected boolean useInternalCPs = true;
	protected boolean checkCollisions = false; // TODO Check if it works
	protected ArrayList<CollisionEvent> collisionsList = new ArrayList<>();
	protected Thread collisionThread = null;
	protected AtomicInteger totalMessagesLost = new AtomicInteger(0);
	protected AtomicInteger totalPacketsLost = new AtomicInteger(0);
	protected double DEFAULT_MAX_VELOCITY;
	protected double DEFAULT_MAX_ACCELERATION;
	protected int DEFAULT_ROBOT_TRACKING_PERIOD;
	public static double MAX_DEFAULT_FOOTPRINT_DIMENSION = 4.4;

	/**
	 * Create a new {@link TrajectoryEnvelopeCoordinatorSimulation} with given parameters.
	 * @param CONTROL_PERIOD The control period of the coordinator (e.g., 1000 milliseconds)
	 * @param TEMPORAL_RESOLUTION The temporal resolution at which the control period is specified (e.g., 1000)
	 * @param MAX_VELOCITY The maximum speed of a robot (used to appropriately instantiate the {@link TrajectoryEnvelopeTrackerRK4} instances).
	 * @param MAX_ACCELERATION The maximum acceleration/deceleration of a robot (used to appropriately instantiate the {@link TrajectoryEnvelopeTrackerRK4} instances).
	 * @param DEFAULT_ROBOT_TRACKING_PERIOD The default tracking period in milliseconds (used to appropriately instantiate the {@link TrajectoryEnvelopeTrackerRK4} instances).
	 */
	public TrajectoryEnvelopeCoordinatorSimulation(int CONTROL_PERIOD, double TEMPORAL_RESOLUTION, double MAX_VELOCITY, double MAX_ACCELERATION, int DEFAULT_ROBOT_TRACKING_PERIOD) {
		super(CONTROL_PERIOD, TEMPORAL_RESOLUTION);
		this.DEFAULT_MAX_VELOCITY = MAX_VELOCITY;
		this.DEFAULT_MAX_ACCELERATION = MAX_ACCELERATION;
		this.DEFAULT_ROBOT_TRACKING_PERIOD = DEFAULT_ROBOT_TRACKING_PERIOD;
		trajectoryEnvelopeCoordinatorSimulation = this;
	}

	public TrajectoryEnvelopeCoordinatorSimulation(int CONTROL_PERIOD, double TEMPORAL_RESOLUTION, double MAX_VELOCITY, double MAX_ACCELERATION) {
		this(CONTROL_PERIOD, TEMPORAL_RESOLUTION, MAX_VELOCITY, MAX_ACCELERATION, 30);
	}

	public TrajectoryEnvelopeCoordinatorSimulation(double MAX_VELOCITY, double MAX_ACCELERATION) {
		this(1000, 1000, MAX_VELOCITY, MAX_ACCELERATION, 30);
	}
	/**
	 * The default footprint used for robots if none is specified.
	 * NOTE: coordinates in footprints must be given in CCW or CW order.
	 */
	public static Coordinate[] DEFAULT_FOOTPRINT = new Coordinate[] {
			new Coordinate(-1.7, 0.7),	//back left
			new Coordinate(-1.7, -0.7),	//back right
			new Coordinate(2.7, -0.7),	//front right
			new Coordinate(2.7, 0.7)	//front left
	};

	/**
	 * Enable the collision checking thread.
	 * @param enable <code>true</code>  if the thread for checking collisions should be enabled.
	 */
	public void setCheckCollisions(boolean enable) {
		this.checkCollisions = enable;
	}

	/**
	 * Just for statistic purposes (simulation).
	 */
	public void incrementLostMessagesCounter() {
		this.totalMessagesLost.incrementAndGet();
	}

	/**
	 * Just for statistic purposes (simulation).
	 */
	public void incrementLostPacketsCounter() {
		this.totalPacketsLost.incrementAndGet();
	}

	private ArrayList<Integer> computeStoppingPoints(PoseSteering[] poses) {
		ArrayList<Integer> ret = new ArrayList<>();
		double prevTheta = poses[0].getTheta();
		if (poses.length > 1) prevTheta = Math.atan2(poses[1].getY() - poses[0].getY(), poses[1].getX() - poses[0].getX());
		for (int i = 0; i < poses.length-1; i++) {
			double theta = Math.atan2(poses[i+1].getY() - poses[i].getY(), poses[i+1].getX() - poses[i].getX());
			double deltaTheta = (theta-prevTheta);
			prevTheta = theta;
			if (Math.abs(deltaTheta) > Math.PI/2 && Math.abs(deltaTheta) < 1.9*Math.PI) {
				ret.add(i);
			}
		}
		return ret;
	}

	@Override
	public boolean addMissions(Mission... missions) {
        var userStoppingPoints = new HashMap<Mission, HashMap<Pose,Integer>>();
		if (this.useInternalCPs) {
			for (Mission m : missions) {
				PoseSteering[] path = m.getPath();
				ArrayList<Integer> sps = computeStoppingPoints(path);
				userStoppingPoints.put(m, m.getStoppingPoints());
				for (Integer i : sps) m.setStoppingPoint(path[i-1].getPose(), 100);
			}
		}
		if (!super.addMissions(missions)) {
			if (this.useInternalCPs) {
				for (Mission m : missions) {
					m.clearStoppingPoints();
					for (Pose p : userStoppingPoints.get(m).keySet()) {
						m.setStoppingPoint(p, userStoppingPoints.get(m).get(p));
					}
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Enable (default) or disable the use of internal critical points in the {@link TrajectoryEnvelopeTrackerRK4} trackers.
	 * @param value <code>true</code> if these critical points should be used to slow down, <code>false</code> otherwise.
	 */
	public void setUseInternalCriticalPoints(boolean value) {
		this.useInternalCPs = value;
	}

	/**
	 * Get the maximum velocity of a given robot (m/s).
	 * @param robotID The ID of the robot.
	 * @return The maximum velocity of the robot (or the default value if not specified).
	 */
	@Override
	public Double getRobotMaxVelocity(int robotID) {
		robotMaxVelocity = VehiclesHashMap.getVehicleMaxVelocities();
		if (this.robotMaxVelocity.containsKey(robotID))
			return this.robotMaxVelocity.get(robotID);
		return DEFAULT_MAX_VELOCITY;
	}

	/**
	 * Get the maximum acceleration of a given robot (m/s^2).
	 * @param robotID The ID of the robot.
	 * @return The maximum acceleration of the robot (or the default value if not specified).
	 */
	@Override
	public Double getRobotMaxAcceleration(int robotID) {
		robotMaxAcceleration = VehiclesHashMap.getVehicleMaxAccelerations();
		if (this.robotMaxAcceleration.containsKey(robotID))
			return this.robotMaxAcceleration.get(robotID);
		return DEFAULT_MAX_ACCELERATION;
	}

	/**
	 * Get the tracking period of a given robot (milliseconds).
	 * @param robotID The ID of the robot.
	 * @return The tracking period of the robot (or the default value if not specified).
	 */
	@Override
	public Integer getRobotTrackingPeriodInMillis(int robotID) {
		robotTrackingPeriodInMillis = VehiclesHashMap.getVehicleTrackingPeriod();
		if (this.robotTrackingPeriodInMillis.containsKey(robotID))
			return this.robotTrackingPeriodInMillis.get(robotID);
		return DEFAULT_ROBOT_TRACKING_PERIOD;
	}

	@Override
	protected Double getMaxFootprintDimension(int robotID) {
		if (this.footprints.containsKey(robotID)) return maxFootprintDimensions.get(robotID);
		return MAX_DEFAULT_FOOTPRINT_DIMENSION;
	}

	/**
	 * Get the {@link Coordinate}s defining the default footprint of robots.
	 * @return The {@link Coordinate}s defining the default footprint of robots.
	 */
	public Coordinate[] getDefaultFootprint() {
		return DEFAULT_FOOTPRINT;
	}

	/**
	 * Get the {@link Coordinate}s defining the footprint of a given robot.
	 * @param robotID the ID of the robot
	 * @return The {@link Coordinate}s defining the footprint of a given robot.
	 */
	@Override
	public Coordinate[] getFootprint(int robotID) {
		if (this.footprints.containsKey(robotID)) return this.footprints.get(robotID);
		return DEFAULT_FOOTPRINT;
	}

	/**
	 * Get a {@link Geometry} representing the default footprint of robots.
	 * @return A {@link Geometry} representing the default footprint of robots.
	 */
	public Geometry getDefaultFootprintPolygon() {
        return TrajectoryEnvelope.createFootprintPolygon(DEFAULT_FOOTPRINT);
	}

	/**
	 * Set the default footprint of robots, which is used for computing spatial envelopes.
	 * Provide the bounding polygon of the machine assuming its reference point is in (0,0), and its
	 * orientation is aligned with the x-axis. The coordinates must be in CW or CCW order.
	 * @param coordinates The coordinates delimiting bounding polygon of the footprint.
	 */
	public void setDefaultFootprint(Coordinate ... coordinates) {
		DEFAULT_FOOTPRINT = coordinates;
		MAX_DEFAULT_FOOTPRINT_DIMENSION = computeMaxFootprintDimension(coordinates);
	}

	@Override
	public AbstractTrajectoryEnvelopeTracker getNewTracker(TrajectoryEnvelope te, TrackingCallback cb) {
		if (this.getRobotTrackingPeriodInMillis(te.getRobotID()) == null || this.getRobotMaxVelocity(te.getRobotID()) == null || this.getRobotMaxAcceleration(te.getRobotID()) == null) throw new Error("Robot" +  te.getRobotID() + ": missing kinodynamic parameters.");
		var ret = new AdaptiveTrackerRK4(te, this.getRobotTrackingPeriodInMillis(te.getRobotID()), TEMPORAL_RESOLUTION, this.getRobotMaxVelocity(te.getRobotID()), this.getRobotMaxAcceleration(te.getRobotID()), this, cb) {

			//Method for measuring time in the trajectory envelope tracker
			@Override
			public long getCurrentTimeInMillis() {
				return tec.getCurrentTimeInMillis();
			}
		};
		//ret.setUseInternalCriticalPoints(this.useInternalCPs);
		ret.setUseInternalCriticalPoints(false);
		return ret;
	}

	//Method for measuring time in the trajectory envelope coordinator
	@Override
	public long getCurrentTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis()-START_TIME;
	}

	@Override
	protected String[] getStatistics() {

		String CONNECTOR_BRANCH = (char)0x251C + "" + (char)0x2500 + " ";
		String CONNECTOR_LEAF = (char)0x2514 + "" + (char)0x2500 + " ";
		ArrayList<String> ret = new ArrayList<String>();
		int numVar = solver.getConstraintNetwork().getVariables().length;
		int numCon = solver.getConstraintNetwork().getConstraints().length;

		synchronized (trackers) {
			ret.add("Status @ "  + getCurrentTimeInMillis() + " ms");
			ret.add(CONNECTOR_BRANCH + 		"Eff period .......... " + EFFECTIVE_CONTROL_PERIOD + " ms");
			ret.add(CONNECTOR_BRANCH + 		"Constraint network .. " + numVar + " variables, " + numCon + " constraints");
			HashSet<Integer> allRobots = new HashSet<Integer>();
			for (Integer robotID : trackers.keySet()) {
				allRobots.add(robotID);
			}
			String st = CONNECTOR_BRANCH +	"Robot states ........ ";
			for (Integer robotID : allRobots) {
				AbstractTrajectoryEnvelopeTracker tracker = trackers.get(robotID);
				RobotReport rr = tracker.getRobotReport();
				int currentPP = rr.getPathIndex();
				st += tracker.getTrajectoryEnvelope().getComponent();
				if (tracker instanceof TrajectoryEnvelopeTrackerDummy) st += " (P)";
				else st += " (D)";
				st += ": " + currentPP + "   ";
			}
			ret.add(st);
			st = CONNECTOR_BRANCH + 		"Robots' view ........ ";
			for (Integer robotID : allRobots) {
				AbstractTrajectoryEnvelopeTracker tracker = trackers.get(robotID);
				RobotReport rr = getRobotReport(robotID);
				if (rr != null ) {
					int currentPP = rr.getPathIndex();
					st += tracker.getTrajectoryEnvelope().getComponent();
					if (tracker instanceof TrajectoryEnvelopeTrackerDummy) st += " (P)";
					else st += " (D)";
					st += ": " + currentPP + "   ";
				}
			}
			ret.add(st);
		}
		synchronized (currentDependencies) {
			ret.add(CONNECTOR_BRANCH + 		"Dependencies ........ " + currentDependencies);
		}
		if (checkCollisions) {
			int numberOfCollisions = 0;
			synchronized (collisionsList) {
				numberOfCollisions = collisionsList.size();
				ret.add(CONNECTOR_BRANCH + 	"Collisions .......... " + numberOfCollisions + ".");
				if (numberOfCollisions>0) {
					for (int cindex = 0; cindex < collisionsList.size()-1; cindex++) {
						CollisionEvent ce = collisionsList.get(cindex);
						ret.add(" " + CONNECTOR_BRANCH + " " + ce.toString());
					}
					CollisionEvent ce = collisionsList.get(collisionsList.size()-1);
					ret.add(" " + CONNECTOR_LEAF + " " + ce.toString());
				}
			}
		}
		ret.add(CONNECTOR_BRANCH + 			"Obsolete critical sections .. " + criticalSectionCounter.get() + ".");
		ret.add(CONNECTOR_BRANCH + 			"Messages sent ....... " + totalMsgsSent.get() + ", lost: " + totalMessagesLost.get() + ", retransmitted: " + totalMsgsReTx.get() + ". Packets lost: " + totalPacketsLost.get() + ", number of replicas: " + numberOfReplicas + ".");
		ret.add(CONNECTOR_BRANCH + 			"Un-alive states ...... " + nonliveStatesDetected.get() + ", avoided: " + nonliveStatesAvoided.get() + ", revised according to heuristic: " + currentOrdersHeurusticallyDecided.get() + ".");
		ret.add(CONNECTOR_LEAF + 			"Re-planned paths .... " + replanningTrialsCounter.get() + ", successful: " + successfulReplanningTrialsCounter.get() + ".");
		return ret.toArray(new String[ret.size()]);
	}

	@Override
	public void onCriticalSectionUpdate() {

		if (checkCollisions && (collisionThread == null) && (!this.allCriticalSections.isEmpty())) {
			// Start the collision checking thread.
			// The thread will be alive until there will be almost one critical section.
			collisionThread = new Thread("Collision checking thread.") {

				@Override
				public void run() {
					metaCSPLogger.info("Starting the collision checking thread.");
					ArrayList<CriticalSection> previousCollidingCS = new ArrayList<>();
					ArrayList<CriticalSection> newCollidingCS = new ArrayList<>();

					while(true) {
						newCollidingCS.clear();

						//collisions can happen only in critical sections
						synchronized (allCriticalSections) {
							if (allCriticalSections.isEmpty())
								break; //break the thread if there are no critical sections to control

							for (CriticalSection cs : allCriticalSections) {
								//check if both the robots are inside the critical section

								//FIXME sample the real pose of the robots (ok in simulation, but not otherwise)
								RobotReport robotReport1, robotReport2;
								AbstractTrajectoryEnvelopeTracker tracker1, tracker2;
								try {
									synchronized (trackers)	{
										tracker1 = trackers.get(cs.getTrajectoryEnvelope1().getRobotID());
										robotReport1 = tracker1.getRobotReport();
										tracker2 = trackers.get(cs.getTrajectoryEnvelope2().getRobotID());
										robotReport2 = tracker2.getRobotReport();
									}
								}
								catch (NullPointerException e) {
									continue; //skip this cycle
								}

								if ( robotReport1 != null && robotReport2 != null &&
								(robotReport1.getPathIndex() <= cs.getTrajectoryEnvelopeEnd1()) && (robotReport1.getPathIndex() >= cs.getTrajectoryEnvelopeStart1()) && //robot1 is inside
								(robotReport2.getPathIndex() <= cs.getTrajectoryEnvelopeEnd2()) && (robotReport2.getPathIndex() >= cs.getTrajectoryEnvelopeStart2())  	//robot2 is inside
								) {
									//place robot  in pose and get geometry
									PoseSteering[] path1 = cs.getTrajectoryEnvelope1().getTrajectory().getPoseSteering();
									Geometry placement1 = cs.getTrajectoryEnvelope1().makeFootprint(path1[robotReport1.getPathIndex()]);

									PoseSteering[] path2 = cs.getTrajectoryEnvelope2().getTrajectory().getPoseSteering();
									Geometry placement2 = cs.getTrajectoryEnvelope2().makeFootprint(path2[robotReport2.getPathIndex()]);

									//check intersection
									if (placement1.intersects(placement2)) {
										if (!previousCollidingCS.contains(cs)) {
											metaCSPLogger.info(" * NEW COLLISION *");
											CollisionEvent ce = new CollisionEvent(Calendar.getInstance().getTimeInMillis(),robotReport1,robotReport2);
											synchronized (collisionsList) {
												collisionsList.add(ce);
											}
											newCollidingCS.add(cs);
										}
									}
									else previousCollidingCS.remove(cs); //remove the ones that are not colliding anymore
								}
							}
						}
						previousCollidingCS.addAll(newCollidingCS);

						try { Thread.sleep((long)(1000.0/30.0)); }
						catch (InterruptedException e) { e.printStackTrace(); }
					}
					metaCSPLogger.info("Ending the collision checking thread.");
				}

			};
			collisionThread.start();
		}
	}

	public int getCountCollisionsList() {
		return collisionsList.size();
	}

	public void setFootprints() {
		if (VehiclesHashMap.getInstance() == null || VehiclesHashMap.getList().isEmpty()) throw new Error("No vehicles available to place at locations.");
		for (AbstractVehicle vehicle : VehiclesHashMap.getList().values()) {
			if (vehicle instanceof AutonomousVehicle) {
				var autonomousVehicle = (AutonomousVehicle) vehicle;
				if (autonomousVehicle.getFootprint() == null)
					throw new Error("No footprint available for vehicle " + autonomousVehicle.getID());
				else {
					this.setFootprint(autonomousVehicle.getID(), autonomousVehicle.getFootprint());
				}
			}
		}

	}

	public void placeRobotsAtStartPoses() {
		if (VehiclesHashMap.getInstance() == null || VehiclesHashMap.getList().isEmpty()) throw new Error("No vehicles available to place at locations.");
		for (AbstractVehicle vehicle : VehiclesHashMap.getList().values()) {
			if (vehicle instanceof AutonomousVehicle) {
				var autonomousVehicle = (AutonomousVehicle) vehicle;
				if (autonomousVehicle.getPaths().isEmpty()) {
					System.out.println(("No path available for vehicle " + autonomousVehicle.getID()));
					this.placeRobot(autonomousVehicle.getID(), autonomousVehicle.getInitialPose());
				}
				else
					this.placeRobot(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0)[0].getPose());
			}
		}
	}

	public void setForwardModelsForRobots() {
		if (VehiclesHashMap.getInstance() == null || VehiclesHashMap.getList().isEmpty()) throw new Error("No vehicles available to place at locations.");
		for (AbstractVehicle vehicle : VehiclesHashMap.getList().values()) {
			if (vehicle instanceof AutonomousVehicle) {
				var autonomousVehicle = (AutonomousVehicle) vehicle;
				if (autonomousVehicle.getForwardModel() == null)
					throw new Error("No forward model available for vehicle " + autonomousVehicle.getID());
				else {
					this.setForwardModel(autonomousVehicle.getID(), autonomousVehicle.getForwardModel());
				}
			}
		}
	}
}
