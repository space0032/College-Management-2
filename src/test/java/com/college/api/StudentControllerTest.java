package com.college.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentControllerTest {
    @Test
    void normalizesApiPrefixAndExtractsStudentId() {
        assertEquals("/students/13", StudentController.normalizePath("/api/students/13"));
        assertEquals("/students/13/courses", StudentController.normalizePath("/api/students/13/courses"));
        assertEquals("/students/13/enroll", StudentController.normalizePath("/api/students/13/enroll"));
        assertEquals("/students/search", StudentController.normalizePath("/api/students/search"));
        assertEquals(13, StudentController.extractStudentId("/api/students/13"));
        assertEquals(13, StudentController.extractStudentId("/students/13/courses"));
    }

    @Test
    void putToApiStudentPathReachesProtectedUpdateRoute() throws Exception {
        HttpExchange exchange = exchange("PUT", "/api/students/13");

        new StudentController().handle(exchange);

        verify(exchange).sendResponseHeaders(eq(401), anyLong());
    }

    @Test
    void unsupportedMethodStillReturnsMethodNotAllowed() throws Exception {
        HttpExchange exchange = exchange("PATCH", "/api/students/13");

        new StudentController().handle(exchange);

        verify(exchange).sendResponseHeaders(eq(405), anyLong());
    }

    private HttpExchange exchange(String method, String path) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(URI.create(path));
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
