package org.pgstyle.autoutils.talesclicker.action;

import java.awt.AWTException;
import java.awt.Robot;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;

/**
 * Robot actions, all robot activities should be perform via action provides by
 * this class.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Actions {

    private static final Robot ROBOT;
    private static final Capturer CAPTURER;
    private static final Clicker CLICKER;
    private static final Idler IDLER;
    private static final Typer TYPER;

    static {
        // all action performer should shared and synchronously use a robot object
        try {
            ROBOT = new Robot();
        } catch (AWTException e) {
            Application.log(Level.FATAL, "failed to create robot: %s", e);
            throw new IllegalStateException("no windows toolkit", e);
        }
        CAPTURER = new Capturer(ROBOT);
        CLICKER = new Clicker(ROBOT);
        IDLER = new Idler(ROBOT);
        TYPER = new Typer(ROBOT);
    }

    /**
     * Get the screenshot capturer.
     *
     * @return the capturer
     */
    public static Capturer getCapturer() {
        return Actions.CAPTURER;
    }

    /**
     * Get the pointer clicker.
     *
     * @return the clicker
     */
    public static Clicker getClicker() {
        return Actions.CLICKER;
    }

    /**
     * Get the thread idler.
     *
     * @return the idler
     */
    public static Idler getIdler() {
        return Actions.IDLER;
    }

    /**
     * Get the key typer.
     *
     * @return the typer
     */
    public static Typer getTyper() {
        return Actions.TYPER;
    }

    private Actions() {}

}
