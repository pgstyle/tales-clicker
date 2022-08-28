package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Stencil {
    public static final Map<Point, Color> STENCIL_REFERENCES = Collections.unmodifiableMap(Stencil.loadReference("/imagedb/stencil.list"));
    public static final Map<Point, Color> ERROR_REFERENCES = Collections.unmodifiableMap(Stencil.loadReference("/imagedb/error.list"));

    private static Map<Point, Color> loadReference(String name) {
        Properties list = new Properties();
        try {
            list.load(Stencil.class.getResourceAsStream(name));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list.stringPropertyNames().stream().collect(Collectors.toMap(k -> new Point(Integer.parseInt(k.split(",")[0]), Integer.parseInt(k.split(",")[1])), k -> new Color(Integer.parseInt(list.getProperty(k), 16))));
    }

    public static final  Comparator<Point> POINT_COMPARATOR = (a, b) -> a.y < b.y || (a.y == b.y && a.x < b.x) ? -1 : 1;

    
}
