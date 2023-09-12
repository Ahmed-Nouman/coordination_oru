package se.oru.coordination.coordination_oru;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeTrackerRK4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

/**
 * A critical section is defined as a quadruple (te1, te2, [start1,end1], [start2,end2]), where
 * te1 and te2 are {@link TrajectoryEnvelope}s that overlap when robot 1 is in pose
 * with index start1 and robot 2 is in pose with index start2, until the two robots reach poses
 * end1 and end2 respectively.
 * 
 * @author fpa
 *
 */
public class CriticalSection {
	
	private TrajectoryEnvelope te1;
	private TrajectoryEnvelope te2;
	private int te1Start = -1;
	private int te2Start = -1;
	private int te1End = -1;
	private int te2End = -1;
	private int te1Break = -1;
	private int te2Break = -1;
	public int te1HigherWeight = 0;
	public int te2HigherWeight = 0;

	// Transient information (not used in `equal`, `hashCode`):
	public int robotIDInferior = -1;

	public CriticalSection(TrajectoryEnvelope te1, TrajectoryEnvelope te2, int te1Start, int te2Start, int te1End, int te2End) {
		this.te1 = te1;
		this.te2 = te2;
		this.te1Start = te1Start;
		this.te2Start = te2Start;
		this.te1End = te1End;
		this.te2End = te2End;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CriticalSection other = (CriticalSection) obj;
		if (te1 == null || te2 == null) {
			if (other.te1 != null && other.te2 != null)
				return false;
			else if (te1 == null) 
				return te2.equals((other.te1 == null) ? other.te2 : other.te1);
		} else {
			if (!(te1.equals(other.te1) && te2.equals(other.te2) || te1.equals(other.te2) && te2.equals(other.te1)))
				return false;
			if (te1.equals(other.te1) && te2.equals(other.te2)) {
				if (te1End != other.te1End || te2End != other.te2End || te1Start != other.te1Start || te2Start != other.te2Start)
					return false;
			}
			else if (te1.equals(other.te2) && te2.equals(other.te1)) {
				if (te1End != other.te2End || te2End != other.te1End || te1Start != other.te2Start || te2Start != other.te1Start)
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((te1 == null) ? 0 : te1.hashCode()) + ((te2 == null) ? 0 : te2.hashCode());
		result = prime * result + te1End + te2End;
		result = prime * result + Math.abs(te1End - te2End);
		result = prime * result + te1Start + te2Start;
		result = prime * result + Math.abs(te1Start - te2Start);
		return result;
	}

	public TrajectoryEnvelope getTe1() {
		return te1;
	}

	public TrajectoryEnvelope getTe2() {
		return te2;
	}

	public int getTe1Start() {
		return te1Start;
	}

	public int getTe2Start() {
		return te2Start;
	}

	public int getTe1End() {
		return te1End;
	}

	public int getTe2End() {
		return te2End;
	}

	public int getTe1Break() {
		return te1Break;
	}
	
	public int getTe2Break() {
		return te2Break;
	}
	
	public void setTe1Break(int te1Break) {
		this.te1Break = te1Break;
	}
	
	public void setTe2Break(int te2Break) {
		this.te2Break = te2Break;
	}

	public Integer getTe1RobotID() {
		if (getTe1() == null) {
			return null;
		}
		return getTe1().getRobotID();
	}

	public Integer getTe2RobotID() {
		if (getTe2() == null) {
			return null;
		}
		return getTe2().getRobotID();
	}

	public boolean isTe1(int robotID) {
		return getTe1RobotID() == robotID;
	}

	public boolean isTe2(int robotID) {
		return getTe2RobotID() == robotID;
	}

	public int getOtherRobotID(int robotID) {
		return isTe1(robotID) ? getTe2RobotID() : getTe1RobotID();
	}

	public Integer getStart(int robotID) {
		if (isTe1(robotID)) {
			return getTe1Start();
		}
		if (isTe2(robotID)) {
			return getTe2Start();
		}
		return null;
	}

	public Integer getEnd(int robotID) {
		if (isTe1(robotID)) {
			return getTe1End();
		}
		if (isTe2(robotID)) {
			return getTe2End();
		}
		return null;
	}

	public boolean isRobotOnCS(int robotID) {
		int index = TrajectoryEnvelopeCoordinatorSimulation.tec.getRobotReport(robotID).getPathIndex();
		return getStart(robotID) <= index && index <= getEnd(robotID);
	}

	public void setHigher(int robotID, int weight) {
		if (isTe1(robotID)) {
			te1HigherWeight = weight;
		} else if (isTe2(robotID)) {
			te2HigherWeight = weight;
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public String toString() {
		return toStringForVisualization();
	}

	class DistanceEstimation {
		int currentIndex;
		int goalIndex;
		int deltaIndexes;
		double distance;
		double velocity;
		double time;

		public DistanceEstimation(int robotID, boolean isInferior) {
			TrajectoryEnvelopeCoordinator tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
			RobotReport rr = tec.getRobotReport(robotID);

			currentIndex = rr.getPathIndex();
			goalIndex = currentIndex == -1 ? -1 : isInferior ? getEnd(robotID) : getStart(robotID);
			deltaIndexes = Math.max(0, goalIndex - currentIndex);

			distance = deltaIndexes == 0 ? 0.0 :
					TrajectoryEnvelopeTrackerRK4.computeDistance(tec.trackers.get(robotID).traj, currentIndex, goalIndex);
			velocity = estimateVelocity(robotID, isInferior);
			time = distance / velocity;
		}

		protected double estimateVelocity(int robotID, boolean isInferior) {
			TrajectoryEnvelopeCoordinator tec = TrajectoryEnvelopeCoordinatorSimulation.tec;
			double maxVelocity = tec.getRobotMaxVelocity(robotID);

			if (! isInferior) {
				return maxVelocity;
			}

			RobotReport rr = tec.getRobotReport(robotID);
			double currentVelocity = rr.getVelocity();
			double delta = Math.abs(maxVelocity - currentVelocity);

			return Math.min(currentVelocity, maxVelocity) + 0.4 * delta;
		}

		@Override
		public String toString() {
			return String.format(
					"%d pt = %.1f m ≈ %.1f s at %.1f m/s",
					deltaIndexes, distance, time, velocity
			);
		}
	}

	public String toStringForVisualization() {
		String ret = "";
		String robot1 = getTe1() == null ? "null" : String.valueOf(getTe1().getRobotID());
		String robot2 = getTe2() == null ? "null" : String.valueOf(getTe2().getRobotID());
		ret += robot1 + makeStars(te1HigherWeight) + " [" + getTe1Start() + ";" + getTe1End() + "], ";
		ret += robot2 + makeStars(te2HigherWeight) + " [" + getTe2Start() + ";" + getTe2End() + "]";

		if (robotIDInferior == -1) {
			return ret;
		}
		int robotIDSuperior = getOtherRobotID(robotIDInferior);

		DistanceEstimation estimationInferior = new DistanceEstimation(robotIDInferior, true);
		DistanceEstimation estimationSuperior = new DistanceEstimation(robotIDSuperior, false);
		ret += String.format(
				" (inferior %d %s make %s before superior %d makes %s)",
				robotIDInferior, canPassFirst(robotIDInferior) ? "will" : "will NOT", estimationInferior,
				robotIDSuperior, estimationSuperior
		);
		return ret;
	}

	public boolean canPassFirst(int myID) {
		int otherID = getOtherRobotID(myID);

		boolean isInferior = myID == robotIDInferior;
		if (! isInferior) {
			assert otherID == robotIDInferior;
		}

		DistanceEstimation myEstimation = new DistanceEstimation(myID, isInferior);
		DistanceEstimation otherEstimation = new DistanceEstimation(otherID, ! isInferior);

		return myEstimation.time < otherEstimation.time;
	}

	private String makeStars(int count) {
		return new String(new char[count]).replace("\0", "*");
	}

	public static ArrayList<CriticalSection> sortCriticalSections(Collection<CriticalSection> criticalSectionsUnsorted) {
		ArrayList<CriticalSection> criticalSections = new ArrayList<>(criticalSectionsUnsorted);
		criticalSections.sort(new Comparator<CriticalSection>() {
			@Override
			public int compare(CriticalSection cs1, CriticalSection cs2) {
				int[] list1 = csToInts(cs1);
				int[] list2 = csToInts(cs2);
				for (int i = 0; i < list1.length; i++) {
					int value = Integer.compare(list1[i], list2[i]);
					if (value != 0)
						return value;
				}
				return 0;
			}

			private int[] csToInts(CriticalSection cs) {
				return new int[] {
						(cs.getTe1() == null ? -1 : cs.getTe1().getRobotID()),
						cs.getTe1Start(),
						cs.getTe1End(),
						(cs.getTe2() == null ? -1 : cs.getTe2().getRobotID()),
						cs.getTe2Start(),
						cs.getTe2End(),
				};
			}
		});
		return criticalSections;
	}

	public static ArrayList<CriticalSection> sortCriticalSectionsForRobotID(Collection<CriticalSection> criticalSectionsUnsorted, int robotID) {
		ArrayList<CriticalSection> criticalSections = new ArrayList<>(criticalSectionsUnsorted);

		Function<CriticalSection, Integer[]> csToInts = (CriticalSection cs) -> {
			if (cs.getTe1RobotID() == robotID) {
				return new Integer[] { cs.getTe1Start(), cs.getTe1End(), 1 };
			}
			if (cs.getTe2RobotID() == robotID) {
				return new Integer[] { cs.getTe2Start(), cs.getTe2End(), 2 };
			}
			return new Integer[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		};

		criticalSections.sort(new Comparator<CriticalSection>() {
			@Override
			public int compare(CriticalSection cs1, CriticalSection cs2) {
				Integer[] list1 = csToInts.apply(cs1);
				Integer[] list2 = csToInts.apply(cs2);
				for (int i = 0; i < list1.length; i++) {
					int value = Integer.compare(list1[i], list2[i]);
					if (value != 0)
						return value;
				}
				return 0;
			}

		});
		while (! criticalSections.isEmpty() &&
				csToInts.apply(criticalSections.get(criticalSections.size() - 1))[0] == Integer.MAX_VALUE) {
			criticalSections.remove(criticalSections.size() - 1);
		}
		return criticalSections;
	}
}
