package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.awt.*;

public class AutonomousVehicle extends AbstractVehicle {

    public AutonomousVehicle(int ID, String name, int priorityID, Color color, double maxVelocity, double maxAcceleration,
                             double length, double width, Pose initialPose, double safetyDistance, int missionRepetition) {
        super(ID, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition);
    }

    public AutonomousVehicle(String name, int priorityID, Color color, double maxVelocity, double maxAcceleration, double length,
                             double width, Pose initialPose, double safetyDistance, int missionRepetition) {
        this(vehicleNumber, name, priorityID, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                missionRepetition);
    }

}
