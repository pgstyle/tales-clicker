package org.pgstyle.talesclicker.action;

import java.awt.AWTException;
import java.awt.Robot;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

public final class Action {

    private static final Robot ROBOT;
    private static final Capturer CAPTURER;
    private static final Clicker CLICKER;
    private static final Idler IDLER;
    private static final Typer TYPER;

    static {
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            Application.log(Level.FATAL, "failed to create robot: %s", e);
            e.printStackTrace();
            throw new IllegalStateException("no windows toolkit", e);
        }
        CAPTURER = new Capturer(ROBOT);
        CLICKER = new Clicker(ROBOT);
        IDLER = new Idler(ROBOT);
        TYPER = new Typer(ROBOT);
    }

    public static Capturer getCapturer() {
        return Action.CAPTURER;
    }

    public static Clicker getClicker() {
        return Action.CLICKER;
    }

    public static Idler getIdler() {
        return Action.IDLER;
    }

    public static Typer getTyper() {
        return Action.TYPER;
    }

    private Action() {}

}
