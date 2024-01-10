package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;

public class ControllerMap {
    private final SceneMap scene;
    public ControllerMap(SceneMap scene) {
        this.scene = scene;
    }

    public void browseClicked() {
        scene.getBrowse().setOnAction(e -> {
            var file = Utils.chooseFile(scene.getMain(), "Select a map file to open: ", "yaml");
            if (file != null) {
                scene.getMain().getDataStatus().getProjectData().setMap(file.getAbsolutePath());
                scene.getMain().getDataStatus().setMapData(Utils.parseYAML(scene.getMain().getDataStatus().getProjectData().getMap()));
                var interactiveMapDisplay = new InteractiveMapDisplayWithMarkers(scene);
                scene.getPane().setCenter(interactiveMapDisplay.createMapInteractionNode());
                scene.getAddLocation().setDisable(false);
            }
        });
    }

    public void addLocationClicked() {
        scene.getAddLocation().setOnAction(e -> {
            var pose = new Pose(0, 0, 0);
            var addedPoseAsList = AddLocationDialogBox.display(pose.getX(), pose.getY());
            if (addedPoseAsList != null) {
                var addedPoseName = addedPoseAsList.get(0);
                var addedPoseX = Double.parseDouble(addedPoseAsList.get(2));
                var addedPoseY = Double.parseDouble(addedPoseAsList.get(3));
                var addedPoseTheta = Utils.getOrientation(addedPoseAsList.get(1));
                var addedPose = new Pose(addedPoseX, addedPoseY, addedPoseTheta);
                scene.getMain().getDataStatus().getProjectData().getPoses().put(addedPoseName, addedPose);
                scene.getLocations().getItems().add(addedPoseName);
                scene.getLocations().getSelectionModel().select(addedPoseName);
                updateLocations();
            }
        });
    }

    public void updateLocations() { //FIXME: This method should be called if interactive map is updated
        scene.getLocations().getItems().clear();
        scene.getMain().getDataStatus().getProjectData().getPoses().forEach((key, value) -> scene.getLocations().getItems().add(key));
        scene.getLocations().getSelectionModel().selectFirst();
        verifyNext();
        verifyDeletion();
    }

    private void verifyNext() {
        int minPoses = 2;
        scene.getMain().getNavigationButton().getNextButton().setDisable(scene.getLocations().getItems().size() < minPoses);
    }

    private void verifyDeletion() {
        scene.getDeleteLocation().setDisable(scene.getLocations().getItems().isEmpty());
    }

    public void deleteLocationClicked() {
        scene.getDeleteLocation().setOnAction(e -> {
            scene.getMain().getDataStatus().getProjectData().getPoses().remove(scene.getLocations().getSelectionModel().getSelectedItem());
            scene.getLocations().getItems().remove(scene.getLocations().getSelectionModel().getSelectedItem());
            scene.getLocations().getSelectionModel().selectFirst();
            updateLocations();
        });
    }
}