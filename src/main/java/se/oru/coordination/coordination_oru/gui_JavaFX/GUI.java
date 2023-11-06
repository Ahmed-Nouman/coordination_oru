package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class GUI extends Application {

    private final Stage stage = new Stage();
    private final Label pathLabel = new Label("");  // To display the file path
    private String filenameYAML = "";
    private final JsonParser jsonParser = new JsonParser();
    private ProjectData projectData;
    private YamlData yamlData;
    private final YamlParser yamlParser = new YamlParser();
    private final Button nextProjectButton = new Button("Next");
    private final Button backMapButton = new Button("Back");
    private final Button nextMapButton = new Button("Next");
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Scene projectScene = getProjectScene();
        stage.setTitle("Coordination_ORU");
        stage.setScene(projectScene);
        stage.show();

        nextProjectButton.setOnAction( e -> {
            stage.setScene(getMapScene());
            stage.centerOnScreen();
        });

        nextMapButton.setOnAction( e -> {
            stage.setScene(getVehicleScene());
            stage.centerOnScreen();
        });

        backMapButton.setOnAction( e -> {
            // FIXME Centering Issue
            stage.setScene(getProjectScene());
            stage.centerOnScreen();
            nextProjectButton.setVisible(true);
        });

    }

    private Scene getMapScene() {

        BorderPane borderPane = new BorderPane();

        VBox vBox = new VBox();

        Text changeMapMessage = new Text("Would you like to change the map? ");

        yamlData = yamlParser.parse(projectData.getMap());
        String imageFile = yamlData.getImage();
        Image mapImage = new Image("file:" + "/home/ra2/mine-map-paper-2023.png"); // FIXME Path problem
        // TODO Update the map names

        // Set the preferred dimensions for the image
        double preferredWidth = 800; // you can set this value to whatever width you want
        double preferredHeight = 640; // you can set this value to whatever height you want

        ImageView imageView = new ImageView(mapImage);
        imageView.setFitWidth(preferredWidth);
        imageView.setFitHeight(preferredHeight);
        imageView.setPreserveRatio(true); // This will keep the image's aspect ratio

        Button changeMapButton = new Button("Change Map");

        vBox.getChildren().addAll(changeMapMessage, imageView, changeMapButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        borderPane.setCenter(vBox);
        var leftPane = new StackPane(backMapButton);
        var rightPane = new StackPane(nextMapButton);
        borderPane.setLeft(leftPane);
        borderPane.setRight(rightPane);

        BorderPane.setMargin(leftPane, new Insets(0, 0, 0, 60)); // 20px top spacing
        BorderPane.setMargin(rightPane, new Insets(0, 60, 0, 0)); // 20px top spacing

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        changeMapButton.setOnAction(e -> {
            fileChooser.setTitle("Choose Map File");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml"));
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                try {
                    // TODO Update projectData
                    // TODO Show the Updated map
                    String content = new String(Files.readAllBytes(file.toPath()));
                    filenameYAML = file.getAbsolutePath();
//                    pathLabel.setText("File opened: " + filenameYAML);
//                    projectData = jsonParser.parse(filenameYAML);
                } catch (IOException ex) {
                    DialogBox.display("Error", "Could not read the file.");
                }
            }
        });

        return new Scene(borderPane, 1024, 720);
    }

    private Scene getVehicleScene() {
        BorderPane borderPane = new BorderPane();
        return new Scene(borderPane, 1024, 720);
    }

    private Scene getProjectScene() {

        BorderPane borderPane = new BorderPane();

        nextProjectButton.setVisible(false);

        VBox vbox = new VBox(40); // 40px spacing between elements
        vbox.setAlignment(Pos.CENTER); // Center alignment for all children of VBox

        Text welcomeMessage = new Text("Welcome to Coordination_ORU!");

        HBox hbox = new HBox(40); // 40px spacing between buttons
        hbox.setAlignment(Pos.CENTER); // Center alignment for all children of HBox

        Button createProject = new Button("Create Project");
        Button openProject = new Button("Open Project");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));


        createProject.setOnAction(e -> {
            fileChooser.setTitle("Create Project File");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Ensure the file has .json extension
                if (!file.getName().endsWith(".json")) {
                    filenameYAML = file.getAbsolutePath() + ".json";
                    file = new File(filenameYAML);
                }

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("{}"); // Save empty JSON or default data
                    pathLabel.setText("File created: " + file.getAbsolutePath());
                    nextProjectButton.setVisible(true);
                } catch (IOException ex) {
                    pathLabel.setText("Error: Could not save the file.");
                }
            }
        });

        openProject.setOnAction(e -> {
            fileChooser.setTitle("Open Project File");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    filenameYAML = file.getAbsolutePath();
                    pathLabel.setText("File opened: " + filenameYAML);
                    nextProjectButton.setVisible(true);
                    projectData = jsonParser.parse(filenameYAML);
                } catch (IOException ex) {
                    DialogBox.display("Error", "Could not read the file.");
                }
            }
        });

        hbox.getChildren().addAll(createProject, openProject); // Add buttons to HBox
        vbox.getChildren().addAll(welcomeMessage, hbox, pathLabel, nextProjectButton); // Add HBox (with buttons) to VBox below the text

        borderPane.setCenter(vbox);

        return new Scene(borderPane, 400, 300);
    }

}

