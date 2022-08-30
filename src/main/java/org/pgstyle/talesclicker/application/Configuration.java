package org.pgstyle.talesclicker.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.pgstyle.talesclicker.clicker.TalesClicker;

public final class Configuration {

    private static final Properties DEFAULT = Configuration.setDefault();
    private static final Configuration INSTANCE = Configuration.load();

    private static Configuration load() {
        Path path = Paths.get("./tales-clicker/tales-clicker.properties");
        Properties properties = new Properties(Configuration.DEFAULT);
        if (Files.exists(path)) {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                Configuration.DEFAULT.store(fos, "Settings for Tales Clicker");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Configuration(properties);
    }

    private static Properties setDefault() {
        Properties properties = new Properties(Configuration.DEFAULT);
        try {
            properties.load(TalesClicker.loadResource("default-config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Configuration getConfig() {
        return Configuration.INSTANCE;
    }

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    private final Properties properties;
    
}
