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
            if (path.matches(".*/attendance/student/[^/]+")) {
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
        if (!requirePermission(t, "VIEW_ATTENDANCE")) return;
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
        if (!requirePermission(t, "VIEW_ATTENDANCE")) return;
        int studentId = resolvePathStudentId(path.substring(path.lastIndexOf('/') + 1));
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Invalid or unknown student"));
            return;
        }
        List<Attendance> list = attendanceDAO.getAttendanceByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetStats(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ATTENDANCE")) return;
        java.util.Map<String, String> params = getQueryMap(t);
        int courseId = getIntParam(params, "courseId", 0);
        double threshold = 75.0;
        try {
            if (params.containsKey("threshold")) threshold = Double.parseDouble(params.get("threshold"));
        } catch (NumberFormatException ignored) {
        }

        if (courseId <= 0) {
            sendResponse(t, 400, errorJson("Valid courseId is required"));
            return;
        }

        java.util.Map<Integer, Double> stats = attendanceDAO.getCourseAttendanceStats(courseId);
        List<Integer> lowAttendanceList = attendanceDAO.getLowAttendanceStudents(courseId, threshold); // configurable threshold

        com.college.dao.StudentDAO studentDAO = new com.college.dao.StudentDAO();
        List<com.college.models.Student> allStudents = studentDAO.getAllStudents();
        java.util.Map<Integer, String> nameMap = new java.util.HashMap<>();
        java.util.Map<Integer, String> enrollMap = new java.util.HashMap<>();
        java.util.Map<Integer, int[]> countMap = new java.util.HashMap<>();
        for (com.college.models.Student s : allStudents) {
            nameMap.put(s.getId(), s.getName());
            enrollMap.put(s.getId(), s.getEnrollmentId() != null ? s.getEnrollmentId() : s.getUsername());
        }
        // present/total counts per student for richer cards (LATE stays non-present)
        try {
            java.util.List<Attendance> all = new java.util.ArrayList<>();
            for (com.college.models.Student s : allStudents) {
                java.util.List<Attendance> recs = attendanceDAO.getAttendanceByStudent(s.getId());
                int total = 0, present = 0;
                for (Attendance a : recs) {
                    if (a.getCourseId() == courseId) {
                        total++;
                        if ("PRESENT".equalsIgnoreCase(a.getStatus())) present++;
                    }
                }
                if (total > 0) countMap.put(s.getId(), new int[]{present, total});
            }
        } catch (Exception ignored) {
        }

        java.util.List<java.util.Map<String, Object>> statsList = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, Double> entry : stats.entrySet()) {
            java.util.Map<String, Object> statObj = new java.util.HashMap<>();
            int sId = entry.getKey();
            statObj.put("studentId", sId);
            statObj.put("studentName", nameMap.getOrDefault(sId, "Unknown"));
            statObj.put("enrollmentId", enrollMap.getOrDefault(sId, null));
            statObj.put("percentage", entry.getValue());
            statObj.put("isLow", lowAttendanceList.contains(sId));
            int[] counts = countMap.get(sId);
            if (counts != null) {
                statObj.put("present", counts[0]);
                statObj.put("total", counts[1]);
            }
            statsList.add(statObj);
        }

        String jsonStats = gson.toJson(statsList);
        String response = String.format("{\"stats\": %s, \"threshold\": %s}", jsonStats, threshold);

        sendResponse(t, 200, response);
    }

    private void handlePost(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "CREATE_ATTENDANCE", "MANAGE_ATTENDANCE")) return;
        String body = readBody(t);
        Attendance attendance = gson.fromJson(body, Attendance.class);
        if (attendance == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        java.util.Map<String, Object> raw = gson.fromJson(body, java.util.Map.class);
        int studentId = resolveStudentId(raw, attendance.getStudentId());
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Invalid or unknown student / enrollmentId"));
            return;
        }
        attendance.setStudentId(studentId);
        String validationError = validateRecord(attendance);
        if (validationError != null) {
            sendResponse(t, 400, errorJson(validationError));
            return;
        }
        int markedBy = currentUserId(t);
        boolean ok = attendanceDAO.markAttendance(attendance, markedBy);
        if (ok)
            sendResponse(t, 201, "{\"status\":\"Attendance marked\"}");
        else
            sendResponse(t, 400, errorJson("Failed to mark attendance"));
    }

    private void handleBulk(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "CREATE_ATTENDANCE", "MANAGE_ATTENDANCE")) return;
        String body = readBody(t);
        Type listType = new TypeToken<List<Attendance>>() {
        }.getType();
        List<Attendance> list = gson.fromJson(body, listType);
        if (list == null || list.isEmpty()) {
            sendResponse(t, 400, errorJson("Request body must be a non-empty array"));
            return;
        }
        List<java.util.Map<String, Object>> rawList = gson.fromJson(body,
                new TypeToken<List<java.util.Map<String, Object>>>() {
                }.getType());
        List<Attendance> valid = new java.util.ArrayList<>();
        List<java.util.Map<String, Object>> failed = new java.util.ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Attendance a = list.get(i);
            java.util.Map<String, Object> raw = rawList != null && i < rawList.size() ? rawList.get(i) : null;
            int studentId = resolveStudentId(raw, a != null ? a.getStudentId() : 0);
            if (a == null || studentId <= 0) {
                failed.add(java.util.Map.of("index", i, "reason", "Invalid or unknown student / enrollmentId"));
                continue;
            }
            a.setStudentId(studentId);
            String err = validateRecord(a);
            if (err != null) {
                failed.add(java.util.Map.of("index", i, "reason", err));
                continue;
            }
            valid.add(a);
        }
        if (valid.isEmpty()) {
            sendResponse(t, 400, errorJson("No valid records. " + failed.size() + " failed validation"));
            return;
        }
        int markedBy = currentUserId(t);
        int count = attendanceDAO.markBulkAttendance(valid, markedBy);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("marked", count);
        resp.put("failed", failed);
        sendResponse(t, 200, gson.toJson(resp));
    }

    private int currentUserId(HttpExchange t) {
        try {
            TokenStore.TokenInfo info = getTokenInfo(t);
            if (info != null && info.userId > 0) return info.userId;
        } catch (Exception ignored) {
        }
        return 0;
    }

    /** Returns null when valid, otherwise a human-readable error. */
    private String validateRecord(Attendance a) {
        if (a.getCourseId() <= 0) return "Valid courseId is required";
        if (a.getDate() == null) return "Valid date (yyyy-MM-dd) is required";
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date today = cal.getTime();
        if (a.getDate().after(today)) return "Attendance cannot be marked for a future date";
        if (com.college.dao.AttendanceDAO.normalizeStatus(a.getStatus()) == null)
            return "Status must be PRESENT, ABSENT or LATE";
        return null;
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
