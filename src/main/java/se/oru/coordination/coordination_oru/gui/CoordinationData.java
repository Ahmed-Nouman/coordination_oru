package se.oru.coordination.coordination_oru.gui;

import java.util.List;

public class CoordinationData {
    private String priorityRule;
    private String trafficControlStrategy;
    private String triggerVehicle;
    private List<Integer> missionTriggers; // Change to List<Integer>
    private List<String> vehiclesToComply;
    private double velocityChangeRatio;

    public String getPriorityRule() {
        return priorityRule;
    }

    public void setPriorityRule(String priorityRule) {
        this.priorityRule = priorityRule;
    }

    public String getTrafficControlStrategy() {
        return trafficControlStrategy;
    }

    public void setTrafficControlStrategy(String trafficControlStrategy) {
        this.trafficControlStrategy = trafficControlStrategy;
    }

    public String getTriggerVehicle() {
        return triggerVehicle;
    }

    public void setTriggerVehicle(String triggerVehicle) {
        this.triggerVehicle = triggerVehicle;
    }

    public List<Integer> getMissionTriggers() { // Change to List<Integer>
        return missionTriggers;
    }

    public void setMissionTriggers(List<Integer> missionTriggers) { // Change to List<Integer>
        this.missionTriggers = missionTriggers;
    }

    public List<String> getVehiclesToComply() {
        return vehiclesToComply;
    }

    public void setVehiclesToComply(List<String> vehiclesToComply) {
        this.vehiclesToComply = vehiclesToComply;
    }

    public double getVelocityChangeRatio() {
        return velocityChangeRatio;
    }

    public void setVelocityChangeRatio(double velocityChangeRatio) {
        this.velocityChangeRatio = velocityChangeRatio;
    }
}
