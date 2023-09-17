package org.pgstyle.autoutils.talesclicker.application;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;
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

    /** Standard date-time formatter for application core. */
    public static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");
    private static final String APPLICATION;
    private static final String VERSION;

    static {
        PrintStream console = System.out;
        PrintStream error = System.err;
        // load application info from MANIFEST.MF
        console.println("fetch application informations...");
        try {
            APPLICATION = Optional.ofNullable(Application.class.getPackage().getImplementationTitle()).orElse("application");
            VERSION = Optional.ofNullable(Application.class.getPackage().getImplementationVersion()).orElse("version");
        } catch (NullPointerException e) {
            error.println("load failed");
            e.printStackTrace();
            throw new IllegalStateException("failed to initialise application", e);
        }
        console.println("fetch completed!");
        Console.initialise(AppConfig.getConfig().getLoggingLevel(), null);
        Console.log(Level.INFO, "application initialisation completed!");
        Console.log(Level.DEBUG, "enter Application.main...");
    }

    /**
     * Output an image as a file.
     *
     * @param image the image to be stored
     * @param target the target file
     */
    public static void log(RenderedImage image, String target) {
        if (AppConfig.getConfig().isLogEnabled() && AppConfig.getConfig().isCaptchaLogged() && Console.level().value() <= Level.DEBUG.value()) {
            target += ".png";
            Console.log(Level.DEBUG, "store captcha text to \"%s\"", target);
            try {
                ImageIO.write(image, "png", Paths.get("./tales-clicker/" + target).toFile());
            } catch (IOException e) {
                Console.log(Level.WARN, "exception when output image %s: %s", target, e);
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
        Console.log(Level.TRACE, "Application - main: %s", Arrays.toString(args));
        Console.log(Level.INFO, "Starting application: %s/version %s", Application.name(), Application.version());
        // from 1.0, use module-environment-runner model
        // the module manager will load and start all modules
        ModuleRunner manager = ModuleRunner.of(ModuleManager.class, Environment.getInstance(), new String[0]);
        manager.start();
        // shutdown hook for cleaning modules
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Console.log(Level.INFO, "JVM Shutdown");
            Console.log(Level.INFO, "Send terminate signal to ModuleManager.");
            manager.shutdown(Signal.TERMINATE);
            try {
                manager.join();
                Console.log(Level.INFO, "ModuleManager has shutted down.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            Console.log(Level.INFO, "Exit Shutdown Hook [%s]", Thread.currentThread().getName());
        }));
        try {
            Console.log(Level.INFO, "Startup completed, application life-cycle control handed-over to ModuleManger");
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
