package org.pgstyle.autoutils.talesclicker.module.running;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.imagedb.Capture;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;

public class RunningCapture extends Capture {

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static RunningCapture fromImage(BufferedImage image) {
        return new RunningCapture(image);
    }

    /** Point reference of the ready button. */
    private static final Map<Point, Color> READY_STENCIL;
    /** Point reference of the wait button. */
    private static final Map<Point, Color> WAIT_STENCIL;
    /** Point reference of the start button. */
    private static final Map<Point, Color> START_STENCIL;
    /** Point reference of the on button. */
    private static final Map<Point, Color> ON_STENCIL;

    public static final Dimension ON_DIMENSION;
    public static final Dimension LOBBY_DIMENSION;

    static {
        BufferedImage image = null;
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/button-ready.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        READY_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/button-wait.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        WAIT_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/button-start.png"));
            LOBBY_DIMENSION = new Dimension(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        START_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/button-on.png"));
            ON_DIMENSION = new Dimension(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        ON_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
    }

    protected RunningCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Get the offset of the ready button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no ready button in
     *         the screenshot
     */
    public Point findReadyOffset() {
        return this.getPointsOffset(RunningCapture.READY_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the wait button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no wait button in
     *         the screenshot
     */
    public Point findWaitOffset() {
        return this.getPointsOffset(RunningCapture.WAIT_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the start button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no start button in
     *         the screenshot
     */
    public Point findStartOffset() {
        return this.getPointsOffset(RunningCapture.START_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the on button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no on button in
     *         the screenshot
     */
    public Point findOnOffset() {
        return this.getPointsOffset(RunningCapture.ON_STENCIL, 6 / 256f);
    }

}
