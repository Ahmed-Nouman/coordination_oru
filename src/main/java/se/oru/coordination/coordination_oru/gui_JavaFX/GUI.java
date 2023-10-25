package se.oru.coordination.coordination_oru.gui_JavaFX;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Coordination_ORU");
        Scene welcomeScene = getWelcomeScene(primaryStage);
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }

    private Scene getWelcomeScene(Stage primaryStage) {
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
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                // Ensure the file has .json extension
                if (!file.getName().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("{}"); // Save empty JSON or default data
                    pathLabel.setText("File created: " + file.getAbsolutePath());
                } catch (IOException ex) {
                    pathLabel.setText("Error: Could not save the file.");
                }
            }
        });

        openProject.setOnAction(e -> {
            fileChooser.setTitle("Open Project File");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    pathLabel.setText("File opened: " + file.getAbsolutePath());
//                    System.out.println(content);  //FIXME Check Parse json file
                } catch (IOException ex) {
                    DialogBox.getDialogBox("Error", "Could not read the file.");
                }
            }
        });

        hbox.getChildren().addAll(createProject, openProject); // Add buttons to HBox
        vbox.getChildren().addAll(welcomeMessage, hbox, pathLabel); // Add HBox (with buttons) to VBox below the text

        return new Scene(vbox, 400, 300);
    }
}

