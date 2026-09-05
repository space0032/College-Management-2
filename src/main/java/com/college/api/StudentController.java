package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EnrollmentDAO;
import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StudentController extends BaseController implements HttpHandler {

    private final StudentDAO studentDAO = new StudentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = normalizePath(t.getRequestURI().getPath());

if (path.matches("/students/\\d+/courses")) {
            if ("GET".equals(method))
                handleGetCourses(t, extractStudentId(path));
            else
                sendResponse(t, 405, "Method Not Allowed");
        } else if (path.matches("/students/\\d+/enroll")) {
            if ("POST".equals(method))
                handleEnroll(t, extractStudentId(path));
            else
                sendResponse(t, 405, "Method Not Allowed");
        } else if (path.endsWith("/students/search") && "GET".equals(method)) {
            handleSearch(t);
        } else if (path.endsWith("/students/template") && "GET".equals(method)) {
            handleDownloadTemplate(t);
        } else if ("GET".equals(method) && path.matches("/students/\\d+")) {
            handleGetById(t, extractStudentId(path));
        } else if ("PUT".equals(method) && path.matches("/students/\\d+")) {
            handlePut(t, extractStudentId(path));
        } else if ("DELETE".equals(method) && path.matches("/students/\\d+")) {
            handleDelete(t, extractStudentId(path));
        } else if ("GET".equals(method)) {
            handleGet(t);
        } else if ("POST".equals(method)) {
            handlePost(t);
        } else {
            sendResponse(t, 405, "Method Not Allowed");
        }
    }

    private void handleSearch(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_STUDENT"))
            return;
        String keyword = getQueryMap(t).getOrDefault("q", "").trim();
        if (keyword.isEmpty()) {
            sendResponse(t, 200, JsonHelper.toJson(studentDAO.getAllStudents()));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(studentDAO.searchStudents(keyword)));
    }

    static String normalizePath(String path) {
        return path.startsWith("/api/") ? path.substring(4) : path;
    }

    static int extractStudentId(String path) {
        String normalizedPath = normalizePath(path);
        String[] parts = normalizedPath.split("/");
        // /students/123/courses -> parts[0]="", [1]="students", [2]="123"
        return Integer.parseInt(parts[2]);
    }

    private void handleGetCourses(HttpExchange t, int studentId) throws IOException {
        if (!requirePermission(t, "VIEW_STUDENT"))
            return;
        List<?> courses = studentDAO.getRegisteredCourses(studentId);
        sendResponse(t, 200, JsonHelper.toJson(courses));
    }

    private void handleEnroll(HttpExchange t, int studentId) throws IOException {
        if (!requirePermission(t, "UPDATE_STUDENT"))
            return;
        InputStream is = t.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        try {
            int courseId = Integer.parseInt(body.replaceAll(".*\"courseId\"\\s*:\\s*(\\d+).*", "$1"));
            int semester = 1;
            if (body.contains("semester"))
                semester = Integer.parseInt(body.replaceAll(".*\"semester\"\\s*:\\s*(\\d+).*", "$1"));
            int year = 2025;
            if (body.contains("year"))
                year = Integer.parseInt(body.replaceAll(".*\"year\"\\s*:\\s*(\\d+).*", "$1"));

            if (studentDAO.registerCourse(studentId, courseId, semester, year)) {
                sendResponse(t, 200, "{\"status\":\"Enrolled\"}");
            } else {
                sendResponse(t, 400, "{\"error\":\"Enrollment failed\"}");
            }
        } catch (Exception e) {
            sendResponse(t, 400, "{\"error\":\"Invalid JSON: " + e.getMessage() + "\"}");
        }
    }

    private void handleGet(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_STUDENT"))
            return;
        try {
            java.util.Map<String, String> params = getQueryMap(t);
            int page = getIntParam(params, "page", 1);
            int size = getIntParam(params, "size", Integer.MAX_VALUE);

            List<Student> students;
            if (params.containsKey("page")) {
                students = studentDAO.getAllStudentsPaginated(page, size);
                int totalCount = studentDAO.getTotalCount();
                t.getResponseHeaders().set("X-Total-Count", String.valueOf(totalCount));
                t.getResponseHeaders().set("Access-Control-Expose-Headers", "X-Total-Count");
            } else {
                students = studentDAO.getAllStudents();
            }

            String json = JsonHelper.toJson(students);
            sendResponse(t, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetById(HttpExchange t, int studentId) throws IOException {
        if (!requirePermission(t, "VIEW_STUDENT_PROFILE"))
            return;
        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student != null) {
                sendResponse(t, 200, JsonHelper.toJson(student));
            } else {
                sendResponse(t, 404, "{\"error\":\"Student not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handlePost(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_STUDENT"))
            return;
        try {
            InputStream is = t.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            Student student = JsonHelper.fromJson(body, Student.class);
            if (student == null) {
                sendResponse(t, 400, "{\"error\":\"Invalid JSON\"}");
                return;
            }
            if (student.getEnrollmentDate() == null) {
                student.setEnrollmentDate(new java.util.Date());
            }

            // Extract password from raw JSON (default: "123")
            String password = "123";
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"password\"\\s*:\\s*\"([^\"]*)\"")
                    .matcher(body);
            if (m.find()) {
                String pwd = m.group(1).trim();
                if (!pwd.isEmpty()) {
                    password = pwd;
                }
            }

            // Use EnrollmentDAO to create student with enrollment number and user account
            Student enrolledStudent = enrollmentDAO.enrollStudent(student, password);
            if (enrolledStudent != null) {
                String response = "{\"id\":" + enrolledStudent.getId()
                        + ",\"username\":\"" + escapeJson(enrolledStudent.getUsername()) + "\""
                        + ",\"password\":\"" + escapeJson(password) + "\""
                        + ",\"enrollmentNumber\":\"" + escapeJson(enrolledStudent.getUsername()) + "\""
                        + ",\"name\":\"" + escapeJson(enrolledStudent.getName()) + "\""
                        + ",\"email\":\"" + escapeJson(enrolledStudent.getEmail()) + "\""
                        + "}";
                sendResponse(t, 201, response);
            } else {
                sendResponse(t, 400, "{\"error\":\"Failed to create student. Check logs for details.\"}");
            }
        } catch (com.college.dao.EnrollmentException ee) {
            ee.printStackTrace();
            sendResponse(t, 400, "{\"error\":\"" + escapeJson(ee.getMessage()) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handlePut(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "UPDATE_STUDENT"))
            return;
        try {
            InputStream is = t.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            Student student = JsonHelper.fromJson(body, Student.class);
            if (student == null) {
                sendResponse(t, 400, "{\"error\":\"Invalid JSON\"}");
                return;
            }
            student.setId(id);
            boolean ok = studentDAO.updateStudentChecked(student);
            if (ok) {
                sendResponse(t, 200, "{\"status\":\"updated\",\"id\":" + id + "}");
            } else {
                sendResponse(t, 400, "{\"error\":\"Failed to update student\"}");
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            sendResponse(t, 400, "{\"error\":\"" + escapeJson(re.getMessage()) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleDelete(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_STUDENT"))
            return;
        try {
            // deleteStudent now cascades to the associated user account
            boolean ok = studentDAO.deleteStudent(id);
            sendResponse(t, ok ? 200 : 400, ok ? "{\"status\":\"Deleted\"}" : "{\"error\":\"Delete failed\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void handleDownloadTemplate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_STUDENT"))
            return;
        String csv = "name,email,phone,department,specialization,semester,batch,address,password,hostelite\n"
                + "John Doe,john.doe@college.edu,9876543210,Computer Science,Cyber Security,3,2023-2027,123 Main St,,No\n"
                + "Jane Doe,jane.doe@college.edu,9123456780,IT,,2,2024-2028,456 Park Ave,,No\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "text/csv");
        t.getResponseHeaders().set("Content-Disposition", "attachment; filename=student_import_template.csv");
        CorsSupport.addHeaders(t);
        t.sendResponseHeaders(200, bytes.length);
        try (java.io.OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
