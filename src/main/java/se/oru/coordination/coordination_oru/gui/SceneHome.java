package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class SceneHome {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final int SPACING = 40;
    private static final int PADDING = 40;
    private static final int FONT_SIZE = 16;
    private final Main main;
    private BorderPane pane;
    private Button newProject = new Button();
    private Button openProject = new Button();
    private Text filePath = new Text("");
    public final ControllerHome controller = new ControllerHome(this);

    public SceneHome(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = initializePane();
        getMenuBar();
        getCenterPane();
        getNavigationBar();

        controller.newProjectClicked();
        controller.openProjectClicked();
        return new Scene(pane);
    }

    private void getMenuBar() {
        pane.setTop(MenuBar.update(main, SceneState.HOME));
    }

    private void getNavigationBar() {
        pane.setBottom(NavigationBar.update(main, SceneState.HOME));
    }

    private BorderPane initializePane() {
        pane = new BorderPane();
        pane.setPrefWidth(WIDTH);
        pane.setPrefHeight(HEIGHT);
        main.getNavigationButton().getNextButton().setDisable(true);
        return pane;
    }

    private void getCenterPane() {
        var centerPane = new VBox();
        centerPane.setSpacing(SPACING);
        centerPane.setPadding(new Insets(PADDING));
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        var welcomeMessage = welcomeMessage();
        var buttons = buttonPane();
        filePath = new Text("");
        centerPane.getChildren().addAll(welcomeMessage, buttons, filePath);
    }

    private Label welcomeMessage() {
        var welcomeMessageLabel = new Label("Welcome to Coordination_ORU!");
        welcomeMessageLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, FONT_SIZE));
        welcomeMessageLabel.setAlignment(Pos.CENTER);
        return welcomeMessageLabel;
    }

    private HBox buttonPane() {
        var buttonPane = new HBox();
        buttonPane.setSpacing(SPACING);
        buttonPane.setAlignment(Pos.CENTER);

        newProject = new Button("New Project");
        openProject = new Button("Open Project");
        buttonPane.getChildren().addAll(newProject, openProject);
        return buttonPane;
    }

    public Main getMain() {
        return main;
    }

    public Button getNewProject() {
        return newProject;
    }

    public void setNewProject(Button newProject) {
        this.newProject = newProject;
    }

    public Button getOpenProject() {
        return openProject;
    }

    public void setOpenProject(Button openProject) {
        this.openProject = openProject;
    }

    public Text getFilePath() {
        return filePath;
    }

    public void setFilePath(Text filePath) {
        this.filePath = filePath;
    }
}