package org.pgstyle.autoutils.talesclicker.imagedb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.pgstyle.autoutils.talesclicker.application.AppUtils;
import org.pgstyle.autoutils.talesclicker.application.Application;
import org.pgstyle.autoutils.talesclicker.application.Application.Level;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * The {@code Convolution} uses grey-scale masks for fuzzy image recognition.
 *
 * @since 1.1
 * @author PGKan
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Convolution {

    /**
     * Load convolution masks and create convolution masks.
     * 
     * @return all created masks
     */
    public static Convolution create(List<String> maskFiles) {
        return Convolution.create(maskFiles, 0, 0, 1);
    }

    /**
     * Load convolution masks and create multiple rotated masks for each mask
     * files.
     * 
     * @return all created masks
     */
    public static Convolution create(List<String> maskFiles, int min, int max, int step) {
        Application.log(Level.DEBUG, "initialise convolution mask");
        List<Set<ConvolutionMask>> masks = new ArrayList<>();
        for (int i = 0; i < maskFiles.size(); i++) {
            String file = maskFiles.get(i);
            Set<ConvolutionMask> set = new HashSet<>();
            try {
                Application.log(Level.DEBUG, "load mask %s", file);
                BufferedImage image = ImageIO.read(AppUtils.getResource(file));
                for (int r = min; r <= max; r += step) {
                    set.add(ConvolutionMask.fromImage(image, r));
                }
            } catch (IOException | IllegalArgumentException e) {
                Application.log(Level.ERROR, "failed to load mask %s, %s", file, e);
                e.printStackTrace();
            }
            masks.add(Collections.unmodifiableSet(set));
        }
        return new Convolution(masks);
    }

    private final List<Set<ConvolutionMask>> masks;

    /**
     * Calculate the confident value of an image.
     *
     * @param image the image to be calculated
     * @return the confident matrix
     */
    public float[][] convolution(BufferedImage image) {
        float[][] confident = new float[2][10];
        for (int i = 0; i < 10; i++) {
            float[] max = new float[2];
            // use the most matching mask rotation for that digit
            for (ConvolutionMask mask : this.masks.get(i)) {
                float[][] convoluted = mask.convolute(ConvolutionMask.quantify(image, a -> (a & 0xff) / 256f));
                AppUtils.nestedLoop(0, convoluted[0].length, 0, convoluted.length, (y, x) -> {
                    // left-half of the image is the first digit, and the other
                    // half is the second digit
                    int index = x < convoluted.length / 2 ? 0 : 1;
                    max[index] = Math.max(max[index], convoluted[x][y]);
                });
            }
            confident[0][i] = max[0];
            confident[1][i] = max[1];
        }
        return confident;
    }

}
