package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class UserController extends BaseController implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/users/\\d+/password")) {
                if ("PUT".equals(method)) handleUpdatePassword(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/users/\\d+/role")) {
                if ("PUT".equals(method)) handleUpdateRole(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/users/\\d+")) {
                if ("DELETE".equals(method)) handleDelete(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/profile/contact")) {
                if ("PUT".equals(method)) handleUpdateMyContact(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/users")) {
                if ("GET".equals(method)) handleGetAll(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else sendResponse(t, 404, errorJson("Not found"));
        } catch (com.college.utils.ManagementException e) {
            sendResponse(t, e.getStatus(), errorJson(e.getMessage()));
        } catch (com.google.gson.JsonParseException | IllegalArgumentException e) {
            sendResponse(t, 400, errorJson("Invalid request input"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson("Could not complete user request"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_USER")) return;
        List<User> users = userDAO.getAllUsers();
        sendResponse(t, 200, JsonHelper.toJson(users));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_USER")) return;
        int id = extractId(path);
        new com.college.dao.AccessManagementDAO().deleteUser(getTokenInfo(t).userId, id);
        TokenStore.removeTokensForUser(id);
        sendResponse(t, 200, JSON.toJson(java.util.Map.of("status", "Deleted")));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateRole(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_USER")) return;
        String[] parts = path.split("/");
        int userId = Integer.parseInt(parts[parts.length - 2]); // /users/{id}/role
        int roleId = com.college.utils.ManagementValidation.integer(com.college.utils.ManagementValidation.object(readBody(t)), "roleId", 1, Integer.MAX_VALUE);
        new com.college.dao.AccessManagementDAO().assignRole(getTokenInfo(t).userId, userId, roleId);
        com.college.models.Role role = new com.college.dao.RoleDAO().getRoleById(roleId);
        TokenStore.refreshRoleForUser(userId, role.getCode());
        sendResponse(t, 200, JSON.toJson(java.util.Map.of("status", "Role updated")));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdatePassword(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_USER")) return;
        String[] parts = path.split("/");
        int userId = Integer.parseInt(parts[parts.length - 2]); // /users/{id}/password

        String body = readBody(t);
        java.util.Map<String, String> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);

        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            sendResponse(t, 400, errorJson("Old and new passwords required"));
            return;
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            sendResponse(t, 404, errorJson("User not found"));
            return;
        }

        if (authenticateUser(user.getUsername(), oldPassword) <= 0) {
            sendResponse(t, 401, errorJson("Current password is incorrect"));
            return;
        }

        boolean ok = userDAO.updatePassword(userId, newPassword);
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Password updated successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update password"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateMyContact(HttpExchange t) throws IOException {
        if (!requireAuth(t)) return;

        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        String phone = map.get("phone") == null ? null : String.valueOf(map.get("phone")).trim();
        String address = map.get("address") == null ? null : String.valueOf(map.get("address")).trim();
        if (phone == null || phone.isEmpty()) {
            sendResponse(t, 400, errorJson("Phone is required"));
            return;
        }

        String role = getTokenInfo(t).role;
        int userId = getTokenInfo(t).userId;
        String sql;
        if ("STUDENT".equals(role)) {
            sql = "UPDATE students SET phone=COALESCE(?, phone), address=COALESCE(?, address) WHERE user_id=?";
        } else if ("FACULTY".equals(role)) {
            sql = "UPDATE faculty SET phone=COALESCE(?, phone), address=COALESCE(?, address) WHERE user_id=?";
        } else {
            sendResponse(t, 400, errorJson("Only students and faculty can update contact info"));
            return;
        }
        try (java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, (address == null || address.isEmpty()) ? null : address);
            pstmt.setInt(3, userId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                sendResponse(t, 200, "{\"message\":\"Contact info updated\"}");
            } else {
                sendResponse(t, 404, errorJson("Profile not found"));
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Failed to update profile contact", e);
            sendResponse(t, 500, errorJson("Could not update contact info"));
        }
    }

    private int authenticateUser(String username, String password) {
        String sql = "SELECT id, password FROM users WHERE username=?";
        try (java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (com.college.utils.PasswordUtils.verifyPassword(password, storedHash)) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Authentication failed", e);
        }
        return 0;
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
