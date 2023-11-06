package se.oru.coordination.coordination_oru.gui_JavaFX;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class YamlParser {

    public YamlData parse(String filenameYAML) {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(filenameYAML)) {
            return yaml.loadAs(in, YamlData.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing YAML file: " + filenameYAML, e);
        }
    }
}
