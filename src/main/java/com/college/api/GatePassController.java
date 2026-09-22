package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.GatePassDAO;
import com.college.models.GatePass;
import com.college.utils.JsonHelper;
import com.college.utils.Logger;

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
            if (path.matches(".*/gatepass/student/[^/]+")) {
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
            Logger.error("Gate pass API error", e);
            sendResponse(t, 500, errorJson("Internal server error"));
        }
    }

private void handleGetStudentPasses(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GATEPASS")) return;
        String[] parts = path.split("/");
        int studentId = scopeStudentAccess(t, parts[parts.length - 1]);
        if (studentId < 0) return;
        List<GatePass> passes = GatePassDAO.getStudentPasses(studentId);
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

private void handleGetPendingPasses(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_GATEPASS")) return;
        List<GatePass> passes = GatePassDAO.getPendingPasses();
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

    private void handleGetAllPasses(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_GATEPASS")) return;
        List<GatePass> passes = GatePassDAO.getAllPasses();
        sendResponse(t, 200, JsonHelper.toJson(passes));
    }

private void handleCreateRequest(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "CREATE_GATEPASS", "REQUEST_GATE_PASS")) return;
        String body = readBody(t);
        GatePass gatePass = JsonHelper.fromJson(body, GatePass.class);

        if (gatePass == null) {
            sendResponse(t, 400, errorJson("Invalid payload"));
            return;
        }

        // Date validation — no past departures, return date on/after departure.
        if (gatePass.getFromDate() == null) {
            sendResponse(t, 400, errorJson("From date is required"));
            return;
        }
        java.time.LocalDate from = gatePass.getFromDate();
        if (from.isBefore(java.time.LocalDate.now())) {
            sendResponse(t, 400, errorJson("From date cannot be in the past"));
            return;
        }
        if (gatePass.getToDate() != null && gatePass.getToDate().isBefore(from)) {
            sendResponse(t, 400, errorJson("Return date must be on or after departure date"));
            return;
        }

        // Students can only ever request a pass for their own account: the
        // enrollmentId supplied in the body is ignored and replaced with the
        // caller's own students.id (IDOR protection). Staff/warden creating on
        // behalf of a student keep the body-driven enrollmentId.
        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        if (tokenInfo != null && "STUDENT".equalsIgnoreCase(tokenInfo.role)) {
            int self = new com.college.dao.StudentDAO().getStudentIdByUserId(tokenInfo.userId);
            if (self <= 0) {
                sendResponse(t, 403, errorJson("Forbidden: No student profile linked to this account"));
                return;
            }
            gatePass.setStudentId(self);
        } else if (gatePass.getEnrollmentId() != null && !gatePass.getEnrollmentId().trim().isEmpty()) {
            int studentId = new com.college.dao.StudentDAO().getStudentIdByEnrollment(gatePass.getEnrollmentId().trim());
            if (studentId <= 0) {
                sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
                return;
            }
            gatePass.setStudentId(studentId);
        }
        if (gatePass.getStudentId() == 0) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
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
        if (!requirePermission(t, "MANAGE_GATEPASS")) return;
        String[] parts = path.split("/");
        int passId = Integer.parseInt(parts[parts.length - 2]); // .../gatepass/{id}/approve

        String comment = "Approved";
        String body = readBody(t);
        if (body != null && !body.isBlank()) {
            Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
            if (map != null && map.get("comment") != null) {
                comment = (String) map.get("comment");
            }
        }

        int approvedBy = actorUserId(t);
        boolean success = GatePassDAO.approveRequest(passId, approvedBy, comment);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Gate pass approved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to approve gate pass"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRejectPass(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_GATEPASS")) return;
        String[] parts = path.split("/");
        int passId = Integer.parseInt(parts[parts.length - 2]); // .../gatepass/{id}/reject

        String comment = "Rejected";
        String body = readBody(t);
        if (body != null && !body.isBlank()) {
            Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
            if (map != null && map.get("comment") != null) {
                comment = (String) map.get("comment");
            }
        }

        int rejectedBy = actorUserId(t);
        boolean success = GatePassDAO.rejectRequest(passId, rejectedBy, comment);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Gate pass rejected\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to reject gate pass"));
        }
    }

    /**
     * Resolve the acting users.id from the bearer token. The caller must already
     * have passed MANAGE_GATEPASS, so the token is guaranteed present.
     */
    private int actorUserId(HttpExchange t) {
        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        return tokenInfo != null ? tokenInfo.userId : 0;
    }
}
