package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.WardenDAO;
import com.college.models.Warden;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for warden management. Backs the warden feature that previously had no
 * web endpoint. Creating a warden auto-generates a WARDEN user account
 * (default password "password123") exactly like the JavaFX app.
 */
public class WardenController extends BaseController implements HttpHandler {

    private final WardenDAO wardenDAO;

    public WardenController() {
        this.wardenDAO = new WardenDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/wardens") && "GET".equals(method)) {
                if (!requirePermission(t, "VIEW_HOSTEL"))
                    return;
                List<Warden> list = wardenDAO.getAllWardens();
                sendResponse(t, 200, JsonHelper.toJson(list));
            } else if (path.matches(".*/wardens/\\d+") && "GET".equals(method)) {
                if (!requirePermission(t, "VIEW_HOSTEL"))
                    return;
                int id = extractId(path);
                Warden w = wardenDAO.getWardenById(id);
                if (w != null) {
                    sendResponse(t, 200, JsonHelper.toJson(w));
                } else {
                    sendResponse(t, 404, errorJson("Warden not found"));
                }
            } else if (path.endsWith("/wardens") && "POST".equals(method)) {
                if (!requirePermission(t, "MANAGE_HOSTEL"))
                    return;
                handleCreate(t);
            } else if (path.matches(".*/wardens/\\d+") && "PUT".equals(method)) {
                if (!requirePermission(t, "MANAGE_HOSTEL"))
                    return;
                handleUpdate(t, path);
            } else if (path.matches(".*/wardens/\\d+") && "DELETE".equals(method)) {
                if (!requirePermission(t, "MANAGE_HOSTEL"))
                    return;
                int id = extractId(path);
                if (wardenDAO.deleteWarden(id)) {
                    sendResponse(t, 200, "{\"message\":\"Warden deleted\"}");
                } else {
                    sendResponse(t, 400, errorJson("Failed to delete warden"));
                }
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("name") == null) {
            sendResponse(t, 400, errorJson("name is required"));
            return;
        }
        Warden w = new Warden();
        w.setName((String) map.get("name"));
        w.setEmail((String) map.getOrDefault("email", ""));
        w.setPhone((String) map.getOrDefault("phone", ""));
        w.setHostelId(map.get("hostelId") != null ? ((Number) map.get("hostelId")).intValue() : 0);

        int id = wardenDAO.addWarden(w);
        if (id > 0) {
            w.setId(id);
            sendResponse(t, 201, JsonHelper.toJson(w));
        } else {
            sendResponse(t, 400, errorJson("Failed to create warden"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdate(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null) {
            sendResponse(t, 400, errorJson("Invalid body"));
            return;
        }
        Warden w = new Warden();
        w.setId(id);
        w.setName((String) map.getOrDefault("name", ""));
        w.setEmail((String) map.getOrDefault("email", ""));
        w.setPhone((String) map.getOrDefault("phone", ""));
        w.setHostelId(map.get("hostelId") != null ? ((Number) map.get("hostelId")).intValue() : 0);

        if (wardenDAO.updateWarden(w)) {
            sendResponse(t, 200, "{\"message\":\"Warden updated\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update warden"));
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
