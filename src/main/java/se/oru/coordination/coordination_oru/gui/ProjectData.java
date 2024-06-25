package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.io.Serializable;
import java.util.*;

/**
 * The ProjectData class is used to manage and store the project's data
 * including the map, vehicles, and poses.
 */
//FIXME: Maybe move the current data things to DataStatus class
public class ProjectData implements Serializable {
    private String map;
    private List<Vehicle> vehicles = new ArrayList<>();
    private Map<String, Pose> poses = new HashMap<>();
    private String trafficControl;
    private List<Trigger> triggers = new ArrayList<>();

    public String getMapImage(MapData mapData) {
        String mapFilePath = this.map;
        if (mapFilePath == null || mapFilePath.isEmpty()) {
            return null;
        }
        return String.join("/", Arrays.asList(mapFilePath.split("/")).subList(0,
                mapFilePath.split("/").length - 1)) + "/" + mapData.getImage();
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public Vehicle getVehicle(int vehicleID) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getID() == vehicleID) {
                return vehicle;
            }
        }
        return null;
    }

    public int getVehicleID(String vehicleName, List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            if (vehicleName.equals(vehicle.getName())) {
                return vehicle.getID();
            }
        }
        return -1;
    }

    public List<String> getVehicleNames() {
        List<String> vehicleNames = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            vehicleNames.add(vehicle.getName());
        }
        return vehicleNames;
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    public void removeVehicle(int vehicleID) {
        vehicles.removeIf(vehicle -> vehicle.getID() == vehicleID);
    }

    @JsonIgnore
    public ArrayList<String> getPosesName() {
        return new ArrayList<>(poses.keySet());
    }

    public Map<String, Pose> getPoses() {
        return poses;
    }

    public Pose getPose(String poseName) {
        return poses.get(poseName);
    }

    public void setPoses(Map<String, Pose> poses) {
        this.poses = poses;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public String getTrafficControl() {
        return trafficControl;
    }

    public void setTrafficControl(String trafficControl) {
        this.trafficControl = trafficControl;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectData that = (ProjectData) o;
        return Objects.equals(map, that.map) &&
                Objects.equals(vehicles, that.vehicles) &&
                Objects.equals(poses, that.poses) &&
                Objects.equals(trafficControl, that.trafficControl) &&
                Objects.equals(triggers, that.triggers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, vehicles, poses, trafficControl, triggers);
    }

    public void addPose(String poseName, Pose pose) {
        poses.put(poseName, pose);
    }

    public int noOfPoses() {
        return this.getPoses().size();
    }

    public static class Vehicle implements Serializable {
        private static int nextId = 1;
        private final int ID;
        private String name;
        private int priority;
        private String type;
        private double lookAheadDistance;
        private String color;
        private double maxVelocity;
        private double maxAcceleration;
        private double length;
        private double width;
        private String initialPose;
        private String pathFile;
        private List<TaskStep> task;
        private int taskRepetition;
        private double safetyDistance;

        public Vehicle() {
            this.ID = nextId++;
        }

        public int getID() {
            return ID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getLookAheadDistance() {
            return lookAheadDistance;
        }

        public void setLookAheadDistance(double lookAheadDistance) {
            this.lookAheadDistance = lookAheadDistance;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public double getMaxVelocity() {
            return maxVelocity;
        }

        public void setMaxVelocity(double maxVelocity) {
            this.maxVelocity = maxVelocity;
        }

        public double getMaxAcceleration() {
            return maxAcceleration;
        }

        public void setMaxAcceleration(double maxAcceleration) {
            this.maxAcceleration = maxAcceleration;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public String getInitialPose() {
            return initialPose;
        }

        public void setStartPose(String initialPose) {
            this.initialPose = initialPose;
        }

        public List<TaskStep> getTasks() {
            return task;
        }

        public void setTask(List<TaskStep> task) {
            this.task = task;
        }

        public int getTasksRepetition() {
            return taskRepetition;
        }

        public void setTaskRepetition(int taskRepetition) {
            this.taskRepetition = taskRepetition;
        }

        public double getSafetyDistance() {
            return safetyDistance;
        }

        public void setSafetyDistance(double safetyDistance) {
            this.safetyDistance = safetyDistance;
        }

        public String getPathFile() {
            return pathFile;
        }

        public void setPathFile(String pathFile) {
            this.pathFile = pathFile;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            var vehicle = (Vehicle) object;
            return ID == vehicle.ID &&
                    priority == vehicle.priority &&
                    Double.compare(lookAheadDistance, vehicle.lookAheadDistance) == 0 &&
                    Double.compare(maxVelocity, vehicle.maxVelocity) == 0 &&
                    Double.compare(maxAcceleration, vehicle.maxAcceleration) == 0 &&
                    Double.compare(length, vehicle.length) == 0 &&
                    Double.compare(width, vehicle.width) == 0 &&
                    taskRepetition == vehicle.taskRepetition &&
                    Double.compare(safetyDistance, vehicle.safetyDistance) == 0 &&
                    Objects.equals(name, vehicle.name) &&
                    Objects.equals(type, vehicle.type) &&
                    Objects.equals(color, vehicle.color) &&
                    Objects.equals(initialPose, vehicle.initialPose) &&
                    Objects.equals(task, vehicle.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ID, name, priority, type, lookAheadDistance, color, maxVelocity, maxAcceleration, length, width, initialPose, task, taskRepetition, safetyDistance);
        }
    }

    public static class TaskStep implements Serializable {
        private String taskName;
        private String poseName;
        private double duration; // in minutes
        private int priority;
        private int repetition;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getPoseName() {
            return poseName;
        }

        public void setPoseName(String poseName) {
            this.poseName = poseName;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getRepetition() {
            return repetition;
        }

        public void setRepetition(int repetition) {
            this.repetition = repetition;
        }

        @Override
        public String toString() {
            return taskName + " (" + poseName + ", " + duration + ", " + priority + ", " + repetition + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaskStep that = (TaskStep) o;
            return Double.compare(duration, that.duration) == 0 &&
                    priority == that.priority &&
                    repetition == that.repetition &&
                    Objects.equals(taskName, that.taskName) &&
                    Objects.equals(poseName, that.poseName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskName, poseName, duration, priority, repetition);
        }
    }

    public static class Trigger implements Serializable {
        private String vehicle;
        private List<String> task;
        private List<String> vehicleToComply;

        public String getVehicle() {
            return vehicle;
        }

        public void setVehicle(String vehicle) {
            this.vehicle = vehicle;
        }

        public List<String> getTask() {
            return task;
        }

        public void setTask(List<String> task) {
            this.task = task;
        }

        public List<String> getVehicleToComply() {
            return vehicleToComply;
        }

        public void setVehicleToComply(List<String> vehicleToComply) {
            this.vehicleToComply = vehicleToComply;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Trigger trigger = (Trigger) o;
            return Objects.equals(vehicle, trigger.vehicle) &&
                    Objects.equals(task, trigger.task) &&
                    Objects.equals(vehicleToComply, trigger.vehicleToComply);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vehicle, task, vehicleToComply);
        }
    }
}
