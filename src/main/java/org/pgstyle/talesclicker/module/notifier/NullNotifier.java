package org.pgstyle.talesclicker.module.notifier;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

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
        Application.log(Level.WARN, "The NullNotifier should not be use as method of notification, do implement and connect a notifier for notifying the event.");
        Application.log(Level.DEBUG, "NullNotifier - notifies");
        Application.log(Level.DEBUG, payload);
        return true;
    }

}
