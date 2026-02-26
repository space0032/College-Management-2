package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.StaffLeaveDAO;
import com.college.dao.StudentLeaveDAO;
import com.college.models.StaffLeave;
import com.college.models.StudentLeave;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveController extends BaseController implements HttpHandler {

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
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetStaffLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LEAVE")) return;
        String query = t.getRequestURI().getQuery();
        if (query != null && query.contains("userId=")) {
            int userId = Integer.parseInt(query.split("userId=")[1].split("&")[0]);
            List<StaffLeave> leaves = staffLeaveDAO.getLeavesByUser(userId);
            sendResponse(t, 200, JsonHelper.toJson(leaves));
        } else {
            sendResponse(t, 400, errorJson("Missing userId parameter"));
        }
    }

    private void handleGetStudentLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LEAVE")) return;
        String query = t.getRequestURI().getQuery();
        if (query != null && query.contains("studentId=")) {
            int studentId = Integer.parseInt(query.split("studentId=")[1].split("&")[0]);
            List<StudentLeave> leaves = studentLeaveDAO.getLeavesByStudent(studentId);
            sendResponse(t, 200, JsonHelper.toJson(leaves));
        } else {
            sendResponse(t, 400, errorJson("Missing studentId parameter"));
        }
    }

    private void handleGetAllPendingLeaves(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_LEAVE")) return;
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
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        StaffLeave leave = new StaffLeave();
        leave.setUserId(((Double) map.get("userId")).intValue());
        leave.setLeaveType((String) map.get("leaveType"));
        leave.setStartDate(LocalDate.parse((String) map.get("startDate")));
        leave.setEndDate(LocalDate.parse((String) map.get("endDate")));
        leave.setReason((String) map.get("reason"));
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now());
        
        boolean ok = staffLeaveDAO.createLeaveRequest(leave);
        if (ok) sendResponse(t, 201, "{\"message\":\"Leave request created successfully\"}");
        else sendResponse(t, 400, errorJson("Failed to create leave request"));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateStudentLeave(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_LEAVE")) return;
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        StudentLeave leave = new StudentLeave();
        leave.setStudentId(((Double) map.get("studentId")).intValue());
        leave.setLeaveType((String) map.get("leaveType"));
        leave.setStartDate(java.sql.Date.valueOf((String) map.get("startDate")));
        leave.setEndDate(java.sql.Date.valueOf((String) map.get("endDate")));
        leave.setReason((String) map.get("reason"));
        
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
        
        String status = (String) map.get("status");
        int approvedBy = ((Double) map.get("approvedBy")).intValue();
        String comments = map.containsKey("comments") ? (String) map.get("comments") : null;
        
        boolean ok = staffLeaveDAO.updateLeaveStatus(id, status, approvedBy, comments);
        if (ok) sendResponse(t, 200, "{\"message\":\"Leave status updated\"}");
        else sendResponse(t, 400, errorJson("Failed to update status"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpdateStudentLeaveStatus(HttpExchange t) throws IOException {
        if (!requirePermission(t, "UPDATE_LEAVE")) return;
        int id = Integer.parseInt(t.getRequestURI().getPath().split("/")[4]);
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);
        
        String status = (String) map.get("status");
        int approvedBy = ((Double) map.get("approvedBy")).intValue();
        
        boolean ok = studentLeaveDAO.updateLeaveStatus(id, status, approvedBy);
        if (ok) sendResponse(t, 200, "{\"message\":\"Leave status updated\"}");
        else sendResponse(t, 400, errorJson("Failed to update status"));
    }
}
