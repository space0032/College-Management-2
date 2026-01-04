package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.EnhancedFeeDAO;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FeeController implements HttpHandler {

    private final EnhancedFeeDAO feeDAO = new EnhancedFeeDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if ("GET".equals(t.getRequestMethod())) {
            String path = t.getRequestURI().getPath();
            // Expected /fees/pending
            if (path.endsWith("/pending")) {
                try {
                    var fees = feeDAO.getPendingFees();
                    String json = JsonHelper.toJson(fees);
                    sendResponse(t, 200, json);
                } catch (Exception e) {
                    sendResponse(t, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                sendResponse(t, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } else {
            sendResponse(t, 405, "Method Not Allowed");
        }
    }

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
