package se.oru.coordination.coordination_oru.gui;

import org.metacsp.multi.spatioTemporal.paths.Pose;

public class MapController {
    private final MapScene scene;
    public MapController(MapScene scene) {
        this.scene = scene;
    }

    public void clickBrowse() {
        var file = Utils.chooseFile(scene.getMain(), "Select a map file to open: ", "yaml");
        if (file != null) {
            scene.getMain().getDataStatus().getProjectData().setMap(file.getAbsolutePath());
            scene.getMain().getDataStatus().setMapData(Utils.parseYAML(scene.getMain().getDataStatus().getProjectData().getMap()));
            var interactiveMapDisplay = new InteractiveMap(scene.getMain(), this);
            scene.getPane().setCenter(interactiveMapDisplay.createInteractiveMapNode());
            scene.getAddLocation().setDisable(false);
        }
    }

    public void clickAddLocation() {
        var pose = new Pose(0, 0, 0);
        var addPose = LocationDialog.add(pose.getX(), pose.getY());
        if (addPose != null) {
            var poseName = addPose.get(0);
            var orientation = Utils.getOrientation(addPose.get(1));
            var x = Double.parseDouble(addPose.get(2));
            var y = Double.parseDouble(addPose.get(3));
            var addedPose = new Pose(x, y, orientation);
            scene.getMain().getDataStatus().getProjectData().getPoses().put(poseName, addedPose);
            scene.getLocations().getItems().add(poseName);
            scene.getLocations().getSelectionModel().select(poseName);
            updateLocations();
        }
    }

    public void updateLocations() {
        scene.getLocations().getItems().clear();
        scene.getMain().getDataStatus().getProjectData().getPoses().forEach((key, value) -> scene.getLocations().getItems().add(key));
        scene.getLocations().getSelectionModel().selectFirst(); //FIXME: Remove clearing and set the location to last edited one. Maybe?
        verifyNext();
        verifyDeletion();
    }

    private void verifyNext() {
        int minPoses = 2;
        scene.getMain().getNavigationButton().getNext().setDisable(scene.getLocations().getItems().size() < minPoses);
    }

    private void verifyDeletion() {
        scene.getDeleteLocation().setDisable(scene.getLocations().getItems().isEmpty());
    }

    public void clickDeleteLocation() {
        scene.getMain().getDataStatus().getProjectData().getPoses().remove(scene.getLocations().getSelectionModel().getSelectedItem());
        scene.getLocations().getItems().remove(scene.getLocations().getSelectionModel().getSelectedItem());
        scene.getLocations().getSelectionModel().selectFirst();
        updateLocations();
    }

    public void getPoseList(InteractiveMap interactiveMap) {
        scene.getLocations().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            UpdateMapImage.drawMapMarkersWithSelection(interactiveMap, newValue);
        });
    }

    public void addPose(String poseName) {
        scene.getLocations().getItems().add(poseName);
        scene.getLocations().getSelectionModel().select(poseName);
    }

    public void doubleCLickLocation() {
        var poseName = scene.getLocations().getSelectionModel().getSelectedItem();
        var pose = LocationDialog.edit(scene, poseName);
        if (pose != null) {
            scene.getMain().getDataStatus().getProjectData().getPoses().remove(poseName);
            var newPoseName = pose.get(0);
            var theta = Math.toRadians(Double.parseDouble(pose.get(1)));
            var x = Double.parseDouble(pose.get(2));
            var y = Double.parseDouble(pose.get(3));
            var newPose = new Pose(x, y, theta);
            scene.getMain().getDataStatus().getProjectData().getPoses().put(newPoseName, newPose);
            updateLocations();
        }
    }
}