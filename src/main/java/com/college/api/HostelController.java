package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.HostelDAO;
import com.college.models.Hostel;
import com.college.models.Room;
import com.college.models.HostelAllocation;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class HostelController extends BaseController implements HttpHandler {

    private final HostelDAO hostelDAO = new HostelDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/hostels/allocations/\\d+")) {
                if ("DELETE".equals(method))
                    handleVacate(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/hostels/allocations.*")) {
                if ("GET".equals(method))
                    handleGetAllocations(t);
                else if ("POST".equals(method))
                    handleAllocate(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/hostels/rooms/\\d+")) {
                if ("DELETE".equals(method))
                    handleDeleteRoom(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/hostels/rooms.*")) {
                if ("GET".equals(method))
                    handleGetRooms(t);
                else if ("POST".equals(method))
                    handleAddRoom(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/hostels/\\d+")) {
                if ("PUT".equals(method))
                    handleUpdateHostel(t, path);
                else if ("DELETE".equals(method))
                    handleDeleteHostel(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                if ("GET".equals(method))
                    handleGetHostels(t);
                else if ("POST".equals(method))
                    handleAddHostel(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetHostels(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL")) return;
        List<Hostel> hostels = hostelDAO.getAllHostels();
        sendResponse(t, 200, JsonHelper.toJson(hostels));
    }

    private void handleAddHostel(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_HOSTEL")) return;
        String body = readBody(t);
        Hostel hostel = JsonHelper.fromJson(body, Hostel.class);
        if (hostel == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = hostelDAO.addHostel(hostel);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(hostel));
        else
            sendResponse(t, 400, errorJson("Failed to add hostel"));
    }

    private void handleGetRooms(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL")) return;
        List<Room> rooms = hostelDAO.getAllRooms();
        sendResponse(t, 200, JsonHelper.toJson(rooms));
    }

    private void handleUpdateHostel(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "UPDATE_HOSTEL")) return;
        Hostel hostel = JsonHelper.fromJson(readBody(t), Hostel.class);
        if (hostel == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        hostel.setId(extractId(path));
        boolean ok = hostelDAO.updateHostel(hostel);
        sendResponse(t, ok ? 200 : 400, ok ? JsonHelper.toJson(hostel) : errorJson("Failed to update hostel"));
    }

    private void handleAddRoom(HttpExchange t) throws IOException {
        if (!requirePermission(t, "CREATE_HOSTEL")) return;
        String body = readBody(t);
        Room room = JsonHelper.fromJson(body, Room.class);
        if (room == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = hostelDAO.addRoom(room);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(room));
        else
            sendResponse(t, 400, errorJson("Failed to add room"));
    }

    private void handleGetAllocations(HttpExchange t) throws IOException {
        if (!requirePermission(t, "VIEW_HOSTEL")) return;
        List<HostelAllocation> allocations = hostelDAO.getAllActiveAllocations();
        sendResponse(t, 200, JsonHelper.toJson(allocations));
    }

    private void handleAllocate(HttpExchange t) throws IOException {
        if (!requirePermission(t, "MANAGE_HOSTEL")) return;
        String body = readBody(t);
        HostelAllocation allocation = JsonHelper.fromJson(body, HostelAllocation.class);
        if (allocation == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = hostelDAO.allocateRoom(allocation);
        if (ok)
            sendResponse(t, 201, JsonHelper.toJson(allocation));
        else
            sendResponse(t, 400, errorJson("Failed to allocate room"));
    }

    private void handleVacate(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "MANAGE_HOSTEL")) return;
        int id = extractId(path);
        boolean ok = hostelDAO.vacateRoom(id);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Vacated\"}");
        else
            sendResponse(t, 400, errorJson("Failed to vacate room"));
    }

    private void handleDeleteHostel(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_HOSTEL")) return;
        int id = extractId(path);
        boolean ok = hostelDAO.deleteHostel(id);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else
            sendResponse(t, 400, errorJson("Failed to delete hostel. Ensure it has no rooms."));
    }

    private void handleDeleteRoom(HttpExchange t, String path) throws IOException {
        if (!requirePermission(t, "DELETE_HOSTEL")) return;
        int id = extractId(path);
        boolean ok = hostelDAO.deleteRoom(id);
        if (ok)
            sendResponse(t, 200, "{\"status\":\"Deleted\"}");
        else
            sendResponse(t, 400, errorJson("Failed to delete room. Ensure it is unoccupied."));
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
