package se.oru.coordination.coordination_oru.motionplanning;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;

public interface VehiclePlanner {
    PoseSteering[] plan(String map, Coordinate[] footprint, Pose initialPose, Pose[] goalPoses);
    PoseSteering[] plan(String map, Coordinate[] footprint, Pose initialPose, Pose[] goalPoses, ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm,
                        double radius, double planningTimeInSecs, double turningRadius, double distanceBetweenPathPoints);
}