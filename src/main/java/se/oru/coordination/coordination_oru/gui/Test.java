package se.oru.coordination.coordination_oru.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Test extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 300);

        ListView<ProjectData.MissionStep> listView = new ListView<>();

        // Sample data - replace this with your actual List<MissionStep>
        List<ProjectData.MissionStep> missionSteps = List.of(
//                new ProjectData.MissionStep("Pose1", 15),
//                new ProjectData.MissionStep("Pose2", 30)
                // Add more MissionStep objects as needed
        );
        listView.getItems().addAll(missionSteps);

        root.getChildren().add(listView);

        stage.setTitle("Mission Steps");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Include your MissionStep class here
}
