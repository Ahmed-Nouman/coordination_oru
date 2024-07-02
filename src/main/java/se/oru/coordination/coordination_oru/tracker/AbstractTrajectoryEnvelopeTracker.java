package se.oru.coordination.coordination_oru.tracker;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import se.oru.coordination.coordination_oru.coordinator.AbstractTrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.utils.Dependency;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.RobotReport;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractTrajectoryEnvelopeTracker {

	protected AbstractTrajectoryEnvelopeCoordinator tec;
	public TrajectoryEnvelope te;
	protected Trajectory trajectory;
	protected double temporalResolution;
	protected Integer externalCPCounter;
	protected Integer reportCounter = -1;
	protected int criticalPoint;
	protected HashSet<TrajectoryEnvelope> startedGroundEnvelopes = new HashSet<>();
	protected HashSet<TrajectoryEnvelope> finishedGroundEnvelopes = new HashSet<>();
	protected HashMap<TrajectoryEnvelope,AllenIntervalConstraint> deadlines = new HashMap<>();
	protected int trackingPeriodInMillis;
	protected TrackingCallback cb;
	protected Map mapMetaConstraint = null;
	protected boolean calledOnTrackingStart = false;
	protected boolean calledStartTracking = false;
	protected boolean canStartTracking = false;
	protected long startingTimeInMillis;
	protected Logger metaCSPLogger = MetaCSPLogging.getLogger(AbstractTrajectoryEnvelopeTracker.class);
//	protected volatile int pauseCounter = 0;
	protected volatile ArrayList<AutonomousVehicle> pauseByVehicles = new ArrayList<>();
	protected volatile int priorityChangeCounter = 0;
	protected volatile int slowdownCounter = 0;
	protected volatile boolean isPaused = false;
	protected Heuristics currentHeuristics = null;

	public AbstractTrajectoryEnvelopeTracker(TrajectoryEnvelope te, double temporalResolution, AbstractTrajectoryEnvelopeCoordinator tec, int trackingPeriodInMillis, TrackingCallback cb) {
		this.te = te;
		this.trajectory = te.getTrajectory();
		this.externalCPCounter = -1;
		this.criticalPoint = -1;
		this.temporalResolution = temporalResolution;
		this.startingTimeInMillis = tec.getCurrentTimeInMillis();
		this.tec = tec;
		this.trackingPeriodInMillis = trackingPeriodInMillis;
		this.cb = cb;
		startMonitoringThread();
	}

	public int getTrackingPeriod() {
		return this.trackingPeriodInMillis;
	}

	public long getStartingTimeInMillis() {
		return this.startingTimeInMillis;
	}

	public void resetStartingTimeInMillis() {
		this.startingTimeInMillis= tec.getCurrentTimeInMillis();
	}

	protected abstract void onTrajectoryEnvelopeUpdate();

	public void updateTrajectoryEnvelope(TrajectoryEnvelope te) {
		synchronized(tec.getSolver()) {
			synchronized(this.te) {
				metaCSPLogger.info("Updating trajectory Robot" +this.te.getRobotID()+". TEID: " + this.te.getID() + "--> TEID: " + te.getID()+ ".");
				this.te = te;
				this.cb.updateTrajectoryEnvelope(te);
				this.trajectory = te.getTrajectory();
				this.onTrajectoryEnvelopeUpdate();
			}
		}
	}

	@Deprecated
	public void setMapMetaConstraint(Map mapMetaConstraint) {
		this.mapMetaConstraint = mapMetaConstraint;
	}

	public void setCanStartTracking() {
		this.canStartTracking = true;
	}

	public boolean canStartTracking() {
		return this.canStartTracking;
	}

	public boolean isStarted(TrajectoryEnvelope env) {
		return this.startedGroundEnvelopes.contains(env);
	}

	public boolean tracksEnvelope(TrajectoryEnvelope env) {
		for (TrajectoryEnvelope subEnv : this.getAllSubEnvelopes()) {
			if (subEnv.equals(env)) return true;
		}
		return false;
	}

	public boolean isFinished(TrajectoryEnvelope env) {
		if (!this.tracksEnvelope(env)) return true;
		return this.finishedGroundEnvelopes.contains(env);
	}

	public abstract void setCriticalPoint(int criticalPoint);

	public void setCriticalPoint(int criticalPointToSet, int externalCPCounter) {

		if (
				(externalCPCounter < this.externalCPCounter && externalCPCounter-this.externalCPCounter > Integer.MAX_VALUE/2.0) ||
						(this.externalCPCounter > externalCPCounter && this.externalCPCounter-externalCPCounter < Integer.MAX_VALUE/2.0)) {
			metaCSPLogger.info("Ignored critical point " + criticalPointToSet + " related to counter " + externalCPCounter + " because counter is already at " + this.externalCPCounter + ".");
			return;
		}
		setCriticalPoint(criticalPointToSet);
		this.externalCPCounter = externalCPCounter;
	}

	public void setReportCounter(int reportCounter) {
		this.reportCounter = reportCounter;
	}

	public int getReportCounter() {
		return this.reportCounter;
	}

	public int getCriticalPoint() {
		return this.criticalPoint;
	}

	public int getTrackingPeriodInMillis() {
		return this.trackingPeriodInMillis;
	}

	public RobotReport getLastRobotReport() {
		return getRobotReport();
	}

	public abstract RobotReport getRobotReport();

	protected void onPositionUpdate() {
		String[] extraRobotState = null;
		if (cb != null) {
			extraRobotState = cb.onPositionUpdate();
		}

		if (tec.getVisualization() != null) {
			RobotReport rr = getRobotReport();
			tec.getVisualization().displayRobotState(te, rr, extraRobotState);

			RobotReport rrWaiting = getRobotReport();
			synchronized (tec.getCurrentDependencies()) {
				for (int robotID : tec.getCurrentDependencies().keySet()) {
					Dependency dep = tec.getCurrentDependencies().get(robotID);
					synchronized (tec.trackers) {
						AbstractTrajectoryEnvelopeTracker waitingTrackers = tec.trackers.get(dep.getWaitingRobotID());
						AbstractTrajectoryEnvelopeTracker drivingTrackers = tec.trackers.get(dep.getDrivingRobotID());
						if (waitingTrackers.equals(this)) {
							if (drivingTrackers != null) {
								RobotReport rrDriving = drivingTrackers.getRobotReport();
								String arrowIdentifier = "_"+dep.getWaitingRobotID()+"-"+dep.getDrivingRobotID();
								tec.getVisualization().displayDependency(rrWaiting, rrDriving, arrowIdentifier);
							}
						}
					}
				}
			}

			tec.getVisualization().updateVisualization();
		}
	}

	public abstract long getCurrentTimeInMillis();

	protected static AllenIntervalConstraint[] getConstriants(AllenIntervalConstraint.Type type, TrajectoryEnvelope env, TrajectoryEnvelopeSolver solver) {
		ArrayList<AllenIntervalConstraint> ret = new ArrayList<AllenIntervalConstraint>();
		Constraint[] incidentEdges = solver.getConstraintNetwork().getIncidentEdges(env);
		if (incidentEdges != null) {
			for (Constraint con : incidentEdges) {
				AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
				if (aic.getFrom().equals(env) || aic.getTo().equals(env)) {
					if (aic.getTypes()[0].equals(type)) ret.add(aic);
				}
			}
		}
		return ret.toArray(new AllenIntervalConstraint[ret.size()]);
	}

	protected void updateDeadline(TrajectoryEnvelope trajEnv, long delta) {
		synchronized(tec.getSolver()) {
			long time = getCurrentTimeInMillis()+delta;
			if (time > trajEnv.getTemporalVariable().getEET()) {
				tec.getSolver().removeConstraint(deadlines.get(trajEnv));
				long bound1 = Math.max(time, trajEnv.getTemporalVariable().getEET());
				long bound2 = APSPSolver.INF;
				AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(bound1, bound2));
				deadline.setFrom(trajEnv);
				deadline.setTo(trajEnv);
				boolean added = tec.getSolver().addConstraint(deadline);
				if (!added) {
					metaCSPLogger.severe("ERROR: Could not add deadline constraint " + deadline + " whose ET bounds are [" + trajEnv.getTemporalVariable().getEET() + "," + trajEnv.getTemporalVariable().getLET() +"]");
					throw new Error("Could not add deadline constraint " + deadline + " whose ET bounds are [" + trajEnv.getTemporalVariable().getEET() + "," + trajEnv.getTemporalVariable().getLET() +"]");
				}
				else deadlines.put(trajEnv, deadline);
			}
		}
	}

	protected void fixDeadline(TrajectoryEnvelope trajEnv, long delta) {
		synchronized(tec.getSolver()) {
			long time = getCurrentTimeInMillis()+delta;
			if (time > trajEnv.getTemporalVariable().getEET()) {
				tec.getSolver().removeConstraint(deadlines.get(trajEnv));
				long bound1 = Math.max(time, trajEnv.getTemporalVariable().getEET());
				long bound2 = bound1;
				metaCSPLogger.info("Finishing @ " + time + " " + trajEnv + " (ET bounds: [" + trajEnv.getTemporalVariable().getEET() + "," + trajEnv.getTemporalVariable().getLET() + "])");
				AllenIntervalConstraint deadline = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(bound1, bound2));
				deadline.setFrom(trajEnv);
				deadline.setTo(trajEnv);
				boolean added = tec.getSolver().addConstraint(deadline);
				if (!added) {
					metaCSPLogger.severe("ERROR: Could not add deadline constraint " + deadline + " whose ET bounds are [" + trajEnv.getTemporalVariable().getEET() + "," + trajEnv.getTemporalVariable().getLET() +"]");
					throw new Error("Could not add deadline constraint " + deadline + " whose ET bounds are [" + trajEnv.getTemporalVariable().getEET() + "," + trajEnv.getTemporalVariable().getLET() +"]");
				}
				else deadlines.put(trajEnv, deadline);
			}
		}
	}

	protected void setRelease(TrajectoryEnvelope trajEnv) {
		synchronized(tec.getSolver()) {
			long time = getCurrentTimeInMillis();
			time = Math.max(time, trajEnv.getTemporalVariable().getEST());
			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(time, time));
			release.setFrom(trajEnv);
			release.setTo(trajEnv);
			boolean added = tec.getSolver().addConstraint(release);
			if (!added) {
				metaCSPLogger.severe("ERROR: Could not add release " + release + " constraint on envelope " + trajEnv + " whose ST bounds are [" + trajEnv.getTemporalVariable().getEST() + "," + trajEnv.getTemporalVariable().getLST() +"]");
				throw new Error("Could not add release " + release + " constraint on envelope " + trajEnv + " whose ST bounds are [" + trajEnv.getTemporalVariable().getEST() + "," + trajEnv.getTemporalVariable().getLST() +"]");
			}
		}
	}

	protected TrajectoryEnvelope[] getAllSubEnvelopes() {
		Variable[] allVars = te.getRecursivelyDependentVariables();
		TrajectoryEnvelope[] allSubEnvelopes = new TrajectoryEnvelope[allVars.length];
		for (int i = 0; i < allVars.length; i++) {
			allSubEnvelopes[i] = (TrajectoryEnvelope)allVars[i];
		}
		return allSubEnvelopes;
	}

	public abstract void startTracking();

	public boolean trackingStarted() {
		return calledStartTracking;
	}

	protected void startMonitoringThread() {
		Thread monitorSubEnvelopes = new Thread("Abstract tracker " + te.getComponent()) {
			@Override
			public void run() {
				int prevSeqNumber = -1;

				if (cb != null) cb.beforeTrackingStart();

				while (true) {
					if (te.getTemporalVariable().getEST() <= getCurrentTimeInMillis()) {
						if (cb != null && !calledOnTrackingStart) {
							calledOnTrackingStart = true;
							cb.onTrackingStart();
						}

						if (!calledStartTracking) {
							calledStartTracking = true;
							startTracking();
						}

						RobotReport rr = null;
						while ((rr = tec.getRobotReport(te.getRobotID())) == null) {
							metaCSPLogger.info("(waiting for " + te.getComponent() + "'s tracker to come online)");
							try { Thread.sleep(100); }
							catch (InterruptedException e) { e.printStackTrace(); }
						}

						int currentSeqNumber = rr.getPathIndex();

						for (TrajectoryEnvelope subEnv : getAllSubEnvelopes()) {
							if (subEnv.hasSuperEnvelope()) {
								if (subEnv.getSequenceNumberStart() <= currentSeqNumber && !startedGroundEnvelopes.contains(subEnv)) {
									startedGroundEnvelopes.add(subEnv);
									metaCSPLogger.info(">>>> Dispatched (ground envelope) " + subEnv);
									if (cb != null) cb.onNewGroundEnvelope();
								}
								if (subEnv.getSequenceNumberEnd() < currentSeqNumber && !finishedGroundEnvelopes.contains(subEnv)) {
									finishedGroundEnvelopes.add(subEnv);
									metaCSPLogger.info("<<<< Finished (ground envelope) " + subEnv);
									if (subEnv.getSequenceNumberEnd() < te.getSequenceNumberEnd()) fixDeadline(subEnv, 0);
								} else if (!finishedGroundEnvelopes.contains(subEnv) && currentSeqNumber > prevSeqNumber) {
									updateDeadline(subEnv, 0);
								}
							}
						}

						if (te.getSequenceNumberEnd() == currentSeqNumber || (currentSeqNumber < prevSeqNumber && currentSeqNumber <= 0)) {
							metaCSPLogger.info("At last path point (current: " + currentSeqNumber + ", prev: " + prevSeqNumber + ") of " + te + "...");
							for (TrajectoryEnvelope toFinish : startedGroundEnvelopes) {
								if (!finishedGroundEnvelopes.contains(toFinish)) {
									metaCSPLogger.info("<<<< Finished (ground envelope) " + toFinish);
									finishedGroundEnvelopes.add(toFinish);
								}
							}
							break;
						}

						prevSeqNumber = currentSeqNumber;
					}

					try { Thread.sleep(trackingPeriodInMillis); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}

				synchronized (tec.getSolver()) {
					if (cb != null) cb.beforeTrackingFinished();
					finishTracking();
					if (cb != null) cb.onTrackingFinished();
				}
			}
		};

		monitorSubEnvelopes.start();
	}

	protected void finishTracking() {
		metaCSPLogger.info("<<<< Finished (super envelope) " + this.te);
		if (!(this instanceof TrajectoryEnvelopeTrackerDummy)) fixDeadline(te, 0);
	}

	public TrajectoryEnvelope getTrajectoryEnvelope() {
		return this.te;
	}

	public synchronized void pause(AutonomousVehicle byVehicle) {
		pauseByVehicles.add(byVehicle);
		System.out.println(pauseByVehicles);
		isPaused = true;
	}

	public synchronized void resume(AutonomousVehicle byVehicle) {
		if (pauseByVehicles.contains(byVehicle)) {
			pauseByVehicles.remove(byVehicle);
		}
		if (pauseByVehicles.isEmpty()) {
			isPaused = false;
			notifyAll();
		}
	}

	public void setPauseCounter(ArrayList<AutonomousVehicle> newList) {
		this.pauseByVehicles = newList;
	}

	public ArrayList<AutonomousVehicle> getPauseCounter() {
		return pauseByVehicles;
	}

	public void checkPause() {
		synchronized (this) {
			while (!pauseByVehicles.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public synchronized void slowDown(double targetVelocity) {
		slowdownCounter++;
		adjustVelocity(targetVelocity);
	}

	public synchronized void speedUp(double targetVelocity) {
		if (slowdownCounter > 0) {
			slowdownCounter--;
		}
		if (slowdownCounter == 0) {
			adjustVelocity(targetVelocity);
			notifyAll();
		}
	}

	protected void checkSlowdown() {
		synchronized (this) {
			while (slowdownCounter > 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void changePriority(Heuristics newHeuristics) {
		priorityChangeCounter++;
		adjustHeuristics(newHeuristics);
	}

	public synchronized void resetPriority(Heuristics newHeuristics) {
		if (priorityChangeCounter > 0) {
			priorityChangeCounter--;
		}
		if (priorityChangeCounter == 0) {
			adjustHeuristics(newHeuristics);
			notifyAll();
		}
	}

	protected void checkPriorityChange() {
		synchronized (this) {
			while (priorityChangeCounter > 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void adjustVelocity(double targetVelocity) {
		if (this instanceof AdaptiveTrackerRK4) {
			((AdaptiveTrackerRK4) this).maxVelocity = targetVelocity;
		}
	}

	protected void adjustHeuristics(Heuristics newHeuristics) {
		if (currentHeuristics != newHeuristics) {
			currentHeuristics = newHeuristics;
			tec.clearComparator();
			tec.addComparator(newHeuristics.getComparator());
		}
	}
}
