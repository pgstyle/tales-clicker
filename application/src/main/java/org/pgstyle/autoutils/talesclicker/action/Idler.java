package org.pgstyle.autoutils.talesclicker.action;

import java.awt.Robot;
import java.util.function.BiConsumer;

/**
 * Idler, thread sleeper.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Idler {

    Idler(Robot robot) {
        this.robot = robot;
        action = (r, t) -> {
            try {
                Thread.sleep(t);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted");
            }
        };
    }

    private final Robot robot;
    private final BiConsumer<Robot, Long> action;

    /**
     * Idle for the specific timeout in milliseconds.
     *
     * @param timeout the waiting timeout
     */
    public void idle(long timeout) {
        this.action.accept(this.robot, timeout);
    }

}
