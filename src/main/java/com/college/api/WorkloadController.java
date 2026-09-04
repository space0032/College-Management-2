package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.FacultyDAO;
import com.college.dao.CourseDAO;
import com.college.dao.TimetableDAO;
import com.college.models.Faculty;
import com.college.models.Course;
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
    private final CourseDAO courseDAO = new CourseDAO();
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
            } else if (path.equals("/api/workload/check-conflict")) {
                if ("GET".equals(method)) handleCheckConflict(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/workload/suggest")) {
                if ("GET".equals(method)) handleSuggestCourses(t);
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
        if (!requirePermission(t, "VIEW_WORKLOAD")) return;
        List<Faculty> allFaculty = facultyDAO.getAllFaculty();
        List<Map<String, Object>> analytics = new ArrayList<>();

        for (Faculty f : allFaculty) {
            CourseDAO.WorkloadStats stats = courseDAO.getFacultyWorkload(f.getId());

            Map<String, Object> map = new HashMap<>();
            map.put("facultyId", f.getId());
            map.put("facultyName", f.getName());
            map.put("department", f.getDepartment());
            map.put("totalClasses", stats.count);
            map.put("uniqueSubjects", stats.count);
            map.put("totalCredits", stats.credits);
            map.put("totalStudents", stats.totalStudents);

            double loadPercentage = Math.min(100.0, (stats.credits / 18.0) * 100);
            map.put("loadPercentage", Math.round(loadPercentage));

            String status = stats.credits < 8 ? "UNDERLOAD" : (stats.credits <= 18 ? "OPTIMAL" : "OVERLOAD");
            map.put("status", status);

            analytics.add(map);
        }

        sendResponse(t, 200, JsonHelper.toJson(analytics));
    }

    private void handleGetFacultyWorkload(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_WORKLOAD")) return;
        String query = t.getRequestURI().getQuery();
        if (query == null || !query.contains("name=")) {
            sendResponse(t, 400, errorJson("Missing faculty name param"));
            return;
        }

        String name = query.split("name=")[1].split("&")[0];
        name = java.net.URLDecoder.decode(name, "UTF-8");

        List<Timetable> classes = timetableDAO.getTimetableByFaculty(name);
        
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

    private void handleCheckConflict(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_WORKLOAD")) return;
        Map<String, String> params = getQueryMap(t);
        int facultyId = getIntParam(params, "facultyId", 0);
        int courseId = getIntParam(params, "courseId", 0);

        if (facultyId <= 0 || courseId <= 0) {
            sendResponse(t, 400, errorJson("facultyId and courseId are required"));
            return;
        }

        Faculty faculty = facultyDAO.getFacultyById(facultyId);
        Course course = courseDAO.getCourseById(courseId);

        if (faculty == null || course == null) {
            sendResponse(t, 404, errorJson("Faculty or course not found"));
            return;
        }

        List<Timetable> facultySchedule = timetableDAO.getTimetableByFaculty(faculty.getName());
        List<Timetable> courseSchedule = timetableDAO.getTimetableBySubject(course.getName());

        List<Map<String, String>> conflicts = new ArrayList<>();
        for (Timetable fEntry : facultySchedule) {
            for (Timetable cEntry : courseSchedule) {
                if (fEntry.getDayOfWeek().equals(cEntry.getDayOfWeek()) &&
                    fEntry.getTimeSlot().equals(cEntry.getTimeSlot())) {
                    Map<String, String> conflict = new HashMap<>();
                    conflict.put("dayOfWeek", fEntry.getDayOfWeek());
                    conflict.put("timeSlot", fEntry.getTimeSlot());
                    conflict.put("existingSubject", fEntry.getSubject());
                    conflicts.add(conflict);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("hasConflict", !conflicts.isEmpty());
        response.put("conflicts", conflicts);
        sendResponse(t, 200, JsonHelper.toJson(response));
    }

    private void handleSuggestCourses(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_WORKLOAD")) return;
        Map<String, String> params = getQueryMap(t);
        int facultyId = getIntParam(params, "facultyId", 0);

        if (facultyId <= 0) {
            sendResponse(t, 400, errorJson("facultyId is required"));
            return;
        }

        Faculty faculty = facultyDAO.getFacultyById(facultyId);
        if (faculty == null) {
            sendResponse(t, 404, errorJson("Faculty not found"));
            return;
        }

        if (faculty.getSpecialization() == null || faculty.getSpecialization().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("suggested", new ArrayList<>());
            response.put("message", "No specialization set for this faculty member");
            sendResponse(t, 200, JsonHelper.toJson(response));
            return;
        }

        // Get all unassigned courses
        List<Course> allCourses = courseDAO.getAllCourses();
        List<Course> unassigned = allCourses.stream()
                .filter(c -> c.getFacultyId() == 0)
                .collect(Collectors.toList());

        // Sort: matching specialization first
        unassigned.sort((a, b) -> {
            boolean aMatch = faculty.getSpecialization().equalsIgnoreCase(a.getSpecialization());
            boolean bMatch = faculty.getSpecialization().equalsIgnoreCase(b.getSpecialization());
            return Boolean.compare(bMatch, aMatch);
        });

        sendResponse(t, 200, JsonHelper.toJson(unassigned));
    }
}
