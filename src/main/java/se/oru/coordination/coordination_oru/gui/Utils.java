package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.yaml.snakeyaml.Yaml;
import se.oru.coordination.coordination_oru.gui.ProjectData.MissionStep;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    protected static Boolean validateDouble(TextField textField) {
        try {
            Double.parseDouble(textField.getText());
            textField.setStyle("-fx-border-color: green");
            return true;
        } catch (NumberFormatException e) {
            textField.setStyle("-fx-border-color: red");
            return false;
        }
    }

    protected static Boolean validateInteger(TextField textField) {
        try {
            Integer.parseInt(textField.getText());
            textField.setStyle("-fx-border-color: green");
            return true;
        } catch (NumberFormatException e) {
            textField.setStyle("-fx-border-color: red");
            return false;
        }
    }

    protected static Color stringToColor(String colorStr) {
        if (colorStr == null) {
            return Color.BLACK; // Default color or null, depending on your preference
        }

        // Convert the string to uppercase to match the enum constant naming convention
        String colorUpper = colorStr.toUpperCase();

        // Match the string to the corresponding color constant
        switch (colorUpper) {
            case "YELLOW":
                return Color.YELLOW;
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
                throw new IllegalArgumentException("Unknown color: " + colorStr);
        }
    }

    protected static void listCentering(ListView<String> listView) {
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    protected static void listCentering(ComboBox<String> comboBox) {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setContentDisplay(ContentDisplay.CENTER);
                }
            }
        });
    }

    protected static ImageView getImageView(GUI gui) {
        String imageFile = gui.projectData.getMap();
        String imagePath = String.join("/", Arrays.asList(imageFile.split("/")).subList(0,
                imageFile.split("/").length - 1)) + "/" + gui.mapData.getImage();
        Image mapImage = new Image("file:" + imagePath);

        // Set the preferred dimensions for the image
        double preferredWidth = 680; // you can set this value to whatever width you want
        double preferredHeight = 518; // you can set this value to whatever height you want
        ImageView imageView = new ImageView(mapImage);
        imageView.setFitWidth(preferredWidth);
        imageView.setFitHeight(preferredHeight);
        imageView.setPreserveRatio(true); // This will keep the image's aspect ratio
        return imageView;
    }

    protected static void getPoses(GUI gui, ComboBox<String> Poses) {
        for (String pose : gui.projectData.getPoses().keySet()) {
            Poses.getItems().add(pose);
        }
    }

    protected static File fileChooser(GUI gui, String dialogTitle, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters().clear();
        String extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));

        return fileChooser.showOpenDialog(gui.stage);
    }

    protected static File fileCreator(GUI gui, String dialogTitle, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(dialogTitle);
        fileChooser.getExtensionFilters().clear();
        String extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));

        File file = fileChooser.showSaveDialog(gui.stage);

        if (file != null) {
            String fileExtension = "." + extension;
            if (!file.getName().endsWith(fileExtension)) {
                file = new File(file.getAbsolutePath() + fileExtension);
            }
        }
        return file;
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
            posesMap.put(entry.getKey(), deserializePose(entry.getValue()));
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
            ProjectData.Vehicle vehicle = null;
            try {
                vehicle = objectMapper.treeToValue(vehicleObject, ProjectData.Vehicle.class);
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

    protected static Pose deserializePose(JsonNode poseNode) {
        double x = poseNode.path("x").asDouble();
        double y = poseNode.path("y").asDouble();
        double angle = poseNode.path("angle").asDouble();

        return new Pose(x, y, angle);
    }
}
