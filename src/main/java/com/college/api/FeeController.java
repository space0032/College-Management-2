package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EnhancedFeeDAO;
import com.college.utils.JsonHelper;
import java.io.IOException;

public class FeeController extends BaseController implements HttpHandler {

    private final EnhancedFeeDAO feeDAO = new EnhancedFeeDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (!requireAuth(t))
                return;
            if ("GET".equals(method)) {
                if (path.endsWith("/pending")) {
                    if (!requirePermission(t, "VIEW_FEES"))
                        return;
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getPendingFees()));
                } else if (path.equals("/api/fees")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES", "VIEW_OWN_FEES"))
                        return;
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getAllFees()));
                } else if (path.endsWith("/categories")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "MANAGE_FEES"))
                        return;
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getAllCategories()));
                } else if (path.contains("/structure")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "MANAGE_FEES"))
                        return;
                    handleGetStructure(t);
                } else if (path.matches(".*/fees/history/\\d+")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES"))
                        return;
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getPaymentHistory(id)));
                } else {
                    sendResponse(t, 404, "{\"error\":\"Endpoint not found\"}");
                }
            } else if ("POST".equals(method)) {
                if (path.endsWith("/entry")) {
                    if (!requireAnyPermission(t, "MANAGE_FEES", "CREATE_FEES"))
                        return;
                    String body = readBody(t);
                    java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
                    if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null) || map.get("categoryId") == null || map.get("amount") == null) {
                        sendResponse(t, 400, "{\"error\":\"studentId (or enrollmentId), categoryId and amount are required\"}");
                        return;
                    }
                    int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
                    if (studentId <= 0) {
                        sendResponse(t, 400, "{\"error\":\"Unknown student for the given enrollmentId\"}");
                        return;
                    }
                    int categoryId = ((Number) map.get("categoryId")).intValue();
                    double amount = ((Number) map.get("amount")).doubleValue();
                    if (amount <= 0) {
                        sendResponse(t, 400, "{\"error\":\"Amount must be greater than zero\"}");
                        return;
                    }
                    java.sql.Date dueDate = null;
                    if (map.get("dueDate") != null && !((String) map.get("dueDate")).trim().isEmpty()) {
                        try {
                            dueDate = java.sql.Date.valueOf(((String) map.get("dueDate")).trim());
                        } catch (Exception e) {
                            sendResponse(t, 400, "{\"error\":\"Invalid dueDate (expected yyyy-MM-dd)\"}");
                            return;
                        }
                    }
                    boolean ok = feeDAO.addStudentFee(studentId, categoryId, amount, dueDate);
                    if (ok) {
                        sendResponse(t, 201, "{\"status\":\"Fee entry created\"}");
                    } else {
                        sendResponse(t, 400, "{\"error\":\"Failed to create fee entry\"}");
                    }
                } else if (path.endsWith("/pay")) {
                    if (!requireAnyPermission(t, "PAY_FEES", "MANAGE_FEES"))
                        return;
                    String body = readBody(t);
                    com.college.models.FeePayment payment = new com.google.gson.Gson().fromJson(body,
                            com.college.models.FeePayment.class);
                    if (payment == null || payment.getAmount() <= 0) {
                        sendResponse(t, 400, "{\"error\":\"Invalid payment data\"}");
                        return;
                    }
                    if (payment.getPaymentDate() == null) {
                        payment.setPaymentDate(new java.util.Date());
                    }
                    boolean ok = feeDAO.recordPayment(payment);
                    if (ok) {
                        sendResponse(t, 200, "{\"status\":\"Payment recorded successfully\"}");
                    } else {
                        sendResponse(t, 400, "{\"error\":\"Failed to record payment\"}");
                    }
                } else {
                    sendResponse(t, 404, "{\"error\":\"Endpoint not found\"}");
                }
            } else if ("PUT".equals(method)) {
                if (path.contains("/structure")) {
                    if (!requireAnyPermission(t, "MANAGE_FEES", "CREATE_FEES"))
                        return;
                    handleSaveStructure(t);
                } else {
                    sendResponse(t, 404, "{\"error\":\"Endpoint not found\"}");
                }
            } else {
                sendResponse(t, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGetStructure(HttpExchange t) throws IOException {
        java.util.Map<String, String> params = getQueryMap(t);
        String department = params.getOrDefault("department", "").trim();
        String academicYear = params.getOrDefault("academicYear",
                params.getOrDefault("year", java.time.Year.now().toString())).trim();
        if (department.isEmpty()) {
            sendResponse(t, 400, errorJson("department query parameter is required"));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(feeDAO.getProgramFees(department, academicYear)));
    }

    @SuppressWarnings("unchecked")
    private void handleSaveStructure(HttpExchange t) throws IOException {
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("department") == null) {
            sendResponse(t, 400, errorJson("department and fees are required"));
            return;
        }
        String department = String.valueOf(map.get("department")).trim();
        Object yearObj = map.getOrDefault("academicYear", map.getOrDefault("year",
                java.time.Year.now().toString()));
        String academicYear = String.valueOf(yearObj).trim();
        Object feesObj = map.get("fees");
        if (department.isEmpty() || !(feesObj instanceof java.util.List)) {
            sendResponse(t, 400, errorJson("department and fees are required"));
            return;
        }
        java.util.Set<Integer> busCategoryIds = new java.util.HashSet<>();
        for (com.college.models.FeeCategory c : feeDAO.getAllCategories()) {
            if (c.getCategoryName() != null
                    && c.getCategoryName().toLowerCase(java.util.Locale.ROOT).contains("bus")) {
                busCategoryIds.add(c.getId());
            }
        }
        java.util.List<com.college.models.ProgramFeeStructure> fees = new java.util.ArrayList<>();
        for (Object entry : (java.util.List<?>) feesObj) {
            if (!(entry instanceof java.util.Map)) {
                continue;
            }
            java.util.Map<String, Object> row = (java.util.Map<String, Object>) entry;
            Object catObj = row.get("categoryId");
            Object amtObj = row.get("amount");
            if (catObj == null || amtObj == null) {
                continue;
            }
            try {
                int categoryId = ((Number) catObj).intValue();
                double amount = ((Number) amtObj).doubleValue();
                if (categoryId > 0 && amount > 0 && !busCategoryIds.contains(categoryId)) {
                    fees.add(new com.college.models.ProgramFeeStructure(department, categoryId, academicYear, amount));
                }
            } catch (ClassCastException e) {
                sendResponse(t, 400, errorJson("categoryId and amount must be numbers"));
                return;
            }
        }
        boolean ok = feeDAO.saveProgramFees(department, academicYear, fees);
        if (ok) {
            sendResponse(t, 200, "{\"status\":\"Program fee structure saved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to save program fee structure"));
        }
    }
}
