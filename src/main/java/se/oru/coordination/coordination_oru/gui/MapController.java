package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.metacsp.multi.spatioTemporal.paths.Pose;

public class MapController {

    private final MapScene mapScene;

    public MapController(MapScene mapScene) {
        this.mapScene = mapScene;
    }
    public void deleteLocationClicked(Button deleteLocationButton, ListView<String> locationsListView) {
        deleteLocationButton.setOnAction(e -> {
            mapScene.getMain().getDataStatus().getProjectData().getPoses().remove(locationsListView.getSelectionModel().getSelectedItem());
            locationsListView.getItems().remove(locationsListView.getSelectionModel().getSelectedItem());
            verifyIfNextAllowed(mapScene.getMain());
            verifyIfDeletionAllowed(locationsListView.getItems().size());
        });
    }

    public void verifyIfNextAllowed(Main main) {
        int minPoses = 2;
        main.getNavigationButton().getNextButton().setDisable(main.getDataStatus().getProjectData().getPoses().size() < minPoses);
    }

    private void verifyIfDeletionAllowed(int size) {
        mapScene.getMain().getNavigationButton().getNextButton().setDisable(size <= 0);
    }

    public void addLocationClicked(Button addLocationButton, ListView<String> locationsListView) {
        addLocationButton.setOnAction(e -> {
            var pose = new Pose(0, 0, 0);
            var addedPoseAsList = AddLocationDialogBox.display(pose.getX(), pose.getY());
            if (addedPoseAsList != null) {
                var addedPoseName = addedPoseAsList.get(0);
                var addedPoseX = Double.parseDouble(addedPoseAsList.get(2));
                var addedPoseY = Double.parseDouble(addedPoseAsList.get(3));
                var addedPoseTheta = Utils.getOrientation(addedPoseAsList.get(1));
                var addedPose = new Pose(addedPoseX, addedPoseY, addedPoseTheta);
                mapScene.getMain().getDataStatus().getProjectData().getPoses().put(addedPoseName, addedPose);
                locationsListView.getItems().add(addedPoseName);
                locationsListView.getSelectionModel().select(addedPoseName);
            }
            verifyIfNextAllowed(mapScene.getMain());
            verifyIfDeletionAllowed(locationsListView.getItems().size());

        });
    }

    public void browseClicked(Button mapBrowseButton, ListView<String> locationsListView, BorderPane root, Button addLocationButton, Button deleteLocationButton) {
        mapBrowseButton.setOnAction(e -> {
            var file = Utils.chooseFile(mapScene.getMain(), "Select a map file to open: ", "yaml");
            if (file != null) {
                mapScene.getMain().getDataStatus().getProjectData().setMap(file.getAbsolutePath());
                mapScene.getMain().getDataStatus().setMapData(Utils.parseYAML(mapScene.getMain().getDataStatus().getProjectData().getMap()));
                var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(mapScene.getMain().getDataStatus().getProjectData(),
                        mapScene.getMain().getDataStatus().getMapData(), locationsListView, mapScene.getMain().getNavigationButton().getNextButton());
                root.setCenter(interactiveMapDisplay.createMapInteractionNode());
                addLocationButton.setDisable(false);
                deleteLocationButton.setDisable(false);
            }
        });
    }
}