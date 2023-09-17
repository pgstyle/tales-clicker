package org.pgstyle.autoutils.talesclicker.action;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.function.BiConsumer;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;
import org.pgstyle.autoutils.talesclicker.application.AppConfig;

/**
 * Pointer clicker.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Clicker {

    Clicker(Robot robot) {
        this.robot = robot;
        long[] timing = AppConfig.getConfig().getClickTiming();
        this.moveDelay = timing[0];
        this.clickDelay = timing[1];
        this.actionDelay = timing[2];
        action = (r, p) -> {
            r.mouseMove(p.x, p.y);
            Actions.getIdler().idle(this.moveDelay);
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Actions.getIdler().idle(this.clickDelay);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Actions.getIdler().idle(this.actionDelay);
        };
    }

    private final Robot robot;
    private final BiConsumer<Robot, Point> action;
    private final long moveDelay;
    private final long clickDelay;
    private final long actionDelay;

    /**
     * Move the cursor to specific point and click.
     *
     * @param point the location to be moved to
     */
    public void click(Point point) {
        synchronized (this.robot) {
            Console.log(Level.DEBUG, "action.click %s", point);
            // move cursor to clicking position, press down mouse button,
            // and then release mouse button
            this.action.accept(this.robot, point);
        }
    }

}
