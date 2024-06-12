package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;

import java.io.Serializable;
import java.util.*;

/**
 * The ProjectData class is used to manage and store the project's data
 * including the map, vehicles, and poses.
 *
 * @author anm
 */
public class ProjectData implements Serializable {
    private String map;
    private List<Vehicle> vehicles = new ArrayList<>();
    private Map<String, Pose> poses = new HashMap<>();

    public String getMapImage(MapData mapData) {
        String mapFilePath = this.map;
        if (mapFilePath == null || mapFilePath.isEmpty()) {
            return null;
        }
        return String.join("/", Arrays.asList(mapFilePath.split("/")).subList(0,
                mapFilePath.split("/").length - 1)) + "/" + mapData.getImage();
    }

    /**
     * Gets the map YAML file location.
     *
     * @return A string representing the location of the map YAML file.
     */
    public String getMap() {
        return map;
    }

    /**
     * Sets the map.
     *
     * @param map A string representing the map.
     */
    public void setMap(String map) {
        this.map = map;
    }

    /**
     * Gets the vehicles.
     *
     * @return A map of vehicle IDs to AutonomousVehicle objects.
     */
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Gets the vehicle.
     *
     * @return The specific vehicle object.
     */
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

    /**
     * Sets the list of all poses.
     *
     * @param poses A map of pose IDs to Pose objects.
     */
    public void setPoses(Map<String, Pose> poses) {
        this.poses = poses;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectData that = (ProjectData) o;
        return Objects.equals(map, that.map) && Objects.equals(vehicles, that.vehicles) && Objects.equals(poses, that.poses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, vehicles, poses);
    }

    public void addPose(String poseName, Pose pose) {
        poses.put(poseName, pose);
    }

    public int noOfPoses() {
        return this.getPoses().size();
    }

    public static class Vehicle implements Serializable{
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

        public List<TaskStep> getTask() {
            return task;
        }

        public void setTask(List<TaskStep> task) {
            this.task = task;
        }

        public int getTaskRepetition() {
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
            return ID == vehicle.ID && priority == vehicle.priority && Double.compare(lookAheadDistance, vehicle.lookAheadDistance) == 0 && Double.compare(maxVelocity, vehicle.maxVelocity) == 0 && Double.compare(maxAcceleration, vehicle.maxAcceleration) == 0 && Double.compare(length, vehicle.length) == 0 && Double.compare(width, vehicle.width) == 0 && taskRepetition == vehicle.taskRepetition && Double.compare(safetyDistance, vehicle.safetyDistance) == 0 && Objects.equals(name, vehicle.name) && Objects.equals(type, vehicle.type) && Objects.equals(color, vehicle.color) && Objects.equals(initialPose, vehicle.initialPose) && Objects.equals(task, vehicle.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ID, name, priority, type, lookAheadDistance, color, maxVelocity, maxAcceleration, length, width, initialPose, task, taskRepetition, safetyDistance);
        }
    }

    // Inner class to represent a task step TODO: Can i use the same class as Task?
    public static class TaskStep implements Serializable{
        private String taskName;
        private String poseName;
        private double duration; // in minutes
        private int priority;

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

        @Override
        public String toString() {
            return taskName + " (" + duration + ", " + poseName + ", " + priority + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaskStep that = (TaskStep) o;
            return Double.compare(duration, that.duration) == 0 && Objects.equals(poseName, that.poseName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(poseName, duration);
        }
    }
}
