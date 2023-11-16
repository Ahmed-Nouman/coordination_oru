package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.gui_JavaFX.ProjectData.Vehicle;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

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

    protected static void listViewCentering(ListView<String> listView) {
        listView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    protected static void listComboBoxCentering(ComboBox<String> comboBox) {
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

    protected static Vehicle getAddedVehicle(CheckBox isHumanField, TextField lookAheadDistanceField,
                                                         TextField maxVelocityField, TextField maxAccelerationField,
                                                         TextField safetyDistanceField, ComboBox<String> colorField,
                                                         TextField lengthField, TextField widthField, ComboBox<String> initialPoseField,
                                                         ComboBox<String> goalPoseField) {
            ProjectData.Vehicle vehicle = new ProjectData.Vehicle();
            vehicle.setType("Autonomous");
            if (isHumanField.isSelected()) {
                vehicle.setType("Human");
                vehicle.setLookAheadDistance(Double.parseDouble(lookAheadDistanceField.getText()));
            }
            vehicle.setMaxVelocity(Double.parseDouble(maxVelocityField.getText()));
            vehicle.setMaxAcceleration(Double.parseDouble(maxAccelerationField.getText()));
            vehicle.setSafetyDistance(Double.parseDouble(safetyDistanceField.getText()));
            vehicle.setColor(colorField.getValue());
            vehicle.setLength(Double.parseDouble(lengthField.getText()));
            vehicle.setWidth(Double.parseDouble(widthField.getText()));
            vehicle.setInitialPose(initialPoseField.getValue());
            vehicle.setGoalPoses(new String[]{goalPoseField.getValue()});
            return vehicle;
        }

    /**
     * Gets an array of Pose objects for the given pose names.
     * If a pose is not found, the program exits.
     *
     * @param gui
     * @param poseNames One or more strings representing the keys to retrieve the Poses.
     * @return An array of Pose objects corresponding to the given pose names.
     */
    protected static Pose[] getPosesByName(GUI gui, String... poseNames) {
        ArrayList<Pose> poseList = new ArrayList<>();

        for (String name : poseNames) {
            name = name.trim();
            Pose pose = gui.projectData.getListOfAllPoses().get(name);
            if (pose == null) {
                System.out.println("Pose not found: " + name);
                System.exit(1); // Exit the program if a pose is not found
            }
            poseList.add(pose);
        }

        return poseList.toArray(new Pose[0]); // Convert the List to an array and return
    }

    protected static void getVehicles(ListView<String> vehicles, ProjectData projectData) {
        vehicles.getItems().clear();
        for (String vehicle : projectData.getVehicles().keySet()) {
            vehicles.getItems().add(vehicle);
        }
    }

    protected static void updateVehiclesList(ListView<String> vehiclesList, BorderPane borderPane, Button addVehicleButton,
                                             Button deleteVehicleButton, ProjectData projectData,
                                             TextField nameField, TextField lengthField, TextField widthField,
                                             TextField maxVelocityField, TextField maxAccelerationField,
                                             TextField safetyDistanceField, ComboBox<String> colorField,
                                             ComboBox<String> initialPoseField, ComboBox<String> goalPoseField,
                                             CheckBox isHumanField, Text lookAheadDistance,
                                             TextField lookAheadDistanceField) {
        VBox leftPane = new VBox();
        Text vehiclesText = new Text("Vehicles: ");
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
                ProjectData.Vehicle vehicle = projectData.getVehicles().get(newValue);

                // Update the fields in the centerPane with the details of the selected vehicle
                nameField.setText(newValue);
                lengthField.setText(String.valueOf(vehicle.getLength()));
                widthField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorField.setValue(vehicle.getColor());
                initialPoseField.setValue(vehicle.getInitialPose());
                if (vehicle.getGoalPoses() != null && vehicle.getGoalPoses().length > 0) {
                    goalPoseField.setValue(vehicle.getGoalPoses()[0]);
                }
                isHumanField.setSelected(vehicle.getLookAheadDistance() > 0);
                lookAheadDistance.setVisible(vehicle.getLookAheadDistance() > 0);
                lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                lookAheadDistanceField.setVisible(vehicle.getLookAheadDistance() > 0);
            }
        });

        // 1st item is initially selected
        vehiclesList.getSelectionModel().selectFirst();
    }

    protected static void updateVehiclesList(ListView<String> vehiclesList, BorderPane borderPane, Button addVehicleButton,
                                             Button deleteVehicleButton, ProjectData projectData,
                                             TextField nameField, TextField lengthField, TextField widthField,
                                             TextField maxVelocityField, TextField maxAccelerationField,
                                             TextField safetyDistanceField, TextField colorField,
                                             TextField initialPoseField, ListView<String> goalPoseField,
                                             CheckBox isHumanField, Text lookAheadDistance,
                                             TextField lookAheadDistanceField) {
        VBox leftPane = new VBox();
        Text vehiclesText = new Text("Vehicles: ");
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
                ProjectData.Vehicle vehicle = projectData.getVehicles().get(newValue);

                // Update the fields in the centerPane with the details of the selected vehicle
                nameField.setText(newValue);
                lengthField.setText(String.valueOf(vehicle.getLength()));
                widthField.setText(String.valueOf(vehicle.getWidth()));
                maxVelocityField.setText(String.valueOf(vehicle.getMaxVelocity()));
                maxAccelerationField.setText(String.valueOf(vehicle.getMaxAcceleration()));
                safetyDistanceField.setText(String.valueOf(vehicle.getSafetyDistance()));
                colorField.setText(vehicle.getColor());
                initialPoseField.setText(vehicle.getInitialPose());
                if (!projectData.getVehicles().isEmpty()) {
                    goalPoseField.getItems().clear();
                    Vehicle selectedVehicle = projectData.getVehicles().get(nameField.getText()); // Getting the first vehicle
                    String startPose = selectedVehicle.getInitialPose();
                    String[] goals = selectedVehicle.getGoalPoses();

                    if (goals != null && goals.length > 0) {
                        // Add the first mission step
                        goalPoseField.getItems().add("(" + startPose + " -> " + goals[0] + ")");

                        // Add subsequent mission steps
                        for (int i = 0; i < goals.length - 1; i++) {
                            goalPoseField.getItems().add("(" + goals[i] + " -> " + goals[i + 1] + ")");
                        }
                    }
                }
                listViewCentering(goalPoseField);
                isHumanField.setSelected(vehicle.getLookAheadDistance() > 0);
                lookAheadDistance.setVisible(vehicle.getLookAheadDistance() > 0);
                lookAheadDistanceField.setText(String.valueOf(vehicle.getLookAheadDistance()));
                lookAheadDistanceField.setVisible(vehicle.getLookAheadDistance() > 0);
            }
        });

        // 1st item is initially selected
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
        for (String pose : gui.projectData.getListOfAllPoses().keySet()) {
            Poses.getItems().add(pose);
        }
    }

    protected static void getPoses(GUI gui, TextField Poses) {
        for (String pose : gui.projectData.getListOfAllPoses().keySet()) {
            Poses.setText(Poses.getText() + pose + "\n");
        }
    }

    protected static Button getBrowseButton(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Save the Vehicle Reports: ");

        // Create the Browse button
        Button btnBrowse = new Button("Browse");

        // Set the action to be performed when the Browse button is clicked
        btnBrowse.setOnAction(e -> {
            // Show the directory chooser and get the selected directory
            File selectedDirectory = directoryChooser.showDialog(stage);

            // Check if a directory is selected
            if (selectedDirectory != null) {
                // Do something with the selected directory
                // For example, print the path to the console or use it in your application
                System.out.println("Folder selected: " + selectedDirectory.getAbsolutePath());
            }
        });
        return btnBrowse;
    }

    static void fileJSONOpen(GUI gui) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.setTitle("Select a project file to open: ");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(gui.stage);

        if (file != null) {
            gui.filenameJSON = file.getAbsolutePath();
            gui.pathLabel.setText("Name of Project: " + file.getName());
            gui.nextProjectButton.setVisible(true);
            gui.projectData = gui.jsonParser.parse(gui.filenameJSON);
        }
    }

    static void fileJSONCreate(GUI gui) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.setInitialFileName("Choose a folder and name of the project file: ");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(gui.stage);

        if (file != null) {
            if (!file.getName().endsWith(".json")) {
                gui.filenameJSON = file.getAbsolutePath() + ".json";
                file = new File(gui.filenameJSON);
            }

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write("{}");
                gui.pathLabel.setText("Name of Project: " + file.getName());
                gui.nextProjectButton.setVisible(true);
            } catch (IOException ex) {
                gui.pathLabel.setText("Error: Could not save the file.");
            }
        }
    }
}
