package org.pgstyle.autoutils.talesclicker.module.notifier;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.AppUtils;

/**
 * This class is the modular scheme implementation of a disconnection detector,
 * it wraps around the original {@link DisconnectCapture} disconnection
 * detecting mechanism.
 *
 * @since 1.0
 * @author PGKan
 */
public final class DisconnectDetector implements Detector {

    @Override
    public boolean detect() {
        return DisconnectCapture.fromImage(Actions.getCapturer().capture()).isDisconnected();
    }

    @Override
    public String message() {
        return AppUtils.hostname() + " is down";
    }

}
