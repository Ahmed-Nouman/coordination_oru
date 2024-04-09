package se.oru.coordination.coordination_oru.dataStructue;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Objects;

public class Task {
    public static final int MINUTE_TO_SECOND = 60;
    public static final int SECOND_TO_MILLISECOND = 1000;
    private final Pose[] poses;
    private final double time;

    public Task(double time, Pose[] poses) {
        this.poses = poses;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Task{" +
                "poses=" + poses +
                ", time=" + time +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(poses, task.poses) &&
                Objects.equals(time, task.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poses, time);
    }

    public Pose[] getPoses() {
        return poses;
    }

    public double getTimeInMinutes() {
        return time;
    }

    public long getTimeInMillisecond() {
        return (long) (time * MINUTE_TO_SECOND * SECOND_TO_MILLISECOND);
    }
}
