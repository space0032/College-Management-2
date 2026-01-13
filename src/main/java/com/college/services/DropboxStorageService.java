package com.college.services;

import com.college.utils.EnvConfig;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

public class DropboxStorageService implements StorageService {

    private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
    private final String accessToken;

    public DropboxStorageService() {
        this.accessToken = EnvConfig.get("DROPBOX_ACCESS_TOKEN");
        if (this.accessToken == null || this.accessToken.isEmpty()) {
            System.err.println("Warning: DROPBOX_ACCESS_TOKEN not found in .env");
        }
    }

    @Override
    public String saveImage(Image image, String fileName) {
        try {
            // Convert JavaFX Image to ByteArray
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Use custom ImageUtils to avoid dependency issues with SwingFXUtils
            BufferedImage bImage = com.college.utils.ImageUtils.fromFXImage(image, null);
            if (bImage != null) {
                ImageIO.write(bImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                return uploadToDropbox(imageBytes, fileName);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String saveImage(File file, String fileName) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return uploadToDropbox(fileBytes, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uploadToDropbox(byte[] data, String fileName) {
        if (accessToken == null)
            return null;

        // Ensure unique filename for Dropbox (Student ID is usually in fileName)
        String remotePath = "/CollegeManagement/Profiles/" + fileName;

        // JSON for Dropbox-API-Arg
        String jsonArg = "{\"path\": \"" + remotePath
                + "\",\"mode\": \"add\",\"autorename\": true,\"mute\": false,\"strict_conflict\": false}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/octet-stream")
                .header("Dropbox-API-Arg", jsonArg)
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Upload success: " + response.body());
                // For simplicity, returning the remote path or a success indicator
                // In a production app, you might want to get a shared link here
                return "Dropbox: " + remotePath;
            } else {
                System.err.println("Dropbox Upload Failed (" + response.statusCode() + "): " + response.body());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteImage(String path) {
        if (accessToken == null || path == null || path.isEmpty())
            return;

        // Clean path if it has prefix
        String remotePath = path;
        if (remotePath.startsWith("Dropbox: ")) {
            remotePath = remotePath.substring("Dropbox: ".length());
        }

        // Must be a dropbox path starting with /
        if (!remotePath.startsWith("/")) {
            // If it's a full URL or something else, we ignore it or try to parse
            // For now, assume it must be like /CollegeManagement/...
            // If user passed a local path by mistake, we stop
            return;
        }

        String DELETE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
        String jsonBody = "{\"path\": \"" + remotePath + "\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DELETE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Dropbox Delete success: " + remotePath);
            } else {
                System.err.println("Dropbox Delete Failed (" + response.statusCode() + "): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getTemporaryLink(String path) {
        if (accessToken == null || path == null || path.isEmpty())
            return null;

        String remotePath = path;
        if (remotePath.startsWith("Dropbox: ")) {
            remotePath = remotePath.substring("Dropbox: ".length());
        }

        if (!remotePath.startsWith("/")) {
            return null;
        }

        String URL = "https://api.dropboxapi.com/2/files/get_temporary_link";
        String jsonBody = "{\"path\": \"" + remotePath + "\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse simple JSON to get "link"
                String json = response.body();
                int linkIndex = json.indexOf("\"link\":");
                if (linkIndex != -1) {
                    int startQuote = json.indexOf("\"", linkIndex + 7);
                    int endQuote = json.indexOf("\"", startQuote + 1);
                    if (startQuote != -1 && endQuote != -1) {
                        return json.substring(startQuote + 1, endQuote);
                    }
                }
            } else {
                System.err.println("Dropbox Get Link Failed (" + response.statusCode() + "): " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
