package com.college.fx.views;

import com.college.dao.NotificationDAO;
import com.college.models.Notification;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class NotificationView {

    private final NotificationDAO notificationDAO;
    private final int userId;
    private VBox view;

    public NotificationView() {
        this.notificationDAO = new NotificationDAO();
        this.userId = SessionManager.getInstance().getUserId();
        createView();
    }

    private void createView() {
        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setPrefWidth(400);
        view.setPrefHeight(500);

        Label title = new Label("Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ListView<Notification> listView = new ListView<>();
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(5);
                    card.setPadding(new Insets(10));

                    Label subject = new Label(item.getSubject());
                    subject.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

                    Label message = new Label(item.getMessage());
                    message.setWrapText(true);

                    Label date = new Label(item.getCreatedAt().toString().replace("T", " ").substring(0, 16));
                    // date.setTextFill(Color.GRAY);
                    date.setFont(Font.font("Segoe UI", 10));

                    card.getChildren().addAll(subject, message, date);
                    setGraphic(card);
                }
            }
        });

        List<Notification> notifications = notificationDAO.getPendingNotifications();
        // Filter by user ID (DAO returns all pending, we should filter or DAO should
        // support filtering)
        // Checking NotificationDAO: getPendingNotifications returns ALL pending?
        // Let's check NotificationDAO code again. It selects * from notifications where
        // status = 'PENDING'.
        // It does NOT filter by user. This is a BUG in NotificationDAO as well unless
        // implied.
        // We will filter here for now, but should fix DAO.

        listView.getItems().addAll(notifications.stream()
                .filter(n -> n.getRecipientUserId() == userId)
                .toList());

        view.getChildren().addAll(title, listView);

        if (listView.getItems().isEmpty()) {
            listView.setPlaceholder(new Label("No new notifications"));
        }
    }

    public VBox getView() {
        return view;
    }

    public static void showDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notifications");
        dialog.getDialogPane().setContent(new NotificationView().getView());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
