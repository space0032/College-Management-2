package com.college.services;

import com.college.utils.Logger;
import com.college.utils.EnvConfig;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;

public class GoogleDriveService {

    private static final String APPLICATION_NAME = "College Management System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private static final String CREDENTIALS_FILE_PATH = EnvConfig.get("GOOGLE_CREDENTIALS_PATH");
    private static final String DRIVE_FOLDER_ID = EnvConfig.get("DRIVE_FOLDER_ID");
    
    private Drive service;

    public GoogleDriveService() {
        try {
            if (CREDENTIALS_FILE_PATH == null || CREDENTIALS_FILE_PATH.isEmpty()) {
                Logger.error("GOOGLE_CREDENTIALS_PATH environment variable not set.");
                return;
            }

            // Fallback check if the folder ID isn't set
            if (DRIVE_FOLDER_ID == null || DRIVE_FOLDER_ID.isEmpty()) {
                Logger.error("DRIVE_FOLDER_ID environment variable not set.");
                return;
            }

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load credentials
            InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                    
            Logger.info("Google Drive service initialized successfully.");
        } catch (Exception e) {
            Logger.error("Failed to initialize Google Drive service: " + e.getMessage(), e);
            this.service = null;
        }
    }

    public boolean isConfigured() {
        return service != null;
    }

    /**
     * Uploads a file to Google Drive and returns a shared link.
     */
    public String uploadFile(java.io.File file, String mimeType) {
        if (service == null) {
            Logger.error("Google Drive client is not initialized.");
            return null;
        }

        try {
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setParents(Collections.singletonList(DRIVE_FOLDER_ID));

            FileContent mediaContent = new FileContent(mimeType, file);
            File uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, webContentLink")
                    .execute();
            
            Logger.info("Uploaded file to Google Drive: " + uploadedFile.getId());

            // Make it readable by anyone with the link
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            
            service.permissions().create(uploadedFile.getId(), permission).execute();

            return uploadedFile.getWebViewLink();
        } catch (Exception e) {
            Logger.error("Google Drive Upload Error: " + e.getMessage(), e);
            return null;
        }
    }

    public void downloadFile(String fileId, java.io.OutputStream outputStream) throws Exception {
        if (service == null) {
            throw new IllegalStateException("Google Drive client not authenticated.");
        }
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
    }

    public void deleteFile(String fileId) throws Exception {
        if (service == null) {
            return;
        }
        service.files().delete(fileId).execute();
    }
    
    /**
     * Helper to extract file ID from a Google Drive URL
     */
    public static String extractFileIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        
        // Example URL: https://drive.google.com/file/d/1abcdefg.../view?usp=drivesdk
        try {
            if (url.contains("/file/d/")) {
                String[] parts = url.split("/file/d/");
                if (parts.length > 1) {
                    return parts[1].split("/")[0];
                }
            } else if (url.contains("id=")) {
                String[] parts = url.split("id=");
                if (parts.length > 1) {
                    return parts[1].split("&")[0];
                }
            }
        } catch (Exception e) {
            Logger.warn("Failed to extract file ID from URL: " + url);
        }
        
        return null;
    }
}
