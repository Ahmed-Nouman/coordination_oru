package se.oru.coordination.coordination_oru.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DisplayLine {
    public JLabel createLine(int fontType, int fontSize,  String text) {

        String message = "<html><center>" + text + "</center></html>";
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font(label.getFont().getName(), fontType, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}