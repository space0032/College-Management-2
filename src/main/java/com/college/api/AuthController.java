package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.UserDAO;
import com.college.dao.RoleDAO;
import com.college.models.User;
import com.college.models.Role;
import com.college.utils.DatabaseConnection;
import com.college.utils.PasswordUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController extends BaseController implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String path = t.getRequestURI().getPath();
        String method = t.getRequestMethod();

        if (path.endsWith("/login") && "POST".equals(method)) {
            handleLogin(t);
        } else if (path.endsWith("/logout") && "POST".equals(method)) {
            handleLogout(t);
        } else if (path.endsWith("/session") && "GET".equals(method)) {
            handleSession(t);
        } else {
            sendResponse(t, 404, errorJson("Endpoint not found"));
        }
    }

    private void handleLogin(HttpExchange t) throws IOException {
        try {
            String body = readBody(t);
            // Parse username and password from JSON
            String username = extractJsonString(body, "username");
            String password = extractJsonString(body, "password");

            if (username == null || password == null) {
                sendResponse(t, 400, errorJson("Username and password required"));
                return;
            }

            int userId = authenticateUser(username, password);
            if (userId <= 0) {
                sendResponse(t, 401, errorJson("Invalid username or password"));
                return;
            }

            User user = userDAO.getUserById(userId);
            if (user == null) {
                sendResponse(t, 500, errorJson("User not found after authentication"));
                return;
            }

            String token = TokenStore.createToken(userId, username,
                    user.getRoleName() != null ? user.getRoleName() : user.getRole());

            Role role = new RoleDAO().getRoleById(user.getRoleId());
            String permsJson = "[]";
            if (role != null && role.getPermissions() != null) {
                permsJson = com.college.utils.JsonHelper.toJson(role.getPermissions());
            }

            String resp = "{\"token\":\"" + token + "\","
                    + "\"user\":{\"id\":" + user.getId()
                    + ",\"username\":\"" + user.getUsername() + "\""
                    + ",\"role\":\"" + (user.getRoleName() != null ? user.getRoleName() : user.getRole()) + "\""
                    + ",\"roleId\":" + user.getRoleId() + ","
                    + "\"permissions\":" + permsJson + "}}";
            sendResponse(t, 200, resp);
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleLogout(HttpExchange t) throws IOException {
        String auth = t.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            TokenStore.removeToken(auth.substring(7));
        }
        sendResponse(t, 200, "{\"status\":\"Logged out\"}");
    }

    private void handleSession(HttpExchange t) throws IOException {
        TokenStore.TokenInfo info = getTokenInfo(t);
        if (info == null) {
            sendResponse(t, 401, errorJson("Unauthorized"));
            return;
        }
        sendResponse(t, 200, "{\"userId\":" + info.userId
                + ",\"username\":\"" + info.username + "\""
                + ",\"role\":\"" + info.role + "\"}");
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

    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int idx = json.indexOf(searchKey);
        if (idx < 0)
            return null;
        int colon = json.indexOf(':', idx + searchKey.length());
        if (colon < 0)
            return null;
        // Find value start
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start)))
            start++;
        if (start >= json.length())
            return null;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            if (end < 0)
                return null;
            return json.substring(start + 1, end);
        }
        return null;
    }
}
