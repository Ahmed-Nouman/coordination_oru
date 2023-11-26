package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.yaml.snakeyaml.Yaml;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    protected static void validateDouble(TextField textField) {
        try {
            Double.parseDouble(textField.getText());
            textField.setStyle("-fx-border-color: green");
        } catch (NumberFormatException e) {
            textField.setStyle("-fx-border-color: red");
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

    protected static void getVehicles(ListView<String> vehicles, ProjectData projectData) {
        vehicles.getItems().clear();
        for (AbstractVehicle vehicle : projectData.getVehicles()) {
            vehicles.getItems().add(vehicle.getName());
        }
    }

    protected static void updateVehiclesList(ListView<String> vehiclesList, BorderPane borderPane, Button addVehicleButton,
                                             Button deleteVehicleButton, ProjectData projectData,
                                             TextField nameField, TextField lengthField, TextField widthField,
                                             TextField maxVelocityField, TextField maxAccelerationField,
                                             TextField safetyDistanceField, ComboBox<String> colorField,
                                             ComboBox<String> initialPoseField, VBox goalPoseField,
                                             CheckBox isHumanField, Text lookAheadDistance,
                                             TextField lookAheadDistanceField) {
        VBox leftPane = new VBox();
        Text vehiclesText = new Text("Vehicles: ");
        vehiclesText.setFont(Font.font("System", FontWeight.BOLD, 12));
        getVehicles(vehiclesList, projectData);
        leftPane.setAlignment(Pos.CENTER);
        HBox buttons = new HBox(addVehicleButton, deleteVehicleButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        leftPane.getChildren().addAll(vehiclesText, vehiclesList, buttons);
        leftPane.setSpacing(10);
        borderPane.setLeft(leftPane);
        BorderPane.setMargin(leftPane, new Insets(0, 0, 0, 20));

        // Listener for list selection changes
        vehiclesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get the selected vehicle's details
                AbstractVehicle vehicle = projectData.getVehicle(newValue);

                // Update the fields in the centerPane with the details of the selected vehicle
                nameField.setText(newValue);
                lengthField.setText(String.valueOf(vehicle.getLength()));
                widthField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
//                colorField.setValue(vehicle.getColor());
//                initialPoseField.setValue(vehicle.getInitialPose());
//                if (vehicle.getGoalPoses() != null && vehicle.getGoalPoses().length > 0) {
//                    goalPoseField.setValue(vehicle.getGoalPoses()[0]);
//                }
//                isHumanField.setSelected(vehicle.getLookAheadDistance() > 0);
//                lookAheadDistance.setVisible(vehicle.getLookAheadDistance() > 0);
//                lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
//                lookAheadDistanceField.setVisible(vehicle.getLookAheadDistance() > 0);
            }
        });
        vehiclesList.getSelectionModel().selectFirst();
    }

    protected static ImageView getImageView(GUI gui) {
        String imageFile = gui.projectData.getMap();
        String imagePath = String.join("/", Arrays.asList(imageFile.split("/")).subList(0,
                imageFile.split("/").length - 1)) + "/" + gui.mapData.getImage();
        javafx.scene.image.Image mapImage = new Image("file:" + imagePath);

        // Set the preferred dimensions for the image
        double preferredWidth = 800; // you can set this value to whatever width you want
        double preferredHeight = 640; // you can set this value to whatever height you want
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

    static File fileChooser(GUI gui, String dialogTitle, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters().clear();
        String extensionDescription = extension.toUpperCase() + " Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, "*." + extension));

        return fileChooser.showOpenDialog(gui.stage);
    }

    static File fileCreator(GUI gui, String dialogTitle, String extension) {
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
        // Configure ObjectMapper to not fail on unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode rootNode = objectMapper.readTree(new File(filePath));

        JsonNode vehiclesNode = rootNode.path("vehicles");
        JsonNode posesNode = rootNode.path("poses");

        Map<String, Pose> posesMap = new HashMap<>();
        posesNode.fields().forEachRemaining(entry -> {
            posesMap.put(entry.getKey(), deserializePose(entry.getValue()));
        });

        ArrayList<AbstractVehicle> vehicles = new ArrayList<>();
        vehiclesNode.forEach(vehicleNode -> {
            // Temporarily convert to a tree to manually handle 'initialPose'
            ObjectNode vehicleObject = (ObjectNode) vehicleNode;
            String initialPoseName = vehicleObject.get("initialPose").asText();
            Pose initialPose = posesMap.get(initialPoseName);

            // Remove 'initialPose' field so Jackson doesn't try to deserialize it
            vehicleObject.remove("initialPose");

            // Now let Jackson deserialize the rest of the vehicle object
            AbstractVehicle vehicle = null;
            try {
                vehicle = objectMapper.treeToValue(vehicleObject, AbstractVehicle.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            // Set the initial pose to the vehicle
            vehicle.setInitialPose(initialPose);

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

        return new Pose(x, y, angle); // Assuming such a constructor exists
    }

}
