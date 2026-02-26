package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EventDAO;
import com.college.models.Event;
import com.college.models.EventRegistration;
import com.college.models.EventBudget;
import com.college.models.EventPoll;
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
            } else if (path.matches(".*/events/\\d+/budget")) {
                if ("GET".equals(method))
                    handleGetEventBudgets(t, path);
                else if ("POST".equals(method))
                    handleAddEventBudget(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/budget/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteEventBudget(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/\\d+/polls")) {
                if ("GET".equals(method))
                    handleGetEventPolls(t, path);
                else if ("POST".equals(method))
                    handleCreateEventPoll(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/events/polls/\\d+/close")) {
                if ("PUT".equals(method))
                    handleCloseEventPoll(t, path);
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
        if (!requirePermission(t, "VIEW_EVENT"))
            return;
        List<Event> events = eventDAO.getAllEvents();
        sendResponse(t, 200, JsonHelper.toJson(events));
    }

    private void handleGetEvent(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_EVENT"))
            return;
        int id = extractId(path);
        Event event = eventDAO.getEventById(id);
        if (event == null) {
            sendResponse(t, 404, errorJson("Event not found"));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(event));
    }

    private void handleAddEvent(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_EVENT"))
            return;
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
        if (!requirePermission(t, "UPDATE_EVENT"))
            return;
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
        if (!requirePermission(t, "DELETE_EVENT"))
            return;
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
        if (!requirePermission(t, "REGISTER_EVENT"))
            return;
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
        if (!requirePermission(t, "UNREGISTER_EVENT"))
            return;
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
        if (!requirePermission(t, "VIEW_EVENT"))
            return;
        String[] parts = path.split("/");
        int eventId = Integer.parseInt(parts[parts.length - 2]); // .../events/{id}/registrations
        List<EventRegistration> registrations = eventDAO.getEventRegistrations(eventId);
        sendResponse(t, 200, JsonHelper.toJson(registrations));
    }

    @SuppressWarnings("unchecked")
    private void handleMarkAttendance(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MARK_ATTENDANCE"))
            return;
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
        if (!requirePermission(t, "VIEW_EVENT"))
            return;
        int studentId = extractId(path); // .../events/student/{id}
        List<Event> events = eventDAO.getStudentRegisteredEvents(studentId);
        sendResponse(t, 200, JsonHelper.toJson(events));
    }

    private void handleGetEventBudgets(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_BUDGET"))
            return;
        int eventId = extractId(path);
        List<EventBudget> budgets = eventDAO.getEventBudgets(eventId);
        sendResponse(t, 200, JsonHelper.toJson(budgets));
    }

    private void handleAddEventBudget(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "CREATE_BUDGET"))
            return;
        int eventId = extractId(path);
        String body = readBody(t);
        EventBudget budget = JsonHelper.fromJson(body, EventBudget.class);
        if (budget == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        budget.setEventId(eventId);
        boolean success = eventDAO.addBudget(budget);
        if (success) {
            sendResponse(t, 201, "{\"message\":\"Budget item added\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to add budget item"));
        }
    }

    private void handleDeleteEventBudget(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_BUDGET"))
            return;
        int id = extractId(path);
        boolean success = eventDAO.deleteBudget(id);
        if (success) {
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to delete budget item"));
        }
    }

    private void handleGetEventPolls(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_POLL"))
            return;
        int eventId = extractId(path);
        List<EventPoll> polls = eventDAO.getEventPolls(eventId);
        sendResponse(t, 200, JsonHelper.toJson(polls));
    }

    private void handleCreateEventPoll(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "CREATE_POLL"))
            return;
        int eventId = extractId(path);
        String body = readBody(t);
        EventPoll poll = JsonHelper.fromJson(body, EventPoll.class);
        if (poll == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        poll.setEventId(eventId);
        poll.setStatus("ACTIVE");
        boolean success = eventDAO.createPoll(poll);
        if (success) {
            sendResponse(t, 201, "{\"message\":\"Poll created\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create poll"));
        }
    }

    private void handleCloseEventPoll(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_POLL"))
            return;
        String[] parts = path.split("/");
        int pollId = Integer.parseInt(parts[parts.length - 2]); // .../polls/{id}/close
        boolean success = eventDAO.closePoll(pollId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Poll closed\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to close poll"));
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
