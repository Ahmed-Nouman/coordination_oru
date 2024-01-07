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
import org.metacsp.multi.spatioTemporal.paths.Pose;

import java.io.File;

public class MapScene {
    private final se.oru.coordination.coordination_oru.gui.GUI GUI;

    public MapScene(se.oru.coordination.coordination_oru.gui.GUI GUI) {
        this.GUI = GUI;
    }

    public Scene get() {

        var root = new BorderPane();
        root.setPrefWidth(1200);
        root.setPrefHeight(800);

        // Top Pane - Menu Bar
        root.setTop(GUIMenuBar.getMenuBar(GUI));
        GUIMenuBar.disableNewProject();
        GUIMenuBar.disableOpenProject();
        GUIMenuBar.disableSaveProject();
        GUIMenuBar.disableRunProject();

        // Right Pane - Map Locations
        var rightPane = new VBox();
        rightPane.setSpacing(10);
        rightPane.setAlignment(Pos.CENTER);
//        BorderPane.setMargin(rightPane, new Insets(10, 10, 10, 0));
        rightPane.setPadding(new Insets(10, 20, 10, 20));

        HBox locationButtons = new HBox();
        locationButtons.setSpacing(10);
        locationButtons.setAlignment(Pos.CENTER);
        Button addLocationButton = new Button("Add Location");
        addLocationButton.setDisable(true);
        Button deleteLocationButton = new Button("Delete Location");
        deleteLocationButton.setDisable(true);
        locationButtons.getChildren().addAll(addLocationButton, deleteLocationButton);

        Label locationsLabel = new Label("List of Locations: ");
        locationsLabel.setAlignment(Pos.CENTER);
        ListView<String> locationsListView = new ListView<String>();
        locationsListView.setPrefWidth(230);
        locationsListView.setPrefHeight(820);
        GUI.getDataStatus().getProjectData().getPoses().forEach((key, value) -> locationsListView.getItems().add(key));
        locationsListView.getSelectionModel().selectFirst();
        rightPane.getChildren().addAll(locationsLabel, locationsListView, locationButtons);

        addLocationButton.setOnAction(e -> {
            var pose = new Pose(0, 0, 0);
            var addedPoseAsList = AddLocationDialogBox.display(pose.getX(), pose.getY());
            if (addedPoseAsList != null) {
                String addedPoseName = addedPoseAsList.get(0);
                double addedPoseX = Double.parseDouble(addedPoseAsList.get(2));
                double addedPoseY = Double.parseDouble(addedPoseAsList.get(3));
                double addedPoseTheta = Utils.getOrientation(addedPoseAsList.get(1));
                Pose addedPose = new Pose(addedPoseX, addedPoseY, addedPoseTheta);
                GUI.getDataStatus().getProjectData().getPoses().put(addedPoseName, addedPose);
                locationsListView.getItems().add(addedPoseName);
                locationsListView.getSelectionModel().select(addedPoseName);
            }
            checkSizePoses(GUI);
        });

        deleteLocationButton.setOnAction(e -> {
            GUI.getDataStatus().getProjectData().getPoses().remove(locationsListView.getSelectionModel().getSelectedItem());
            locationsListView.getItems().remove(locationsListView.getSelectionModel().getSelectedItem());
            checkSizePoses(GUI);
        });

        root.setRight(rightPane);

        // Center Pane
        if (GUI.getDataStatus().isNewProject()) {
            var centerPane = new VBox();
            centerPane.setAlignment(Pos.CENTER);
            root.setCenter(centerPane);
            centerPane.setSpacing(30);
            centerPane.setPadding(new Insets(30, 10, 30, 10));
//        BorderPane.setMargin(centerPane, new Insets(20, 0, 20, 0));
            GUI.getNavigationBar().getNextButton().setDisable(true);
            Text mapMessageText = new Text("Select a map to load: ");
            Button mapBrowseButton = new Button("Browse...");
            centerPane.getChildren().addAll(mapMessageText, mapBrowseButton);

            // Set action for browsing a map
            mapBrowseButton.setOnAction(e -> {
                File file = Utils.chooseFile(GUI, "Select a map file to open: ", "yaml");
                if (file != null) {
                    GUI.getDataStatus().getProjectData().setMap(file.getAbsolutePath());
                    GUI.getDataStatus().setMapData(Utils.parseYAML(GUI.getDataStatus().getProjectData().getMap()));
                    var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(GUI.getDataStatus().getProjectData(), GUI.getDataStatus().getMapData(), locationsListView, GUI.getNavigationBar().getNextButton());
                    root.setCenter(interactiveMapDisplay.createMapInteractionNode());
                    addLocationButton.setDisable(false);
                    deleteLocationButton.setDisable(false);
                }
            });

        } else {
            addLocationButton.setDisable(false);
            deleteLocationButton.setDisable(false);
            var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(GUI.getDataStatus().getProjectData(), GUI.getDataStatus().getMapData(), locationsListView, GUI.getNavigationBar().getNextButton());
            root.setCenter(interactiveMapDisplay.createMapInteractionNode());
        }

        // Bottom Pane - Navigation Buttons
        root.setBottom(BottomPane.getBottomPane(GUI.getNavigationBar().getBackButton(), GUI.getNavigationBar().getNextButton()));

        return new Scene(root);
    }

    private void checkSizePoses(GUI gui) {
        gui.getNavigationBar().getNextButton().setDisable(gui.getDataStatus().getProjectData().getPoses().size() < 2);
    }
}