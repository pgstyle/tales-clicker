package org.pgstyle.autoutils.talesclicker.module.notifier;

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

import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;

/**
 * The {@code DisconnectCapture} is a screenshot capture to test if a disconnect
 * event is occurred.
 *
 * @since 0.6-dev
 * @author PGKan
 */
public final class DisconnectCapture extends Capture {

    private static final Pattern LOADABLE = Pattern.compile("./imagedb/disconnect/disconnect-.*\\.png");
    
    /** All disconnect dialog candidate for testing the capture. */
    private static final List<Map<Point, Color>> TEXTS = DisconnectCapture.loadTexts();


    private static List<Map<Point, Color>> loadTexts() {
        return AppUtils.getResources("./imagedb/disconnect")
                       .filter(s -> DisconnectCapture.LOADABLE.matcher(s).matches())
                       .map(name -> {
                           try {
                               // all candidates are loaded under the imagedb resources with a
                               // name of disconnect-{number}.png
                               return Stencil.fromImage(ImageIO.read(AppUtils.getResource(name)));
                           } catch (IllegalArgumentException e) {
                               // not found
                           } catch (IOException e) {
                               Application.log(Level.ERROR, "failed to load disconnect text %s, %s", name, e);
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
     * @param image the image of the captcha text
     * @return a capture object
     */
    public static DisconnectCapture fromImage(BufferedImage image) {
        return new DisconnectCapture(image);
    }

    private DisconnectCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Detect a disconnect event.
     *
     * @return {@code true} if a disconnect event do occurred; or {@code false}
     *         otherwise
     */
    public boolean isDisconnected() {
        // check all candidates
        return DisconnectCapture.TEXTS.stream().map(pc -> this.getPointsOffset(pc, 6f / 256)).anyMatch(Objects::nonNull);
    }

}
