package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;

import java.util.ArrayList;
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
    private List<AbstractVehicle> vehicles;
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
    public List<AbstractVehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Gets the vehicle.
     *
     * @return The specific vehicle object.
     */
    public AbstractVehicle getVehicle(String vehicleName) {
        for (AbstractVehicle vehicle : vehicles) {
            if (vehicle.getName().equals(vehicleName)) {
                return vehicle;
            }
        }
        return null;
    }

    public AbstractVehicle getVehicle(int ID) {
        for (AbstractVehicle vehicle : vehicles) {
            if (vehicle.getID() == ID) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * Adds a vehicle.
     *
     * @param vehicle The AutonomousVehicle object to add.
     */
    public void addVehicle(AbstractVehicle vehicle) {
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

    public void setVehicles(ArrayList<AbstractVehicle> vehicles) {
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

    //    public static class Vehicle {
//
//        private static int nextId = 1;
//        private final int id;
//        private String name;
//        private String type;
//        private double lookAheadDistance;
//        private String color;
//        private double maxVelocity;
//        private double maxAcceleration;
//        private double length;
//        private double width;
//        private String initialPose;
//        private List<Goal> goals;
//        private double safetyDistance;
//
//        public Vehicle() {
//            this.id = nextId;
//        }
//
//        public int getId() {
//            return id;
//        }
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getType() {
//            return type;
//        }
//
//        public void setType(String type) {
//            this.type = type;
//        }
//
//        public double getLookAheadDistance() {
//            return lookAheadDistance;
//        }
//
//        public void setLookAheadDistance(double lookAheadDistance) {
//            this.lookAheadDistance = lookAheadDistance;
//        }
//
//        public String getColor() {
//            return color;
//        }
//
//        public void setColor(String color) {
//            this.color = color;
//        }
//
//        public double getMaxVelocity() {
//            return maxVelocity;
//        }
//
//        public void setMaxVelocity(double maxVelocity) {
//            this.maxVelocity = maxVelocity;
//        }
//
//        public double getMaxAcceleration() {
//            return maxAcceleration;
//        }
//
//        public void setMaxAcceleration(double maxAcceleration) {
//            this.maxAcceleration = maxAcceleration;
//        }
//
//        public double getLength() {
//            return length;
//        }
//
//        public void setLength(double length) {
//            this.length = length;
//        }
//
//        public double getWidth() {
//            return width;
//        }
//
//        public void setWidth(double width) {
//            this.width = width;
//        }
//
//        public String getInitialPose() {
//            return initialPose;
//        }
//
//        public void setInitialPose(String initialPose) {
//            this.initialPose = initialPose;
//        }
//
//        public List<Goal> getGoals() {
//            return goals;
//        }
//
//        public void setGoals(List<Goal> goals) {
//            this.goals = goals;
//        }
//
//        public double getSafetyDistance() {
//            return safetyDistance;
//        }
//
//        public void setSafetyDistance(double safetyDistance) {
//            this.safetyDistance = safetyDistance;
//        }
//    }
}
