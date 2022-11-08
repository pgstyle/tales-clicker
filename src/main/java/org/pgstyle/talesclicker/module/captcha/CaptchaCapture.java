package org.pgstyle.talesclicker.module.captcha;

import java.awt.image.BufferedImage;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.imagedb.Capture;

/**
 * The {@code CaptchaCapture} is the container of the captcha text of the
 * captcha dialog. Upon the creation of the capture object, channel extraction
 * and thresholding will be performed to the underlying image.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class CaptchaCapture extends Capture {

    /**
     * Create a capture object from an image.
     *
     * @param image the image of the captcha text
     * @return a capture object
     */
    public static CaptchaCapture fromImage(BufferedImage image) {
        AppUtils.nestedLoop(0, image.getHeight(), 0, image.getWidth(), (y, x) -> {
            // extract blue channel only
            int b = image.getRGB(x, y) & 0x000000ff;
            // thresholding at 120 and flip colour
            b = b > 120 ? 0 : 255;
            // reapply pixel colour
            image.setRGB(x, y, (b << 16) + (b << 8) + b);
        });
        return new CaptchaCapture(image);
    }

    private CaptchaCapture(BufferedImage image) {
        super(image);
    }

}
