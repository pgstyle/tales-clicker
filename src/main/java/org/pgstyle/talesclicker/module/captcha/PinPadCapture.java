package org.pgstyle.talesclicker.module.captcha;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.imagedb.Capture;
import org.pgstyle.talesclicker.imagedb.Stencil;

/**
 * The {@code PinPadCapture} is the container of the number pinpad of the
 * captcha dialog, it provides methods to find the position of the pinpad
 * button.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class PinPadCapture extends Capture {

    public static Point getOffset() {
        return new Point(PinPadCapture.PINPAD_OFFSET);
    }

    private static final Pattern LOADABLE = Pattern.compile("/imagedb/button-.*\\.png");
    
    /** Relative position of hte pinpad to the captcha dialog. */
    private static final Point PINPAD_OFFSET = new Point(286, 99);

    private static final List<Map<Point, Color>> BUTTONS = PinPadCapture.loadButtons();
    


    private static List<Map<Point, Color>> loadButtons() {
        return AppUtils.getResources("/imagedb")
                       .filter(s -> PinPadCapture.LOADABLE.matcher(s).matches())
                       .map(name -> {
                           try {
                               // all candidates are loaded under the imagedb resources with a
                               // name of button-{number}.png
                               return Stencil.fromImage(ImageIO.read(AppUtils.getResource(name)));
                           } catch (IllegalArgumentException e) {
                               // not found
                           } catch (IOException e) {
                               Application.log(Level.ERROR, "failed to load button %s, %s", name, e);
                               e.printStackTrace();
                           }
                           return null;
                       })
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }

    /**
     * Create a capture object from an image.
     *
     * @param image the image of the pinpad
     * @return a capture object
     */
    public static PinPadCapture fromImage(BufferedImage image) {
        return new PinPadCapture(image);
    }

    private PinPadCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Find the relative position of a pinpad button to the position of the pin-
     * pad.
     *
     * @param number the number of the button to be found
     * @return the position offset of the button; or {@code null} if the button
     *         is not found
     */
    public final Point findNumber(int number) {
        Point point = this.getPointsOffset(PinPadCapture.BUTTONS.get(number), 6f / 256);
        if (Objects.nonNull(point)) {
            point.translate(PinPadCapture.PINPAD_OFFSET.x, PinPadCapture.PINPAD_OFFSET.y);
            point.translate(23, 23);
        }
        return point;
    }

}
