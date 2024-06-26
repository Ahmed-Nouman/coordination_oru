package se.oru.coordination.coordination_oru.gui;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.utils.Heuristics;

public class SetupController {
    private final SetupScene scene;

    public SetupController(SetupScene scene) {
        this.scene = scene;
    }

    public void choosePathPlanner() {
        var pathPlanner = scene.getPathPlannerField().getValue();
        if (pathPlanner != null && pathPlanner.equals("Fast (RRTConnect)")) {
            scene.getMain().getDataStatus().setPathPlanner(ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect);
        } else scene.getMain().getDataStatus().setPathPlanner(ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTstar);
    }

    public void changeSimulationTime() {
        var validated = Utils.validateInteger(scene.getSimulationTimeField());
        if (validated)
            scene.getMain().getDataStatus().setSimulationTime(Integer.parseInt(scene.getSimulationTimeField().getText()));
    }

    public void checkSavingReport() {
        if (scene.getSaveReportField().isSelected()) {
            scene.getReportFolder().setVisible(true);
            scene.getReportFolderField().setVisible(true);
        } else {
            scene.getReportFolder().setVisible(false);
            scene.getReportFolderField().setVisible(false);
            scene.getReportLocation().setVisible(false);
            scene.getReportLocationField().setVisible(false);
        }
        scene.getMain().getDataStatus().setWriteVehicleReports(scene.getSaveReportField().isSelected());
    }

    public void changeNumberOfRun() {
        var validated = Utils.validateInteger(scene.getNumberOfRunField());
        if (validated)
            scene.getMain().getDataStatus().setNumberOfRuns(Integer.parseInt(scene.getNumberOfRunField().getText()));
    }

    public void clickReportFolder() {
        var directoryChooser = new DirectoryChooser();
        var directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
            scene.getReportLocation().setVisible(true);
            scene.getReportLocationField().setVisible(true);
            scene.getMain().getDataStatus().setReportsFolder(directory.getAbsolutePath());
            scene.getReportLocationField().setText(scene.getMain().getDataStatus().getReportsFolder());
        }
    }
}
