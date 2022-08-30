package org.pgstyle.talesclicker.imagedb;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.pgstyle.talesclicker.clicker.TalesClicker;

public final class ConvolutionMask {

    public static float[][] convolution(BufferedImage image) {
        float[][] confident = new float[2][10];
        for (int i = 0; i < 10; i++) {
            float max1 = 0, max2 = 0;
            for (ConvolutionMask mask : ConvolutionMask.MASKS.get(i)) {
                float[][] convoluted = mask.convolute(ConvolutionMask.quantify(image, a -> a / 256f));
                for (int y = 0; y < convoluted[0].length; y++) {
                    for (int x = 0; x < convoluted.length; x++) {
                        if (x < convoluted.length / 2) {
                            if (convoluted[x][y] > max1) {
                                max1 = convoluted[x][y];
                            }
                        }
                        else {
                            if (convoluted[x][y] > max2) {
                                max2 = convoluted[x][y];
                            }
                        }
                    }
                }
            }
            confident[0][i] = max1;
            confident[1][i] = max2;
        }
        return confident;
    } 

    public static final Map<Integer, Set<ConvolutionMask>> MASKS = ConvolutionMask.createMasks();

    public static float[][] quantify(BufferedImage image, Function<Integer, Float> i2f) {
        float[][] quantities = new float[image.getWidth()][image.getHeight()];
        for (int y = 0; y< image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                quantities[x][y] = i2f.apply(image.getRGB(x, y) & 0xff);
            }
        }
        return quantities;
    }

    public static BufferedImage unquantify(float[][] quantities, Function<Float, Integer> f2i) {
        BufferedImage image = new BufferedImage(quantities.length, quantities[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y< image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int value = f2i.apply(quantities[x][y]) & 0xff;
                image.setRGB(x, y, (value << 16) + (value << 8) + value);
            }
        }
        return image;
    }

    private static Map<Integer, Set<ConvolutionMask>> createMasks() {
        Map<Integer, Set<ConvolutionMask>> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Set<ConvolutionMask> set = new HashSet<>();
            for (int r = -45; r <= 45; r += 5) {
                BufferedImage image;
                try {
                    image = ImageIO.read(TalesClicker.loadResource("imagedb/mask-" + i + "-0.png"));
                    set.add(ConvolutionMask.fromImage(image, r));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
        for (int y = 0; y < confident[0].length; y++) {
            for (int x = 0; x < confident.length; x++) {
                float sum = 0;
                for (int j = 0; j < this.mask[0].length; j++) {
                    for (int i = 0; i < this.mask.length; i++) {
                        sum += this.mask[i][j] * image[x + i][y + j];
                    }
                }
                confident[x][y] = Math.max(sum / this.weight, 0);
            }
        }
        return confident;
    }

}
