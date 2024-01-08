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
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final int SPACING = 40;
    private static final int PADDING = 40;
    private static final int FONT_SIZE = 16;
    private final Main main;
    public Text filePathField;
    public final HomeController homeController = new HomeController(this);
    public HomeScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        var pane = getPane();
        getMenuBar(pane);
        getCenterPane(pane);
        getNavigationBar(pane);
        return new Scene(pane);
    }

    private void getMenuBar(BorderPane pane) {
        pane.setTop(MenuBar.update(main, SceneState.HOME));
    }

    private void getNavigationBar(BorderPane pane) {
        pane.setBottom(NavigationBar.update(main, SceneState.HOME));
    }

    private BorderPane getPane() {
        var pane = new BorderPane();
        pane.setPrefWidth(WIDTH);
        pane.setPrefHeight(HEIGHT);
        main.getNavigationButton().getNextButton().setVisible(false);
        return pane;
    }

    private void getCenterPane(BorderPane pane) {
        var centerPane = new VBox();
        centerPane.setSpacing(SPACING);
        centerPane.setPadding(new Insets(PADDING));
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        var welcomeMessageLabel = getWelcomeMessage();
        var projectButtonPane = getProjectPane();
        filePathField = new Text("");

        centerPane.getChildren().addAll(welcomeMessageLabel, projectButtonPane, filePathField);
    }

    private static Label getWelcomeMessage() {
        var welcomeMessageLabel = new Label("Welcome to Coordination_ORU!");
        welcomeMessageLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, FONT_SIZE));
        welcomeMessageLabel.setAlignment(Pos.CENTER);
        return welcomeMessageLabel;
    }

    private HBox getProjectPane() {
        var projectButtonPane = new HBox();
        projectButtonPane.setSpacing(SPACING);
        var newProjectButton = new Button("New Project");
        var openProjectButton = new Button("Open Project");
        projectButtonPane.getChildren().addAll(newProjectButton, openProjectButton);
        projectButtonPane.setAlignment(Pos.CENTER);
        newProjectButton.setOnAction(e -> homeController.newProject());
        openProjectButton.setOnAction(e -> homeController.openProject());
        return projectButtonPane;
    }

    public Main getMain() {
        return main;
    }

}