package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class HomeScene {
    private final se.oru.coordination.coordination_oru.gui.GUI GUI;
    private final Text filePath = new Text("");

    public HomeScene(se.oru.coordination.coordination_oru.gui.GUI GUI) {
        this.GUI = GUI;
    }

    public Scene get() {

        var root = new BorderPane();
        root.setPrefWidth(400);
        root.setPrefHeight(300);
        Separator separator = new Separator();
        separator.setVisible(false);
        GUI.getNavigationBar().getNextButton().setVisible(false);

        // Top Pane - Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(GUI));
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableCloseProject();
        GUIMenuBar.disableRunProject();

        // Center Pane
        var centerPane = new VBox();
        centerPane.setSpacing(40);
        centerPane.setPadding(new Insets(40));

        var welcomeMessageLabel = new Label("Welcome to Coordination_ORU!");
        welcomeMessageLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        welcomeMessageLabel.setAlignment(Pos.CENTER);

        // Center - Button Pane
        var projectButtonPane = new HBox();
        projectButtonPane.setSpacing(40);
        Button newProjectButton = new Button("New Project");
        Button openProjectButton = new Button("Open Project");
        projectButtonPane.getChildren().addAll(newProjectButton, openProjectButton);
        projectButtonPane.setAlignment(Pos.CENTER);

        centerPane.getChildren().addAll(welcomeMessageLabel, projectButtonPane, filePath);
        centerPane.setAlignment(Pos.CENTER);

        // Set VBox children to grow equally
        VBox.setVgrow(projectButtonPane, Priority.ALWAYS);

        root.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        newProjectButton.setOnAction(e -> GUI.getHomeScene().newProject(GUI));

        openProjectButton.setOnAction(e -> GUI.getHomeScene().openProject(GUI));

        // Bottom Pane - Navigation Buttons
        root.setBottom(BottomPane.getBottomPane(GUI.getNavigationBar().getNextButton()));
        return new Scene(root);
    }

    public void openProject(GUI gui) {
        File file = chooseFile(gui, "Select a project file to open: ", "json");
        if (file != null) {
            gui.getDataStatus().setProjectFile(file.getAbsolutePath());
            filePath.setText("Name of Project: " + file.getName());
            gui.getDataStatus().setNewProject(false);
            gui.getNavigationBar().getNextButton().setVisible(true);
            try {
                gui.getDataStatus().setProjectData(parseJSON(gui.getDataStatus().getProjectFile()));
                gui.getDataStatus().setOriginalProjectData(deepCopy(gui.getDataStatus().getProjectData()));
                gui.getDataStatus().setMapData(parseYAML(gui.getDataStatus().getProjectData().getMap()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void newProject(GUI gui) {
        File selectedFile = createFile(gui, "newProject", "json");
        if (selectedFile != null) {
            gui.getDataStatus().setProjectFile(selectedFile.getAbsolutePath());
            filePath.setText("Name of Project: " + selectedFile.getName());
            gui.getDataStatus().setProjectData(new ProjectData());
            gui.getDataStatus().setMapData(new MapData());
            gui.getDataStatus().setNewProject(true);
            gui.getNavigationBar().getNextButton().setVisible(true);

            // Write {} to the new project file
            try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                fileWriter.write("{}");
            } catch (IOException ex) {
                filePath.setText("Error: Could not save the file.");
            }
        }
    }

}