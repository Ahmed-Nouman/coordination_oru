package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class GUI extends Application {

    private final Label pathLabel = new Label("");  // To display the file path
    private String filenameJSON = "";
    private final JsonParser parser = new JsonParser();
    private ProjectData data;
    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Scene projectScene = getProjectScene(primaryStage);
        primaryStage.setTitle("Coordination_ORU");
        primaryStage.setScene(projectScene);
        primaryStage.show();

        nextButton.setOnAction( e -> primaryStage.setScene(getMapScene(primaryStage)));

    }

    private Scene getMapScene(Stage stage) {

        BorderPane borderPane = new BorderPane();

        Text changeMapMessage = new Text("Change the map: ");
        borderPane.setTop(changeMapMessage);

        // Set alignment to center for the Text node
        BorderPane.setAlignment(changeMapMessage, Pos.CENTER);

        // Add spacing to the top of the BorderPane for the Text node
        BorderPane.setMargin(changeMapMessage, new Insets(20, 0, 0, 0)); // 20px top spacing

        return new Scene(borderPane, 400, 300);
    }

    private Scene getProjectScene(Stage stage) {

        BorderPane borderPane = new BorderPane();

        nextButton.setVisible(false);

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
                    filenameJSON = file.getAbsolutePath() + ".json";
                    file = new File(filenameJSON);
                }

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("{}"); // Save empty JSON or default data
                    pathLabel.setText("File created: " + file.getAbsolutePath());
                    nextButton.setVisible(true);
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
                    filenameJSON = file.getAbsolutePath();
                    pathLabel.setText("File opened: " + filenameJSON);
                    nextButton.setVisible(true);
                    data = parser.parse(filenameJSON);
                } catch (IOException ex) {
                    DialogBox.display("Error", "Could not read the file.");
                }
            }
        });

        hbox.getChildren().addAll(createProject, openProject); // Add buttons to HBox
        vbox.getChildren().addAll(welcomeMessage, hbox, pathLabel, nextButton); // Add HBox (with buttons) to VBox below the text

        borderPane.setCenter(vbox);

        return new Scene(borderPane, 400, 300);
    }
}

