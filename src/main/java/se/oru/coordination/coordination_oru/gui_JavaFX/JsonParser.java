package se.oru.coordination.coordination_oru.gui_JavaFX;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;

public class JsonParser {

    public ProjectData parse(String filenameJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(Paths.get(filenameJSON).toFile(), ProjectData.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSON file: " + filenameJSON, e);
        }
    }
}
