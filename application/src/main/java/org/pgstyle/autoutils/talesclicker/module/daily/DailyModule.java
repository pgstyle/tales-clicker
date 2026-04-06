package org.pgstyle.autoutils.talesclicker.module.daily;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.ProgressMonitorInputStream;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
import org.pgstyle.autoutils.talesclicker.application.Configuration;
import org.pgstyle.autoutils.talesclicker.module.Environment;
import org.pgstyle.autoutils.talesclicker.module.Module;
import org.pgstyle.autoutils.talesclicker.module.ModuleControl;
import org.pgstyle.autoutils.talesclicker.module.notifier.LineNotifier;

public final class DailyModule implements Module {

    private long delay;

    private LineNotifier notifier;

    @Override
    public boolean initialise(Environment env, String[] args) {
        // load timing settings from config
        this.delay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("daily", "delay");
        this.notifier = new LineNotifier();
        return true;
    }

    private static DailyCapture takeCapture() {
        return DailyCapture.fromImage(Actions.getCapturer().capture());
    }

    private void actionDelay() {
        Actions.getIdler().idle(1000);
    }

    @Override
    public ModuleControl execute() {
        Application.log(Level.INFO, "start check daily");
        Actions.getTyper().type("ESC");
        this.actionDelay();
        Actions.getTyper().type("ENTER");
        this.actionDelay();
        Actions.getTyper().type("ESC");
        this.actionDelay();
        DailyCapture capture = DailyModule.takeCapture();
        this.confirm(capture);
        // daily login bonus
        if (!this.locator(capture, new Point(33, 10))) {
                Application.log(Level.WARN, "cannot find topbar locator");
                return ModuleControl.next(60000);
        }
        capture = DailyModule.takeCapture();
        if (!this.exists(capture, DailyCapture::findLoginOffset)) {
            Application.log(Level.WARN, "cannot find daily login page");
            return ModuleControl.next(60000);
        }
        boolean bonus = this.findAndClick(capture, DailyCapture::findBonusOffset, new Point(90, 20));
        capture = DailyModule.takeCapture();
        this.actionDelay();
        this.confirm(capture);
        this.close(capture);
        Actions.getTyper().type("ESC");
        this.actionDelay();

        //daily mission
        capture = DailyModule.takeCapture();
        if (!this.locator(capture, new Point(56, 10))) {
            Application.log(Level.WARN, "cannot find topbar locator");
            return ModuleControl.next(60000);
        }
        capture = DailyModule.takeCapture();
        this.findAndClick(capture, DailyCapture::findInactiveOffset);
        this.actionDelay();
        capture = DailyModule.takeCapture();
        if (!this.exists(capture, DailyCapture::findActiveOffset)) {
            Application.log(Level.WARN, "cannot find daily mission page");
            Actions.getTyper().type("ESC");
            this.actionDelay();
            Actions.getTyper().type("ESC");
            return ModuleControl.next(60000);
        }
        boolean reward = this.findAndClick(capture, DailyCapture::findRewardOffset, new Point(65, 10));
        capture = DailyModule.takeCapture();
        this.confirm(capture);
        reward &= !this.exists(capture, DailyCapture::findNoRewardOffset);
        Actions.getTyper().type("ESC");
        this.findAndClick(capture, DailyCapture::findCloseMissionOffset);
        Actions.getTyper().type("ESC");
        if (bonus) {
            this.notifier.notifies("[" + AppUtils.hostname() + "] 跑Online 每日登入已完成");
        }
        if (reward) {
            this.notifier.notifies("[" + AppUtils.hostname() + "] 跑Online 已收每日任務");
        }
        return ModuleControl.next(this.delay);
    }

    private boolean exists(DailyCapture capture, Function<DailyCapture, Point> offset) {
        Point button = offset.apply(capture);
        return Objects.nonNull(button);
    }

    private boolean findAndClick(DailyCapture capture, Function<DailyCapture, Point> offset) {
        return this.findAndClick(capture, offset, new Point());
    }

    private boolean findAndClick(DailyCapture capture, Function<DailyCapture, Point> offset, Point additionalOffset) {
        Point button = offset.apply(capture);
        if (Objects.nonNull(button)) {
            button = new Point(button.x + additionalOffset.x, button.y + additionalOffset.y);
            Actions.getClicker().click(button);
            this.actionDelay();
        }
        return Objects.nonNull(button);
    }

    private boolean locator(DailyCapture capture, Point offest) {
        boolean clicked = this.findAndClick(capture, DailyCapture::findLocaterOffset, offest);
        this.actionDelay();
        return clicked;
    }

    private boolean confirm(DailyCapture capture) {
        return this.findAndClick(capture, DailyCapture::findConfirmOffset);
    }

    private boolean close(DailyCapture capture) {
        return this.findAndClick(capture, DailyCapture::findCloseOffset);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
