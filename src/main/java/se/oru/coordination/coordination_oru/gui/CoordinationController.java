package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.utils.Heuristics;

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
        scene.getMain().getDataStatus().setTrafficControl(trafficControl);

        // Disable Control Fields
        boolean isMixedTraffic = "Mixed Traffic".equals(trafficControl);
        boolean isVehicleStoppage = "Vehicle Stoppage".equals(trafficControl);
        boolean isVehicleSpeedChange = "Vehicle Speed Change".equals(trafficControl);
        boolean isPriorityRuleChange = "Priority Rule Change".equals(trafficControl);

        scene.getTriggerVehicleField().setDisable(isMixedTraffic);
        scene.getTriggerTasksField().setDisable(isMixedTraffic);
        scene.getTriggerVelocityRatioField().setDisable(isMixedTraffic || isVehicleStoppage || isPriorityRuleChange);
        scene.getNewPriorityRuleField().setDisable(isMixedTraffic || isVehicleStoppage || isVehicleSpeedChange);
    }

    public void chooseTriggerVehicle() {
        var selectedVehicle = scene.getTriggerVehicleField().getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            DataStatus.TriggerVehicleData data = scene.getMain().getDataStatus().getTriggerVehicleData(selectedVehicle);
            if (data == null) {
                data = scene.getMain().getDataStatus().new TriggerVehicleData(selectedVehicle);
                scene.getMain().getDataStatus().addTriggerVehicleData(data);
            }
            scene.loadTriggerVehicleData(data);
        }
    }

    public void saveTriggerVehicleData() {
        var selectedVehicle = scene.getTriggerVehicleField().getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            DataStatus.TriggerVehicleData data = scene.getMain().getDataStatus().getTriggerVehicleData(selectedVehicle);
            if (data != null) {
                data.setTriggerMissions(scene.getSelectedTaskIndices());
                data.setTriggerVelocityRatio(scene.getTriggerVelocityRatioField().getText());
            }
        }
    }

    public void chooseTriggerVelocityRatio() {
        saveTriggerVehicleData();
    }

    public void chooseNewHeuristic() {
        var heuristic = scene.getNewPriorityRuleField().getValue();
        if (heuristic != null) {
            switch (heuristic) {
                case "MOST_DISTANCE_TRAVELLED_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TRAVELLED_FIRST));
                    break;
                case "MOST_DISTANCE_TO_TRAVEL_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.MOST_DISTANCE_TO_TRAVEL_FIRST));
                    break;
                case "RANDOM":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.RANDOM));
                    break;
                case "HIGHEST_PRIORITY_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.HIGHEST_PRIORITY_FIRST));
                    break;
                case "HUMAN_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.HUMAN_FIRST));
                    break;
                case "AUTONOMOUS_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.AUTONOMOUS_FIRST));
                    break;
                case "BIGGER_VEHICLE_FIRST":
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.BIGGER_VEHICLE_FIRST));
                    break;
                default:
                    scene.getMain().getDataStatus().setNewHeuristics(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST));
                    break;
            }
        }
    }
}
