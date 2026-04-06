package org.pgstyle.autoutils.talesclicker.module.daily;

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

public class DailyCapture extends Capture {

    /**
     * Create a capture object from an image.
     *
     * @param image a screenshot image
     * @return a capture object
     */
    public static DailyCapture fromImage(BufferedImage image) {
        return new DailyCapture(image);
    }

    private static final Map<Point, Color> LOCATOR_STENCIL;
    private static final Map<Point, Color> LOGIN_STENCIL;
    private static final Map<Point, Color> ACTIVE_STENCIL;
    private static final Map<Point, Color> INACTIVE_STENCIL;
    private static final Map<Point, Color> BONUS_STENCIL;
    private static final Map<Point, Color> REWARD_STENCIL;
    private static final Map<Point, Color> NO_REWARD_STENCIL;
    private static final Map<Point, Color> CLOSE_MISSION_STENCIL;

    /** Point reference of the comfirm button. */
    private static final Map<Point, Color> CONFIRM_STENCIL;
    /** Point reference of the close button. */
    private static final Map<Point, Color> CLOSE_STENCIL;

    static {
        LOCATOR_STENCIL = Capture.loadImageAsStencil("./imagedb/button/topbar-locator.png");
        LOGIN_STENCIL = Capture.loadImageAsStencil("./imagedb/point/daily-login.png");
        ACTIVE_STENCIL = Capture.loadImageAsStencil("./imagedb/button/daily-mission-0.png");
        INACTIVE_STENCIL = Capture.loadImageAsStencil("./imagedb/button/daily-mission-1.png");
        BONUS_STENCIL = Capture.loadImageAsStencil("./imagedb/button/login-bonus.png");
        REWARD_STENCIL = Capture.loadImageAsStencil("./imagedb/button/get-reward.png");
        NO_REWARD_STENCIL = Capture.loadImageAsStencil("./imagedb/point/no-reward.png");
        CLOSE_MISSION_STENCIL = Capture.loadImageAsStencil("./imagedb/button/close-mission.png");
        CONFIRM_STENCIL = Capture.loadImageAsStencil("./imagedb/button/button-confirm-0.png");
        CLOSE_STENCIL = Capture.loadImageAsStencil("./imagedb/button/close.png");
    }

    protected DailyCapture(BufferedImage image) {
        super(image);
    }

    public Point findLocaterOffset() {
        return this.getPointsOffset(DailyCapture.LOCATOR_STENCIL, 6 / 256f);
    }

    public Point findLoginOffset() {
        return this.getPointsOffset(DailyCapture.LOGIN_STENCIL, 6 / 256f);
    }

    public Point findActiveOffset() {
        return this.getPointsOffset(DailyCapture.ACTIVE_STENCIL, 6 / 256f);
    }

    public Point findInactiveOffset() {
        return this.getPointsOffset(DailyCapture.INACTIVE_STENCIL, 6 / 256f);
    }

    public Point findBonusOffset() {
        return this.getPointsOffset(DailyCapture.BONUS_STENCIL, 6 / 256f);
    }

    public Point findRewardOffset() {
        return this.getPointsOffset(DailyCapture.REWARD_STENCIL, 6 / 256f);
    }

    public Point findNoRewardOffset() {
        return this.getPointsOffset(DailyCapture.NO_REWARD_STENCIL, 6 / 256f);
    }

    public Point findCloseMissionOffset() {
        return this.getPointsOffset(DailyCapture.CLOSE_MISSION_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the confirm button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no confirm button
     *         in the screenshot
     */
    public Point findConfirmOffset() {
        return this.getPointsOffset(DailyCapture.CONFIRM_STENCIL, 6 / 256f);
    }

    /**
     * Get the offset of the close button in the screenshot capture.
     *
     * @return the offset point; or {@code null} if there is no close button
     *         in the screenshot
     */
    public Point findCloseOffset() {
        return this.getPointsOffset(DailyCapture.CLOSE_STENCIL, 6 / 256f);
    }

}
