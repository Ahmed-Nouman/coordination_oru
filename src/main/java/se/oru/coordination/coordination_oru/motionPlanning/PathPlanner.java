package se.oru.coordination.coordination_oru.motionPlanning;

import com.vividsolutions.jts.geom.Coordinate;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;

public interface PathPlanner {
    PoseSteering[] plan(Coordinate[] footprint, Pose initialPose, Pose[] goalPoses);
}