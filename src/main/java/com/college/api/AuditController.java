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

    private static final int MAX_LIMIT = 1000;

    @Override
    public void handle(HttpExchange t) throws IOException {
        // Note: this controller is wrapped in ProtectedHandler which performs
        // auth + CORS for registered contexts; keep OPTIONS handling for safety.
        addCorsHeaders(t);
        if ("OPTIONS".equals(t.getRequestMethod())) {
            t.sendResponseHeaders(204, -1);
            t.getResponseBody().close();
            return;
        }

        String method = t.getRequestMethod();
        String query = t.getRequestURI().getQuery();

        try {
            if ("GET".equals(method)) {
                handleGetAuditLogs(t, query);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGetAuditLogs(HttpExchange t, String query) throws IOException {
        if (!requirePermission(t, "VIEW_AUDIT")) return;
        String userIdParam = getQueryParam(query, "userId");
        String fromParam = getQueryParam(query, "from");
        String toParam = getQueryParam(query, "to");
        String limitParam = getQueryParam(query, "limit");
        String qParam = getQueryParam(query, "q");

        List<AuditLog> logs;

        try {
            if (qParam != null && !qParam.isEmpty()) {
                int limit = limitParam != null && !limitParam.isEmpty() ? Integer.parseInt(limitParam) : 200;
                if (limit <= 0) {
                    sendResponse(t, 400, errorJson("limit must be a positive integer"));
                    return;
                }
                logs = AuditLogDAO.searchLogs(qParam, Math.min(limit, MAX_LIMIT));
            } else if (userIdParam != null && !userIdParam.isEmpty()) {
                int userId = Integer.parseInt(userIdParam);
                if (userId <= 0) {
                    sendResponse(t, 400, errorJson("userId must be a positive integer"));
                    return;
                }
                logs = AuditLogDAO.getLogsByUser(userId);
            } else if (fromParam != null || toParam != null) {
                // One-sided filters are handled by bounding the open end (B-M7).
                LocalDate from = (fromParam != null && !fromParam.isEmpty())
                        ? LocalDate.parse(fromParam)
                        : LocalDate.of(1970, 1, 1);
                LocalDate to = (toParam != null && !toParam.isEmpty())
                        ? LocalDate.parse(toParam)
                        : LocalDate.now().plusDays(1);
                if (to.isBefore(from)) {
                    sendResponse(t, 400, errorJson("to must not be before from"));
                    return;
                }
                logs = AuditLogDAO.getLogsByDateRange(from, to);
            } else if (limitParam != null && !limitParam.isEmpty()) {
                int limit = Integer.parseInt(limitParam);
                if (limit <= 0) {
                    sendResponse(t, 400, errorJson("limit must be a positive integer"));
                    return;
                }
                logs = AuditLogDAO.getRecentLogs(Math.min(limit, MAX_LIMIT));
            } else {
                logs = AuditLogDAO.getRecentLogs(200);
            }
        } catch (NumberFormatException e) {
            sendResponse(t, 400, errorJson("Invalid numeric parameter (userId, limit, from, to)"));
            return;
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