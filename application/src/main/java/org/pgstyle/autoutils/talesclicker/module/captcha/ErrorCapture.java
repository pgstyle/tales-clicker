package org.pgstyle.autoutils.talesclicker.module.captcha;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;

/**
 * The {@code ErrorCapture} is the container of the error dialog.
 *
 * @since 0.5-dev
 * @author PGKan
 */
public final class ErrorCapture extends Capture {

    /** Point reference of the error dialog. */
    private static final Map<Point, Color> ERROR_STENCIL = Collections.unmodifiableMap(Stencil.loadReference("./imagedb/point/error.list"));

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static ErrorCapture fromImage(BufferedImage image) {
        return new ErrorCapture(image);
    }

    private ErrorCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Find the position of the error dialog.
     *
     * @return the position of the error dialog; or {@code null} if the dialog
     *         is not found
     */
    public Point findOffset() {
        Point offset = this.getPointsOffset(ErrorCapture.ERROR_STENCIL);
        Optional.ofNullable(offset).ifPresent(o -> o.translate(36, 15));
        return offset;
    }

}