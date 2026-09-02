package com.college.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.college.utils.EnvConfig;

/**
 * Simple in-memory token store for API authentication.
 * Stores user info keyed by session token.
 */
public class TokenStore {

    private static final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L;
    private static final long expirationMs = loadExpirationMs();

    public static String createToken(int userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenInfo(userId, username, role, System.currentTimeMillis() + expirationMs));
        return token;
    }

    public static TokenInfo getTokenInfo(String token) {
        if (token == null) {
            return null;
        }
        TokenInfo info = tokens.get(token);
        if (info != null && info.isExpired(System.currentTimeMillis())) {
            tokens.remove(token, info);
            return null;
        }
        return info;
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }

    public static class TokenInfo {
        public final int userId;
        public final String username;
        public final String role;
        public final long expiresAt;

        public TokenInfo(int userId, String username, String role, long expiresAt) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.expiresAt = expiresAt;
        }

        boolean isExpired(long now) {
            return expiresAt <= now;
        }
    }

    private static long loadExpirationMs() {
        String configured = EnvConfig.get("API_TOKEN_EXPIRATION_MS");
        if (configured == null || configured.isBlank()) {
            return DEFAULT_EXPIRATION_MS;
        }
        try {
            long value = Long.parseLong(configured);
            return value > 0 ? value : DEFAULT_EXPIRATION_MS;
        } catch (NumberFormatException ignored) {
            return DEFAULT_EXPIRATION_MS;
        }
    }
}
