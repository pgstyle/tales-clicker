package org.pgstyle.talesclicker.clicker;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.pgstyle.talesclicker.imagedb.ConvolutionMask;
import org.pgstyle.talesclicker.imagedb.DisconnectCapture;
import org.pgstyle.talesclicker.imagedb.ErrorCapture;
import org.pgstyle.talesclicker.imagedb.FullCapture;
import org.pgstyle.talesclicker.imagedb.PinPadCapture;

public final class TalesClicker {
    public static final TalesClicker INSTANCE = new TalesClicker();
    
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static void log(String pattern, Object... args) {
        System.out.printf("[" + TalesClicker.LOG_FORMATTER.format(LocalDateTime.now()) + "] " + pattern + "%n", args);
    }

    private TalesClicker() {
        try {
            Robot robot = new Robot();
            this.capturer = new Capturer(robot);
            this.clicker = new Clicker(robot);
        } catch (AWTException e) {
            throw new IllegalStateException("no windows toolkit", e);
        }
    }

    private final Capturer capturer;
    private final Clicker clicker;

    public boolean isDisconnected() {
        return DisconnectCapture.fromImage(this.capturer.capture()).isDisconnected();
    }

    public boolean run() {
        String timestamp = TalesClicker.DT_FORMATTER.format(LocalDateTime.now());
        TalesClicker.log("start: %s", timestamp);
        boolean hit = false;

        ErrorCapture error = ErrorCapture.fromImage(this.capturer.capture());
        Point errorOffset = error.findOffset();
        hit = Objects.nonNull(errorOffset);
        if (hit) {
            TalesClicker.log("error hit: %s", errorOffset);
            this.clicker.click(errorOffset);
        }
        else {
            FullCapture full = FullCapture.fromImage(this.capturer.capture());
            Point fullOffset = full.findOffset();
            hit = Objects.nonNull(fullOffset);
            if (hit) {
                TalesClicker.log("captcha hit: %s", fullOffset);
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
                TalesClicker.log("Captcha Number are [%d, %d]", first, second);
                TalesClicker.log("With Confident of [%f, %f]", confident[0][first], confident[1][second]);
                LocalDateTime datetime = LocalDateTime.now();
                try {
                    ImageIO.write(check, "png", Paths.get("./tales-clicker/captchas/" + TalesClicker.DT_FORMATTER.format(datetime) + ".png").toFile());
                } catch (IOException e) {
                    TalesClicker.log("exception when output captcha: %s", e);
                    e.printStackTrace();
                }
                PinPadCapture pinpad = full.getPinPadCapture();
                Point firstPoint = pinpad.findNumber(first);
                firstPoint.translate(fullOffset.x, fullOffset.y);
                firstPoint.translate(PinPadCapture.PINPAD_OFFSET.x, PinPadCapture.PINPAD_OFFSET.y);
                clicker.click(firstPoint);
                Point secondPoint = pinpad.findNumber(second);
                secondPoint.translate(fullOffset.x, fullOffset.y);
                secondPoint.translate(PinPadCapture.PINPAD_OFFSET.x, PinPadCapture.PINPAD_OFFSET.y);
                clicker.click(secondPoint);
            }
        }
        TalesClicker.log("hit: %s", hit);
        TalesClicker.log("end: %s", timestamp);
        return hit;
    }

    public static int main(String[] args) {
        try {
            Files.createDirectories(Paths.get("./tales-clicker/captchas"));
        } catch (IOException e) {}
        try {
            Files.createDirectories(Paths.get("./tales-clicker/logs"));
        } catch (IOException e) {}
        try {
            System.setOut(new PrintStream(new RedirectOutputStream(System.out, new FileOutputStream("./tales-clicker/logs/" + System.currentTimeMillis() + ".log"))));
        } catch (FileNotFoundException e) { e.printStackTrace(); }
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
                    TalesClicker.log("disconnect notice: %d", connection.getResponseCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            try {
                Thread.sleep(hit ? 5000 : 30000);
            } catch (InterruptedException e) {
                TalesClicker.log("Interrupted: ", e.getMessage());
                interrupted = true;
            }
        }
        return 130;
    }
}
