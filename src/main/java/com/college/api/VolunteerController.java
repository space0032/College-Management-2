package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EventDetailsDAO;
import com.college.dao.EventDAO;
import com.college.dao.StudentDAO;
import com.college.models.Event;
import com.college.models.EventVolunteer;
import com.college.models.Student;
import com.college.utils.JsonHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VolunteerController extends BaseController implements HttpHandler {

    private final EventDetailsDAO eventDetailsDAO = new EventDetailsDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String query = t.getRequestURI().getQuery();

        try {
            if (path.equals("/api/volunteers/my-tasks")) {
                if ("GET".equals(method))
                    handleGetMyTasks(t, query);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/volunteers/opportunities")) {
                if ("GET".equals(method))
                    handleGetOpportunities(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/volunteers/apply")) {
                if ("POST".equals(method))
                    handleApply(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetMyTasks(HttpExchange t, String query) throws IOException {
        if (!requirePermission(t, "VIEW_VOLUNTEER")) return;
        if (query == null || !query.contains("userId=")) {
            sendResponse(t, 400, errorJson("Missing userId parameter"));
            return;
        }

        int userId = Integer.parseInt(query.split("userId=")[1].split("&")[0]);
        Student student = studentDAO.getStudentByUserId(userId);

        if (student == null) {
            sendResponse(t, 200, "[]");
            return;
        }

        List<EventVolunteer> tasks = eventDetailsDAO.getVolunteersByStudent(student.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (EventVolunteer ev : tasks) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ev.getId());
            map.put("eventId", ev.getEventId());
            map.put("eventName", ev.getEventName());
            map.put("taskDescription", ev.getTaskDescription());
            map.put("status", ev.getStatus());
            map.put("hoursLogged", ev.getHoursLogged());
            result.add(map);
        }

        sendResponse(t, 200, JsonHelper.toJson(result));
    }

    private void handleGetOpportunities(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_VOLUNTEER")) return;
        List<Event> allEvents = eventDAO.getAllEvents();
        List<Event> upcoming = allEvents.stream()
                .filter(e -> "UPCOMING".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Event e : upcoming) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("name", e.getName());
            map.put("eventType", e.getEventType());
            map.put("status", e.getStatus());
            map.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : null);
            map.put("location", e.getLocation());
            result.add(map);
        }

        sendResponse(t, 200, JsonHelper.toJson(result));
    }

    @SuppressWarnings("unchecked")
    private void handleApply(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_VOLUNTEER")) return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);

        if (!req.containsKey("userId") || !req.containsKey("eventId") || !req.containsKey("taskDescription")) {
            sendResponse(t, 400, errorJson("Missing required fields: userId, eventId, taskDescription"));
            return;
        }

        int userId = ((Double) req.get("userId")).intValue();
        int eventId = ((Double) req.get("eventId")).intValue();
        String task = (String) req.get("taskDescription");

        Student student = studentDAO.getStudentByUserId(userId);
        if (student == null) {
            sendResponse(t, 400, errorJson("No student profile found for this user"));
            return;
        }

        if (eventDetailsDAO.isVolunteer(eventId, student.getId())) {
            sendResponse(t, 409, errorJson("Already registered as volunteer for this event"));
            return;
        }

        boolean ok = eventDetailsDAO.registerVolunteer(eventId, student.getId(), task);
        if (ok)
            sendResponse(t, 201, "{\"success\":true,\"message\":\"Volunteer application submitted successfully\"}");
        else
            sendResponse(t, 500, errorJson("Failed to submit volunteer application"));
    }
}
