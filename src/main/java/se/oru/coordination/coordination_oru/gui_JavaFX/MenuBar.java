package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static se.oru.coordination.coordination_oru.gui_JavaFX.Utils.fileJSONCreate;
import static se.oru.coordination.coordination_oru.gui_JavaFX.Utils.fileJSONOpen;

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
        newProject.setOnAction(e -> fileJSONCreate(gui));

        openProject = new MenuItem("Open Project...");
        openProject.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProject.setOnAction(e -> fileJSONOpen(gui));

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
            String content = "Coordination_ORU - Robot-agnostic online coordination for multiple robots\n\n" +
                    "Copyright: © 2017-2023\n\n" +
                    "Authors: Federico Pecora, Anna Mannucci, Ahmed Nouman, Olga Mironenko\n";
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
