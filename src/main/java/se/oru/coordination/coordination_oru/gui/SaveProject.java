package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;

import java.io.IOException;

public class SaveProject {
    private final ControllerNavigation controllerNavigation;

    public SaveProject(ControllerNavigation controllerNavigation) {
        this.controllerNavigation = controllerNavigation;
    }

    public void clickSave() {
        trySaveProject();
    }

    public void trySaveProject() {
        try {
            boolean isProjectUnchanged = controllerNavigation.getMain().getDataStatus().getProjectData().equals(controllerNavigation.getMain().getDataStatus().getOriginalProjectData());
            if (isProjectUnchanged) doNotSaveProject();
            else saveProject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doNotSaveProject() {
        AlertBox.display("Saving the Project", "There are no changes to save in the project: " + controllerNavigation.getMain().getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
    }

    public void saveProject() throws IOException {
        var file = Utils.createFile(controllerNavigation.getMain(), "project", "json");
        if (file != null) {
            controllerNavigation.getMain().getDataStatus().setProjectFile(file.getAbsolutePath());
            Utils.writeJSON(controllerNavigation.getMain().getDataStatus().getProjectData(), controllerNavigation.getMain().getDataStatus().getProjectFile());
            AlertBox.display("Saving the Project", "The project has been saved to: " + controllerNavigation.getMain().getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
        }
    }

    public void trySaveProject(ControllerNavigation controllerNavigation) {
        trySaveProject();
    }
}