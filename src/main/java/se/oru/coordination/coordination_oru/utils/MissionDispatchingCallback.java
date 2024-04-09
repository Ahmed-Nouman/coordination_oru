package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.dataStructue.Mission;

public interface MissionDispatchingCallback {
	
	void beforeMissionDispatch(Mission m);
	
	void afterMissionDispatch(Mission m);
	
}
