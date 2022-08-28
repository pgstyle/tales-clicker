package org.pgstyle.talesclicker;

import java.awt.AWTException;
import java.io.IOException;
import java.time.LocalDateTime;

import org.pgstyle.talesclicker.clicker.TalesClicker;

public final class Main {

    public static void main(String[] args) throws AWTException, IOException, InterruptedException {
        System.exit(TalesClicker.main(args));
        // Files.walk(Paths.get("./imagedb/")).map(Path::toFile).skip(1).forEach(
        //     file -> {
        //         try {
        //             BufferedImage image = ImageIO.read(file);
        //             FullCapture capture = FullCapture.fromImage(image);
        //             if (Objects.nonNull(capture)) {
        //                 BufferedImage check = capture.getCaptchaCapture().getImage();
        //                 float[][] confident = ConvolutionMask.convolution(check);
        //                 List<Float> confidentLeft = new ArrayList<>();
        //                 for (float f : confident[0]) {
        //                     confidentLeft.add(f);
        //                 }
        //                 List<Float> confidentRight = new ArrayList<>();
        //                 for (float f : confident[1]) {
        //                     confidentRight.add(f);
        //                 }
        //                 System.out.println(file);
        //                 System.out.println(confidentLeft.indexOf(Collections.max(confidentLeft)));
        //                 System.out.println(confidentRight.indexOf(Collections.max(confidentRight)));
        //                 System.out.println(Arrays.toString(confident[0]));
        //                 System.out.println(Arrays.toString(confident[1]));
        //             }
        //         } catch (IOException e) {
        //             // TODO Auto-generated catch block
        //             e.printStackTrace();
        //         }
        //     }
        // );
    }

}
