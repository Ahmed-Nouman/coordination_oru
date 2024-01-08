package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Year;

public class MenuBar {

    private static MenuItem newProject;
    private static MenuItem openProject;
    private static MenuItem saveProject;
    private static MenuItem closeProject;
    private static MenuItem runProject;

    protected static javafx.scene.control.MenuBar update(Main main, SceneState sceneState) {

        var menuBar = new javafx.scene.control.MenuBar();
        var separator = new SeparatorMenuItem();

        getMenu(main, sceneState, separator, menuBar);

        return menuBar;
    }

    private static void getMenu(Main main, SceneState sceneState, SeparatorMenuItem separator, javafx.scene.control.MenuBar menuBar) {
        var file = getFileMenu(main, separator);
        var run = getRunMenu(main);
        var help = getHelpMenu();

        menuBar.getMenus().addAll(file, run, help);

        switch (sceneState) {
            case HOME:
                saveProject.setDisable(true);
                closeProject.setDisable(true);
                runProject.setDisable(true);
                break;
            case MAP:
            case VEHICLE:
                newProject.setDisable(true);
                openProject.setDisable(true);
                saveProject.setDisable(true);
                runProject.setDisable(true);
                break;
            case SIMULATION:
                newProject.setDisable(true);
                openProject.setDisable(true);
                break;
            default:
                break;
        }
    }

    private static Menu getFileMenu(Main main, SeparatorMenuItem separator) {
        var fileMenu = new Menu("File");
        newProject(main);
        openProject(main);
        closeProject(main);
        saveProject(main);
        var quit = quit(main);

        fileMenu.getItems().addAll(newProject, openProject, closeProject, saveProject, separator, quit);
        return fileMenu;
    }

    private static Menu getRunMenu(Main main) {
        var run = new Menu("Run");
        runProject(main);
        run.getItems().addAll(runProject);
        return run;
    }

    //FIXME: Not working
    private static void runProject(Main main) {
        runProject = new MenuItem("Run Project...");
        runProject.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN));
        runProject.setOnAction(e -> main.getNavigationButton().runClicked(main));
    }

    private static MenuItem quit(Main main) {
        var quit = new MenuItem("Quit");
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN));
        quit.setOnAction(e -> main.closeProgram(main.getPrimaryStage()));
        return quit;
    }

    private static Menu getHelpMenu() {
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

    private static void saveProject(Main main) {
        saveProject = new MenuItem("Save Project...");
        saveProject.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
        saveProject.setOnAction(e -> main.getNavigationButton().saveProject(main));
    }

    private static void closeProject(Main main) {
        closeProject = new MenuItem("Close Project");
        closeProject.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN));
        closeProject.setOnAction(e -> {
            var stage = main.getPrimaryStage();
            main.initializeStage(stage);
        });
    }

    private static void openProject(Main main) {
        openProject = new MenuItem("Open Project...");
        openProject.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
        openProject.setOnAction(e -> main.getHomeScene().homeController.openProject());
    }

    private static void newProject(Main main) {
        newProject = new MenuItem("New Project...");
        newProject.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newProject.setOnAction(e -> main.getHomeScene().homeController.newProject());
    }
}
