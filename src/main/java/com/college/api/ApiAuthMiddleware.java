package com.college.api;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class ApiAuthMiddleware {

    // Simple API Key for demonstration. In prod, use real JWT/OAuth
    private static final String API_KEY = "Bearer college-erp-secret-key";

    public static boolean isAuthenticated(HttpExchange t) {
        String authHeader = t.getRequestHeaders().getFirst("Authorization");
        return authHeader != null && authHeader.equals(API_KEY);
    }

    public static void sendUnauthorized(HttpExchange t) throws IOException {
        String resp = "{\"error\": \"Unauthorized - Invalid API Key\"}";
        t.sendResponseHeaders(401, resp.length());
        t.getResponseBody().write(resp.getBytes());
        t.getResponseBody().close();
    }
}
