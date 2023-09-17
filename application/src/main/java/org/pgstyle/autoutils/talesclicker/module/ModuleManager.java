package org.pgstyle.autoutils.talesclicker.module;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.stream.IntStream;

import org.pgstyle.autoutils.talesclicker.application.AppConfig;
import org.pgstyle.autoutils.talesclicker.common.AppResource;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;
import org.pgstyle.autoutils.talesclicker.module.ModuleRunner.State;

/**
 * The {@code ModuleManager} module manages the creation, execution and
 * termination of all module in the application.
 *
 * @since 1.0
 * @author PGKan
 */
public final class ModuleManager implements Module {

    /**
     * Get the {@code ManagerAPI} object for accessing manager control APIs.
     * @return the {@code ManagerAPI} object
     */
    public static ManagerAPI getManagerApi() {
        return ModuleManager.apiInstance;
    }

    /**
     * The {@code ManagerAPI} is the publicly accessible control API for all
     * modules to interact with the {@code ModuleManager}.
     */
    public class ManagerAPI {

        /**
         * Register a new module to the {@code ModuleManager}, the registered
         * module will be handled by the {@code ModuleManager}, a dedicated
         * {@link ModuleRunner} for the module will be created and started.
         *
         * @param module the class of the module to be registered
         * @param args arguments for the module initialisation
         *             {@link Module#initialise(Environment, String[])} call
         *             during the start of the {@code ModuleRunner}
         * @return {@code true} if the module is registered successfully; or
         *         {@code false} otherwise
         */
        public boolean register(Class<? extends Module> module, String... args) {
            return this.register(module, 1, args);
        }

        /**
         * Register a new module to the {@code ModuleManager}, the registered
         * module will be handled by the {@code ModuleManager}, a dedicated
         * {@link ModuleRunner} for the module will be created and started.
         *
         * @param module the class of the module to be registered
         * @param count the count of {@code ModuleRunner} instance will be
         *              created
         * @param args arguments for the module initialisation
         *             {@link Module#initialise(Environment, String[])} call
         *             during the start of the {@code ModuleRunner}
         * @return {@code true} if the module is registered successfully; or
         *         {@code false} otherwise
         */
        public boolean register(Class<? extends Module> module, int count, String... args) {
            return IntStream.range(0, count).map(i -> ModuleManager.this.dynamics.add(new SimpleEntry<>(module, args)) ? 0 : 1).sum() == 0;
        }

        /**
         * Send shutdown signal to the {@code ModuleManger} to initiate the
         * shutdown process, the {@code ModuleManager} will shutdown all modules.
         *
         * @return {@code true} if the shutdown signal is sent successfully; or
         *         {@code false} if shutdown is already in progress
         */
        public boolean shutdown() {
            if (!"true".equals(ModuleManager.this.env.get("shutdown"))) {
                ModuleManager.this.env.set("shutdown", "true");
                return true;
            }
            return false;
        }

        /**
         * Inform {@code ModuleManger} to use a specific signal to shutdown
         * instances of a module.
         *
         * @param module the class of module to be shutted down
         * @param signal application signal of the termination of the module
         */
        public void shutdown(Class<? extends Module> module, Signal signal) {
            ModuleManager.this.shutdown.add(new SimpleEntry<>(module, signal));
        }
    }

    private static ManagerAPI apiInstance;

    /** Application Environment */
    private Environment env;
    /** All module runners managed by the module manager. */
    private Map<Class<? extends Module>, List<ModuleRunner>> modules;
    /** Dynamically create module from manager API call. */
    private Queue<Entry<Class<? extends Module>, String[]>> dynamics;
    /** Module shutdown from manager API call. */
    private Queue<Entry<Class<? extends Module>, Signal>> shutdown;
    /** Module name cache */
    private Map<Class<? extends Module>, String> moduleNames;
    /** execution interval */
    private long monitorTimeout;

