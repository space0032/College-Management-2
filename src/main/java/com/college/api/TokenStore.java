package com.college.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory token store for API authentication.
 * Stores user info keyed by session token.
 */
public class TokenStore {

    private static final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public static String createToken(int userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenInfo(userId, username, role));
        return token;
    }

    public static TokenInfo getTokenInfo(String token) {
        return tokens.get(token);
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }

    public static class TokenInfo {
        public final int userId;
        public final String username;
        public final String role;

        public TokenInfo(int userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }
}
