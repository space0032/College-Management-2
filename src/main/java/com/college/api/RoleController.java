package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.RoleDAO;
import com.college.dao.PermissionDAO;
import com.college.models.Role;
import com.college.models.Permission;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RoleController extends BaseController implements HttpHandler {

    private final RoleDAO roleDAO = new RoleDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/roles/permissions")) {
                if ("GET".equals(method))
                    handleGetPermissionsList(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/roles/\\d+/permissions")) {
                if ("GET".equals(method))
                    handleGetRolePermissions(t, path);
                else if ("PUT".equals(method))
                    handleSetRolePermissions(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/roles/\\d+")) {
                if ("DELETE".equals(method))
                    handleDelete(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method))
                    handleGetAll(t);
                else if ("POST".equals(method))
                    handleCreate(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ROLE"))
            return;
        List<Role> list = roleDAO.getAllRoles();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleCreate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_ROLE"))
            return;
        String body = readBody(t);
        Role role = JsonHelper.fromJson(body, Role.class);
        if (role == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = roleDAO.createRole(role);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(role));
        else
            sendResponse(t, 400, errorJson("Failed to create role"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_ROLE"))
            return;
        int id = extractId(path);
        boolean ok = roleDAO.deleteRole(id);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else
            sendResponse(t, 400, errorJson("Failed to delete role"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private void handleGetPermissionsList(HttpExchange t) throws IOException {
        if (!requireAuth(t))
            return;
        List<Permission> list = new PermissionDAO().getAllPermissions();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetRolePermissions(HttpExchange t, String path) throws IOException {
        if (!requireAuth(t))
            return;
        String[] parts = path.split("/");
        int roleId = Integer.parseInt(parts[parts.length - 2]);
        Role r = roleDAO.getRoleById(roleId);
        if (r == null) {
            sendResponse(t, 404, errorJson("Role not found"));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(r.getPermissions()));
    }

    @SuppressWarnings("unchecked")
    private void handleSetRolePermissions(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_ROLE"))
            return;

        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        int requesterId = tokenInfo.userId;

        String[] parts = path.split("/");
        int roleId = Integer.parseInt(parts[parts.length - 2]);
        String body = readBody(t);
        List<Double> doubleIds = new com.google.gson.Gson().fromJson(body, java.util.List.class);
        if (doubleIds == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        List<Integer> permIds = new ArrayList<>();
        for (Double d : doubleIds)
            permIds.add(d.intValue());

        // RBAC VALIDATION: Prevent Privilege Escalation
        // Ensure user is not assigning permissions they don't have
        PermissionDAO permDAO = new PermissionDAO();
        List<Permission> requestedPerms = permDAO.getPermissionsByIds(permIds);
        com.college.utils.PermissionService permService = com.college.utils.PermissionService.getInstance();

        for (Permission p : requestedPerms) {
            if (!permService.hasPermission(requesterId, p.getCode())) {
                sendResponse(t, 403, errorJson(
                        "Forbidden: You cannot assign permission '" + p.getCode() + "' that you do not possess."));
                return;
            }
        }

        boolean ok = roleDAO.setRolePermissions(roleId, permIds);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Permissions updated\"}");
        else
            sendResponse(t, 400, errorJson("Failed to update permissions"));
    }
}
