package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EmployeeDAO;
import com.college.models.Employee;
import com.college.utils.JsonHelper;
import com.college.utils.ManagementException;
import com.college.utils.ManagementValidation;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeController extends BaseController implements HttpHandler {
    private final EmployeeDAO employeeDAO;
    public EmployeeController() { this(new EmployeeDAO()); }
    EmployeeController(EmployeeDAO employeeDAO) { this.employeeDAO = employeeDAO; }
    @Override public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;
        try {
            if (!t.getRequestURI().getPath().equals("/api/employees")) { sendResponse(t, 404, errorJson("Not found")); return; }
            String method = t.getRequestMethod();
            if (method.equals("GET")) {
                if (!requirePermission(t, "VIEW_EMPLOYEE")) return;
                sendResponse(t, 200, JsonHelper.toJson(employeeDAO.getAllEmployees())); return;
            }
            if (!method.equals("POST") && !method.equals("PUT")) { sendResponse(t, 405, errorJson("Method not allowed")); return; }
            if (!requirePermission(t, method.equals("POST") ? "CREATE_EMPLOYEE" : "UPDATE_EMPLOYEE")) return;
            JsonObject body = ManagementValidation.object(readBody(t));
            Employee employee = parseEmployee(body);
            if (method.equals("PUT")) {
                int id = ManagementValidation.integer(body, "id", 1, Integer.MAX_VALUE);
                Employee existing = employeeDAO.getAllEmployees().stream().filter(e -> e.getId() == id).findFirst().orElseThrow(() -> new ManagementException(404, "Employee not found."));
                if (!java.util.Objects.equals(existing.getEmployeeId(), employee.getEmployeeId())) throw new ManagementException(409, "Employee ID cannot be changed.");
employee.setId(id);
                // Preserve the existing designation for user-linked employees only
                // when the update did not include a new one (B-L3).
                if (existing.getUserId() != null
                        && (employee.getDesignation() == null || employee.getDesignation().isBlank())) {
                    employee.setDesignation(existing.getDesignation());
                }
                if (!employeeDAO.updateEmployee(employee)) throw new ManagementException(404, "Employee not found.");
            } else {
                Employee linked = employeeDAO.getAllEmployees().stream().filter(e -> e.getEmployeeId() != null && e.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId())).findFirst().orElse(null);
                if (linked != null && linked.getId() > 0) throw new ManagementException(409, "Employee ID already exists.");
                if (linked != null && linked.getUserId() != null) employee.setDesignation(linked.getDesignation());
                if (!employeeDAO.addEmployee(employee)) throw new ManagementException(500, "Could not create employee.");
            }
            sendResponse(t, method.equals("POST") ? 201 : 200, JSON.toJson(java.util.Map.of("message", "Employee saved successfully")));
        } catch (ManagementException e) { sendResponse(t, e.getStatus(), errorJson(e.getMessage())); }
        catch (IllegalArgumentException e) { sendResponse(t, 400, errorJson("Invalid employee input.")); }
        catch (Exception e) { com.college.utils.Logger.error("Employee request failed", e); sendResponse(t, 500, errorJson("Could not complete employee request.")); }
    }
    static Employee parseEmployee(JsonObject body) {
        Employee employee = new Employee();
        employee.setEmployeeId(ManagementValidation.text(body, "employeeId", true, 50));
        employee.setFirstName(ManagementValidation.text(body, "firstName", true, 100));
        employee.setLastName(ManagementValidation.text(body, "lastName", false, 100));
        String email = ManagementValidation.text(body, "email", true, 100);
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new ManagementException(400, "Enter a valid email address.");
        employee.setEmail(email);
        String phone = ManagementValidation.text(body, "phone", false, 20);
        if (!phone.isEmpty() && (!phone.matches("[+0-9 -]+") || phone.replaceAll("[^0-9]", "").length() < 7 || phone.replaceAll("[^0-9]", "").length() > 15 || phone.indexOf('+') > 0 || phone.lastIndexOf('+') > 0)) throw new ManagementException(400, "Phone must contain 7 to 15 digits, with an optional leading +.");
        employee.setPhone(phone);
        employee.setDesignation(ManagementValidation.text(body, "designation", true, 100));
        String date = ManagementValidation.text(body, "joinDate", false, 10);
        try { employee.setJoinDate(date.isEmpty() ? null : LocalDate.parse(date)); }
        catch (Exception e) { throw new ManagementException(400, "Enter a valid joining date."); }
        employee.setSalary(ManagementValidation.money(body, "salary", BigDecimal.ZERO));
        try { employee.setStatus(body.has("status") ? Employee.Status.valueOf(ManagementValidation.text(body, "status", true, 20)) : Employee.Status.ACTIVE); }
        catch (IllegalArgumentException e) { throw new ManagementException(400, "Invalid employee status."); }
        return employee;
    }
}
