package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.VisitorDAO;
import com.college.models.Visitor;
import com.college.models.VisitorLog;
import com.college.utils.JsonHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VisitorController extends BaseController implements HttpHandler {

    private final VisitorDAO visitorDAO;

    public VisitorController() {
        this.visitorDAO = new VisitorDAO();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/visitors/phone/.*")) {
                if ("GET".equals(method))
                    handleGetVisitorByPhone(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/visitors/log/entry")) {
                if ("POST".equals(method))
                    handleLogEntry(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/visitors/log/\\d+/exit")) {
                if ("PUT".equals(method))
                    handleLogExit(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/visitors/active")) {
                if ("GET".equals(method))
                    handleGetActiveVisitors(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/visitors/logs")) {
                if ("GET".equals(method))
                    handleGetAllVisitorLogs(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetVisitorByPhone(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_VISITOR")) return;
        String phone = path.substring(path.lastIndexOf('/') + 1);
        Visitor visitor = visitorDAO.getVisitorByPhone(phone);
        if (visitor != null) {
            sendResponse(t, 200, JsonHelper.toJson(visitor));
        } else {
            sendResponse(t, 404, errorJson("Visitor not found"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleLogEntry(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_VISITOR")) return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        if (map == null || map.get("phone") == null || map.get("name") == null) {
            sendResponse(t, 400, errorJson("Visitor details are missing"));
            return;
        }

        String phone = (String) map.get("phone");
        Visitor visitor = visitorDAO.getVisitorByPhone(phone);

        int visitorId;
        if (visitor != null) {
            visitorId = visitor.getId();
        } else {
            // Register new visitor
            Visitor newVisitor = new Visitor(
                    0,
                    (String) map.get("name"),
                    phone,
                    (String) map.get("email"),
                    (String) map.get("idProofType"),
                    (String) map.get("idProofNumber"),
                    null);
            visitorId = visitorDAO.addVisitor(newVisitor);
            if (visitorId == -1) {
                sendResponse(t, 500, errorJson("Failed to register new visitor"));
                return;
            }
        }

        // Log entry
        String purpose = (String) map.get("purpose");
        String personToMeet = (String) map.get("personToMeet");
        String gateNumber = (String) map.get("gateNumber");

        visitorDAO.logEntry(visitorId, purpose, personToMeet, gateNumber);
        sendResponse(t, 201, "{\"message\":\"Visitor entry logged successfully\", \"visitorId\": " + visitorId + "}");
    }

    private void handleLogExit(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_VISITOR")) return;
        String[] parts = path.split("/");
        int logId = Integer.parseInt(parts[parts.length - 2]); // .../log/{id}/exit
        visitorDAO.logExit(logId);
        sendResponse(t, 200, "{\"message\":\"Visitor exit logged successfully\"}");
    }

    private void handleGetActiveVisitors(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_VISITOR")) return;
        List<VisitorLog> logs = visitorDAO.getActiveVisitors();
        sendResponse(t, 200, JsonHelper.toJson(logs));
    }

    private void handleGetAllVisitorLogs(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_VISITOR")) return;
        List<VisitorLog> logs = visitorDAO.getAllVisitorLogs();
        sendResponse(t, 200, JsonHelper.toJson(logs));
    }
}
