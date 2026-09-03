package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.BookRequestDAO;
import com.college.models.BookRequest;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for library book requests. Backs the book request feature that was
 * previously only available in the JavaFX app.
 */
public class BookRequestController extends BaseController implements HttpHandler {

    private final BookRequestDAO bookRequestDAO;

    public BookRequestController() {
        this.bookRequestDAO = new BookRequestDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/book-requests") && "GET".equals(method)) {
                handleGetAll(t);
            } else if (path.matches(".*/book-requests/student/\\d+") && "GET".equals(method)) {
                handleGetByStudent(t, path);
            } else if (path.endsWith("/book-requests") && "POST".equals(method)) {
                handleCreate(t);
            } else if (path.matches(".*/book-requests/\\d+/approve") && "POST".equals(method)) {
                handleApprove(t, path);
            } else if (path.matches(".*/book-requests/\\d+/reject") && "POST".equals(method)) {
                handleReject(t, path);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "VIEW_LIBRARY", "MANAGE_LIBRARY"))
            return;
        List<BookRequest> list = bookRequestDAO.getPendingRequests();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_LIBRARY", "MANAGE_LIBRARY"))
            return;
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 1]);
        List<BookRequest> list = bookRequestDAO.getRequestsByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "VIEW_LIBRARY", "MANAGE_LIBRARY"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("studentId") == null || map.get("bookId") == null) {
            sendResponse(t, 400, errorJson("studentId and bookId are required"));
            return;
        }
        BookRequest br = new BookRequest();
        br.setStudentId(((Number) map.get("studentId")).intValue());
        br.setBookId(((Number) map.get("bookId")).intValue());
        br.setLoanPeriodDays(map.get("loanPeriodDays") != null ? ((Number) map.get("loanPeriodDays")).intValue() : 14);
        br.setRemarks((String) map.getOrDefault("remarks", ""));

        boolean ok = bookRequestDAO.createRequest(br);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Book request created\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create book request"));
        }
    }

    private void handleApprove(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_LIBRARY"))
            return;
        String[] parts = path.split("/");
        int requestId = Integer.parseInt(parts[parts.length - 2]);
        int userId = getTokenInfo(t) != null ? getTokenInfo(t).userId : 0;
        boolean ok = bookRequestDAO.approveRequest(requestId, userId);
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Book request approved and issued\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to approve request (book may be unavailable)"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleReject(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_LIBRARY"))
            return;
        String[] parts = path.split("/");
        int requestId = Integer.parseInt(parts[parts.length - 2]);
        int userId = getTokenInfo(t) != null ? getTokenInfo(t).userId : 0;
        String remarks = null;
        try {
            String body = readBody(t);
            Map<String, Object> map = JSON.fromJson(body, Map.class);
            if (map != null) {
                remarks = (String) map.get("remarks");
            }
        } catch (Exception ignored) {
        }
        boolean ok = bookRequestDAO.rejectRequest(requestId, userId, remarks != null ? remarks : "");
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Book request rejected\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to reject request"));
        }
    }
}
