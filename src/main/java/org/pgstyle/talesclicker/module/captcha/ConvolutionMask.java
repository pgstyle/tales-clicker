package org.pgstyle.talesclicker.module.captcha;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import javax.imageio.ImageIO;

import org.pgstyle.talesclicker.application.AppUtils;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

public final class ConvolutionMask {

    public static float[][] convolution(BufferedImage image) {
        float[][] confident = new float[2][10];
        for (int i = 0; i < 10; i++) {
            float[] max = new float[2];
            for (ConvolutionMask mask : ConvolutionMask.MASKS.get(i)) {
                float[][] convoluted = mask.convolute(ConvolutionMask.quantify(image, a -> a / 256f));
                AppUtils.loop(0, convoluted[0].length, 0, convoluted.length, (y, x) -> {
                    int index = x < convoluted.length / 2 ? 0 : 1;
                    max[index] = Math.max(max[index], convoluted[x][y]);
                });
            }
            confident[0][i] = max[0];
            confident[1][i] = max[1];
        }
        return confident;
    } 

    private static final Map<Integer, Set<ConvolutionMask>> MASKS = ConvolutionMask.createMasks();

    public static float[][] quantify(BufferedImage image, IntFunction<Float> i2f) {
        float[][] quantities = new float[image.getWidth()][image.getHeight()];
        AppUtils.loop(0, image.getHeight(), 0, image.getWidth(), (y, x) -> quantities[x][y] = i2f.apply(image.getRGB(x, y) & 0xff));
        return quantities;
    }

    public static BufferedImage unquantify(float[][] quantities, ToIntFunction<Float> f2i) {
        BufferedImage image = new BufferedImage(quantities.length, quantities[0].length, BufferedImage.TYPE_INT_RGB);
        AppUtils.loop(0, image.getHeight(), 0, image.getWidth(), (y, x) -> {
            int value = f2i.applyAsInt(quantities[x][y]) & 0xff;
            image.setRGB(x, y, (value << 16) + (value << 8) + value);
        });
        return image;
    }

    private static Map<Integer, Set<ConvolutionMask>> createMasks() {
        Application.log(Level.DEBUG, "initialise convolution mask");
        Map<Integer, Set<ConvolutionMask>> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Set<ConvolutionMask> set = new HashSet<>();
            BufferedImage image;
            try {
                Application.log(Level.DEBUG, "load mask %d", i);
                image = ImageIO.read(AppUtils.getResource("imagedb/mask-" + i + "-0.png"));
                for (int r = -45; r <= 45; r += 5) {
                    set.add(ConvolutionMask.fromImage(image, r));
                }
            } catch (IOException e) {
                Application.log(Level.ERROR, "failed to load mask %d, %s", i, e);
                e.printStackTrace();
            }
            map.put(i, Collections.unmodifiableSet(set));
        }
        return Collections.unmodifiableMap(map);
    }

    private static ConvolutionMask fromImage(BufferedImage image, int rotation) {
        AffineTransform transform = new AffineTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(10, 10));
        transform.concatenate(AffineTransform.getRotateInstance(Math.toRadians(rotation)));
        transform.concatenate(AffineTransform.getTranslateInstance(-10, -10));
        BufferedImage rotated = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) rotated.getGraphics();
        graphics.drawImage(image, transform, null);
        return new ConvolutionMask(ConvolutionMask.quantify(rotated, i -> i == 0 ? -1f : i / 256f));
    }

    private ConvolutionMask(float[][] mask) {
        this.mask = mask;
        float sum = 0;
        for (float[] fs : mask) {
            for (float f : fs) {
                sum += Math.max(f, 0);
            }
        }
        this.weight = sum;
    }

    private final float[][] mask;
    private final float weight;

    public float[][] convolute(float[][] image) {
        float[][] confident = new float[image.length - this.mask.length][image[0].length - this.mask[0].length];
        AppUtils.loop(0, confident[0].length, 0, confident.length, (y, x) -> {
            float[] sum = new float[1];
            AppUtils.loop(0, this.mask[0].length, 0, this.mask.length, (j, i) -> sum[0] += this.mask[i][j] * image[x + i][y + j]);
            confident[x][y] = Math.max(sum[0] / this.weight, 0);
        });
        return confident;
    }

}
