package se.oru.coordination.coordination_oru.gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

public class VerifyPlan {
    private final ControllerNavigation controllerNavigation;

    public VerifyPlan(ControllerNavigation controllerNavigation) {
        this.controllerNavigation = controllerNavigation;
    }

    public void clickVerify() {

        var progressDialog = progressDialog();
        var task = new Task<>() {
            @Override
            protected Void call() {

                var YAML_FILE = controllerNavigation.getMain().getDataStatus().getProjectData().getMap();
                var mapResolution = controllerNavigation.getMain().getDataStatus().getMapData().getResolution();
                var scaleAdjustment = 1 / mapResolution;

                for (var vehicle : controllerNavigation.getMain().getDataStatus().getProjectData().getVehicles()) {
                    AbstractVehicle newVehicle;
                    if ("Human".equals(vehicle.getType()))
                        newVehicle = new LookAheadVehicle(vehicle.getLookAheadDistance() / scaleAdjustment);
                    else newVehicle = new AutonomousVehicle();

                    newVehicle.setID(vehicle.getID());
                    newVehicle.setName(vehicle.getName());
                    newVehicle.setLength(vehicle.getLength() / scaleAdjustment);
                    newVehicle.setWidth(vehicle.getWidth() / scaleAdjustment);
                    newVehicle.setMaxVelocity(vehicle.getMaxVelocity() / scaleAdjustment);
                    newVehicle.setMaxAcceleration(vehicle.getMaxAcceleration() / scaleAdjustment);
                    newVehicle.setSafetyDistance(vehicle.getSafetyDistance() / scaleAdjustment);
                    newVehicle.setColor(Utils.stringToColor(vehicle.getColor()));
                    newVehicle.setInitialPose(controllerNavigation.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()));
                    newVehicle.setGoalPoses(vehicle.getMission()
                            .stream()
                            .map(ProjectData.MissionStep::getPoseName)
                            .map(poseName -> controllerNavigation.getMain().getDataStatus().getProjectData().getPose(poseName))
                            .toArray(Pose[]::new));
//            newVehicle.setMission(vehicle.getMission()); //FIXME Fix Mission, How to handle multiple missions to GoalPoses, handle stoppages
                    newVehicle.setMissionRepetition(vehicle.getMissionRepetition()); //FIXME Handle Mission Repetitions in missionsDispatcher

                    newVehicle.getPlan(YAML_FILE);
                    newVehicle.setPlanningAlgorithm(controllerNavigation.getMain().getDataStatus().getPathPlanner());

                    controllerNavigation.getMain().getDataStatus().getVehicles().add(newVehicle);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                progressDialog.close();
                updateNavigationBar();
            }

            @Override
            protected void failed() {
                super.failed();
                progressDialog.close();
            }
        };
        progressDialog.show();
        new Thread(task).start();
    }

    public Stage progressDialog() {
        var progressDialog = new Stage();
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        progressDialog.setTitle("Verifying and Saving Plans");

        var progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);

        var dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.getChildren().add(progressBar);

        var dialogScene = new Scene(dialogVbox);
        progressDialog.setScene(dialogScene);
        return progressDialog;
    }

    public void updateNavigationBar() {
        controllerNavigation.getMain().getDataStatus().setPlansVerified(true);
        controllerNavigation.getMain().getSetupScene().getPane().setBottom(NavigationBar.getBar(controllerNavigation.getMain(), SceneState.EXPERIMENT));
    }
}