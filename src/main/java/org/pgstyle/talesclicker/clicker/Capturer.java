package org.pgstyle.talesclicker.clicker;

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
        this.robot.mouseMove(0, 0);
        return this.robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

}
