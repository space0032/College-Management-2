package com.college.api;

import com.college.dao.CommunityDAO;
import com.college.models.Scholarship;
import com.college.models.ScholarshipApplication;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ScholarshipControllerTest {
    private ScholarshipController controller(CommunityDAO dao) {
        return new ScholarshipController(dao) {
            @Override protected boolean requirePermission(HttpExchange exchange, String permission) { return true; }
        };
    }

    private HttpExchange exchange(String method, String path, String body) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(URI.create(path));
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return exchange;
    }

    @Test void reviewUsesScholarshipIdFromPath() throws Exception {
        CommunityDAO dao = mock(CommunityDAO.class);
        when(dao.getApplications(42)).thenReturn(List.of());
        HttpExchange exchange = exchange("GET", "/api/scholarships/42/applications", "");
        controller(dao).handle(exchange);
        verify(dao).getApplications(42);
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
    }

    @Test void cannotUpdateApplicationUnderAnotherScholarship() throws Exception {
        CommunityDAO dao = mock(CommunityDAO.class);
        when(dao.getApplications(42)).thenReturn(List.of());
        HttpExchange exchange = exchange("PUT", "/api/scholarships/42/applications/9/status", "{\"status\":\"APPROVED\"}");
        controller(dao).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        verify(dao, never()).updateApplicationStatus(anyInt(), anyString());
    }

    @Test void rejectsNonPositiveAward() throws Exception {
        CommunityDAO dao = mock(CommunityDAO.class);
        HttpExchange exchange = exchange("POST", "/api/scholarships", "{\"title\":\"Merit\",\"description\":\"Eligibility\",\"amount\":-10}");
        controller(dao).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(400), anyLong());
        verify(dao, never()).createScholarship(any());
    }

    @Test void resolvesEnrollmentBeforePersistingApplication() throws Exception {
        CommunityDAO dao = mock(CommunityDAO.class);
        Scholarship scholarship = new Scholarship();
        scholarship.setId(42);
        scholarship.setStatus("OPEN");
        when(dao.getAllScholarships()).thenReturn(List.of(scholarship));
        when(dao.getApplications(42)).thenReturn(List.of());
        when(dao.applyForScholarship(any())).thenReturn(true);
        ScholarshipController controller = new ScholarshipController(dao) {
            @Override protected boolean requirePermission(HttpExchange exchange, String permission) { return true; }
            @Override protected int resolveStudentId(java.util.Map<String, Object> body, int numericId) {
                return "S100".equals(body.get("enrollmentId")) ? 7 : 0;
            }
        };
        HttpExchange exchange = exchange("POST", "/api/scholarships/42/applications", "{\"enrollmentId\":\"S100\",\"statement\":\"My academic goals\",\"status\":\"APPROVED\"}");
        controller.handle(exchange);
        verify(dao).applyForScholarship(argThat(app -> app.getStudentId() == 7
                && app.getScholarshipId() == 42 && "APPLIED".equals(app.getStatus())));
        verify(exchange).sendResponseHeaders(eq(201), anyLong());
    }

    @Test void rejectsDuplicateApplication() throws Exception {
        CommunityDAO dao = mock(CommunityDAO.class);
        Scholarship scholarship = new Scholarship();
        scholarship.setId(42);
        scholarship.setStatus("OPEN");
        ScholarshipApplication existing = new ScholarshipApplication();
        existing.setStudentId(7);
        when(dao.getAllScholarships()).thenReturn(List.of(scholarship));
        when(dao.getApplications(42)).thenReturn(List.of(existing));
        HttpExchange exchange = exchange("POST", "/api/scholarships/42/applications", "{\"studentId\":7,\"statement\":\"My academic goals\"}");
        controller(dao).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(409), anyLong());
        verify(dao, never()).applyForScholarship(any());
    }
}
