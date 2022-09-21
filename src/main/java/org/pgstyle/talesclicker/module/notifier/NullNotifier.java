package org.pgstyle.talesclicker.module.notifier;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

public final class NullNotifier implements Notifier {

    @Override
    public boolean notifies(String payload) {
        Application.log(Level.DEBUG, "NullNotifier - notifies");
        Application.log(Level.DEBUG, payload);
        return true;
    }

}
