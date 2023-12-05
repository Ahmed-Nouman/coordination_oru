package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.List;
import java.util.Map;

/**
 * The ProjectData class is used to manage and store the project's data
 * including the map, vehicles, and poses.
 *
 * @author anm
 */
public class ProjectData {
    private String map;
    private List<Vehicle> vehicles;
    private Map<String, Pose> poses;

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

    public String getPoseName(Pose pose) {
        for (Map.Entry<String, Pose> entry : poses.entrySet()) {
            if (entry.getValue().equals(pose)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Inner class to represent a vehicle
    public static class Vehicle {
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
    }

    // Inner class to represent a mission step
    public static class MissionStep {
        private String poseName;
        private double duration; // in minutes

        public MissionStep() {
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

        @Override
        public String toString() {
            return "(" + poseName + ", " + duration + ")";
        }
    }
}
