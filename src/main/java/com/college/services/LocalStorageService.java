package com.college.services;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LocalStorageService implements StorageService {

    private final String baseDir;

    public LocalStorageService() {
        this.baseDir = "user_data/profiles";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String saveImage(Image image, String fileName) {
        try {
            File outputFile = new File(baseDir, fileName);
            // Use custom ImageUtils to avoid dependency issues with SwingFXUtils
            BufferedImage bImage = com.college.utils.ImageUtils.fromFXImage(image, null);
            if (bImage != null) {
                ImageIO.write(bImage, "png", outputFile);
                return outputFile.getAbsolutePath();
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String saveImage(File file, String fileName) {
        try {
            File outputFile = new File(baseDir, fileName);
            Files.copy(file.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteImage(String path) {
        if (path == null || path.isEmpty())
            return;

        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Local file deleted: " + path);
            } else {
                System.err.println("Failed to delete local file: " + path);
            }
        }
    }
}
