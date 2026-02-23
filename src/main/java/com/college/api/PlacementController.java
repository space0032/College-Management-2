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
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/placements/drives/\\d+")) {
                if ("DELETE".equals(method)) handleDeleteDrive(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/drives.*")) {
                if ("GET".equals(method)) handleGetDrives(t);
                else if ("POST".equals(method)) handleAddDrive(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/companies/\\d+")) {
                if ("DELETE".equals(method)) handleDeleteCompany(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/placements/companies.*")) {
                if ("GET".equals(method)) handleGetCompanies(t);
                else if ("POST".equals(method)) handleAddCompany(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
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

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
