package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class DepartmentController extends BaseController implements HttpHandler {

    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/departments/\\d+")) {
                if ("PUT".equals(method)) handleUpdate(t, path);
                else if ("DELETE".equals(method)) handleDelete(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method)) handleGetAll(t);
                else if ("POST".equals(method)) handleAdd(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_DEPARTMENT")) return;
        List<Department> list = departmentDAO.getAllDepartments();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleAdd(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_DEPARTMENT")) return;
        String body = readBody(t);
        Department department = JsonHelper.fromJson(body, Department.class);
        if (department == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = departmentDAO.addDepartment(department);
        if (ok) sendResponse(t, 201, JsonHelper.toJson(department));
        else sendResponse(t, 400, errorJson("Failed to add department"));
    }

    private void handleUpdate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_DEPARTMENT")) return;
        int id = extractId(path);
        String body = readBody(t);
        Department department = JsonHelper.fromJson(body, Department.class);
        if (department == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        department.setId(id);
        boolean ok = departmentDAO.updateDepartment(department);
        if (ok) sendResponse(t, 200, JsonHelper.toJson(department));
        else sendResponse(t, 400, errorJson("Failed to update department"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_DEPARTMENT")) return;
        int id = extractId(path);
        boolean ok = departmentDAO.deleteDepartment(id);
        if (ok) sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else sendResponse(t, 400, errorJson("Failed to delete department"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
