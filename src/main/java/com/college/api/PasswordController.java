package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.utils.DatabaseConnection;
import com.college.utils.PasswordUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class PasswordController extends BaseController implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if ("PUT".equals(method) && path.matches(".*/users/\\d+/password")) {
                handleUpdatePassword(t, path);
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdatePassword(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int userId = Integer.parseInt(parts[parts.length - 2]); // /users/{id}/password

        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);

        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null
                || newPassword.trim().isEmpty()) {
            sendResponse(t, 400, errorJson("Old and new passwords required"));
            return;
        }

        // Verify old password
        User user = userDAO.getUserById(userId);
        if (user == null) {
            sendResponse(t, 404, errorJson("User not found"));
            return;
        }

        if (authenticateUser(user.getUsername(), oldPassword) <= 0) {
            sendResponse(t, 401, errorJson("Current password is incorrect"));
            return;
        }

        // Update password
        boolean ok = userDAO.updatePassword(userId, newPassword);
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Password updated successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update password"));
        }
    }

    private int authenticateUser(String username, String password) {
        String sql = "SELECT id, password FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtils.verifyPassword(password, storedHash)) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Authentication failed", e);
        }
        return 0;
    }
}
