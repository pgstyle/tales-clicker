package org.pgstyle.talesclicker.application;

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
import java.util.Properties;

import org.pgstyle.talesclicker.application.Application.Level;

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

    public int[] getTypeTiming() {
        String[] raw = this.properties.getProperty("application.action.type.timing").split(",");
        return new int[] {Integer.parseInt(raw[0]), Integer.parseInt(raw[1])};
    }

    public double getMonitorFrequency() {
        return Double.parseDouble(this.properties.getProperty("application.module.manager.monitor.frequency"));
    }

    public double getDetectFrequency() {
        return Double.parseDouble(this.properties.getProperty("application.module.notifier.detect.frequency"));
    }

    public double getRetryFrequency() {
        return Double.parseDouble(this.properties.getProperty("application.module.manager.monitor.frequency"));
    }

    public Level getLoggingLevel() {
        return Level.valueOf(this.properties.getProperty("application.log.level"));
    }

    public boolean isModuleEnabled(String name) {
        return Boolean.parseBoolean(this.properties.getProperty("application.module." + name + ".enable"));
    }

    public long getCaptchaDelayShort() {
        return 1000l * Integer.parseInt(this.properties.getProperty("application.module.captcha.delay.short"));
    }

    public long getCaptchaDelayLong() {
        return 1000l * Integer.parseInt(this.properties.getProperty("application.module.captcha.delay.long"));
    }

    public String getNotifyMethod() {
        return this.properties.getProperty("application.module.notifier.method");
    }

    public String getLineToken() {
        String token = this.properties.getProperty("application.module.notifier.line.token");
        if (token.startsWith("SysEnv")) {
            token = System.getenv(token.substring(7));
        }
        return token;
    }

    public String[] getModuleArgs(String name, int index) {
        String[] args = Optional.ofNullable(this.properties.getProperty("application.module." + name + ".args")).map(s -> s.split(";")).orElse(new String[0]);
        return args.length > index ? args[index].split(",") : new String[0];
    }

    public int getModuleCount(String name) {
        String[] args = Optional.ofNullable(this.properties.getProperty("application.module." + name + ".args")).map(s -> s.split(";")).orElse(new String[0]);
        return Math.max(args.length, 1);
    }

}
