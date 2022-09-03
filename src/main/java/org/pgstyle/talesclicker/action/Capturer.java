package org.pgstyle.talesclicker.action;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Configuration;

public class Capturer {

    public Capturer(Robot robot) {
        this.robot = robot;
        int[] dimension = Configuration.getConfig().getCaptureArea();
        Application.log("loaded configuration: %s", Arrays.toString(dimension));
        this.area = new Rectangle(dimension[0], dimension[1]);
    }

    private final Robot robot;
    private final Rectangle area;

    public BufferedImage capture() {
        Application.log("action.capture %s", this.area);
        Point original = MouseInfo.getPointerInfo().getLocation();
        this.robot.mouseMove(0, 0);
        BufferedImage capture = this.robot.createScreenCapture(this.area);
        this.robot.mouseMove(original.x, original.y);
        return capture;
    }

}
