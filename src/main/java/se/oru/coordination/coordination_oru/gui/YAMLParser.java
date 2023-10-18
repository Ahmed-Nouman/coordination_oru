package se.oru.coordination.coordination_oru.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class YAMLParser {

    public static String getImageFilePath(String yamlFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(yamlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("image:")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
