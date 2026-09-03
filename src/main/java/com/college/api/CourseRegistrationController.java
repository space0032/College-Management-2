package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CourseRegistrationDAO;
import com.college.dao.CourseRegistrationDAO.RegistrationRequest;
import com.college.models.Student;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API for course registration requests. Backs the course registration feature
 * that was previously only available in the JavaFX app.
 */
public class CourseRegistrationController extends BaseController implements HttpHandler {

    private final CourseRegistrationDAO registrationDAO;

    public CourseRegistrationController() {
        this.registrationDAO = new CourseRegistrationDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.endsWith("/course-registrations/pending") && "GET".equals(method)) {
                handleGetPending(t);
            } else if (path.matches(".*/course-registrations/student/\\d+/ids") && "GET".equals(method)) {
                handleGetStudentIds(t, path);
            } else if (path.matches(".*/course-registrations/course/\\d+/students") && "GET".equals(method)) {
                handleGetEnrolled(t, path);
            } else if (path.endsWith("/course-registrations") && "POST".equals(method)) {
                handleRegister(t);
            } else if (path.matches(".*/course-registrations/\\d+/approve") && "POST".equals(method)) {
                handleApprove(t, path);
            } else if (path.matches(".*/course-registrations/\\d+/reject") && "POST".equals(method)) {
                handleReject(t, path);
            } else if (path.matches(".*/course-registrations/drop/.+") && "DELETE".equals(method)) {
                handleDrop(t, path);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetPending(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COURSE", "MANAGE_COURSES"))
            return;
        List<RegistrationRequest> list = registrationDAO.getPendingRequests();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleGetStudentIds(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COURSE", "MANAGE_COURSES"))
            return;
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 2]);
        List<Integer> ids = registrationDAO.getRegisteredCourseIds(studentId);
        ids.addAll(registrationDAO.getPendingCourseIds(studentId));
        sendResponse(t, 200, JsonHelper.toJson(ids));
    }

    private void handleGetEnrolled(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COURSE", "MANAGE_COURSES"))
            return;
        String[] parts = path.split("/");
        int courseId = Integer.parseInt(parts[parts.length - 2]);
        List<Student> list = registrationDAO.getEnrolledStudents(courseId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleRegister(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COURSE", "MANAGE_COURSES"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("studentId") == null || map.get("courseId") == null) {
            sendResponse(t, 400, errorJson("studentId and courseId are required"));
            return;
        }
        int studentId = ((Number) map.get("studentId")).intValue();
        int courseId = ((Number) map.get("courseId")).intValue();
        String result = registrationDAO.registerCourse(studentId, courseId);
        if ("SUCCESS".equals(result)) {
            sendResponse(t, 201, "{\"message\":\"Course registration requested\"}");
        } else {
            sendResponse(t, 200, "{\"message\":\"" + escape(result) + "\"}");
        }
    }

    private void handleApprove(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_COURSES"))
            return;
        String[] parts = path.split("/");
        int requestId = Integer.parseInt(parts[parts.length - 2]);
        if (registrationDAO.approveRequest(requestId)) {
            sendResponse(t, 200, "{\"message\":\"Registration approved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to approve registration"));
        }
    }

    private void handleReject(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_COURSES"))
            return;
        String[] parts = path.split("/");
        int requestId = Integer.parseInt(parts[parts.length - 2]);
        if (registrationDAO.rejectRequest(requestId)) {
            sendResponse(t, 200, "{\"message\":\"Registration rejected\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to reject registration"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDrop(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_COURSE", "MANAGE_COURSES"))
            return;
        // path format: /api/course-registrations/drop?studentId=&courseId=
        Map<String, String> query = getQueryMap(t);
        int studentId = getIntParam(query, "studentId", -1);
        int courseId = getIntParam(query, "courseId", -1);
        if (studentId < 0 || courseId < 0) {
            sendResponse(t, 400, errorJson("studentId and courseId query params required"));
            return;
        }
        if (registrationDAO.dropCourse(studentId, courseId)) {
            sendResponse(t, 200, "{\"message\":\"Course dropped\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to drop course"));
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "'");
    }
}
