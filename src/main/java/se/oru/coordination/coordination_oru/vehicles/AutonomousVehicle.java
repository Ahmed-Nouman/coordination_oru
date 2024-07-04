package se.oru.coordination.coordination_oru.vehicles;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;

import java.awt.*;

public class AutonomousVehicle extends AbstractVehicle {

    public AutonomousVehicle(int ID, String name, int priority, Color color, double maxVelocity, double maxAcceleration,
                             double length, double width, Pose initialPose, double safetyDistance, int goalRepetition, ForwardModel model) {
        super(ID, name, priority, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                goalRepetition, model);
    }

    public AutonomousVehicle(String name, int priority, Color color, double maxVelocity, double maxAcceleration, double length,
                             double width, Pose initialPose, double safetyDistance, int goalRepetition, ForwardModel model) {
        this(vehicleNumber, name, priority, color, maxVelocity, maxAcceleration, length, width, initialPose, safetyDistance,
                goalRepetition, model);
    }

}
