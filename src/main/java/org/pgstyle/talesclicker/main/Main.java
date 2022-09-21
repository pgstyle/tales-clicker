package org.pgstyle.talesclicker.main;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.clicker.TalesClicker;
import org.pgstyle.talesclicker.module.Environment;
import org.pgstyle.talesclicker.module.ModuleManager;
import org.pgstyle.talesclicker.module.ModuleRunner;
import org.pgstyle.talesclicker.module.Signal;

public final class Main {

    public static final Environment env = new Environment();

    public static void main(String[] args) {
        try {
            System.exit(TalesClicker.main(args));
        }
        catch (Exception e) {
            Application.log(Level.FATAL, "Uncaught exception: %s", e);
            e.printStackTrace();
        }
    }

}
