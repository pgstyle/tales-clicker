package org.pgstyle.autoutils.talesclicker.launcher.main;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

import org.pgstyle.autoutils.talesclicker.common.AppResource;
import org.pgstyle.autoutils.talesclicker.launcher.Jar;

/**
 * program entrypoint
 */
public final class Main {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Jar.loadClasses("org.pgstyle").forEach(System.out::println);
    }

}
