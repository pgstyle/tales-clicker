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

import org.pgstyle.autoutils.talesclicker.common.AppResource;
import org.pgstyle.autoutils.talesclicker.common.Configuration;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;
import org.pgstyle.autoutils.talesclicker.common.Properties;

/**
 * The {@code Configuration} class loads settings from external properties file
 * and internal default settings to modify the behaviour of the
 * {@code Tales-Clicker} program.
 *
 * @since 1.0
 * @author PGKan
 */
public final class AppConfig extends Configuration {

    private static final Properties DEFAULT = AppConfig.setDefault();
    private static final AppConfig INSTANCE = AppConfig.load();

    private static AppConfig load() {
        return new AppConfig("./tales-clicker/tales-clicker.properties");
    }

    private static Properties setDefault() {
        Properties properties = Properties.empty();
        try {
            properties.load(AppResource.getResource("./default-config.properties"));
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
    public static AppConfig getConfig() {
        return AppConfig.INSTANCE;
    }

    private AppConfig(String properties) {
        super(properties, AppConfig.DEFAULT);
    }

    /**
     * Check if application logging is enabled.
     *
     * @return {@code true} if application logging is enabled; or {@code false}
     *         otherwise
     */
    public boolean isLogEnabled() {
        return this.getBoolean("application.log.enable");
    }

    /**
     * Get the application logging level.
     *
     * @return the application logging level
     */
    public Level getLoggingLevel() {
        return Level.valueOf(this.getString("application.log.level", Level.DEBUG.name()));
    }

    /**
     * Check if application logging for captcha image is enabled.
     *
     * @return {@code true} if captcha image logging is enabled; or
     *         {@code false} otherwise
     */
    public boolean isCaptchaLogged() {
        return this.getBoolean("application.log.captcha");
    }

    /**
     * Get the action timing of clicking action.
     *
     * @return the timing sequence (millisecond)
     */
    public long[] getClickTiming() {
        return this.getIntegers("application.action.click.timing");
    }

    /**
     * Get the action timing of typing action.
     *
     * @return the timing sequence (millisecond)
     */
    public long[] getTypeTiming() {
        return this.getIntegers("application.action.type.timing");
    }

    /**
     * Get the capture area of capturing action.
     *
     * @return the area sequence
     */
    public long[] getCaptureArea() {
        String raw = this.getString("application.action.capture.area", "FULL");
        long[] area;
        if ("FULL".equalsIgnoreCase(raw)) {
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            area = new long[] {0, 0, dimension.width, dimension.height};
        }
        else {
            area = this.getIntegers("application.action.type.timing");
        }
        return area;
    }

    /**
     * Check if the module is enabled.
     *
     * @return {@code true} if the module is enabled; or {@code false} otherwise
     */
    public boolean isModuleEnabled(String name) {
        return this.getBoolean("application.module." + name + ".enable");
    }

    /**
     * Calculate the module thread starting count.
     *
     * @return the module count
     */
    public int getModuleCount(String name) {
        return Math.max(this.getStringsSet("application.module." + name + ".args").length, 1);
    }

    /**
     * Get module argument from a module thread.
     *
     * @return the module arguments
     */
    public String[] getModuleArgs(String name, int index) {
        String[][] argsSet = this.getStringsSet("application.module." + name + ".args");
        return argsSet.length > index ? argsSet[index] : new String[0];
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
        String value = this.getString(name, "");
        Console.log(Level.DEBUG, "load from property, %s=%s", name, value);
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
