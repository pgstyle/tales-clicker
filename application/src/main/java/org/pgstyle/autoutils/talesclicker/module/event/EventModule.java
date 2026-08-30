package org.pgstyle.autoutils.talesclicker.module.event;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;
import org.pgstyle.autoutils.talesclicker.module.ModuleManager;
import org.pgstyle.autoutils.talesclicker.module.Signal;

public final class EventModule implements Module {

    public static final String ACTIONS_SCRIPT = "(?<func>[A-Za-z_]\\w*)\\s*\\(\\s*(?<args>[^)]*)\\s*\\)\\s*;";
    private static final String PATTERN_STRING = "(?<name>[A-Za-z_]\\w*)\\s*\\[\\s*(?<event>[A-Za-z_][\\w.]*)\\s*(?:,\\s*(?<delay>\\d+)\\s*(?:,\\s*(?<longDelay>\\d+))?\\s*)?\\]\\s*\\{\\s*(?<actions>(?:\\s*" + ACTIONS_SCRIPT + ")+)\\s*}";
    private static final Pattern HANDLER_SCRIPT_PATTERN = Pattern.compile(PATTERN_STRING);

    @Override
    public boolean initialise(Environment env, String[] args) {
        Application.log(Level.INFO, "loaded event handling script");
        String raw = Configuration.getConfig().getModuleProperty("event", "script");
        Matcher matcher = HANDLER_SCRIPT_PATTERN.matcher(raw);
        Application.log(Level.DEBUG, "parsing event handling script: " + raw);
        while (matcher.find()) {
            String name = matcher.group("name");
            String event = matcher.group("event");
            String delay = matcher.group("delay");
            String longDelay = matcher.group("longDelay");
            String actions = matcher.group("actions");
            Application.log(Level.DEBUG, "found handler segment: %s", name);
            if (ModuleManager
                    .getManagerApi()
                    .register(EventHandlerModule.class, name, event, delay, longDelay, actions)) {
                Application.log(Level.INFO, "registered event handler: %s", name);
            } else {
                Application.log(Level.WARN, "failed to register event handler: %s", name);
            }
        }
        return true;
    }

    @Override
    public ModuleControl execute() {
        // NOP
        // this module does not execute any actions directly,
        // it only registers event handlers on initialisation
        return ModuleControl.end(0, Signal.SUCCESS);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
