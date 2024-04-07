package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;

public interface VehiclePlanner {
    void configure(String map, double radius, double planningTime, double turningRadius, double distanceBetweenPathPoints);
    PoseSteering[] planPath(Pose initialPose, Pose[] goalPoses, boolean inversePath);
}
