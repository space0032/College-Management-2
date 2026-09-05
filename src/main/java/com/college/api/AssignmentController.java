package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.AssignmentDAO;
import com.college.dao.SubmissionDAO;
import com.college.models.Assignment;
import com.college.models.Submission;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class AssignmentController extends BaseController implements HttpHandler {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            // Assignment endpoints
            if (path.matches(".*/assignments")) {
                if ("GET".equals(method))
                    handleGetAssignments(t); // Query params for courseId
                else if ("POST".equals(method))
                    handleCreateAssignment(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/assignments/\\d+")) {
                if ("GET".equals(method))
                    handleGetAssignmentById(t, path);
                else if ("PUT".equals(method))
                    handleUpdateAssignment(t, path);
                else if ("DELETE".equals(method))
                    handleDeleteAssignment(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            }
            // Submission endpoints
            else if (path.matches(".*/assignments/\\d+/submissions")) {
                if ("GET".equals(method))
                    handleGetSubmissionsByAssignment(t, path);
                else if ("POST".equals(method))
                    handleSubmitAssignment(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/assignments/\\d+/submissions/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetSubmissionByStudent(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/submissions/\\d+/grade")) {
                if ("PUT".equals(method))
                    handleGradeSubmission(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetAssignments(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_ASSIGNMENT")) return;
        String query = t.getRequestURI().getQuery();
        List<Assignment> list;
        if (query != null && query.contains("studentId=")) {
            int studentId = parseIntParam(query, "studentId", 0);
            list = studentId > 0 ? assignmentDAO.getAssignmentsByStudent(studentId)
                    : assignmentDAO.getAssignmentsBySemester(1);
        } else if (query != null && query.contains("courseIds=")) {
            list = assignmentDAO.getAssignmentsByCourseIds(parseIdList(queryParam(query, "courseIds")));
        } else if (query != null && query.contains("courseId=")) {
            int courseId = Integer.parseInt(query.split("courseId=")[1].split("&")[0]);
            list = assignmentDAO.getAssignmentsByCourse(courseId);
        } else if (query != null && query.contains("facultyId=")) {
            int facultyId = Integer.parseInt(query.split("facultyId=")[1].split("&")[0]);
            list = assignmentDAO.getAssignmentsByFaculty(facultyId);
        } else {
            // fallback, get by semester 1
            list = assignmentDAO.getAssignmentsBySemester(1);
        }
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private static String queryParam(String query, String key) {
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static int parseIntParam(String query, String key, int def) {
        try {
            return Integer.parseInt(queryParam(query, key).split(",")[0].trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static List<Integer> parseIdList(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }
        for (String part : csv.split(",")) {
            try {
                int id = Integer.parseInt(part.trim());
                if (id > 0) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private void handleCreateAssignment(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_ASSIGNMENT")) return;
        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || !(map.get("courseId") instanceof Number)
                || !(map.get("title") instanceof String)
                || !(map.get("dueDate") instanceof String)) {
            sendResponse(t, 400, errorJson("Course, title, and due date are required"));
            return;
        }

        String title = ((String) map.get("title")).trim();
        String dueDateStr = ((String) map.get("dueDate")).trim();
        int courseId = ((Number) map.get("courseId")).intValue();
        int semester = map.get("semester") instanceof Number ? ((Number) map.get("semester")).intValue() : 1;
        if (title.isEmpty() || dueDateStr.isEmpty() || courseId <= 0 || semester < 1 || semester > 8) {
            sendResponse(t, 400, errorJson("Provide a valid course, title, due date, and semester (1-8)"));
            return;
        }

        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(title);
        assignment.setDescription((String) map.get("description"));
        if (dueDateStr.length() == 10) {
            dueDateStr += "T23:59:59"; // default to end of day if only date is provided
        }
        assignment.setDueDate(Timestamp.valueOf(dueDateStr.replace("T", " ")));
        assignment.setCreatedBy(tokenInfo.userId);
        assignment.setSemester(semester);

        boolean ok = assignmentDAO.createAssignment(assignment);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Assignment created successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to create assignment"));
    }

    private void handleGetAssignmentById(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        Assignment assignment = assignmentDAO.getAssignmentById(id);
        if (assignment != null) {
            sendResponse(t, 200, JsonHelper.toJson(assignment));
        } else {
            sendResponse(t, 404, errorJson("Assignment not found"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateAssignment(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        Assignment assignment = assignmentDAO.getAssignmentById(id);
        if (assignment == null) {
            sendResponse(t, 404, errorJson("Assignment not found"));
            return;
        }

        if (map.containsKey("title"))
            assignment.setTitle((String) map.get("title"));
        if (map.containsKey("description"))
            assignment.setDescription((String) map.get("description"));
        if (map.containsKey("dueDate")) {
            String dueDateStr = (String) map.get("dueDate");
            if (dueDateStr.length() == 10) {
                dueDateStr += "T23:59:59";
            }
            assignment.setDueDate(Timestamp.valueOf(dueDateStr.replace("T", " ")));
        }
        if (map.containsKey("semester"))
            assignment.setSemester(((Double) map.get("semester")).intValue());

        boolean ok = assignmentDAO.updateAssignment(assignment);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Assignment updated successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to update assignment"));
    }

    private void handleDeleteAssignment(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int id = Integer.parseInt(parts[parts.length - 1]);
        boolean ok = assignmentDAO.deleteAssignment(id);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Assignment deleted successfully\"}");
        else
            sendResponse(t, 404, errorJson("Assignment not found or delete failed"));
    }

    // --- Submissions ---

    private void handleGetSubmissionsByAssignment(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int assignmentId = Integer.parseInt(parts[parts.length - 2]);
        List<Submission> list = submissionDAO.getSubmissionsByAssignment(assignmentId);
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleSubmitAssignment(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int assignmentId = Integer.parseInt(parts[parts.length - 2]);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        Submission sub = new Submission();
        sub.setAssignmentId(assignmentId);
        int studentId = resolveStudentId(map, map.get("studentId") != null ? ((Number) map.get("studentId")).intValue() : 0);
        if (studentId <= 0) {
            sendResponse(t, 400, errorJson("studentId or enrollmentId is required / unknown student"));
            return;
        }
        sub.setStudentId(studentId);
        sub.setSubmissionText((String) map.getOrDefault("submissionText", ""));

        boolean ok = submissionDAO.submitAssignment(sub);
        if (ok) {
            // Also triggers plagiarism check in SubmissionDAO.submitAssignment logic
            sendResponse(t, 201, "{\"message\":\"Submission successful\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to submit assignment"));
        }
    }

    private void handleGetSubmissionByStudent(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int studentId = resolvePathStudentId(parts[parts.length - 1]);
        int assignmentId = Integer.parseInt(parts[parts.length - 4]); // /assignments/{id}/submissions/student/{stdId}

        Submission sub = submissionDAO.getSubmission(assignmentId, studentId);
        if (sub != null)
            sendResponse(t, 200, JsonHelper.toJson(sub));
        else
            sendResponse(t, 200, "null"); // Not submitted yet
    }

    @SuppressWarnings("unchecked")
    private void handleGradeSubmission(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_ASSIGNMENT")) return;
        String[] parts = path.split("/");
        int submissionId = Integer.parseInt(parts[parts.length - 2]); // /submissions/{id}/grade
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        double grade = ((Double) map.get("grade"));
        String feedback = (String) map.get("feedback");

        boolean ok = submissionDAO.gradeSubmission(submissionId, grade, feedback);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Submission graded successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to grade submission"));
    }
}
