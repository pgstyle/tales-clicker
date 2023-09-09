package org.pgstyle.talesclicker.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pgstyle.talesclicker.application.Application.Level;

/**
 * The {@code AppUtils (Application Utilities)} provides utility methods.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class AppUtils {

    /** Standard date-time formatter for making timestamp string. */
    public static final DateTimeFormatter TS_FORMATTER;

    private static final String ASSET_STORE;

    private static final String HOSTNAME;

    static {
        String hostname = System.getenv("HOSTNAME");
        if (Objects.isNull(hostname)) {
            try {
                byte[] bytes = new byte[1024];
                int length = Runtime.getRuntime().exec("hostname").getInputStream().read(bytes);
                hostname = new String(bytes).substring(0, length);
            } catch (IOException e) {
                Application.log(Level.WARN, "failed to get hostname, %s", e);
                hostname = "localhost";
            }
        }
        HOSTNAME = hostname.trim();
        ASSET_STORE = "/META-INF/org.pgstyle/tales-clicker/";
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

    /**
     * Get resource from the tales-clicker resource class path.
     *
     * @param name the name of the resource to be loaded
     * @return the input stream of the resource
     * @throws IllegalArgumentException if resource not found
     */
    public static InputStream getResource(String name) {
        return Optional.ofNullable(AppUtils.ASSET_STORE + name)
                .map(Paths::get).map(Object::toString).map(p -> p.replace("\\", "/"))
                .map(AppUtils.class::getResourceAsStream).filter(Objects::nonNull)
                .orElseThrow(() -> new IllegalArgumentException(
                        "cannot find resource \"" + name + "\" in application resources"));
    }

    /**
     * Get a stream of resource paths in the application class resources
     * location ({@code /META-INF/org.pgstyle/tales-clicker}). This method is
     * equivalent to {@code AppUtils.getResources("/")}
     *
     * @return stream of paths
     */
    public static Stream<String> getResources() {
        return AppUtils.getResources("/");
    }

    /**
     * Get a stream of resource paths in the specified path under the
     * application class resources location
     * ({@code /META-INF/org.pgstyle/tales-clicker}).
     *
     * @param path the path to be scanned
     * @return stream of paths
     */
    public static Stream<String> getResources(String path) {
        List<String> files = new ArrayList<>();
        int i = 0;
        do {
            files.addAll(AppUtils.listDirectory(path).map((path + (path.endsWith("/") ? "" : "/"))::concat)
                    .collect(Collectors.toList()));
            path = i < files.size() ? files.get(i) : null;
        } while (++i < files.size());
        return files.stream();
    }

    public static Stream<String> listDirectory(String path) {
        List<String> files = new ArrayList<>();
        String target = Paths.get(AppUtils.ASSET_STORE + path).normalize().toString().replace("\\",
                "/");
        URL url = AppUtils.class.getResource(target);
        // check execution classpath type
        if (url.getProtocol().startsWith("jar")) {
            AppUtils.findInJar(files, path, target);
        } else {
            AppUtils.findInFileSystem(files, path, target);
        }
        files.sort(String::compareTo);
        return files.stream();
    }

    private static void findInFileSystem(List<String> files, String path, String target) {
        InputStream is = AppUtils.class.getResourceAsStream(target);
        if (Objects.isNull(is) || !is.getClass().getName().contains("ByteArrayInputStream")) {
            // path isn't a directory
            Application.log(Level.WARN, "%s is't a directory", target);
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                files.add(resource);
            }
        } catch (IOException e) {
            // unable to read resource list
            Application.log(Level.WARN, "unable to read resource list: " + path, e);
        }
    }

    private static void findInJar(List<String> files, String path, String target) {
        Enumeration<JarEntry> entries = null;
        try (JarFile jar = new JarFile(AppUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath())) {
            entries = jar.entries();
        } catch (IOException e) {
            Application.log(Level.ERROR, "unable to read Jar entry: " + path, e);
            throw new IllegalStateException("Jar classpath not available", e);
        }
        while (Objects.nonNull(entries) && entries.hasMoreElements()) {
            String file = entries.nextElement().getName();
            if (file.length() > target.length() && file.startsWith(target.substring(1))) {
                files.add(file.substring(target.length()));
            }
        }
    }

    /**
     * Get the class of the caller of the caller of this method.
     *
     * @return the class object; or {@code null} if the caller of this method
     *         has no caller
     */
    public static Class<?> getCallerClass() {
        return AppUtils.getCallerClass(1);
    }

    /**
     * Get the class of the caller of the caller of this method at the specific
     * call stack depth.
     *
     * @param depth call stack depth
     * @return the class object; or {@code null} if the caller of this method
     *         has no caller
     */
    public static Class<?> getCallerClass(int depth) {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[depth + 3].getClassName());
        } catch (ReflectiveOperationException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Get the top-level class of a class object. If the given class is an inner
     * class, the outer class will be returned; otherwise the class itself will
     * be returned.
     *
     * @param clazz the class
     * @return the outer class if the given class is an inner class; or the
     *         class itself
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Class<?> getTopLevelClass(Class<?> clazz) {
        return Optional.ofNullable((Class) clazz.getEnclosingClass()).map(AppUtils::getTopLevelClass)
                .orElse((Class) clazz);
    }

    /**
     * Get the full text of the caller of the caller of this method.
     *
     * @return the class name text or {@code null} if the caller of this method
     *         has no caller
     */
    public static String getCallerText() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 3) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
            String className = caller.getClassName();
            String packageName = Arrays.stream(className.substring(0, className.lastIndexOf(".")).split("\\."))
                    .map(s -> s.subSequence(0, 1))
                    .collect(Collectors.joining("."));
            className = className.substring(className.lastIndexOf(".") + 1);
            return packageName + "." + className + ":" + caller.getLineNumber();
        }
        return null;
    }

    private AppUtils() {
    }

}
