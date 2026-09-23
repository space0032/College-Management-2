package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.StaffLeaveDAO;
import com.college.dao.StudentLeaveDAO;
import com.college.models.StaffLeave;
import com.college.models.StudentLeave;
import com.college.utils.JsonHelper;
import com.college.utils.PermissionService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeaveController extends BaseController implements HttpHandler {

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED", "CANCELLED");

    private final StaffLeaveDAO staffLeaveDAO = new StaffLeaveDAO();
    private final StudentLeaveDAO studentLeaveDAO = new StudentLeaveDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.equals("/api/leaves/staff")) {
                if ("GET".equals(method)) handleGetStaffLeaves(t);
                else if ("POST".equals(method)) handleCreateStaffLeave(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/leaves/student")) {
                if ("GET".equals(method)) handleGetStudentLeaves(t);
                else if ("POST".equals(method)) handleCreateStudentLeave(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches("/api/leaves/staff/\\d+/status")) {
                if ("PUT".equals(method)) handleUpdateStaffLeaveStatus(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches("/api/leaves/student/\\d+/status")) {
                if ("PUT".equals(method)) handleUpdateStudentLeaveStatus(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.equals("/api/leaves/pending")) {
                if ("GET".equals(method)) handleGetAllPendingLeaves(t);
                else sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (NumberFormatException e) {
            sendResponse(t, 400, errorJson("Invalid numeric path parameter"));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson("Internal server error"));
        }
    }

    /**
     * Staff leave history. STUDENT accounts never have staff leaves, and a
     * viewer who only holds VIEW_LEAVE is restricted to their own records.
     * Approvers (UPDATE_LEAVE) and admins may read any staff member's history.
     */
    private void handleGetStaffLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LEAVE")) return;
        TokenStore.TokenInfo token = getTokenInfo(t);
        if (token == null) return;

        if ("STUDENT".equalsIgnoreCase(token.role)) {
            sendResponse(t, 403, errorJson("Forbidden: Students do not have staff leave records"));
            return;
        }
        int requestedUserId = Integer.parseInt(t.getRequestURI().getQuery().split("userId=")[1].split("&")[0]);
        if (requestedUserId != token.userId
                && !PermissionService.getInstance().hasPermission(token.userId, "UPDATE_LEAVE")) {
            sendResponse(t, 403, errorJson("Forbidden: You may only view your own leave records"));
            return;
        }
        List<StaffLeave> leaves = staffLeaveDAO.getLeavesByUser(requestedUserId);
        sendResponse(t, 200, JsonHelper.toJson(leaves));
    }

    /**
     * Student leave history. A STUDENT token is always scoped to their own
     * record (its student_id is their own users.id). Approvers (UPDATE_LEAVE)
     * and admins may read any student's history; other staff see only their own.
     */
    private void handleGetStudentLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LEAVE")) return;
        String query = t.getRequestURI().getQuery();
        if (query == null || !query.contains("studentId=")) {
            sendResponse(t, 400, errorJson("Missing studentId parameter"));
            return;
        }
        int requestedUserId = Integer.parseInt(query.split("studentId=")[1].split("&")[0]);
        TokenStore.TokenInfo token = getTokenInfo(t);
        if (token == null) return;

        if ("STUDENT".equalsIgnoreCase(token.role)) {
            requestedUserId = token.userId; // IDOR guard: always own data
        } else if (requestedUserId != token.userId
                && !PermissionService.getInstance().hasPermission(token.userId, "UPDATE_LEAVE")) {
            sendResponse(t, 403, errorJson("Forbidden: You may only view your own leave records"));
            return;
        }
        List<StudentLeave> leaves = studentLeaveDAO.getLeavesByStudent(requestedUserId);
        sendResponse(t, 200, JsonHelper.toJson(leaves));
    }

    /**
     * Approved/rejected only for approvers (UPDATE_LEAVE path). Reading the
     * full pending inbox requires the approver permission, not just VIEW_LEAVE.
     */
    private void handleGetAllPendingLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_LEAVE")) return;
        List<StaffLeave> staffLeaves = staffLeaveDAO.getAllPendingLeaves();
        List<StudentLeave> studentLeaves = studentLeaveDAO.getPendingLeaves();

        Map<String, Object> response = new HashMap<>();
        response.put("staff", staffLeaves);
        response.put("students", studentLeaves);

        sendResponse(t, 200, JsonHelper.toJson(response));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateStaffLeave(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_LEAVE")) return;
        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || !(map.get("leaveType") instanceof String)
                || !(map.get("startDate") instanceof String)
                || !(map.get("endDate") instanceof String)
                || !(map.get("reason") instanceof String)) {
            sendResponse(t, 400, errorJson("Leave type, dates, and reason are required"));
            return;
        }

        LocalDate startDate = LocalDate.parse((String) map.get("startDate"));
        LocalDate endDate = LocalDate.parse((String) map.get("endDate"));
        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(startDate)) {
            sendResponse(t, 400, errorJson("Provide a valid current or future leave date range"));
            return;
        }

        StaffLeave leave = new StaffLeave();
        leave.setUserId(tokenInfo.userId);
        leave.setLeaveType((String) map.get("leaveType"));
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setReason(((String) map.get("reason")).trim());
        if (leave.getReason().isEmpty()) {
            sendResponse(t, 400, errorJson("Reason is required"));
            return;
        }
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now());

        boolean ok = staffLeaveDAO.createLeaveRequest(leave);
        if (ok) sendResponse(t, 201, "{\"message\":\"Leave request created successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to create leave request"));
    }

    /**
     * Student leave creation. The student_id column stores a users.id; a
     * STUDENT token is always tied to their own account so the supplied value
     * is ignored (IDOR guard). Staff acting on a student's behalf may supply
     * an explicit studentId/enrollment, but the request is validated first.
     */
    @SuppressWarnings("unchecked")
    private void handleCreateStudentLeave(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_LEAVE")) return;
        TokenStore.TokenInfo tokenInfo = getTokenInfo(t);
        if (tokenInfo == null) return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || !(map.get("leaveType") instanceof String)
                || !(map.get("startDate") instanceof String)
                || !(map.get("endDate") instanceof String)
                || !(map.get("reason") instanceof String)) {
            sendResponse(t, 400, errorJson("Leave type, dates, and reason are required"));
            return;
        }

        int subjectUserId;
        if ("STUDENT".equalsIgnoreCase(tokenInfo.role)) {
            subjectUserId = tokenInfo.userId;
        } else {
            Object enrolled = map.get("enrollmentId");
            if (enrolled != null && String.valueOf(enrolled).trim().length() > 0) {
                subjectUserId = new com.college.dao.StudentDAO().getUserIdByEnrollment(String.valueOf(enrolled).trim());
                if (subjectUserId <= 0) {
                    sendResponse(t, 400, errorJson("Unknown student for the given enrollmentId"));
                    return;
                }
            } else if (map.get("studentId") != null) {
                int sid = ((Number) map.get("studentId")).intValue();
                if (sid <= 0) {
                    sendResponse(t, 400, errorJson("Invalid studentId"));
                    return;
                }
                subjectUserId = sid;
            } else {
                sendResponse(t, 400, errorJson("studentId or enrollmentId is required"));
                return;
            }
        }

        String startVal = (String) map.get("startDate");
        String endVal = (String) map.get("endDate");
        LocalDate startDate = LocalDate.parse(startVal);
        LocalDate endDate = LocalDate.parse(endVal);
        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(startDate)) {
            sendResponse(t, 400, errorJson("Provide a valid current or future leave date range"));
            return;
        }
        String reason = ((String) map.get("reason")).trim();
        if (reason.isEmpty()) {
            sendResponse(t, 400, errorJson("Reason is required"));
            return;
        }

        StudentLeave leave = new StudentLeave();
        leave.setStudentId(subjectUserId);
        leave.setLeaveType((String) map.get("leaveType"));
        leave.setStartDate(java.sql.Date.valueOf(startVal));
        leave.setEndDate(java.sql.Date.valueOf(endVal));
        leave.setReason(reason);

        boolean ok = studentLeaveDAO.createLeaveRequest(leave);
        if (ok) sendResponse(t, 201, "{\"message\":\"Leave request created successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to create leave request"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateStaffLeaveStatus(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_LEAVE")) return;
        int id = Integer.parseInt(t.getRequestURI().getPath().split("/")[4]);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || map.get("status") == null) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        String status = String.valueOf(map.get("status")).toUpperCase();
        if (!VALID_STATUSES.contains(status)) {
            sendResponse(t, 400, errorJson("Invalid status (expected PENDING, APPROVED, REJECTED or CANCELLED)"));
            return;
        }
        int approvingUser = getTokenInfo(t).userId;
        int owner = staffLeaveDAO.getLeaveOwnerUserId(id);
        if (owner > 0 && owner == approvingUser) {
            sendResponse(t, 403, errorJson("Forbidden: You cannot approve or reject your own leave request"));
            return;
        }
        String comments = map.containsKey("comments") && map.get("comments") != null ? String.valueOf(map.get("comments")) : null;

        boolean ok = staffLeaveDAO.updateLeaveStatus(id, status, approvingUser, comments);
        if (ok) sendResponse(t, 200, "{\"message\":\"Leave status updated\"}");
        else sendResponse(t, 400, errorJson("Failed to update status"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateStudentLeaveStatus(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_LEAVE")) return;
        int id = Integer.parseInt(t.getRequestURI().getPath().split("/")[4]);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        if (map == null || map.get("status") == null) {
            sendResponse(t, 400, errorJson("status is required"));
            return;
        }
        String status = String.valueOf(map.get("status")).toUpperCase();
        if (!VALID_STATUSES.contains(status)) {
            sendResponse(t, 400, errorJson("Invalid status (expected PENDING, APPROVED, REJECTED or CANCELLED)"));
            return;
        }
        int approvingUser = getTokenInfo(t).userId;
        int owner = studentLeaveDAO.getLeaveOwnerUserId(id);
        if (owner > 0 && owner == approvingUser) {
            sendResponse(t, 403, errorJson("Forbidden: You cannot approve or reject your own leave request"));
            return;
        }

        boolean ok = studentLeaveDAO.updateLeaveStatus(id, status, approvingUser);
        if (ok) sendResponse(t, 200, "{\"message\":\"Leave status updated\"}");
        else sendResponse(t, 400, errorJson("Failed to update status"));
    }
}