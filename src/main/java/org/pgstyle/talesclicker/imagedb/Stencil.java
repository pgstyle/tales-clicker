package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import org.pgstyle.talesclicker.application.AppUtils;

/**
 * Point references for finding point offset in a screen capture.
 *
 * @since 0.2-dev
 * @author PGKan
 */
public final class Stencil {

    /** Point reference of the captcha dialog. */
    public static final Map<Point, Color> CAPTCHA_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("/imagedb/captcha.list"));
    /** Point reference of the error dialog. */
    public static final Map<Point, Color> ERROR_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("/imagedb/error.list"));

    private static Map<Point, Color> loadReference(String name) {
        Properties list = new Properties();
        try {
            list.load(AppUtils.getResource(name));
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("unloadable reference: " + name, e);
        }
        return list.stringPropertyNames().stream().collect(Collectors.toMap(k -> new Point(Integer.parseInt(k.split(",")[0]), Integer.parseInt(k.split(",")[1])), k -> new Color(Integer.parseInt(list.getProperty(k), 16))));
    }

    private static final Random random = new Random();

    public static Map<Point, Color> fromImage(BufferedImage image) {
        return Stencil.fromImage(image, 0);
    }

    public static Map<Point, Color> fromImage(BufferedImage image, float degradation) {
        Map<Point, Color> map = new HashMap<>();
        AppUtils.nestedLoop(0, image.getHeight(), 0, image.getWidth(), (y, x) -> {
            if (random.nextDouble() > degradation) {
                map.put(new Point(x, y), new Color(image.getRGB(x, y)));
            }
        });
        return map;
    }

    /** This comparator can sort points in top-to-down, left-to-right order. */
    public static final Comparator<Point> POINT_COMPARATOR = (a, b) -> a.y < b.y || (a.y == b.y && a.x < b.x) ? -1 : 1;

    private Stencil() {}

}
