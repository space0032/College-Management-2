package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EventDAO;
import com.college.models.Event;
import com.college.models.EventRegistration;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class EventController extends BaseController implements HttpHandler {

    private final EventDAO eventDAO = new EventDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/events/\\d+/register")) {
                if ("POST".equals(method))
                    handleRegisterEvent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/\\d+/unregister")) {
                if ("POST".equals(method))
                    handleUnregisterEvent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/\\d+/registrations")) {
                if ("GET".equals(method))
                    handleGetEventRegistrations(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/registrations/\\d+/attendance")) {
                if ("PUT".equals(method))
                    handleMarkAttendance(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetStudentEvents(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/\\d+")) {
                if ("GET".equals(method))
                    handleGetEvent(t, path);
                else if ("PUT".equals(method))
                    handleUpdateEvent(t, path);
                else if ("DELETE".equals(method))
                    handleDeleteEvent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events.*")) {
                if ("GET".equals(method))
                    handleGetEvents(t);
                else if ("POST".equals(method))
                    handleAddEvent(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetEvents(HttpExchange t) throws IOException {
        List<Event> events = eventDAO.getAllEvents();
        sendResponse(t, 200, JsonHelper.toJson(events));
    }

    private void handleGetEvent(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        Event event = eventDAO.getEventById(id);
        if (event == null) {
            sendResponse(t, 404, errorJson("Event not found"));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(event));
    }

    private void handleAddEvent(HttpExchange t) throws IOException {
        String body = readBody(t);
        Event event = JsonHelper.fromJson(body, Event.class);
        if (event == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean success = eventDAO.createEvent(event);
        if (success) {
            sendResponse(t, 201, "{\"message\":\"Event created successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create event"));
        }
    }

    private void handleUpdateEvent(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        String body = readBody(t);
        Event event = JsonHelper.fromJson(body, Event.class);
        if (event == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        event.setId(id);
        boolean success = eventDAO.updateEvent(event);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Event updated successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update event"));
        }
    }

    private void handleDeleteEvent(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        boolean success = eventDAO.deleteEvent(id);
        if (success) {
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to delete event"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRegisterEvent(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int eventId = Integer.parseInt(parts[parts.length - 2]); // .../events/{id}/register
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("studentId") == null) {
            sendResponse(t, 400, errorJson("studentId is required"));
            return;
        }
        int studentId = ((Double) map.get("studentId")).intValue();

        if (eventDAO.isStudentRegistered(eventId, studentId)) {
            sendResponse(t, 400, errorJson("Already registered for this event"));
            return;
        }
        boolean success = eventDAO.registerStudent(eventId, studentId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Registered successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to register"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUnregisterEvent(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int eventId = Integer.parseInt(parts[parts.length - 2]); // .../events/{id}/unregister
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("studentId") == null) {
            sendResponse(t, 400, errorJson("studentId is required"));
            return;
        }
        int studentId = ((Double) map.get("studentId")).intValue();

        boolean success = eventDAO.unregisterStudent(eventId, studentId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Unregistered successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to unregister"));
        }
    }

    private void handleGetEventRegistrations(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int eventId = Integer.parseInt(parts[parts.length - 2]); // .../events/{id}/registrations
        List<EventRegistration> registrations = eventDAO.getEventRegistrations(eventId);
        sendResponse(t, 200, JsonHelper.toJson(registrations));
    }

    @SuppressWarnings("unchecked")
    private void handleMarkAttendance(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int registrationId = Integer.parseInt(parts[parts.length - 2]); // .../registrations/{id}/attendance
        String body = readBody(t);
        java.util.Map<String, String> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("status") == null) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        boolean success = eventDAO.markAttendance(registrationId, map.get("status"));
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Attendance updated\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update attendance"));
        }
    }

    private void handleGetStudentEvents(HttpExchange t, String path) throws IOException {
        int studentId = extractId(path); // .../events/student/{id}
        List<Event> events = eventDAO.getStudentRegisteredEvents(studentId);
        sendResponse(t, 200, JsonHelper.toJson(events));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
