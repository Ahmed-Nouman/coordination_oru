package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NavigationBar extends VBox {

    private final Button resetButton;
    private final Button backButton;
    private final Button nextButton;
    private final Button saveButton;
    private final Button runButton;
    private final Button stopButton;
    private final HBox navigationButtons;

    public NavigationBar() {
        resetButton = new Button("Reset");
        backButton = new Button("Back");
        nextButton = new Button("Next");
        saveButton = new Button("Save");
        runButton = new Button("Run");
        stopButton = new Button("Stop");

        // Initialize navigationButtons HBox to hold buttons
        navigationButtons = new HBox();
        int buttonSpacing = 50;
        navigationButtons.setSpacing(buttonSpacing);
        navigationButtons.setAlignment(Pos.BOTTOM_RIGHT);

        // Configure NavigationBar VBox
        int separatorSpacing = 8;
        this.setSpacing(separatorSpacing);
        int padding = 10;
        this.setPadding(new Insets(0, padding, padding, padding));
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(new Separator(), navigationButtons);
    }

    public void addResetButton() {
        if (!navigationButtons.getChildren().contains(resetButton)) {
            navigationButtons.getChildren().add(resetButton);
        }
    }

    public void addBackButton() {
        if (!navigationButtons.getChildren().contains(backButton)) {
            navigationButtons.getChildren().add(backButton);
        }
    }

    public void addNextButton() {
        if (!navigationButtons.getChildren().contains(nextButton)) {
            navigationButtons.getChildren().add(nextButton);
        }
    }

    public void addSaveButton() {
        if (!navigationButtons.getChildren().contains(saveButton)) {
            navigationButtons.getChildren().add(saveButton);
        }
    }

    public void addRunButton() {
        if (!navigationButtons.getChildren().contains(runButton)) {
            navigationButtons.getChildren().add(runButton);
        }
    }

    public void addStopButton() {
        if (!navigationButtons.getChildren().contains(stopButton)) {
            navigationButtons.getChildren().add(stopButton);
        }
    }

    public void removeResetButton() {
        navigationButtons.getChildren().remove(resetButton);
    }

    public void removeBackButton() {
        navigationButtons.getChildren().remove(backButton);
    }

    public void removeNextButton() {
        navigationButtons.getChildren().remove(nextButton);
    }

    public void removeSaveButton() {
        navigationButtons.getChildren().remove(saveButton);
    }

    public void removeRunButton() {
        navigationButtons.getChildren().remove(runButton);
    }

    public void removeStopButton() {
        navigationButtons.getChildren().remove(stopButton);
    }
}
