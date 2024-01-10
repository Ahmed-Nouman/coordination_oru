package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.TextField;

import java.util.Objects;

public class ControllerVehicle {
    private final SceneVehicle sceneVehicle;
    //TODO: Move all the interaction logic from VehicleScene to this class

    public ControllerVehicle(SceneVehicle sceneVehicle) {
        this.sceneVehicle = sceneVehicle;
    }

    public void getName(TextField nameField) {
        nameField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newVehicleName = nameField.getText();
                String oldVehicleName = sceneVehicle.getVehicleListView().getSelectionModel().getSelectedItem();
                if (!Objects.equals(newVehicleName, "") && newVehicleName != null) {
                    sceneVehicle.getMain().getDataStatus().getProjectData().getVehicle(sceneVehicle.getMain().getDataStatus().getProjectData().getVehicleID(oldVehicleName, sceneVehicle.getMain().getDataStatus().getProjectData().getVehicles())).setName(newVehicleName);
                    sceneVehicle.getVehicleListView().getItems().clear();
                    sceneVehicle.getMain().getDataStatus().getProjectData().getVehicles().forEach(vehicle -> sceneVehicle.getVehicleListView().getItems().add(vehicle.getName()));
                    sceneVehicle.getVehicleListView().getSelectionModel().selectFirst();
                }
            }
        });
    }

    public void getPriority(TextField priorityTextField) {
        priorityTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(priorityTextField);
                String selectedVehicleName = sceneVehicle.getVehicleListView().getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newPriority = Integer.parseInt(priorityTextField.getText());
                    sceneVehicle.getMain().getDataStatus().getProjectData().getVehicle(sceneVehicle.getMain().getDataStatus().getProjectData().getVehicleID(selectedVehicleName, sceneVehicle.getMain().getDataStatus().getProjectData().getVehicles())).setPriority(newPriority);
                }
            }
        });
    }
}