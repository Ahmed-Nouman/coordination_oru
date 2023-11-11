package se.oru.coordination.coordination_oru.gui_JavaFX;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.util.HashMap;
import java.util.Map;

/**
 * The ProjectData class is used to manage and store the project's data
 * including the map, vehicles, and poses.
 *
 * @author anm
 */
public class ProjectData {
    private String map;
    private Map<String, Vehicle> vehicles;
    private Map<String, Pose> listOfAllPoses;

    /**
     * Constructor for ProjectData.
     * Initializes the map, vehicles, and listOfAllPoses as empty.
     */
    public ProjectData() {
        map = "";
        vehicles = new HashMap<>();
        listOfAllPoses = new HashMap<>();
    }

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
    public Map<String, Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Sets the vehicles.
     *
     * @param vehicles A map of vehicle IDs to AutonomousVehicle objects.
     */
    public void setVehicles(Map<String, Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    /**
     * Adds a vehicle.
     *
     * @param key     The key to associate with the vehicle.
     * @param vehicle The AutonomousVehicle object to add.
     */
    public void addVehicle(String key, Vehicle vehicle) {
        this.vehicles.put(key, vehicle);
    }

    /**
     * Removes a vehicle.
     *
     * @param key The key of the vehicle to remove.
     */
    public void removeVehicle(String key) {
        this.vehicles.remove(key);
    }

    /**
     * Gets the list of all poses.
     *
     * @return A map of pose IDs to Pose objects.
     */
    public Map<String, Pose> getListOfAllPoses() {
        return listOfAllPoses;
    }

    /**
     * Sets the list of all poses.
     *
     * @param listOfAllPoses A map of pose IDs to Pose objects.
     */
    public void setListOfAllPoses(Map<String, Pose> listOfAllPoses) {
        this.listOfAllPoses = listOfAllPoses;
    }

    /**
     * Adds a pose.
     *
     * @param key  The key to associate with the pose.
     * @param pose The Pose object to add.
     */
    public void addPose(String key, Pose pose) {
        this.listOfAllPoses.put(key, pose);
    }

    /**
     * Sets poses from a map of positions.
     *
     * @param poses A map of pose IDs to position values.
     */
    public void setPoses(Map<String, Map<String, Double>> poses) {
        listOfAllPoses.clear();
        for (Map.Entry<String, Map<String, Double>> entry : poses.entrySet()) {
            String key = entry.getKey();
            Map<String, Double> value = entry.getValue();
            double x = value.getOrDefault("x", 0.0);
            double y = value.getOrDefault("y", 0.0);
            double z = value.getOrDefault("z", Double.NaN); // Assuming 'z' might not always be present
            double roll = value.getOrDefault("roll", Double.NaN); // Assuming 'roll' might not always be present
            double pitch = value.getOrDefault("pitch", Double.NaN); // Assuming 'pitch' might not always be present
            double yaw = value.getOrDefault("yaw", value.getOrDefault("angle", 0.0)); // 'angle' is treated as 'yaw' if present

            Pose pose = new Pose(x, y, z, roll, pitch, yaw);
            listOfAllPoses.put(key, pose);
        }
    }

    public static class Vehicle {

        private String type;
        private double lookAheadDistance;
        private String color;
        private double maxVelocity;
        private double maxAcceleration;
        private double length;
        private double width;
        private String initialPose;
        private String[] goalPoses;
        private double safetyDistance;

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

        public String[] getGoalPoses() {
            return goalPoses;
        }

        public void setGoalPoses(String[] goalPoses) {
            this.goalPoses = goalPoses;
        }

        public double getSafetyDistance() {
            return safetyDistance;
        }

        public void setSafetyDistance(double safetyDistance) {
            this.safetyDistance = safetyDistance;
        }
    }
}
