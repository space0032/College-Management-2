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

        if ("GET".equals(method)) {
            handleGet(t);
        } else if ("POST".equals(method)) {
            handlePost(t);
        } else {
            sendResponse(t, 405, "Method Not Allowed");
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
