package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Arrays;
import java.util.Objects;

//TODO: Maybe i need to add name to the task
public class Task {
    public static final int MINUTE_TO_SECOND = 60;
    public static final int SECOND_TO_MILLISECOND = 1000;
    private final Pose[] poses;
    private final double time;
    private final int priority;

    public Task(double time, Pose[] poses, int priority) {
        this.poses = poses;
        this.time = time;
        this.priority = priority;
    }

    public boolean isEmpty() {
        return poses.length == 0 &&
                time == 0 &&
                priority == 0;
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

    public int getPriority() {
        return priority;
    }
}
