package org.pgstyle.talesclicker.action;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.application.Application.Level;

public final class Capturer {

    Capturer(Robot robot) {
        this.robot = robot;
        this.defaultArea = Configuration.getConfig().getCaptureArea();
        Application.log(Level.DEBUG, "loaded configuration: %s", Arrays.toString(this.defaultArea));
    }

    private final Robot robot;
    private final int[] defaultArea;

    public BufferedImage capture() {
        return this.capture(defaultArea[0], defaultArea[1], defaultArea[2], defaultArea[3]);
    }

    public BufferedImage capture(int x, int y, int width, int height) {
        synchronized (this.robot) {
            Rectangle area = new Rectangle(x, y, width, height);
            Application.log(Level.DEBUG, "action.capture %s", area);
            Point original = MouseInfo.getPointerInfo().getLocation();
            this.robot.mouseMove(0, 0);
            BufferedImage capture = this.robot.createScreenCapture(area);
            this.robot.mouseMove(original.x, original.y);
            return capture;
        }
    }

}
