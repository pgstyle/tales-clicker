package org.pgstyle.autoutils.talesclicker.module.captcha;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;

/**
 * The {@code FullCapture} is the container of a screenshot capture, it provides
 * methods to find captcha dialog and the clickable pin pad.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class FullCapture extends Capture {

    /** Point reference of the captcha dialog. */
    private static final Map<Point, Color> CAPTCHA_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("./imagedb/point/captcha.list"));

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static FullCapture fromImage(BufferedImage image) {
        return new FullCapture(image);
    }

    private FullCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Get the captcha dialog capture object from the full screenshot.
     *
     * @return the capture object; or {@code null} if the captcha dialog is not
     *         found
     */
    public CaptchaCapture getCaptchaCapture() {
        Point offset = this.findOffset();
        return Objects.isNull(offset) ? null: CaptchaCapture.fromImage(this.getImage(offset.x + 155, offset.y + 78, 75, 50));
    }

    /**
     * Get the captcha dialog pinpad capture object from the full screenshot.
     *
     * @return the capture object; or {@code null} if the captcha dialog is not
     *         found
     */
    public PinPadCapture getPinPadCapture() {
        Point offset = this.findOffset();
        return Objects.isNull(offset) ? null: PinPadCapture.fromImage(this.getImage(offset.x + PinPadCapture.getOffset().x, offset.y + PinPadCapture.getOffset().y, 156, 193));
    }

    /**
     * Get the offset of the captcha dialog in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no captcha dialog
     *         in the screenshot
     */
    public Point findOffset() {
        return this.getPointsOffset(FullCapture.CAPTCHA_STENCIL);
    }

}
