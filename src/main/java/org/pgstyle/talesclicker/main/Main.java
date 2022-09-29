package org.pgstyle.talesclicker.main;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

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
