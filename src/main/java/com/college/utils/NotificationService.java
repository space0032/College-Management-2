package com.college.utils;

import com.college.dao.NotificationDAO;
import com.college.models.Notification;

import java.util.List;

public class NotificationService {
    private final NotificationDAO dao = new NotificationDAO();

    public void queueEmail(int userId, String email, String subject, String body) {
        Notification note = new Notification(userId, email, Notification.Type.EMAIL, subject, body);
        dao.createNotification(note);
        System.out.println("Notification queued: " + subject + " -> " + email);
    }

    public void processPending() {
        System.out.println("Processing pending notifications...");
        List<Notification> pending = dao.getPendingNotifications();
        for (Notification n : pending) {
            try {
                // Simulate sending logic
                sendReal(n);
                dao.updateStatus(n.getId(), Notification.Status.SENT);
            } catch (Exception e) {
                System.err.println("Failed to send notification ID " + n.getId());
                dao.updateStatus(n.getId(), Notification.Status.FAILED);
            }
        }
    }

    private void sendReal(Notification n) {
        // Here we would integrate JavaMailSender or Twilio
        // For now, we simulate success
        System.out.println("SENDING [" + n.getType() + "] to " + n.getRecipientContact() + ": " + n.getMessage());
    }
}
