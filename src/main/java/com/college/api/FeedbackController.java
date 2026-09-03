package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.StudentFeedbackDAO;
import com.college.models.StudentFeedback;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for student feedback about faculty. Backs the feedback feature that was
 * previously only available in the JavaFX app.
 */
public class FeedbackController extends BaseController implements HttpHandler {

    private final StudentFeedbackDAO feedbackDAO;

    public FeedbackController() {
        this.feedbackDAO = new StudentFeedbackDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/feedback/student/[^/]+") && "GET".equals(method)) {
                handleGetByStudent(t, path);
            } else if (path.endsWith("/feedback") && "POST".equals(method)) {
                handleCreate(t);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_FACULTY", "VIEW_STUDENT"))
            return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 1]);
        List<StudentFeedback> list = feedbackDAO.getFeedbackByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_FACULTY"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null) || map.get("facultyId") == null) {
            sendResponse(t, 400, errorJson("studentId (or enrollmentId) and facultyId are required"));
            return;
        }
        StudentFeedback sf = new StudentFeedback();
        int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
            return;
        }
        sf.setStudentId(studentId);
        sf.setFacultyId(((Number) map.get("facultyId")).intValue());
        sf.setFeedbackText((String) map.getOrDefault("feedbackText", ""));
        sf.setCategory((String) map.getOrDefault("category", "General"));
        sf.setPrivate(map.get("private") != null && Boolean.TRUE.equals(map.get("private")));

        boolean ok = feedbackDAO.addFeedback(sf);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Feedback submitted\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to submit feedback"));
        }
    }
}
