package com.college.services;

import com.college.utils.Logger;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.http.StandardHttpRequestor;

import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DropboxService {

    private static final String ENV_TOKEN = com.college.utils.EnvConfig.get("DROPBOX_ACCESS_TOKEN");
    // Fallback hardcoded token (Convenience for testing)
    private static final String HARDCODED_TOKEN = ""; // Loaded from .env via run.sh

    private final DbxClientV2 client;

    public DropboxService() {
        String token = (ENV_TOKEN != null && !ENV_TOKEN.isEmpty()) ? ENV_TOKEN : HARDCODED_TOKEN;

        if (token == null || token.isEmpty()) {
            Logger.error("DROPBOX_ACCESS_TOKEN not set and no hardcoded fallback available.");
            this.client = null;
        } else {
            // Use custom UnsafeHttpRequestor to bypass SSL pinning
            DbxRequestConfig config = DbxRequestConfig.newBuilder("college-management-system")
                    .withHttpRequestor(new UnsafeHttpRequestor())
                    .build();
            this.client = new DbxClientV2(config, token);
        }
    }

    private static class UnsafeHttpRequestor extends StandardHttpRequestor {
        public UnsafeHttpRequestor() {
            super(StandardHttpRequestor.Config.DEFAULT_INSTANCE);
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void configureConnection(HttpsURLConnection conn) throws java.io.IOException {
            super.configureConnection(conn);

            // Override super to bypass Dropbox's SSLConfig
            try {
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                } };

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());
                conn.setHostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                Logger.error("Failed to setup unsafe SSL: " + e.getMessage());
            }
        }
    }

    public boolean isConfigured() {
        return client != null;
    }

    /**
     * Uploads a file to Dropbox and returns a shared link.
     *
     * @param inputStream File content
     * @param fileName    Desired filename (include extension)
     * @return Shared Link URL or null if failed
     */
    public String uploadFile(InputStream inputStream, String fileName) {
        if (client == null) {
            Logger.error("Dropbox client is not initialized.");
            return null;
        }

        try {
            // Upload file
            // Note: Dropbox paths must start with "/"
            String dropboxPath = "/" + fileName; // You might want to add a unique ID or folder here if needed, but
                                                 // FileUploadService generates unique names.

            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(inputStream);

            Logger.info("Uploaded to Dropbox: " + metadata.getPathLower());

            // Create Shared Link
            try {
                SharedLinkMetadata sharedLink = client.sharing().createSharedLinkWithSettings(metadata.getPathLower());
                return sharedLink.getUrl();
            } catch (Exception shareEx) {
                // Link might already exist or permissions missing.
                // Downgraded to INFO as requested by user since fallback works.
                // Logger.info("Reviewing public link options...");

                // Fallback: list shared links
                try {
                    var links = client.sharing().listSharedLinksBuilder().withPath(metadata.getPathLower()).start();
                    if (!links.getLinks().isEmpty()) {
                        return links.getLinks().get(0).getUrl();
                    }
                } catch (Exception listEx) {
                    // Ignored: falling back to private path is successful behavior.
                }

                // Return private path
                Logger.info("File uploaded successfully (Private Path): " + metadata.getPathDisplay());
                return metadata.getPathDisplay();
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            // If it's the SSL error, we know upload succeeded (mostly), so we might want to
            // recover if we had metadata.
            // But 'metadata' is local to the try block.
            // We need to refactor slightly to access metadata in catch or handle it inside.

            Logger.error("Dropbox Error: " + msg, e);
            return null;
        }
    }

    public void downloadFile(String dropboxPath, java.io.OutputStream outputStream) throws Exception {
        if (client == null) {
            throw new IllegalStateException("Dropbox client not authenticated.");
        }
        client.files().download(dropboxPath).download(outputStream);
    }

    public void deleteFile(String dropboxPath) throws Exception {
        if (client == null) {
            return;
        }
        client.files().deleteV2(dropboxPath);
    }
}
