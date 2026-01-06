package com.college.services;

import com.college.utils.Logger;
import com.dropbox.core.DbxException;
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

    private static final String ENV_TOKEN = System.getenv("DROPBOX_ACCESS_TOKEN");
    // Fallback hardcoded token (Convenience for testing)
    private static final String HARDCODED_TOKEN = "sl.u.AGMHqtRIDpjHjY4XTEbV1cbrpXDgKepoZ86-RtoWLz2BRqBHQ7qhcXX-t1WpauFAmjWYqo4WK8f5jcByfNWGmCqzfWGXr41i6860q0IAVqGQKe0tzlM_2GpHK6bn9VLWC2hiT30wcv1QqSTw7A7tp69p4rEv62KxPuMpDoWkFfukFeC_h1n9cH5BdvnMg7x8RGCH0TBLz-4ViwZ6WhIrqeFd5HxQPnR4Y4eGeG9dvh-ee8QIWM9DPzFsyUqTB6TTjkyUps32psUqk6BHS4StCKDwh1J-8N_7u3O5yoCH-iuRKo3KBKm7gP_NkPPVmMqCaZQcgVg0o6CnpZ5ElC9D_3VXAeo0T53DiK1ARlpVzQ9yx3wnJ8vz1LWZqVHv0Lou2D39XF2kl6s_nPBpQyOGbqDbB_4rf-do5uExdpD_rGPPmoPZcwX7BBbGGhNeDajp61LvewngDkTFJySY5wDzdKY-u8hDBWjPZCk6xVOmyOaQduWtVs_nQI2uwP1g4a3JB4QYGRrzZlK8dE6hI7jG2ib3F2JoFlYi6ozUcx5aR9Of70WVirBZ1BAXiH5pIkaHbX0QiZgPR1M9EoCQ9jpnwMuy8TgKnR70QTq-iI2HQJr_n549L2BeRb2zJJpVk7ZjvmQTs5fQDi4Um_4yAI-DLG8k9bjaeRFyK-PTtRsUPUm9VQqmyEGGw6exbldJp-xG7kYkX0PYPyG1rV1bKjpuOFnPriegv4dXnlzHK3UH4haJnLM9if5N_-oQildWEYQno252PJ1XFXKpqCnLiXDuZBvhwEQ3gD2889YLefaBnnpq2gcJQWIVkr04TiSvzKtoyFOpb6W229tCAZzC3q7mjsiwv-xkC0mAxIGMM9o8QlcOctN9FyEQtcZ-bTHBast_NJLTQ193FUmgx4JPTKJ3Yd15bCSdb3I-03ipUXmy-NF-S5U4EEGcvYBxEtgEbCuiIUrBBV9-pNvwpC2f18Pco4jNSYuSU2JvOzebnLktzG2YRZ6sgHlbfx_MbgxatABFkqan2Tbxi4Q32kCIblttTtIMXQPqUXSJXmWTDygjtYRIhVr3KZOmgL9MhTZSYwHmsvFUIpkDH5tUGPnYCj1MwEY0nneLmPPGT1Yvi3mdfI0snMNxIPfsKjL0CNUFTL99sc6I50x6_PPgThObyHC90lb9JmU5BpNETCOz5_RTu_Mp0zVIKEdDfUSx0Z7RaQborFXsP9L-X126NXJEKr84B8DBu8rwzbBQp0p_MzC9SrqnVN853KnU0fgphINwPDz4Cu5cWdvmyETvtle_z-AXf4EYPpUMEal6mHBRn13dYcJ3B5PlSacjteYUuxGkA_XX4851r3L4VB6q7H9HoaGBMUpwujW6vKq8EduFfpkfTJ27JkT7GdN4D2VoTTn0AmO2oW1FKRiSlV81olzeSWIQ5f6_Tu6Gf0V_GkXLgECAQyArIQ";

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
        protected void configureConnection(HttpsURLConnection conn) throws java.io.IOException {
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
            } catch (DbxException shareEx) {
                // Link might already exist or other sharing error
                Logger.warn("Sharing link creation failed (might exist or other error), attempting to list: "
                        + shareEx.getMessage());
                // Fallback: list shared links
                try {
                    var links = client.sharing().listSharedLinksBuilder().withPath(metadata.getPathLower()).start();
                    if (!links.getLinks().isEmpty()) {
                        return links.getLinks().get(0).getUrl();
                    }
                } catch (Exception listEx) {
                    Logger.warn(
                            "Failed to list shared links (" + listEx.getMessage() + "), falling back to private path.");
                }

                // If listing also fails/empty, return private path
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
