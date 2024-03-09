package org.pgstyle.autoutils.talesclicker.module.fishing;

import java.awt.Color;
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

public class FishingCapture extends Capture {

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static FishingCapture fromImage(BufferedImage image) {
        return new FishingCapture(image);
    }

    /** Point reference of the collect button. */
    private static final Map<Point, Color> COLLECT_STENCIL;
    /** Point reference of the start button. */
    private static final Map<Point, Color> START_STENCIL;
    /** Point reference of the stop button. */
    private static final Map<Point, Color> STOP_STENCIL;
    /** Point reference of the send button. */
    private static final Map<Point, Color> SEND_STENCIL;
    /** Point reference of the comfirm button. */
    private static final Map<Point, Color> CONFIRM_STENCIL;
    /** Point reference of the fishing captcha. */
    private static final Map<Point, Color> CAPTCHA_STENCIL;
    /** Point reference of the fish button. */
    private static final List<Map<Point, Color>> FISH_BUTTON_STENCILS;
    /** Point reference of the fish sample. */
    private static final List<Map<Point, Color>> FISH_SAMPLE_STENCILS;

    static {
        BufferedImage image = null;
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/fishing-collect.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        COLLECT_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/fishing-start.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        START_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/fishing-stop.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        STOP_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/fishing-send.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        SEND_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/point/fishing-captcha.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        CAPTCHA_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        try {
            image = ImageIO.read(AppUtils.getResource("./imagedb/button/button-confirm-0.png"));
        } catch (IOException e) {
            Application.log(Level.ERROR, "cannot load stencil", e);
            throw new IllegalStateException("cannot initialise", e);
        }
        CONFIRM_STENCIL = Collections.unmodifiableMap(Stencil.fromImage(image));
        List<Map<Point, Color>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                image = ImageIO.read(AppUtils.getResource("./imagedb/button/fishing-" + i + ".png"));
            } catch (IOException e) {
                Application.log(Level.ERROR, "cannot load stencil", e);
                throw new IllegalStateException("cannot initialise", e);
            }
            list.add(Collections.unmodifiableMap(Stencil.fromImage(image)));
        }
        FISH_BUTTON_STENCILS = Collections.unmodifiableList(list);
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                image = ImageIO.read(AppUtils.getResource("./imagedb/mask/fishing-" + i + ".png"));
            } catch (IOException e) {
                Application.log(Level.ERROR, "cannot load stencil", e);
                throw new IllegalStateException("cannot initialise", e);
            }
            list.add(Collections.unmodifiableMap(Stencil.fromImage(image)));
        }
        FISH_SAMPLE_STENCILS = Collections.unmodifiableList(list);
    }

    protected FishingCapture(BufferedImage image) {
        super(image);
    }

    /**
     * Get the offset of the start fishing button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no start fishing
     *         button in the screenshot
     */
    public Point findStartOffset() {
        return this.getPointsOffset(FishingCapture.START_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the stop fishing button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no stop fishing
     *         button in the screenshot
     */
    public Point findStopOffset() {
        return this.getPointsOffset(FishingCapture.STOP_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the collect fishing net button in the screenshot
     * capture.
     *
     * @return the offset point; or {@code null} if there is no collect fishing
     *         net button in the screenshot
     */
    public Point findCollectOffset() {
        return this.getPointsOffset(FishingCapture.COLLECT_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the send fish button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no send fish button
     *         in the screenshot
     */
    public Point findSendOffset() {
        return this.getPointsOffset(FishingCapture.SEND_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the confirm button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no confirm button
     *         in the screenshot
     */
    public Point findConfirmOffset() {
        return this.getPointsOffset(FishingCapture.CONFIRM_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the fish captcha in the screenshot capture.
     *
     * @return the offset point; or {@code null} if the specific fish captcha is
     *         not exists in the screenshot
     */
    public Point findCaptchaOffset() {
        return this.getPointsOffset(FishingCapture.CAPTCHA_STENCIL, 6 / 256f);
    }

    /**
    /**
     * Get the offset of the a specific fish in the screenshot capture.
     *
     * @return the offset point; or {@code null} if the specific fish is not
     *         exists in the screenshot
     */
    public Point findFishOffset(int index) {
        return this.getPointsOffset(FishingCapture.FISH_SAMPLE_STENCILS.get(index), 6 / 256f);
    }

    /**
     * Get the offset of the a specific fish button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if the specific fish button is
     *         not exists in the screenshot
     */
    public Point findFishButtonOffset(int index) {
        return this.getPointsOffset(FishingCapture.FISH_BUTTON_STENCILS.get(index), 6 / 256f);
    }

    /**
     * Get the captcha dialog image from the full screenshot.
     *
     * @return the image; or {@code null} if the captcha dialog is not found
     */
    public BufferedImage getCaptchaImage() {
        Point offset = this.findCaptchaOffset();
        return Objects.isNull(offset) ? null: this.getImage(offset.x + 45, offset.y + 28, 152, 68);
    }

}
