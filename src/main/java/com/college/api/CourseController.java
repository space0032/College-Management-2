package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CourseDAO;
import com.college.dao.FacultyDAO;
import com.college.dao.TimetableDAO;
import com.college.dao.NotificationDAO;
import com.college.models.Course;
import com.college.models.Faculty;
import com.college.models.Notification;
import com.college.models.Timetable;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class CourseController extends BaseController implements HttpHandler {

    private final CourseDAO courseDAO = new CourseDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        // /api/courses/{id}/assign
        if (path.matches(".*/courses/\\d+/assign")) {
            int courseId = extractId(path);
            if ("POST".equals(method)) {
                handleAssignFaculty(t, courseId);
            } else if ("DELETE".equals(method)) {
                handleUnassignFaculty(t, courseId);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        } else if (path.matches(".*/courses/\\d+")) {
            int id = extractId(path);
            if ("GET".equals(method)) {
                handleGetById(t, id);
            } else if ("PUT".equals(method)) {
                handleUpdate(t, id);
            } else if ("DELETE".equals(method)) {
                handleDelete(t, id);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        } else {
            if ("GET".equals(method)) {
                handleGetAll(t);
            } else if ("POST".equals(method)) {
                handleCreate(t);
            } else {
                sendResponse(t, 405, errorJson("Method Not Allowed"));
            }
        }
    }

    private void handleGetAll(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_COURSE"))
            return;
        try {
            java.util.Map<String, String> params = getQueryMap(t);
            int page = getIntParam(params, "page", 1);
            int size = getIntParam(params, "size", 10);
            String search = params.getOrDefault("search", params.getOrDefault("q", "")).trim();

            List<Course> list;
            int total;
            if (!search.isEmpty()) {
                list = courseDAO.searchCoursesPaginated(search, page, size);
                total = courseDAO.countSearch(search);
            } else {
                list = courseDAO.getAllCoursesPaginated(page, size);
                total = courseDAO.getTotalCount();
            }

            t.getResponseHeaders().add("X-Total-Count", String.valueOf(total));
            t.getResponseHeaders().add("Access-Control-Expose-Headers", "X-Total-Count");

            sendResponse(t, 200, JsonHelper.toJson(list));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleGetById(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "VIEW_COURSE"))
            return;
        try {
            Course c = courseDAO.getCourseById(id);
            if (c == null) {
                sendResponse(t, 404, errorJson("Course not found"));
            } else {
                sendResponse(t, 200, JsonHelper.toJson(c));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_COURSE"))
            return;
        try {
            String body = readBody(t);
            Course c = JsonHelper.fromJson(body, Course.class);
            if (c == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            boolean ok = courseDAO.addCourse(c);
            sendResponse(t, ok ? 201 : 400, ok ? JsonHelper.toJson(c) : errorJson("Failed to create course"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "UPDATE_COURSE"))
            return;
        try {
            String body = readBody(t);
            Course c = JsonHelper.fromJson(body, Course.class);
            if (c == null) {
                sendResponse(t, 400, errorJson("Invalid request body"));
                return;
            }
            c.setId(id);
            boolean ok = courseDAO.updateCourse(c);
            sendResponse(t, ok ? 200 : 400, ok ? JsonHelper.toJson(c) : errorJson("Update failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange t, int id) throws IOException {
        if (!requirePermission(t, "DELETE_COURSE"))
            return;
        try {
            boolean ok = courseDAO.deleteCourse(id);
            sendResponse(t, ok ? 200 : 400, ok ? "{\"status\":\"Deleted\"}" : errorJson("Delete failed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    // ===== Assign / Unassign Faculty =====

    @SuppressWarnings("unchecked")
    private void handleAssignFaculty(HttpExchange t, int courseId) throws IOException {
        if (!requirePermission(t, "UPDATE_COURSE")) return;

        try {
            String body = readBody(t);
            Map<String, Object> bodyMap = JSON.fromJson(body, Map.class);
            int facultyId = bodyMap.get("facultyId") != null ? ((Number) bodyMap.get("facultyId")).intValue() : 0;

            if (facultyId <= 0) {
                sendResponse(t, 400, errorJson("facultyId is required"));
                return;
            }

            Faculty faculty = facultyDAO.getFacultyById(facultyId);
            if (faculty == null) {
                com.college.utils.Logger.warn("Assign failed: faculty " + facultyId + " not found for course " + courseId);
                sendResponse(t, 404, errorJson("Faculty not found (id " + facultyId + ")"));
                return;
            }

            Course course = courseDAO.getCourseById(courseId);
            if (course == null) {
                com.college.utils.Logger.warn("Assign failed: course " + courseId + " not found for faculty " + facultyId);
                sendResponse(t, 404, errorJson("Course not found (id " + courseId + ")"));
                return;
            }

            // Check for time conflicts via timetable (null-safe; only exact matches block).
            List<Timetable> facultySchedule = timetableDAO.getTimetableByFaculty(faculty.getName());
            List<Timetable> courseSchedule = timetableDAO.getTimetableBySubject(course.getName());
            if (facultySchedule == null) facultySchedule = new ArrayList<>();
            if (courseSchedule == null) courseSchedule = new ArrayList<>();
            for (Timetable fEntry : facultySchedule) {
                if (fEntry == null || fEntry.getDayOfWeek() == null || fEntry.getTimeSlot() == null) continue;
                for (Timetable cEntry : courseSchedule) {
                    if (cEntry == null || cEntry.getDayOfWeek() == null || cEntry.getTimeSlot() == null) continue;
                    if (fEntry.getDayOfWeek().equals(cEntry.getDayOfWeek()) &&
                        fEntry.getTimeSlot().equals(cEntry.getTimeSlot())) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("conflict", true);
                        conflict.put("message", "Time conflict: Faculty is already busy on " + fEntry.getDayOfWeek() + " at " + fEntry.getTimeSlot() + " (" + fEntry.getSubject() + ")");
                        conflict.put("dayOfWeek", fEntry.getDayOfWeek());
                        conflict.put("timeSlot", fEntry.getTimeSlot());
                        sendResponse(t, 409, JsonHelper.toJson(conflict));
                        return;
                    }
                }
            }

            // Reject re-assigning to the same faculty so retries are explicit.
            if (course.getFacultyId() == facultyId) {
                sendResponse(t, 409, errorJson("Course is already assigned to this faculty"));
                return;
            }

            boolean ok = courseDAO.assignFaculty(courseId, facultyId);
            if (ok) {
                // Best-effort notification: assignment already persisted.
                try {
                    if (faculty.getEmail() != null && !faculty.getEmail().isEmpty()) {
                        Notification note = new Notification(
                            faculty.getUserId(),
                            faculty.getEmail(),
                            Notification.Type.EMAIL,
                            "New Course Assignment: " + course.getName(),
                            "Dear " + faculty.getName() + ",\n\nYou have been assigned the course: " + course.getCode() + " - " + course.getName() + ".\nCredits: " + course.getCredits() + "\n\nPlease check your schedule."
                        );
                        notificationDAO.createNotification(note);
                    }
                } catch (Exception notifyEx) {
                    com.college.utils.Logger.warn("Assignment notification failed for course " + courseId + ": " + notifyEx.getMessage());
                }
                sendResponse(t, 200, "{\"message\":\"Faculty assigned to course successfully\"}");
            } else {
                com.college.utils.Logger.warn("Assign failed: DAO returned false for course " + courseId + " faculty " + facultyId);
                sendResponse(t, 400, errorJson("Failed to assign faculty (course " + courseId + ", faculty " + facultyId + ": no rows updated)"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private void handleUnassignFaculty(HttpExchange t, int courseId) throws IOException {
        if (!requirePermission(t, "UPDATE_COURSE")) return;

        try {
            boolean ok = courseDAO.assignFaculty(courseId, 0);
            sendResponse(t, ok ? 200 : 400, ok ? "{\"message\":\"Faculty unassigned successfully\"}" : errorJson("Failed to unassign faculty"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage()));
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
