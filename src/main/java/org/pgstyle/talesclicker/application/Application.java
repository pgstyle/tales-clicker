package org.pgstyle.talesclicker.application;

import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

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

    public static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");

    private static final PrintStream STDOUT;

    static {
        try {
            Files.createDirectories(Paths.get("./tales-clicker/captchas"));
        } catch (IOException e) { e.printStackTrace(); }
        try {
            Files.createDirectories(Paths.get("./tales-clicker/logs"));
        } catch (IOException e) { e.printStackTrace(); }
        PrintStream stdout = null;
        try {
            stdout = new PrintStream(new RedirectOutputStream(System.out, new FileOutputStream("./tales-clicker/logs/" + System.currentTimeMillis() / 1000 + ".log")));
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        STDOUT = stdout;
        stdInit = false;
    }

    private static boolean stdInit;
    private static Level logLevel;

    public static void log(Level level, String message, Object... args) {
        if (!Application.stdInit) {
            if (Configuration.getConfig().isLogEnabled()) {
                System.setOut(Application.STDOUT);
                System.setErr(System.out);
            }
            Application.logLevel = Configuration.getConfig().getLoggingLevel();
            Application.stdInit = true;
        }
        if (level.value() >= Application.logLevel.value()) {
            System.out.printf("%s|%-4s|%-32s|%-5s| %s%n",
                              Application.LOG_FORMATTER.format(LocalDateTime.now()),
                              Long.toString(Thread.currentThread().getId() & 0x1fff), AppUtils.getCallerText(), level.name(),
                              String.format(message, args));
        }
    }

    public static void log(RenderedImage image, String target) {
        if (Configuration.getConfig().isLogEnabled() && Configuration.getConfig().isCaptchaLogged() && Application.logLevel.value() <= Level.DEBUG.value()) {
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

    private Application() {}

}
