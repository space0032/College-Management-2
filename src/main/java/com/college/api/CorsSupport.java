package com.college.api;

import com.college.utils.EnvConfig;
import com.sun.net.httpserver.HttpExchange;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Applies the API's configured cross-origin policy consistently. */
final class CorsSupport {
    private static final String DEFAULT_ORIGINS = "http://localhost:3000,http://localhost:5173";
    private static final Set<String> ALLOWED_ORIGINS = loadAllowedOrigins();

    private CorsSupport() {
    }

    static void addHeaders(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Vary", "Origin");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    static boolean isOriginAllowed(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        return origin == null || ALLOWED_ORIGINS.contains(origin);
    }

    private static Set<String> loadAllowedOrigins() {
        String configured = EnvConfig.get("CORS_ALLOWED_ORIGINS");
        String value = configured == null || configured.isBlank() ? DEFAULT_ORIGINS : configured;
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
