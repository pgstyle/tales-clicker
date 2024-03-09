package org.pgstyle.autoutils.talesclicker.module.testing;

import java.awt.Point;
import java.util.Objects;
import java.util.function.Function;

import org.pgstyle.autoutils.talesclicker.action.Actions;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;
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
public final class TestingModule implements Module {

    private long shortDelay;

    @Override
    public boolean initialise(Environment env, String[] args) {
        // load timing settings from config
        this.shortDelay = 1000l * 10;
        return true;
    }

    private static TestingCapture takCapture() {
        return TestingCapture.fromImage(Actions.getCapturer().capture());
    }

    @Override
    public ModuleControl execute() {
        Application.log(Level.INFO, "start testing");
        TestingCapture capture = TestingModule.takCapture();
        Application.log(Level.INFO, "find and start chrome");
        this.findAndClick(capture, TestingCapture::findOffset);
        Application.log(Level.INFO, "dones testing");
        return ModuleControl.next(this.shortDelay);
    }

    private boolean findAndClick(TestingCapture capture, Function<TestingCapture, Point> offset) {
        Point button = offset.apply(capture);
        if (Objects.nonNull(button)) {
            button.translate(20, 20);
            Actions.getClicker().click(button);
            Actions.getClicker().click(button);
        }
        return Objects.nonNull(button);
    }

    @Override
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
