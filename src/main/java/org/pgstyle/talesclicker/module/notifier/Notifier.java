package org.pgstyle.talesclicker.module.notifier;


/**
 * The {@link NotifierModule} uses a notifier to send a notification after
 * detecting an event.The {@code Notifier} provides mechanism to send the
 * notification message.
 *
 * @since 1.0
 * @author PGKan
 */
public interface Notifier {

    /**
     * Send the string payload as notification.
     * 
     * @param payload the payload
     * @return {@code true} if the payload is sent successfully; or
     *         {@code false} otherwise
     */
    boolean notifies(String payload);

}
