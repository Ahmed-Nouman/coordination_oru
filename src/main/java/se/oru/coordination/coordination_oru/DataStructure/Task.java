package se.oru.coordination.coordination_oru.DataStructure;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.Objects;

public class Task {
    private final Pose[] poses;
    private final Double time;

    public Task(Pose[] poses, Double time) {
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

    public Double getTime() {
        return time;
    }
}
