package org.pgstyle.talesclicker.application;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class AppUtils {

    public static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyDDDAAAAAAAA");

    public static void loop(int outer, int outerLimit, int inner, int innerLimit, BiConsumer<Integer, Integer> action) {
        for (int o = outer; o < outerLimit; o++) {
            for (int i = inner; i < innerLimit; i++) {
                action.accept(o, i);
            }
        }
    }

    public static String timestamp() {
        return AppUtils.TS_FORMATTER.format(LocalDateTime.now()).substring(0, 9);
    }

    public static InputStream getResource(String name) {
        return AppUtils.class.getResourceAsStream("/META-INF/org.pgstyle/tales-clicker/" + name);
    }

    public static String getCallerText(StackTraceElement caller) {
        String className = caller.getClassName();
        String packageName = Arrays.stream(className.substring(0, className.lastIndexOf(".")).split("\\."))
                                   .map(s -> s.subSequence(0, 1))
                                   .collect(Collectors.joining("."));
        className = className.substring(className.lastIndexOf(".") + 1);
        return packageName + "." + className + ":" + caller.getLineNumber();
    }

    private AppUtils() {}

}
