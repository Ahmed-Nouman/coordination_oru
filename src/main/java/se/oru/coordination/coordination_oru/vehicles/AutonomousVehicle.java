package se.oru.coordination.coordination_oru.vehicles;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.util.Missions;
import se.oru.coordination.coordination_oru.util.NoPathFoundError;

import java.awt.*;
import java.io.File;

public class AutonomousVehicle extends AbstractVehicle {
    public static ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;

    public AutonomousVehicle(int id, int priorityID, Color color, double maxVelocity, double maxAcceleration, double length, double width) {
        super(id, priorityID, color, maxVelocity, maxAcceleration, length, width);
    }

    public AutonomousVehicle(int priorityID, Color color, double maxVelocity, double maxAcceleration, double length, double width) {
        super(vehicleNumber, priorityID, color, maxVelocity, maxAcceleration, length, width);
    }

    public AutonomousVehicle() {
        super(vehicleNumber, 1, Color.YELLOW, 5, 2, 0.5, 0.5);
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
        getPlan(initial, goals, map, inversePath, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect, 0.01, 60, 0.01, 0.1);
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

        var rsp = configureReedsSheppCarPlanner(planningAlgorithm, map, radius, planningTime, turningRadius, distanceBetweenPathPoints);
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

    private static String poseToString(Pose pose) {
        return round(pose.getX()) + "," + round(pose.getY()) + "," + round(pose.getTheta());
    }
}
