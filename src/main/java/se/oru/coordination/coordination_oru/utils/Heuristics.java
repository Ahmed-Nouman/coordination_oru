package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.CriticalSection;
import se.oru.coordination.coordination_oru.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.RobotReport;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static se.oru.coordination.coordination_oru.vehicles.AbstractVehicle.calculateFootprintArea;

/**
 * This class provides various heuristics for determining the order in which robots move through critical sections.
 *
 * @author anm
 */
public class Heuristics {

    private String heuristicName;

    public static List<String> getAllHeuristicNames() {
        List<String> heuristicNames = new ArrayList<>();
        heuristicNames.add("AUTONOMOUS_FIRST");
        heuristicNames.add("BIGGER_FOOTPRINT_FIRST");
        heuristicNames.add("CLOSEST_FIRST");
        heuristicNames.add("HUMAN_FIRST");
        heuristicNames.add("HIGHEST_PRIORITY_FIRST");
        heuristicNames.add("MOST_DISTANCE_TO_TRAVEL");
        heuristicNames.add("MOST_DISTANCE_TRAVELLED");
        heuristicNames.add("RANDOM");
        return heuristicNames;
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
        Random random = new Random();

        return (_robot1, _robot2) -> {
            switch (random.nextInt(3)) { // This will give either 0, 1, or 2
                case 0:
                    return 0; // Indicates equality
                case 1:
                    return 1; // Indicates robot1 is 'greater than' robot2
                default:
                    return -1; // Indicates robot1 is 'less than' robot2
            }
        };
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
            return (int) Math.signum(priority1 - priority2);
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
                return -1; // robot1 is a look-ahead robot and robot2 is not, so robot1 should go first
            } else if (!isO1LookAhead && isO2LookAhead) {
                return 1; // robot2 is a look-ahead robot and robot1 is not, so robot2 should go first
            } else {
                return 0; // both or neither are look-ahead robots, so they are considered equal
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
            boolean isO1Autonomous = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getClass().getSimpleName().equals("Autonomous");
            boolean isO2Autonomous = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getClass().getSimpleName().equals("Autonomous");

            if (isO1Autonomous && !isO2Autonomous) {
                return -1; // robot1 is an autonomous robot and robot2 is not, so robot1 should go first
            } else if (!isO1Autonomous && isO2Autonomous) {
                return 1; // robot2 is an autonomous robot and robot1 is not, so robot2 should go first
            } else {
                return 0; // both or neither are look-ahead robots, so they are considered equal
            }
        };
    }

    public Comparator<RobotAtCriticalSection> largerFootprintFirst() {
        heuristicName = "BIGGER_FOOTPRINT_FIRST";
        return (robot1, robot2) -> {
            double length1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getLength();
            double length2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getLength();
            double width1 = VehiclesHashMap.getVehicle(robot1.getRobotReport().getRobotID()).getWidth();
            double width2 = VehiclesHashMap.getVehicle(robot2.getRobotReport().getRobotID()).getWidth();
            double footprintArea1 = calculateFootprintArea(length1, width1);
            double footprintArea2 = calculateFootprintArea(length2, width2);

            return Double.compare(footprintArea2, footprintArea1); // Note the order: robot2 - robot1
        };
    }


    public String getName() {
        return heuristicName;
    }

}
