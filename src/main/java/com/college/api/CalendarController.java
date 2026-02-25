package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CalendarDAO;
import com.college.models.CalendarEvent;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public class CalendarController extends BaseController implements HttpHandler {

    private final CalendarDAO calendarDAO = new CalendarDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/calendar/events/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteEvent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/calendar/month/\\d+/\\d+")) { // year/month
                if ("GET".equals(method))
                    handleGetMonthEvents(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/calendar/events")) {
                if ("POST".equals(method))
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

    private void handleGetMonthEvents(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int month = Integer.parseInt(parts[parts.length - 1]);
        int year = Integer.parseInt(parts[parts.length - 2]);

        List<CalendarEvent> events = calendarDAO.getEventsByMonth(year, month);
        sendResponse(t, 200, JsonHelper.toJson(events));
    }

    @SuppressWarnings("unchecked")
    private void handleAddEvent(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || map.get("title") == null || map.get("eventDate") == null) {
            sendResponse(t, 400, errorJson("Missing required fields"));
            return;
        }

        try {
            CalendarEvent event = new CalendarEvent();
            event.setTitle((String) map.get("title"));
            event.setEventDate(LocalDate.parse((String) map.get("eventDate")));
            event.setEventType(CalendarEvent.EventType.valueOf((String) map.get("eventType")));
            event.setDescription((String) map.get("description"));

            boolean success = calendarDAO.addEvent(event);
            if (success) {
                sendResponse(t, 201, "{\"message\":\"Event created successfully\"}");
            } else {
                sendResponse(t, 400, errorJson("Failed to create event"));
            }
        } catch (IllegalArgumentException e) {
            sendResponse(t, 400, errorJson("Invalid format for date or event type"));
        }
    }

    private void handleDeleteEvent(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);

        boolean success = calendarDAO.deleteEvent(id);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Event deleted successfully\"}");
        } else {
            sendResponse(t, 404, errorJson("Event not found or delete failed"));
        }
    }
}
