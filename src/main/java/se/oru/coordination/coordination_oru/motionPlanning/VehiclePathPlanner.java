package se.oru.coordination.coordination_oru.motionPlanning;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;

import java.util.Arrays;

public class VehiclePathPlanner implements PathPlanner {

    private final String map;
    private final ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm;
    private final double radius;
    private final double planningTimeInSecs;
    private final double turningRadius;
    private final double distanceBetweenPathPoints;

    public VehiclePathPlanner(String map, ReedsSheppCarPlanner.PLANNING_ALGORITHM planningAlgorithm,
                              double radius, double planningTimeInSecs, double turningRadius,
                              double distanceBetweenPathPoints) {
        this.map = map;
        this.planningAlgorithm = planningAlgorithm;
        this.radius = radius;
        this.planningTimeInSecs = planningTimeInSecs;
        this.turningRadius = turningRadius;
        this.distanceBetweenPathPoints = distanceBetweenPathPoints;
    }

    @Override
    public PoseSteering[] plan(Coordinate[] footprint, Pose initialPose, Pose[] goalPoses) {
        var planner = new ReedsSheppCarPlanner();
        planner.setMap(this.map);
        planner.setFootprint(footprint);
        planner.setStart(initialPose);
        planner.setGoals(goalPoses);
        planner.setPlanningAlgorithm(this.planningAlgorithm);
        planner.setRadius(this.radius);
        planner.setPlanningTimeInSecs(this.planningTimeInSecs);
        planner.setTurningRadius(this.turningRadius);
        planner.setDistanceBetweenPathPoints(this.distanceBetweenPathPoints);

        try {
            planner.plan();
            return planner.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Planning completed for the vehicle: " + initialPose + " to the goal: " + java.util.Arrays.toString(goalPoses));
        }
    }

}
