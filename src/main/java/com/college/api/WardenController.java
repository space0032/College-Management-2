package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.WardenDAO;
import com.college.models.Warden;
import com.college.utils.JsonHelper;
import com.college.utils.Logger;
import com.college.utils.WardenValidation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for warden management. Creating a warden auto-generates a WARDEN user
 * account (default password "123" for testing) exactly like the JavaFX app.
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
            Logger.error("Warden API error", e);
            sendResponse(t, 500, errorJson("Internal server error"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        String validationError = validatePayload(map);
        if (validationError != null) {
            sendResponse(t, 400, errorJson(validationError));
            return;
        }

        Warden w = toWarden(map, 0);
        int id = wardenDAO.addWarden(w);
        if (id > 0) {
            w.setId(id);
            sendResponse(t, 201, JsonHelper.toJson(w));
        } else {
            sendResponse(t, 400, errorJson(
                    "Failed to create warden. Check that the name is set, the email is valid and unused, "
                            + "the phone is valid, and the hostel (if given) exists."));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdate(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        String validationError = validatePayload(map);
        if (validationError != null) {
            sendResponse(t, 400, errorJson(validationError));
            return;
        }

        Warden w = toWarden(map, id);
        if (wardenDAO.updateWarden(w)) {
            sendResponse(t, 200, JsonHelper.toJson(w));
        } else {
            sendResponse(t, 400, errorJson("Failed to update warden. Check the email is valid and the hostel exists."));
        }
    }

    private String validatePayload(Map<String, Object> map) {
        if (map == null) {
            return "Invalid payload";
        }
        Object name = map.get("name");
        if (name == null || String.valueOf(name).isBlank()) {
            return "name is required";
        }
        Object email = map.getOrDefault("email", "");
        if (!WardenValidation.isValidEmail(String.valueOf(email))) {
            return "A valid email is required";
        }
        Object phone = map.getOrDefault("phone", "");
        if (!WardenValidation.isValidPhone(String.valueOf(phone))) {
            return "Enter a valid phone number";
        }
        return null;
    }

    private Warden toWarden(Map<String, Object> map, int id) {
        Warden w = new Warden();
        w.setId(id);
        w.setName(String.valueOf(map.get("name")).trim());
        w.setEmail(String.valueOf(map.getOrDefault("email", "")).trim());
        w.setPhone(String.valueOf(map.getOrDefault("phone", "")).trim());
        Object hostelId = map.get("hostelId");
        w.setHostelId(hostelId != null ? ((Number) hostelId).intValue() : 0);
        return w;
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}