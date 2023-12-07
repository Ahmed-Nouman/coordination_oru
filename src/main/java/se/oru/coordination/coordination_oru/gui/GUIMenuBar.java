package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.time.Year;

public class GUIMenuBar {

    private static MenuItem newProjectMenuItem;
    private static MenuItem openProjectMenuItem;
    private static MenuItem saveProjectMenuItem;

    /**
     * Creates and returns a MenuBar node with 'File' and 'Help' menus.
     *
     * @return A MenuBar node containing the constructed menus and their items.
     * @param gui
     */
    protected static MenuBar getMenuBar(GUI gui) {

        MenuBar menuBar = new javafx.scene.control.MenuBar();
        MenuItem separator = new SeparatorMenuItem();

        // File menu
        Menu fileMenu = new Menu("File");

        // File menu items
        newProjectMenuItem = new MenuItem("New Project...");
        newProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newProjectMenuItem.setOnAction(e -> gui.newProject());

        openProjectMenuItem = new MenuItem("Open Project...");
        openProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProjectMenuItem.setOnAction(e -> gui.openProject());

        saveProjectMenuItem = new MenuItem("Save Project");
        saveProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
        saveProjectMenuItem.setOnAction(e -> gui.saveProject());

        MenuItem quit = new MenuItem("QUIT");
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN));
        quit.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(newProjectMenuItem, openProjectMenuItem, saveProjectMenuItem, separator, quit);

        // Help menu
        Menu helpMenu = new Menu("Help");

        // Help menu items
        MenuItem about = new MenuItem("About");
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN));

        about.setOnAction(e -> {
            String content = "A Framework for Multi-Robot Motion Planning, Coordination and Control.\n\n" +
                    "Copyright: © 2017-" + Year.now() + "\n\n" +
                    "Authors: Federico Pecora, Anna Mannucci, Franziska Klügl, Ahmed Nouman, Olga Mironenko\n";
            AlertBox.display("Coordination_ORU", content, Alert.AlertType.INFORMATION);
        });

        helpMenu.getItems().addAll(about);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    public static void enableNewProject() {
        newProjectMenuItem.setDisable(false);
    }

    public static void disableNewProject() {
        newProjectMenuItem.setDisable(true);
    }

    public static void enableOpenProject() {
        openProjectMenuItem.setDisable(false);
    }

    public static void disableOpenProject() {
        openProjectMenuItem.setDisable(true);
    }

    public static void enableSaveProject() {
        saveProjectMenuItem.setDisable(false);
    }

    public static void disableSaveProject() {
        saveProjectMenuItem.setDisable(true);
    }
}
