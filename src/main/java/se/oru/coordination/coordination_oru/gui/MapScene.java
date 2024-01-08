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

public class MapScene {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    public static final int SPACING = 10;
    private final Main main;
    public final MapController mapController = new MapController(this);

    public MapScene(Main main) {
        this.main = main;
    }

    public Scene get() {

        var root = new BorderPane();
        root.setPrefWidth(WIDTH);
        root.setPrefHeight(HEIGHT);

        //Navigation Bar
        root.setBottom(NavigationBar.update(main.getNavigationButton().getBackButton(), main.getNavigationButton().getNextButton()));
        //Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(main));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableRunProject();

        // Right Pane - Map Locations
        var rightPane = new VBox();
        rightPane.setSpacing(SPACING);
        rightPane.setAlignment(Pos.CENTER);
//        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        rightPane.setPadding(new Insets(10, 20, 10, 20));

        var locationButtons = new HBox();
        locationButtons.setSpacing(SPACING);
        locationButtons.setAlignment(Pos.CENTER);
        var addLocationButton = new Button("Add Location");
        addLocationButton.setDisable(true);
        var deleteLocationButton = new Button("Delete Location");
        deleteLocationButton.setDisable(true);
        locationButtons.getChildren().addAll(addLocationButton, deleteLocationButton);

        var locationsLabel = new Label("List of Locations: ");
        locationsLabel.setAlignment(Pos.CENTER);
        ListView<String> locationsListView = new ListView<String>();
        locationsListView.setPrefWidth(230);
        locationsListView.setPrefHeight(820);
        main.getDataStatus().getProjectData().getPoses().forEach((key, value) -> locationsListView.getItems().add(key));
        locationsListView.getSelectionModel().selectFirst();
        rightPane.getChildren().addAll(locationsLabel, locationsListView, locationButtons);

        mapController.addLocationClicked(addLocationButton, locationsListView);
        mapController.deleteLocationClicked(deleteLocationButton, locationsListView);
        root.setRight(rightPane);

        // Center Pane
        if (main.getDataStatus().isNewProject()) {
            var centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER);
            root.setCenter(centerPane);
            centerPane.setSpacing(30);
            centerPane.setPadding(new Insets(30, 10, 30, 10));
//        BorderPane.setMargin(centerPane, new Insets(20, 0, 20, 0));
            var mapMessageText = new Text("Select a map to load: ");
            var mapBrowseButton = new Button("Browse...");
            centerPane.getChildren().addAll(mapMessageText, mapBrowseButton);

            main.getNavigationButton().getNextButton().setDisable(true);
            mapController.browseClicked(mapBrowseButton, locationsListView, root, addLocationButton, deleteLocationButton);

        } else {
            addLocationButton.setDisable(false);
            deleteLocationButton.setDisable(false);
            var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(main.getDataStatus().getProjectData(), main.getDataStatus().getMapData(), locationsListView, main.getNavigationButton().getNextButton());
            root.setCenter(interactiveMapDisplay.createMapInteractionNode());
        }
        return new Scene(root);
    }

    public Main getMain() {
        return main;
    }
}