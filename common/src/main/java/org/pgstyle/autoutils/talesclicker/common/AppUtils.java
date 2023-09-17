package org.pgstyle.autoutils.talesclicker.common;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.pgstyle.autoutils.talesclicker.common.Console.Level;

/**
 * The {@code AppUtils (Application Utilities)} provides utility methods.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class AppUtils {

    /** Standard date-time formatter for making timestamp string. */
    public static final DateTimeFormatter TS_FORMATTER;

    private static final String HOSTNAME;

    static {
        String hostname = System.getenv("HOSTNAME");
        if (Objects.isNull(hostname)) {
            try {
                byte[] bytes = new byte[1024];
                int length = Runtime.getRuntime().exec("hostname").getInputStream().read(bytes);
                hostname = new String(bytes).substring(0, length);
            } catch (IOException e) {
                Console.log(Level.WARN, "failed to get hostname, %s", e);
                hostname = "localhost";
            }
        }
        HOSTNAME = hostname.trim();
        TS_FORMATTER = DateTimeFormatter.ofPattern("yyDDDAAAAAAAA");
    }

    /**
     * 2-layer nested for-loop.
     *
     * @param outer      start of outer loop counter
     * @param outerLimit exclusive limit of outer loop counter
     * @param inner      start of inner loop counter
     * @param innerLimit exclusive limit of inner loop counter
     * @param action     the action to perform in the loop body
     */
    public static void nestedLoop(int outer, int outerLimit, int inner, int innerLimit,
            BiConsumer<Integer, Integer> action) {
        for (int o = outer; o < outerLimit; o++) {
            for (int i = inner; i < innerLimit; i++) {
                action.accept(o, i);
            }
        }
    }

    /**
     * Get current timestamp in {@link AppUtils#TS_FORMATTER} format.
     *
     * @return timestring string
     */
    public static String timestamp() {
        return AppUtils.TS_FORMATTER.format(LocalDateTime.now()).substring(0, 9);
    }

    /**
     * Get host name of the machine.
     *
     * @return hostname
     */
    public static String hostname() {
        return AppUtils.HOSTNAME;
    }

    private AppUtils() {
    }

}
