package se.oru.coordination.coordination_oru.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class BottomPane {
    protected static Pane getBottomPane(Button... buttons) {

        Separator separator = new Separator();

        VBox bottomPane = new VBox();
        bottomPane.setSpacing(8);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.setAlignment(Pos.CENTER);

        HBox buttonsPane = new HBox();
        buttonsPane.getChildren().addAll(buttons);
        buttonsPane.setSpacing(50);
        buttonsPane.setAlignment(Pos.BOTTOM_RIGHT);

        bottomPane.getChildren().addAll(separator, buttonsPane);
        return bottomPane;
    }
}
