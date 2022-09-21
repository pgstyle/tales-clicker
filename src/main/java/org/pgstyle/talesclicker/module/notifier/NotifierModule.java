package org.pgstyle.talesclicker.module.notifier;

import java.util.HashMap;
import java.util.Map;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.module.Environment;
import org.pgstyle.talesclicker.module.Module;
import org.pgstyle.talesclicker.module.ModuleControl;
import org.pgstyle.talesclicker.module.Signal;

public final class NotifierModule implements Module {

    private static final Map<String, Detector> detectors;
    private static final Map<String, Notifier> notifiers;

    static {
        detectors = new HashMap<>();
        detectors.put("Disconnect", new DisconnectDetector());
        detectors.put(null, new NullDetector());
        notifiers = new HashMap<>();
        notifiers.put("Line", new LineNotifier());
        notifiers.put(null, new NullNotifier());
    }

    private String event;
    private Detector detector;
    private Notifier notifier;
    private ModuleControl action;

    @Override
    public boolean initialise(Environment env, String[] args) {
        this.event = args.length > 0 ? args[0] : "null";
        this.detector = NotifierModule.detectors.get(args.length > 0 ? args[0] : null);
        this.notifier = NotifierModule.notifiers.get(args.length > 1 ? args[1] : null);
        switch (args.length > 2 ? args[2] : "null") {
        case "terminate":
            this.action = ModuleControl.terminate(0, Signal.valueOf((args.length > 3 ? args[3] : "TERMINATE").toUpperCase()));
            break;
        case "end":
            this.action = ModuleControl.end(0, Signal.valueOf((args.length > 3 ? args[3] : "TERMINATE").toUpperCase()));
            break;
        case "continue":
        default:
            this.action = ModuleControl.next(Module.calculateTimeout(Configuration.getConfig().getDetectFrequency()));
            break;
        }
        return true;
    }

    @Override
    public ModuleControl execute() {
        if (this.detector.detect()) {
            Application.log(Level.INFO, "%s detected", this.event);
            if (this.notifier.notifies(this.detector.message())) {
                return this.action;
            }
            else {
                Application.log(Level.WARN, "notify failed, will retry");
                return ModuleControl.next(Module.calculateTimeout(Configuration.getConfig().getRetryFrequency()));
            }
        }
        return ModuleControl.next(Module.calculateTimeout(Configuration.getConfig().getDetectFrequency()));
    }

    @Override
    public boolean finalise(ModuleControl state) {
        // NOP
        return true;
    }
    
}
