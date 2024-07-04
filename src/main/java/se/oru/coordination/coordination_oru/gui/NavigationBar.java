package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;

public class NavigationBar {

    private static final int SPACING = 8;
    private static final int PADDING = 10;
    private static final int BUTTON_SPACING = 50;

    public static Pane getBar(Main main, SceneState scene) {

        var navigationPane = navigationPane();
        var buttonsButtons = navigationButtons();

        switch (scene) {
            case HOME:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getNext());
                break;
            case MAP:
            case VEHICLE:
            case COORDINATION:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getBack(), main.getNavigationButton().getNext());
                break;
            case SETUP:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getBack(),
                        main.getNavigationButton().getSave());
                boolean directoryExists = isDirectoryExists(main);

                if (main.getDataStatus().getOriginalProjectData().equals(main.getDataStatus().getProjectData()) && (directoryExists)) // Decide between Run and Verify
                    buttonsButtons.getChildren().add(main.getNavigationButton().getRun());
                else buttonsButtons.getChildren().add(main.getNavigationButton().getVerify());
                break;
            default:
                break;
        }

        navigationPane.getChildren().addAll(new Separator(), buttonsButtons);
        return navigationPane;
    }

    private static boolean isDirectoryExists(Main main) {
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length - 1].getFileName().split("\\.")[0];
        String pathsFolderName = "paths/" + className + "/";
        String projectFileName = main.getDataStatus().getProjectFile();
        String projectName = projectFileName.replaceAll(".*/([^/]+)\\.json$", "$1");
        String targetPath = pathsFolderName + projectName + "/";
        File directory = new File(targetPath);
        return directory.exists() && directory.isDirectory();
    }

    private static HBox navigationButtons() {
        var buttonsPane = new HBox();
        buttonsPane.setSpacing(BUTTON_SPACING);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);
        return buttonsPane;
    }

    private static VBox navigationPane() {
        var bottomPane = new VBox();
        bottomPane.setSpacing(SPACING);
        bottomPane.setPadding(new Insets(0, PADDING, PADDING, PADDING));
        bottomPane.setAlignment(Pos.CENTER);
        return bottomPane;
    }
}
