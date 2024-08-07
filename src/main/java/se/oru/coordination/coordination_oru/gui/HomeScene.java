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

public class HomeScene {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final int SPACING = 40;
    private static final int PADDING = 40;
    private static final int FONT_SIZE = 16;
    private Button newProject = new Button();
    private Button openProject = new Button();
    private final Text filePath = new Text("");
    private final Main main;
    private BorderPane pane;
    public final HomeController controller = new HomeController(this);

    public HomeScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = initializePane();
        menuBar();
        centerPane();
        navigationBar();
        controllers();
        return new Scene(pane);
    }

    private void controllers() {
        newProject.setOnAction(e -> controller.clickNewProject());
        openProject.setOnAction(e -> controller.clickOpenProject());
    }

    private void menuBar() {
        pane.setTop(MenuBar.update(main, SceneState.HOME));
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.HOME));
    }

    private BorderPane initializePane() {
        pane = new BorderPane();
        pane.setPrefWidth(WIDTH);
        pane.setPrefHeight(HEIGHT);
        main.getNavigationButton().getNext().setDisable(true);
        return pane;
    }

    private void centerPane() {
        var centerPane = initializeCenterPane();
        var welcomeMessage = welcomeMessage();
        var buttons = buttonPane();
        centerPane.getChildren().addAll(welcomeMessage, buttons, filePath);
    }

    private VBox initializeCenterPane() {
        var centerPane = new VBox();
        centerPane.setSpacing(SPACING);
        centerPane.setPadding(new Insets(PADDING));
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);
        return centerPane;
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

    public Button getOpenProject() {
        return openProject;
    }

    public Text getFilePath() {
        return filePath;
    }

}