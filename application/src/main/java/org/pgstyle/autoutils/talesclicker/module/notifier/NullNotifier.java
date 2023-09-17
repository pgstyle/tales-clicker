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
public final class NullNotifier implements Notifier {

    /**
     * Log the payload text.
     *
     * @return {@code true}, always
     */
    @Override
    public boolean notifies(String payload) {
        Console.log(Level.WARN, "The NullNotifier should not be use as method of notification, do implement and connect a notifier for notifying the event.");
        Console.log(Level.DEBUG, "NullNotifier - notifies");
        Console.log(Level.DEBUG, payload);
        return true;
    }

}
