package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

public class WriteReports {
    /**
     * Writes robot reports to the specified directory based on the provided conditions.
     * Robot reports are generated at an interval and for a duration defined by the user.
     * The method also incorporates the heuristic information used for the simulation.
     * The reported values, including the robot's pose and speed, can be scaled using the {@code scaleAdjustment} parameter.
     *
     * @param tec                  The {@link TrajectoryEnvelopeCoordinator} managing the missions.
     * @param writeReports         If {@code true}, the method will write reports, otherwise, it won't.
     * @param intervalInSeconds    The time interval (in seconds) between consecutive report generations.
     * @param terminationInMinutes The maximum duration (in minutes) for which reports should be written.
     * @param heuristicName        The name of the heuristic used during the simulation.
     * @param resultDirectory      The directory path where the robot reports will be saved.
     * @param scaleAdjustment      A scaling factor to adjust the reported values of robot pose and speed.
     */
    public static void writeReports(TrajectoryEnvelopeCoordinator tec,
                                    boolean writeReports, double intervalInSeconds, int terminationInMinutes,
                                    String heuristicName, String resultDirectory,
                                    double scaleAdjustment) {
        double updatedLookAheadDistance = 0.0;
        double lookAheadDistance = 0.0;

        // For fully predictable path of the robot
        for (int robotID : Missions.convertSetToIntArray(tec.getAllRobotIDs())) {
            var robot = VehiclesHashMap.getVehicle(robotID);
            if (robot instanceof LookAheadVehicle) {
                var lookAheadRobot = (LookAheadVehicle) robot;
                lookAheadDistance = lookAheadRobot.getLookAheadDistance();
                if (lookAheadDistance < 0) {
                    updatedLookAheadDistance = lookAheadRobot.getPlanLength();
                }
            }
        }

        // Write robot reports to ../results/... folder in .csv format
        if (writeReports) {
            System.out.println("Writing robot reports.");
            double distance = lookAheadDistance > 0 ? lookAheadDistance : updatedLookAheadDistance;

            String filePath = Missions.createFile(distance * scaleAdjustment, heuristicName, resultDirectory);
            var reportCollector = new RobotReportCollector();
            reportCollector.handleRobotReports(tec, filePath, (long) (1000 * intervalInSeconds),
                    terminationInMinutes, scaleAdjustment);
        } else {
            System.out.println("Not writing robot reports.");
        }
    }
}
