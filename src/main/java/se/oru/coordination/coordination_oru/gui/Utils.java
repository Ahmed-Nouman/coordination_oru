package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.yaml.snakeyaml.Yaml;
import se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;
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

        // Convert the string to uppercase to match the enum constant naming convention
        String colorUpper = colorStr.toUpperCase();

        // Match the string to the corresponding color constant
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

    protected static File chooseFile(GUI gui, String dialogTitle, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters().clear();
        String extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));
        return fileChooser.showOpenDialog(gui.primaryStage);
    }

    protected static File createFile(GUI gui, String defaultFileName, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().clear();
        String extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));
        File selectedFile = fileChooser.showSaveDialog(gui.primaryStage);
        if (selectedFile != null) {
            String fileExtension = "." + extension;
            if (!selectedFile.getName().endsWith(fileExtension)) {
               selectedFile = new File(selectedFile.getAbsolutePath() + fileExtension);
            }
        }
        return selectedFile;
    }

    protected static MapData parseYAML(String filenameYAML) {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(filenameYAML)) {
            return yaml.loadAs(in, MapData.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing YAML file: " + filenameYAML, e);
        }
    }

    protected static ProjectData parseJSON(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode rootNode = objectMapper.readTree(new File(filePath));

        JsonNode vehiclesNode = rootNode.path("vehicles");
        JsonNode posesNode = rootNode.path("poses");

        // Map for storing pose names and their corresponding Pose objects
        Map<String, Pose> posesMap = new HashMap<>();
        posesNode.fields().forEachRemaining(entry -> {
            Pose pose = new Pose(entry.getValue().path("x").asDouble(),
                                 entry.getValue().path("y").asDouble(),
                                 entry.getValue().path("theta").asDouble());
            posesMap.put(entry.getKey(), pose);
        });

        List<ProjectData.Vehicle> vehicles = new ArrayList<>();
        vehiclesNode.forEach(vehicleNode -> {
            ObjectNode vehicleObject = (ObjectNode) vehicleNode;

            // Handle mission
            JsonNode missionNode = vehicleObject.get("mission");
            List<MissionStep> mission = new ArrayList<>();
            missionNode.forEach(missionStepNode -> {
                String poseName = missionStepNode.get(0).asText();
                double duration = missionStepNode.get(1).asDouble();
                MissionStep missionStep = new MissionStep();
                missionStep.setPoseName(poseName);
                missionStep.setDuration(duration);
                mission.add(missionStep);
            });
            vehicleObject.remove("mission");

            // Deserialize the vehicle
            Vehicle vehicle = null;
            try {
                vehicle = objectMapper.treeToValue(vehicleObject, Vehicle.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            vehicle.setMission(mission);

            vehicles.add(vehicle);
        });

        ProjectData projectData = new ProjectData();
        projectData.setMap(rootNode.path("map").asText());
        projectData.setVehicles(vehicles);
        projectData.setPoses(posesMap);

        return projectData;
    }

    protected static void writeJSON(ProjectData projectData, String projectFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        JsonSerializer<Object> serializer = new JsonSerializer<>() {
            @Override
            public void serialize(Object object, JsonGenerator GSON, SerializerProvider serializers) throws IOException {
                if (object instanceof Vehicle) {
                    GSON.writeStartObject();
                    // Serialize other Vehicle fields
                    GSON.writeStringField("ID", String.valueOf(((Vehicle) object).getID()));
                    GSON.writeStringField("name", ((Vehicle) object).getName());
                    GSON.writeStringField("type", ((Vehicle) object).getType());
                    GSON.writeStringField("lookAheadDistance", String.valueOf(((Vehicle) object).getLookAheadDistance()));
                    GSON.writeStringField("color", ((Vehicle) object).getColor());
                    GSON.writeStringField("maxVelocity", String.valueOf(((Vehicle) object).getMaxVelocity()));
                    GSON.writeStringField("maxAcceleration", String.valueOf(((Vehicle) object).getMaxAcceleration()));
                    GSON.writeStringField("length", String.valueOf(((Vehicle) object).getLength()));
                    GSON.writeStringField("width", String.valueOf(((Vehicle) object).getWidth()));
                    GSON.writeStringField("initialPose", ((Vehicle) object).getInitialPose());
                    GSON.writeArrayFieldStart("mission");
                    for (MissionStep missionStep : ((Vehicle) object).getMission()) {
                        GSON.writeStartArray();
                        GSON.writeString(missionStep.getPoseName());
                        GSON.writeNumber(missionStep.getDuration());
                        GSON.writeEndArray();
                    }
                    GSON.writeEndArray();
                    GSON.writeStringField("safetyDistance", String.valueOf(((Vehicle) object).getSafetyDistance()));
                    GSON.writeStringField("missionRepetition", String.valueOf(((Vehicle) object).getMissionRepetition()));
                    GSON.writeEndObject();
                } else if (object instanceof Pose) {
                    // Serialize Pose as an object
                    GSON.writeStartObject();
                    GSON.writeNumberField("x", ((Pose) object).getX());
                    GSON.writeNumberField("y", ((Pose) object).getY());
                    GSON.writeNumberField("theta", ((Pose) object).getTheta());
                    GSON.writeEndObject();
                }
            }
        };

        module.addSerializer(Vehicle.class, serializer);
        module.addSerializer(Pose.class, serializer);
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Write to the json project file
        try (FileWriter fileWriter = new FileWriter(projectFile)) {
            fileWriter.write(mapper.writeValueAsString(projectData));
        } catch (IOException ex) {
            System.out.println(("Error: Could not save the file."));
        }
    }

    public static <T extends Serializable> T deepCopy(T object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            T copy = (T) objectInputStream.readObject();
            objectInputStream.close();

            return copy;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
