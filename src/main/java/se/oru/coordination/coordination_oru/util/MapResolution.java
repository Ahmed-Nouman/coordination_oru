package se.oru.coordination.coordination_oru.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MapResolution {
    public double getMapResolution(String filePath) {
        double result = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("resolution:")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        result = Double.parseDouble(parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
