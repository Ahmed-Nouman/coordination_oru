package se.oru.coordination.coordination_oru.gui;

import java.util.List;
import java.util.Map;

public class ProjectData {
    private String map;
    private List<Vehicle> vehicles;
    private Map<String, Pose> listOfAllPoses;

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public List<Vehicle> getRobots() {
        return vehicles;
    }

    public void setRobots(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Map<String, Pose> getListOfAllPoses() {
        return listOfAllPoses;
    }

    public void setListOfAllPoses(Map<String, Pose> listOfAllPoses) {
        this.listOfAllPoses = listOfAllPoses;
    }

    // Define any additional methods or constructors as needed

    // Inner class representing a Robot
    public static class Vehicle {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;
        private double maxVelocity;
        private double maxAcceleration;
        private String color;
        private String initialPose;
        private String goalPoses;

        // Getter and setter methods for Robot fields

        // Define any additional methods or constructors as needed
    }

    // Inner class representing a Pose
    public static class Pose {
        private double x;
        private double y;
        private double angle;

        // Getter and setter methods for Pose fields

        // Define any additional methods or constructors as needed
    }
}
