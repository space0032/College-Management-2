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

    private final DropboxService dropboxService;

    private static final String UPLOAD_DIR_SYLLABI = "uploads/syllabi";
    private static final String UPLOAD_DIR_RESOURCES = "uploads/resources";

    // Max file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // Allowed extensions
    private static final String[] ALLOWED_EXTENSIONS = {
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip", ".jpg", ".png"
    };

    public FileUploadService() {
        this.dropboxService = new DropboxService();
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

        // Try Dropbox first
        if (dropboxService.isConfigured()) {
            String dropboxUrl = dropboxService.uploadFile(inputStream, safeFilename);
            if (dropboxUrl != null) {
                return dropboxUrl;
            }
            Logger.warn("Dropbox upload failed, falling back to local storage.");
        }

        // Fallback to local storage
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

    public void downloadFile(String path, java.io.File destination) throws java.io.IOException {
        if (path == null)
            return;

        // Dropbox Private Path
        if (path.startsWith("/")) {
            try {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destination)) {
                    dropboxService.downloadFile(path, fos);
                }
            } catch (Exception e) {
                throw new java.io.IOException("Failed to download from Dropbox: " + e.getMessage(), e);
            }
        }
        // Local File
        else {
            java.io.File source = new java.io.File(path);
            if (!source.exists()) {
                return;
            }
            java.nio.file.Files.copy(source.toPath(), destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void deleteFile(String path) {
        if (path == null)
            return;

        try {
            // Dropbox
            if (path.startsWith("/")) {
                dropboxService.deleteFile(path); // dropboxService is final initialized in ctor
                Logger.info("Deleted file from Dropbox: " + path);
            }
            // Local
            else {
                java.io.File file = new File(path);
                if (file.exists()) {
                    file.delete();
                    Logger.info("Deleted local file: " + path);
                }
            }
        } catch (Exception e) {
            Logger.warn("Failed to delete file (" + path + "): " + e.getMessage());
        }
    }
}
