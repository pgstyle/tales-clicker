package org.pgstyle.talesclicker.action;

import java.awt.Robot;
import java.util.function.BiConsumer;

public final class Idler {

    Idler(Robot robot) {
        this.robot = robot;
        action = (r, t) -> {
            try { Thread.sleep(t); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };
    }

    private final Robot robot;
    private final BiConsumer<Robot, Long> action;

    public void idle(long timeout) {
        this.action.accept(this.robot, timeout);
    }

}
