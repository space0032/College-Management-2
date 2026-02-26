package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.AnnouncementDAO;
import com.college.models.Announcement;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class AnnouncementController extends BaseController implements HttpHandler {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/announcements/\\d+")) {
                if ("PUT".equals(method)) handleUpdate(t, path);
                else if ("DELETE".equals(method)) handleDelete(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method)) handleGetAll(t);
                else if ("POST".equals(method)) handleAdd(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ANNOUNCEMENT")) return;
        List<Announcement> list = announcementDAO.getAllAnnouncements();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleAdd(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_ANNOUNCEMENT")) return;
        String body = readBody(t);
        Announcement announcement = JsonHelper.fromJson(body, Announcement.class);
        if (announcement == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        int id = announcementDAO.addAnnouncement(announcement);
        if (id > 0) {
            sendResponse(t, 201, "{\"id\":" + id + "}");
        } else {
            sendResponse(t, 400, errorJson("Failed to add announcement"));
        }
    }

    private void handleUpdate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_ANNOUNCEMENT")) return;
        int id = extractId(path);
        String body = readBody(t);
        Announcement announcement = JsonHelper.fromJson(body, Announcement.class);
        if (announcement == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        announcement.setId(id);
        boolean ok = announcementDAO.updateAnnouncement(announcement);
        if (ok) sendResponse(t, 200, JsonHelper.toJson(announcement));
        else sendResponse(t, 400, errorJson("Failed to update announcement"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_ANNOUNCEMENT")) return;
        int id = extractId(path);
        boolean ok = announcementDAO.deleteAnnouncement(id);
        if (ok) sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else sendResponse(t, 400, errorJson("Failed to delete announcement"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
