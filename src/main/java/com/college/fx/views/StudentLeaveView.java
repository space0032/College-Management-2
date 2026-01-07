package com.college.fx.views;

import com.college.dao.StudentLeaveDAO;
import com.college.models.StudentLeave;
import com.college.models.User;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.Date;
import java.time.LocalDate;

public class StudentLeaveView {

    private final StudentLeaveDAO leaveDAO = new StudentLeaveDAO();
    private TableView<StudentLeave> leaveTable;

    public VBox getView() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("Leave Application");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #1e293b;");

        // Application Form
        VBox form = createApplicationForm();

        // History Table
        VBox history = createHistoryTable();

        mainLayout.getChildren().addAll(title, form, new Separator(), history);
        return mainLayout;
    }

    private VBox createApplicationForm() {
        VBox box = new VBox(15);
        box.setStyle(
                "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label formTitle = new Label("Apply for Leave");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setStyle("-fx-text-fill: #334155;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // Inputs
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("SICK", "CASUAL", "EMERGENCY");
        typeCombo.setPromptText("Select Type");
        typeCombo.setPrefWidth(200);

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for leave...");
        reasonArea.setPrefRowCount(3);

        grid.add(new Label("Leave Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(new Label("Reason:"), 0, 3);
        grid.add(reasonArea, 1, 3);
        GridPane.setColumnSpan(reasonArea, 2);

        Button applyBtn = new Button("Submit Request");
        applyBtn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        applyBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null || startDate.getValue() == null || endDate.getValue() == null
                    || reasonArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
                return;
            }
            if (endDate.getValue().isBefore(startDate.getValue())) {
                showAlert(Alert.AlertType.ERROR, "Error", "End date cannot be before start date.");
                return;
            }

            SessionManager session = SessionManager.getInstance();
            if (!session.isLoggedIn())
                return; // Should not happen

            StudentLeave leave = new StudentLeave(
                    session.getUserId(), // Assuming User ID == Student ID link, normally we query student table.
                    // Wait, student_leaves links to users(id) via student_id FK in my schema.
                    // Yes, FK is to users(id). Correct.
                    typeCombo.getValue(),
                    Date.valueOf(startDate.getValue()),
                    Date.valueOf(endDate.getValue()),
                    reasonArea.getText().trim());

            if (leaveDAO.createLeaveRequest(leave)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully.");
                typeCombo.setValue(null);
                startDate.setValue(null);
                endDate.setValue(null);
                reasonArea.clear();
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit request.");
            }
        });

        box.getChildren().addAll(formTitle, grid, applyBtn);
        return box;
    }

    private VBox createHistoryTable() {
        VBox box = new VBox(10);
        Label historyTitle = new Label("My Leave History");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        historyTitle.setStyle("-fx-text-fill: #334155;");

        leaveTable = new TableView<>();

        TableColumn<StudentLeave, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaveType()));

        TableColumn<StudentLeave, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));

        TableColumn<StudentLeave, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));

        TableColumn<StudentLeave, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("APPROVED"))
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else if (item.equals("REJECTED"))
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });

        leaveTable.getColumns().addAll(typeCol, fromCol, toCol, statusCol);
        leaveTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        leaveTable.setPrefHeight(300);

        refreshTable();

        box.getChildren().addAll(historyTitle, leaveTable);
        return box;
    }

    private void refreshTable() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            leaveTable.getItems().setAll(leaveDAO.getLeavesByStudent(session.getUserId()));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
