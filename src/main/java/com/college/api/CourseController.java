package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CourseDAO;
import com.college.models.Course;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class CourseController extends BaseController implements HttpHandler {

    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        if (path.matches(".*/courses/\\d+")) {
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
        if (!requirePermission(t, "VIEW_COURSE"))
            return;
        try {
            List<Course> list = courseDAO.getAllCourses();
            sendResponse(t, 200, JsonHelper.toJson(list));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetById(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "VIEW_COURSE"))
            return;
        try {
            Course c = courseDAO.getCourseById(id);
            if (c == null) {
                sendResponse(t, 404, errorJson("Course not found"));
            } else {
                sendResponse(t, 200, JsonHelper.toJson(c));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_COURSE"))
            return;
        try {
            String body = readBody(t);
            Course c = JsonHelper.fromJson(body, Course.class);
            if (c == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            boolean ok = courseDAO.addCourse(c);
            sendResponse(t, ok ? 201 : 400, ok ? JsonHelper.toJson(c) : errorJson("Failed to create course"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "UPDATE_COURSE"))
            return;
        try {
            String body = readBody(t);
            Course c = JsonHelper.fromJson(body, Course.class);
            if (c == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            c.setId(id);
            boolean ok = courseDAO.updateCourse(c);
            sendResponse(t, ok ? 200 : 400, ok ? JsonHelper.toJson(c) : errorJson("Update failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_COURSE"))
            return;
        try {
            boolean ok = courseDAO.deleteCourse(id);
            sendResponse(t, ok ? 200 : 400, ok ? "{\"status\":\"Deleted\"}" : errorJson("Delete failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
