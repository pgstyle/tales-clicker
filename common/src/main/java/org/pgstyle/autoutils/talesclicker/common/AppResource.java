package org.pgstyle.autoutils.talesclicker.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pgstyle.autoutils.talesclicker.common.Console.Level;

public final class AppResource {

    private static final Path store = Paths.get("/org.pgstyle/autoutils/tales-clicker/");

    /**
     * Get resource from the tales-clicker resource class path.
     *
     * @param name the name of the resource to be loaded
     * @return the input stream of the resource
     * @throws IllegalArgumentException if resource not found
     */
    public static InputStream getResource(String name) {
        return Optional.ofNullable(name)
                .map(AppResource.store::resolve).map(Path::normalize)
                .map(Object::toString).map(p -> p.replace("\\", "/"))
                .map(AppResource.class::getResourceAsStream).filter(Objects::nonNull)
                .orElseThrow(() -> new IllegalArgumentException(
                        "cannot find resource \"" + name + "\" in application resources"));
    }

    /**
     * Get a stream of resource paths in the application class resources
     * location ({@code /META-INF/org.pgstyle/autoutils/tales-clicker}). This
     * method is equivalent to {@code AppResource.getResources("./")}
     *
     * @return stream of paths
     */
    public static Stream<String> getResources() {
        return AppResource.getResources("./");
    }

    /**
     * Get a stream of resource paths in the specified path under the
     * application class resources location
     * ({@code /META-INF/org.pgstyle/autoutils/tales-clicker}).
     *
     * @param path the path to be scanned
     * @return stream of paths
     */
    public static Stream<String> getResources(String path) {
        List<String> files = new ArrayList<>();
        int i = 0;
        path = Paths.get(path).normalize().toString().replace("\\", "/");
        do {
            files.addAll(AppResource.listDirectory(path).map((path + (path.endsWith("/") ? "" : "/"))::concat)
                    .collect(Collectors.toList()));
            path = i < files.size() ? files.get(i) : null;
        } while (i++ < files.size());
        return files.stream();
    }

    public static Stream<String> listDirectory(String path) {
        Set<String> files = new TreeSet<>();
        path = Paths.get(path).normalize().toString().replace("\\", "/");
        String target = AppResource.store.resolve(path).normalize().toString().replace("\\", "/");
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(target.substring(1));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getPath().contains(path)) {
                    files.addAll(AppResource.listDirectory(url, path));
                }
            }
        } catch (IOException e) {
            Console.log(Level.ERROR, "cannot get resources list", e);
        }
        return files.stream();
    }

    private static List<String> listDirectory(URL url, String path) {
        List<String> files = new ArrayList<>();
        // check execution classpath type
        if (url.getProtocol().startsWith("jar")) {
            AppResource.findInJar(files, url, path);
        } else {
            AppResource.findInFileSystem(files, url, path);
        }
        return files;
    }

    private static void findInJar(List<String> files, URL url, String path) {
        try (JarFile jar = new JarFile(url.getPath().substring(5, url.getPath().lastIndexOf("!")))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (Objects.nonNull(entries) && entries.hasMoreElements()) {
                String file = entries.nextElement().getName();
                if (file.length() > path.length() && file.startsWith(path.substring(1))) {
                    files.add(file.substring(path.length()));
                }
            }
        } catch (IOException e) {
            Console.log(Level.ERROR, "unable to read Jar entry: ", e);
            throw new IllegalStateException("Jar classpath not available", e);
        }
    }

    private static void findInFileSystem(List<String> files, URL url, String path) {
        try (InputStream is = url.openStream()) {
            if (Objects.isNull(is) || !is.getClass().getName().contains("ByteArrayInputStream")) {
                // path isn't a directory
                return;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String resource;
                while ((resource = br.readLine()) != null) {
                    files.add(resource);
                }
            }
        } catch (IOException e) {
            // unable to read resource list
            Console.log(Level.WARN, "unable to read resource list: " + path, e);
        }
    }

    private static void findInFileSystem(List<String> files, String path, String target) {
        InputStream is = AppResource.class.getResourceAsStream(target);
        if (Objects.isNull(is) || !is.getClass().getName().contains("ByteArrayInputStream")) {
            // path isn't a directory
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String resource;
            while ((resource = br.readLine()) != null) {
                files.add(resource);
            }
        } catch (IOException e) {
            // unable to read resource list
            Console.log(Level.WARN, "unable to read resource list: " + path, e);
        }
    }

    private static void findInJar(List<String> files, String path, String target) {
        try (JarFile jar = new JarFile(
                AppResource.class.getProtectionDomain().getCodeSource().getLocation().getPath())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (Objects.nonNull(entries) && entries.hasMoreElements()) {
                String file = entries.nextElement().getName();
                if (file.length() > target.length() && file.startsWith(target.substring(1))) {
                    files.add(file.substring(target.length()));
                }
            }
        } catch (IOException e) {
            Console.log(Level.ERROR, "unable to read Jar entry: " + path, e);
            throw new IllegalStateException("Jar classpath not available", e);
        }
    }

    private AppResource() {}

}
