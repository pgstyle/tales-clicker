package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Capture {

    protected Capture(BufferedImage image) {
        this.image = image;
    }

    private final BufferedImage image;

    public final BufferedImage getImage() {
        return this.image;
    }

    public final BufferedImage getImage(int x, int y, int width, int height) {
        return this.image.getSubimage(x, y, width, height);
    }

    public Point getPointsOffset(Map<Point, Color> pointColors) {
        List<Point> sortedPoints = new ArrayList<>(pointColors.keySet());
        sortedPoints.sort((a, b) -> a.y < b.y ? -1 : (a.y == b.y ? (a.x < b.x ? -1 : 1) : 1));
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

    public void save(File file) {
        try {
            ImageIO.write(this.image, "png", file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
