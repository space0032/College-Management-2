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
    private final com.college.dao.EnhancedFeeDAO feeDAO = new com.college.dao.EnhancedFeeDAO();

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
        if (!normalizeAndValidate(t, department)) return;
        boolean ok = departmentDAO.addDepartment(department);
        if (ok) {
            seedDefaultProgramFees(department.getName());
            sendResponse(t, 201, JsonHelper.toJson(department));
        }
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
        if (!normalizeAndValidate(t, department)) return;
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

    /**
     * Seed editable default fee breakdown for a newly created program
     * from current global base amounts. Failures are logged only.
     */
    private void seedDefaultProgramFees(String departmentName) {
        try {
            java.util.List<com.college.models.FeeCategory> categories = feeDAO.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                return;
            }
            String currentYear = java.time.Year.now().toString();
            String nextYear = String.valueOf(java.time.Year.now().getValue() + 1);
            for (String year : new String[] { currentYear, nextYear }) {
                if (!feeDAO.getProgramFees(departmentName, year).isEmpty()) {
                    continue;
                }
                java.util.List<com.college.models.ProgramFeeStructure> defaults = new java.util.ArrayList<>();
                for (com.college.models.FeeCategory c : categories) {
                    if (c.getBaseAmount() > 0) {
                        defaults.add(new com.college.models.ProgramFeeStructure(
                                departmentName, c.getId(), year, c.getBaseAmount()));
                    }
                }
                feeDAO.saveProgramFees(departmentName, year, defaults);
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Failed to seed default program fees for " + departmentName, e);
        }
    }

    private boolean normalizeAndValidate(HttpExchange exchange, Department department) throws IOException {
        String name = department.getName() == null ? "" : department.getName().trim();
        String code = department.getCode() == null ? "" : department.getCode().trim().toUpperCase(java.util.Locale.ROOT);
        if (name.isEmpty() || code.isEmpty()) {
            sendResponse(exchange, 400, errorJson("Department name and code are required"));
            return false;
        }
        if (code.length() > 10 || !code.matches("[A-Z0-9_-]+")) {
            sendResponse(exchange, 400, errorJson("Department code must be at most 10 letters, numbers, underscores, or hyphens"));
            return false;
        }
        department.setName(name);
        department.setCode(code);
        return true;
    }
}
