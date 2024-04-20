package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Arrays;
import java.util.Objects;

public class Task {
    public static final int MINUTE_TO_SECOND = 60;
    public static final int SECOND_TO_MILLISECOND = 1000;
    private final Pose[] poses;
    private final double time;
    private final boolean priority;

    public Task(double time, Pose[] poses, boolean priority) {
        this.poses = poses;
        this.time = time;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Task{" +
                "poses=" + Arrays.toString(poses) +
                ", time=" + time +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Arrays.equals(poses, task.poses) &&
                Objects.equals(time, task.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(poses), time);
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

    public boolean isPrioritized() {
        return priority;
    }
}
