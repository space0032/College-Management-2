package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.RoleDAO;
import com.college.dao.PermissionDAO;
import com.college.models.Role;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class RoleController extends BaseController implements HttpHandler {

    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/roles/\\d+")) {
                if ("DELETE".equals(method)) handleDelete(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method)) handleGetAll(t);
                else if ("POST".equals(method)) handleCreate(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        List<Role> list = roleDAO.getAllRoles();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleCreate(HttpExchange t) throws IOException {
        String body = readBody(t);
        Role role = JsonHelper.fromJson(body, Role.class);
        if (role == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = roleDAO.createRole(role);
        if (ok) sendResponse(t, 201, JsonHelper.toJson(role));
        else sendResponse(t, 400, errorJson("Failed to create role"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        boolean ok = roleDAO.deleteRole(id);
        if (ok) sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else sendResponse(t, 400, errorJson("Failed to delete role"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
