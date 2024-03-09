package org.pgstyle.autoutils.talesclicker.application;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.pgstyle.autoutils.talesclicker.application.Application.Level;

/**
 * The {@code Configuration} class loads settings from external properties file
 * and internal default settings to modify the behaviour of the
 * {@code Tales-Clicker} program.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Configuration {

    private static final Properties DEFAULT = Configuration.setDefault();
    private static final Configuration INSTANCE = Configuration.load();

    private static Configuration load() {
        Path path = Paths.get("./tales-clicker/tales-clicker.properties");
        Properties properties = Properties.from(Configuration.DEFAULT);
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
        Properties properties = Properties.empty();
        try {
            properties.load(AppUtils.getResource("./default-config.properties"));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * Get the configuration instance of this application.
     *
     * @return the {@code Configuration}
     */
    public static Configuration getConfig() {
        return Configuration.INSTANCE;
    }

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    private final Properties properties;

    /**
     * Check if application logging is enabled.
     *
     * @return {@code true} if application logging is enabled; or {@code false}
     *         otherwise
     */
    public boolean isLogEnabled() {
        return Boolean.parseBoolean(this.properties.getProperty("application.log.enable", "true"));
    }

    /**
     * Get the application logging level.
     *
     * @return the application logging level
     */
    public Level getLoggingLevel() {
        return Level.valueOf(this.properties.getProperty("application.log.level", "DEBUG"));
    }

    /**
     * Check if application logging for captcha image is enabled.
     *
     * @return {@code true} if captcha image logging is enabled; or
     *         {@code false} otherwise
     */
    public boolean isCaptchaLogged() {
        return Boolean.parseBoolean(this.properties.getProperty("application.log.captcha", "true"));
    }

    /**
     * Get the action timing of clicking action.
     *
     * @return the timing sequence (millisecond)
     */
    public int[] getClickTiming() {
        String[] raw = this.properties.getProperty("application.action.click.timing", "250,50,100").split(",");
        return new int[] {Integer.parseInt(raw[0]), Integer.parseInt(raw[1]), Integer.parseInt(raw[2])};
    }

    /**
     * Get the action timing of capturing action.
     *
     * @return the timing sequence (millisecond)
     */
    public int[] getCaptureTiming() {
        String[] raw = this.properties.getProperty("application.action.capture.timing", "250").split(",");
        return new int[] {Integer.parseInt(raw[0])};
    }

    /**
     * Get the action timing of typing action.
     *
     * @return the timing sequence (millisecond)
     */
    public int[] getTypeTiming() {
        String[] raw = this.properties.getProperty("application.action.type.timing", "120,500").split(",");
        return new int[] {Integer.parseInt(raw[0]), Integer.parseInt(raw[1])};
    }

    /**
     * Get the capture area of capturing action.
     *
     * @return the area sequence
     */
    public int[] getCaptureArea() {
        String raw = this.properties.getProperty("application.action.capture.area", "FULL");
        int[] area;
        if ("FULL".equalsIgnoreCase(raw)) {
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            area = new int[] {0, 0, dimension.width, dimension.height};
        }
        else {
            area = Arrays.stream(raw.split(",")).mapToInt(Integer::parseInt).toArray();
        }
        return area;
    }

    /**
     * Check if the module is enabled.
     *
     * @return {@code true} if the module is enabled; or {@code false} otherwise
     */
    public boolean isModuleEnabled(String name) {
        return Boolean.parseBoolean(this.properties.getProperty("application.module." + name + ".enable", "false"));
    }

    /**
     * Calculate the module thread starting count.
     *
     * @return the module count
     */
    public int getModuleCount(String name) {
        String[] args = Optional.ofNullable(this.properties.getProperty("application.module." + name + ".args", "")).map(s -> s.split(";")).orElse(new String[0]);
        return Math.max(args.length, 1);
    }

    /**
     * Get module argument from a module thread.
     *
     * @return the module arguments
     */
    public String[] getModuleArgs(String name, int index) {
        String[] args = Optional.ofNullable(this.properties.getProperty("application.module." + name + ".args", "")).map(s -> s.split(";")).orElse(new String[0]);
        return args.length > index ? args[index].split(",") : new String[0];
    }

    /**
     * Get property string of a module.
     *
     * @param module the name of the module
     * @param name the name of the property
     * @return the property string
     */
    public String getModuleProperty(String module, String name) {
        name = "application.module." + module + "." + name;
        String value = this.properties.getProperty(name, "");
        Application.log(Level.DEBUG, "load from property, %s=%s", name, value);
        if (value.startsWith("SysEnv ")) {
            String[] parts = value.split(" ");
            value = Optional.ofNullable(System.getenv(parts.length > 1 ? parts[1] : "")).orElse(parts.length > 2 ? parts[2] : "");
        }
        else if (value.startsWith("SysProp")) {
            String[] parts = value.split(" ");
            value = System.getProperty(parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "");
        }
        return value;
    }

    /**
     * Get property string of a module and convert it into integer (long).
     *
     * @param module the name of the module
     * @param name the name of the property
     * @return the property integer
     */
    public long getModulePropertyAsInteger(String module, String name) {
        return Optional.ofNullable(this.getModuleProperty(module, name)).map(Long::parseLong).orElse(0l);
    }

    /**
     * Get property string of a module and convert it into real number (double).
     *
     * @param module the name of the module
     * @param name the name of the property
     * @return the property number
     */
    public double getModulePropertyAsReal(String module, String name) {
        return Optional.ofNullable(this.getModuleProperty(module, name)).map(Double::parseDouble).orElse(0.0);
    }

    /**
     * Get property string of a module and convert it into boolean.
     *
     * @param module the name of the module
     * @param name the name of the property
     * @return the property boolean
     */
    public boolean getModulePropertyAsBoolean(String module, String name) {
        return Optional.ofNullable(this.getModuleProperty(module, name)).map(Boolean::parseBoolean).orElse(false);
    }

}
