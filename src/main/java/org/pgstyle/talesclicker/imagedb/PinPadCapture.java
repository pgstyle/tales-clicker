package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.pgstyle.talesclicker.clicker.TalesClicker;

public class PinPadCapture extends Capture {

    public static final Point PINPAD_OFFSET = new Point(286, 99);

    private static final BufferedImage[] BUTTONS = PinPadCapture.loadButtons();

    private static BufferedImage[] loadButtons() {
        BufferedImage[] buttons = new BufferedImage[10];
        for (int i = 0; i < buttons.length; i++) {
            try {
                buttons[i] = ImageIO.read(TalesClicker.loadResource("imagedb/button-" + i + ".png"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return buttons;
    }

    public static PinPadCapture fromImage(BufferedImage image) {
        return new PinPadCapture(image);
    }

    private PinPadCapture(BufferedImage image) {
        super(image);
    }

    public final Point findNumber(int number) {
        BufferedImage image = PinPadCapture.BUTTONS[number];
        Map<Point, Color> map = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                map.put(new Point(x, y), new Color(image.getRGB(x, y)));
            }
        }
        Point point = this.getPointsOffset(map);
        point.translate(23, 23);
        return point;
    }

}
