package com.college.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.college.api.TokenStore.TokenInfo;
import com.college.utils.PermissionService;

/**
 * Base handler providing shared utilities for all API controllers.
 */
public abstract class BaseController {

    protected void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        addCorsHeaders(t);
        t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        t.getResponseBody().write(bytes);
        t.getResponseBody().close();
    }

    protected void addCorsHeaders(HttpExchange t) {
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    protected boolean handleOptions(HttpExchange t) throws IOException {
        if ("OPTIONS".equals(t.getRequestMethod())) {
            addCorsHeaders(t);
            t.sendResponseHeaders(204, -1);
            t.getResponseBody().close();
            return true;
        }
        return false;
    }

    protected String readBody(HttpExchange t) throws IOException {
        return new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected String errorJson(String message) {
        return "{\"error\":\"" + message.replace("\"", "'") + "\"}";
    }

    protected TokenStore.TokenInfo getTokenInfo(HttpExchange t) {
        String auth = t.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return TokenStore.getTokenInfo(auth.substring(7));
        }
        return null;
    }

    protected boolean requireAuth(HttpExchange t) throws IOException {
        TokenInfo tokenInfo = getTokenInfo(t);
        if (tokenInfo == null) {
            sendResponse(t, 401, errorJson("Unauthorized: Missing or invalid token"));
            return false;
        }
        return true;
    }

    protected boolean requirePermission(HttpExchange t, String permissionCode) throws IOException {
        TokenInfo tokenInfo = getTokenInfo(t);
        if (tokenInfo == null) {
            sendResponse(t, 401, errorJson("Unauthorized: Missing or invalid token"));
            return false;
        }
        if (!PermissionService.getInstance().hasPermission(tokenInfo.userId, permissionCode)) {
            sendResponse(t, 403, errorJson("Forbidden: Requires permission " + permissionCode));
            return false;
        }
        return true;
    }

    protected boolean requireAnyPermission(HttpExchange t, String... permissionCodes) throws IOException {
        TokenInfo tokenInfo = getTokenInfo(t);
        if (tokenInfo == null) {
            sendResponse(t, 401, errorJson("Unauthorized: Missing or invalid token"));
            return false;
        }
        if (!PermissionService.getInstance().hasAnyPermission(tokenInfo.userId, permissionCodes)) {
            sendResponse(t, 403, errorJson("Forbidden: Insufficient permissions"));
            return false;
        }
        return true;
    }

    protected java.util.Map<String, String> getQueryMap(HttpExchange t) {
        String query = t.getRequestURI().getQuery();
        java.util.Map<String, String> result = new java.util.HashMap<>();
        if (query == null)
            return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                try {
                    result.put(entry[0],
                            java.net.URLDecoder.decode(entry[1], java.nio.charset.StandardCharsets.UTF_8.name()));
                } catch (java.io.UnsupportedEncodingException e) {
                    result.put(entry[0], entry[1]);
                }
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    protected int getIntParam(java.util.Map<String, String> params, String key, int defaultValue) {
        try {
            String val = params.get(key);
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
