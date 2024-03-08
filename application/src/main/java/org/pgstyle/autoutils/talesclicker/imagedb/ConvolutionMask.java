package org.pgstyle.autoutils.talesclicker.imagedb;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import org.pgstyle.autoutils.talesclicker.application.AppUtils;

/**
 * The {@code ConvolutionMask} uses a grey-scale mask of numerical digits to
 * calculate how closely an image matches a numerical digit.
 *
 * @since 1.1
 * @author PGKan
 */
public final class ConvolutionMask {

    /**
     * Convert the image into a matrix of float.
     *
     * @param image the image to be converted
     * @param i2f the convertion function
     * @return the converted matrix
     */
    public static float[][] quantify(BufferedImage image, IntFunction<Float> i2f) {
        float[][] quantities = new float[image.getWidth()][image.getHeight()];
        AppUtils.nestedLoop(0, image.getHeight(), 0, image.getWidth(),
                            (y, x) -> quantities[x][y] = i2f.apply(0xffffff & image.getRGB(x, y)));
        return quantities;
    }

    /**
     * Convert a matrix of float into an image.
     *
     * @param quantities the matrix to be converted
     * @param f2i the convertion function
     * @return the converted image
     */
    public static BufferedImage unquantify(float[][] quantities, ToIntFunction<Float> f2i) {
        BufferedImage image = new BufferedImage(quantities.length, quantities[0].length, BufferedImage.TYPE_INT_RGB);
        AppUtils.nestedLoop(0, image.getHeight(), 0, image.getWidth(),
                            (y, x) -> image.setRGB(x, y, ~ 0xffffff | f2i.applyAsInt(quantities[x][y])));
        return image;
    }

    public static ConvolutionMask fromImage(BufferedImage image, int rotation) {
        // rotate the image as a new mask
        AffineTransform transform = new AffineTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(10, 10));
        transform.concatenate(AffineTransform.getRotateInstance(Math.toRadians(rotation)));
        transform.concatenate(AffineTransform.getTranslateInstance(-10, -10));
        BufferedImage rotated = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) rotated.getGraphics();
        graphics.drawImage(image, transform, null);
        // quantify the image in grey-scale using only the blue channel
        return new ConvolutionMask(ConvolutionMask.quantify(rotated, i -> i == 0 ? -1f : (i & 0xff) / 256f));
    }

    private ConvolutionMask(float[][] mask) {
        this.mask = mask;
        // the sum of mask is used to normalise the confident value for
        // comparing confident to other masks
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

    /**
     * Perform convolution use this mask.
     *
     * @param image the matrix of the target image
     * @return the convolution result matrix
     */
    public float[][] convolute(float[][] image) {
        float[][] confident = new float[image.length - this.mask.length][image[0].length - this.mask[0].length];
        AppUtils.nestedLoop(0, confident[0].length, 0, confident.length, (y, x) -> {
            float[] sum = new float[1];
            AppUtils.nestedLoop(0, this.mask[0].length, 0, this.mask.length, (j, i) -> sum[0] += this.mask[i][j] * image[x + i][y + j]);
            // normalise the confident value use the total weight of the mask
            confident[x][y] = Math.max(sum[0] / this.weight, 0);
        });
        return confident;
    }

}
