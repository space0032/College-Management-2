package com.college.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiServerTest {
    @Test
    void usesLocalPortWhenRenderPortIsMissing() {
        assertEquals(7000, ApiServer.resolvePort(null));
        assertEquals(7000, ApiServer.resolvePort(" "));
    }

    @Test
    void acceptsRenderPort() {
        assertEquals(10000, ApiServer.resolvePort("10000"));
    }

    @Test
    void rejectsInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> ApiServer.resolvePort("invalid"));
        assertThrows(IllegalArgumentException.class, () -> ApiServer.resolvePort("70000"));
    }
}
