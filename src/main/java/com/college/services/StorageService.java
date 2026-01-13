package com.college.services;

import javafx.scene.image.Image;
import java.io.File;

public interface StorageService {
    /**
     * Save a JavaFX Image object.
     * 
     * @param image    The image to save (e.g. cropped image)
     * @param fileName Preferred filename
     * @return Path or URL of the saved image
     */
    String saveImage(Image image, String fileName);

    /**
     * Save a File from disk.
     * 
     * @param file     The original file
     * @param fileName Preferred filename
     * @return Path or URL of the saved image
     */
    String saveImage(File file, String fileName);

    /**
     * Delete an image from storage.
     * 
     * @param path The path or identifier of the image to delete
     */
    void deleteImage(String path);
}
