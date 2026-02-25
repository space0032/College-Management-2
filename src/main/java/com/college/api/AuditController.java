package com.college.api;

import com.college.dao.AuditLogDAO;
import com.college.models.AuditLog;
import com.college.utils.JsonHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * AuditController — REST API for viewing audit log entries (Admin only).
 * GET /api/audit → last 200 entries
 * GET /api/audit?userId=N → logs for a specific user
 * GET /api/audit?from=&to= → logs in date range (YYYY-MM-DD)
 * GET /api/audit?limit=N → recent N entries
 */
public class AuditController extends BaseController implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        addCorsHeaders(t);
        if ("OPTIONS".equals(t.getRequestMethod())) {
            t.sendResponseHeaders(204, -1);
            return;
        }

        String method = t.getRequestMethod();
        String query = t.getRequestURI().getQuery();

        try {
            if ("GET".equals(method)) {
                handleGetAuditLogs(t, query);
            } else {
                sendResponse(t, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetAuditLogs(HttpExchange t, String query) throws IOException {
        String userIdParam = getQueryParam(query, "userId");
        String fromParam = getQueryParam(query, "from");
        String toParam = getQueryParam(query, "to");
        String limitParam = getQueryParam(query, "limit");

        List<AuditLog> logs;

        if (userIdParam != null && !userIdParam.isEmpty()) {
            int userId = Integer.parseInt(userIdParam);
            logs = AuditLogDAO.getLogsByUser(userId);
        } else if (fromParam != null && toParam != null) {
            LocalDate from = LocalDate.parse(fromParam);
            LocalDate to = LocalDate.parse(toParam);
            logs = AuditLogDAO.getLogsByDateRange(from, to);
        } else if (limitParam != null) {
            int limit = Integer.parseInt(limitParam);
            logs = AuditLogDAO.getRecentLogs(limit);
        } else {
            logs = AuditLogDAO.getRecentLogs(200);
        }

        sendResponse(t, 200, JsonHelper.toJson(logs));
    }

    /**
     * Small helper to extract a named query parameter value.
     */
    private String getQueryParam(String query, String name) {
        if (query == null || query.isEmpty())
            return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name))
                return kv[1];
        }
        return null;
    }
}
