package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.FacultyDAO;
import com.college.dao.TimetableDAO;
import com.college.models.Faculty;
import com.college.models.Timetable;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkloadController extends BaseController implements HttpHandler {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/workload/analytics")) {
                if ("GET".equals(method)) handleGetWorkloadAnalytics(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/workload/faculty")) {
                 if ("GET".equals(method)) handleGetFacultyWorkload(t);
                 else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetWorkloadAnalytics(HttpExchange t) throws IOException {
        List<Faculty> allFaculty = facultyDAO.getAllFaculty();
        List<Map<String, Object>> analytics = new ArrayList<>();

        for (Faculty f : allFaculty) {
            List<Timetable> classes = timetableDAO.getTimetableByFaculty(f.getName());
            
            int totalHours = classes.size(); // Assuming 1 hour per slot for simplicity
            long uniqueSubjects = classes.stream().map(Timetable::getSubject).distinct().count();

            Map<String, Object> map = new HashMap<>();
            map.put("facultyId", f.getId());
            map.put("facultyName", f.getName());
            map.put("department", f.getDepartment());
            map.put("totalClasses", totalHours);
            map.put("uniqueSubjects", uniqueSubjects);
            
            // Calculate a simple "load percentage" assuming max load is 20 classes per week
            double loadPercentage = Math.min(100.0, (totalHours / 20.0) * 100);
            map.put("loadPercentage", Math.round(loadPercentage));
            
            analytics.add(map);
        }

        sendResponse(t, 200, JsonHelper.toJson(analytics));
    }

    private void handleGetFacultyWorkload(HttpExchange t) throws IOException {
        String query = t.getRequestURI().getQuery();
        if (query == null || !query.contains("name=")) {
            sendResponse(t, 400, errorJson("Missing faculty name param"));
            return;
        }

        String name = query.split("name=")[1].split("&")[0];
        name = java.net.URLDecoder.decode(name, "UTF-8");

        List<Timetable> classes = timetableDAO.getTimetableByFaculty(name);
        
        // Group by subject to get credit distribution
        Map<String, Long> subjectCounts = classes.stream()
                .collect(Collectors.groupingBy(Timetable::getSubject, Collectors.counting()));
                
        List<Map<String, Object>> distribution = new ArrayList<>();
        subjectCounts.forEach((subject, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("subject", subject);
            map.put("hours", count);
            distribution.add(map);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("schedule", classes);
        response.put("distribution", distribution);
        response.put("totalHours", classes.size());

        sendResponse(t, 200, JsonHelper.toJson(response));
    }
}
