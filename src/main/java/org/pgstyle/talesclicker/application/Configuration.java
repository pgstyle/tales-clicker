package org.pgstyle.talesclicker.application;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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
            properties.load(AppUtils.getResource("default-config.properties"));
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

    public boolean isLogEnabled() {
        return Boolean.parseBoolean(this.properties.getProperty("application.log.enable"));
    }

    public boolean isCaptchaLogged() {
        return Boolean.parseBoolean(this.properties.getProperty("application.log.captcha"));
    }

    public boolean isCaptchaEnabled() {
        return Boolean.parseBoolean(this.properties.getProperty("application.module.captcha.enable"));
    }

    public int[] getClickTiming() {
        String[] raw = this.properties.getProperty("application.action.click.timing").split(",");
        return new int[] {Integer.parseInt(raw[0]), Integer.parseInt(raw[1]), Integer.parseInt(raw[2])};
    }

    public int[] getCaptureArea() {
        String raw = this.properties.getProperty("application.action.capture.area");
        Dimension dimension;
        if ("FULL".equals(raw.toUpperCase())) {
            dimension = Toolkit.getDefaultToolkit().getScreenSize();
        }
        else {
            String[] split = raw.split(",");
            dimension = new Dimension(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        return new int[] {dimension.width, dimension.height};
    }

    public int[] getTypeTiming() {
        String[] raw = this.properties.getProperty("application.action.type.timing").split(",");
        return new int[] {Integer.parseInt(raw[0]), Integer.parseInt(raw[1])};
    }
}
