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
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;

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
                final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                        0.09, 60, 2.0, 0.1); //FIXME: HARD CODED

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
                                vehicle.getTaskRepetition(), model);
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
                            vehicle.getTaskRepetition(), model);

                    newVehicle.setGoals(vehicle.getTask()
                            .stream()
                            .map(ProjectData.TaskStep::getPoseName)
                            .map(poseName -> navigationController.getMain().getDataStatus().getProjectData().getPose(poseName))
                            .toArray(Pose[]::new));
//            newVehicle.setMission(vehicle.getMission()); //FIXME Fix Mission, How to handle multiple missions to GoalPoses, handle stoppages
//                    if (newVehicle.getTasks().get(0).getPriority() != 0) {
                    if (!newVehicle.getTasks().get(0).isEmpty()) newVehicle.generatePlans(planner);
//                    }
//                    newVehicle.savePlans(className);  //FIXME: Do I need savePlans() option in GUI?
//                    newVehicle.setPlanningAlgorithm(controllerNavigation.getMain().getDataStatus().getPathPlanner()); //FIXME: HARD CODED

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

    public void updateNavigationBar() {
        navigationController.getMain().getDataStatus().setPlansVerified(true);
        navigationController.getMain().getSetupScene().getPane().setBottom(NavigationBar.getBar(navigationController.getMain(), SceneState.EXPERIMENT));
    }
}