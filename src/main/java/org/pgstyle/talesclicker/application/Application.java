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

    public static void log(String message, Object... args) {
        if (!Application.stdInit) {
            if (Configuration.getConfig().isLogEnabled()) {
                System.setOut(Application.STDOUT);
                System.setErr(System.out);
            }
            Application.stdInit = true;
        }
        System.out.printf("%s | %-32s | %s%n",
                          Application.LOG_FORMATTER.format(LocalDateTime.now()),
                          AppUtils.getCallerText(Thread.currentThread().getStackTrace()[2]),
                          String.format(message, args));
    }

    public static void log(RenderedImage image, String target) {
        if (Configuration.getConfig().isLogEnabled() && Configuration.getConfig().isCaptchaLogged()) {
            target += ".png";
            try {
                ImageIO.write(image, "png", Paths.get("./tales-clicker/" + target).toFile());
            } catch (IOException e) {
                Application.log("exception when output image %s: %s", target, e);
                e.printStackTrace();
            }
        }
    }

    private Application() {}

}
