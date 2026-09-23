package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.SystemSettingsDAO;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsController extends BaseController implements HttpHandler {

    private static final String DROPBOX_KEY = "dropbox_api_key";
    private static final String MASK = "********";

    private static final String[] KNOWN_KEYS = {
        "college_name", "college_logo_url", DROPBOX_KEY, "timezone", "default_theme", "accent_color"
    };

    private final SystemSettingsDAO settingsDAO = new SystemSettingsDAO();

    public static String[] getKnownKeys() {
        return KNOWN_KEYS;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/settings")) {
                if ("GET".equals(method)) handleGetSettings(t);
                else if ("PUT".equals(method)) handleUpdateSettings(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetSettings(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_SETTINGS")) return;

        Map<String, String> settings = new HashMap<>();
        for (String key : KNOWN_KEYS) {
            String val = settingsDAO.getSetting(key);
            if (DROPBOX_KEY.equals(key)) {
                // Never echo the secret back; a toggle indicates a stored value exists.
                settings.put(key, val != null && !val.isEmpty() ? MASK : "");
            } else {
                settings.put(key, val != null ? val : "");
            }
        }

        sendResponse(t, 200, JsonHelper.toJson(settings));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateSettings(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_SETTINGS")) return;
        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null) {
            sendResponse(t, 400, errorJson("Request body is required"));
            return;
        }

        boolean updated = false;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            // Whitelist: only persist known settings keys.
            if (!contains(KNOWN_KEYS, key)) {
                continue;
            }
            // Never overwrite the stored Dropbox key with the masked placeholder.
            if (DROPBOX_KEY.equals(key) && MASK.equals(entry.getValue())) {
                continue;
            }
            updated = true;
            settingsDAO.updateSetting(key, entry.getValue());
        }

        sendResponse(t, 200, updated
                ? "{\"message\":\"Settings updated successfully\"}"
                : "{\"message\":\"No valid settings provided\"}");
    }

    private boolean contains(String[] arr, String value) {
        for (String s : arr) {
            if (s.equals(value)) return true;
        }
        return false;
    }
}