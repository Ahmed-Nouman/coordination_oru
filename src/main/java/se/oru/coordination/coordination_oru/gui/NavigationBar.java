package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class NavigationBar {

    private static final int SPACING = 8;
    private static final int PADDING = 10;
    private static final int BUTTON_SPACING = 50;

    public static Pane getBar(Main main, SceneState sceneState) {

        var navigationPane = navigationPane();
        var buttonsButtons = navigationButtons();

        switch (sceneState) {
            case HOME:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getNextButton());
                break;
            case MAP:
            case VEHICLE:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getBackButton(), main.getNavigationButton().getNextButton());
                break;
            case EXPERIMENT:
                buttonsButtons.getChildren().addAll(main.getNavigationButton().getResetButton(), main.getNavigationButton().getBackButton(),
                        main.getNavigationButton().getSaveButton(), main.getNavigationButton().getRunButton());
                break;
            default:
                break;
        }

        navigationPane.getChildren().addAll(new Separator(), buttonsButtons);
        return navigationPane;
    }

    private static HBox navigationButtons() {
        var buttonsPane = new HBox();
        buttonsPane.setSpacing(BUTTON_SPACING);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);
        return buttonsPane;
    }

    private static VBox navigationPane() {
        var bottomPane = new VBox();
        bottomPane.setSpacing(SPACING);
        bottomPane.setPadding(new Insets(0, PADDING, PADDING, PADDING));
        bottomPane.setAlignment(Pos.CENTER);
        return bottomPane;
    }
}
