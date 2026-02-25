package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CommunityDAO;
import com.college.models.Scholarship;
import com.college.models.ScholarshipApplication;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ScholarshipController extends BaseController implements HttpHandler {

    private final CommunityDAO communityDAO = new CommunityDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/scholarships/\\d+/applications/\\d+/status")) { // PUT update application status
                if ("PUT".equals(method))
                    handleUpdateApplicationStatus(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/scholarships/\\d+/applications")) {
                if ("GET".equals(method))
                    handleGetApplications(t, path);
                else if ("POST".equals(method))
                    handleApply(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/scholarships")) {
                if ("GET".equals(method))
                    handleGetAllScholarships(t);
                else if ("POST".equals(method))
                    handleCreateScholarship(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAllScholarships(HttpExchange t) throws IOException {
        List<Scholarship> list = communityDAO.getAllScholarships();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleCreateScholarship(HttpExchange t) throws IOException {
        String body = readBody(t);
        Scholarship scholarship = JsonHelper.fromJson(body, Scholarship.class);
        if (scholarship == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = communityDAO.createScholarship(scholarship);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Scholarship created successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create scholarship"));
        }
    }

    private void handleApply(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int scholarshipId = Integer.parseInt(parts[parts.length - 2]);

        String body = readBody(t);
        ScholarshipApplication app = JsonHelper.fromJson(body, ScholarshipApplication.class);
        if (app == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        app.setScholarshipId(scholarshipId);

        boolean ok = communityDAO.applyForScholarship(app);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Application submitted successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to submit application"));
        }
    }

    private void handleGetApplications(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int scholarshipId = Integer.parseInt(parts[parts.length - 1]);

        List<ScholarshipApplication> list = communityDAO.getApplications(scholarshipId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateApplicationStatus(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int applicationId = Integer.parseInt(parts[parts.length - 2]); // .../applications/{id}/status

        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);
        String status = map != null ? map.get("status") : null;

        if (status == null || status.trim().isEmpty()) {
            sendResponse(t, 400, errorJson("Status is required"));
            return;
        }

        boolean ok = communityDAO.updateApplicationStatus(applicationId, status);
        if (ok) {
            sendResponse(t, 200, "{\"message\":\"Application status updated\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update status"));
        }
    }
}
