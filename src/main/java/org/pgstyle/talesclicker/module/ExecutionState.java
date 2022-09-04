package org.pgstyle.talesclicker.module;

import java.util.Collections;
import java.util.Set;

public final class ExecutionState {

    public enum Status {
        NEXT,
        END,
        RELOAD,
        TERMINATE
    }

    public static ExecutionState end(long next, Set<Class<? extends Module>> end) {
        return new ExecutionState(Status.END, next, end, 0);
    }

    public static ExecutionState reload(long next) {
        return new ExecutionState(Status.RELOAD, next, Collections.emptySet(), 0);
    }

    public static ExecutionState next(long next) {
        return new ExecutionState(Status.NEXT, next, Collections.emptySet(), 0);
    }

    public static ExecutionState terminate(long next, int terminate) {
        return new ExecutionState(Status.TERMINATE, next, Collections.emptySet(), terminate);
    }

    private ExecutionState(Status status, long next, Set<Class<? extends Module>> end, int terminate) {
        this.status = status;
        this.next = next;
        this.end = Collections.unmodifiableSet(end);
        this.terminate = terminate;
    }

    private final Status status;
    private final long next;
    private final Set<Class<? extends Module>> end;
    private final int terminate;

    public Status getStatus() {
        return this.status;
    }

    public long next() {
        return this.next;
    }

    public Set<Class<? extends Module>> end() {
        return this.end;
    }

    public int terminate() {
        return this.terminate;
    }
    
}
