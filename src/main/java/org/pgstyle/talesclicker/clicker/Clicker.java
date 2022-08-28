package org.pgstyle.talesclicker.clicker;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.function.BiConsumer;

public class Clicker {

    private static final BiConsumer<Robot, Point> ACTION = (r, p) -> {
        r.mouseMove(p.x, p.y);
        try { Thread.sleep(250); } catch (InterruptedException e) {}
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        try { Thread.sleep(30); } catch (InterruptedException e) {}
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    };

    public Clicker(Robot robot) {
        this.robot = robot;
    }

    private final Robot robot;

    public void click(Point point) {
        Clicker.ACTION.accept(this.robot, point);
    }

}
