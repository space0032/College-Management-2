package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.SpecializationDAO;
import com.college.models.Specialization;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

/**
 * SpecializationController - CRUD for tracks inside departments.
 * Gated on department permissions: tracks belong to departments.
 */
public class SpecializationController extends BaseController implements HttpHandler {

    private final SpecializationDAO specializationDAO = new SpecializationDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/specializations/\\d+")) {
                if ("PUT".equals(method))
                    handleUpdate(t, path);
                else if ("DELETE".equals(method))
                    handleDelete(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method))
                    handleGetAll(t);
                else if ("POST".equals(method))
                    handleAdd(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_DEPARTMENT"))
            return;
        java.util.Map<String, String> params = getQueryMap(t);
        List<Specialization> list;
        if (params.containsKey("departmentId")) {
            try {
                list = specializationDAO.getByDepartment(Integer.parseInt(params.get("departmentId")));
            } catch (NumberFormatException e) {
                sendResponse(t, 400, errorJson("Invalid departmentId"));
                return;
            }
        } else {
            list = specializationDAO.getAllSpecializations();
        }
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleAdd(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_DEPARTMENT"))
            return;
        String body = readBody(t);
        Specialization spec = JsonHelper.fromJson(body, Specialization.class);
        if (spec == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        if (!normalizeAndValidate(t, spec))
            return;
        boolean ok = specializationDAO.addSpecialization(spec);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(spec));
        else
            sendResponse(t, 400, errorJson("Failed to add specialization (duplicate name in department?)"));
    }

    private void handleUpdate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_DEPARTMENT"))
            return;
        int id = extractId(path);
        String body = readBody(t);
        Specialization spec = JsonHelper.fromJson(body, Specialization.class);
        if (spec == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        if (!normalizeAndValidate(t, spec))
            return;
        spec.setId(id);
        boolean ok = specializationDAO.updateSpecialization(spec);
        if (ok)
            sendResponse(t, 200, JsonHelper.toJson(spec));
        else
            sendResponse(t, 400, errorJson("Failed to update specialization"));
    }

    private void handleDelete(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_DEPARTMENT"))
            return;
        int id = extractId(path);
        if (specializationDAO.countUsage(id) > 0) {
            sendResponse(t, 409,
                    errorJson("Track is in use by subjects or students and cannot be deleted. Deactivate it instead."));
            return;
        }
        boolean ok = specializationDAO.deleteSpecialization(id);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else
            sendResponse(t, 400, errorJson("Failed to delete specialization"));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private boolean normalizeAndValidate(HttpExchange exchange, Specialization spec) throws IOException {
        String name = spec.getName() == null ? "" : spec.getName().trim();
        if (name.isEmpty()) {
            sendResponse(exchange, 400, errorJson("Track name is required"));
            return false;
        }
        if (spec.getDepartmentId() <= 0) {
            sendResponse(exchange, 400, errorJson("Department is required"));
            return false;
        }
        spec.setName(name);
        if (spec.getCode() != null) {
            spec.setCode(spec.getCode().trim().toUpperCase(java.util.Locale.ROOT));
        }
        if (spec.getDescription() != null) {
            spec.setDescription(spec.getDescription().trim());
        }
        return true;
    }
}
