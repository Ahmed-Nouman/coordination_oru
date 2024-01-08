package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Year;

public class GUIMenuBar {

    private static MenuItem newProjectMenuItem;
    private static MenuItem openProjectMenuItem;
    private static MenuItem saveProjectMenuItem;
    private static MenuItem closeProjectMenuItem;
    private static MenuItem runProjectMenuItem;

    /**
     * Creates and returns a MenuBar node with 'File' and 'Help' menus.
     *
     * @return A MenuBar node containing the constructed menus and their items.
     * @param main
     */

    //FIXME: Handle Menu logic in this class
    protected static MenuBar getMenuBar(Main main) {

        MenuBar menuBar = new javafx.scene.control.MenuBar();
        MenuItem separator = new SeparatorMenuItem();

        // File menu
        Menu fileMenu = new Menu("File");

        // File menu items
        newProjectMenuItem = new MenuItem("New Project...");
        newProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newProjectMenuItem.setOnAction(e -> {
            var selectedFile = Utils.createFile(main, "newProject", "json");
            if (selectedFile != null) {
                main.getDataStatus().setProjectFile(selectedFile.getAbsolutePath());
                main.getHomeScene().homeController.getFilePathField(main.getHomeScene()).setText("Name of Project: " + selectedFile.getName());
                main.getDataStatus().setProjectData(new ProjectData());
                main.getDataStatus().setMapData(new MapData());
                main.getDataStatus().setNewProject(true);
                main.getNavigationButton().getNextButton().setVisible(true);

                // Write {} to the new project file
                try (FileWriter fileWriter = new FileWriter(selectedFile)) {
                    fileWriter.write("{}");
                } catch (IOException ex) {
                    main.getHomeScene().homeController.getFilePathField(main.getHomeScene()).setText("Error: Could not save the file.");
                }
            }
        });

        openProjectMenuItem = new MenuItem("Open Project...");
        openProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProjectMenuItem.setOnAction(e -> {
            var file = Utils.chooseFile(main, "Select a project file to open: ", "json");
            if (file != null) {
                main.getDataStatus().setProjectFile(file.getAbsolutePath());
                main.getHomeScene().homeController.getFilePathField(main.getHomeScene()).setText("Name of Project: " + file.getName());
                main.getDataStatus().setNewProject(false);
                main.getNavigationButton().getNextButton().setVisible(true);
                try {
                    main.getDataStatus().setProjectData(Utils.parseJSON(main.getDataStatus().getProjectFile()));
                    main.getDataStatus().setOriginalProjectData(Utils.deepCopy(main.getDataStatus().getProjectData()));
                    main.getDataStatus().setMapData(Utils.parseYAML(main.getDataStatus().getProjectData().getMap()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        closeProjectMenuItem = new MenuItem("Close Project");
        closeProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN));
        closeProjectMenuItem.setOnAction(e -> {
            Stage stage = main.getPrimaryStage();
            main.initializeStage(stage);
        });

        saveProjectMenuItem = new MenuItem("Save Project...");
        saveProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
//        saveProjectMenuItem.setOnAction(e -> main.getSimulationScene().saveProject(main));

        MenuItem quitMenuItem = new MenuItem("Quit");
        quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN));
        quitMenuItem.setOnAction(e -> main.closeProgram(main.getPrimaryStage()));

        fileMenu.getItems().addAll(newProjectMenuItem, openProjectMenuItem, closeProjectMenuItem, saveProjectMenuItem, separator, quitMenuItem);

        // Run menu
        Menu runMenu = new Menu("Run");
        runProjectMenuItem = new MenuItem("Run Project...");
        runProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN));
        runProjectMenuItem.setOnAction(e -> {
//            var thread = new Thread(gui.getSimulationScene().runProject(gui.getDataStatus()));
//            thread.start();
        });
        runMenu.getItems().addAll(runProjectMenuItem);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About...");
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN));
        about.setOnAction(e -> {
            String title = "About Coordination_ORU Framework";
            String content = "A Framework for Multi-Agents Motion Planning, Coordination and Control.\n\n" +
                    "Copyright: © 2017-" + Year.now() + "\n\n" +
                    "Authors: Federico Pecora, Anna Mannucci, Franziska Klügl, Ahmed Nouman, Olga Mironenko\n";
            AlertBox.display(title, content, Alert.AlertType.INFORMATION);
        });
        helpMenu.getItems().addAll(about);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, runMenu, helpMenu);

        return menuBar;
    }

    public static void disableNewProject() {
        newProjectMenuItem.setDisable(true);
    }

    public static void disableOpenProject() {
        openProjectMenuItem.setDisable(true);
    }

    public static void disableSaveProject() {
        saveProjectMenuItem.setDisable(true);
    }

    public static void disableCloseProject() {
        closeProjectMenuItem.setDisable(true);
    }

    public static void disableRunProject() {
        runProjectMenuItem.setDisable(true);
    }
}
