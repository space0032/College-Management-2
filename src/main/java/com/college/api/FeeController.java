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
                if (path.matches(".*/fees/student/\\d+")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES", "VIEW_OWN_FEES"))
                        return;
                    int studentId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getStudentFees(studentId)));
                } else if (path.endsWith("/pending")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES"))
                        return;
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getPendingFees()));
                } else if (path.equals("/api/fees")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES"))
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
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES", "VIEW_OWN_FEES"))
                        return;
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getPaymentHistory(id)));
                } else {
                    sendResponse(t, 404, errorJson("Endpoint not found"));
                }
            } else if ("POST".equals(method)) {
                if (path.endsWith("/entry")) {
                    if (!requireAnyPermission(t, "MANAGE_FEES", "CREATE_FEES"))
                        return;
                    String body = readBody(t);
                    java.util.Map<String, Object> map;
                    try {
                        map = JSON.fromJson(body, java.util.Map.class);
                    } catch (Exception e) {
                        sendResponse(t, 400, errorJson("Invalid JSON body"));
                        return;
                    }
                    if (map == null || (map.get("studentId") == null && map.get("enrollmentId") == null) || map.get("categoryId") == null || map.get("amount") == null) {
                        sendResponse(t, 400, errorJson("studentId (or enrollmentId), categoryId and amount are required"));
                        return;
                    }
                    Integer studentIdRaw = toInt(map.get("studentId"));
                    int studentId = resolveStudentId(map, studentIdRaw == null ? 0 : studentIdRaw);
                    if (studentId <= 0) {
                        sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
                        return;
                    }
                    Integer categoryIdObj = toInt(map.get("categoryId"));
                    Double amountObj = toDouble(map.get("amount"));
                    if (categoryIdObj == null || categoryIdObj <= 0) {
                        sendResponse(t, 400, errorJson("categoryId must be a positive number"));
                        return;
                    }
                    if (amountObj == null || !(amountObj > 0) || !Double.isFinite(amountObj)) {
                        sendResponse(t, 400, errorJson("Amount must be greater than zero"));
                        return;
                    }
                    if (!feeDAO.categoryExists(categoryIdObj)) {
                        sendResponse(t, 400, errorJson("Unknown fee category"));
                        return;
                    }
                    java.sql.Date dueDate = null;
                    if (map.get("dueDate") != null && !String.valueOf(map.get("dueDate")).trim().isEmpty()
                            && !"null".equalsIgnoreCase(String.valueOf(map.get("dueDate")).trim())) {
                        try {
                            dueDate = java.sql.Date.valueOf(String.valueOf(map.get("dueDate")).trim());
                        } catch (Exception e) {
                            sendResponse(t, 400, errorJson("Invalid dueDate (expected yyyy-MM-dd)"));
                            return;
                        }
                    }
                    boolean ok = feeDAO.addStudentFee(studentId, categoryIdObj, amountObj, dueDate);
                    if (ok) {
                        sendResponse(t, 201, "{\"status\":\"Fee entry created\"}");
                    } else {
                        sendResponse(t, 400, errorJson("Failed to create fee entry"));
                    }
                } else if (path.endsWith("/pay")) {
                    if (!requireAnyPermission(t, "PAY_FEES", "MANAGE_FEES"))
                        return;
                    String body = readBody(t);
                    java.util.Map<String, Object> map;
                    try {
                        map = JSON.fromJson(body, java.util.Map.class);
                    } catch (Exception e) {
                        sendResponse(t, 400, errorJson("Invalid JSON body"));
                        return;
                    }
                    Integer feeIdObj = map == null ? null : toInt(map.get("studentFeeId"));
                    Double amountObj = map == null ? null : toDouble(map.get("amount"));
                    if (feeIdObj == null || feeIdObj <= 0 || amountObj == null || !(amountObj > 0) || !Double.isFinite(amountObj)) {
                        sendResponse(t, 400, errorJson("studentFeeId and a positive amount are required"));
                        return;
                    }
                    String mode = map.get("paymentMode") == null ? "CASH" : String.valueOf(map.get("paymentMode")).trim().toUpperCase();
                    if (!mode.matches("CASH|ONLINE|CHEQUE|CARD|UPI|BANK_TRANSFER")) {
                        sendResponse(t, 400, errorJson("Invalid paymentMode (expected CASH, ONLINE, CHEQUE, CARD, UPI or BANK_TRANSFER)"));
                        return;
                    }
                    String remarks = map.get("remarks") == null ? null : String.valueOf(map.get("remarks"));
                    if (remarks != null && remarks.length() > 500) {
                        sendResponse(t, 400, errorJson("remarks must be at most 500 characters"));
                        return;
                    }
                    com.college.models.FeePayment payment = new com.college.models.FeePayment();
                    payment.setStudentFeeId(feeIdObj);
                    payment.setAmount(amountObj);
                    payment.setPaymentMode(mode);
                    payment.setRemarks(remarks);
                    payment.setPaymentDate(new java.util.Date());
                    TokenStore.TokenInfo info = getTokenInfo(t);
                    if (info != null) {
                        payment.setReceivedBy(info.userId);
                    }
                    com.college.dao.EnhancedFeeDAO.PaymentResult result = feeDAO.recordPaymentDetailed(payment);
                    if (result.ok) {
                        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
                        resp.put("status", "Payment recorded successfully");
                        resp.put("recordedAmount", result.recordedAmount);
                        resp.put("capped", result.capped);
                        resp.put("receiptNumber", result.receiptNumber);
                        sendResponse(t, 200, JSON.toJson(resp));
                    } else {
                        sendResponse(t, 400, errorJson(result.error == null ? "Failed to record payment" : result.error));
                    }
                } else {
                    sendResponse(t, 404, errorJson("Endpoint not found"));
                }
            } else if ("PUT".equals(method)) {
                if (path.contains("/structure")) {
                    if (!requireAnyPermission(t, "MANAGE_FEES", "CREATE_FEES"))
                        return;
                    handleSaveStructure(t);
                } else {
                    sendResponse(t, 404, errorJson("Endpoint not found"));
                }
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            try {
                sendResponse(t, 400, errorJson("Invalid JSON body"));
            } catch (IOException ignored) {
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Fee API failed", e);
            try {
                sendResponse(t, 500, errorJson("Internal server error"));
            } catch (IOException ignored) {
            }
        }
    }

    private static Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleGetStructure(HttpExchange t) throws IOException {
        java.util.Map<String, String> params = getQueryMap(t);
        String department = params.getOrDefault("department", "").trim();
        String academicYear = params.getOrDefault("academicYear",
                params.getOrDefault("year", java.time.Year.now().toString())).trim();
        String specialization = params.getOrDefault("specialization", params.getOrDefault("track", "")).trim();
        if (department.isEmpty()) {
            sendResponse(t, 400, errorJson("department query parameter is required"));
            return;
        }
        sendResponse(t, 200,
                JsonHelper.toJson(feeDAO.getProgramFees(department, specialization, academicYear)));
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
        Object specObj = map.getOrDefault("specialization", map.getOrDefault("track", ""));
        String specialization = specObj == null ? "" : String.valueOf(specObj).trim();
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
            Integer categoryIdObj = toInt(catObj);
            Double amountObj = toDouble(amtObj);
            if (categoryIdObj == null || amountObj == null) {
                sendResponse(t, 400, errorJson("categoryId and amount must be numbers"));
                return;
            }
            int categoryId = categoryIdObj;
            double amount = amountObj;
            // Zero is treated as unset (not stored); blank/zero falls back to global defaults.
            // Bus categories are managed separately and excluded here.
            if (categoryId > 0 && amount > 0 && !busCategoryIds.contains(categoryId)) {
                fees.add(new com.college.models.ProgramFeeStructure(department, specialization, categoryId,
                        academicYear, amount));
            }
        }
        boolean ok = feeDAO.saveProgramFees(department, specialization, academicYear, fees);
        if (ok) {
            sendResponse(t, 200, "{\"status\":\"Program fee structure saved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to save program fee structure"));
        }
    }
}
