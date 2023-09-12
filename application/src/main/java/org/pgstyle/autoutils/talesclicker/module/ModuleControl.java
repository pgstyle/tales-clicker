package org.pgstyle.autoutils.talesclicker.module;

import java.util.Arrays;
import java.util.Optional;

/**
 * The {@code ModuleControl} is a signal container to control how the
 * {@link ModuleManager} and {@link ModuleRunner} handle the module.
 *
 * @since 1.0
 * @author PGKan
 */
public final class ModuleControl {

    /** The type of control action allowed. */
    public enum Action {
        /** continue the module execution */
        NEXT,
        /** stop the module execution */
        STOP,
        /** reinitialise the module and continue execution */
        RELOAD,
        /** terminate the application (will cause the module manager to stop all modules) */
        TERMINATE;
    }

    /**
     * Create a {@code ModuleControl} with an {@code END} action.
     *
     * @param delay delay before performing the control action
     * @param signal application signal of the termination of the module
     * @return a {@code ModuleControl} object
     */
    public static ModuleControl end(long delay, Signal signal) {
        return new ModuleControl(Action.STOP, delay, signal, null);
    }

    /**
     * Create a {@code ModuleControl} with an {@code RELOAD} action.
     *
     * @param delay delay before performing the control action
     * @param args arguments for invoking the
     *             {@link Module#initialise(Environment, String[])} method
     * @return a {@code ModuleControl} object
     */
    public static ModuleControl reload(long delay, String[] args) {
        return new ModuleControl(Action.RELOAD, delay, Signal.SUCCESS, args);
    }

    /**
     * Create a {@code ModuleControl} with an {@code NEXT} action.
     *
     * @param delay delay before performing the control action
     * @return a {@code ModuleControl} object
     */
    public static ModuleControl next(long delay) {
        return new ModuleControl(Action.NEXT, delay, Signal.SUCCESS, null);
    }

    /**
     * Create a {@code ModuleControl} with an {@code TERMINATE} action.
     *
     * @param delay delay before performing the control action
     * @param signal application signal of the termination of the appliction
     * @return a {@code ModuleControl} object
     */
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

    /**
     * Get the control action.
     * @return the control action
     */
    public Action getAction() {
        return this.status;
    }

    /**
     * Get the delay timeout.
     * @return the delay timeout (millisecond)
     */
    public long getDelay() {
        return this.delay;
    }

    /**
     * Get the application signal.
     * @return the application signal
     */
    public Signal getSignal() {
        return this.signal;
    }

    /**
     * Get the reinitialising arguments.
     * @return the reinitialising arguments
     */
    public String[] getArgs() {
        return Optional.ofNullable(this.args).map(a -> Arrays.copyOf(a, a.length)).orElseGet(() -> new String[0]);
    }

}
