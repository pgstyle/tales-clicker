package org.pgstyle.autoutils.talesclicker.module.notifier;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.pgstyle.autoutils.talesclicker.imagedb.Capture;

/**
 * The {@code DisconnectCapture} is a screenshot capture to test if a disconnect
 * event is occurred.
 *
 * @since 0.6-dev
 * @author PGKan
 */
public final class DisconnectCaptureStarE extends Capture {

    private static final Map<Point, Color> STAR_E_STENCIL = Capture.loadImageAsStencil("./imagedb/button/topbar-locator.png");
    /**
     * Create a capture object from an image.
     *
     * @param image the image of the captcha text
     * @return a capture object
     */
    public static DisconnectCaptureStarE fromImage(BufferedImage image) {
        return new DisconnectCaptureStarE(image);
    }

    private DisconnectCaptureStarE(BufferedImage image) {
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
        return this.getPointsOffset(DisconnectCaptureStarE.STAR_E_STENCIL, 6f / 256) == null;
    }

}
