package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Year;

import static se.oru.coordination.coordination_oru.gui.Utils.*;

public class MenuBar {

    private static MenuItem newProject;
    private static MenuItem openProject;
    private static MenuItem saveProject;

    /**
     * Creates and returns a MenuBar node with 'File' and 'Help' menus.
     *
     * @return A MenuBar node containing the constructed menus and their items.
     * @param gui
     */
    protected static javafx.scene.control.MenuBar getMenuBar(GUI gui) {

        javafx.scene.control.MenuBar menuBar = new javafx.scene.control.MenuBar();
        MenuItem separator = new SeparatorMenuItem();

        // File menu
        Menu fileMenu = new Menu("File");

        // File menu items
        newProject = new MenuItem("New Project...");
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newProject.setOnAction(e -> {
            File selectedFile = fileCreator(gui, "Name of Project: ", "json");
            if (selectedFile != null) {
                gui.projectFile = selectedFile.getAbsolutePath();
                gui.pathLabel.setText("Name of Project: " + selectedFile.getName());
                gui.nextProjectButton.setVisible(true);

                try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                    fileWriter.write("{}");
                } catch (IOException ex) {
                    gui.pathLabel.setText("Error: Could not save the file.");
                }
            }

            gui.isNewProject = true;
            gui.nextProjectButton.setVisible(true);
        });

        openProject = new MenuItem("Open Project...");
        openProject.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProject.setOnAction(e -> {
            File file = fileChooser(gui, "Select a project file to open: ", "json");
            if (file != null) {
                gui.projectFile = file.getAbsolutePath();
                gui.pathLabel.setText("Name of Project: " + file.getName());
                gui.nextProjectButton.setVisible(true);
                try {
                    gui.projectData = parseJSON(gui.projectFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            gui.nextProjectButton.setVisible(true);
        });

        saveProject = new MenuItem("Save Project");
        saveProject.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
        saveProject.setOnAction(e -> System.out.println("Save Project"));
//        saveProject.setOnAction(e -> fileJSONSave(this)); FIXME

        MenuItem quit = new MenuItem("QUIT");
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN));
        quit.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(newProject, openProject, saveProject, separator, quit);

        // Help menu
        Menu helpMenu = new Menu("Help");

        // Help menu items
        MenuItem about = new MenuItem("About");
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN));

        about.setOnAction(e -> {
            String content = "A Framework for Multi-Robot Motion Planning, Coordination and Control.\n\n" +
                    "Copyright: © 2017-" + String.valueOf(Year.now()) + "\n\n" +
                    "Authors: Federico Pecora, Anna Mannucci, Franziska Klügl, Ahmed Nouman, Olga Mironenko\n";
            AlertBox.display("Coordination_ORU", content, Alert.AlertType.INFORMATION);
        });

        helpMenu.getItems().addAll(about);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    public static void enableNewProject() {
        newProject.setDisable(false);
    }

    public static void disableNewProject() {
        newProject.setDisable(true);
    }

    public static void enableOpenProject() {
        openProject.setDisable(false);
    }

    public static void disableOpenProject() {
        openProject.setDisable(true);
    }

    public static void enableSaveProject() {
        saveProject.setDisable(false);
    }

    public static void disableSaveProject() {
        saveProject.setDisable(true);
    }
}
