package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.LearningResourceDAO;
import com.college.models.LearningResource;
import com.college.models.ResourceCategory;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResourceController extends BaseController implements HttpHandler {

    private final LearningResourceDAO resourceDAO = new LearningResourceDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/resources")) {
                if ("GET".equals(method)) handleGetResources(t);
                else if ("POST".equals(method)) handleAddResource(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/resources/categories")) {
                if ("GET".equals(method)) handleGetCategories(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/api/resources/search.*")) {
                if ("GET".equals(method)) handleGetResources(t); // handle course filtering
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/resources/\\d+/download")) {
                if ("POST".equals(method)) handleIncrementDownload(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/resources/\\d+")) {
                if ("DELETE".equals(method)) handleDeleteResource(t, path);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetResources(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getQuery();
        List<LearningResource> resources;
        
        if (query != null && query.contains("courseId=")) {
            int courseId = Integer.parseInt(query.split("courseId=")[1].split("&")[0]);
            resources = resourceDAO.getResourcesByCourse(courseId);
        } else {
            resources = resourceDAO.getAllResources();
        }
        sendResponse(t, 200, JsonHelper.toJson(resources));
    }

    private void handleGetCategories(HttpExchange t) throws IOException {
        List<ResourceCategory> categories = resourceDAO.getAllCategories();
        sendResponse(t, 200, JsonHelper.toJson(categories));
    }

    @SuppressWarnings("unchecked")
    private void handleAddResource(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        LearningResource r = new LearningResource();
        r.setTitle((String) map.get("title"));
        r.setDescription((String) map.get("description"));
        r.setCourseId(((Double) map.get("courseId")).intValue());
        r.setCategoryId(((Double) map.get("categoryId")).intValue());
        r.setFilePath((String) map.get("filePath"));
        r.setFileType((String) map.get("fileType"));
        r.setFileSize(((Double) map.get("fileSize")).longValue());
        r.setUploadedBy(((Double) map.get("uploadedBy")).intValue());
        r.setPublic((Boolean) map.get("isPublic"));

        boolean ok = resourceDAO.addResource(r);
        if (ok) sendResponse(t, 201, "{\"message\":\"Resource added successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to add resource"));
    }

    private void handleDeleteResource(HttpExchange t, String path) throws IOException {
        int id = extractId(path);
        boolean ok = resourceDAO.deleteResource(id);
        if (ok) sendResponse(t, 200, "{\"message\":\"Resource deleted successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to delete resource"));
    }

    private void handleIncrementDownload(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 2]); // /resources/{id}/download
        resourceDAO.incrementDownloadCount(id);
        sendResponse(t, 200, "{\"message\":\"Download count incremented\"}");
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
