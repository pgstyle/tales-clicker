package org.pgstyle.autoutils.talesclicker.launcher;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.pgstyle.autoutils.talesclicker.common.AppResource;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;

public final class Jar {

    public static List<Class<?>> loadClasses(String packagePath) {
        packagePath = packagePath.indexOf(".") != -1 ? packagePath.replace(".", "/") : packagePath;
        packagePath = Paths.get("/", packagePath).normalize().toString().replace("\\", "/");
        return AppResource.getResources(packagePath)
                .filter(p -> p.contains(".class"))
                .map(p -> p.substring(1, p.length() - 6).replace("/", "."))
                .map(Jar::loadClass)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static Class<?> loadClass(String className) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            Console.log(Level.WARN, "failed to load class: " + className, e);
            return null;
        }
    }

}
