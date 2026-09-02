package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.UserDAO;
import com.college.dao.RoleDAO;
import com.college.dao.AuditLogDAO;
import com.college.models.User;
import com.college.models.Role;
import com.college.utils.DatabaseConnection;
import com.college.utils.PasswordUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonParseException;

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
            LoginRequest request = JSON.fromJson(readBody(t), LoginRequest.class);
            String username = request == null ? null : request.username;
            String password = request == null ? null : request.password;

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
            Map<String, Object> userPayload = new LinkedHashMap<>();
            userPayload.put("id", user.getId());
            userPayload.put("username", user.getUsername());
            userPayload.put("role", user.getRoleName() != null ? user.getRoleName() : user.getRole());
            userPayload.put("roleId", user.getRoleId());
            userPayload.put("permissions", role != null && role.getPermissions() != null
                    ? role.getPermissions() : java.util.List.of());
            AuditLogDAO.logAction(userId, username, "LOGIN", "USER", userId, "Web login succeeded");
            sendResponse(t, 200, JSON.toJson(Map.of("token", token, "user", userPayload)));
        } catch (JsonParseException e) {
            sendResponse(t, 400, errorJson("Malformed JSON request"));
        } catch (Exception e) {
            com.college.utils.Logger.error("API login failed", e);
            sendResponse(t, 500, errorJson("Authentication service unavailable"));
        }
    }

    private void handleLogout(HttpExchange t) throws IOException {
        String auth = t.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            TokenStore.TokenInfo info = TokenStore.getTokenInfo(auth.substring(7));
            if (info != null) {
                AuditLogDAO.logAction(info.userId, info.username, "LOGOUT", "USER", info.userId,
                        "Web logout succeeded");
            }
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
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("userId", info.userId);
        session.put("username", info.username);
        session.put("role", info.role);
        session.put("expiresAt", info.expiresAt);
        sendResponse(t, 200, JSON.toJson(session));
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

    private static final class LoginRequest {
        private String username;
        private String password;
    }
}
