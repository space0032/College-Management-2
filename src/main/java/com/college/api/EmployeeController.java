package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EmployeeDAO;
import com.college.models.Employee;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EmployeeController extends BaseController implements HttpHandler {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/employees")) {
                if ("GET".equals(method)) handleGetEmployees(t);
                else if ("POST".equals(method)) handleAddEmployee(t);
                else if ("PUT".equals(method)) handleUpdateEmployee(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetEmployees(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_EMPLOYEE")) return;
        List<Employee> employees = employeeDAO.getAllEmployees();
        sendResponse(t, 200, JsonHelper.toJson(employees));
    }

    @SuppressWarnings("unchecked")
    private void handleAddEmployee(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_EMPLOYEE")) return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        Employee emp = mapToEmployee(map);
        boolean ok = employeeDAO.addEmployee(emp);
        if (ok) sendResponse(t, 201, "{\"message\":\"Employee added successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to add employee"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateEmployee(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_EMPLOYEE")) return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        Employee emp = mapToEmployee(map);
        // Ensure ID is passed for update if modifying existing record
        if (map.containsKey("id") && map.get("id") != null) {
            emp.setId(((Double) map.get("id")).intValue());
        }

        boolean ok = employeeDAO.updateEmployee(emp);
        if (ok) sendResponse(t, 200, "{\"message\":\"Employee updated successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to update employee"));
    }

    private Employee mapToEmployee(Map<String, Object> map) {
        Employee e = new Employee();
        if (map.containsKey("employeeId")) e.setEmployeeId((String) map.get("employeeId"));
        if (map.containsKey("firstName")) e.setFirstName((String) map.get("firstName"));
        if (map.containsKey("lastName")) e.setLastName((String) map.get("lastName"));
        if (map.containsKey("email")) e.setEmail((String) map.get("email"));
        if (map.containsKey("phone")) e.setPhone((String) map.get("phone"));
        if (map.containsKey("designation")) e.setDesignation((String) map.get("designation"));
        
        if (map.containsKey("joinDate") && map.get("joinDate") != null && !((String)map.get("joinDate")).isEmpty()) {
            e.setJoinDate(LocalDate.parse((String) map.get("joinDate")));
        }
        
        if (map.containsKey("salary") && map.get("salary") != null) {
            e.setSalary(new BigDecimal(map.get("salary").toString()));
        }

        if (map.containsKey("status") && map.get("status") != null) {
            e.setStatus(Employee.Status.valueOf((String) map.get("status")));
        } else {
            e.setStatus(Employee.Status.ACTIVE);
        }
        
        return e;
    }
}
