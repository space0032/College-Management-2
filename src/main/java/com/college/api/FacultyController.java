package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.FacultyDAO;
import com.college.dao.CourseDAO;
import com.college.dao.TimetableDAO;
import com.college.dao.StudentFeedbackDAO;
import com.college.dao.NotificationDAO;
import com.college.models.Faculty;
import com.college.models.Course;
import com.college.models.StudentFeedback;
import com.college.models.Notification;
import com.college.utils.JsonHelper;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;
import com.college.dao.UserDAO;
import com.college.dao.RoleDAO;
import com.college.models.Role;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class FacultyController extends BaseController implements HttpHandler {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final StudentFeedbackDAO feedbackDAO = new StudentFeedbackDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        // /api/faculty/me/*
        if (path.equals("/api/faculty/me")) {
            if ("GET".equals(method)) handleGetMe(t);
            else if ("PUT".equals(method)) handleUpdateMe(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/me/courses")) {
            if ("GET".equals(method)) handleGetMyCourses(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/me/workload")) {
            if ("GET".equals(method)) handleGetMyWorkload(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/me/feedback")) {
            if ("GET".equals(method)) handleGetMyFeedback(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/me/schedule")) {
            if ("GET".equals(method)) handleGetMySchedule(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/import")) {
            if ("POST".equals(method)) handleImportFaculty(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.equals("/api/faculty/template")) {
            if ("GET".equals(method)) handleDownloadTemplate(t);
            else sendResponse(t, 405, errorJson("Method Not Allowed"));
        } else if (path.matches(".*/faculty/\\d+")) {
            int id = extractId(path);
            if ("GET".equals(method)) {
                handleGetById(t, id);
            } else if ("PUT".equals(method)) {
                handleUpdate(t, id);
            } else if ("DELETE".equals(method)) {
                handleDelete(t, id);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        } else if (path.endsWith("/faculty/search")) {
            if ("GET".equals(method)) {
                handleSearch(t);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        } else {
            if ("GET".equals(method)) {
                handleGetAll(t);
            } else if ("POST".equals(method)) {
                handleCreate(t);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY"))
            return;
        try {
            java.util.Map<String, String> params = getQueryMap(t);
            int page = getIntParam(params, "page", 1);
            int size = getIntParam(params, "size", Integer.MAX_VALUE);

            List<Faculty> list;
            if (params.containsKey("page")) {
                list = facultyDAO.getAllFacultyPaginated(page, size);
                int totalCount = facultyDAO.getTotalCount();
                t.getResponseHeaders().set("X-Total-Count", String.valueOf(totalCount));
                t.getResponseHeaders().set("Access-Control-Expose-Headers", "X-Total-Count");
            } else {
                list = facultyDAO.getAllFaculty();
            }

            sendResponse(t, 200, JsonHelper.toJson(list));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetById(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY"))
            return;
        try {
            Faculty f = facultyDAO.getFacultyById(id);
            if (f == null) {
                sendResponse(t, 404, errorJson("Faculty not found"));
            } else {
                sendResponse(t, 200, JsonHelper.toJson(f));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_FACULTY"))
            return;
        try {
            String body = readBody(t);
            Faculty f = JsonHelper.fromJson(body, Faculty.class);
            if (f == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            // Extract password via JSON parsing instead of fragile regex
            String password = "123";
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> bodyMap = JSON.fromJson(body, Map.class);
                Object pwd = bodyMap.get("password");
                if (pwd != null && !String.valueOf(pwd).trim().isEmpty()) {
                    password = String.valueOf(pwd);
                }
            } catch (Exception ignored) {
            }

            int userId = createFacultyUser(f, password);
            if (userId <= 0) {
                sendResponse(t, 500, errorJson("Failed to create faculty user account"));
                return;
            }

            String generatedUsername = f.getUsername();

            int id = facultyDAO.addFaculty(f, userId);
            if (id > 0) {
                f.setId(id);
                f.setUserId(userId);
                sendResponse(t, 201, "{\"id\":" + id
                        + ",\"username\":\"" + escapeJson(generatedUsername)
                        + "\",\"password\":\"" + escapeJson(password) + "\"}");
            } else {
                sendResponse(t, 400, errorJson("Failed to create faculty"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private int createFacultyUser(Faculty f, String password) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            // Generate unique username by querying max existing number
            String username = generateUniqueUsername(conn);
            RoleDAO roleDAO = new RoleDAO();
            UserDAO userDAO = new UserDAO();
            Role role = roleDAO.getRoleByCode(conn, "FACULTY");
            int roleId = (role != null) ? role.getId() : 0;
            int userId;
            if (roleId > 0) {
                userId = userDAO.addUser(conn, username, password, "FACULTY", roleId);
            } else {
                userId = userDAO.addUser(conn, username, password, "FACULTY");
            }
            if (userId <= 0) {
                conn.rollback();
                return -1;
            }
            f.setUsername(username);
            conn.commit();
            return userId;
        } catch (SQLException e) {
            Logger.error("Failed to create faculty user", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.error("Rollback failed", ex);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.error("Connection close failed", e);
                }
            }
        }
    }

    private String generateUniqueUsername(Connection conn) throws SQLException {
        String sql = "SELECT username FROM users WHERE username LIKE 'FAC%' ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String last = rs.getString("username");
                if (last != null && last.startsWith("FAC")) {
                    try {
                        int num = Integer.parseInt(last.substring(3));
                        return "FAC" + (num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return "FAC1001";
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void handleUpdate(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "UPDATE_FACULTY"))
            return;
        try {
            String body = readBody(t);
            Faculty f = JsonHelper.fromJson(body, Faculty.class);
            if (f == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            f.setId(id);
            if (f.getJoinDate() == null) {
                Faculty existing = facultyDAO.getFacultyById(id);
                if (existing != null) {
                    f.setJoinDate(existing.getJoinDate());
                }
            }
            boolean ok = facultyDAO.updateFaculty(f);
            sendResponse(t, ok ? 200 : 400, ok ? JsonHelper.toJson(f) : errorJson("Update failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_FACULTY"))
            return;
        try {
            // deleteFaculty now cascades to the associated user account
            boolean ok = facultyDAO.deleteFaculty(id);
            sendResponse(t, ok ? 200 : 400, ok ? "{\"status\":\"Deleted\"}" : errorJson("Delete failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleSearch(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY"))
            return;
        try {
            String query = t.getRequestURI().getQuery();
            String keyword = "";
            if (query != null && query.contains("q=")) {
                keyword = java.net.URLDecoder.decode(query.split("q=")[1].split("&")[0], "UTF-8");
            }
            List<Faculty> list = facultyDAO.searchFaculty(keyword);
            sendResponse(t, 200, JsonHelper.toJson(list));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    // ===== Self-Service Endpoints =====

    private Faculty resolveFacultyFromToken(HttpExchange t) throws IOException {
        var tokenInfo = getTokenInfo(t);
        if (tokenInfo == null) {
            sendResponse(t, 401, errorJson("Unauthorized"));
            return null;
        }
        Faculty f = facultyDAO.getFacultyByUserId(tokenInfo.userId);
        if (f == null) {
            sendResponse(t, 404, errorJson("No faculty profile found for this account"));
        }
        return f;
    }

    private void handleGetMe(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f != null) sendResponse(t, 200, JsonHelper.toJson(f));
    }

    private void handleUpdateMe(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f == null) return;

        try {
            String body = readBody(t);
            @SuppressWarnings("unchecked")
            Map<String, Object> updates = JSON.fromJson(body, Map.class);

            if (updates.containsKey("phone")) f.setPhone((String) updates.get("phone"));
            if (updates.containsKey("specialization")) f.setSpecialization((String) updates.get("specialization"));
            if (updates.containsKey("qualification")) f.setQualification((String) updates.get("qualification"));

            boolean ok = facultyDAO.updateFaculty(f);
            sendResponse(t, ok ? 200 : 400, ok ? JsonHelper.toJson(f) : errorJson("Update failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetMyCourses(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f == null) return;

        List<Course> courses = courseDAO.getCoursesByFaculty(f.getId());
        sendResponse(t, 200, JsonHelper.toJson(courses));
    }

    private void handleGetMyWorkload(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f == null) return;

        try {
            CourseDAO.WorkloadStats stats = courseDAO.getFacultyWorkload(f.getId());
            List<Course> courses = courseDAO.getCoursesByFaculty(f.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("stats", stats);
            response.put("courses", courses);
            response.put("facultyName", f.getName());
            response.put("department", f.getDepartment());

            sendResponse(t, 200, JsonHelper.toJson(response));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetMyFeedback(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f == null) return;

        try {
            List<Map<String, Object>> feedbackList = feedbackDAO.getFeedbackByFacultyId(f.getId());
            sendResponse(t, 200, JsonHelper.toJson(feedbackList));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetMySchedule(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;
        Faculty f = resolveFacultyFromToken(t);
        if (f == null) return;

        List<?> schedule = timetableDAO.getTimetableByFaculty(f.getName());
        sendResponse(t, 200, JsonHelper.toJson(schedule));
    }

    // ===== Bulk Import =====

    private void handleImportFaculty(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_FACULTY")) return;

        try {
            String contentType = t.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                sendResponse(t, 400, errorJson("Expected multipart/form-data upload"));
                return;
            }

            // Read multipart body
            String boundary = contentType.split("boundary=")[1].trim();
            byte[] bodyBytes = t.getRequestBody().readAllBytes();
            String bodyStr = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);

            // Extract file content from multipart
            String csvData = extractMultipartField(bodyStr, boundary, "file");
            if (csvData == null || csvData.trim().isEmpty()) {
                sendResponse(t, 400, errorJson("No file data provided"));
                return;
            }

            String[] lines = csvData.split("\\r?\\n");
            if (lines.length < 2) {
                sendResponse(t, 400, errorJson("CSV must have a header row and at least one data row"));
                return;
            }

            // Parse header
            String[] headers = parseCsvLine(lines[0]);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim().toLowerCase(), i);
            }

            int imported = 0;
            int failed = 0;
            List<Map<String, Object>> errors = new ArrayList<>();

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) continue;
                try {
                    String[] fields = parseCsvLine(lines[i]);
                    String name = getField(fields, headerIndex, "name");
                    String email = getField(fields, headerIndex, "email");

                    if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
                        failed++;
                        errors.add(Map.of("row", i + 1, "message", "Name and email are required"));
                        continue;
                    }

                    Faculty f = new Faculty();
                    f.setName(name);
                    f.setEmail(email);
                    f.setPhone(getField(fields, headerIndex, "phone"));
                    f.setDepartment(getField(fields, headerIndex, "department"));
                    f.setQualification(getField(fields, headerIndex, "qualification"));
                    f.setSpecialization(getField(fields, headerIndex, "specialization"));

                    String password = getField(fields, headerIndex, "password");
                    if (password == null || password.isEmpty()) password = "123";

                    int userId = createFacultyUser(f, password);
                    if (userId <= 0) {
                        failed++;
                        errors.add(Map.of("row", i + 1, "message", "Failed to create user account for: " + email));
                        continue;
                    }

                    int id = facultyDAO.addFaculty(f, userId);
                    if (id > 0) {
                        imported++;
                    } else {
                        failed++;
                        errors.add(Map.of("row", i + 1, "message", "Failed to create faculty record for: " + email));
                    }
                } catch (Exception e) {
                    failed++;
                    errors.add(Map.of("row", i + 1, "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("imported", imported);
            result.put("failed", failed);
            result.put("errors", errors);
            sendResponse(t, 200, JsonHelper.toJson(result));

        } catch (Exception e) {
            sendResponse(t, 500, errorJson("Import failed: " + e.getMessage()));
        }
    }

    private String extractMultipartField(String body, String boundary, String fieldName) {
        String delimiter = "--" + boundary;
        String[] parts = body.split(delimiter);
        for (String part : parts) {
            if (part.contains("name=\"" + fieldName + "\"")) {
                int headerEnd = part.indexOf("\r\n\r\n");
                if (headerEnd == -1) headerEnd = part.indexOf("\n\n");
                if (headerEnd != -1) {
                    return part.substring(headerEnd + (part.charAt(headerEnd) == '\r' ? 4 : 2)).trim();
                }
            }
        }
        return null;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }

    private String getField(String[] fields, Map<String, Integer> headerIndex, String name) {
        Integer idx = headerIndex.get(name);
        if (idx == null || idx >= fields.length) return null;
        String val = fields[idx];
        return val.isEmpty() ? null : val;
    }

    private void handleDownloadTemplate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY")) return;

        String csv = "Name,Email,Phone,Department,Qualification,Specialization,Password\n"
                + "John Smith,john.smith@college.edu,9876543210,CS,PhD,Data Structures,\n"
                + "Jane Doe,jane.doe@college.edu,9123456780,IT,M.Tech,Machine Learning,securePass123\n";

        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "text/csv");
        t.getResponseHeaders().set("Content-Disposition", "attachment; filename=faculty_import_template.csv");
        CorsSupport.addHeaders(t);
        t.sendResponseHeaders(200, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
