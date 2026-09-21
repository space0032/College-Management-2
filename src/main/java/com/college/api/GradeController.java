package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.GradeDAO;
import com.college.models.Grade;
import com.college.utils.JsonHelper;
import com.google.gson.Gson;
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
            if (path.matches(".*/grades/student/[^/]+/cgpa")) {
                if ("GET".equals(method))
                    handleGetCGPA(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/grades/student/[^/]+")) {
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
            } else if (path.matches(".*/grades/bulk.*")) {
                if ("POST".equals(method))
                    handleBulkSaveGrade(t);
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
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        List<Grade> grades = gradeDAO.getAllGrades();
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetStudentGrades(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetFacultyGrades(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        String[] parts = path.split("/");
        int facultyId = Integer.parseInt(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByFaculty(facultyId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetCourseGrades(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        String[] parts = path.split("/");
        int courseId = Integer.parseInt(parts[parts.length - 1]);
        List<Grade> grades = gradeDAO.getGradesByCourse(courseId);
        sendResponse(t, 200, JsonHelper.toJson(grades));
    }

    private void handleGetCGPA(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 2]); // .../student/{id}/cgpa
        double cgpa = gradeDAO.calculateCGPA(studentId);
        sendResponse(t, 200, "{\"cgpa\":" + cgpa + "}");
    }

    private void handleGetGradeDistribution(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_GRADES"))
            return;
        String[] parts = path.split("/");
        int courseId = Integer.parseInt(parts[parts.length - 2]); // .../course/{id}/distribution
        Map<String, Integer> dist = gradeDAO.getGradeDistribution(courseId);
        sendResponse(t, 200, JsonHelper.toJson(dist));
    }

    private void handleSaveGrade(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_GRADES"))
            return;
        String body = readBody(t);
        Grade grade = JsonHelper.fromJson(body, Grade.class);
        if (grade == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        String validation = validateGrade(grade);
        if (validation != null) {
            sendResponse(t, 400, errorJson(validation));
            return;
        }
        boolean success = gradeDAO.saveGrade(grade);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Grade saved successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to save grade"));
        }
    }

    private void handleBulkSaveGrade(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_GRADES"))
            return;
        String body = readBody(t);
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Grade>>() {
        }.getType();
        List<Grade> list = new Gson().fromJson(body, listType);
        if (list == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        int count = 0;
        for (Grade g : list) {
            if (validateGrade(g) != null) {
                continue; // skip invalid entries, report only valid saves
            }
            if (gradeDAO.saveGrade(g)) {
                count++;
            }
        }
        sendResponse(t, 200, "{\"saved\":" + count + "}");
    }

    private static final java.util.Set<String> VALID_GRADES =
            java.util.Set.of("A", "B", "C", "D", "E", "F");

    /**
     * Server-side validation mirroring the web UI rules. Returns an error
     * message, or null when the grade is acceptable.
     */
    private String validateGrade(Grade g) {
        if (g == null)
            return "Grade is empty";
        if (g.getStudentId() <= 0)
            return "A valid student is required";
        if (g.getCourseId() <= 0)
            return "A valid subject is required";
        if (g.getExamType() == null || g.getExamType().trim().isEmpty())
            return "Exam type is required";
        double marks = g.getMarksObtained();
        if (Double.isNaN(marks) || Double.isInfinite(marks) || marks < 0 || marks > 100)
            return "Marks must be between 0 and 100";
        if (g.getGrade() == null || !VALID_GRADES.contains(g.getGrade().trim()))
            return "Grade must be one of A, B, C, D, E, F";
        return null;
    }
}
