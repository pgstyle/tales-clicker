package org.pgstyle.talesclicker.action;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.function.BiConsumer;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Configuration;

public class Clicker {

    public Clicker(Robot robot) {
        this.robot = robot;
        int[] timing = Configuration.getConfig().getClickTiming();
        Application.log("loaded configuration: %s", Arrays.toString(timing));
        this.moveDelay = timing[0];
        this.clickDelay = timing[1];
        this.actionDelay = timing[2];
        action = (r, p) -> {
            r.mouseMove(p.x, p.y);
            Action.getIdler().idle(this.moveDelay);
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Action.getIdler().idle(this.clickDelay);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Action.getIdler().idle(this.actionDelay);
        };
    }

    private final Robot robot;
    private final BiConsumer<Robot, Point> action;
    private final int moveDelay;
    private final int clickDelay;
    private final int actionDelay;

    public void click(Point point) {
        Application.log("action.click %s", point);
        this.action.accept(this.robot, point);
    }

}
