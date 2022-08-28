package org.pgstyle.talesclicker.imagedb;

import java.awt.image.BufferedImage;

public class CaptchaCapture extends Capture {

    public static CaptchaCapture fromImage(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                 int argb = image.getRGB(x, y);
                 int b = argb & 0x000000ff;
                 b = b > 120 ? 0 : 255;
                 image.setRGB(x, y, (b << 16) + (b << 8) + b);
            }
        }
        return new CaptchaCapture(image);
    }

    private CaptchaCapture(BufferedImage image) {
        super(image);
    }

}
