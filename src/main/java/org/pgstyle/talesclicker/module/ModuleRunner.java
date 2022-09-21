package org.pgstyle.talesclicker.module;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.module.ModuleControl.Action;

public final class ModuleRunner extends Thread {

    public enum State {
        INIT, RUNNING, STOP, DEAD;
    }

    private static final Map<Class<?>, Integer> SEQUENCES = Collections.synchronizedMap(new HashMap<>());

    private static Integer getSequence(Class<? extends Module> clazz) {
        ModuleRunner.SEQUENCES.putIfAbsent(clazz, -1);
        return ModuleRunner.SEQUENCES.computeIfPresent(clazz, (c, i) -> i + 1);
    }

    public static ModuleRunner of(Class<? extends Module> module, Environment env, String[] args) {
        try {
            return new ModuleRunner(module.newInstance(), env, args);
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

    public State getRunnerState() {
        synchronized (this.module) {
            return this.state;
        }
    }

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
        ModuleControl control = ModuleControl.next(Module.calculateTimeout(Configuration.getConfig().getRetryFrequency()));
        try {
            Application.log(Level.TRACE, "%s - start execute", this.module.getClass().getSimpleName());
            control = this.module.execute();
            Application.log(Level.TRACE, "%s - finish execute", this.module.getClass().getSimpleName());
        }
        catch (RuntimeException e) {
            Application.log(Level.ERROR, "error when executing module, %s", e);
            e.printStackTrace();
        }
        try {
            if (Objects.isNull(control) || control.getDelay() < 0) {
                Application.log(Level.ERROR, "illegal module control: %s", control);
                control = ModuleControl.end(0, Signal.FATAL_CONTROL);
            }
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
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return this.state != State.STOP ? control : ModuleControl.end(0, this.signal);
    }

}
