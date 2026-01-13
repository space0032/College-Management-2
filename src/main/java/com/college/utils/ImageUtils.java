package com.college.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelFormat;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage fromFXImage(Image img, BufferedImage bimg) {
        PixelReader pr = img.getPixelReader();
        if (pr == null) {
            return null;
        }
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        if (bimg == null) {
            bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        int[] pixels = new int[w * h];
        pr.getPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);

        bimg.setRGB(0, 0, w, h, pixels, 0, w);
        return bimg;
    }
}
