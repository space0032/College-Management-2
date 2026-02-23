package com.college.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class ApiAuthMiddleware {

    public static boolean isAuthenticated(HttpExchange t) {
        String authHeader = t.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        return TokenStore.getTokenInfo(authHeader.substring(7)) != null;
    }

    public static void sendUnauthorized(HttpExchange t) throws IOException {
        String resp = "{\"error\": \"Unauthorized - Invalid or expired token\"}";
        t.getResponseHeaders().set("Content-Type", "application/json");
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = resp.getBytes();
        t.sendResponseHeaders(401, bytes.length);
        t.getResponseBody().write(bytes);
        t.getResponseBody().close();
    }
}
