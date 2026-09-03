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
                } else if (path.matches(".*/fees/history/\\d+")) {
                    if (!requireAnyPermission(t, "VIEW_FEES", "VIEW_ALL_FEES"))
                        return;
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    sendResponse(t, 200, JsonHelper.toJson(feeDAO.getPaymentHistory(id)));
                } else {
                    sendResponse(t, 404, "{\"error\":\"Endpoint not found\"}");
                }
            } else if ("POST".equals(method)) {
                if (path.endsWith("/pay")) {
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
            } else {
                sendResponse(t, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
