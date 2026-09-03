package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EventDetailsDAO;
import com.college.models.EventCollaborator;
import com.college.models.EventResource;
import com.college.models.EventVolunteer;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for the event detail management features (collaborators, event resources
 * and volunteers) that were previously only available in the JavaFX app.
 */
public class EventDetailsController extends BaseController implements HttpHandler {

    private final EventDetailsDAO eventDetailsDAO;

    public EventDetailsController() {
        this.eventDetailsDAO = new EventDetailsDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/event-details/volunteers/student/[^/]+") && "GET".equals(method)) {
                handleGetVolunteersByStudent(t, path);
            } else if (path.matches(".*/event-details/volunteers/\\d+") && "PUT".equals(method)) {
                handleUpdateVolunteer(t, path);
            } else if (path.matches(".*/event-details/resources/\\d+")) {
                if ("PUT".equals(method))
                    handleUpdateResourceStatus(t, path);
                else if ("DELETE".equals(method))
                    handleDeleteResource(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/event-details/collaborators/\\d+") && "DELETE".equals(method)) {
                handleDeleteCollaborator(t, path);
            } else if (path.matches(".*/event-details/\\d+/collaborators")) {
                if ("GET".equals(method))
                    handleGetCollaborators(t, path);
                else if ("POST".equals(method))
                    handleAddCollaborator(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/event-details/\\d+/resources")) {
                if ("GET".equals(method))
                    handleGetResources(t, path);
                else if ("POST".equals(method))
                    handleAddResource(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/event-details/\\d+/volunteers")) {
                if ("GET".equals(method))
                    handleGetVolunteers(t, path);
                else if ("POST".equals(method))
                    handleRegisterVolunteer(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private int extractEventId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("event-details".equals(parts[i])) {
                return Integer.parseInt(parts[i + 1]);
            }
        }
        throw new IllegalArgumentException("Unable to find event id in path");
    }

    private int extractLastId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    // --- Collaborators ---
    private void handleGetCollaborators(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_COLLABORATOR"))
            return;
        int eventId = extractEventId(path);
        List<EventCollaborator> list = eventDetailsDAO.getCollaborators(eventId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleAddCollaborator(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "CREATE_COLLABORATOR"))
            return;
        int eventId = extractEventId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("departmentId") == null) {
            sendResponse(t, 400, errorJson("departmentId is required"));
            return;
        }
        int deptId = ((Number) map.get("departmentId")).intValue();
        boolean ok = eventDetailsDAO.addCollaborator(eventId, deptId);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Collaborator added\"}");
        else
            sendResponse(t, 400, errorJson("Failed to add collaborator"));
    }

    private void handleDeleteCollaborator(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_COLLABORATOR"))
            return;
        int id = extractLastId(path);
        boolean ok = eventDetailsDAO.deleteCollaborator(id);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Collaborator removed\"}");
        else
            sendResponse(t, 400, errorJson("Failed to remove collaborator"));
    }

    // --- Resources ---
    private void handleGetResources(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_EVENT_RESOURCE"))
            return;
        int eventId = extractEventId(path);
        List<EventResource> list = eventDetailsDAO.getResources(eventId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleAddResource(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "CREATE_EVENT_RESOURCE"))
            return;
        int eventId = extractEventId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("resourceName") == null) {
            sendResponse(t, 400, errorJson("resourceName is required"));
            return;
        }
        EventResource res = new EventResource();
        res.setEventId(eventId);
        res.setResourceName((String) map.get("resourceName"));
        res.setQuantity(map.get("quantity") != null ? ((Number) map.get("quantity")).intValue() : 1);
        boolean ok = eventDetailsDAO.addResource(res);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Resource added\"}");
        else
            sendResponse(t, 400, errorJson("Failed to add resource"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateResourceStatus(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_EVENT_RESOURCE"))
            return;
        int id = extractLastId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        String status = map != null ? (String) map.get("status") : null;
        if (status == null || status.isEmpty()) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        boolean ok = eventDetailsDAO.updateResourceStatus(id, status);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Resource status updated\"}");
        else
            sendResponse(t, 400, errorJson("Failed to update resource status"));
    }

    private void handleDeleteResource(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_EVENT_RESOURCE"))
            return;
        int id = extractLastId(path);
        boolean ok = eventDetailsDAO.deleteResource(id);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Resource removed\"}");
        else
            sendResponse(t, 400, errorJson("Failed to remove resource"));
    }

    // --- Volunteers ---
    private void handleGetVolunteers(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_VOLUNTEER"))
            return;
        int eventId = extractEventId(path);
        List<EventVolunteer> list = eventDetailsDAO.getVolunteers(eventId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetVolunteersByStudent(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_VOLUNTEER"))
            return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 1]);
        List<EventVolunteer> list = eventDetailsDAO.getVolunteersByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleRegisterVolunteer(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "REGISTER_VOLUNTEER", "MANAGE_VOLUNTEER"))
            return;
        int eventId = extractEventId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null)) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
            return;
        }
        int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
            return;
        }
        String task = map.get("task") != null ? (String) map.get("task") : "";
        if (task.isEmpty()) {
            sendResponse(t, 400, errorJson("task is required"));
            return;
        }
        boolean ok = eventDetailsDAO.registerVolunteer(eventId, studentId, task);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Volunteer registered\"}");
        else
            sendResponse(t, 400, errorJson("Failed to register volunteer"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateVolunteer(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "UPDATE_VOLUNTEER", "MANAGE_VOLUNTEER"))
            return;
        int id = extractLastId(path);
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        String task = map.get("task") != null ? (String) map.get("task") : "";
        String status = map.get("status") != null ? (String) map.get("status") : "";
        boolean ok = eventDetailsDAO.updateVolunteerTask(id, task, status);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Volunteer task updated\"}");
        else
            sendResponse(t, 400, errorJson("Failed to update volunteer task"));
    }
}
