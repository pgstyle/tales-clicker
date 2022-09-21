package org.pgstyle.talesclicker.clicker;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pgstyle.talesclicker.action.Action;
import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.module.Environment;
import org.pgstyle.talesclicker.module.ModuleManager;
import org.pgstyle.talesclicker.module.ModuleRunner;
import org.pgstyle.talesclicker.module.Signal;
import org.pgstyle.talesclicker.module.captcha.ConvolutionMask;
import org.pgstyle.talesclicker.module.captcha.ErrorCapture;
import org.pgstyle.talesclicker.module.captcha.FullCapture;
import org.pgstyle.talesclicker.module.captcha.PinPadCapture;
import org.pgstyle.talesclicker.module.notifier.DisconnectCapture;

public final class TalesClicker {
    public static final TalesClicker INSTANCE = new TalesClicker();

    private TalesClicker() {}

    public boolean isDisconnected() {
        return DisconnectCapture.fromImage(Action.getCapturer().capture()).isDisconnected();
    }

    public boolean run() {
        String timestamp = AppUtils.timestamp();
        Application.log(Level.INFO, "start: %s", timestamp);
        boolean hit = false;

        
        BufferedImage screenshot = Action.getCapturer().capture();
        ErrorCapture error = ErrorCapture.fromImage(screenshot);
        Point errorOffset = error.findOffset();
        hit = Objects.nonNull(errorOffset);
        if (hit) {
            Application.log(Level.INFO, "error hit: %s", errorOffset);
            Action.getClicker().click(errorOffset);
        }
        else {
            FullCapture full = FullCapture.fromImage(screenshot);
            Point fullOffset = full.findOffset();
            hit = Objects.nonNull(fullOffset);
            if (hit) {
                Application.log(Level.INFO, "captcha hit: %s", fullOffset);
                BufferedImage check = full.getCaptchaCapture().getImage();
                float[][] confident = ConvolutionMask.convolution(check);
                List<Float> confidentLeft = new ArrayList<>();
                for (float f : confident[0]) {
                    confidentLeft.add(f);
                }
                List<Float> confidentRight = new ArrayList<>();
                for (float f : confident[1]) {
                    confidentRight.add(f);
                }
                int first = confidentLeft.indexOf(Collections.max(confidentLeft));
                int second = confidentRight.indexOf(Collections.max(confidentRight));
                Application.log(Level.INFO, "Captcha Number are [%d, %d]", first, second);
                Application.log(Level.INFO, "With Confident of [%f, %f]", confident[0][first], confident[1][second]);
                Application.log(check, "captchas/" + timestamp);
                PinPadCapture pinpad = full.getPinPadCapture();
                Point firstPoint = pinpad.findNumber(first);
                firstPoint.translate(fullOffset.x, fullOffset.y);
                firstPoint.translate(PinPadCapture.PINPAD_OFFSET.x, PinPadCapture.PINPAD_OFFSET.y);
                Action.getClicker().click(firstPoint);
                Point secondPoint = pinpad.findNumber(second);
                secondPoint.translate(fullOffset.x, fullOffset.y);
                secondPoint.translate(PinPadCapture.PINPAD_OFFSET.x, PinPadCapture.PINPAD_OFFSET.y);
                Action.getClicker().click(secondPoint);
            }
        }
        Application.log(Level.INFO, "hit: %s", hit);
        Application.log(Level.INFO, "end: %s", timestamp);
        return hit;
    }

    public static int main(String[] args) {
        ModuleRunner manager = ModuleRunner.of(ModuleManager.class, Environment.getInstance(), new String[0]);
        manager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Application.log(Level.INFO, "JVM Shutdown");
            manager.shutdown(Signal.TERMINATE);
            try {
                manager.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }));
        try {
            manager.join();
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return 130;
        }
    }
}
