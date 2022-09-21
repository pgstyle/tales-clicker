package org.pgstyle.talesclicker.module.captcha;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Objects;

import org.pgstyle.talesclicker.imagedb.Capture;
import org.pgstyle.talesclicker.imagedb.Stencil;

public class FullCapture extends Capture {

    public static FullCapture fromImage(BufferedImage image) {
        return new FullCapture(image);
    }

    private FullCapture(BufferedImage image) {
        super(image);
    }

    public CaptchaCapture getCaptchaCapture() {
        Point offset = this.findOffset();
        return Objects.isNull(offset) ? null: CaptchaCapture.fromImage(this.getImage(offset.x + 155, offset.y + 78, 75, 50));
    }

    public PinPadCapture getPinPadCapture() {
        Point offset = this.findOffset();
        return Objects.isNull(offset) ? null: PinPadCapture.fromImage(this.getImage(offset.x + PinPadCapture.PINPAD_OFFSET.x, offset.y + PinPadCapture.PINPAD_OFFSET.y, 156, 193));
    }

    public Point findOffset() {
        return this.getPointsOffset(Stencil.CAPTCHA_STENCIL);
    }

}
