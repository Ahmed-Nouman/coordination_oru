package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;

import java.io.IOException;

public class SaveProject {
    private final NavigationController navigationController;

    public SaveProject(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public void clickSave() {
        trySaveProject();
    }

    public void trySaveProject() {
        try {
            boolean isProjectUnchanged = navigationController.getMain().getDataStatus().getProjectData().equals(navigationController.getMain().getDataStatus().getOriginalProjectData());
            if (isProjectUnchanged) doNotSaveProject();
            else saveProject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doNotSaveProject() {
        AlertBox.display("Saving the Project", "There are no changes to save in the project: " + navigationController.getMain().getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
    }

    public void saveProject() throws IOException {
        var file = Utils.createFile(navigationController.getMain(), "project", "json");
        if (file != null) {
            navigationController.getMain().getDataStatus().setProjectFile(file.getAbsolutePath());
            Utils.writeJSON(navigationController.getMain().getDataStatus().getProjectData(), navigationController.getMain().getDataStatus().getProjectFile());
            AlertBox.display("Saving the Project", "The project has been saved to: " + navigationController.getMain().getDataStatus().getProjectFile(), Alert.AlertType.INFORMATION);
        }
    }

    public void trySaveProject(NavigationController navigationController) {
        trySaveProject();
    }
}