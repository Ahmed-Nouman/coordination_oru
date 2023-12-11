package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;

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

    /**
     * Gets the map.
     *
     * @return A string representing the map.
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
        return -1; // Return -1 if the vehicle is not found
    }

    /**
     * Adds a vehicle.
     *
     * @param vehicle The AutonomousVehicle object to add.
     */
    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    /**
     * Removes a vehicle.
     *
     */
    public void removeVehicle(int vehicleID) {
        vehicles.removeIf(vehicle -> vehicle.getID() == vehicleID);
    }

    /**
     * Gets the list of all poses.
     *
     * @return A map of pose IDs to Pose objects.
     */
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

    // Inner class to represent a vehicle
    public static class Vehicle implements Serializable{
        private static int nextId = 1;
        private final int ID;
        private String name;
        private String type;
        private double lookAheadDistance;
        private String color;
        private double maxVelocity;
        private double maxAcceleration;
        private double length;
        private double width;
        private String initialPose;
        private List<MissionStep> mission;
        private int missionRepetition;
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

        public void setInitialPose(String initialPose) {
            this.initialPose = initialPose;
        }

        public List<MissionStep> getMission() {
            return mission;
        }

        public void setMission(List<MissionStep> mission) {
            this.mission = mission;
        }

        public int getMissionRepetition() {
            return missionRepetition;
        }

        public void setMissionRepetition(int missionRepetition) {
            this.missionRepetition = missionRepetition;
        }

        public double getSafetyDistance() {
            return safetyDistance;
        }

        public void setSafetyDistance(double safetyDistance) {
            this.safetyDistance = safetyDistance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vehicle vehicle = (Vehicle) o;
            return ID == vehicle.ID && Double.compare(lookAheadDistance, vehicle.lookAheadDistance) == 0 && Double.compare(maxVelocity, vehicle.maxVelocity) == 0 && Double.compare(maxAcceleration, vehicle.maxAcceleration) == 0 && Double.compare(length, vehicle.length) == 0 && Double.compare(width, vehicle.width) == 0 && missionRepetition == vehicle.missionRepetition && Double.compare(safetyDistance, vehicle.safetyDistance) == 0 && Objects.equals(name, vehicle.name) && Objects.equals(type, vehicle.type) && Objects.equals(color, vehicle.color) && Objects.equals(initialPose, vehicle.initialPose) && Objects.equals(mission, vehicle.mission);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ID, name, type, lookAheadDistance, color, maxVelocity, maxAcceleration, length, width, initialPose, mission, missionRepetition, safetyDistance);
        }
    }

    // Inner class to represent a mission step
    public static class MissionStep implements Serializable{
        private String poseName;
        private double duration; // in minutes

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

        @Override
        public String toString() {
            return "(" + poseName + ", " + duration + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MissionStep that = (MissionStep) o;
            return Double.compare(duration, that.duration) == 0 && Objects.equals(poseName, that.poseName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(poseName, duration);
        }
    }
}
