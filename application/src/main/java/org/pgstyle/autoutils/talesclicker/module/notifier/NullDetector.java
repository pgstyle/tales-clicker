package org.pgstyle.autoutils.talesclicker.module.notifier;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;

/**
 * The {@code NullNotifier} is a placeholder notifier as default notifier to use
 * when an event is detected with no notifier is set up.
 *
 * @since 1.0
 * @author PGKan
 */
public final class NullDetector implements Detector {

    /**
     * @return {@code false}, always
     */
    @Override
    public boolean detect() {
        Console.log(Level.WARN, "The NullDetector should not be use as method of detection, do implement and connect a detector for detecting an event.");
        // NOP
        return false;
    }

    /**
     * @return {@code null}, always
     */
    @Override
    public String message() {
        return null;
    }

}
