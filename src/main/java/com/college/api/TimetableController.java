package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.TimetableDAO;
import com.college.models.Timetable;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class TimetableController extends BaseController implements HttpHandler {

    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/timetable/\\d+")) {
                if ("DELETE".equals(method)) handleDelete(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method)) handleGet(t);
                else if ("POST".equals(method)) handlePost(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGet(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_TIMETABLE")) return;
        String query = t.getRequestURI().getQuery();
        String department = "";
        int semester = 1;
        String specialization = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    if ("department".equals(kv[0])) department = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                    else if ("semester".equals(kv[0])) {
                        try { semester = Integer.parseInt(kv[1]); } catch (Exception ignored) {}
                    } else if ("specialization".equals(kv[0]) || "track".equals(kv[0])) {
                        specialization = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                        if (specialization.trim().isEmpty()) specialization = null;
                    }
                }
            }
        }
        List<Timetable> list = timetableDAO.getTimetableByDepartmentSemesterAndTrack(department, semester, specialization);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handlePost(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_TIMETABLE")) return;
        String body = readBody(t);
        Timetable entry = JsonHelper.fromJson(body, Timetable.class);
        if (entry == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        if (entry.getDepartment() == null || entry.getDepartment().isBlank()
                || entry.getSemester() < 1
                || entry.getDayOfWeek() == null || entry.getDayOfWeek().isBlank()
                || entry.getTimeSlot() == null || entry.getTimeSlot().isBlank()
                || entry.getSubject() == null || entry.getSubject().isBlank()) {
            sendResponse(t, 400, errorJson("Department, semester, day, time slot, and subject are required"));
            return;
        }
        boolean ok = timetableDAO.saveTimetableEntry(entry);
        if (ok) sendResponse(t, 201, JsonHelper.toJson(entry));
        else sendResponse(t, 400, errorJson("Failed to save timetable entry"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_TIMETABLE")) return;
        int id = extractId(path);
        boolean ok = timetableDAO.deleteTimetableEntry(id);
        if (ok) sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else sendResponse(t, 400, errorJson("Failed to delete timetable entry"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
