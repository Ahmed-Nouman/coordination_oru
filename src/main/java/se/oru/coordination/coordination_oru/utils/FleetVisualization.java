package se.oru.coordination.coordination_oru.utils;

import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import se.oru.coordination.coordination_oru.dataStructue.RobotReport;

public interface FleetVisualization {
	
	void displayRobotState(TrajectoryEnvelope te, RobotReport rr, String... extraStatusInfo);

	void displayDependency(RobotReport rrWaiting, RobotReport rrDriving, String dependencyDescriptor);
	
	void addEnvelope(TrajectoryEnvelope te);
	
	void removeEnvelope(TrajectoryEnvelope te);
	
	void updateVisualization();
	
	void setMap(String yamlFile);
	
	int periodicEnvelopeRefreshInMillis();

}
