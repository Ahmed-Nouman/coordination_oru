package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import se.oru.coordination.coordination_oru.motionplanning.ompl.ReedsSheppCarPlanner;

public class ReedsSheppVehiclePlanner implements VehiclePlanner {

    private ReedsSheppCarPlanner reedsSheppCarPlanner;
    private String map;
    private double radius;
    private double planningTime;
    private double turningRadius;
    private double distanceBetweenPathPoints;

    @Override
    public void configure(String map, double radius, double planningTime, double turningRadius, double distanceBetweenPathPoints) {
        this.map = map;
        this.radius = radius;
        this.planningTime = planningTime;
        this.turningRadius = turningRadius;
        this.distanceBetweenPathPoints = distanceBetweenPathPoints;
        this.reedsSheppCarPlanner = new ReedsSheppCarPlanner();
    }

    @Override
    public PoseSteering[] planPath(Pose initialPose, Pose[] goalPoses, boolean inversePath) {
        reedsSheppCarPlanner.setStart(initialPose);
        reedsSheppCarPlanner.setGoals(goalPoses);
        reedsSheppCarPlanner.plan();
        return reedsSheppCarPlanner.getPath();
    }
}
