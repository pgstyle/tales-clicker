package org.pgstyle.autoutils.talesclicker.module.testing;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;

public class TestingCapture extends Capture {

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static TestingCapture fromImage(BufferedImage image) {
        return new TestingCapture(image);
    }

    private static final Map<Point, Color> CHROME_STENCIL;

    static {
        BufferedImage image = null;
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/test-chrome.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        CHROME_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
    }

    protected TestingCapture(BufferedImage image) {
        super(image);
    }

    public Point findOffset() {
        return this.getPointsOffset(TestingCapture.CHROME_STENCIL);
    }

}
