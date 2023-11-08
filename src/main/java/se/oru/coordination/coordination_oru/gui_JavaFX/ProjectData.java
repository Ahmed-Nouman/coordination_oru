package se.oru.coordination.coordination_oru.gui_JavaFX;

import java.util.Map;

public class ProjectData {
    private String map;
    private Map<String, Vehicle> vehicles;
    private Map<String, Pose> listOfAllPoses;

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public void setVehicles(Map<String, Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Map<String, Vehicle> getVehicles() {
        return vehicles;
    }

    // Method to add a single vehicle to the map
    public void addVehicle(String key, Vehicle vehicle) {
        this.vehicles.put(key, vehicle);
    }

    public void removeVehicle(String key) {
        this.vehicles.remove(key);
    }

    public void setListOfAllPoses(Map<String, Pose> listOfAllPoses) {
        this.listOfAllPoses = listOfAllPoses;
    }

    public Map<String, Pose> getListOfAllPoses() {
        return listOfAllPoses;
    }

    // Method to add a single pose to the map
    public void addPose(String key, Pose pose) {
        this.listOfAllPoses.put(key, pose);
    }
}

class Vehicle {
    private double maxVelocity;
    private double maxAcceleration;
    private int trackingPeriod;
    private double safetyDistance;
    private String color;
    private String type;
    private double lookAheadDistance;
    private String initialPose;
    private String goalPoses;
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

    public int getTrackingPeriod() {
        return trackingPeriod;
    }

    public void setTrackingPeriod(int trackingPeriod) {
        this.trackingPeriod = trackingPeriod;
    }

    public double getSafetyDistance() {
        return safetyDistance;
    }

    public void setSafetyDistance(double safetyDistance) {
        this.safetyDistance = safetyDistance;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInitialPose() {
        return initialPose;
    }

    public void setInitialPose(String initialPose) {
        this.initialPose = initialPose;
    }

    public String getGoalPoses() {
        return goalPoses;
    }

    public void setGoalPoses(String goalPoses) {
        this.goalPoses = goalPoses;
    }

    public double getLookAheadDistance() {
        return lookAheadDistance;
    }

    public void setLookAheadDistance(double lookAheadDistance) {
        this.lookAheadDistance = lookAheadDistance;
    }
}

class Pose {
    private double x;
    private double y;
    private double angle;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
