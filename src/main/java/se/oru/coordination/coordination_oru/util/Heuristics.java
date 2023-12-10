package se.oru.coordination.coordination_oru.util;

import se.oru.coordination.coordination_oru.CriticalSection;
import se.oru.coordination.coordination_oru.RobotAtCriticalSection;
import se.oru.coordination.coordination_oru.RobotReport;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class provides various heuristics for determining the order in which robots move through critical sections.
 *
 * @author anm
 */
public class Heuristics {

    private String heuristicName;

    public static List<String> getAllHeuristicNames() {
        List<String> heuristicNames = new ArrayList<>();
        heuristicNames.add("CLOSEST");
        heuristicNames.add("MOST_DISTANCE_TO_TRAVEL");
        heuristicNames.add("LOWEST_ID");
        heuristicNames.add("HIGHEST_ID");
        heuristicNames.add("LOOK_AHEAD_FIRST");
        heuristicNames.add("AUTONOMOUS_FIRST");
        return heuristicNames;
    }

    /**
     * Returns a comparator for determining the order based on the robot closest to a critical section.
     *
     * @return The comparator for the closest heuristic.
     */
    public Comparator<RobotAtCriticalSection> closest() {
        heuristicName = "CLOSEST";
        return (o1, o2) -> {
            CriticalSection cs = o1.getCriticalSection();
            RobotReport robotReport1 = o1.getRobotReport();
            RobotReport robotReport2 = o2.getRobotReport();
            return ((cs.getTe1Start() - robotReport1.getPathIndex()) - (cs.getTe2Start() - robotReport2.getPathIndex()));
        };
    }

    /**
     * Returns a comparator for determining the order based on the robot with more distance traveled.
     *
     * @return The comparator for mostDistanceToTravel heuristic.
     */
    public Comparator<RobotAtCriticalSection> mostDistanceToTravel() {
        heuristicName = "MOST_DISTANCE_TO_TRAVEL";
        return (o1, o2) -> (int) Math.signum(o1.getRobotReport().getDistanceTraveled() - o2.getRobotReport().getDistanceTraveled());
    }

    /**
     * Returns a comparator for determining the order based on the robot with the lowest ID number.
     *
     * @return The comparator for lowestIDNumber heuristic.
     */
    public Comparator<RobotAtCriticalSection> lowestIDNumber() {
        heuristicName = "LOWEST_ID";
        return (o1, o2) -> o1.getRobotReport().getRobotID() - o2.getRobotReport().getRobotID();
    }

    /**
     * Returns a comparator for determining the order based on the robot with the highest ID number.
     *
     * @return The comparator for highestIDNumber heuristic.
     */
    public Comparator<RobotAtCriticalSection> highestIDNumber() {
        heuristicName = "HIGHEST_ID";
        return (o1, o2) -> o2.getRobotReport().getRobotID() - o1.getRobotReport().getRobotID();
    }

    /**
     * Returns a comparator for determining the order based on whether the robot is a look-ahead robot.
     * Look-ahead robots are given priority over other robots.
     * If two robots are both look-ahead or both not look-ahead, they are considered equal.
     *
     * @return The comparator for lookAheadRobot heuristic.
     */
    public Comparator<RobotAtCriticalSection> lookAheadFirst() {
        heuristicName = "LOOK_AHEAD_FIRST";
        return (o1, o2) -> {
            boolean isO1LookAhead = VehiclesHashMap.getVehicle(o1.getRobotReport().getRobotID()).getClass().getSimpleName().equals("LookAheadVehicle");
            boolean isO2LookAhead = VehiclesHashMap.getVehicle(o2.getRobotReport().getRobotID()).getClass().getSimpleName().equals("LookAheadVehicle");

            if (isO1LookAhead && !isO2LookAhead) {
                return -1; // o1 is a look-ahead robot and o2 is not, so o1 should go first
            } else if (!isO1LookAhead && isO2LookAhead) {
                return 1; // o2 is a look-ahead robot and o1 is not, so o2 should go first
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
        return (o1, o2) -> {
            boolean isO1Autonomous = VehiclesHashMap.getVehicle(o1.getRobotReport().getRobotID()).getClass().getSimpleName().equals("Autonomous");
            boolean isO2Autonomous = VehiclesHashMap.getVehicle(o2.getRobotReport().getRobotID()).getClass().getSimpleName().equals("Autonomous");

            if (isO1Autonomous && !isO2Autonomous) {
                return -1; // o1 is an autonomous robot and o2 is not, so o1 should go first
            } else if (!isO1Autonomous && isO2Autonomous) {
                return 1; // o2 is an autonomous robot and o1 is not, so o2 should go first
            } else {
                return 0; // both or neither are look-ahead robots, so they are considered equal
            }
        };
    }

    public String getName() {
        return heuristicName;
    }

}
