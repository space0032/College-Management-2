package com.college.services;

import com.college.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadService {

    private static final String UPLOAD_DIR_SYLLABI = "uploads/syllabi";
    private static final String UPLOAD_DIR_RESOURCES = "uploads/resources";

    // Max file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // Allowed extensions
    private static final String[] ALLOWED_EXTENSIONS = {
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip", ".jpg", ".png"
    };

    public FileUploadService() {
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR_SYLLABI));
            Files.createDirectories(Paths.get(UPLOAD_DIR_RESOURCES));
        } catch (IOException e) {
            Logger.error("Failed to create upload directories", e);
        }
    }

    /**
     * Upload a syllabus file
     * 
     * @param inputStream      File content stream
     * @param originalFilename Original filename
     * @return Saved file path (relative) or null if failed
     */
    public String uploadSyllabus(InputStream inputStream, String originalFilename, long fileSize) {
        return saveFile(inputStream, originalFilename, fileSize, UPLOAD_DIR_SYLLABI);
    }

    /**
     * Upload a learning resource file
     * 
     * @param inputStream      File content stream
     * @param originalFilename Original filename
     * @return Saved file path (relative) or null if failed
     */
    public String uploadResource(InputStream inputStream, String originalFilename, long fileSize) {
        return saveFile(inputStream, originalFilename, fileSize, UPLOAD_DIR_RESOURCES);
    }

    private String saveFile(InputStream inputStream, String originalFilename, long fileSize, String targetDir) {
        // Validate size
        if (fileSize > MAX_FILE_SIZE) {
            Logger.error("File upload failed: Size exceeds limit (" + fileSize + " > " + MAX_FILE_SIZE + ")");
            return null;
        }

        // Validate extension
        String extension = getExtension(originalFilename);
        if (!isValidExtension(extension)) {
            Logger.error("File upload failed: Invalid extension " + extension);
            return null;
        }

        // Generate safe unique filename
        String safeFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(targetDir, safeFilename);

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            Logger.error("Failed to save file: " + originalFilename, e);
            return null;
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex).toLowerCase();
    }

    private boolean isValidExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get file for download
     */
    public File getFile(String relativePath) {
        File file = new File(relativePath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }
}
