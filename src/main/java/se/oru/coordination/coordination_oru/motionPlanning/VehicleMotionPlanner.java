package se.oru.coordination.coordination_oru.motionPlanning;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;

import java.util.Arrays;

public class VehicleMotionPlanner implements VehiclePlanner {
    @Override
    public PoseSteering[] plan(String map, Coordinate[] footprint, Pose initialPose, Pose[] goalPoses) {
        return plan(map, footprint, initialPose, goalPoses, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60.0, 2.0, 0.1);
    }

    @Override
    public PoseSteering[] plan(String map, Coordinate[] footprint, Pose initialPose, Pose[] goalPoses,
                               ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm, double radius,
                               double planningTimeInSecs, double turningRadius, double distanceBetweenPathPoints) {

        var planner = new ReedsSheppCarPlanner();
        planner.setMap(map);
        planner.setFootprint(footprint);
        planner.setStart(initialPose);
        planner.setGoals(goalPoses);
        planner.setPlanningAlgorithm(planningAlgorithm);
        planner.setRadius(radius);
        planner.setPlanningTimeInSecs(planningTimeInSecs);
        planner.setTurningRadius(turningRadius);
        planner.setDistanceBetweenPathPoints(distanceBetweenPathPoints);

        try {
            planner.plan();
            return planner.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Plan not found! for the vehicle: " + initialPose + " to the goal: " + Arrays.toString(goalPoses));
        }
    }
}
