package org.pgstyle.autoutils.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Screen capture container with subpicture and point offset utility.
 *
 * @since 0.1-dev
 * @author PGKan
 */
public class Capture {

    protected Capture(BufferedImage image) {
        this.image = image;
    }

    private final BufferedImage image;

    /**
     * Get the underlying image.
     *
     * @return an image
     */
    public final BufferedImage getImage() {
        return this.image;
    }

    /**
     * Get a specific area of the underlying image.
     *
     * @return an image
     */
    public final BufferedImage getImage(int x, int y, int width, int height) {
        return this.image.getSubimage(x, y, width, height);
    }

    /**
     * Find the specified point reference in the underlying image.
     *
     * @param pointColors point reference
     * @return the offset of the point reference; or {@code null} if the point
     *         reference does not match in the underlying image
     */
    public Point getPointsOffset(Map<Point, Color> pointColors) {
        return this.getPointsOffset(pointColors, 0);
    }

    public Point getPointsOffset(Map<Point, Color> pointColors, float delta) {
        List<Point> sortedPoints = new ArrayList<>(pointColors.keySet());
        sortedPoints.sort(Stencil.POINT_COMPARATOR);
        for (int y = 0; y < this.getImage().getHeight(); y++) {
            for (int x = 0; x < this.getImage().getWidth(); x++) {
                boolean hit = true;
                for (Point point : sortedPoints) {
                    if (x + point.x >= this.getImage().getWidth() || y + point.y >= this.getImage().getHeight() || Capture.colourDifferent(pointColors.get(point), new Color(this.getImage().getRGB(x + point.x, y + point.y))) > delta) {
                        hit = false;
                        break;
                    }
                }
                if (hit) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    public static float colourDifferent(Color color1, Color color2) {
        int rgb1 = color1.getRGB();
        int rgb2 = color2.getRGB();
        int r = Math.abs((rgb1 >> 16 & 0xff) - (rgb2 >> 16 & 0xff));
        int g = Math.abs((rgb1 >> 8 & 0xff) - (rgb2 >> 8 & 0xff));
        int b = Math.abs((rgb1 & 0xff) - (rgb2 & 0xff));
        return Math.max(Math.max(r, g), b) / 256f;
    }

}
