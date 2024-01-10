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

public class SceneMap {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int SPACING = 10;
    private BorderPane pane;
    private ListView<String> locations = new ListView<>();
    private Button addLocation = new Button();
    private Button deleteLocation = new Button();
    private Button browse = new Button();
    private final Main main;
    public final ControllerMap controller = new ControllerMap(this);

    public SceneMap(Main main) {
        this.main = main;
    }

    public Scene get() {
        pane = initializePane();
        menuBar();
        navigationBar();
        rightPane();
        centerPane();

        controller.addLocationClicked();
        controller.deleteLocationClicked();
        return new Scene(pane);
    }

    private void centerPane() {
        if (main.getDataStatus().isNewProject()) {
            var centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER);
            pane.setCenter(centerPane);
            centerPane.setSpacing(30);
            centerPane.setPadding(new Insets(30, 10, 30, 10));
//        BorderPane.setMargin(centerPane, new Insets(20, 0, 20, 0));
            var message = message();
            browse = browse();
            centerPane.getChildren().addAll(message, browse);

            main.getNavigationButton().getNextButton().setDisable(true);
            controller.browseClicked();

        } else {
            addLocation.setDisable(false);
            deleteLocation.setDisable(false);
            var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(this);
            pane.setCenter(interactiveMapDisplay.createMapInteractionNode()); //FIXME: Fix arguments
        }
    }

    private Button browse() {
        return new Button("Browse...");
    }

    private Text message() {
        return new Text("Select a map to load: ");
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
//        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
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
        controller.updateLocations();
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
        pane.setBottom(NavigationBar.update(main, SceneState.MAP));
    }

    public Main getMain() {
        return main;
    }

    public BorderPane getPane() {
        return pane;
    }

    public void setPane(BorderPane pane) {
        this.pane = pane;
    }

    public Button getBrowse() {
        return browse;
    }

    public void setBrowse(Button browse) {
        this.browse = browse;
    }

    public Button getAddLocation() {
        return addLocation;
    }

    public void setAddLocation(Button addLocation) {
        this.addLocation = addLocation;
    }

    public Button getDeleteLocation() {
        return deleteLocation;
    }

    public void setDeleteLocation(Button deleteLocation) {
        this.deleteLocation = deleteLocation;
    }

    public ListView<String> getLocations() {
        return locations;
    }

    public void setLocations(ListView<String> locations) {
        this.locations = locations;
    }
}