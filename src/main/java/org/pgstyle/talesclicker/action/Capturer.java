package org.pgstyle.talesclicker.action;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;

/**
 * Screenshot capturer.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Capturer {

    Capturer(Robot robot) {
        this.robot = robot;
        this.defaultArea = Configuration.getConfig().getCaptureArea();
    }

    private final Robot robot;
    private final int[] defaultArea;

    public BufferedImage capture() {
        return this.capture(defaultArea[0], defaultArea[1], defaultArea[2], defaultArea[3]);
    }

    /**
     * Take a screenshot of the specified area.
     *
     * @param x starting position X coordinate
     * @param y starting position Y coordinate
     * @param width width of the screenshot area
     * @param height height of the screenshot area
     * @return the captured image
     */
    public BufferedImage capture(int x, int y, int width, int height) {
        // Since cursor need to move out of the capture area before taking
        // screenshot, the capture action is required to synchronise with other
        // actions.
        synchronized (this.robot) {
            Rectangle area = new Rectangle(x, y, width, height);
            Application.log(Level.DEBUG, "action.capture %s", area);
            Point original = MouseInfo.getPointerInfo().getLocation();
            // Move cursor to buttom-right of the capture area, such that the
            // cursor body is outside of the area.
            this.robot.mouseMove(x + width, y + height);
            BufferedImage capture = this.robot.createScreenCapture(area);
            // revert the cursor position
            this.robot.mouseMove(original.x, original.y);
            return capture;
        }
    }

}
