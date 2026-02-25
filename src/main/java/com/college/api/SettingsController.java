package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.SystemSettingsDAO;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsController extends BaseController implements HttpHandler {

    private final SystemSettingsDAO settingsDAO = new SystemSettingsDAO();

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
        // Return a predefined set of settings, or everything if we query the DB
        // For simplicity, we just fetch known keys
        String[] keys = {
            "college_name", "college_logo_url", "dropbox_api_key", "timezone", "default_theme"
        };
        
        Map<String, String> settings = new HashMap<>();
        for (String key : keys) {
            String val = settingsDAO.getSetting(key);
            settings.put(key, val != null ? val : "");
        }

        sendResponse(t, 200, JsonHelper.toJson(settings));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateSettings(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            settingsDAO.updateSetting(entry.getKey(), entry.getValue());
        }

        sendResponse(t, 200, "{\"message\":\"Settings updated successfully\"}");
    }
}
