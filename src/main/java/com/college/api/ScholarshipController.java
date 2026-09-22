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

    private final CommunityDAO communityDAO;

    public ScholarshipController() { this(new CommunityDAO()); }

    ScholarshipController(CommunityDAO communityDAO) { this.communityDAO = communityDAO; }

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
        if (!requirePermission(t, "VIEW_SCHOLARSHIP")) return;
        List<Scholarship> list = communityDAO.getAllScholarships();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handleCreateScholarship(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_SCHOLARSHIP")) return;
        String body = readBody(t);
        Scholarship scholarship = JsonHelper.fromJson(body, Scholarship.class);
        if (scholarship == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        if (scholarship.getTitle() == null || scholarship.getTitle().isBlank()
                || scholarship.getDescription() == null || scholarship.getDescription().isBlank()
                || !Double.isFinite(scholarship.getAmount()) || scholarship.getAmount() <= 0) {
            sendResponse(t, 400, errorJson("Title, eligibility and a positive award amount are required"));
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
        if (!requireAnyPermission(t, "MANAGE_SCHOLARSHIP", "SCHOLARSHIP_APPLY")) return;
        String[] parts = path.split("/");
        int scholarshipId = Integer.parseInt(parts[parts.length - 2]);

        String body = readBody(t);
        ScholarshipApplication app = JsonHelper.fromJson(body, ScholarshipApplication.class);
        if (app == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        Map<String, Object> payload = JSON.fromJson(body, Map.class);
        app.setStudentId(resolveStudentId(payload, app.getStudentId()));
        if (app.getStudentId() <= 0 || app.getStatement() == null || app.getStatement().isBlank()
                || app.getStatement().trim().split("\\s+").length > 500) {
            sendResponse(t, 400, errorJson("A valid student and a statement of 1 to 500 words are required"));
            return;
        }
        Scholarship scholarship = communityDAO.getAllScholarships().stream()
                .filter(item -> item.getId() == scholarshipId).findFirst().orElse(null);
        if (scholarship == null || !"OPEN".equals(scholarship.getStatus())) {
            sendResponse(t, 400, errorJson("This scholarship is not open for applications"));
            return;
        }
        if (communityDAO.getApplications(scholarshipId).stream().anyMatch(item -> item.getStudentId() == app.getStudentId())) {
            sendResponse(t, 409, errorJson("You have already applied for this scholarship"));
            return;
        }
        app.setStatus("APPLIED");
        app.setScholarshipId(scholarshipId);

        boolean ok = communityDAO.applyForScholarship(app);
        if (ok) {
            sendResponse(t, 201, "{\"message\":\"Application submitted successfully\"}");
        } else if (communityDAO.getApplications(scholarshipId).stream().anyMatch(item -> item.getStudentId() == app.getStudentId())) {
            // Lost a TOCTOU race: a concurrent submit inserted first. Report as
            // a duplicate, matching the pre-check that normally catches this.
            sendResponse(t, 409, errorJson("You have already applied for this scholarship"));
        } else {
            sendResponse(t, 400, errorJson("Failed to submit application"));
        }
    }

    private void handleGetApplications(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_SCHOLARSHIP")) return;
        String[] parts = path.split("/");
        int scholarshipId = Integer.parseInt(parts[parts.length - 2]);

        List<ScholarshipApplication> list = communityDAO.getApplications(scholarshipId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateApplicationStatus(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_SCHOLARSHIP")) return;
        String[] parts = path.split("/");
        int applicationId = Integer.parseInt(parts[parts.length - 2]); // .../applications/{id}/status

        String body = readBody(t);
        Map<String, String> map = new com.google.gson.Gson().fromJson(body, Map.class);
        String status = map != null ? map.get("status") : null;

        if (!java.util.Set.of("APPROVED", "REJECTED", "APPLIED").contains(status == null ? "" : status)) {
            sendResponse(t, 400, errorJson("Status is required"));
            return;
        }

        int scholarshipId = Integer.parseInt(parts[parts.length - 4]);
        if (communityDAO.getApplications(scholarshipId).stream().noneMatch(app -> app.getId() == applicationId)) {
            sendResponse(t, 404, errorJson("Application not found for this scholarship"));
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
