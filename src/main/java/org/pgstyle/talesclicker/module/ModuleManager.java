package org.pgstyle.talesclicker.module;

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

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.module.ModuleRunner.State;

public final class ModuleManager implements Module {

    public static ManagerAPI getManagerApi() {
        return ModuleManager.apiInstance;
    }

    public class ManagerAPI {

        public boolean register(Class<? extends Module> module, String... args) {
            return this.register(module, 1, args);
        }

        public boolean register(Class<? extends Module> module, int count, String... args) {
            return IntStream.range(0, count).map(i -> ModuleManager.this.dynamics.add(new SimpleEntry<>(module, args)) ? 0 : 1).sum() == 0;
        }        

        public boolean shutdown() {
            if (!"true".equals(ModuleManager.this.env.get("shutdown"))) {
                ModuleManager.this.env.set("shutdown", "true");
                return true;
            }
            return false;
        }

        public void shutdown(Class<? extends Module> module, Signal signal) {
            ModuleManager.this.modules.get(module).stream().forEach(r -> r.shutdown(signal));
        }
    }

    private static ManagerAPI apiInstance;

    private Environment env;
    private Map<Class<? extends Module>, List<ModuleRunner>> modules;
    private Queue<Entry<Class<? extends Module>, String[]>> dynamics;
    private Map<Class<? extends Module>, String> moduleNames;

    @SuppressWarnings("unchecked")
    @Override
    public boolean initialise(Environment env, String[] args) {
        this.env = env;
        if (this.env.findGlobal("mm")) {
            Application.log(Level.FATAL, "unclean environment: %s", this.env.get("mm"));
            return false;
        }
        this.env.declareGlobal("mm", Integer.toHexString(this.hashCode()));
        this.env.declare("shutdown", null);
        this.env.declareGlobal("shutdown", null);
        this.modules = new HashMap<>();
        this.moduleNames = new HashMap<>();
        this.dynamics = new LinkedList<>();
        Properties classes = new Properties();
        try {
            classes.load(AppUtils.getResource("modules.list"));
        } catch (IOException e) {
            Application.log(Level.FATAL, "failed to load module list: %s", e);
            e.printStackTrace();
            return false;
        }
        ModuleManager.apiInstance = this.new ManagerAPI();
        for (Object name : classes.keySet()) {
            if (Configuration.getConfig().isModuleEnabled(name.toString())) {
                try {
                    Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(classes.getProperty(name.toString()));
                    if (!this.modules.containsKey(clazz)) {
                        this.modules.put(clazz, new ArrayList<>());
                    }
                    IntStream.range(0, Configuration.getConfig().getModuleCount(name.toString())).forEach(i -> this.modules.get(clazz).add(null));
                    this.moduleNames.put(clazz, name.toString());
                } catch (ClassNotFoundException e) {
                    Application.log(Level.WARN, "module class not found: %s", e);
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public ModuleControl execute() {
        if (this.env.find("shutdown") && "true".equals(this.env.get("shutdown"))) {
            Application.log(Level.INFO, "Received shutdown signal from environment.");
            return ModuleControl.terminate(0, Signal.TERMINATE);
        }
        for (Entry<Class<? extends Module>, List<ModuleRunner>> entry : this.modules.entrySet()) {
            List<ModuleRunner> runners = entry.getValue();
            for (int i = 0 ; i < runners.size(); i++) {
                ModuleRunner runner = runners.get(i);
                if (Objects.isNull(runner)) {
                    // load module, instantiate module and its runner
                    Application.log(Level.INFO, "Create module runner for [%s]", entry.getKey().getSimpleName());
                    runners.set(i, ModuleRunner.of(entry.getKey(), this.env, Configuration.getConfig().getModuleArgs(this.moduleNames.get(entry.getKey()), i)));
                }
                else if (runner.getRunnerState() == State.INIT) {
                    // start module runner thread
                    Application.log(Level.INFO, "Start module runner [%s]", runner.getName());
                    runner.start();
                }
            }
        }
        while (!this.dynamics.isEmpty()) {
            Entry<Class<? extends Module>, String[]> entry = this.dynamics.poll();
            Application.log(Level.INFO, "Create module runner for [%s] dynamically", entry.getKey().getSimpleName());
            if (!this.modules.containsKey(entry.getKey())) {
                this.modules.put(entry.getKey(), new ArrayList<>());
            }
            this.modules.get(entry.getKey()).add(ModuleRunner.of(entry.getKey(), this.env, entry.getValue()));
        }
        return ModuleControl.next(Module.calculateTimeout(Configuration.getConfig().getMonitorFrequency()));
    }

    @Override
    public boolean finalise(ModuleControl state) {
        int code = state.getSignal().getCode() - Signal.TERMINATE.getCode();
        Application.log(Level.DEBUG, "Signal Code: %d", code);
        Application.log(Level.INFO, "Shutdown modules before terminate module manager");
        this.modules.entrySet().stream().filter(e -> !e.getValue().isEmpty()).parallel()
        .forEach(e -> {
            Application.log(Level.DEBUG, "Shutdown module [%s]", e.getKey().getSimpleName());
            e.getValue().forEach(r -> r.shutdown(state.getSignal()));
            e.getValue().forEach(r -> {try {r.join();} catch (InterruptedException ex) {Thread.currentThread().interrupt();}});
        });
        return true;
    }
    
}
