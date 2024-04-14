package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RobotReportWriter {
    /**
     * Writes robot reports to the specified directory based on the provided conditions.
     * Robot reports are generated at an interval and for a duration defined by the user.
     * The method also incorporates the heuristic information used for the simulation.
     * The reported values, including the robot's pose and speed, can be scaled using the {@code scaleAdjustment} parameter.
     *
     * @param tec                  The {@link TrajectoryEnvelopeCoordinator} managing the missions.
     * @param intervalInSeconds    The time interval (in seconds) between consecutive report generations.
     * @param terminationInMinutes The maximum duration (in minutes) for which reports should be written.
     * @param heuristicName        The name of the heuristic used during the simulation.
     * @param resultDirectory      The directory path where the robot reports will be saved.
     * @param scaleAdjustment      A scaling factor to adjust the reported values of robot pose and speed.
     */
    public static void writeReports(TrajectoryEnvelopeCoordinator tec,
                                    double intervalInSeconds, int terminationInMinutes,
                                    String heuristicName, String resultDirectory,
                                    double scaleAdjustment) {
        double updatedLookAheadDistance = 0.0;
        double lookAheadDistance = 0.0;

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
        System.out.println("Writing robot reports.");
        double distance = lookAheadDistance > 0 ? lookAheadDistance : updatedLookAheadDistance;

        String filePath = createFile(distance * scaleAdjustment, heuristicName, resultDirectory);
        var reportCollector = new RobotReportCollector();
        reportCollector.handleRobotReports(tec, filePath, (long) (Missions.SECOND_TO_MILLISECOND * intervalInSeconds),
                terminationInMinutes, scaleAdjustment);
    }

    /**
     * Constructs a file path for saving robot reports. The method also creates the directory
     * if it doesn't exist. The filename is derived from the count of autonomous and lookahead robots,
     * the heuristic applied, and the lookahead distance.
     *
     * @param lookAheadDistance The distance for which the robot plans its path ahead of its current position.
     *                          Should be a positive number.
     * @param heuristicName     The heuristic's name used in the simulation. Expected to be non-null.
     * @param resultDirectory   The directory path where the robot report files will be saved.
     *                          If the path doesn't exist, the method attempts to create it.
     * @return The full path, including directory and filename, where the report will be saved.
     */

    public static String createFile(double lookAheadDistance, String heuristicName,
                                    String resultDirectory) {

        var directoryPath = Paths.get(resultDirectory);

        try {
            Files.createDirectories(directoryPath);
            System.out.println("Directory created successfully.");
        } catch (IOException e) {
            System.err.println("Error while creating directory: " + e.getMessage());
        }

        int autonomousRobotCount = 0;
        int lookAheadRobotCount = 0;

        for (AbstractVehicle robot : VehiclesHashMap.getList().values()) {
            if (robot instanceof AutonomousVehicle) {
                autonomousRobotCount++;
            } else if (robot instanceof LookAheadVehicle) {
                lookAheadRobotCount++;
            }
        }

        String fileName = heuristicName.charAt(0) + "_" + "S" + "_" + VehiclesHashMap.getVehicle(1).getSafetyDistance() + "_"
                + "V" + "_" + VehiclesHashMap.getVehicle(1).getMaxVelocity();

        return resultDirectory + "/" + fileName;
    }
}
