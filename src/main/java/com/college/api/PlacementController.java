package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.PlacementDAO;
import com.college.models.PlacementDrive;
import com.college.models.PlacementCompany;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class PlacementController extends BaseController implements HttpHandler {

    private final PlacementDAO placementDAO = new PlacementDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/placements/drives/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteDrive(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/drives.*")) {
                if ("GET".equals(method))
                    handleGetDrives(t);
                else if ("POST".equals(method))
                    handleAddDrive(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/companies/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteCompany(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/companies.*")) {
                if ("GET".equals(method))
                    handleGetCompanies(t);
                else if ("POST".equals(method))
                    handleAddCompany(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/applications/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetApplicationsForStudent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/applications/drive/\\d+")) {
                if ("GET".equals(method))
                    handleGetApplicationsForDrive(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/apply")) {
                if ("POST".equals(method))
                    handleApply(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/applications/\\d+/status")) {
                if ("PUT".equals(method))
                    handleUpdateApplicationStatus(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetDrives(HttpExchange t) throws IOException {
        List<PlacementDrive> drives = placementDAO.getAllDrives();
        sendResponse(t, 200, JsonHelper.toJson(drives));
    }

    private void handleAddDrive(HttpExchange t) throws IOException {
        String body = readBody(t);
        PlacementDrive drive = JsonHelper.fromJson(body, PlacementDrive.class);
        if (drive == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        placementDAO.addDrive(drive);
        sendResponse(t, 201, JsonHelper.toJson(drive));
    }

    private void handleDeleteDrive(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        placementDAO.deleteDrive(id);
        sendResponse(t, 200, "{\"status\":\"Deleted\"}");
    }

    private void handleGetCompanies(HttpExchange t) throws IOException {
        List<PlacementCompany> companies = placementDAO.getAllCompanies();
        sendResponse(t, 200, JsonHelper.toJson(companies));
    }

    private void handleAddCompany(HttpExchange t) throws IOException {
        String body = readBody(t);
        PlacementCompany company = JsonHelper.fromJson(body, PlacementCompany.class);
        if (company == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        placementDAO.addCompany(company);
        sendResponse(t, 201, JsonHelper.toJson(company));
    }

    private void handleDeleteCompany(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        placementDAO.deleteCompany(id);
        sendResponse(t, 200, "{\"status\":\"Deleted\"}");
    }

    private void handleGetApplicationsForStudent(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        sendResponse(t, 200, JsonHelper.toJson(placementDAO.getApplicationsForStudent(id)));
    }

    private void handleGetApplicationsForDrive(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        sendResponse(t, 200, JsonHelper.toJson(placementDAO.getApplicationsForDrive(id)));
    }

    @SuppressWarnings("unchecked")
    private void handleApply(HttpExchange t) throws IOException {
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("driveId") == null || map.get("studentId") == null) {
            sendResponse(t, 400, errorJson("driveId and studentId are required"));
            return;
        }
        int driveId = ((Double) map.get("driveId")).intValue();
        int studentId = ((Double) map.get("studentId")).intValue();

        if (placementDAO.hasApplied(driveId, studentId)) {
            sendResponse(t, 400, errorJson("Already applied for this drive"));
            return;
        }

        placementDAO.applyForDrive(driveId, studentId);
        sendResponse(t, 201, "{\"message\":\"Applied successfully\"}");
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateApplicationStatus(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int applicationId = Integer.parseInt(parts[parts.length - 2]); // .../applications/{id}/status
        String body = readBody(t);
        java.util.Map<String, String> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("status") == null) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        placementDAO.updateApplicationStatus(applicationId, map.get("status"));
        sendResponse(t, 200, "{\"message\":\"Status updated successfully\"}");
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
