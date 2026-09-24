package com.college.api;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;
import com.college.utils.JsonHelper;
import com.college.utils.ManagementException;
import com.college.utils.ManagementValidation;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class PayrollController extends BaseController implements HttpHandler {
    private final PayrollDAO payrollDAO;
    private final EmployeeDAO employeeDAO;
    public PayrollController() { this(new PayrollDAO(), new EmployeeDAO()); }
    PayrollController(PayrollDAO payrollDAO, EmployeeDAO employeeDAO) { this.payrollDAO = payrollDAO; this.employeeDAO = employeeDAO; }

    @Override public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;
        String path = t.getRequestURI().getPath(), method = t.getRequestMethod();
        try {
            if (path.equals("/api/payroll") && method.equals("GET")) {
                if (!requirePermission(t, "VIEW_PAYROLL")) return;
                JsonObject query = new JsonObject(); getQueryMap(t).forEach(query::addProperty);
                int[] period = period(query);
                Map<Integer, Employee> employees = new HashMap<>();
                employeeDAO.getAllEmployees().forEach(e -> { if (e.getId() > 0) employees.put(e.getId(), e); });
                List<Map<String, Object>> data = new ArrayList<>();
                for (PayrollEntry entry : payrollDAO.getPayrollEntriesByMonthYear(period[0], period[1])) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", entry.getId()); row.put("employeeId", entry.getEmployeeId());
                    row.put("month", entry.getMonth()); row.put("year", entry.getYear());
                    row.put("basicSalary", entry.getBasicSalary()); row.put("bonuses", entry.getBonuses()); row.put("deductions", entry.getDeductions()); row.put("netSalary", entry.getNetSalary());
                    row.put("status", entry.getStatus().name()); row.put("paymentDate", entry.getPaymentDate() == null ? null : entry.getPaymentDate().toString());
                    Employee employee = employees.get(entry.getEmployeeId());
                    row.put("employeeName", employee == null ? "Employee " + entry.getEmployeeId() : ((employee.getFirstName() == null ? "" : employee.getFirstName()) + " " + (employee.getLastName() == null ? "" : employee.getLastName())).trim());
                    row.put("designation", employee == null ? "" : employee.getDesignation()); data.add(row);
                }
                sendResponse(t, 200, JsonHelper.toJson(Map.of("data", data, "month", period[0], "year", period[1], "total", data.size())));
            } else if (path.equals("/api/payroll") && method.equals("POST")) {
                if (!requirePermission(t, "MANAGE_PAYROLL")) return;
                int[] period = period(ManagementValidation.object(readBody(t)));
                List<PayrollEntry> entries = new ArrayList<>(); List<Map<String, Object>> skipped = new ArrayList<>();
                for (Employee employee : employeeDAO.getAllEmployees()) {
                    String reason = eligibility(employee, period[0], period[1]);
                    if (reason != null) { skipped.add(Map.of("employeeId", Objects.toString(employee.getEmployeeId(), "Unknown"), "reason", reason)); continue; }
                    entries.add(new PayrollEntry(employee.getId(), period[0], period[1], employee.getSalary()));
                }
List<Integer> inserted = payrollDAO.generateBatch(entries);
                int existing = entries.size() - inserted.size();
                // "skipped" lists only ineligible staff; employees whose period
                // already had an entry are counted in "existing" and are NOT
                // repeated in "skipped" to avoid double-counting in the summary.
                sendResponse(t, 200, JsonHelper.toJson(Map.of("generated", inserted.size(), "existing", existing, "skipped", skipped, "message", "Generated " + inserted.size() + " payroll entries; " + existing + " already existed; " + skipped.size() + " skipped.")));
            } else if (path.equals("/api/payroll/mark-paid") && method.equals("POST")) {
                if (!requirePermission(t, "MANAGE_PAYROLL")) return;
                int id = ManagementValidation.integer(ManagementValidation.object(readBody(t)), "id", 1, Integer.MAX_VALUE);
PayrollEntry entry = entry(id);
                if (entry.getStatus() != PayrollEntry.Status.PENDING) throw new ManagementException(409, "Only pending entries can be marked paid. Refresh the payroll list.");
                if (!payrollDAO.markAsPaid(id)) throw new ManagementException(409, "Payroll changed while marking paid. Refresh and retry.");
                sendResponse(t, 200, "{\"success\":true,\"message\":\"Marked as paid\"}");
} else if (path.equals("/api/payroll/mark-all-paid") && method.equals("POST")) {
                if (!requirePermission(t, "MANAGE_PAYROLL")) return;
                int[] period = period(ManagementValidation.object(readBody(t)));
                int paid = payrollDAO.markMonthAsPaidCount(period[0], period[1]);
                sendResponse(t, 200, JsonHelper.toJson(Map.of("success", true, "paid", paid, "message", paid + " pending payroll entr" + (paid == 1 ? "y" : "ies") + " marked as paid.")));
            } else if (path.matches("/api/payroll/[0-9]+") && (method.equals("PUT") || method.equals("DELETE"))) {
                if (!requirePermission(t, method.equals("PUT") ? "UPDATE_PAYROLL" : "DELETE_PAYROLL")) return;
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1)); PayrollEntry entry = entry(id);
                if (entry.getStatus() != PayrollEntry.Status.PENDING) throw new ManagementException(409, "Only pending payroll entries can be changed.");
                boolean updated;
                if (method.equals("PUT")) {
                    JsonObject body = ManagementValidation.object(readBody(t));
                    entry.setBonuses(ManagementValidation.money(body, "bonuses", entry.getBonuses()));
                    entry.setDeductions(ManagementValidation.money(body, "deductions", entry.getDeductions()));
                    updated = payrollDAO.updatePayrollEntry(entry);
                } else updated = payrollDAO.deletePayrollEntry(id);
                if (!updated) throw new ManagementException(409, "Payroll changed while you were editing. Refresh and retry.");
                sendResponse(t, 200, "{\"success\":true}");
            } else if (path.equals("/api/payroll") || path.matches("/api/payroll/([0-9]+|mark-paid|mark-all-paid)")) sendResponse(t, 405, errorJson("Method not allowed"));
            else sendResponse(t, 404, errorJson("Not found"));
        } catch (ManagementException e) { sendResponse(t, e.getStatus(), errorJson(e.getMessage())); }
        catch (IllegalArgumentException e) { sendResponse(t, 400, errorJson("Invalid payroll input.")); }
        catch (Exception e) { com.college.utils.Logger.error("Payroll request failed", e); sendResponse(t, 500, errorJson("Could not complete payroll request.")); }
    }
    private PayrollEntry entry(int id) { PayrollEntry value = payrollDAO.getById(id); if (value == null) throw new ManagementException(404, "Payroll entry not found."); return value; }
    static int[] period(JsonObject body) {
        int month = body.has("month") ? ManagementValidation.integer(body, "month", 1, 12) : LocalDate.now().getMonthValue();
        int year = body.has("year") ? ManagementValidation.integer(body, "year", 1, 9999) : LocalDate.now().getYear();
        return new int[]{month, year};
    }
    static String eligibility(Employee employee, int month, int year) {
        if (employee.getStatus() != Employee.Status.ACTIVE) return "Not active";
        if (employee.getId() <= 0) return "Employee profile not saved";
        if (employee.getSalary() == null || employee.getSalary().signum() <= 0) return "Set a positive monthly salary";
        if (employee.getJoinDate() == null) return "Set a joining date";
        if (employee.getJoinDate().isAfter(YearMonth.of(year, month).atEndOfMonth())) return "Joining date is after this period";
        return null;
    }
}
