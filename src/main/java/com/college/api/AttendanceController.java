package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.AttendanceDAO;
import com.college.models.Attendance;
import com.college.utils.JsonHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AttendanceController extends BaseController implements HttpHandler {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/attendance/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetByStudent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/attendance/stats")) {
                if ("GET".equals(method))
                    handleGetStats(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/attendance/bulk")) {
                if ("POST".equals(method))
                    handleBulk(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method))
                    handleGetByCourseAndDate(t);
                else if ("POST".equals(method))
                    handlePost(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetByCourseAndDate(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getQuery();
        int courseId = 0;
        Date date = new Date();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    if ("courseId".equals(kv[0]))
                        courseId = Integer.parseInt(kv[1]);
                    else if ("date".equals(kv[0])) {
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(kv[1]);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        List<Attendance> list = attendanceDAO.getAttendanceByCourseAndDate(courseId, date);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        int studentId = extractId(path);
        List<Attendance> list = attendanceDAO.getAttendanceByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetStats(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getQuery();
        int courseId = 0;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "courseId".equals(kv[0])) {
                    courseId = Integer.parseInt(kv[1]);
                }
            }
        }

        if (courseId <= 0) {
            sendResponse(t, 400, errorJson("Valid courseId is required"));
            return;
        }

        java.util.Map<Integer, Double> stats = attendanceDAO.getCourseAttendanceStats(courseId);
        List<Integer> lowAttendanceList = attendanceDAO.getLowAttendanceStudents(courseId, 75.0); // 75% threshold

        com.college.dao.StudentDAO studentDAO = new com.college.dao.StudentDAO();
        List<com.college.models.Student> allStudents = studentDAO.getAllStudents();
        java.util.Map<Integer, String> nameMap = new java.util.HashMap<>();
        for (com.college.models.Student s : allStudents) {
            nameMap.put(s.getId(), s.getName());
        }

        java.util.List<java.util.Map<String, Object>> statsList = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, Double> entry : stats.entrySet()) {
            java.util.Map<String, Object> statObj = new java.util.HashMap<>();
            int sId = entry.getKey();
            statObj.put("studentId", sId);
            statObj.put("studentName", nameMap.getOrDefault(sId, "Unknown"));
            statObj.put("percentage", entry.getValue());
            statObj.put("isLow", lowAttendanceList.contains(sId));
            statsList.add(statObj);
        }

        String jsonStats = gson.toJson(statsList);
        String response = String.format("{\"stats\": %s}", jsonStats);

        sendResponse(t, 200, response);
    }

    private void handlePost(HttpExchange t) throws IOException {
        String body = readBody(t);
        Attendance attendance = gson.fromJson(body, Attendance.class);
        if (attendance == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = attendanceDAO.markAttendance(attendance);
        if (ok)
            sendResponse(t, 201, "{\"status\":\"Attendance marked\"}");
        else
            sendResponse(t, 400, errorJson("Failed to mark attendance"));
    }

    private void handleBulk(HttpExchange t) throws IOException {
        String body = readBody(t);
        Type listType = new TypeToken<List<Attendance>>() {
        }.getType();
        List<Attendance> list = gson.fromJson(body, listType);
        if (list == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        int count = attendanceDAO.markBulkAttendance(list);
        sendResponse(t, 200, "{\"marked\":" + count + "}");
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
