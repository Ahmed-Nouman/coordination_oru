package se.oru.coordination.coordination_oru.gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

import java.util.Arrays;

public class VerifyPlan {
    public static final int WIDTH = 250;
    public static final int SPACING = 20;
    private final NavigationController navigationController;

    public VerifyPlan(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public void clickVerify() {

        var progressDialog = progressDialog();
        var task = new Task<>() {
            @Override
            protected Void call() {

                var map = navigationController.getMain().getDataStatus().getProjectData().getMap();
                var mapResolution = navigationController.getMain().getDataStatus().getMapData().getResolution();
                var scaleAdjustment = 1 / mapResolution;
                final String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
                final ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30); //FIXME: HARD CODED
                final var planner = new VehiclePathPlanner(map, navigationController.getMain().getDataStatus().getPathPlanner(),
                        0.09, 60, 2.0, 0.1);

                for (var vehicle : navigationController.getMain().getDataStatus().getProjectData().getVehicles()) {
                    AbstractVehicle newVehicle;
                    if ("Human".equals(vehicle.getType()))
                        newVehicle = new LookAheadVehicle(vehicle.getID(),
                                vehicle.getName(),
                                vehicle.getLookAheadDistance() / scaleAdjustment,
                                vehicle.getPriority(),
                                Utils.stringToColor(vehicle.getColor()),
                                vehicle.getMaxVelocity() / scaleAdjustment,
                                vehicle.getMaxAcceleration() / scaleAdjustment,
                                vehicle.getLength() / scaleAdjustment,
                                vehicle.getWidth() / scaleAdjustment,
                                navigationController.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                                vehicle.getSafetyDistance() / scaleAdjustment,
                                vehicle.getTasksRepetition(), model);
                    else newVehicle = new AutonomousVehicle(vehicle.getID(),
                            vehicle.getName(),
                            vehicle.getPriority(),
                            Utils.stringToColor(vehicle.getColor()),
                            vehicle.getMaxVelocity() / scaleAdjustment,
                            vehicle.getMaxAcceleration() / scaleAdjustment,
                            vehicle.getLength() / scaleAdjustment,
                            vehicle.getWidth() / scaleAdjustment,
                            navigationController.getMain().getDataStatus().getProjectData().getPose(vehicle.getInitialPose()),
                            vehicle.getSafetyDistance() / scaleAdjustment,
                            vehicle.getTasksRepetition(), model);

                    for (var task : vehicle.getTasks()) {
                        var poses = Arrays.stream(task.getPoseName().split(" -> "))
                                .map(poseName -> navigationController.getMain().getDataStatus().getProjectData().getPose(poseName.trim()))
                                .toArray(Pose[]::new);
                        newVehicle.addTask(new se.oru.coordination.coordination_oru.utils.Task(task.getTaskName(), task.getDuration(), poses, task.getPriority()), task.getRepetition());
                    }
                    if (!newVehicle.getTasks().get(0).isEmpty())
                        try {
                            newVehicle.generatePlans(planner);
                        } catch (Exception e) {
                            showErrorAlert("Error in generating plans", "Error in generating plans for vehicle " + newVehicle.getName() + " with ID " + newVehicle.getID() + " and type " + newVehicle.getType() + " in VerifyPlan.java");
                            e.printStackTrace();
                        }

                    var filePath = navigationController.getMain().getDataStatus().getProjectFile();
                    var parts = filePath.split("/");
                    var lastPart = parts[parts.length - 1];
                    var projectName = lastPart.split("\\.")[0];
                    newVehicle.savePlans(className + "/" + projectName);

                    navigationController.getMain().getDataStatus().getVehicles().add(newVehicle);
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
                showErrorAlert("Motion planning failed", "Please try a different setting for the vehicles and re-try.");
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
        progressBar.setPrefWidth(WIDTH);

        var dialogVbox = new VBox(SPACING);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(SPACING));
        dialogVbox.getChildren().add(progressBar);

        var dialogScene = new Scene(dialogVbox);
        progressDialog.setScene(dialogScene);
        return progressDialog;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updateNavigationBar() {
        navigationController.getMain().getDataStatus().setPlansVerified(true);

        // Replace Verify button with Run button in the navigation bar
        var navigationBar = NavigationBar.getBar(navigationController.getMain(), SceneState.SETUP);
        var buttonsPane = (HBox) navigationBar.getChildren().get(1);
        buttonsPane.getChildren().removeIf(node -> node == navigationController.getVerify());
        if (!buttonsPane.getChildren().contains(navigationController.getRun())) {
            buttonsPane.getChildren().add(navigationController.getRun());
        }

        navigationController.getMain().getSetupScene().getPane().setBottom(navigationBar);
    }
}
