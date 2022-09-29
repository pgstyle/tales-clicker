package org.pgstyle.talesclicker.imagedb;

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
        List<Point> sortedPoints = new ArrayList<>(pointColors.keySet());
        sortedPoints.sort(Stencil.POINT_COMPARATOR);
        for (int y = 0; y < this.getImage().getHeight(); y++) {
            for (int x = 0; x < this.getImage().getWidth(); x++) {
                boolean hit = true;
                for (Point point : sortedPoints) {
                    if (x + point.x >= this.getImage().getWidth() || y + point.y >= this.getImage().getHeight() || !pointColors.get(point).equals(new Color(this.getImage().getRGB(x + point.x, y + point.y)))) {
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

}
