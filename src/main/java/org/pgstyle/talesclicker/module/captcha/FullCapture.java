package org.pgstyle.talesclicker.module.captcha;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Objects;

import org.pgstyle.talesclicker.imagedb.Capture;
import org.pgstyle.talesclicker.imagedb.Stencil;

/**
 * The {@code FullCapture} is the container of a screenshot capture, it provides
 * methods to find captcha dialog and the clickable pin pad.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public class FullCapture extends Capture {

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
        return this.getPointsOffset(Stencil.CAPTCHA_STENCIL);
    }

}
