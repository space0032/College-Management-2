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

    private final GoogleDriveService googleDriveService;

    private static final String UPLOAD_DIR_SYLLABI = "uploads/syllabi";
    private static final String UPLOAD_DIR_RESOURCES = "uploads/resources";

    // Max file size: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // Allowed extensions
    private static final String[] ALLOWED_EXTENSIONS = {
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".txt", ".zip", ".jpg", ".png"
    };

    public FileUploadService() {
        this.googleDriveService = new GoogleDriveService();
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

        // Try Google Drive first
        if (googleDriveService.isConfigured()) {
            try {
                // Determine mime type
                String mimeType = "application/octet-stream";
                if (extension.equals(".pdf")) mimeType = "application/pdf";
                else if (extension.equals(".doc") || extension.equals(".docx")) mimeType = "application/msword";
                else if (extension.equals(".ppt") || extension.equals(".pptx")) mimeType = "application/vnd.ms-powerpoint";
                else if (extension.equals(".xls") || extension.equals(".xlsx")) mimeType = "application/vnd.ms-excel";
                else if (extension.equals(".txt")) mimeType = "text/plain";
                else if (extension.equals(".zip")) mimeType = "application/zip";
                else if (extension.equals(".jpg") || extension.equals(".jpeg")) mimeType = "image/jpeg";
                else if (extension.equals(".png")) mimeType = "image/png";

                // We need to write InputStream to a temporary file because Drive API expects a File
                File tempFile = File.createTempFile("upload-", safeFilename);
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                String driveUrl = googleDriveService.uploadFile(tempFile, mimeType);
                
                // Delete temp file after upload attempt
                tempFile.delete();
                
                if (driveUrl != null) {
                    return driveUrl;
                }
                Logger.warn("Google Drive upload failed, falling back to local storage.");
            } catch (Exception e) {
                Logger.error("Failed to upload to Google Drive: " + e.getMessage(), e);
            }
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

        // Web Link (Assume Google Drive)
        if (path.startsWith("http")) {
            try {
                String fileId = GoogleDriveService.extractFileIdFromUrl(path);
                if (fileId != null) {
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destination)) {
                        googleDriveService.downloadFile(fileId, fos);
                    }
                } else {
                    throw new java.io.IOException("Could not extract Google Drive File ID from URL: " + path);
                }
            } catch (Exception e) {
                throw new java.io.IOException("Failed to download from Google Drive: " + e.getMessage(), e);
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
            // Google Drive Web Link
            if (path.startsWith("http")) {
                String fileId = GoogleDriveService.extractFileIdFromUrl(path);
                if (fileId != null) {
                    googleDriveService.deleteFile(fileId);
                    Logger.info("Deleted file from Google Drive: " + fileId);
                }
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
