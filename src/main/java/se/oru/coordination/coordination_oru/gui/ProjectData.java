package se.oru.coordination.coordination_oru.gui;

import java.awt.*;
import java.util.Map;

public class ProjectData {
    private String map;
    private Map<String, Vehicle> vehicles;
    private Map<String, Pose> listOfAllPoses;

    // In ProjectData class:
    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public Map<String, Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(Map<String, Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Map<String, Pose> getListOfAllPoses() {
        return listOfAllPoses;
    }

    public void setListOfAllPoses(Map<String, Pose> listOfAllPoses) {
        this.listOfAllPoses = listOfAllPoses;
    }

    // Inner class representing a Robot
    public static class Vehicle {
        private double maxVelocity;
        private double maxAcceleration;
        private String color;
        private String initialPose;
        private String goalPoses;

        // In Vehicle inner class:
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

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
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
    }

    // Inner class representing a Pose
    public static class Pose {
        private double x;
        private double y;
        private double angle;

        // In Pose inner class:
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
}
