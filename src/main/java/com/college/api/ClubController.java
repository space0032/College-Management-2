package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.ClubDAO;
import com.college.models.Club;
import com.college.models.ClubMembership;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class ClubController extends BaseController implements HttpHandler {

    private final ClubDAO clubDAO = new ClubDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/clubs/memberships/\\d+/approve")) {
                if ("PUT".equals(method))
                    handleApproveMembership(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/memberships/\\d+/reject")) {
                if ("PUT".equals(method))
                    handleRejectMembership(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/memberships/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetMyMemberships(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/student/\\d+")) {
                if ("GET".equals(method))
                    handleGetStudentClubs(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/\\d+/join")) {
                if ("POST".equals(method))
                    handleJoinClub(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/\\d+/leave")) {
                if ("POST".equals(method))
                    handleLeaveClub(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/\\d+/members")) {
                if ("GET".equals(method))
                    handleGetClubMembers(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/\\d+/pending")) {
                if ("GET".equals(method))
                    handleGetPendingMemberships(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs/\\d+")) {
                if ("GET".equals(method))
                    handleGetClub(t, path);
                else if ("PUT".equals(method))
                    handleUpdateClub(t, path);
                else if ("DELETE".equals(method))
                    handleDeleteClub(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/clubs.*")) {
                if ("GET".equals(method))
                    handleGetClubs(t);
                else if ("POST".equals(method))
                    handleAddClub(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetClubs(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        List<Club> clubs = clubDAO.getAllClubs();
        sendResponse(t, 200, JsonHelper.toJson(clubs));
    }

    private void handleGetClub(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        int id = extractId(path);
        Club club = clubDAO.getClubById(id);
        if (club == null) {
            sendResponse(t, 404, errorJson("Club not found"));
            return;
        }
        sendResponse(t, 200, JsonHelper.toJson(club));
    }

    private void handleAddClub(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_CLUB")) return;
        String body = readBody(t);
        Club club = JsonHelper.fromJson(body, Club.class);
        if (club == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean success = clubDAO.createClub(club);
        if (success) {
            sendResponse(t, 201, "{\"message\":\"Club created successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to create club"));
        }
    }

    private void handleUpdateClub(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_CLUB")) return;
        int id = extractId(path);
        String body = readBody(t);
        Club club = JsonHelper.fromJson(body, Club.class);
        if (club == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        club.setId(id);
        boolean success = clubDAO.updateClub(club);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Club updated successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to update club"));
        }
    }

    private void handleDeleteClub(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_CLUB")) return;
        int id = extractId(path);
        boolean success = clubDAO.deleteClub(id);
        if (success) {
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to delete club"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleJoinClub(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_CLUB")) return;
        String[] parts = path.split("/");
        int clubId = Integer.parseInt(parts[parts.length - 2]); // .../clubs/{id}/join
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("studentId") == null) {
            sendResponse(t, 400, errorJson("studentId is required"));
            return;
        }
        int studentId = ((Double) map.get("studentId")).intValue();

        if (clubDAO.isStudentMember(clubId, studentId)) {
            sendResponse(t, 400, errorJson("Already a member or request pending"));
            return;
        }
        boolean success = clubDAO.joinClub(clubId, studentId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Join request sent successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to send join request"));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleLeaveClub(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_CLUB")) return;
        String[] parts = path.split("/");
        int clubId = Integer.parseInt(parts[parts.length - 2]); // .../clubs/{id}/leave
        String body = readBody(t);
        java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(body, java.util.Map.class);
        if (map == null || map.get("studentId") == null) {
            sendResponse(t, 400, errorJson("studentId is required"));
            return;
        }
        int studentId = ((Double) map.get("studentId")).intValue();

        boolean success = clubDAO.leaveClub(clubId, studentId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Left club successfully\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to leave club"));
        }
    }

    private void handleGetClubMembers(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        String[] parts = path.split("/");
        int clubId = Integer.parseInt(parts[parts.length - 2]); // .../clubs/{id}/members
        List<ClubMembership> members = clubDAO.getClubMembers(clubId);
        sendResponse(t, 200, JsonHelper.toJson(members));
    }

    private void handleGetPendingMemberships(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        String[] parts = path.split("/");
        int clubId = Integer.parseInt(parts[parts.length - 2]); // .../clubs/{id}/pending
        List<ClubMembership> pending = clubDAO.getPendingMemberships(clubId);
        sendResponse(t, 200, JsonHelper.toJson(pending));
    }

    private void handleApproveMembership(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_CLUB")) return;
        String[] parts = path.split("/");
        int membershipId = Integer.parseInt(parts[parts.length - 2]); // .../memberships/{id}/approve
        boolean success = clubDAO.approveMembership(membershipId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Membership approved\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to approve membership"));
        }
    }

    private void handleRejectMembership(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_CLUB")) return;
        String[] parts = path.split("/");
        int membershipId = Integer.parseInt(parts[parts.length - 2]); // .../memberships/{id}/reject
        boolean success = clubDAO.rejectMembership(membershipId);
        if (success) {
            sendResponse(t, 200, "{\"message\":\"Membership rejected\"}");
        } else {
            sendResponse(t, 400, errorJson("Failed to reject membership"));
        }
    }

    private void handleGetStudentClubs(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        int studentId = extractId(path); // .../clubs/student/{id}
        List<Club> clubs = clubDAO.getStudentClubs(studentId);
        sendResponse(t, 200, JsonHelper.toJson(clubs));
    }

    private void handleGetMyMemberships(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "VIEW_CLUB")) return;
        int studentId = extractId(path); // .../memberships/student/{id}
        List<ClubMembership> memberships = clubDAO.getMyMemberships(studentId);
        sendResponse(t, 200, JsonHelper.toJson(memberships));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
