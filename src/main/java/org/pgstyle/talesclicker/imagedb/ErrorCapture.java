package org.pgstyle.talesclicker.imagedb;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class ErrorCapture extends Capture {

    public static ErrorCapture fromImage(BufferedImage image) {
        return new ErrorCapture(image);
    }

    private ErrorCapture(BufferedImage image) {
        super(image);
    }

    public Point findOffset() {
        Point offset = this.getPointsOffset(Stencil.ERROR_REFERENCES);
        Optional.ofNullable(offset).ifPresent(o -> o.translate(36, 15));
        return offset;
    }

}
