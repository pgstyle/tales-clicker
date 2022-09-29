package org.pgstyle.talesclicker.module.notifier;

/**
 * The {@link NotifierModule} uses a detector to detect a certain event. The
 * {@code Detector} provides detection checks and event message.
 *
 * @since 1.0
 * @author PGKan
 */
public interface Detector {

    /**
     * Detect a specific event that the detector is dedicated to detect.
     *
     * @return {@code true} if the event has been detected; or {@code false}
     *         otherwise
     */
    boolean detect();

    /**
     * Retrive the message of the event detected.
     *
     * @return the message of the detected event
     */
    String message();

}
