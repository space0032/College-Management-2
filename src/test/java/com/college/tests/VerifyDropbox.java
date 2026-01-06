package com.college.tests;

import com.college.services.DropboxService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class VerifyDropbox {
    public static void main(String[] args) {
        System.out.println("Starting Dropbox Verification...");

        try {
            DropboxService service = new DropboxService();
            if (!service.isConfigured()) {
                System.err.println("FAILURE: DropboxService is NOT configured (Token missing?)");
                System.exit(1);
            }

            String dummyContent = "This is a test file for College Management System Dropbox Integration.";
            String fileName = "test_upload_" + System.currentTimeMillis() + ".txt";

            System.out.println("Attempting to upload: " + fileName);
            String url = service.uploadFile(
                    new ByteArrayInputStream(dummyContent.getBytes(StandardCharsets.UTF_8)),
                    fileName);

            if (url != null) {
                if (url.startsWith("http")) {
                    System.out.println("SUCCESS: File uploaded and Shared Link Created!");
                    System.out.println("Shared Link: " + url);
                } else if (url.startsWith("/")) {
                    System.out.println("SUCCESS: File uploaded (Private Path)!");
                    System.out.println("Path: " + url);
                } else {
                    System.out.println("SUCCESS: File uploaded: " + url);
                }
                System.exit(0);
            } else {
                System.err.println("FAILURE: Upload returned null or invalid URL.");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
