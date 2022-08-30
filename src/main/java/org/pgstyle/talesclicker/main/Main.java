package org.pgstyle.talesclicker.main;

import org.pgstyle.talesclicker.clicker.TalesClicker;

public final class Main {

    public static void main(String[] args) {
        try {
            System.exit(TalesClicker.main(args));
        }
        catch (Exception e) {
            TalesClicker.log("Uncaught exception: %s", e);
            e.printStackTrace();
        }
    }

}
