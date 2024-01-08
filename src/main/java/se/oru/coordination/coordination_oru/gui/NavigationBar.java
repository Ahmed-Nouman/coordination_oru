package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class NavigationBar {
    //FIXME: Handle buttons logic inside this class
    protected static Pane update(Button... buttons) {

        var bottomPane = new VBox();
        bottomPane.setSpacing(8);
        bottomPane.setPadding(new Insets(0, 10, 10, 10));
        bottomPane.setAlignment(Pos.CENTER);

        var buttonsPane = new HBox();
        buttonsPane.setSpacing(50);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);

        buttonsPane.getChildren().addAll(buttons);

        bottomPane.getChildren().addAll(new Separator(), buttonsPane);
        return bottomPane;
    }
}
