package org.pgstyle.autoutils.talesclicker.module.notifier;

import java.util.HashMap;
import java.util.Map;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.common.Console;
import org.pgstyle.autoutils.talesclicker.common.Console.Level;
import org.pgstyle.autoutils.talesclicker.application.AppConfig;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;
import org.pgstyle.autoutils.talesclicker.module.Signal;

/**
 * The {@code NotifierModule} can detect a specific event with a detector and
 * send out notification with a notifier.
 *
 * @since 0.6-dev
 * @author PGKan
 */
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
    private long detectTimeout;
    private long retryTimeout;

    @Override
    public boolean initialise(Environment env, String[] args) {
        // load configuration from initialisation arguments
        this.event = args.length > 0 ? args[0] : "null";
        this.detector = NotifierModule.detectors.getOrDefault(args.length > 0 ? args[0] : null, NotifierModule.detectors.get(null));
        this.notifier = NotifierModule.notifiers.getOrDefault(args.length > 1 ? args[1] : null, NotifierModule.notifiers.get(null));
        this.detectTimeout = Module.calculateTimeout(AppConfig.getConfig().getModulePropertyAsReal("notifier", "detect.frequency"));
        this.retryTimeout = Module.calculateTimeout(AppConfig.getConfig().getModulePropertyAsReal("manager", "retry.frequency"));
        switch (args.length > 2 ? args[2] : "null") {
        case "terminate":
            this.action = ModuleControl.terminate(0, Signal.valueOf((args.length > 3 ? args[3] : "TERMINATE").toUpperCase()));
            break;
        case "end":
            this.action = ModuleControl.end(0, Signal.valueOf((args.length > 3 ? args[3] : "TERMINATE").toUpperCase()));
            break;
        case "continue":
        default:
            this.action = ModuleControl.next(this.detectTimeout);
            break;
        }
        return true;
    }

    @Override
    public ModuleControl execute() {
        if (this.detector.detect()) {
            Console.log(Level.INFO, "%s detected", this.event);
            if (this.notifier.notifies(this.detector.message())) {
                return this.action;
            }
            else {
                Console.log(Level.WARN, "notify failed, will retry");
                return ModuleControl.next(this.retryTimeout);
            }
        }
        return ModuleControl.next(this.detectTimeout);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
