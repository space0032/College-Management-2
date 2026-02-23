package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.NotificationDAO;
import com.college.models.Notification;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;

public class NotificationController extends BaseController implements HttpHandler {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t)) return;

        String method = t.getRequestMethod();

        try {
            if ("GET".equals(method)) handleGet(t);
            else if ("POST".equals(method)) handlePost(t);
            else sendResponse(t, 405, errorJson("Method not allowed"));
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGet(HttpExchange t) throws IOException {
        List<Notification> list = notificationDAO.getPendingNotifications();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    private void handlePost(HttpExchange t) throws IOException {
        String body = readBody(t);
        Notification notification = JsonHelper.fromJson(body, Notification.class);
        if (notification == null) {
            sendResponse(t, 400, errorJson("Invalid JSON"));
            return;
        }
        boolean ok = notificationDAO.createNotification(notification);
        if (ok) sendResponse(t, 201, "{\"status\":\"Notification created\"}");
        else sendResponse(t, 400, errorJson("Failed to create notification"));
    }
}
