package se.oru.coordination.coordination_oru.gui;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import se.oru.coordination.coordination_oru.utils.Heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoordinationController {
    private final CoordinationScene scene;

    public CoordinationController(CoordinationScene scene) {
        this.scene = scene;
    }

    public void chooseHeuristic() {
        var heuristic = scene.getPriorityRuleField().getValue();
        if (heuristic != null) {
            switch (heuristic) {
                case "MOST_DISTANCE_TRAVELLED_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TRAVELLED_FIRST));
                    break;
                case "MOST_DISTANCE_TO_TRAVEL_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TO_TRAVEL_FIRST));
                    break;
                case "RANDOM":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.RANDOM));
                    break;
                case "HIGHEST_PRIORITY_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST));
                    break;
                case "HUMAN_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST));
                    break;
                case "AUTONOMOUS_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST));
                    break;
                case "BIGGER_VEHICLE_FIRST":
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.BIGGER_VEHICLE_FIRST));
                    break;
                default:
                    scene.getMain().getDataStatus().setHeuristics(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST));
                    break;
            }
        }
    }

    public void chooseTrafficControl() {
        var trafficControl = scene.getTrafficControlField().getValue();
        scene.getMain().getDataStatus().getProjectData().setTrafficControl(trafficControl);
        if (trafficControl == null) scene.getMain().getDataStatus().getProjectData().setTrafficControl("Mixed Traffic");
        updateVisibilityBasedOnTrafficControl();
    }

    public void chooseTransientHeuristic() {
        var heuristic = scene.getTemporaryPriorityRuleField().getValue();
        if (heuristic != null) {
            switch (heuristic) {
                case "MOST_DISTANCE_TRAVELLED_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TRAVELLED_FIRST));
                    break;
                case "MOST_DISTANCE_TO_TRAVEL_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TO_TRAVEL_FIRST));
                    break;
                case "RANDOM":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.RANDOM));
                    break;
                case "HIGHEST_PRIORITY_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST));
                    break;
                case "HUMAN_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST));
                    break;
                case "AUTONOMOUS_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST));
                    break;
                case "BIGGER_VEHICLE_FIRST":
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.BIGGER_VEHICLE_FIRST));
                    break;
                default:
                    scene.getMain().getDataStatus().setTransientHeuristics(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST));
                    break;
            }
        }
    }

    public void addTrigger() {
        TriggerDialog.add(scene);
    }

    public void deleteTrigger() {
        ListView<String> triggerList = scene.getTriggerList();
        ObservableList<Integer> selectedIndices = triggerList.getSelectionModel().getSelectedIndices();
        if (!selectedIndices.isEmpty()) {
            int indexToRemove = selectedIndices.get(0);
            triggerList.getItems().remove(indexToRemove);
            updateProjectDataTriggers();
        }
    }

    private void updateProjectDataTriggers() {
        ObservableList<String> triggerItems = scene.getTriggerList().getItems();
        List<ProjectData.Trigger> triggers = new ArrayList<>();
        for (String item : triggerItems) {
            // Parse the item string to reconstruct the Trigger object
            String[] parts = item.split(", ");
            String vehicle = parts[0].substring(1);
            List<String> task = Arrays.asList(parts[1].replace("[", "").replace("]", "").split(", "));
            List<String> vehicleToComply = Arrays.asList(parts[2].replace("[", "").replace("]", "").split(", "));
            ProjectData.Trigger trigger = new ProjectData.Trigger();
            trigger.setVehicle(vehicle);
            trigger.setTask(task);
            trigger.setVehicleToComply(vehicleToComply);
            triggers.add(trigger);
        }
        scene.getMain().getDataStatus().getProjectData().setTriggers(triggers);
    }

    public void updateVisibilityBasedOnTrafficControl() {
        var trafficControl = scene.getTrafficControlField().getValue();
        if (trafficControl == null) trafficControl = "Mixed Traffic";

        switch (trafficControl) {
            case "Mixed Traffic":
                scene.getTemporaryVelocityField().setVisible(false);
                scene.getTemporaryPriorityRuleField().setVisible(false);
                scene.getTemporaryVelocity().setVisible(false);
                scene.getTemporaryPriorityRule().setVisible(false);
                scene.getTriggerField().setVisible(false);
                scene.getTrigger().setVisible(false);
                break;
            case "Velocity Adaptation":
                scene.getTemporaryVelocityField().setVisible(true);
                scene.getTemporaryPriorityRuleField().setVisible(false);
                scene.getTemporaryVelocity().setVisible(true);
                scene.getTemporaryPriorityRule().setVisible(false);
                scene.getTriggerField().setVisible(true);
                scene.getTrigger().setVisible(true);
                break;
            case "Priority Rule Adaptation":
                scene.getTemporaryVelocityField().setVisible(false);
                scene.getTemporaryPriorityRuleField().setVisible(true);
                scene.getTemporaryVelocity().setVisible(false);
                scene.getTemporaryPriorityRule().setVisible(true);
                scene.getTriggerField().setVisible(true);
                scene.getTrigger().setVisible(true);
                break;
            case "Shutdown":
                scene.getTemporaryVelocityField().setVisible(false);
                scene.getTemporaryPriorityRuleField().setVisible(false);
                scene.getTemporaryVelocity().setVisible(false);
                scene.getTemporaryPriorityRule().setVisible(false);
                scene.getTriggerField().setVisible(true);
                scene.getTrigger().setVisible(true);
                break;
            default:
                scene.getTemporaryVelocityField().setVisible(true);
                scene.getTemporaryPriorityRuleField().setVisible(true);
                scene.getTemporaryVelocity().setVisible(true);
                scene.getTemporaryPriorityRule().setVisible(true);
                scene.getTriggerField().setVisible(true);
                scene.getTrigger().setVisible(true);
                break;
        }
    }
}
