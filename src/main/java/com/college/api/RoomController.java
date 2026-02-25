package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.TimetableDAO;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomController extends BaseController implements HttpHandler {

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/rooms/availability")) {
                if ("GET".equals(method)) handleCheckAvailability(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/rooms")) {
                if ("GET".equals(method)) handleGetAllRooms(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAllRooms(HttpExchange t) throws IOException {
        List<String> rooms = timetableDAO.getAllRooms();
        
        // Convert string list to object list for frontend
        List<Map<String, String>> roomObjects = new ArrayList<>();
        for (String num : rooms) {
            Map<String, String> map = new HashMap<>();
            map.put("roomNumber", num);
            map.put("type", num.startsWith("Lab") ? "LABORATORY" : "CLASSROOM");
            roomObjects.add(map);
        }
        
        sendResponse(t, 200, JsonHelper.toJson(roomObjects));
    }

    private void handleCheckAvailability(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getQuery();
        if (query == null || !query.contains("day=") || !query.contains("timeSlot=")) {
            sendResponse(t, 400, errorJson("Missing day or timeSlot parameter"));
            return;
        }

        String day = "";
        String timeSlot = "";
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("day=")) day = param.split("=")[1];
            if (param.startsWith("timeSlot=")) timeSlot = param.split("=")[1];
        }

        // Decode URL params
        day = java.net.URLDecoder.decode(day, "UTF-8");
        timeSlot = java.net.URLDecoder.decode(timeSlot, "UTF-8");

        List<String> allRooms = timetableDAO.getAllRooms();
        List<String> occupiedRooms = timetableDAO.getOccupiedRooms(day, timeSlot);

        List<Map<String, Object>> availability = new ArrayList<>();
        
        for (String room : allRooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomNumber", room);
            map.put("isAvailable", !occupiedRooms.contains(room));
            map.put("type", room.startsWith("Lab") ? "LABORATORY" : "CLASSROOM");
            availability.add(map);
        }

        sendResponse(t, 200, JsonHelper.toJson(availability));
    }
}
