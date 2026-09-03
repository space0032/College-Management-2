package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.HostelAttendanceDAO;
import com.college.models.HostelAttendance;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * API for hostel attendance. Backs the hostel attendance feature that was
 * previously only available in the JavaFX app.
 */
public class HostelAttendanceController extends BaseController implements HttpHandler {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    private final HostelAttendanceDAO attendanceDAO;

    public HostelAttendanceController() {
        this.attendanceDAO = new HostelAttendanceDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/hostel/attendance/mark") && "POST".equals(method)) {
                handleMark(t);
            } else if (path.matches(".*/hostel/attendance/date/.+") && "GET".equals(method)) {
                handleGetByDate(t, path);
            } else if (path.matches(".*/hostel/attendance/student/\\d+") && "GET".equals(method)) {
                handleGetByStudent(t, path);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMark(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL_ATTENDANCE"))
            return;
        if (!requirePermission(t, "MANAGE_HOSTEL"))
            return;
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

        HostelAttendance ha = new HostelAttendance();
        ha.setStudentId(studentId);
        ha.setHostelId(map.get("hostelId") != null ? ((Number) map.get("hostelId")).intValue() : 0);
        ha.setDate(parseDate((String) map.getOrDefault("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))));
        ha.setStatus((String) map.getOrDefault("status", "PRESENT"));
        ha.setRemarks((String) map.getOrDefault("remarks", ""));
        ha.setMarkedBy(getTokenInfo(t) != null ? getTokenInfo(t).userId : 0);

        boolean ok = attendanceDAO.markAttendance(ha);
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Attendance marked\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to mark attendance"));
        }
    }

    private void handleGetByDate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL_ATTENDANCE"))
            return;
        String dateStr = path.substring(path.lastIndexOf('/') + 1);
        Date date = parseDate(dateStr);
        if (date == null) {
            sendResponse(t, 400, errorJson("Invalid date, expected yyyy-MM-dd"));
            return;
        }
        List<HostelAttendance> list = attendanceDAO.getAttendanceByDate(date);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL_ATTENDANCE"))
            return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 1]);
        List<HostelAttendance> list = attendanceDAO.getAttendanceByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private Date parseDate(String dateStr) {
        try {
            return DATE_FMT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
