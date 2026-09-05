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
        String name = role.getName() == null ? "" : role.getName().trim();
        if (name.isEmpty()) {
            sendResponse(t, 400, errorJson("Role name is required"));
            return;
        }
        role.setName(name);
        if (role.getCode() == null || role.getCode().isBlank()) {
            role.setCode(name.toUpperCase(java.util.Locale.ROOT)
                    .replaceAll("[^A-Z0-9]+", "_")
                    .replaceAll("^_+|_+$", ""));
        }
        if (role.getCode().isBlank()) {
            sendResponse(t, 400, errorJson("Role name must contain letters or numbers"));
            return;
        }
        role.setSystemRole(false);
        if (role.getPortalType() == null || role.getPortalType().isBlank()) {
            role.setPortalType("ADMIN");
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
        if (!requirePermission(t, "VIEW_ROLE"))
            return;
        List<Permission> list = new PermissionDAO().getAllPermissions();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetRolePermissions(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_ROLE"))
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

    private void handleSetRolePermissions(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_ROLE"))
            return;

        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        int requesterId = tokenInfo.userId;

        String[] parts = path.split("/");
        int roleId;
        try {
            roleId = Integer.parseInt(parts[parts.length - 2]);
        } catch (NumberFormatException e) {
            sendResponse(t, 400, errorJson("Invalid role id in path"));
            return;
        }
        if (roleDAO.getRoleById(roleId) == null) {
            sendResponse(t, 404, errorJson("Role not found"));
            return;
        }
        String body = readBody(t);
        List<Integer> permIds;
        try {
            permIds = parsePermissionIds(body);
        } catch (IllegalArgumentException e) {
            sendResponse(t, 400, errorJson(e.getMessage()));
            return;
        }

        // RBAC VALIDATION: Prevent Privilege Escalation
        // Ensure user is not assigning permissions they don't have.
        // (ADMIN passes every check via the PermissionService superuser bypass.)
        PermissionDAO permDAO = new PermissionDAO();
        List<Permission> requestedPerms = permDAO.getPermissionsByIds(permIds);
        if (requestedPerms.size() != permIds.size()) {
            java.util.Set<Integer> found = new java.util.HashSet<>();
            for (Permission p : requestedPerms)
                found.add(p.getId());
            List<Integer> unknown = new ArrayList<>();
            for (Integer id : permIds)
                if (!found.contains(id))
                    unknown.add(id);
            sendResponse(t, 400, errorJson("Unknown permission IDs: " + unknown));
            return;
        }
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

    /**
     * Parse the PUT body into a deduplicated list of permission IDs.
     * Accepts a raw JSON array ({@code [1,2,3]}, the format sent by the web
     * client) as well as an object-wrapped array ({@code {"permissionIds": [...]}}).
     * Elements may be numbers or numeric strings. Throws IllegalArgumentException
     * with a human-readable message for any malformed input.
     */
    private List<Integer> parsePermissionIds(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Invalid JSON: empty request body");
        }
        com.google.gson.JsonElement root;
        try {
            root = com.google.gson.JsonParser.parseString(body);
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON: malformed request body");
        }
        com.google.gson.JsonArray arr;
        if (root.isJsonArray()) {
            arr = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            com.google.gson.JsonObject obj = root.getAsJsonObject();
            if (obj.has("permissionIds") && obj.get("permissionIds").isJsonArray()) {
                arr = obj.getAsJsonArray("permissionIds");
            } else if (obj.has("ids") && obj.get("ids").isJsonArray()) {
                arr = obj.getAsJsonArray("ids");
            } else {
                throw new IllegalArgumentException("Invalid JSON: expected an array of permission IDs");
            }
        } else {
            throw new IllegalArgumentException("Invalid JSON: expected an array of permission IDs");
        }
        java.util.LinkedHashSet<Integer> ids = new java.util.LinkedHashSet<>();
        for (com.google.gson.JsonElement el : arr) {
            if (el.isJsonNull()) {
                continue;
            }
            try {
                if (el.isJsonPrimitive()) {
                    com.google.gson.JsonPrimitive prim = el.getAsJsonPrimitive();
                    if (prim.isNumber()) {
                        ids.add(prim.getAsInt());
                    } else if (prim.isString()) {
                        ids.add(Integer.parseInt(prim.getAsString().trim()));
                    } else if (prim.isBoolean()) {
                        throw new IllegalArgumentException("Invalid JSON: permission IDs must be numbers");
                    }
                } else {
                    throw new IllegalArgumentException("Invalid JSON: permission IDs must be numbers");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid JSON: permission IDs must be numbers");
            }
        }
        return new ArrayList<>(ids);
    }
}
