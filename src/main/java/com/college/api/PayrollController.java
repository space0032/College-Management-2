package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;
import com.college.utils.JsonHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PayrollController extends BaseController implements HttpHandler {

    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String query = t.getRequestURI().getQuery();

        try {
            if (path.equals("/api/payroll")) {
                if ("GET".equals(method))
                    handleGetPayroll(t, query);
                else if ("POST".equals(method))
                    handleGeneratePayroll(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/payroll/mark-paid")) {
                if ("POST".equals(method))
                    handleMarkPaid(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/payroll/mark-all-paid")) {
                if ("POST".equals(method))
                    handleMarkAllPaid(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches("/api/payroll/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                if ("PUT".equals(method))
                    handleUpdatePayrollEntry(t, id);
                else if ("DELETE".equals(method))
                    handleDeletePayrollEntry(t, id);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetPayroll(HttpExchange t, String query) throws IOException {
        if (!requirePermission(t, "VIEW_PAYROLL")) return;
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    if ("month".equals(kv[0]))
                        month = Integer.parseInt(kv[1]);
                    if ("year".equals(kv[0]))
                        year = Integer.parseInt(kv[1]);
                }
            }
        }

        List<PayrollEntry> entries = payrollDAO.getPayrollEntriesByMonthYear(month, year);

        // Build employee map for name lookups
        List<Employee> employees = employeeDAO.getAllEmployees();
        Map<Integer, Employee> empMap = new HashMap<>();
        for (Employee e : employees) {
            if (e.getId() > 0)
                empMap.put(e.getId(), e);
        }

        // Enrich with employee name/designation
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (PayrollEntry p : entries) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("employeeId", p.getEmployeeId());
            map.put("month", p.getMonth());
            map.put("year", p.getYear());
            map.put("basicSalary", p.getBasicSalary());
            map.put("bonuses", p.getBonuses());
            map.put("deductions", p.getDeductions());
            map.put("netSalary", p.getNetSalary());
            map.put("status", p.getStatus().name());
            map.put("paymentDate", p.getPaymentDate() != null ? p.getPaymentDate().toString() : null);

            Employee emp = empMap.get(p.getEmployeeId());
            if (emp != null) {
                map.put("employeeName", emp.getFirstName() + " " + emp.getLastName());
                map.put("designation", emp.getDesignation());
            } else {
                map.put("employeeName", "Unknown");
                map.put("designation", "");
            }
            enriched.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", enriched);
        response.put("month", month);
        response.put("year", year);
        response.put("total", enriched.size());
        sendResponse(t, 200, JsonHelper.toJson(response));
    }

    @SuppressWarnings("unchecked")
    private void handleGeneratePayroll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_PAYROLL")) return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);

        int month = req.containsKey("month") ? ((Double) req.get("month")).intValue() : LocalDate.now().getMonthValue();
        int year = req.containsKey("year") ? ((Double) req.get("year")).intValue() : LocalDate.now().getYear();

        List<Employee> active = employeeDAO.getAllEmployees().stream()
                .filter(e -> e.getStatus() == Employee.Status.ACTIVE)
                .collect(Collectors.toList());

        List<PayrollEntry> existing = payrollDAO.getPayrollEntriesByMonthYear(month, year);
        Set<Integer> existingIds = existing.stream()
                .map(PayrollEntry::getEmployeeId)
                .collect(Collectors.toSet());

        int count = 0;
        for (Employee e : active) {
            if (!existingIds.contains(e.getId())) {
                PayrollEntry entry = new PayrollEntry(e.getId(), month, year,
                        e.getSalary() != null ? e.getSalary() : BigDecimal.ZERO);
                if (payrollDAO.createPayrollEntry(entry))
                    count++;
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("generated", count);
        res.put("message", "Generated " + count + " payroll entries for " + month + "/" + year);
        sendResponse(t, 200, JsonHelper.toJson(res));
    }

    @SuppressWarnings("unchecked")
    private void handleMarkPaid(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_PAYROLL")) return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);
        if (!req.containsKey("id")) {
            sendResponse(t, 400, errorJson("Missing payroll entry id"));
            return;
        }
        int id = ((Double) req.get("id")).intValue();
        boolean ok = payrollDAO.markAsPaid(id);
        if (ok)
            sendResponse(t, 200, "{\"success\":true,\"message\":\"Marked as paid\"}");
        else
            sendResponse(t, 500, errorJson("Failed to mark as paid"));
    }

    @SuppressWarnings("unchecked")
    private void handleMarkAllPaid(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_PAYROLL")) return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);
        int month = req.containsKey("month") ? ((Double) req.get("month")).intValue() : LocalDate.now().getMonthValue();
        int year = req.containsKey("year") ? ((Double) req.get("year")).intValue() : LocalDate.now().getYear();

        boolean ok = payrollDAO.markMonthAsPaid(month, year);
        if (ok)
            sendResponse(t, 200, "{\"success\":true,\"message\":\"All pending entries marked as paid\"}");
        else
            sendResponse(t, 500, errorJson("Failed to mark all as paid"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdatePayrollEntry(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "UPDATE_PAYROLL")) return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);

        List<PayrollEntry> all = payrollDAO.getAllPayrollEntries();
        PayrollEntry existing = all.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        if (existing == null) {
            sendResponse(t, 404, errorJson("Payroll entry not found"));
            return;
        }

        if (req.containsKey("bonuses"))
            existing.setBonuses(new BigDecimal(req.get("bonuses").toString()));
        if (req.containsKey("deductions"))
            existing.setDeductions(new BigDecimal(req.get("deductions").toString()));
        existing.calculateNet();

        boolean ok = payrollDAO.updatePayrollEntry(existing);
        if (ok)
            sendResponse(t, 200, "{\"success\":true}");
        else
            sendResponse(t, 500, errorJson("Failed to update payroll entry"));
    }

    private void handleDeletePayrollEntry(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_PAYROLL")) return;
        boolean ok = payrollDAO.deletePayrollEntry(id);
        if (ok)
            sendResponse(t, 200, "{\"success\":true}");
        else
            sendResponse(t, 500, errorJson("Failed to delete payroll entry"));
    }
}
