package org.pgstyle.talesclicker.clicker;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import org.pgstyle.talesclicker.action.Action;
import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.imagedb.ConvolutionMask;
import org.pgstyle.talesclicker.imagedb.DisconnectCapture;
import org.pgstyle.talesclicker.imagedb.ErrorCapture;
import org.pgstyle.talesclicker.imagedb.FullCapture;
import org.pgstyle.talesclicker.imagedb.PinPadCapture;

public final class TalesClicker {
    public static final TalesClicker INSTANCE = new TalesClicker();

    private TalesClicker() {}

    public boolean isDisconnected() {
        return DisconnectCapture.fromImage(Action.getCapturer().capture()).isDisconnected();
    }

    public boolean run() {
        String timestamp = AppUtils.timestamp();
        Application.log("start: %s", timestamp);
        boolean hit = false;

        
        BufferedImage screenshot = Action.getCapturer().capture();
        ErrorCapture error = ErrorCapture.fromImage(screenshot);
        Point errorOffset = error.findOffset();
        hit = Objects.nonNull(errorOffset);
        if (hit) {
            Application.log("error hit: %s", errorOffset);
            Action.getClicker().click(errorOffset);
        }
        else {
            FullCapture full = FullCapture.fromImage(screenshot);
            Point fullOffset = full.findOffset();
            hit = Objects.nonNull(fullOffset);
            if (hit) {
                Application.log("captcha hit: %s", fullOffset);
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
                Application.log("Captcha Number are [%d, %d]", first, second);
                Application.log("With Confident of [%f, %f]", confident[0][first], confident[1][second]);
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
        Application.log("hit: %s", hit);
        Application.log("end: %s", timestamp);
        return hit;
    }

    public static int main(String[] args) {
        boolean interrupted = false;
        while (!interrupted) {
            boolean hit = TalesClicker.INSTANCE.run();
            if (TalesClicker.INSTANCE.isDisconnected()) {
                try {
                    URL line = new URL("https://api.line.me/v2/bot/message/broadcast");
                    HttpsURLConnection connection = (HttpsURLConnection) line.openConnection();
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Content-Type", "application/json;charset=utf-8;encoding=utf-8");
                    connection.addRequestProperty("Authorization", "Bearer " + System.getenv("LINE_TOKEN"));
                    connection.setDoOutput(true);
                    String hostname = System.getenv("HOSTNAME");
                    byte[] bytes = new byte[1024];
                    if (Objects.isNull(hostname)) {
                        int length = Runtime.getRuntime().exec("hostname").getInputStream().read(bytes);
                        hostname = new String(bytes).substring(0, length);
                    }
                    connection.getOutputStream().write(("{ \"messages\":[{\"type\":\"text\", \"text\":\"" + hostname.trim() + " is down\" }]}").getBytes(StandardCharsets.UTF_8));
                    Application.log("disconnect notice: %d", connection.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            Action.getIdler().idle(hit ? 5000 : 30000);
            if (Thread.interrupted()) {
                Application.log("Interrupted");
                interrupted = true;
            }
        }
        return 130;
    }
}
