package org.pgstyle.talesclicker.module;

public interface Module {

    boolean initialise(Environment env);

    ExecutionState execute();

    boolean finalise(ExecutionState state);

}
