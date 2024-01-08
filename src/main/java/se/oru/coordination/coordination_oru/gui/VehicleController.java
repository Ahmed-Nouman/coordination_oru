package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.TextField;

import java.util.Objects;

public class VehicleController {
    private final VehicleScene vehicleScene;
    //TODO: Move all the interaction logic from VehicleScene to this class

    public VehicleController(VehicleScene vehicleScene) {
        this.vehicleScene = vehicleScene;
    }

    public void getName(TextField nameField) {
        nameField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String newVehicleName = nameField.getText();
                String oldVehicleName = vehicleScene.getVehicleListView().getSelectionModel().getSelectedItem();
                if (!Objects.equals(newVehicleName, "") && newVehicleName != null) {
                    vehicleScene.getMain().getDataStatus().getProjectData().getVehicle(vehicleScene.getMain().getDataStatus().getProjectData().getVehicleID(oldVehicleName, vehicleScene.getMain().getDataStatus().getProjectData().getVehicles())).setName(newVehicleName);
                    vehicleScene.getVehicleListView().getItems().clear();
                    vehicleScene.getMain().getDataStatus().getProjectData().getVehicles().forEach(vehicle -> vehicleScene.getVehicleListView().getItems().add(vehicle.getName()));
                    vehicleScene.getVehicleListView().getSelectionModel().selectFirst();
                }
            }
        });
    }

    public void getPriority(TextField priorityTextField) {
        priorityTextField.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Boolean validated = Utils.validateInteger(priorityTextField);
                String selectedVehicleName = vehicleScene.getVehicleListView().getSelectionModel().getSelectedItem();
                if (validated && selectedVehicleName != null) {
                    int newPriority = Integer.parseInt(priorityTextField.getText());
                    vehicleScene.getMain().getDataStatus().getProjectData().getVehicle(vehicleScene.getMain().getDataStatus().getProjectData().getVehicleID(selectedVehicleName, vehicleScene.getMain().getDataStatus().getProjectData().getVehicles())).setPriority(newPriority);
                }
            }
        });
    }
}