    @SuppressWarnings("unchecked")
    @Override
    public boolean initialise(Environment env, String[] args) {
        this.env = env;
        this.modules = new HashMap<>();
        this.moduleNames = new HashMap<>();
        this.dynamics = new LinkedList<>();
        this.shutdown = new LinkedList<>();
        this.monitorTimeout = Module.calculateTimeout(AppConfig.getConfig().getModulePropertyAsReal("manager", "monitor.frequency"));
        // initialise environment
        if (this.env.findGlobal("mm")) {
            Console.log(Level.FATAL, "unclean environment: %s", this.env.get("mm"));
            return false;
        }
        this.env.declareGlobal("mm", Integer.toHexString(this.hashCode()));
        this.env.declare("shutdown", null);
        this.env.declareGlobal("shutdown", null);
        // load module list
        Properties classes = new Properties();
        try {
            classes.load(AppResource.getResource("./modules.list"));
        } catch (IOException | IllegalArgumentException e) {
            Console.log(Level.FATAL, "failed to load module list: %s", e);
            e.printStackTrace();
            return false;
        }
        // register pre-registered modules
        for (Object name : classes.keySet()) {
            if (AppConfig.getConfig().isModuleEnabled(name.toString())) {
                try {
                    Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(classes.getProperty(name.toString()));
                    if (!this.modules.containsKey(clazz)) {
                        this.modules.put(clazz, new ArrayList<>());
                    }
                    IntStream.range(0, AppConfig.getConfig().getModuleCount(name.toString())).forEach(i -> this.modules.get(clazz).add(null));
                    this.moduleNames.put(clazz, name.toString());
                } catch (ClassNotFoundException e) {
                    Console.log(Level.WARN, "module class not found: %s", e);
                    e.printStackTrace();
                }
            }
        }
        // open manager API accessor
        synchronized (ModuleManager.class) {
            ModuleManager.apiInstance = this.new ManagerAPI();
        }
        return true;
    }

    @Override
    public ModuleControl execute() {
        // check environment shutdown request
        if (this.env.find("shutdown") && "true".equals(this.env.get("shutdown"))) {
            Console.log(Level.INFO, "Received shutdown signal from environment.");
            return ModuleControl.terminate(0, Signal.TERMINATE);
        }
        // process pre-registered modules
        for (Entry<Class<? extends Module>, List<ModuleRunner>> entry : this.modules.entrySet()) {
            List<ModuleRunner> runners = entry.getValue();
            for (int i = 0 ; i < runners.size(); i++) {
                ModuleRunner runner = runners.get(i);
                if (Objects.isNull(runner)) {
                    // load module, instantiate module and its runner
                    Console.log(Level.INFO, "Create module runner for [%s]", entry.getKey().getSimpleName());
                    runners.set(i, ModuleRunner.of(entry.getKey(), this.env, AppConfig.getConfig().getModuleArgs(this.moduleNames.get(entry.getKey()), i)));
                }
                else if (runner.getRunnerState() == State.INIT) {
                    // start module runner thread
                    Console.log(Level.INFO, "Start module runner [%s]", runner.getName());
                    runner.start();
                }
            }
        }
        // process dynamically registered modules
        while (!this.dynamics.isEmpty()) {
            Entry<Class<? extends Module>, String[]> entry = this.dynamics.poll();
            Console.log(Level.INFO, "Create module runner for [%s] dynamically", entry.getKey().getSimpleName());
            if (!this.modules.containsKey(entry.getKey())) {
                this.modules.put(entry.getKey(), new ArrayList<>());
            }
            this.modules.get(entry.getKey()).add(ModuleRunner.of(entry.getKey(), this.env, entry.getValue()));
        }
        // process shutdown request
        while (!this.shutdown.isEmpty()) {
            Entry<Class<? extends Module>, Signal> entry = this.shutdown.poll();
            Console.log(Level.INFO, "Send shutdown signal to module [%s]", entry.getKey().getSimpleName());
            for (ModuleRunner runner : this.modules.get(entry.getKey())) {
                Console.log(Level.INFO, "Send shutdown signal to module runner [%s]", runner.getName());
                runner.shutdown(entry.getValue());
            }
        }
        return ModuleControl.next(this.monitorTimeout);
    }

    @Override
    public boolean finalise(ModuleControl state) {
        int code = state.getSignal().getCode() - Signal.TERMINATE.getCode();
        Console.log(Level.DEBUG, "Signal Code: %d", code);
        Console.log(Level.INFO, "Shutdown modules before terminate module manager");
        // send shutdown signal to all modules and wait all modules shutdown
        this.modules.entrySet().stream().filter(e -> !e.getValue().isEmpty()).parallel()
        .forEach(e -> {
            Console.log(Level.DEBUG, "Shutdown module [%s]", e.getKey().getSimpleName());
            e.getValue().forEach(r -> r.shutdown(state.getSignal()));
            e.getValue().forEach(r -> {try {r.join();} catch (InterruptedException ex) {Thread.currentThread().interrupt();}});
        });
        return true;
    }

}
