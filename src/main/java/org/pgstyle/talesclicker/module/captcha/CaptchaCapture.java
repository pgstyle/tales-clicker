package org.pgstyle.talesclicker.module.captcha;

import java.awt.image.BufferedImage;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.imagedb.Capture;

public class CaptchaCapture extends Capture {

    public static CaptchaCapture fromImage(BufferedImage image) {
        AppUtils.loop(0, image.getHeight(), 0, image.getWidth(), (y, x) -> {
            int b = image.getRGB(x, y) & 0x000000ff;
            b = b > 120 ? 0 : 255;
            image.setRGB(x, y, (b << 16) + (b << 8) + b);
        });
        return new CaptchaCapture(image);
    }

    private CaptchaCapture(BufferedImage image) {
        super(image);
    }

}
