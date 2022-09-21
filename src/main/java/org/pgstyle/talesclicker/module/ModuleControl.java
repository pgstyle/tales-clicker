package org.pgstyle.talesclicker.module;

import java.util.Arrays;

public final class ModuleControl {

    public enum Action {
        NEXT,
        STOP,
        RELOAD,
        TERMINATE
    }

    public static ModuleControl end(long delay, Signal signal) {
        return new ModuleControl(Action.STOP, delay, signal, null);
    }

    public static ModuleControl reload(long delay, String[] args) {
        return new ModuleControl(Action.RELOAD, delay, Signal.SUCCESS, args);
    }

    public static ModuleControl next(long delay) {
        return new ModuleControl(Action.NEXT, delay, Signal.SUCCESS, null);
    }

    public static ModuleControl terminate(long delay, Signal signal) {
        return new ModuleControl(Action.TERMINATE, delay, signal, null);
    }

    private ModuleControl(Action status, long delay, Signal signal, String[] args) {
        this.status = status;
        this.delay = delay;
        this.signal = signal;
        this.args = args;
    }

    private final Action status;
    private final long delay;
    private final Signal signal;
    private final String[] args;

    public Action getAction() {
        return this.status;
    }

    public long getDelay() {
        return this.delay;
    }

    public Signal getSignal() {
        return this.signal;
    }

    public String[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length);
    }
    
}
