package com.college.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenStoreTest {
    @Test
    void createdTokenHasAFutureExpirationAndCanBeRevoked() {
        String token = TokenStore.createToken(7, "student", "STUDENT");
        TokenStore.TokenInfo info = TokenStore.getTokenInfo(token);

        assertNotNull(info);
        assertFalse(info.isExpired(System.currentTimeMillis()));

        TokenStore.removeToken(token);
        assertNull(TokenStore.getTokenInfo(token));
    }

    @Test
    void tokenInfoRecognizesExpirationBoundary() {
        TokenStore.TokenInfo info = new TokenStore.TokenInfo(7, "student", "STUDENT", 1000L);

        assertFalse(info.isExpired(999L));
        assertTrue(info.isExpired(1000L));
    }
}
