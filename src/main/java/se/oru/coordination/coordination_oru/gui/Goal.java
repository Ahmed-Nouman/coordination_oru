package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Goal {
    private String pose;
    private long duration;

    @JsonCreator
    public Goal(@JsonProperty("pose") String pose, @JsonProperty("duration") long duration) {
        this.pose = pose;
        this.duration = duration * 1000 * 1000;
    }

    public String getPose() {
        return pose;
    }

    public void setPose(String pose) {
        this.pose = pose;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
