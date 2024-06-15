package se.oru.coordination.coordination_oru.gui;

import java.util.ArrayList;
import java.util.Objects;

public class VehicleController {
    private final VehicleScene scene;

    public VehicleController(VehicleScene scene) {
        this.scene = scene;
    }

    public void changeName() {
        var newName = scene.getNameField().getText();
        var oldName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (!Objects.equals(newName, "") && newName != null) {
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(oldName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setName(newName);
            scene.getVehicles().getItems().clear();
            scene.getMain().getDataStatus().getProjectData().getVehicles().forEach(vehicle -> scene.getVehicles().getItems().add(vehicle.getName()));
            scene.getVehicles().getSelectionModel().select(newName);
        }
    }

    public void changePriority() {
        var validated = Utils.validateInteger(scene.getPriorityField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newPriority = Integer.parseInt(scene.getPriorityField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setPriority(newPriority);
        }
    }

    public void changeLength() {
        var validated = Utils.validateDouble(scene.getLengthField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newLength = Double.parseDouble(scene.getLengthField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setLength(newLength);
        }
    }

    public void changeWidth() {
        var validated = Utils.validateDouble(scene.getWidthField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newWidth = Double.parseDouble(scene.getWidthField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setWidth(newWidth);
        }
    }

    public void changeMaxVelocity() {
        var validated = Utils.validateDouble(scene.getMaxVelocityField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newMaxVelocity = Double.parseDouble(scene.getMaxVelocityField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setMaxVelocity(newMaxVelocity);
        }
    }

    public void changeMaxAcceleration() {
        var validated = Utils.validateDouble(scene.getMaxAccelerationField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newMaxAcceleration = Double.parseDouble(scene.getMaxAccelerationField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setMaxAcceleration(newMaxAcceleration);
        }
    }

    public void changeSafetyDistance() {
        var validated = Utils.validateDouble(scene.getSafetyDistanceField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newSafetyDistance = Double.parseDouble(scene.getSafetyDistanceField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setSafetyDistance(newSafetyDistance);
        }
    }

    public void chooseColor() {
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (vehicleName != null) {
            var newColor = scene.getColorField().getValue();
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setColor(newColor);
        }
    }

    public void chooseStartPose() {
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (vehicleName != null) {
            var newStartPose = scene.getStartPoseField().getValue();
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setStartPose(newStartPose);
        }
    }

    public void checkIsHuman() {
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        var vehicle = vehicleName != null ? scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())) : null;

        if (scene.getIsHumanField().isSelected()) {
            scene.getLookAheadDistance().setVisible(true);
            scene.getLookAheadDistanceField().setVisible(true);
            if (vehicle != null) {
                vehicle.setType("Human");
            }
        } else {
            scene.getLookAheadDistance().setVisible(false);
            scene.getLookAheadDistanceField().setVisible(false);
            if (vehicle != null) {
                vehicle.setType("Autonomous");
            }
        }
    }

    public void changeTaskRepetition() {
        var validated = Utils.validateInteger(scene.getTaskRepetitionField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newMissionRepetition = Integer.parseInt(scene.getTaskRepetitionField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setTaskRepetition(newMissionRepetition);
        }
    }

    public void changeLookAhead() {
        var validated = Utils.validateDouble(scene.getLookAheadDistanceField());
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        if (validated && vehicleName != null) {
            var newLookAheadDistance = Double.parseDouble(scene.getLookAheadDistanceField().getText());
            scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).setLookAheadDistance(newLookAheadDistance);
        }
    }

    public void clickAddVehicle() {

    var baseNameOfVehicle = "vehicle";
    var priorityOfVehicle = 1;
    var lengthOfVehicle = 8.0;
    var widthOfVehicle = 4.0;
    var maxVelocityOfVehicle = 10.0;
    var maxAccelerationOfVehicle = 1.0;
    var safetyDistanceOfVehicle = 0.0;
    var colorOfVehicle = "Yellow";
    var initialPoseOfVehicle = scene.getMain().getDataStatus().getProjectData().getPoses().keySet().stream().findFirst().orElse(null);
    var taskRepetitionOfVehicle = 1;
    var typeOfVehicle = "Autonomous";
    var lookAheadDistanceOfVehicle = 0.0;
    var taskOfVehicle = new ArrayList<ProjectData.TaskStep>();

    // Handle duplicate names for vehicles
    String nameOfVehicle = baseNameOfVehicle;
    if (scene.getMain().getDataStatus().getProjectData().getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(baseNameOfVehicle))) {
        scene.getMain().getDataStatus().setVehicleCounter(scene.getMain().getDataStatus().getVehicleCounter() + 1);
        nameOfVehicle = baseNameOfVehicle + " (" + scene.getMain().getDataStatus().getVehicleCounter() + ")";
    }

    var vehicle = new ProjectData.Vehicle();
    vehicle.setName(nameOfVehicle);
    vehicle.setPriority(priorityOfVehicle);
    vehicle.setLength(lengthOfVehicle);
    vehicle.setWidth(widthOfVehicle);
    vehicle.setMaxVelocity(maxVelocityOfVehicle);
    vehicle.setMaxAcceleration(maxAccelerationOfVehicle);
    vehicle.setSafetyDistance(safetyDistanceOfVehicle);
    vehicle.setColor(colorOfVehicle);
    vehicle.setStartPose(initialPoseOfVehicle);
    vehicle.setTask(taskOfVehicle);
    vehicle.setTaskRepetition(taskRepetitionOfVehicle);
    vehicle.setType(typeOfVehicle);
    vehicle.setLookAheadDistance(lookAheadDistanceOfVehicle);

    scene.getMain().getDataStatus().getProjectData().addVehicle(vehicle);
    scene.getVehicles().getItems().add(vehicle.getName());
    scene.getVehicles().getSelectionModel().selectLast();

    verifyDeleteVehicle();
    verifyNext();
    }

    public void clickDeleteVehicle() {
    scene.getDeleteVehicle().setOnAction(e -> {
        var vehicleName = scene.getVehicles().getSelectionModel().getSelectedItem();
        scene.getMain().getDataStatus().getProjectData().removeVehicle(scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(vehicleName, scene.getMain().getDataStatus().getProjectData().getVehicles())).getID());
        scene.getVehicles().getItems().remove(vehicleName);
        scene.getVehicles().getSelectionModel().selectFirst();
        verifyDeleteVehicle();
        verifyNext();
        });
    }

private void verifyDeleteVehicle() {
    scene.getDeleteVehicle().setDisable(scene.getVehicles().getItems().isEmpty());
    }
private void verifyNext() {
    scene.getMain().getNavigationButton().getNext().setDisable(scene.getVehicles().getItems().isEmpty());
    }

public void clickDelete() {
    var vehicle = scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(scene.getVehicles().getSelectionModel().getSelectedItem(), scene.getMain().getDataStatus().getProjectData().getVehicles()));
    if (vehicle != null) {
        var index = scene.getTasks().getSelectionModel().getSelectedIndex();
        scene.getTasks().getItems().remove(index);
        vehicle.getTasks().remove(index);
        }
    }

    public void clickAdd() {
        TaskDialog.add(scene);
    }

    public void clickDown() {
        var index = scene.getTasks().getSelectionModel().getSelectedIndex();
        var vehicle = scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(scene.getVehicles().getSelectionModel().getSelectedItem(), scene.getMain().getDataStatus().getProjectData().getVehicles()));
        if (index > 0) {
            var itemToMove = scene.getTasks().getItems().remove(index);
            scene.getTasks().getItems().add(index - 1, itemToMove);
            scene.getTasks().getSelectionModel().select(index - 1);
            if (vehicle != null) {
                var taskSteps = vehicle.getTasks();
                var taskStep = taskSteps.remove(index);
                taskSteps.add(index - 1, taskStep);
            }
        }
    }

    public void clickUp() {
        var index = scene.getTasks().getSelectionModel().getSelectedIndex();
        var vehicle = scene.getMain().getDataStatus().getProjectData().getVehicle(scene.getMain().getDataStatus().getProjectData().getVehicleID(scene.getVehicles().getSelectionModel().getSelectedItem(), scene.getMain().getDataStatus().getProjectData().getVehicles()));
        if (index < scene.getTasks().getItems().size() - 1) {
            var itemToMove = scene.getTasks().getItems().remove(index);
            scene.getTasks().getItems().add(index + 1, itemToMove);
            scene.getTasks().getSelectionModel().select(index + 1);
            if (vehicle != null) {
                var missionSteps = vehicle.getTasks();
                var missionStep = missionSteps.remove(index);
                missionSteps.add(index + 1, missionStep);
            }
        }
    }

    public void doubleClickTask() {
        TaskDialog.edit(scene);
    }

}