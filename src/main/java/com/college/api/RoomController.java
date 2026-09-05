package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.AcademicRoomDAO;
import com.college.dao.TimetableDAO;
import com.college.models.AcademicRoom;
import com.college.models.Timetable;
import com.college.utils.JsonHelper;
import com.college.utils.TimeSlotUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomController extends BaseController implements HttpHandler {

    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final AcademicRoomDAO roomDAO = new AcademicRoomDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/rooms/availability")) {
                if ("GET".equals(method)) handleCheckAvailability(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/rooms/free-slots")) {
                if ("GET".equals(method)) handleFreeSlots(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/rooms/day-grid")) {
                if ("GET".equals(method)) handleDayGrid(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/rooms")) {
                if ("GET".equals(method)) handleGetAllRooms(t);
                else if ("POST".equals(method)) handleCreateRoom(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/rooms/\\d+")) {
                if ("PUT".equals(method)) handleUpdateRoom(t, path);
                else if ("DELETE".equals(method)) handleDeleteRoom(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private List<AcademicRoom> inventory() {
        List<AcademicRoom> rooms = roomDAO.findActive();
        if (!rooms.isEmpty()) {
            return rooms;
        }
        // Fallback for DBs where V67 has not run yet: derive from timetable strings
        List<AcademicRoom> fallback = new ArrayList<>();
        for (String num : timetableDAO.getAllRooms()) {
            AcademicRoom r = new AcademicRoom();
            r.setRoomNumber(num);
            r.setType(num != null && num.startsWith("Lab") ? "LABORATORY" : "CLASSROOM");
            r.setCapacity(40);
            r.setStatus("ACTIVE");
            fallback.add(r);
        }
        return fallback;
    }

    private List<AcademicRoom> applyFilters(List<AcademicRoom> rooms, Map<String, String> q) {
        String type = q.getOrDefault("type", "All");
        String building = q.getOrDefault("building", "");
        int minCapacity = getIntParam(q, "minCapacity", 0);
        List<AcademicRoom> out = new ArrayList<>();
        for (AcademicRoom r : rooms) {
            if (!"All".equalsIgnoreCase(type) && (r.getType() == null || !r.getType().equalsIgnoreCase(type))) {
                continue;
            }
            if (!building.isBlank() && (r.getBuilding() == null
                    || !r.getBuilding().toLowerCase().contains(building.toLowerCase().trim()))) {
                continue;
            }
            if (minCapacity > 0 && r.getCapacity() < minCapacity) {
                continue;
            }
            out.add(r);
        }
        return out;
    }

    private void handleGetAllRooms(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ROOM")) return;
        Map<String, String> q = getQueryMap(t);
        List<AcademicRoom> rooms = applyFilters(inventory(), q);
        sendResponse(t, 200, JsonHelper.toJson(rooms));
    }

    private void handleCheckAvailability(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ROOM")) return;
        Map<String, String> q = getQueryMap(t);
        String day = TimeSlotUtil.normalizeDay(q.getOrDefault("day", ""));
        String timeSlot = q.getOrDefault("timeSlot", "").trim();
        if (day.isEmpty() || timeSlot.isEmpty()) {
            sendResponse(t, 400, errorJson("Missing day or timeSlot parameter"));
            return;
        }
        if (TimeSlotUtil.parse(timeSlot) == null) {
            sendResponse(t, 400, errorJson("Unrecognized timeSlot format: " + timeSlot));
            return;
        }

        List<AcademicRoom> rooms = applyFilters(inventory(), q);
        List<Map<String, Object>> availability = new ArrayList<>();
        for (AcademicRoom room : rooms) {
            Timetable occupant = timetableDAO.findOccupant(room.getRoomNumber(), day, timeSlot, -1);
            Map<String, Object> map = new HashMap<>();
            map.put("roomNumber", room.getRoomNumber());
            map.put("type", room.getType());
            map.put("capacity", room.getCapacity());
            map.put("building", room.getBuilding());
            map.put("isAvailable", occupant == null);
            if (occupant != null) {
                Map<String, Object> occ = new HashMap<>();
                occ.put("subject", occupant.getSubject());
                occ.put("facultyName", occupant.getFacultyName());
                occ.put("department", occupant.getDepartment());
                occ.put("semester", occupant.getSemester());
                occ.put("timeSlot", occupant.getTimeSlot());
                map.put("occupiedBy", occ);
                map.put("course", occupant.getSubject() + " (" + occupant.getDepartment() + ")");
            }
            availability.add(map);
        }
        sendResponse(t, 200, JsonHelper.toJson(availability));
    }

    private void handleFreeSlots(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ROOM")) return;
        Map<String, String> q = getQueryMap(t);
        String day = TimeSlotUtil.normalizeDay(q.getOrDefault("day", ""));
        String roomNumber = q.getOrDefault("roomNumber", "").trim();
        if (day.isEmpty() || roomNumber.isEmpty()) {
            sendResponse(t, 400, errorJson("Missing day or roomNumber parameter"));
            return;
        }
        List<String> free = new ArrayList<>();
        for (String slot : TimeSlotUtil.CANONICAL_SLOTS) {
            if (!timetableDAO.isRoomOccupied(roomNumber, day, slot, -1)) {
                free.add(slot);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("day", day);
        resp.put("roomNumber", roomNumber);
        resp.put("freeSlots", free);
        sendResponse(t, 200, JsonHelper.toJson(resp));
    }

    private void handleDayGrid(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ROOM")) return;
        Map<String, String> q = getQueryMap(t);
        String day = TimeSlotUtil.normalizeDay(q.getOrDefault("day", ""));
        if (day.isEmpty()) {
            sendResponse(t, 400, errorJson("Missing day parameter"));
            return;
        }
        List<AcademicRoom> rooms = applyFilters(inventory(), q);
        List<String> slots = TimeSlotUtil.CANONICAL_SLOTS;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AcademicRoom room : rooms) {
            Map<String, Object> row = new HashMap<>();
            row.put("roomNumber", room.getRoomNumber());
            row.put("type", room.getType());
            row.put("capacity", room.getCapacity());
            row.put("building", room.getBuilding());
            Map<String, Object> cells = new HashMap<>();
            for (String slot : slots) {
                Timetable occupant = timetableDAO.findOccupant(room.getRoomNumber(), day, slot, -1);
                if (occupant == null) {
                    cells.put(slot, null);
                } else {
                    Map<String, Object> occ = new HashMap<>();
                    occ.put("subject", occupant.getSubject());
                    occ.put("facultyName", occupant.getFacultyName());
                    occ.put("department", occupant.getDepartment());
                    cells.put(slot, occ);
                }
            }
            row.put("slots", cells);
            rows.add(row);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("day", day);
        resp.put("slots", slots);
        resp.put("rooms", rows);
        sendResponse(t, 200, JsonHelper.toJson(resp));
    }

    private void handleCreateRoom(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "MANAGE_ROOM", "MANAGE_ROOMS")) return;
        AcademicRoom room = JsonHelper.fromJson(readBody(t), AcademicRoom.class);
        if (room == null || room.getRoomNumber() == null || room.getRoomNumber().isBlank()) {
            sendResponse(t, 400, errorJson("roomNumber is required"));
            return;
        }
        room.setRoomNumber(room.getRoomNumber().trim());
        if (roomDAO.create(room)) {
            AcademicRoom created = roomDAO.findByNumber(room.getRoomNumber());
            sendResponse(t, 201, JsonHelper.toJson(created != null ? created : room));
        } else {
            sendResponse(t, 400, errorJson("Failed to create room (duplicate room number?)"));
        }
    }

    private void handleUpdateRoom(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "MANAGE_ROOM", "MANAGE_ROOMS")) return;
        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        AcademicRoom room = JsonHelper.fromJson(readBody(t), AcademicRoom.class);
        if (room == null || room.getRoomNumber() == null || room.getRoomNumber().isBlank()) {
            sendResponse(t, 400, errorJson("roomNumber is required"));
            return;
        }
        room.setRoomNumber(room.getRoomNumber().trim());
        if (roomDAO.update(id, room)) {
            sendResponse(t, 200, JsonHelper.toJson(room));
        } else {
            sendResponse(t, 400, errorJson("Failed to update room"));
        }
    }

    private void handleDeleteRoom(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "MANAGE_ROOM", "MANAGE_ROOMS")) return;
        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        if (roomDAO.delete(id)) {
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to delete room"));
        }
    }
}
