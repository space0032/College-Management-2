package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.GatePassDAO;
import com.college.models.GatePass;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GatePassController extends BaseController implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/gatepass/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetStudentPasses(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/gatepass/pending")) {
                if ("GET".equals(method))
                    handleGetPendingPasses(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/gatepass/\\d+/approve")) {
                if ("PUT".equals(method))
                    handleApprovePass(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/gatepass/\\d+/reject")) {
                if ("PUT".equals(method))
                    handleRejectPass(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/gatepass")) {
                if ("GET".equals(method))
                    handleGetAllPasses(t);
                else if ("POST".equals(method))
                    handleCreateRequest(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetStudentPasses(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 1]);
        List<GatePass> passes = GatePassDAO.getStudentPasses(studentId);
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

    private void handleGetPendingPasses(HttpExchange t) throws IOException {
        List<GatePass> passes = GatePassDAO.getPendingPasses();
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

    private void handleGetAllPasses(HttpExchange t) throws IOException {
        List<GatePass> passes = GatePassDAO.getAllPasses();
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateRequest(HttpExchange t) throws IOException {
        String body = readBody(t);
        GatePass gatePass = JsonHelper.fromJson(body, GatePass.class);

        if (gatePass == null || gatePass.getStudentId() == 0) {
            sendResponse(t, 400, errorJson("Invalid payload or missing studentId"));
            return;
        }

        boolean success = GatePassDAO.createRequest(gatePass);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Gate pass requested successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create request. Ensure student has active hostel allocation."));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleApprovePass(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int passId = Integer.parseInt(parts[parts.length - 2]); // .../gatepass/{id}/approve

        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null || map.get("approvedBy") == null) {
            sendResponse(t, 400, errorJson("approvedBy is required"));
            return;
        }

        int approvedBy = ((Double) map.get("approvedBy")).intValue();
        String comment = map.get("comment") != null ? (String) map.get("comment") : "Approved";

        boolean success = GatePassDAO.approveRequest(passId, approvedBy, comment);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Gate pass approved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to approve gate pass"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRejectPass(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int passId = Integer.parseInt(parts[parts.length - 2]); // .../gatepass/{id}/reject

        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null || map.get("rejectedBy") == null) {
            sendResponse(t, 400, errorJson("rejectedBy is required"));
            return;
        }

        int rejectedBy = ((Double) map.get("rejectedBy")).intValue();
        String comment = map.get("comment") != null ? (String) map.get("comment") : "Rejected";

        boolean success = GatePassDAO.rejectRequest(passId, rejectedBy, comment);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Gate pass rejected\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to reject gate pass"));
        }
    }
}
