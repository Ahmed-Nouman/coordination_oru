package se.oru.coordination.coordination_oru.dataStructue;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;

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
	
	private final TrajectoryEnvelope trajectoryEnvelope1;
	private final TrajectoryEnvelope trajectoryEnvelope2;
	private final int trajectoryEnvelopeStart1;
	private final int trajectoryEnvelopeStart2;
	private final int trajectoryEnvelopeEnd1;
	private final int trajectoryEnvelopeEnd2;

	public CriticalSection(TrajectoryEnvelope trajectoryEnvelope1, TrajectoryEnvelope trajectoryEnvelope2, int trajectoryEnvelopeStart1, int trajectoryEnvelopeStart2, int trajectoryEnvelopeEnd1, int trajectoryEnvelopeEnd2) {
		this.trajectoryEnvelope1 = trajectoryEnvelope1;
		this.trajectoryEnvelope2 = trajectoryEnvelope2;
		this.trajectoryEnvelopeStart1 = trajectoryEnvelopeStart1;
		this.trajectoryEnvelopeStart2 = trajectoryEnvelopeStart2;
		this.trajectoryEnvelopeEnd1 = trajectoryEnvelopeEnd1;
		this.trajectoryEnvelopeEnd2 = trajectoryEnvelopeEnd2;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		CriticalSection otherCriticalSection = (CriticalSection) object;
		if (trajectoryEnvelope1 == null || trajectoryEnvelope2 == null) {
			if (otherCriticalSection.trajectoryEnvelope1 != null && otherCriticalSection.trajectoryEnvelope2 != null)
				return false;
			else if (trajectoryEnvelope1 == null)
				return trajectoryEnvelope2.equals((otherCriticalSection.trajectoryEnvelope1 == null) ? otherCriticalSection.trajectoryEnvelope2 : otherCriticalSection.trajectoryEnvelope1);
		} else {
			boolean isIdenticalTrajectoryEnvelopes = trajectoryEnvelope1.equals(otherCriticalSection.trajectoryEnvelope1) && trajectoryEnvelope2.equals(otherCriticalSection.trajectoryEnvelope2);
			boolean isFlippedTrajectoryEnvelopes = trajectoryEnvelope1.equals(otherCriticalSection.trajectoryEnvelope2) && trajectoryEnvelope2.equals(otherCriticalSection.trajectoryEnvelope1);
			if (!(isIdenticalTrajectoryEnvelopes || isFlippedTrajectoryEnvelopes))
				return false;
			else if (isIdenticalTrajectoryEnvelopes) {
                return trajectoryEnvelopeEnd1 == otherCriticalSection.trajectoryEnvelopeEnd1 && trajectoryEnvelopeEnd2 == otherCriticalSection.trajectoryEnvelopeEnd2 && trajectoryEnvelopeStart1 == otherCriticalSection.trajectoryEnvelopeStart1 && trajectoryEnvelopeStart2 == otherCriticalSection.trajectoryEnvelopeStart2;
			}
			else {
                return trajectoryEnvelopeEnd1 == otherCriticalSection.trajectoryEnvelopeEnd2 && trajectoryEnvelopeEnd2 == otherCriticalSection.trajectoryEnvelopeEnd1 && trajectoryEnvelopeStart1 == otherCriticalSection.trajectoryEnvelopeStart2 && trajectoryEnvelopeStart2 == otherCriticalSection.trajectoryEnvelopeStart1;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((trajectoryEnvelope1 == null) ? 0 : trajectoryEnvelope1.hashCode()) + ((trajectoryEnvelope2 == null) ? 0 : trajectoryEnvelope2.hashCode());
		result = prime * result + trajectoryEnvelopeEnd1 + trajectoryEnvelopeEnd2;
		result = prime * result + Math.abs(trajectoryEnvelopeEnd1 - trajectoryEnvelopeEnd2);
		result = prime * result + trajectoryEnvelopeStart1 + trajectoryEnvelopeStart2;
		result = prime * result + Math.abs(trajectoryEnvelopeStart1 - trajectoryEnvelopeStart2);
		return result;
	}

	public TrajectoryEnvelope getTrajectoryEnvelope1() {
		return trajectoryEnvelope1;
	}

	public TrajectoryEnvelope getTrajectoryEnvelope2() {
		return trajectoryEnvelope2;
	}

	public int getTrajectoryEnvelopeStart1() {
		return trajectoryEnvelopeStart1;
	}

	public int getTrajectoryEnvelopeStart2() {
		return trajectoryEnvelopeStart2;
	}

	public int getTrajectoryEnvelopeEnd1() {
		return trajectoryEnvelopeEnd1;
	}

	public int getTrajectoryEnvelopeEnd2() {
		return trajectoryEnvelopeEnd2;
	}

	@Override
	public String toString() {
		String robot1 = (trajectoryEnvelope1 == null ? "null" : "Robot"+ trajectoryEnvelope1.getRobotID());
		String robot2 = (trajectoryEnvelope2 == null ? "null" : "Robot"+ trajectoryEnvelope2.getRobotID());
		return "CriticalSection (" + robot1 + " [" + trajectoryEnvelopeStart1 + ";" + trajectoryEnvelopeEnd1 + "], "
				+ robot2 + " [" + trajectoryEnvelopeStart2 + ";" + trajectoryEnvelopeEnd2 + "])";
	}
}
