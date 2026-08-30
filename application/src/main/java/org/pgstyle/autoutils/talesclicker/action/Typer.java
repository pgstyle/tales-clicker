package org.pgstyle.autoutils.talesclicker.action;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.application.Configuration;

/**
 * Key typer.
 *
 * @since 1.0
 * @author PGKan
 */
public final class Typer {

    Typer(Robot robot) {
        this.robot = robot;
        int[] timing = Configuration.getConfig().getTypeTiming();
        this.pressDelay = timing[0];
        this.actionDelay = timing[1];
        action = k -> {
            Arrays.stream(k).forEach(c -> {
                this.robot.keyPress(c);
                Actions.getIdler().idle(this.pressDelay);
            });
            Arrays.stream(k).forEach(this.robot::keyRelease);
            Actions.getIdler().idle(this.actionDelay);
        };
        quickAction = k -> {
            this.robot.keyPress(k);
            Actions.getIdler().idle(this.pressDelay);
            this.robot.keyRelease(k);
        };
        downAction = k -> {
            this.robot.keyPress(k);
            Actions.getIdler().idle(this.pressDelay);
        };
        upAction = k -> {
            this.robot.keyRelease(k);
            Actions.getIdler().idle(this.pressDelay);
        };
    }

    private final Robot robot;
    private final Consumer<int[]> action;
    private final Consumer<Integer> quickAction;
    private final Consumer<Integer> downAction;
    private final Consumer<Integer> upAction;
    private final int pressDelay;
    private final int actionDelay;

    private int decodeKey(String key) {
        switch (key) {
            case "CTRL":
                return KeyEvent.VK_CONTROL;
            case "SHIFT":
                return KeyEvent.VK_SHIFT;
            case "ALT":
                return KeyEvent.VK_ALT;
            case "SUPER":
            case "WIN":
                return KeyEvent.VK_WINDOWS;
            case "ENTER":
                return KeyEvent.VK_ENTER;
            case "ESC":
                return KeyEvent.VK_ESCAPE;
            case "SPACE":
                return KeyEvent.VK_SPACE;
            case "LEFT":
                return KeyEvent.VK_LEFT;
            case "DOWN":
                return KeyEvent.VK_DOWN;
            case "UP":
                return KeyEvent.VK_UP;
            case "RIGHT":
                return KeyEvent.VK_RIGHT;
            default:
                if (key.startsWith("NUM")) {
                    return KeyEvent.VK_NUMPAD0 + (key.charAt(3) - 48);
                } else if (key.startsWith("F") && key.length() > 1) {
                    return KeyEvent.VK_F1 + (key.charAt(1) - 49);
                } else {
                    return key.charAt(0);
                }
        }
    }

    /**
     * Type the specified key, with key modifier support.
     *
     * @param key the key to be typed
     */
    public void type(String key) {
        synchronized (this.robot) {
            Application.log(Level.DEBUG, "action.type %s", key);
            String[] keys = key.toUpperCase().split("[+\\-]");
            List<Integer> codes = new ArrayList<>();
            for (String k : keys) {
                codes.add(this.decodeKey(k));
            }
            this.action.accept(codes.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public void quick(String key) {
        synchronized (this.robot) {
            Application.log(Level.DEBUG, "action.type.quick %s", key);
            this.quickAction.accept(this.decodeKey(key));
        }
    }

    public void down(String key) {
        synchronized (this.robot) {
            Application.log(Level.DEBUG, "action.type.down %s", key);
            this.downAction.accept(this.decodeKey(key));
        }
    }

    public void up(String key) {
        synchronized (this.robot) {
            Application.log(Level.DEBUG, "action.type.up %s", key);
            this.upAction.accept(this.decodeKey(key));
        }
    }
}
