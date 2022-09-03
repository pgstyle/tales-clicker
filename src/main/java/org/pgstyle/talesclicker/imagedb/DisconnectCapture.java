package org.pgstyle.talesclicker.imagedb;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

public class DisconnectCapture extends Capture {

    private static final BufferedImage[] TEXTS = DisconnectCapture.loadButtons();

    private static BufferedImage[] loadButtons() {
        BufferedImage[] buttons = new BufferedImage[2];
        for (int i = 0; i < buttons.length; i++) {
            try {
                buttons[i] = ImageIO.read(PinPadCapture.class.getResourceAsStream("/imagedb/disconnect-" + i + ".png"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return buttons;
    }

    public static DisconnectCapture fromImage(BufferedImage image) {
        return new DisconnectCapture(image);
    }

    private DisconnectCapture(BufferedImage image) {
        super(image);
    }

    public Point findOffset() {
        Point offset = this.getPointsOffset(Stencil.ERROR_REFERENCES);
        Optional.ofNullable(offset).ifPresent(o -> o.translate(36, 15));
        return offset;
    }

    public boolean isDisconnected() {
        return IntStream.range(0, DisconnectCapture.TEXTS.length).mapToObj(this::findText).anyMatch(Objects::nonNull);
    }

    public final Point findText(int index) {
        BufferedImage image = DisconnectCapture.TEXTS[index];
        Map<Point, Color> map = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                map.put(new Point(x, y), new Color(image.getRGB(x, y)));
            }
        }
        return this.getPointsOffset(map);
    }

}
