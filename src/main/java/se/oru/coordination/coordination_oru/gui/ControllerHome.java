package se.oru.coordination.coordination_oru.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ControllerHome {

    private final SceneHome scene;

    public ControllerHome(SceneHome scene) {
        this.scene = scene;
    }

    public void clickNewProject() {
        var file = Utils.createFile(scene.getMain(), "newProject", "json");
        if (file != null) {
            initializeNewProject(file);
            writeToFile(file);
        }
    }

    private void initializeNewProject(File file) {
        scene.getFilePath().setText("Name of Project: " + file.getName());
        scene.getMain().getDataStatus().setProjectFile(file.getAbsolutePath());
        scene.getMain().getDataStatus().setProjectData(new ProjectData());
        scene.getMain().getDataStatus().setMapData(new MapData());
        scene.getMain().getNavigationButton().getNext().setDisable(false);
    }

    private void writeToFile(File file) {
        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write("{}");
        } catch (IOException ex) {
            scene.getFilePath().setText("Error: Could not save the file.");
        }
    }

    public void clickOpenProject() {
        var file = Utils.chooseFile(scene.getMain(), "Select a project file to open: ", "json");
        if (file != null) {
            initializeOpenProject(file);
            readFromFile();
        }
    }

    private void initializeOpenProject(File file) {
        scene.getMain().getDataStatus().setProjectFile(file.getAbsolutePath());
        scene.getFilePath().setText("Name of Project: " + file.getName());
        scene.getMain().getNavigationButton().getNext().setDisable(false);
    }

    private void readFromFile() {
        try {
            scene.getMain().getDataStatus().setProjectData(Utils.parseJSON(scene.getMain().getDataStatus().getProjectFile()));
            scene.getMain().getDataStatus().setOriginalProjectData(Utils.deepCopy(scene.getMain().getDataStatus().getProjectData()));
            scene.getMain().getDataStatus().setMapData(Utils.parseYAML(scene.getMain().getDataStatus().getProjectData().getMap()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}