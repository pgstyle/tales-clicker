package org.pgstyle.talesclicker.module;

public interface Module {

    public static long calculateTimeout(double frequency) {
        return (long) (60000 / frequency);
    }

    boolean initialise(Environment env, String[] args);

    ModuleControl execute();

    boolean finalise(ModuleControl state);

}
