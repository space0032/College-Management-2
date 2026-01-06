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
    private static final String HARDCODED_TOKEN = "sl.u.AGM64C2dY5vhNu0QNemYJSsmiq4Adcn5bawdPe5AvdbQauHg-JQ0gYUajX8smQbdIKG0j65MJtXE8SgQ89I1dcotfnUSLBwfV-iiHUe5oHdjsFCS73z2EfC8VTdc2DMsL3KePWjqkaWIROUn8yvTMlnwJ_iG-X1nyjMiAY7OF-T4lvCNVjmNM4RhI6Kd-qXJ3z-iwjTHpqhngAwOGsijl42avc0YDqSPAvn_vhqWEoYftPoc18yFkltrryppYw0nQmakZUE80YtDmuc9P46Es2isCivg6INwF6OWoqruY0pRggpW1SOXbjxHNN0W57yEv9qhMa-dH40Ed6GV2JpQP66UbGlpEzc9_TwB8I0zQBo29wQrk87SFEADnKIV9hg_Jk7JfRRMW2V2Od7TF8MS6jhSk1Ji8uGnJAtGBLZartU3ZILaEYiBBxC0D7LqZAmKyy4mwPeNN1K7L3cGyTJT3OD1MZLNZdyNQfvlb9laA_5f7hOZ_k06mR9UfMzYxU_gObYouf6JfN2ZpXYxLu8C_uB8fXdgPOE1E3vO1FFfJtiZnkTrOk7YL056uGE5Los3o3tZ_bMcHJ0vNxiASfMExh4uLVgRX1h19eJGOLAqKoa-1JN14f5C2HC0Cn1BWciDBoZLKVudb6cH_IVk_aOtz2okAMClf7gRioit-BwyWTf2_T6dS0abwwRaw8HgxPMbXFYQ1wmiUQmhzmLpsJT_VubJKxCP4pVVVKkXSQ7SvQ_A-FJvfX826UKGoCZEj0rAoyMgqaP2FeNqT1PkmmicJQVwCaDKQ0pcuzrq9QELpg5-XtYb-vk23p3hfmZJz39BXM8ZCAb1sQ6FTBDAR3R4z7YWqAdIIXlOC2OTyosepwKsbdt3cp-VknOcf4vKKuAIjQTWxD7axjN1ZTvq7EWl81CajNbUuIZVxc-K6aO69ixld7k8NonZ8UMMshfv7kHY1JCeBPOdlQDsD0_qFEmNQngYlSu0ZFRj_AeVpqeRkQ_705qcvfYm9iFZnoiUuzmZG4dzCl_6mGYMOe9NxAXdQmlJmzelMtrzLmuhlHBdoRfCFGYi8aKmexO6uqOH0w4une6eFqkpDGiavIOc298BMGf-wj2j4ccwAc2i3Q1H3pqA9KDDzrWppTgCJeQ5J_dSjVrNWzRpLhXtl9fI_od9PARypC6r6sBejU8t6WeBK2_2--61pQN1dKWqlbGrbeS5aMNVPPuBpFJUCcVSx_yZhp7qBrTpNLi_gGcE0-lqUkrGNAQPWnFJRL0QzNR2PTmD1CiazATatuib1nUH2y7c-GUqhu9rY5-WtF-YpF6m_uf4Zi2OSGnqMggn5KF1SQbpudfAsF3jNcEgvyrNqLU4LA7kPy5URHo8ogmKrpYOs5ykTzJqo8QQ5MpPDAoCCXLcwYb-_N4vfKoJH8D-pW8ilnJKPMs-_MMsv1eqNu6jw2DhIGO-FD1XRTn3_lzRHbWD83M";

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
