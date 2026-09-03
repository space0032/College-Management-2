package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.FeeTransactionDAO;
import com.college.models.FeeTransaction;
import com.college.models.FeeTransaction.Type;
import com.college.models.FeeTransaction.PaymentMode;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API for fee transaction audit records. Backs the fee transaction feature that
 * was previously only available in the JavaFX app.
 */
public class FeeTransactionController extends BaseController implements HttpHandler {

    private final FeeTransactionDAO transactionDAO;

    public FeeTransactionController() {
        this.transactionDAO = new FeeTransactionDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/fee-transactions/student/\\d+") && "GET".equals(method)) {
                handleGetByStudent(t, path);
            } else if (path.endsWith("/fee-transactions") && "POST".equals(method)) {
                handleRecord(t);
            } else {
                sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetByStudent(HttpExchange t, String path) throws IOException {
        if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES", "MANAGE_FEES"))
            return;
        String[] parts = path.split("/");
        int studentId = Integer.parseInt(parts[parts.length - 1]);
        List<FeeTransaction> list = transactionDAO.getTransactionsByStudent(studentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleRecord(HttpExchange t) throws IOException {
        if (!requireAnyPermission(t, "MANAGE_FEES", "PAY_FEES"))
            return;
        String body = readBody(t);
        Map<String, Object> map = JSON.fromJson(body, Map.class);
        if (map == null || map.get("studentId") == null || map.get("amount") == null) {
            sendResponse(t, 400, errorJson("studentId and amount are required"));
            return;
        }

        FeeTransaction ft = new FeeTransaction();
        ft.setTransactionId((String) map.getOrDefault("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8)));
        ft.setStudentId(((Number) map.get("studentId")).intValue());
        if (map.get("feePaymentId") != null) {
            ft.setFeePaymentId(((Number) map.get("feePaymentId")).intValue());
        }
        ft.setAmount(new BigDecimal(map.get("amount").toString()));
        ft.setType(parseType((String) map.get("type")));
        ft.setPaymentMode(parseMode((String) map.get("paymentMode")));
        ft.setDescription((String) map.getOrDefault("description", ""));
        ft.setCreatedBy(getTokenInfo(t) != null ? getTokenInfo(t).userId : 0);

        boolean ok = transactionDAO.recordTransaction(ft);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Transaction recorded\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to record transaction"));
        }
    }

    private Type parseType(String s) {
        try {
            return Type.valueOf(s != null ? s.toUpperCase() : "PAYMENT");
        } catch (Exception e) {
            return Type.PAYMENT;
        }
    }

    private PaymentMode parseMode(String s) {
        try {
            return PaymentMode.valueOf(s != null ? s.toUpperCase() : "CASH");
        } catch (Exception e) {
            return PaymentMode.CASH;
        }
    }
}
