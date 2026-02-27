package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class FacultyController extends BaseController implements HttpHandler {

    private final FacultyDAO facultyDAO = new FacultyDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        // /api/faculty/{id}
        if (path.matches(".*/faculty/\\d+")) {
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
                int totalCount = facultyDAO.getTotalFacultyCount();
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
            int id = facultyDAO.addFaculty(f, 0);
            if (id > 0) {
                f.setId(id);
                sendResponse(t, 201, JsonHelper.toJson(f));
            } else {
                sendResponse(t, 400, errorJson("Failed to create faculty"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
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

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
