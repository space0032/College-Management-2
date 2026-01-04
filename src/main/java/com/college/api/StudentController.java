package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StudentController implements HttpHandler {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        if (path.matches("/students/\\d+/courses")) {
            if ("GET".equals(method))
                handleGetCourses(t);
            else
                sendResponse(t, 405, "Method Not Allowed");
        } else if (path.matches("/students/\\d+/enroll")) {
            if ("POST".equals(method))
                handleEnroll(t);
            else
                sendResponse(t, 405, "Method Not Allowed");
        } else if ("GET".equals(method)) {
            handleGet(t);
        } else if ("POST".equals(method)) {
            handlePost(t);
        } else {
            sendResponse(t, 405, "Method Not Allowed");
        }
    }

    private int getIdFromPath(HttpExchange t) {
        String path = t.getRequestURI().getPath();
        String[] parts = path.split("/");
        // /students/123/courses -> parts[0]="", [1]="students", [2]="123"
        return Integer.parseInt(parts[2]);
    }

    private void handleGetCourses(HttpExchange t) throws IOException {
        int studentId = getIdFromPath(t);
        List<?> courses = studentDAO.getRegisteredCourses(studentId);
        sendResponse(t, 200, JsonHelper.toJson(courses));
    }

    private void handleEnroll(HttpExchange t) throws IOException {
        int studentId = getIdFromPath(t);
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
        try {
            List<Student> students = studentDAO.getAllStudents();
            String json = JsonHelper.toJson(students);
            sendResponse(t, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handlePost(HttpExchange t) throws IOException {
        try {
            InputStream is = t.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            Student student = JsonHelper.fromJson(body, Student.class);
            if (student == null) {
                sendResponse(t, 400, "{\"error\":\"Invalid JSON\"}");
                return;
            }

            int id = studentDAO.addStudent(student, 0);
            if (id > 0) {
                student.setId(id);
                sendResponse(t, 201, JsonHelper.toJson(student));
            } else {
                sendResponse(t, 400, "{\"error\":\"Failed to create student\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
