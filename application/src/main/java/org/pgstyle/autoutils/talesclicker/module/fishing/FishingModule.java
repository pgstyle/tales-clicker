package org.pgstyle.autoutils.talesclicker.module.fishing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;

/**
 * The {@code FishingModule} can solve the macro-prevention captcha dialog. The
 * captcha dialog may appear after game session. When the module detects a
 * captcha dialog, it will guess the number and try to solve the captcha.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class FishingModule implements Module {

    private long shortDelay;
    private long longDelay;
    private long actionDelay;

    @Override
    public boolean initialise(Environment env, String[] args) {
        // load timing settings from config
        this.shortDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("fishing", "delay.short");
        this.longDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("fishing", "delay.long");
        this.actionDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("fishing", "delay.action");
        return true;
    }

    private static FishingCapture takeCapture() {
        return FishingCapture.fromImage(Actions.getCapturer().capture());
    }

    private void actionDelay() {
        Actions.getIdler().idle(this.actionDelay);
    }

    @Override
    public ModuleControl execute() {
        Application.log(Level.INFO, "start check fishing");
        FishingCapture capture = FishingModule.takeCapture();
        // start fishing if not started yet, then wait long delay
        if (!this.collectable(capture) && !this.started(capture)) {
            Application.log(Level.INFO, "fishing not started yet, try start fishing");
            this.startFishing(capture);
            this.actionDelay();
            capture = FishingModule.takeCapture();
            if (this.started(capture)) {
                Application.log(Level.DEBUG, "fishing started successfully");
                Application.log(Level.INFO, "fishing check skipped, wait %d seconds for next execution", this.longDelay / 1000);
                return ModuleControl.next(this.longDelay);
            }
            else {
                Application.log(Level.WARN, "cannot start fishing, wait %d seconds for next execution", this.shortDelay / 1000);
                return ModuleControl.next(this.shortDelay);
            }
        }
        // check again after short delay (default 10 sec) if not collectable yet
        if (!this.collectable(capture)) {
            Application.log(Level.INFO, "no collectable fish yet, wait %d seconds for next execution", this.shortDelay / 1000);
            return ModuleControl.next(this.shortDelay);
        }
        // collectable fish !!!
        Application.log(Level.INFO, "there are collectable fishes");
        this.confirm(capture);
        this.actionDelay();
        // stop fishing
        Application.log(Level.INFO, "fishing is started, stop fishing before collection");
        if (this.started(capture)) {
            this.stopFishing(capture);
        }
        // click collect fish, take new capture for send home button
        Application.log(Level.INFO, "collect fishes");
        this.collectFish(capture);
        this.actionDelay();
        capture = FishingModule.takeCapture();
        // click send home, take new capture for fish captcha
        Application.log(Level.INFO, "send fishes");
        this.sendFish(capture);
        this.actionDelay();
        capture = FishingModule.takeCapture();
        Application.log(Level.INFO, "solve fish captchas");
        int[] fishes = this.checkFishCaptcha(capture);
        if (fishes[0] < 0) {
            Application.log(Level.ERROR, "fish captcha solve failed, wait %d seconds for next execution", this.shortDelay / 1000);
            return ModuleControl.next(this.shortDelay);
        }
        String seqNo = AppUtils.timestamp();
        Application.log(Level.DEBUG, "fish captchas are [%d, %d]", fishes[0], fishes[1]);
        Application.log(Level.INFO, "captcha event seqNo: %s", seqNo);
        BufferedImage check = capture.getCaptchaImage();
        Application.log(check, "captchas/" + seqNo);
        this.selectFish(capture, fishes[0]);
        this.selectFish(capture, fishes[1]);
        this.actionDelay();
        capture = FishingModule.takeCapture();
        if (!this.confirm(capture)) {
            this.actionDelay();
            Actions.getTyper().type("ENTER");
        }
        this.startFishing(capture);
        Application.log(Level.INFO, "fishing check finished, wait %d seconds for next execution", this.longDelay / 1000);
        return ModuleControl.next(this.longDelay);
    }

    private boolean exists(FishingCapture capture, Function<FishingCapture, Point> offset) {
        Point button = offset.apply(capture);
        return Objects.nonNull(button);
    }

    private boolean findAndClick(FishingCapture capture, Function<FishingCapture, Point> offset) {
        Point button = offset.apply(capture);
        if (Objects.nonNull(button)) {
            button.translate(20, 20);
            Actions.getClicker().click(button);
            Actions.getIdler().idle(this.actionDelay);
        }
        return Objects.nonNull(button);
    }

    private boolean startFishing(FishingCapture capture) {
        return this.findAndClick(capture, FishingCapture::findStartOffset);
    }

    private boolean stopFishing(FishingCapture capture) {
        return this.findAndClick(capture, FishingCapture::findStopOffset);
    }

    private boolean collectFish(FishingCapture capture) {
        return this.findAndClick(capture, FishingCapture::findCollectOffset);
    }

    private boolean sendFish(FishingCapture capture) {
        return this.findAndClick(capture, FishingCapture::findSendOffset);
    }

    private int[] checkFishCaptcha(FishingCapture capture) {
        Point[] points = IntStream.range(0, 10)
                .mapToObj(capture::findFishOffset)
                .map(p -> Objects.isNull(p) ? new Point(Integer.MIN_VALUE, 0) : p)
                .collect(Collectors.toList()).toArray(new Point[0]);
        for (int i = 0; i < 10; i++) {
            points[i].y = i;
        }
        return Stream.of(points).sorted((a, b) -> Integer.compare(a.x, b.x)).skip(8).mapToInt(p -> p.x >= 0 ? p.y : -1).toArray();
    }

    private boolean selectFish(FishingCapture capture, int index) {
        return this.findAndClick(capture, c -> c.findFishButtonOffset(index));
    }

    private boolean confirm(FishingCapture capture) {
        return this.findAndClick(capture, FishingCapture::findConfirmOffset);
    }

    private boolean collectable(FishingCapture capture) {
        return this.exists(capture, FishingCapture::findCollectOffset);
    }

    private boolean started(FishingCapture capture) {
        return this.exists(capture, FishingCapture::findStopOffset);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
