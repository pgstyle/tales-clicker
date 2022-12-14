package org.pgstyle.talesclicker.action;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;

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
        action = (r, k) -> {
            Arrays.stream(k).forEach(c -> {
                r.keyPress(c);
                Actions.getIdler().idle(this.pressDelay);
            });
            Arrays.stream(k).forEach(r::keyRelease);
            Actions.getIdler().idle(this.actionDelay);
        };
    }

    private final Robot robot;
    private final BiConsumer<Robot, int[]> action;
    private final int pressDelay;
    private final int actionDelay;

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
                switch (k) {
                case "CTRL":
                    codes.add(KeyEvent.VK_CONTROL);
                    break;
                case "SHIFT":
                    codes.add(KeyEvent.VK_SHIFT);
                    break;
                case "ALT":
                    codes.add(KeyEvent.VK_ALT);
                    break;
                case "SUPER":
                case "WIN":
                    codes.add(KeyEvent.VK_WINDOWS);
                    break;
                case "ENTER":
                    codes.add(KeyEvent.VK_ENTER);
                    break;
                case "ESC":
                    codes.add(KeyEvent.VK_ESCAPE);
                    break;
                case "SPACE":
                    codes.add(KeyEvent.VK_SPACE);
                    break;
                default:
                    if (k.startsWith("NUM")) {
                        codes.add(KeyEvent.VK_NUMPAD0 + (k.charAt(3) - 48));
                    }
                    else if (k.startsWith("F") && k.length() > 1) {
                        codes.add(KeyEvent.VK_F1 + (k.charAt(1) - 49));
                    }
                    else {
                        codes.add((int) k.charAt(0));
                    }
                    break;
                }
            }
            this.action.accept(this.robot, codes.stream().mapToInt(Integer::intValue).toArray());
        }
    }

}
