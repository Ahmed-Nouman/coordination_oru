package se.oru.coordination.coordination_oru.gui;

import javafx.scene.text.Text;

import java.io.FileWriter;
import java.io.IOException;

public class HomeController {

    private final HomeScene homeScene;

    public HomeController(HomeScene homeScene) {
        this.homeScene = homeScene;
    }

    public void newProject() {
        var selectedFile = Utils.createFile(homeScene.getMain(), "newProject", "json");
        if (selectedFile != null) {
            homeScene.getMain().getDataStatus().setProjectFile(selectedFile.getAbsolutePath());
            getFilePathField(homeScene).setText(("Name of Project: " + selectedFile.getName()));
            homeScene.getMain().getDataStatus().setProjectData(new ProjectData());
            homeScene.getMain().getDataStatus().setMapData(new MapData());
            homeScene.getMain().getDataStatus().setNewProject(true);
            homeScene.getMain().getNavigationButton().getNextButton().setVisible(true);

            try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                fileWriter.write("{}");
            } catch (IOException ex) {
                getFilePathField(homeScene).setText("Error: Could not save the file.");
            }
        }
    }

    public Text getFilePathField(HomeScene homeScene) {
        return homeScene.filePathField;
    }

    public void openProject() {
        var file = Utils.chooseFile(homeScene.getMain(), "Select a project file to open: ", "json");
        if (file != null) {
            homeScene.getMain().getDataStatus().setProjectFile(file.getAbsolutePath());
            getFilePathField(homeScene).setText("Name of Project: " + file.getName());
            homeScene.getMain().getDataStatus().setNewProject(false);
            homeScene.getMain().getNavigationButton().getNextButton().setVisible(true);
            try {
                homeScene.getMain().getDataStatus().setProjectData(Utils.parseJSON(homeScene.getMain().getDataStatus().getProjectFile()));
                homeScene.getMain().getDataStatus().setOriginalProjectData(Utils.deepCopy(homeScene.getMain().getDataStatus().getProjectData()));
                homeScene.getMain().getDataStatus().setMapData(Utils.parseYAML(homeScene.getMain().getDataStatus().getProjectData().getMap()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}