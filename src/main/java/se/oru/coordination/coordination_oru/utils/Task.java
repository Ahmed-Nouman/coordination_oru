package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Arrays;
import java.util.Objects;

public class Task {
    private static final int MINUTE_TO_SECOND = 60;
    private static final int SECOND_TO_MILLISECOND = 1000;
    private final String name;
    private final Pose[] poses;
    private final double time;
    private final int priority;

    public Task(String name, double time, Pose[] poses, int priority) {
        this.name = name;
        this.poses = poses;
        this.time = time;
        this.priority = priority;
    }

    public boolean isEmpty() {
        return Objects.equals(name, "") &&
                poses.length == 0 &&
                time == 0 &&
                priority == 0;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", poses=" + Arrays.toString(poses) +
                ", time=" + time +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Double.compare(time, task.time) == 0 && priority == task.priority && Objects.equals(name, task.name) && Objects.deepEquals(poses, task.poses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(poses), time, priority);
    }
}
