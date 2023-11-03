package se.oru.coordination.coordination_oru.vehicles;

import org.apache.commons.lang.ArrayUtils;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;

import java.awt.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AutonomousVehicle extends AbstractVehicle {
    public static ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;

    private Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> planSegmentsMap;

    public AutonomousVehicle(int id, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                             double length, double width, Pose initialPose, Pose[] goalPoses, double safetyDistance) {
        super(id, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose,
                goalPoses, safetyDistance);
    }

    public AutonomousVehicle(int priorityID, Color color, double maxVelocity, double maxAcceleration, double length,
                             double width, Pose initialPose, Pose[] goalPoses, double safetyDistance) {
        super(vehicleNumber, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose,
                goalPoses, safetyDistance);
    }

    public AutonomousVehicle(Pose initialPose, Pose[] goalPoses) {
        super(vehicleNumber, 1, Color.YELLOW, 1.0, 0.1, 0.5, 0.5,
                initialPose, goalPoses, 0);
    }

    /**
     * Generates a path for the robot using the default planning algorithm and parameters.
     *
     * @param initial     The initial pose of the robot.
     * @param goals       An array of goal poses.
     * @param map         The map used for planning.
     * @param inversePath A flag indicating whether the inverse path should also be computed.
     */
    @Override
    public void getPlan(Pose initial, Pose[] goals, String map, Boolean inversePath) {
        getPlan(initial, goals, map, inversePath, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect, 0.01,
                60, 0.01, 0.1);
    }

    /**
     * Generates a path for the robot using the specified planning algorithm and parameters.
     *
     * @param initial                   The initial pose of the robot.
     * @param goals                     An array of goal poses.
     * @param map                       The map used for planning.
     * @param inversePath               A flag indicating whether the inverse path should also be computed.
     * @param planningAlgorithm         The planning algorithm to be used.
     * @param radius                    The radius used for planning.
     * @param planningTime              The maximum planning time in seconds.
     * @param turningRadius             The turning radius of the robot.
     * @param distanceBetweenPathPoints The distance between path points in the generated path.
     */
    public void getPlan(Pose initial, Pose[] goals, String map, Boolean inversePath, ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm,
                        double radius, double planningTime, double turningRadius, double distanceBetweenPathPoints) {

        var rsp = configureReedsSheppCarPlanner(planningAlgorithm, map, radius, planningTime, turningRadius,
                distanceBetweenPathPoints);
        generatePath(rsp, initial, goals, inversePath);
    }

    /**
     * Configures a ReedsSheppCarPlanner instance with the specified parameters.
     *
     * @param planningAlgorithm         The planning algorithm to be used.
     * @param map                       The map used for planning.
     * @param radius                    The radius used for planning.
     * @param planningTime              The maximum planning time in seconds.
     * @param turningRadius             The turning radius of the robot.
     * @param distanceBetweenPathPoints The distance between path points in the generated path.
     * @return A configured ReedsSheppCarPlanner instance.
     */
    private ReedsSheppCarPlanner configureReedsSheppCarPlanner(ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm, String map, double radius,
                                                               double planningTime, double turningRadius, double distanceBetweenPathPoints) {
        var rsp = new ReedsSheppCarPlanner(planningAlgorithm);
        rsp.setMap(map);
        rsp.setRadius(radius);
        rsp.setPlanningTimeInSecs(planningTime);
        rsp.setFootprint(getFootPrint());
        rsp.setTurningRadius(turningRadius);
        rsp.setDistanceBetweenPathPoints(distanceBetweenPathPoints);
        return rsp;
    }

    /**
     * Generates a path for the robot using the provided ReedsSheppCarPlanner instance.
     *
     * @param rsp         The ReedsSheppCarPlanner instance used for planning.
     * @param initial     The initial pose of the robot.
     * @param goals       An array of goal poses.
     * @param inversePath A flag indicating whether the inverse path should also be computed.
     */
    private void generatePath(ReedsSheppCarPlanner rsp, Pose initial, Pose[] goals, Boolean inversePath) {
        rsp.setStart(initial);
        rsp.setGoals(goals);
        rsp.plan();

        if (rsp.getPath() == null) {
            throw new Error("No path found.");
        }

        var pathFwd = rsp.getPath();
        var path = inversePath ? (PoseSteering[]) ArrayUtils.addAll(pathFwd, rsp.getPathInv()) : pathFwd;
        VehiclesHashMap.getVehicle(getID()).setPath(path);
    }

    /**
     * Generates a path for the robot using the provided ReedsSheppCarPlanner instance.
     *
     * @param rsp         The ReedsSheppCarPlanner instance used for planning.
     * @param initial     The initial pose of the robot.
     * @param goal       The goal pose of the robot.
     */
    private PoseSteering[] generatePath(ReedsSheppCarPlanner rsp, Pose initial, Pose goal) {
        rsp.setStart(initial);
        rsp.setGoals(goal);
        rsp.plan();
        var path = rsp.getPath();

        if (rsp.getPath() == null) {
            throw new Error("No path found.");
        }

        return path;
    }

    /**
     * Represents a segment plan containing a path plan and waiting time.
     */
    private static class SegmentPlan {

        public PoseSteering[] path;
        public int waitingTime;
        public SegmentPlan(PoseSteering[] path, int waitingTime) {
            this.path = path;
            this.waitingTime = waitingTime;
        }

    }
    /**
     * Plans paths between segments of poses defined by goalPoses.
     *
     * @param initialPose  The initial pose.
     * @param goalPoses    The array of goal poses.
     * @param waitingTime  Waiting time at each goal in seconds.
     * @return A HashMap containing planned paths and waiting times for path segments.
     */
    public Map<Integer, SegmentPlan> getPlanSegments(Pose initialPose, Pose[] goalPoses, int waitingTime, String map,
                                                     ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm,
                                                     double radius, double planningTime, double turningRadius,
                                                     double distanceBetweenPathPoints) {
        Map<Integer, SegmentPlan> planSegments = new HashMap<>();
        var rsp = configureReedsSheppCarPlanner(planningAlgorithm, map, radius, planningTime, turningRadius,
                distanceBetweenPathPoints);

        Pose start = initialPose;
        for (int i = 0; i < goalPoses.length; i++) {
            Pose goal = goalPoses[i];

            // We use the existing generatePath method to obtain the plan for the segment
            PoseSteering[] segmentPath = generatePath(rsp, start, goal);

            // Storing the segment plan in the map
            planSegments.put(i, new SegmentPlan(segmentPath, waitingTime));

            // Updating start for next segment
            start = goal;
        }

        return planSegments;
    }

    /**
     * Plans paths between segments of poses defined by goalPoses.
     *
     * @param initialPose  The initial pose.
     * @param goalPoses    The array of goal poses.
     * @param waitingTimesArray Waiting time at each goal in seconds except for the last goal pose.
     */
    public void getPlanSegments(Pose initialPose, Pose[] goalPoses, int[] waitingTimesArray, String map) {
        if (goalPoses.length - 1 != waitingTimesArray.length) {
            throw new IllegalArgumentException("The length of waitingTimesArray must be one less than the length of goalPoses");
        }

        Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> planSegments = new HashMap<>();
        var rsp = configureReedsSheppCarPlanner(planningAlgorithm, map, 0.1, 60, 0.01, 0.1);

        Pose start = initialPose;
        for (int i = 0; i < goalPoses.length; i++) {
            Pose goal = goalPoses[i];

            // We use the existing generatePath method to obtain the plan for the segment
            PoseSteering[] segmentPath = generatePath(rsp, start, goal);

            // Storing the segment plan in the map
            int waitingTime = (i < waitingTimesArray.length) ? waitingTimesArray[i] : 0;
            planSegments.put(i, new AbstractMap.SimpleEntry<>(segmentPath, waitingTime));

            // Updating start for next segment
            start = goal;
        }
        planSegmentsMap = planSegments;
    }


    private static String poseToString(Pose pose) {
        return round(pose.getX()) + "," + round(pose.getY()) + "," + round(pose.getTheta());
    }

    public Map<Integer, AbstractMap.SimpleEntry<PoseSteering[], Integer>> getPlanSegmentsMap() {
        return planSegmentsMap;
    }
}
