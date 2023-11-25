package se.oru.coordination.coordination_oru.util;

import org.metacsp.multi.spatioTemporal.paths.Pose;

public class Goal {
    private Pose pose;
    private long duration;

    public Goal(Pose pose, long duration) {
        this.pose = pose;
        this.duration = duration * 1000 * 1000;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
