package org.pgstyle.autoutils.talesclicker.module;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl.Action;

/**
 * The {@code ModuleRunner} instantiates and runs a module. It manages the
 * instantiation of a module and the initialisation-execution-finalisation life
 * cycle.
 *
 * @since 1.0
 * @author PGKan
 */
public final class ModuleRunner extends Thread {

    /**
     * State of the {@link ModuleRunner} thread.
     */
    public enum State {
        /** The module runner is yet to be initialised. */
        INIT,
        /** The module runner is live. */
        RUNNING,
        /** The module runner has received a shutdown signal. */
        STOP,
        /** The module runner has shutted down. */
        DEAD;
    }

    private static final Map<Class<?>, Integer> SEQUENCES = Collections.synchronizedMap(new HashMap<>());
    private static final long RETRY_TIMEOUT = Module.calculateTimeout(Configuration.getConfig().getModulePropertyAsReal("manager", "retry.frequency"));

    private static Integer getSequence(Class<? extends Module> clazz) {
        ModuleRunner.SEQUENCES.putIfAbsent(clazz, -1);
        return ModuleRunner.SEQUENCES.computeIfPresent(clazz, (c, i) -> i + 1);
    }

    /**
     * Create a module runner of a module.
     *
     * @param module the class of the module to be instantiated
     * @param env the application environment the runner runs on
     * @param args the arguments for initialising the module
     * @return a module runner
     */
    public static ModuleRunner of(Class<? extends Module> module, Environment env, String[] args) {
        try {
            return new ModuleRunner(module.newInstance(), env, Optional.ofNullable(args).orElseGet(() -> new String[0]));
        } catch (ReflectiveOperationException e) {
            Application.log(Level.ERROR, "failed to instantiate module: %s", e);
            e.printStackTrace();
            return null;
        }
    }

    private ModuleRunner(Module module, Environment env, String[] args) {
        this.setName(module.getClass().getSimpleName() + "-" + ModuleRunner.getSequence(module.getClass()));
        Application.log(Level.DEBUG, "create module runner [%s]", this.getName());
        if (args.length > 0) {
            Application.log(Level.DEBUG, "with arguments: %s", Arrays.toString(args));
        }
        this.setDaemon(!(module instanceof ModuleManager));
        this.module = module;
        this.env = env;
        this.args = args;
        this.state = State.INIT;
        this.signal = Signal.TERMINATE;
    }

    private final Module module;
    private final Environment env;
    private final String[] args;
    private State state;
    private Signal signal;

    /**
     * Get the state of this module runner.
     * 
     * @return the state of this runner
     */
    public State getRunnerState() {
        synchronized (this.module) {
            return this.state;
        }
    }

    /**
     * Signal this module runner to shutdown.
     * 
     * @param signal the shutdown signal
     */
    public void shutdown(Signal signal) {
        if (this.isAlive()) {
            Application.log(Level.INFO, "runner [%s] received shutdown signal: %s", this.getName(), signal);
            synchronized (this.module) {
                this.state = State.STOP;
                this.signal = signal;
                this.module.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        Application.log(Level.DEBUG, "started module runner [%s]", this.getName());
        this.state = State.RUNNING;
        // initialise module
        boolean runnable = false;
        try {
            Application.log(Level.TRACE, "%s - start initialise", this.module.getClass().getSimpleName());
            runnable = this.module.initialise(this.env, this.args);
            Application.log(Level.TRACE, "%s - finish initialise", this.module.getClass().getSimpleName());
        }
        catch (RuntimeException e) {
            Application.log(Level.ERROR, "error when initialising module: %s", e);
            e.printStackTrace();
        }
        ModuleControl control = ModuleControl.end(0, Signal.FAILED_INITIALISE);
        while (runnable) {
            control = this.execute();
            // Action: reload
            if (control.getAction() == Action.RELOAD) {
                Application.log(Level.DEBUG, "reload module runner [%s]", this.getName());
                Application.log(Level.TRACE, "%s - start reload/initialise", this.module.getClass().getSimpleName());
                control = Optional.ofNullable(this.reload(Optional.ofNullable(control.getArgs()).orElse(this.args))).orElse(control);
                Application.log(Level.TRACE, "%s - finish reload/initialise", this.module.getClass().getSimpleName());
            }
            // Action: stop, terminate
            if (control.getAction() != Action.NEXT && control.getAction() != Action.RELOAD) {
                Application.log(Level.DEBUG, "stop module runner [%s]", this.getName());
                this.state = State.STOP;
                runnable = false;
            }
        }
        // stop and finalise module
        Application.log(Level.DEBUG, "finalise module runner [%s]", this.getName());
        boolean finalise = false;
        try {
            Application.log(Level.TRACE, "%s - start finalise", this.module.getClass().getSimpleName());
            finalise = this.module.finalise(control);
            if (!finalise && (control.getSignal() != Signal.KILL)) {
                // for non-forcefull stop failing, try one more time forcefully
                finalise = this.module.finalise(ModuleControl.end(0, Signal.KILL));
            }
            Application.log(Level.TRACE, "%s - finish finalise", this.module.getClass().getSimpleName());
        }
        catch (RuntimeException e) {
            Application.log(Level.ERROR, "error when finalising module: %s", e);
            e.printStackTrace();
        }
        if (!finalise) {
            control = ModuleControl.end(0, Signal.FAILED_FINALISE);
        }
        this.state = State.DEAD;
        Application.log(Level.DEBUG, "runner [%s] exited with state: %s", this.getName(), control.getSignal());
        // for terminate mode, send shutdown signal to module manager after the
        // module has shutted down to initiate the shutdown process
        if (control.getAction() == Action.TERMINATE) {
            ModuleManager.getManagerApi().shutdown();
        }
    }

    private ModuleControl reload(String[] args) {
        try {
            return this.module.initialise(this.env, args) ? null : ModuleControl.end(0, Signal.FAILED_RELOAD);
        }
        catch (RuntimeException e) {
            Application.log(Level.ERROR, "error when reloading module: %s", e);
            e.printStackTrace();
            return ModuleControl.end(0, Signal.FAILED_RELOAD);
        }
    }

    private ModuleControl execute() {
        ModuleControl control = ModuleControl.next(ModuleRunner.RETRY_TIMEOUT);
        try {
            Application.log(Level.TRACE, "%s - start execute", this.module.getClass().getSimpleName());
            control = this.module.execute();
            Application.log(Level.TRACE, "%s - finish execute", this.module.getClass().getSimpleName());
        }
        catch (RuntimeException e) {
            Application.log(Level.ERROR, "error when executing module, %s", e);
            e.printStackTrace();
        }
        if (Objects.isNull(control) || control.getDelay() < 0) {
            Application.log(Level.ERROR, "illegal module control: %s", control);
            control = ModuleControl.end(0, Signal.FATAL_CONTROL);
        }
        try {
            // handle delay for next execution
            if (control.getDelay() > 0) {
                synchronized (this.module) {
                    this.module.wait(control.getDelay());
                }
            }
        } catch (InterruptedException e) {
            Application.log(Level.ERROR, "interrupted: %s", e);
            e.printStackTrace();
            control = ModuleControl.end(0, Signal.INTERRUPTED);
            Thread.currentThread().interrupt();
        }
        return this.state != State.STOP ? control : ModuleControl.end(0, this.signal);
    }

}
