package org.pgstyle.talesclicker.module.captcha;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pgstyle.talesclicker.action.Action;
import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.module.Environment;
import org.pgstyle.talesclicker.module.Module;
import org.pgstyle.talesclicker.module.ModuleControl;

public class CaptchaModule implements Module {

    private long shortDelay;
    private long longDelay;

    private List<List<Float>> solve(BufferedImage check) {
        float[][] confident = ConvolutionMask.convolution(check);
        List<Float> confidentLeft = new ArrayList<>();
        for (float f : confident[0]) {
            confidentLeft.add(f);
        }
        List<Float> confidentRight = new ArrayList<>();
        for (float f : confident[1]) {
            confidentRight.add(f);
        }
        List<List<Float>> result = new ArrayList<>();
        result.add(confidentLeft);
        result.add(confidentRight);
        return result;
    }

    private int[] tryCaptchaCode(BufferedImage check) {
        // calculate confident values
        List<List<Float>> confident = this.solve(check);
        // choose the two highest confident candidate as result
        int first = confident.get(0).indexOf(Collections.max(confident.get(0)));
        int second = confident.get(1).indexOf(Collections.max(confident.get(1)));
        Application.log(Level.INFO, "Captcha Number are [%d, %d]", first, second);
        Application.log(Level.INFO, "With Confident of [%f, %f]", confident.get(0).get(first), confident.get(1).get(second));
        return new int[] {first, second};
    }

    @Override
    public boolean initialise(Environment env, String[] args) {
        this.shortDelay = Configuration.getConfig().getCaptchaDelayShort();
        this.longDelay = Configuration.getConfig().getCaptchaDelayLong();
        return true;
    }

    @Override
    public ModuleControl execute() {
        // full screen capture to find dialog position
        BufferedImage screenshot = Action.getCapturer().capture();

        // check if error dialog exists
        ErrorCapture error = ErrorCapture.fromImage(screenshot);
        Point errorOffset = error.findOffset();
        if (Objects.nonNull(errorOffset)) {
            Application.log(Level.INFO, "found error dialog at %s", errorOffset);
            Action.getClicker().click(errorOffset);
            // wait short delay to retry recognition
            return ModuleControl.next(this.shortDelay);
        }

        // check if captcha dialog exists
        FullCapture full = FullCapture.fromImage(screenshot);
        Point fullOffset = full.findOffset();
        if (Objects.nonNull(fullOffset)) {
            String seqNo = AppUtils.timestamp();
            Application.log(Level.INFO, "found captcha dialog at %s", fullOffset);
            Application.log(Level.INFO, "captcha event seqNo: %s", seqNo);
            BufferedImage check = full.getCaptchaCapture().getImage();
            Application.log(check, "captchas/" + seqNo);

            // solve captcha to the end result
            int[] captcha = this.tryCaptchaCode(check);
            for (int code : captcha) {
                Application.log(Level.DEBUG, "handle captcha code: %d", code);
                PinPadCapture pinpad = full.getPinPadCapture();
                Point buttonOffset = pinpad.findNumber(code);
                if (Objects.nonNull(buttonOffset)) {
                    buttonOffset.translate(fullOffset.x, fullOffset.y);
                    Action.getClicker().click(buttonOffset);
                }
                else {
                    Application.log(Level.ERROR, "cannot find pinpad button");
                    break;
                }
            }
            // wait short delay in case of failed recognition
            return ModuleControl.next(this.shortDelay);
        }
        Application.log(Level.DEBUG, "no captcha dialog or error dialog found");
        // no dialog found, wait longer delay before next check
        return ModuleControl.next(this.longDelay);
    }

    @Override
    public boolean finalise(ModuleControl state) {
        // NOP
        return true;
    }
    
}
