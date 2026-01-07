package com.college.fx.views;

import com.college.dao.EventDetailsDAO;
import com.college.dao.StudentDAO;
import com.college.models.EventVolunteer;
import com.college.models.Student;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class VolunteerTasksView {

    private final EventDetailsDAO eventDetailsDAO = new EventDetailsDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private TableView<EventVolunteer> table;

    public VBox getView() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("My Volunteer Tasks");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #1e293b;");

        table = new TableView<>();

        TableColumn<EventVolunteer, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventName()));

        TableColumn<EventVolunteer, String> taskCol = new TableColumn<>("Task Description");
        taskCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTaskDescription()));

        TableColumn<EventVolunteer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else if ("COMPLETED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<EventVolunteer, String> hoursCol = new TableColumn<>("Hours Logged");
        hoursCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.valueOf(data.getValue().getHoursLogged())));

        table.getColumns().addAll(eventCol, taskCol, statusCol, hoursCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        refreshTable();

        mainLayout.getChildren().addAll(title, table);
        return mainLayout;
    }

    private void refreshTable() {
        // Get current student ID from user ID
        int userId = SessionManager.getInstance().getUserId();
        Student student = studentDAO.getStudentByUserId(userId);

        if (student != null) {
            table.getItems().setAll(eventDetailsDAO.getVolunteersByStudent(student.getId()));
        }
    }
}
