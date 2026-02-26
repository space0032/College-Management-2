package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.SyllabusDAO;
import com.college.dao.CourseDAO;
import com.college.models.Syllabus;
import com.college.models.Course;
import com.college.services.FileUploadService;
import com.college.utils.JsonHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyllabusController extends BaseController implements HttpHandler {

    private final SyllabusDAO syllabusDAO = new SyllabusDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final FileUploadService uploadService = new FileUploadService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();
        String query = t.getRequestURI().getQuery();

        try {
            if (path.equals("/api/syllabus")) {
                if ("GET".equals(method))
                    handleGetSyllabi(t, query);
                else if ("POST".equals(method))
                    handleAddSyllabus(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches("/api/syllabus/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                if ("DELETE".equals(method))
                    handleDeleteSyllabus(t, id);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches("/api/syllabus/download/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                if ("GET".equals(method))
                    handleDownloadSyllabus(t, id);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetSyllabi(HttpExchange t, String query) throws IOException {
        if (!requirePermission(t, "VIEW_SYLLABUS"))
            return;
        if (query != null && query.contains("courseId=")) {
            int courseId = Integer.parseInt(query.split("courseId=")[1].split("&")[0]);
            List<Syllabus> syllabi = syllabusDAO.getSyllabiByCourse(courseId);
            Course course = courseDAO.getCourseById(courseId);
            String courseName = course != null ? course.getName() : "";
            List<Map<String, Object>> enriched = enrichSyllabi(syllabi, courseName);
            sendResponse(t, 200, JsonHelper.toJson(enriched));
        } else {
            List<Course> courses = courseDAO.getAllCourses();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Course c : courses) {
                List<Syllabus> syllabi = syllabusDAO.getSyllabiByCourse(c.getId());
                for (Syllabus s : syllabi) {
                    Map<String, Object> map = syllabusToMap(s);
                    map.put("courseName", c.getName());
                    result.add(map);
                }
            }
            sendResponse(t, 200, JsonHelper.toJson(result));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAddSyllabus(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_SYLLABUS"))
            return;
        String body = readBody(t);
        Map<String, Object> req = gson.fromJson(body, Map.class);

        Syllabus s = new Syllabus();
        s.setCourseId(req.containsKey("courseId") ? ((Double) req.get("courseId")).intValue() : 0);
        s.setTitle(req.containsKey("title") ? (String) req.get("title") : "");
        s.setVersion(req.containsKey("version") ? (String) req.get("version") : "1.0");
        s.setDescription(req.containsKey("description") ? (String) req.get("description") : "");
        s.setUploadedBy(req.containsKey("uploadedBy") ? ((Double) req.get("uploadedBy")).intValue() : 0);

        String filePath = req.containsKey("filePath") ? (String) req.get("filePath") : "";

        if (req.containsKey("fileData") && req.containsKey("fileName")) {
            String b64 = (String) req.get("fileData");
            if (b64.contains(",")) {
                b64 = b64.substring(b64.indexOf(",") + 1);
            }
            byte[] fileBytes = Base64.getDecoder().decode(b64);
            String fileName = (String) req.get("fileName");
            String savedPath = uploadService.uploadSyllabus(new ByteArrayInputStream(fileBytes), fileName,
                    fileBytes.length);
            if (savedPath != null) {
                filePath = savedPath;
            }
        }
        s.setFilePath(filePath);

        if (s.getCourseId() == 0 || s.getTitle().isEmpty()) {
            sendResponse(t, 400, errorJson("courseId and title are required"));
            return;
        }

        boolean ok = syllabusDAO.addSyllabus(s);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Syllabus added successfully\"}");
        else
            sendResponse(t, 500, errorJson("Failed to add syllabus"));
    }

    private void handleDownloadSyllabus(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "VIEW_SYLLABUS"))
            return;

        List<Course> courses = courseDAO.getAllCourses();
        Syllabus target = null;
        for (Course c : courses) {
            List<Syllabus> syllabi = syllabusDAO.getSyllabiByCourse(c.getId());
            for (Syllabus s : syllabi) {
                if (s.getId() == id) {
                    target = s;
                    break;
                }
            }
            if (target != null)
                break;
        }

        if (target == null || target.getFilePath() == null) {
            sendResponse(t, 404, errorJson("File not found"));
            return;
        }

        File file = new File(target.getFilePath());
        if (!file.exists()) {
            sendResponse(t, 404, errorJson("File physically missing on server"));
            return;
        }

        t.getResponseHeaders().set("Content-Type", "application/octet-stream");
        t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        t.sendResponseHeaders(200, file.length());

        try (OutputStream os = t.getResponseBody()) {
            Files.copy(file.toPath(), os);
        }
    }

    private void handleDeleteSyllabus(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_SYLLABUS"))
            return;

        List<Course> courses = courseDAO.getAllCourses();
        Syllabus target = null;
        for (Course c : courses) {
            List<Syllabus> syllabi = syllabusDAO.getSyllabiByCourse(c.getId());
            for (Syllabus s : syllabi) {
                if (s.getId() == id) {
                    target = s;
                    break;
                }
            }
            if (target != null)
                break;
        }

        if (target != null && target.getFilePath() != null) {
            uploadService.deleteFile(target.getFilePath());
        }

        boolean ok = syllabusDAO.deleteSyllabus(id);
        if (ok)
            sendResponse(t, 200, "{\"success\":true}");
        else
            sendResponse(t, 404, errorJson("Syllabus not found or delete failed"));
    }

    private List<Map<String, Object>> enrichSyllabi(List<Syllabus> syllabi, String courseName) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Syllabus s : syllabi) {
            Map<String, Object> map = syllabusToMap(s);
            map.put("courseName", courseName);
            result.add(map);
        }
        return result;
    }

    private Map<String, Object> syllabusToMap(Syllabus s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("courseId", s.getCourseId());
        map.put("title", s.getTitle());
        map.put("version", s.getVersion());
        map.put("description", s.getDescription());
        map.put("filePath", s.getFilePath());
        map.put("uploadedBy", s.getUploadedBy());
        map.put("uploaderName", s.getUploaderName());
        map.put("uploadedAt", s.getUploadedAt() != null ? s.getUploadedAt().toString() : null);
        return map;
    }
}
