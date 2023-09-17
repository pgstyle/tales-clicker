package org.pgstyle.autoutils.talesclicker.common;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public final class Console {
    
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

    private static PrintStream stdout = System.out;
    private static PrintStream stderr = System.err;

    /** Standard date-time formatter for application core. */
    public static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");
    private static Level level = Level.DEBUG;

    private static void setupLogFile(Path logFile) {
        Console.stdout.printf("setup log file %s...", logFile);
        try {
            Files.createDirectories(logFile.getParent());
            Files.createFile(logFile);
        } catch (IOException e) {
            Console.stderr.println("log file setup failed");
            e.printStackTrace(Console.stderr);
            throw new IllegalStateException("failed to initialise console", e);
        }
        Console.stdout.println("log file setup completed!");
    }

    private static void setupLogger(Level level, Path logFile) {
        Console.stdout.println("setup AOS logger...");
        // logging initialise
        Console.level = Objects.requireNonNull(level, "require logging level");
        Console.stdout.println("logging level: " + Console.level);
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
        Console.stdout.println("colourful: " + colourful);
        // setup stdout and file output bi-direction stream
        Console.stdout.println("installing AOS logger...");
        Console.stdout = new ApplicationOutputStream(Console.stdout, Objects.nonNull(logFile) ? logFile.toFile() : null, colourful);
        System.setOut(Console.stdout);
        System.setErr(Console.stdout);
        Console.stdout.println("AOS logger installation completed!");
        Console.stdout.println("logger setup completed!");
    }

    public static boolean initialise(Level level, Path logFile) {
        // initialise log file
        try {
            Optional.ofNullable(logFile).ifPresent(Console::setupLogFile);
            Console.setupLogger(level, logFile);
            Console.log(Level.INFO, "Logging initialised!");
            return true;
        }
        catch (RuntimeException e) {
            Console.stderr.println("cannot initialise console");
            e.printStackTrace(Console.stderr);
            return false;
        }
    }

    public static Level level() {
        return Console.level;
    }

    /**
     * Output log message to standard output and log file.
     *
     * @param level level of log
     * @param message message to be logged
     * @param args arguments for formatting the log message
     */
    public static void log(Level level, String message, Object... args) {
        if (level.value() >= Console.level.value()) {
            Console.stdout.printf("%s|%04d|%-32s|%-5s| %s%n",
                              Console.LOG_FORMATTER.format(LocalDateTime.now()),
                              Thread.currentThread().getId() & 0x1fff, Classes.getCallerText(), level.name(),
                              String.format(message, args));
        }
    }

}
