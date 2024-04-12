package se.oru.coordination.coordination_oru.utils;

public interface MissionDispatchingCallback {
	
	void beforeMissionDispatch(Mission m);
	
	void afterMissionDispatch(Mission m);
	
}
