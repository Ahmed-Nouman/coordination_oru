package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.yaml.snakeyaml.Yaml;
import se.oru.coordination.coordination_oru.gui.ProjectData.TaskStep;
import se.oru.coordination.coordination_oru.gui.ProjectData.Vehicle;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    protected static Boolean validateDouble(TextField textField) {
        try {
            Double.parseDouble(textField.getText());
            textField.setStyle("-fx-border-color: none");
            return true;
        } catch (NumberFormatException e) {
            textField.setStyle("-fx-border-color: red");
            return false;
        }
    }

    protected static Boolean validateInteger(TextField textField) {
        try {
            Integer.parseInt(textField.getText());
            textField.setStyle("-fx-border-color: none");
            return true;
        } catch (NumberFormatException e) {
            textField.setStyle("-fx-border-color: red");
            return false;
        }
    }

    protected static Color stringToColor(String colorStr) {

        String colorUpper = colorStr.toUpperCase();

        switch (colorUpper) {
            case "RED":
                return Color.RED;
            case "BLUE":
                return Color.BLUE;
            case "GREEN":
                return Color.GREEN;
            case "BLACK":
                return Color.BLACK;
            case "WHITE":
                return Color.WHITE;
            case "CYAN":
                return Color.CYAN;
            case "ORANGE":
                return Color.ORANGE;
            default:
                return Color.YELLOW;
        }
    }

    protected static double getOrientation(String orientation) {
        double theta;
        switch (orientation) {
            case "DOWN":
                theta = 3 * Math.PI / 2;
                break;
            case "DOWN_RIGHT":
                theta = 7 * Math.PI / 4;
                break;
            case "DOWN_LEFT":
                theta = 5 * Math.PI / 4;
                break;
            case "LEFT":
                theta = Math.PI;
                break;
            case "UP_LEFT":
                theta = 3 * Math.PI / 4;
                break;
            case "UP":
                theta = Math.PI / 2;
                break;
            case "UP_RIGHT":
                theta = Math.PI / 4;
                break;
            default:
                theta = 0;
                break;
        }
        return theta;
    }

    protected static File chooseFile(Main main, String dialogTitle, String extension) {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters().clear();
        var extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));
        return fileChooser.showOpenDialog(main.getPrimaryStage());
    }

    protected static File createFile(Main main, String defaultFileName, String extension) {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().clear();
        var extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));
        File selectedFile = fileChooser.showSaveDialog(main.getPrimaryStage());
        if (selectedFile != null) {
            String fileExtension = "." + extension;
            if (!selectedFile.getName().endsWith(fileExtension)) {
               selectedFile = new File(selectedFile.getAbsolutePath() + fileExtension);
            }
        }
        return selectedFile;
    }

    protected static MapData parseYAML(String fileName) {
        var yaml = new Yaml();
        try (InputStream in = new FileInputStream(fileName)) {
            return yaml.loadAs(in, MapData.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing YAML file: " + fileName, e);
        }
    }

    protected static ProjectData parseJSON(String filePath) throws IOException {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var rootNode = objectMapper.readTree(new File(filePath));

        var vehiclesNode = rootNode.path("vehicles");
        var posesNode = rootNode.path("poses");

        // Map for storing pose names and their corresponding Pose objects
        Map<String, Pose> posesMap = new HashMap<>();
        posesNode.fields().forEachRemaining(entry -> {
            Pose pose = new Pose(entry.getValue().path("x").asDouble(),
                    entry.getValue().path("y").asDouble(),
                    entry.getValue().path("theta").asDouble());
            posesMap.put(entry.getKey(), pose);
        });

        List<ProjectData.Vehicle> vehicles = new ArrayList<>();
        for (JsonNode vehicleNode : vehiclesNode) {
            ObjectNode vehicleObject = (ObjectNode) vehicleNode;

            // Handle task
            JsonNode taskNode = vehicleObject.get("task");
            List<ProjectData.TaskStep> task = new ArrayList<>();
            taskNode.forEach(taskStepNode -> {
                String taskName = taskStepNode.get(0).asText();
                String poseName = taskStepNode.get(1).asText();
                double duration = taskStepNode.get(2).asDouble();
                int priority = taskStepNode.get(3).asInt();
                int repetition = taskStepNode.get(4).asInt();
                var taskStep = new ProjectData.TaskStep();
                taskStep.setTaskName(taskName);
                taskStep.setPoseName(poseName);
                taskStep.setDuration(duration);
                taskStep.setPriority(priority);
                taskStep.setRepetition(repetition);
                task.add(taskStep);
            });
            vehicleObject.remove("task");

            // Deserialize the vehicle
            ProjectData.Vehicle vehicle = null;
            try {
                vehicle = objectMapper.treeToValue(vehicleObject, ProjectData.Vehicle.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            vehicle.setTask(task);

            vehicles.add(vehicle);
        }

        var projectData = new ProjectData();
        projectData.setMap(rootNode.path("map").asText());
        projectData.setVehicles(vehicles);
        projectData.setPoses(posesMap);

        // Parse trafficControl
        projectData.setTrafficControl(rootNode.path("trafficControl").asText());

        // Parse triggers
        List<ProjectData.Trigger> triggers = new ArrayList<>();
        var triggersNode = rootNode.path("triggers");
        for (JsonNode triggerNode : triggersNode) {
            var trigger = new ProjectData.Trigger();
            trigger.setVehicle(triggerNode.path("vehicle").asText());
            List<String> taskList = new ArrayList<>();
            triggerNode.path("task").forEach(task -> taskList.add(task.asText()));
            trigger.setTask(taskList);
            List<String> vehicleToComplyList = new ArrayList<>();
            triggerNode.path("vehicleToComply").forEach(v -> vehicleToComplyList.add(v.asText()));
            trigger.setVehicleToComply(vehicleToComplyList);
            triggers.add(trigger);
        }
        projectData.setTriggers(triggers);
        return projectData;
    }

    protected static void writeJSON(ProjectData projectData, String projectFile) throws IOException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();

        JsonSerializer<Object> serializer = new JsonSerializer<>() {
            @Override
            public void serialize(Object object, JsonGenerator GSON, SerializerProvider serializers) throws IOException {
                if (object instanceof ProjectData.Vehicle) {
                    GSON.writeStartObject();
                    ProjectData.Vehicle vehicle = (ProjectData.Vehicle) object;
                    GSON.writeStringField("ID", String.valueOf(vehicle.getID()));
                    GSON.writeStringField("priority", String.valueOf(vehicle.getPriority()));
                    GSON.writeStringField("name", vehicle.getName());
                    GSON.writeStringField("type", vehicle.getType());
                    GSON.writeStringField("lookAheadDistance", String.valueOf(vehicle.getLookAheadDistance()));
                    GSON.writeStringField("color", vehicle.getColor());
                    GSON.writeStringField("maxVelocity", String.valueOf(vehicle.getMaxVelocity()));
                    GSON.writeStringField("maxAcceleration", String.valueOf(vehicle.getMaxAcceleration()));
                    GSON.writeStringField("length", String.valueOf(vehicle.getLength()));
                    GSON.writeStringField("width", String.valueOf(vehicle.getWidth()));
                    GSON.writeStringField("initialPose", vehicle.getInitialPose());
                    GSON.writeArrayFieldStart("task");
                    for (ProjectData.TaskStep taskStep : vehicle.getTasks()) {
                        GSON.writeStartArray();
                        GSON.writeString(taskStep.getTaskName());
                        GSON.writeString(taskStep.getPoseName());
                        GSON.writeNumber(taskStep.getDuration());
                        GSON.writeNumber(taskStep.getPriority());
                        GSON.writeNumber(taskStep.getRepetition());
                        GSON.writeEndArray();
                    }
                    GSON.writeEndArray();
                    GSON.writeStringField("safetyDistance", String.valueOf(vehicle.getSafetyDistance()));
                    GSON.writeStringField("taskRepetition", String.valueOf(vehicle.getTasksRepetition()));
                    GSON.writeEndObject();
                } else if (object instanceof Pose) {
                    Pose pose = (Pose) object;
                    GSON.writeStartObject();
                    GSON.writeNumberField("x", pose.getX());
                    GSON.writeNumberField("y", pose.getY());
                    GSON.writeNumberField("theta", pose.getTheta());
                    GSON.writeEndObject();
                } else if (object instanceof ProjectData.Trigger) {
                    ProjectData.Trigger trigger = (ProjectData.Trigger) object;
                    GSON.writeStartObject();
                    GSON.writeStringField("vehicle", trigger.getVehicle());
                    GSON.writeArrayFieldStart("task");
                    for (String task : trigger.getTask()) {
                        GSON.writeString(task);
                    }
                    GSON.writeEndArray();
                    GSON.writeArrayFieldStart("vehicleToComply");
                    for (String vehicle : trigger.getVehicleToComply()) {
                        GSON.writeString(vehicle);
                    }
                    GSON.writeEndArray();
                    GSON.writeEndObject();
                }
            }
        };

        module.addSerializer(ProjectData.Vehicle.class, serializer);
        module.addSerializer(Pose.class, serializer);
        module.addSerializer(ProjectData.Trigger.class, serializer);
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Write to the JSON project file
        try (FileWriter fileWriter = new FileWriter(projectFile)) {
            fileWriter.write(mapper.writeValueAsString(projectData));
        } catch (IOException ex) {
            System.out.println("Error: Could not save the file.");
        }
    }

    public static <T extends Serializable> T deepCopy(T object) {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();

            var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            var objectInputStream = new ObjectInputStream(byteArrayInputStream);
            T copy = (T) objectInputStream.readObject();
            objectInputStream.close();

            return copy;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
