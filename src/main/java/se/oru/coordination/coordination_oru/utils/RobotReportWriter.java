package se.oru.coordination.coordination_oru.utils;

import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinator;

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
     * @param folderName           The name of the folder where the robot reports will be saved.
     * @param scaleAdjustment      A scaling factor to adjust the reported values of robot pose and speed.
     */
    public static void writeReports(TrajectoryEnvelopeCoordinator tec,
                                    double intervalInSeconds, int terminationInMinutes,
                                    String heuristicName, String resultDirectory,
                                    String folderName, double scaleAdjustment) {

        System.out.println("Writing robot reports.");
        String filePath = createFile(heuristicName, resultDirectory, folderName);
        var reportCollector = new RobotReportCollector();
        reportCollector.handleRobotReports(tec, filePath, (long) (Missions.SECOND_TO_MILLISECOND * intervalInSeconds),
                terminationInMinutes, scaleAdjustment);
    }

    /**
     * Constructs a file path for saving robot reports. The method also creates the directory
     * if it doesn't exist. The filename is derived from the count of autonomous and lookahead robots,
     * the heuristic applied, and the lookahead distance.
     *
     * @param heuristicName   The heuristic's name used in the simulation. Expected to be non-null.
     * @param resultDirectory The directory path where the robot report files will be saved.
     *                        If the path doesn't exist, the method attempts to create it.
     * @param fileName        The name of the file where the robot reports will be saved.
     * @return The full path, including directory and filename, where the report will be saved.
     */

    public static String createFile(String heuristicName,
                                    String resultDirectory, String fileName) {

        var directoryPath = Paths.get(resultDirectory);

        try {
            Files.createDirectories(directoryPath);
            System.out.println("Directory created successfully.");
        } catch (IOException e) {
            System.err.println("Error while creating directory: " + e.getMessage());
        }

        return resultDirectory + "/" + fileName;
    }
}
