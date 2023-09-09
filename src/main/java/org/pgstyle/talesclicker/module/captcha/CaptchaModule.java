package org.pgstyle.talesclicker.module.captcha;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pgstyle.talesclicker.action.Actions;
import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;
import org.pgstyle.talesclicker.module.Environment;
import org.pgstyle.talesclicker.module.Module;
import org.pgstyle.talesclicker.module.ModuleControl;

/**
 * The {@code CaptchaModule} can solve the macro-prevention captcha dialog. The
 * captcha dialog may appear after game session. When the module detects a
 * captcha dialog, it will guess the number and try to solve the captcha.
 *
 * @since 0.4-dev
 * @author PGKan
 */
public final class CaptchaModule implements Module {

    private long shortDelay;
    private long longDelay;

    /**
     * Use {@link ConvolutionMask} to solve the preprocessed captcha in the
     * image.
     *
     * @param check the image of the captcha
     * @return the confident value of each digit
     */
    private List<List<Float>> solve(BufferedImage check) {
        float[][] matrix = ConvolutionMask.convolution(check);
        List<List<Float>> result = new ArrayList<>();
        for (float[] confident : matrix) {
            List<Float> list = new ArrayList<>();
            for (float value : confident) {
                list.add(value);
            }
            result.add(list);
        }
        return result;
    }

    /**
     * Find captcha code in the preprocessed image.
     *
     * @param check the image of the captcha
     * @return an array of all captcha digits
     */
    private int[] tryCaptchaCode(BufferedImage check) {
        // calculate confident values
        List<List<Float>> list = this.solve(check);
        // choose the highest confident candidate as result
        float[] confident = new float[list.size()];
        int[] digits = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            confident[i] = Collections.max(list.get(i));
            digits[i] = list.get(i).indexOf(confident[i]);
        }
        Application.log(Level.INFO, "Captcha Number are %s", Arrays.toString(digits));
        Application.log(Level.INFO, "With Confident of %s", Arrays.toString(confident));
        return digits;
    }

    @Override
    public boolean initialise(Environment env, String[] args) {
        // load timing settings from config
        this.shortDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("captcha", "delay.short");
        this.longDelay = 1000l * Configuration.getConfig().getModulePropertyAsInteger("captcha", "delay.long");
        return true;
    }

    @Override
    public ModuleControl execute() {
        // full screen capture to find dialog position
        BufferedImage screenshot = Actions.getCapturer().capture();

        // check if error dialog exists
        ErrorCapture error = ErrorCapture.fromImage(screenshot);
        Point errorOffset = error.findOffset();
        if (Objects.nonNull(errorOffset)) {
            Application.log(Level.INFO, "found error dialog at %s", errorOffset);
            Actions.getClicker().click(errorOffset);
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
            for (int code : this.tryCaptchaCode(check)) {
                Application.log(Level.DEBUG, "handle captcha code: %d", code);
                PinPadCapture pinpad = full.getPinPadCapture();
                Point buttonOffset = pinpad.findNumber(code);
                if (Objects.nonNull(buttonOffset)) {
                    buttonOffset.translate(fullOffset.x, fullOffset.y);
                    Actions.getClicker().click(buttonOffset);
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
    public boolean finalise(ModuleControl control) {
        // NOP
        return true;
    }

}
