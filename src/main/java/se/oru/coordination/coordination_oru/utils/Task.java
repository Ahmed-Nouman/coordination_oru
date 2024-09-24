package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Arrays;
import java.util.Objects;

public class Task {
    private static final int MINUTE_TO_SECOND = 60;
    private static final int SECOND_TO_MILLISECOND = 1000;
    private final String name;
    private final Pose[] poses;
    private final int[] stoppageTimes;
    private final double startTime;
    private final int priority;

    public Task(String name, double startTime, Pose[] poses, int[] stoppageTimes, int priority) {
        this.name = name;
        this.startTime = startTime;
        this.poses = poses;
        // Handle null or empty stoppageTimes by initializing it to an array of zeros with the same length as poses
        if (stoppageTimes == null || stoppageTimes.length == 0) {
            this.stoppageTimes = new int[poses.length];
        } else {
            this.stoppageTimes = stoppageTimes;
        }
        this.priority = priority;
    }

    public Task(String name, double startTime, Pose[] poses, int priority) {
        this(name, startTime, poses, new int[poses.length], priority);
    }

    public boolean isEmpty() {
        return Objects.equals(name, "") &&
                poses.length == 0 &&
                startTime == 0 &&
                priority == 0;
    }

    public String getName() {
        return name;
    }

    public Pose[] getPoses() {
        return poses;
    }

    public double getTimeInMinutes() {
        return startTime;
    }

    public long getTimeInMillisecond() {
        return (long) (startTime * MINUTE_TO_SECOND * SECOND_TO_MILLISECOND);
    }

    public int getPriority() {
        return priority;
    }

    public int[] getStoppageTimes() {
        return stoppageTimes;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", startTime=" + startTime +
                ", poses=" + Arrays.toString(poses) +
                ", stoppageTimes=" + Arrays.toString(stoppageTimes) +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Double.compare(startTime, task.startTime) == 0 &&
                priority == task.priority &&
                Objects.equals(name, task.name) &&
                Objects.deepEquals(poses, task.poses) &&
                Objects.deepEquals(stoppageTimes, task.stoppageTimes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(poses), Arrays.hashCode(stoppageTimes), startTime, priority);
    }
}