package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static se.oru.coordination.coordination_oru.vehicles.AbstractVehicle.calculateFootprintArea;

/**
 * This class provides various heuristics for determining the order in which robots move through critical sections.
 *
 * @author anm
 */
public class Heuristics {

    private String heuristicName;
    private final HeuristicType heuristicType;

    public Heuristics() {
        this.heuristicType = HeuristicType.RANDOM;
    }

    public Heuristics(HeuristicType heuristicType) {
        this.heuristicType = heuristicType;
    }

    public Comparator<RobotAtCriticalSection> getComparator() {
        switch (heuristicType) {
            case CLOSEST_FIRST:
                return closestFirst();
            case MOST_DISTANCE_TRAVELLED_FIRST:
                return mostDistanceTravelled();
            case MOST_DISTANCE_TO_TRAVEL_FIRST:
                return mostDistanceToTravel();
            case RANDOM:
                return random();
            case HIGHEST_PRIORITY_FIRST:
                return highestPriorityFirst();
            case HUMAN_FIRST:
                return humanFirst();
            case AUTONOMOUS_FIRST:
                return autonomousFirst();
            case BIGGER_VEHICLE_FIRST:
                return biggerVehicleFirst();
            case TASK_PRIORITY_FIRST:
                return taskPriorityFirst();
            default:
                throw new IllegalArgumentException("Invalid heuristic type");
        }
    }

    public static ArrayList<String> getHeuristicNames() {
        var types = HeuristicType.values();
        var names = new ArrayList<String>();
        for (HeuristicType type : types) names.add(type.name());
        return names;
    }

    public Comparator<RobotAtCriticalSection> closestFirst() {
        heuristicName = "CLOSEST_FIRST";
        return (robot1, robot2) -> {
            CriticalSection criticalSection = robot1.getCriticalSection();
            RobotReport robotReport1 = robot1.getRobotReport();
            RobotReport robotReport2 = robot2.getRobotReport();
            return ((criticalSection.getTrajectoryEnvelopeStart1() - robotReport1.getPathIndex()) - (criticalSection.getTrajectoryEnvelopeStart2() - robotReport2.getPathIndex()));
        };
    }

    public Comparator<RobotAtCriticalSection> mostDistanceTravelled() {
        heuristicName = "MOST_DISTANCE_TRAVELLED";
        return (robot1, robot2) -> (int) Math.signum(robot1.getRobotReport().getDistanceTraveled() - robot2.getRobotReport().getDistanceTraveled());
    }

    public Comparator<RobotAtCriticalSection> mostDistanceToTravel() {
        heuristicName = "MOST_DISTANCE_TO_TRAVEL";
        return (robot1, robot2) -> {
            double distanceToTravel1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getPathLength() - robot1.getRobotReport().getDistanceTraveled();
            double distanceToTravel2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getPathLength() - robot2.getRobotReport().getDistanceTraveled();
            return (int) Math.signum(distanceToTravel1 - distanceToTravel2);
        };
    }

    public Comparator<RobotAtCriticalSection> random() {
        var random = new Random();
        return (robot1, robot2) -> random.nextInt(2) * 2 - 1;
    }

    public Comparator<RobotAtCriticalSection> highestPriorityFirst() {
        heuristicName = "HIGHEST_PRIORITY_FIRST";
        return (robot1, robot2) -> {
            int priority1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getPriority();
            int priority2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getPriority();
            if (priority1 == priority2) return closestFirst().compare(robot1, robot2);
            return (int) Math.signum(priority2 - priority1);
        };
    }

    private Comparator<RobotAtCriticalSection> taskPriorityFirst() {
        heuristicName = "TASK_PRIORITY_FIRST";
        return (robot1, robot2) -> {
            int currentMissionPriority1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getCurrentTaskIndex();
            int currentMissionPriority2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getCurrentTaskIndex();
            int priority1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getTasks().get(currentMissionPriority1).getPriority();
            int priority2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getTasks().get(currentMissionPriority2).getPriority();
            if (priority1 == priority2) return closestFirst().compare(robot1, robot2);
            else if (priority1 > priority2) return -1;
            else return 1;
        };
    }

    public Comparator<RobotAtCriticalSection> humanFirst() {
        heuristicName = "HUMAN_FIRST";
        return (robot1, robot2) -> {
            boolean isO1LookAhead = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getClass().getSimpleName().equals("LookAheadVehicle");
            boolean isO2LookAhead = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getClass().getSimpleName().equals("LookAheadVehicle");

            if (isO1LookAhead && !isO2LookAhead) {
                return -1;
            } else if (!isO1LookAhead && isO2LookAhead) {
                return 1;
            } else {
                return closestFirst().compare(robot1, robot2);
            }
        };
    }

    public Comparator<RobotAtCriticalSection> autonomousFirst() {
        heuristicName = "AUTONOMOUS_FIRST";
        return (robot1, robot2) -> {
            boolean isO1Autonomous = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getClass().getSimpleName().equals("AutonomousVehicle");
            boolean isO2Autonomous = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getClass().getSimpleName().equals("AutonomousVehicle");

            if (isO1Autonomous && !isO2Autonomous) {
                return -1;
            } else if (!isO1Autonomous && isO2Autonomous) {
                return 1;
            } else {
                return closestFirst().compare(robot1, robot2);
            }
        };
    }

    public Comparator<RobotAtCriticalSection> biggerVehicleFirst() {
        heuristicName = "BIGGER_VEHICLE_FIRST";
        return (robot1, robot2) -> {
            double length1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getLength();
            double length2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getLength();
            double width1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getWidth();
            double width2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getWidth();
            double footprintArea1 = calculateFootprintArea(length1, width1);
            double footprintArea2 = calculateFootprintArea(length2, width2);
            return Double.compare(footprintArea2, footprintArea1);
        };
    }

    public String getName() {
        return heuristicName;
    }

    public enum HeuristicType {
        CLOSEST_FIRST,
        MOST_DISTANCE_TRAVELLED_FIRST,
        MOST_DISTANCE_TO_TRAVEL_FIRST,
        RANDOM,
        HIGHEST_PRIORITY_FIRST,
        HUMAN_FIRST,
        AUTONOMOUS_FIRST,
        BIGGER_VEHICLE_FIRST,
        TASK_PRIORITY_FIRST;
    }

}
