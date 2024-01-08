package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    public final Text filePathField = new Text("");
    public final HomeController homeController = new HomeController(this);
    public HomeScene(Main main) {
        this.main = main;
    }

    public Scene get() {

        var root = new BorderPane();
        root.setPrefWidth(WIDTH);
        root.setPrefHeight(HEIGHT);
        main.getNavigationButton().getNextButton().setVisible(false);

        //Navigation Bar
        root.setBottom(NavigationBar.update(main.getNavigationButton().getNextButton()));
        //Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(main));
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableCloseProject();
        GUIMenuBar.disableRunProject();

        // Center Pane
        var centerPane = new VBox();
        centerPane.setSpacing(SPACING);
        centerPane.setPadding(new Insets(PADDING));

        var welcomeMessageLabel = new Label("Welcome to Coordination_ORU!");
        welcomeMessageLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, FONT_SIZE));
        welcomeMessageLabel.setAlignment(Pos.CENTER);

        // Center - Button Pane
        var projectButtonPane = new HBox();
        projectButtonPane.setSpacing(SPACING);
        var newProjectButton = new Button("New Project");
        var openProjectButton = new Button("Open Project");
        projectButtonPane.getChildren().addAll(newProjectButton, openProjectButton);
        projectButtonPane.setAlignment(Pos.CENTER);

        centerPane.getChildren().addAll(welcomeMessageLabel, projectButtonPane, filePathField);
        centerPane.setAlignment(Pos.CENTER);

        // Set VBox children to grow equally
        VBox.setVgrow(projectButtonPane, Priority.ALWAYS);

        root.setCenter(centerPane);
        BorderPane.setAlignment(centerPane, Pos.CENTER);

        homeController.newProjectClicked(newProjectButton);
        homeController.openProjectClicked(openProjectButton);

        return new Scene(root);
    }

    public Main getMain() {
        return main;
    }

}