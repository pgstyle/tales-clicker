package org.pgstyle.autoutils.talesclicker.application;

import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.ModuleManager;
import org.pgstyle.autoutils.talesclicker.module.ModuleRunner;
import org.pgstyle.autoutils.talesclicker.module.Signal;

/**
 * The core of the {@code Tales-Clicker}, this class provides program entrypoint
 * and logging.
 *
 * @since 0.1-dev
 * @author PGKan
 */
public final class Application {

    public enum Level {
        // control levels
        ALL(0), OFF(100),
        // logging levels
        TRACE(0),
        DEBUG(10),
        INFO(20),
        WARN(30),
        ERROR(40),
        FATAL(50);

        private Level(int value) {
            this.value = value;
        }

        private final int value;

        public int value() {
            return this.value;
        }

    }

    /** Standard date-time formatter for application core. */
    public static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");
    private static final Level LOG_LEVEL;
    private static final String APPLICATION;
    private static final String VERSION;

    static {
        PrintStream console = System.out;
        PrintStream error = System.err;
        // load application info from MANIFEST.MF
        console.println("fetch application informations...");
        try {
            Objects.requireNonNull(APPLICATION = Application.class.getPackage().getImplementationTitle(), "application");
            Objects.requireNonNull(VERSION = Application.class.getPackage().getImplementationVersion(), "version");
        } catch (NullPointerException e) {
            error.println("load failed");
            e.printStackTrace();
            throw new IllegalStateException("failed to initialise application", e);
        }
        console.println("fetch completed!");

        // create directories for logging
        console.println("setup directories ./tales-clicker/...");
        try {
            Files.createDirectories(Paths.get("./tales-clicker/captchas"));
            Files.createDirectories(Paths.get("./tales-clicker/logs"));
        } catch (IOException e) {
            error.println("directories setup failed");
            e.printStackTrace();
            throw new IllegalStateException("failed to initialise application", e);
        }
        console.println("directories setup completed!");

        console.println("setup AOS logger...");
        // logging initialise
        LOG_LEVEL = Configuration.getConfig().getLoggingLevel();
        console.println("logging level: " + LOG_LEVEL);
        // check if terminal support ANSI colour code
        boolean colourful = false;
        if (Optional.ofNullable(System.getenv("TERM")).map(s -> s.contains("xterm")).orElse(false)) {
            // Unix-like system
            colourful = true;
        }
        else {
            // non-Unix-like system, usually Windows
            // check registry
            try {
                Process reg = Runtime.getRuntime().exec("REG QUERY \"HKCU\\Console\" -v VirtualTerminalLevel");
                byte[] buffer = new byte[256];
                if (reg.waitFor() == 0) {
                    reg.getInputStream().read(buffer);
                    String result = new String(buffer).trim();
                    result = result.substring(result.indexOf("0x") + 2);
                    if (Integer.parseInt(result, 16) != 0) {
                        colourful = true;
                    }
                }
            } catch (IOException e) {
                /* no fallback */
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        console.println("colourful: " + colourful);
        // setup stdout and file output bi-direction stream
        console.println("installing AOS logger...");
        try {
            if (Configuration.getConfig().isLogEnabled()) {
                System.setOut(new ApplicationOutputStream(console, new FileOutputStream("./tales-clicker/logs/" + System.currentTimeMillis() / 1000 + ".log"), colourful));
                System.setErr(console);
            }
        } catch (FileNotFoundException e) {
            error.println("logger install failed");
            e.printStackTrace();
            throw new IllegalStateException("failed to initialise application", e);
        }
        console.println("AOS logger installation completed!");
        console.println("logger setup completed!");
        console.println("application initialisation completed!");
        console.println("enter Application.main...");
    }

    /**
     * Output log message to standard output and log file.
     *
     * @param level level of log
     * @param message message to be logged
     * @param args arguments for formatting the log message
     */
    public static void log(Level level, String message, Object... args) {
        if (level.value() >= Application.LOG_LEVEL.value()) {
            System.out.printf("%s|%04d|%-32s|%-5s| %s%n",
                              Application.LOG_FORMATTER.format(LocalDateTime.now()),
                              Thread.currentThread().getId() & 0x1fff, AppUtils.getCallerText(), level.name(),
                              String.format(message, args));
        }
    }

    /**
     * Output an image as a file.
     *
     * @param image the image to be stored
     * @param target the target file
     */
    public static void log(RenderedImage image, String target) {
        if (Configuration.getConfig().isLogEnabled() && Configuration.getConfig().isCaptchaLogged() && Application.LOG_LEVEL.value() <= Level.DEBUG.value()) {
            target += ".png";
            Application.log(Level.DEBUG, "store captcha text to \"%s\"", target);
            try {
                ImageIO.write(image, "png", Paths.get("./tales-clicker/" + target).toFile());
            } catch (IOException e) {
                Application.log(Level.WARN, "exception when output image %s: %s", target, e);
                e.printStackTrace();
            }
        }
    }

    public static String name() {
        return Application.APPLICATION;
    }

    public static String version() {
        return Application.VERSION;
    }

    /**
     * Application entrypoint.
     *
     * @param args program arguments
     * @return status code ({@code 0} if success; or non-{@code 0} otherwise)
     */
    public static int main(String[] args) {
        Application.log(Level.TRACE, "Application - main: %s", Arrays.toString(args));
        Application.log(Level.INFO, "Starting application: %s/version %s", Application.name(), Application.version());
        // from 1.0, use module-environment-runner model
        // the module manager will load and start all modules
        ModuleRunner manager = ModuleRunner.of(ModuleManager.class, Environment.getInstance(), new String[0]);
        manager.start();
        // shutdown hook for cleaning modules
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Application.log(Level.INFO, "JVM Shutdown");
            Application.log(Level.INFO, "Send terminate signal to ModuleManager.");
            manager.shutdown(Signal.TERMINATE);
            try {
                manager.join();
                Application.log(Level.INFO, "ModuleManager has shutted down.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            Application.log(Level.INFO, "Exit Shutdown Hook [%s]", Thread.currentThread().getName());
        }));
        try {
            Application.log(Level.INFO, "Startup completed, application life-cycle control handed-over to ModuleManger");
            manager.join();
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return 130;
        }
    }

    private Application() {}

}
