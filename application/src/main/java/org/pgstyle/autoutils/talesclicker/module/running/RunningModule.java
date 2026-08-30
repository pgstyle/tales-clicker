package org.pgstyle.autoutils.talesclicker.module.running;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;
import org.pgstyle.autoutils.talesclicker.module.ModuleManager;

public final class RunningModule implements Module {

    public static final String RUNNING_ON = "running.on";

    private Environment environment;

    private boolean worker;

    private long shortDelay;
    private long longDelay;
    private long shiftDelay;
    private long controlDelay;
    private long rightDelay;

    private Point onLocation;
    private boolean on;
    private long lastShift;
    private long lastControl;
    private long lastRight;

    @Override
    public boolean initialise(Environment env, String[] args) {
        this.environment = Environment.getInstance();
        if (args.length > 0 && args[0].isEmpty()) {
            Application.log(Level.INFO, "initialise %s as controller", this);
            this.environment.declareGlobal(RunningModule.RUNNING_ON, "false");
            ModuleManager.getManagerApi().register(RunningModule.class, "worker");
        }
        else if (args.length > 0 && args[0].equals("worker")) {
            Application.log(Level.INFO, "initialise %s as work", this);
            this.worker = true;
        }
        // load timing settings from config
        this.shortDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("running", "delay.short");
        this.longDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("running", "delay.long");
        this.shiftDelay = Configuration.getConfig().getModulePropertyAsInteger("running", "delay.shift");
        this.controlDelay = Configuration.getConfig().getModulePropertyAsInteger("running", "delay.control");
        this.rightDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("running", "delay.right");
        this.on = false;
        this. lastControl = this.lastShift = System.currentTimeMillis();
        return true;
    }

    @Override
    public ModuleControl execute() {
        if (this.worker) {
            Application.log(Level.INFO, "execute worker");
            return this.executeWorker();
        }
        else {
            Application.log(Level.INFO, "execute main");
            return this.executeMain();
        }
    }

    private ModuleControl executeMain() {
        BufferedImage screenshot;
        Point globalOffset;
        if (onLocation == null) {
            Application.log(Level.DEBUG, "no on found yet");
            globalOffset = new Point();
            screenshot = Actions.getCapturer().capture();
        }
        else {
            Application.log(Level.DEBUG, "last on found at %s", onLocation);
            globalOffset = new Point(
                onLocation.x - RunningCapture.ON_DIMENSION.width * 5,
                onLocation.y - RunningCapture.ON_DIMENSION.height * 5);
            screenshot = Actions.getCapturer().capture(
                globalOffset.x,
                globalOffset.y,
                onLocation.x + RunningCapture.ON_DIMENSION.width * 5, 
                onLocation.y + RunningCapture.ON_DIMENSION.height * 5);
        }
        RunningCapture capture = RunningCapture.fromImage(screenshot);
        onLocation = capture.findOnOffset();
        if (onLocation != null) {
            onLocation.translate(globalOffset.x, globalOffset.y);
        }
        Application.log(Level.DEBUG, "running.on = %s", onLocation != null);
        this.environment.setGlobal(RunningModule.RUNNING_ON, Boolean.toString(onLocation != null));
        Point offset = capture.findReadyOffset();
        if (offset == null) {
            offset = capture.findStartOffset();
        }
        if (offset != null) {
            offset.translate(globalOffset.x, globalOffset.y);
            Application.log(Level.DEBUG, "lobby button found at %s", offset);
            offset.translate(RunningCapture.LOBBY_DIMENSION.width / 2, RunningCapture.LOBBY_DIMENSION.height / 2);
            Actions.getClicker().click(offset);
        }
        return onLocation != null ? ModuleControl.next(this.shortDelay) : ModuleControl.next(this.longDelay);
    }

    private ModuleControl executeWorker() {
        if (this.environment.findGlobal(RunningModule.RUNNING_ON)
                && Boolean.parseBoolean(this.environment.getGlobal(RunningModule.RUNNING_ON))) {
            if (!this.on) {
                Application.log(Level.DEBUG, "start running");
                Actions.getTyper().down("UP");
                Actions.getTyper().down("Z");
            }
            this.on = true;
            long now = System.currentTimeMillis();
            if (now - this.lastShift >= this.shiftDelay) {
                Actions.getTyper().quick("SHIFT");
                this.lastShift = now;
            }
            if (now - this.lastControl >= this.controlDelay) {
                if (this.lastShift == now) {
                    Actions.getIdler().idle(Math.min(this.controlDelay, this.shiftDelay));
                }
                Actions.getTyper().quick("CTRL");
                this.lastControl = now;
            }
            if (now - this.lastRight >= this.rightDelay) {
                if (this.lastControl == now) {
                    Actions.getIdler().idle(Math.min(this.controlDelay, this.shiftDelay));
                }
                Actions.getTyper().quick("RIGHT");
                this.lastRight = now;
            }
            return ModuleControl.next(Math.min(this.controlDelay, this.shiftDelay));
        }
        Application.log(Level.DEBUG, "running.on is false");
        if (this.on) {
            Application.log(Level.DEBUG, "reset running");
            Actions.getTyper().up("UP");
            Actions.getTyper().up("Z");
            this.on = false;
        }
        return ModuleControl.next(this.longDelay);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }
    
}
