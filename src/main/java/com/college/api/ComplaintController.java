package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.ComplaintDAO;
import com.college.models.Complaint;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for hostel complaints.
 * Backs the hostel complaints feature that exists in the JavaFX app but had no
 * web endpoint.
 */
public class ComplaintController extends BaseController implements HttpHandler {

    private final ComplaintDAO complaintDAO;

    public ComplaintController() {
        this.complaintDAO = new ComplaintDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/complaints") && "GET".equals(method)) {
                handleGetAll(t);
            } else if (path.matches(".*/complaints/student/\\d+") && "GET".equals(method)) {
                handleGetByStudent(t, path);
            } else if (path.matches(".*/complaints/\\d+/status") && "PUT".equals(method)) {
                handleUpdateStatus(t, path);
            } else if (path.endsWith("/complaints") && "POST".equals(method)) {
                handleCreate(t);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_COMPLAINT"))
            return;
        List<Complaint> list = complaintDAO.getAllComplaints();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COMPLAINT", "CREATE_COMPLAINT"))
            return;
        int studentId = resolvePathStudentId(path.substring(path.lastIndexOf('/') + 1));
        List<Complaint> list = complaintDAO.getComplaintsByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "CREATE_COMPLAINT", "MANAGE_COMPLAINT"))
            return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null)) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
            return;
        }
        Complaint c = new Complaint();
        int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
            return;
        }
        c.setStudentId(studentId);
        c.setTitle((String) map.getOrDefault("title", ""));
        c.setDescription((String) map.getOrDefault("description", ""));
        c.setCategory((String) map.getOrDefault("category", "General"));

        boolean ok = complaintDAO.createComplaint(c);
        if (ok) {
            sendResponse(t, 201, "{\"status\":\"Complaint created\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create complaint. Verify the student has an active hostel allocation."));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateStatus(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_COMPLAINT"))
            return;
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 2]); // .../complaints/{id}/status
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null || map.get("status") == null) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        String status = (String) map.get("status");
        int resolvedBy = map.get("resolvedBy") != null ? ((Number) map.get("resolvedBy")).intValue() : 0;
        String remarks = (String) map.getOrDefault("remarks", "");

        boolean ok = complaintDAO.updateStatus(id, status, resolvedBy, remarks);
        if (ok) {
            sendResponse(t, 200, "{\"status\":\"Complaint updated\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update complaint"));
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
