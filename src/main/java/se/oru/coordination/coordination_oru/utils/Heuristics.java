package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.Comparator;

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
            case MOST_DISTANCE_TRAVELLED:
                return mostDistanceTravelled();
            case MOST_DISTANCE_TO_TRAVEL:
                return mostDistanceToTravel();
            case RANDOM:
                return random();
            case HIGHEST_PRIORITY_FIRST:
                return highestPriorityFirst();
            case HIGHEST_PRIORITY_AND_CLOSEST_FIRST:
                return highestPriorityAndClosestFirst();
            case HUMAN_FIRST:
                return humanFirst();
            case HUMAN_AND_CLOSEST_FIRST:
                return humanAndClosestFirst();
            case AUTONOMOUS_FIRST:
                return autonomousFirst();
            case AUTONOMOUS_AND_CLOSEST_FIRST:
                return autonomousAndClosestFirst();
            case BIGGER_VEHICLE_FIRST:
                return biggerVehicleFirst();
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

    /**
     * Returns a comparator for determining the order based on the robot closest to a critical section.
     *
     * @return The comparator for the closest heuristic.
     */
    public Comparator<RobotAtCriticalSection> closestFirst() {
        heuristicName = "CLOSEST_FIRST";
        return (robot1, robot2) -> {
            CriticalSection criticalSection = robot1.getCriticalSection();
            RobotReport robotReport1 = robot1.getRobotReport();
            RobotReport robotReport2 = robot2.getRobotReport();
            return ((criticalSection.getTrajectoryEnvelopeStart1() - robotReport1.getPathIndex()) - (criticalSection.getTrajectoryEnvelopeStart2() - robotReport2.getPathIndex()));
        };
    }

    /**
     * Returns a comparator for determining the order based on the robot with more distance traveled so far.
     *
     * @return The comparator for mostDistanceTravelled heuristic.
     */
    public Comparator<RobotAtCriticalSection> mostDistanceTravelled() {
        heuristicName = "MOST_DISTANCE_TRAVELLED";
        return (robot1, robot2) -> (int) Math.signum(robot1.getRobotReport().getDistanceTraveled() - robot2.getRobotReport().getDistanceTraveled());
    }

    /**
     * Returns a comparator for determining the order based on the robot with more distance to travel.
     *
     * @return The comparator for mostDistanceToTravel heuristic.
     */
    public Comparator<RobotAtCriticalSection> mostDistanceToTravel() {
        heuristicName = "MOST_DISTANCE_TO_TRAVEL";
        return (robot1, robot2) -> {
            double distanceToTravel1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getPathLength() - robot1.getRobotReport().getDistanceTraveled();
            double distanceToTravel2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getPathLength() - robot2.getRobotReport().getDistanceTraveled();
            return (int) Math.signum(distanceToTravel1 - distanceToTravel2);
        };
    }

    /**
     * Returns a comparator for determining the order randomly.
     *
     * @return The comparator for random heuristic.
     */
    /**
     * Returns a comparator for determining the order randomly.
     *
     * @return The comparator for random heuristic.
     */
    public Comparator<RobotAtCriticalSection> random() {
        heuristicName = "RANDOM";
        return (_robot1, _robot2) -> 0;
    }

    /**
     * Returns a comparator for determining the order based on their priorities.
     * Priority is set in ascending order, so the highest priority is the lowest number.
     *
     * @return The comparator for highest priority heuristic.
     */
    public Comparator<RobotAtCriticalSection> highestPriorityFirst() {
        heuristicName = "HIGHEST_PRIORITY_FIRST";
        return (robot1, robot2) -> {
            int priority1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getPriority();
            int priority2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getPriority();
            return (int) Math.signum(priority2 - priority1);
        };
    }

    public Comparator<RobotAtCriticalSection> highestPriorityAndClosestFirst() {
        heuristicName = "HIGHEST_PRIORITY_AND_CLOSEST_FIRST";
        return (robot1, robot2) -> {
            int priority1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getPriority();
            int priority2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getPriority();
            if (priority1 == priority2) return closestFirst().compare(robot1, robot2);
            return (int) Math.signum(priority2 - priority1);
        };
    }

    /**
     * Returns a comparator for determining the order based on whether the robot is a look-ahead robot.
     * Humans are given priority over other robots.
     * If two robots are both look-ahead or both not look-ahead, they are considered equal.
     *
     * @return The comparator for humanFirst heuristic.
     */
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
                return 0;
            }
        };
    }

    public Comparator<RobotAtCriticalSection> humanAndClosestFirst() {
        heuristicName = "HUMAN_AND_CLOSEST_FIRST";
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

    /**
     * Returns a comparator for determining the order based on whether the robot is an autonomous robot.
     * Autonomous robots are given priority over other robots.
     * If two robots are both look-ahead or both not look-ahead, they are considered equal.
     *
     * @return The comparator for autonomous heuristic.
     */
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
                return 0;
            }
        };
    }

    public Comparator<RobotAtCriticalSection> autonomousAndClosestFirst() {
        heuristicName = "AUTONOMOUS_AND_CLOSEST_FIRST";
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
        MOST_DISTANCE_TRAVELLED,
        MOST_DISTANCE_TO_TRAVEL,
        RANDOM,
        HIGHEST_PRIORITY_FIRST,
        HIGHEST_PRIORITY_AND_CLOSEST_FIRST,
        HUMAN_FIRST,
        HUMAN_AND_CLOSEST_FIRST,
        AUTONOMOUS_FIRST,
        AUTONOMOUS_AND_CLOSEST_FIRST,
        BIGGER_VEHICLE_FIRST
    }

}
