package org.pgstyle.talesclicker.application;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;

/**
 * Redirect a copy of standard output stream to file another stream.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class ApplicationOutputStream extends PrintStream {

    /**
     * Set up an {@code ApplicationOutputStream}.
     *
     * @param stdout the stream for standard output
     * @param file the stream for file output
     * @param colourful indicate availability of ANSI colouring on the stdout
     */
    public ApplicationOutputStream(PrintStream stdout, OutputStream file, boolean colourful) {
        super(stdout);
        this.file = new PrintStream(file);
        this.colourful = colourful;
    }

    private final PrintStream file;
    private final boolean colourful;

    @Override
    public void print(String string) {
        super.print(this.makeColourText(string));
        this.file.print(string);
    }

    @Override
    public PrintStream printf(String string, Object... args) {
        this.print(String.format(string, args));
        return this;
    }

    private String makeColourText(String string) {
        if (!this.colourful) {
            return string;
        }
        else if (string.contains("DEBUG")) {
            return string.replace("|DEBUG|", "|\u001b[37mDEBUG\u001b[0m|");
        }
        else if (string.contains("INFO ")) {
            return string.replace("|INFO |", "|\u001b[32mINFO \u001b[0m|");
        }
        else if (string.contains("TRACE")) {
            return string.replace("|TRACE|", "|\u001b[36mTRACE\u001b[0m|");
        }
        else if (string.contains("WARN ")) {
            return string.replace("|WARN |", "|\u001b[33mWARN \u001b[0m|");
        }
        else if (string.contains("ERROR")) {
            return string.replace("|ERROR|", "|\u001b[31mERROR\u001b[0m|");
        }
        else if (string.contains("FATAL")) {
            return string.replace("|FATAL|", "|\u001b[35mFATAL\u001b[0m|");
        }
        // check throwable call
        for (int depth = 1; depth < 24; depth++) {
            Class<?> caller = AppUtils.getCallerClass(depth);
            if (Objects.nonNull(caller) &&
                (UncaughtExceptionHandler.class.isAssignableFrom(caller) || Throwable.class.isAssignableFrom(caller))) {
                return "\u001b[31m" + string + "\u001b[0m";
            }
        }
        return string;
    }

}
