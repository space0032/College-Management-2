package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.GradeDAO;
import com.college.models.Grade;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GradeController extends BaseController implements HttpHandler {

    private final GradeDAO gradeDAO = new GradeDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/grades/student/\\d+/cgpa")) {
                if ("GET".equals(method))
                    handleGetCGPA(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetStudentGrades(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades/faculty/\\d+")) {
                if ("GET".equals(method))
                    handleGetFacultyGrades(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades/course/\\d+/distribution")) {
                if ("GET".equals(method))
                    handleGetGradeDistribution(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades/course/\\d+")) {
                if ("GET".equals(method))
                    handleGetCourseGrades(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades.*")) {
                if ("GET".equals(method))
                    handleGetAllGrades(t);
                else if ("POST".equals(method))
                    handleSaveGrade(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAllGrades(HttpExchange t) throws IOException {
        List<Grade> grades = gradeDAO.getAllGrades();
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetStudentGrades(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetFacultyGrades(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int facultyId = Integer.parseInt(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByFaculty(facultyId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetCourseGrades(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int courseId = Integer.parseInt(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByCourse(courseId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetCGPA(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 2]); // .../student/{id}/cgpa
        double cgpa = gradeDAO.calculateCGPA(studentId);
        sendResponse(t, 200, "{\"cgpa\":" + cgpa + "}");
    }

    private void handleGetGradeDistribution(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int courseId = Integer.parseInt(parts[parts.length - 2]); // .../course/{id}/distribution
        Map<String, Integer> dist = gradeDAO.getGradeDistribution(courseId);
        sendResponse(t, 200, JsonHelper.toJson(dist));
    }

    private void handleSaveGrade(HttpExchange t) throws IOException {
        String body = readBody(t);
        Grade grade = JsonHelper.fromJson(body, Grade.class);
        if (grade == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean success = gradeDAO.saveGrade(grade);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Grade saved successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to save grade"));
        }
    }
}
