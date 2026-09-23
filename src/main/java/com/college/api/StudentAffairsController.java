package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.StudentAffairsDAO;
import com.college.models.DisciplinaryIncident;
import com.college.models.GrievanceTicket;
import com.college.models.ParentCommunication;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for the Student Affairs admin module (web Student Affairs page).
 *
 * Endpoints (all REQUIRE_PREFIX-stripped, i.e. served under /api/affairs):
 *   GET/POST /disciplinary
 *   PUT/DELETE /disciplinary/{id}
 *   GET/POST /grievances
 *   PUT /grievances/{id}/status
 *   GET/POST /communications
 *
 * Everything is gated on MANAGE_STUDENTS, which administrators and wardens
 * hold. The old frontend file expected these endpoints under /api/affairs/*
 * but they were never registered, so the requests silently fell through to
 * the root handler and the page crashed with "filter is not a function".
 */
public class StudentAffairsController extends BaseController implements HttpHandler {

    private final StudentAffairsDAO dao;

    public StudentAffairsController() {
        this.dao = new StudentAffairsDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/disciplinary") && "GET".equals(method)) {
                handleGetIncidents(t);
            } else if (path.endsWith("/disciplinary") && "POST".equals(method)) {
                handleCreateIncident(t);
            } else if (path.matches(".*/disciplinary/\\d+") && "PUT".equals(method)) {
                handleUpdateIncident(t, path);
            } else if (path.matches(".*/disciplinary/\\d+") && "DELETE".equals(method)) {
                handleDeleteIncident(t, path);
            } else if (path.endsWith("/grievances") && "GET".equals(method)) {
                handleGetGrievances(t);
            } else if (path.endsWith("/grievances") && "POST".equals(method)) {
                handleCreateGrievance(t);
            } else if (path.matches(".*/grievances/\\d+/status") && "PUT".equals(method)) {
                handleUpdateGrievanceStatus(t, path);
            } else if (path.endsWith("/communications") && "GET".equals(method)) {
                handleGetCommunications(t);
            } else if (path.endsWith("/communications") && "POST".equals(method)) {
                handleCreateCommunication(t);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetIncidents(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        List<DisciplinaryIncident> list = dao.getAllIncidents();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateIncident(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null)) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
            return;
        }
        int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Unknown student for the given studentId/enrollmentId"));
            return;
        }
        DisciplinaryIncident inc = new DisciplinaryIncident();
        inc.setStudentId(studentId);
        inc.setType(String.valueOf(map.getOrDefault("type", "Misconduct")));
        inc.setAction((String) map.get("action"));
        inc.setSeverity((String) map.getOrDefault("severity", "Low"));
        inc.setStatus((String) map.getOrDefault("status", "Under Review"));
        inc.setDate((String) map.get("date"));
        int reportedBy = getTokenInfo(t) != null ? getTokenInfo(t).userId : 0;

        boolean ok = dao.createIncident(inc, reportedBy);
        sendResponse(t, ok ? 201 : 400, ok ? "{\"status\":\"Incident recorded\"}" : errorJson("Failed to record incident"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateIncident(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        Map<String, Object> map = JSON.fromJson(readBody(t), Map.class);
        String status = map != null ? (String) map.get("status") : null;
        String action = map != null ? (String) map.get("action") : null;
        if (status == null && action == null) {
            sendResponse(t, 400, errorJson("status or action is required"));
            return;
        }
        boolean ok = dao.updateIncident(id, status, action);
        String msg = ok ? "{\"status\":\"Incident updated\"}" : errorJson("Failed to update incident");
        sendResponse(t, ok ? 200 : 400, msg);
    }

    private void handleDeleteIncident(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        boolean ok = dao.deleteIncident(id);
        String msg = ok ? "{\"status\":\"Incident deleted\"}" : errorJson("Failed to delete incident");
        sendResponse(t, ok ? 200 : 400, msg);
    }

    private void handleGetGrievances(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        List<GrievanceTicket> list = dao.getAllGrievances();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateGrievance(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("title") == null) {
            sendResponse(t, 400, errorJson("title is required"));
            return;
        }
        GrievanceTicket ticket = new GrievanceTicket();
        if (map.get("studentId") != null || map.get("enrollmentId") != null) {
            int sid = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
            ticket.setStudentId(sid > 0 ? sid : null);
        }
        ticket.setCategory(String.valueOf(map.getOrDefault("category", "Infrastructure")));
        ticket.setTitle((String) map.get("title"));
        ticket.setDescription((String) map.get("description"));
        ticket.setReporter((String) map.getOrDefault("reporter", "Anonymous"));
        ticket.setPriority(String.valueOf(map.getOrDefault("priority", "Medium")));

        boolean ok = dao.createGrievance(ticket);
        String msg = ok ? "{\"status\":\"Grievance filed\"}" : errorJson("Failed to file grievance");
        sendResponse(t, ok ? 201 : 400, msg);
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateGrievanceStatus(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 2]);
        Map<String, Object> map = JSON.fromJson(readBody(t), Map.class);
        String status = map != null ? (String) map.get("status") : null;
        if (status == null || !java.util.Set.of("Open", "In Progress", "Under Review", "Resolved", "Rejected").contains(status)) {
            sendResponse(t, 400, errorJson("Invalid status (expected Open, In Progress, Under Review, Resolved or Rejected)"));
            return;
        }
        boolean ok = dao.updateGrievanceStatus(id, status);
        String msg = ok ? "{\"status\":\"Grievance updated\"}" : errorJson("Failed to update grievance");
        sendResponse(t, ok ? 200 : 400, msg);
    }

    private void handleGetCommunications(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        List<ParentCommunication> list = dao.getAllCommunications();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateCommunication(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_STUDENTS"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("subject") == null || map.get("message") == null) {
            sendResponse(t, 400, errorJson("subject and message are required"));
            return;
        }
        ParentCommunication comm = new ParentCommunication();
        comm.setSubject((String) map.get("subject"));
        comm.setRecipient((String) map.getOrDefault("recipient", "All Parents"));
        comm.setChannel(String.valueOf(map.getOrDefault("channel", "Email")));
        comm.setMessage((String) map.get("message"));
        int sentBy = getTokenInfo(t) != null ? getTokenInfo(t).userId : 0;

        boolean ok = dao.createCommunication(comm, sentBy);
        String msg = ok ? "{\"status\":\"Communication dispatched\"}" : errorJson("Failed to dispatch communication");
        sendResponse(t, ok ? 201 : 400, msg);
    }
}