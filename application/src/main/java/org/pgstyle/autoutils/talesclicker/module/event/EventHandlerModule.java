package org.pgstyle.autoutils.talesclicker.module.event;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.imagedb.Stencil;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;
import org.pgstyle.autoutils.talesclicker.module.Signal;

public final class EventHandlerModule implements Module {

    private static final Pattern ACTIONS_SCRIPT_PATTERN = Pattern.compile(EventModule.ACTIONS_SCRIPT);

    private String name;
    private EventCaptureProvider capture;
    private long delay;
    private long longDelay;
    private List<Consumer<Point>> actions;

    @Override
    public boolean initialise(Environment env, String[] args) {
        this.name = args[0];
        Application.log(Application.Level.INFO, "initialising event handler: %s", name);
        if (args.length < 5) {
            throw new IllegalArgumentException("insufficient arguments for event handler module");
        }
        try (FileInputStream fis = new FileInputStream(Paths.get("./tales-clicker/", args[1] + ".png").toFile())) {
            this.capture = EventCaptureProvider.anchor(ImageIO.read(fis));
        } catch (IOException e) {
            Application.log(Application.Level.ERROR, "failed to load anchor image for event handler: %s", name);
            throw new IllegalStateException("failed to load capture image", e);
        }
        this.delay = Long.parseLong(args[2]);
        this.longDelay = args[3] != null && !args[3].isEmpty() ? Long.parseLong(args[3]) : this.delay;
        this.actions = new ArrayList<>();
        Matcher matcher = ACTIONS_SCRIPT_PATTERN.matcher(args[4]);
        while (matcher.find()) {
            String func = matcher.group("func");
            String arg = matcher.group("args");
            try {
                actions.add(EventHandlerModule.parseAction(func, arg));
            }
            catch (RuntimeException e) {
                Application.log(Application.Level.WARN, "failed to parse action for event handler: %s", matcher.group(), e);
            }
        }
        return true;
    }

    private static Consumer<Point> parseAction(String func, String arg) {
        String[] args = !arg.isEmpty() ? arg.split(",", -1) : new String[0];
        switch (func) {
            case "Click":
                int x = args.length > 0 && !args[0].isEmpty() ? Integer.parseInt(args[0]) : 0;
                int y = args.length > 1 && !args[1].isEmpty() ? Integer.parseInt(args[1]) : 0;
                return point -> Actions.getClicker().click(new Point(point.x + x, point.y + y));
            case "Type":
                return point -> Actions.getTyper().type(arg);
            case "TypeQ":
                return point -> Actions.getTyper().quick(arg);
            case "Down":
                return point -> Actions.getTyper().down(arg);
            case "Up":
                return point -> Actions.getTyper().up(arg);
            case "Idle":
                long idleTime = args.length > 0 && !args[0].isEmpty() ? Long.parseLong(args[0]) : 0;
                return point -> Actions.getIdler().idle(idleTime);
            default:
                throw new IllegalArgumentException("unknown action function: " + func);
        }
    }

    @Override
    public ModuleControl execute() {
        Application.log(Level.INFO, "execute event handler: %s", name);
        Point anchor = capture.capture(Actions.getCapturer().capture()).findAnchor();
        if (anchor == null) {
            Application.log(Level.DEBUG, "no anchor found, enter long delay");
            return ModuleControl.next(this.longDelay);
        }
        Application.log(Level.INFO, "found anchor %s at %s execute actions", name, anchor);
        for (int i = 0; i < this.actions.size(); i++) {
            Application.log(Level.DEBUG, "execute action %d/%d for event handler: %s", i + 1, this.actions.size(), name);
            try {
                this.actions.get(i).accept(anchor);
            } catch (RuntimeException e) {
                Application.log(Application.Level.ERROR, "failed to execute action for event handler: %s", name);
                break;
            }
        }
        return ModuleControl.next(this.delay);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
