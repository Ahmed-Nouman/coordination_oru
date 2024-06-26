package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.time.Year;

public class MenuBar {

    private static MenuItem newProject;
    private static MenuItem openProject;
    private static MenuItem saveProject;

    protected static javafx.scene.control.MenuBar update(Main main, SceneState sceneState) {
        var menuBar = new javafx.scene.control.MenuBar();
        menu(main, menuBar, sceneState);
        return menuBar;
    }

    private static void menu(Main main, javafx.scene.control.MenuBar menuBar, SceneState sceneState) {
        var file = fileMenu(main);
        var help = helpMenu();

        menuBar.getMenus().addAll(file, help);

        switch (sceneState) {
            case HOME:
                saveProject.setDisable(true);
                break;
            case MAP:
            case VEHICLE:
            case COORDINATION:
                newProject.setDisable(true);
                openProject.setDisable(true);
                saveProject.setDisable(true);
                break;
            case SETUP:
                newProject.setDisable(true);
                openProject.setDisable(true);
                break;
            default:
                break;
        }
    }

    private static Menu fileMenu(Main main) {
        var fileMenu = new Menu("File");
        newProject(main);
        openProject(main);
        saveProject(main);
        var separator = new SeparatorMenuItem();
        var quit = quit(main);

        fileMenu.getItems().addAll(newProject, openProject, saveProject, separator, quit);
        return fileMenu;
    }

    private static MenuItem quit(Main main) {
        var quit = new MenuItem("Quit");
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN));
        quit.setOnAction(e -> main.navigationController.closeProgram(main));
        return quit;
    }

    private static Menu helpMenu() {
        var helpMenu = new Menu("Help");
        var about = about();
        helpMenu.getItems().addAll(about);
        return helpMenu;
    }

    private static MenuItem about() {
        var about = new MenuItem("About...");
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN));
        about.setOnAction(e -> {
            String title = "About Coordination_ORU Framework";
            String content = "A Framework for Multi-Agents Motion Planning, Coordination and Control.\n\n" +
                    "Copyright: © 2017-" + Year.now() + "\n\n" +
                    "Authors: Federico Pecora, Anna Mannucci, Franziska Klügl, Ahmed Nouman, Olga Mironenko\n";
            AlertBox.display(title, content, Alert.AlertType.INFORMATION);
        });
        return about;
    }

    private static void newProject(Main main) {
        newProject = new MenuItem("New Project...");
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newProject.setOnAction(e -> main.getHomeScene().controller.clickNewProject());
    }

    private static void openProject(Main main) {
        openProject = new MenuItem("Open Project...");
        openProject.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProject.setOnAction(e -> main.getHomeScene().controller.clickOpenProject());
    }

    private static void saveProject(Main main) {
        saveProject = new MenuItem("Save Project...");
        saveProject.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
        saveProject.setOnAction(e -> main.getNavigationButton().saveProject.trySaveProject(main.getNavigationButton()));
    }

}
