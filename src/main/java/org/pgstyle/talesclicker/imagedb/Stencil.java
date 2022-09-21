package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.pgstyle.talesclicker.application.AppUtils;

public final class Stencil {

    public static final Map<Point, Color> CAPTCHA_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("imagedb/captcha.list"));
    public static final Map<Point, Color> ERROR_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("imagedb/error.list"));

    private static Map<Point, Color> loadReference(String name) {
        Properties list = new Properties();
        try {
            list.load(AppUtils.getResource(name));
        } catch (IOException e) {
            throw new IllegalArgumentException("unloadable reference: " + name, e);
        }
        return list.stringPropertyNames().stream().collect(Collectors.toMap(k -> new Point(Integer.parseInt(k.split(",")[0]), Integer.parseInt(k.split(",")[1])), k -> new Color(Integer.parseInt(list.getProperty(k), 16))));
    }

    public static final  Comparator<Point> POINT_COMPARATOR = (a, b) -> a.y < b.y || (a.y == b.y && a.x < b.x) ? -1 : 1;

    private Stencil() {}

}
