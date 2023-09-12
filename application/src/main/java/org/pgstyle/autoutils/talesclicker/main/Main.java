package org.pgstyle.autoutils.talesclicker.main;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;

/**
 * program entrypoint
 */
public final class Main {

    public static void main(String[] args) {
        try {
            System.exit(Application.main(args));
        }
        catch (RuntimeException e) {
            Application.log(Level.FATAL, "Uncaught exception: %s", e);
            e.printStackTrace();
        }
    }

}