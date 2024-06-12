package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Objects;

public class MapScene {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int SPACING = 10;
    private ListView<String> locations = new ListView<>();
    private Button addLocation = new Button();
    private Button deleteLocation = new Button();
    private Button browse = new Button();
    private BorderPane pane;
    private final Main main;
    public final MapController controller = new MapController(this);

    public MapScene(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = initializePane();
        menuBar();
        navigationBar();
        rightPane();
        centerPane();
        controllers();
        return new Scene(pane);
    }

    private void controllers() {
        addLocation.setOnAction(e -> controller.clickAddLocation());
        deleteLocation.setOnAction(e -> controller.clickDeleteLocation());
        browse.setOnAction(e -> controller.clickBrowse());
        controller.updateLocations();
        locations.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) controller.doubleCLickLocation();
        });
    }

    private void centerPane() {
        if (Objects.equals(main.getDataStatus().getMapData().getImage(), "")) {
            var centerPane = initializeCenterPane();
            var message = new Text("Select a map to load: ");
            browse = new Button("Browse...");
            centerPane.getChildren().addAll(message, browse);

            main.getNavigationButton().getNext().setDisable(true);

        } else {
            addLocation.setDisable(false);
            deleteLocation.setDisable(false);
            var interactiveMapDisplay = new InteractiveMap(main, controller);
            pane.setCenter(interactiveMapDisplay.createInteractiveMapNode());
        }
    }

    private VBox initializeCenterPane() {
        var centerPane = new VBox();
        centerPane.setAlignment(Pos.CENTER);
        pane.setCenter(centerPane);
        centerPane.setSpacing(30);
        centerPane.setPadding(new Insets(30, 10, 30, 10));
        return centerPane;
    }

    private BorderPane initializePane() {
        pane = new BorderPane();
        pane.setPrefWidth(WIDTH);
        pane.setPrefHeight(HEIGHT);
        return pane;
    }

    private void rightPane() {
        var rightPane = initializeRightPane();
        var label = label();
        locations = locations();
        addLocation = addLocation();
        deleteLocation = deleteLocation();
        var locationButtons = setupButtons();
        rightPane.getChildren().addAll(label, locations, locationButtons);
        pane.setRight(rightPane);
    }

    private VBox initializeRightPane() {
        var rightPane = new VBox();
        rightPane.setSpacing(SPACING);
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setPadding(new Insets(10, 20, 10, 20));
        return rightPane;
    }

    private HBox setupButtons() {
        var locationButtons = new HBox();
        locationButtons.setSpacing(SPACING);
        locationButtons.setAlignment(Pos.CENTER);
        locationButtons.getChildren().addAll(addLocation, deleteLocation);
        return locationButtons;
    }

    private Button deleteLocation() {
        deleteLocation = new Button("Delete Location");
        deleteLocation.setDisable(true);
        return deleteLocation;
    }

    private Button addLocation() {
        addLocation = new Button("Add Location");
        addLocation.setDisable(true);
        return addLocation;
    }

    private ListView<String> locations() {
        locations.setPrefWidth(230);
        locations.setPrefHeight(820);
        return locations;
    }

    private Label label() {
        var locationsLabel = new Label("List of Locations: ");
        locationsLabel.setAlignment(Pos.CENTER);
        return locationsLabel;
    }

    private void menuBar() {
        pane.setTop(MenuBar.update(main, SceneState.MAP));
    }

    private void navigationBar() {
        pane.setBottom(NavigationBar.getBar(main, SceneState.MAP));
    }

    public Main getMain() {
        return main;
    }

    public BorderPane getPane() {
        return pane;
    }

    public Button getAddLocation() {
        return addLocation;
    }

    public Button getDeleteLocation() {
        return deleteLocation;
    }

    public ListView<String> getLocations() {
        return locations;
    }

}