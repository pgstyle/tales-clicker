package org.pgstyle.talesclicker.clicker;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class Capturer {

    public Capturer(Robot robot) {
        this.robot = robot;
    }

    private final Robot robot;

    public BufferedImage capture() {
        Point original = MouseInfo.getPointerInfo().getLocation();
        this.robot.mouseMove(0, 0);
        BufferedImage capture = this.robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        this.robot.mouseMove(original.x, original.y);
        return capture;
    }

}
