package se.oru.coordination.coordination_oru.motionplanning;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;

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

        planner.plan();
        var path = planner.getPath();

        if (path == null) {
            System.err.println("No path found.");
            System.exit(1); //TODO: Handle this better and throw an exception, not exiting the program
        }

        return path;
    }
}
