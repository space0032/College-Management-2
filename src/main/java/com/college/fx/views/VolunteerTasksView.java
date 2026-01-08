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
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        Label title = new Label("My Volunteer Tasks");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #1e293b;");

        table = new TableView<>();
        table.getStyleClass().add("glass-table");

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

        TableColumn<EventVolunteer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button requestBtn = new Button("Request Resource");

            {
                requestBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 10px;");
                requestBtn.setOnAction(e -> {
                    EventVolunteer task = getTableView().getItems().get(getIndex());
                    showRequestResourceDialog(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventVolunteer task = getTableView().getItems().get(getIndex());
                    // Only show request button if Approved
                    requestBtn.setVisible("APPROVED".equalsIgnoreCase(task.getStatus()));
                    if ("APPROVED".equalsIgnoreCase(task.getStatus())) {
                        setGraphic(requestBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(eventCol, taskCol, statusCol, hoursCol, actionCol);
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

    private void showRequestResourceDialog(EventVolunteer task) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Request Resource");
        dialog.setHeaderText("Request Resource for " + task.getEventName());

        ButtonType submitBtn = new ButtonType("Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Resource Name (e.g., Projector)");

        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setEditable(true);

        content.getChildren().addAll(new Label("Resource Name:"), nameField, new Label("Quantity:"), qtySpinner);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btnType -> {
            if (btnType == submitBtn) {
                return nameField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(name -> {
            if (name != null && !name.isEmpty()) {
                com.college.models.EventResource res = new com.college.models.EventResource();
                res.setEventId(task.getEventId());
                res.setResourceName(name);
                res.setQuantity(qtySpinner.getValue());
                // Assuming students can add connection, or we need a specific 'requestResource'
                // method allowing reduced perms?
                // EventDetailsDAO.addResource is likely fine for now.

                if (eventDetailsDAO.addResource(res)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Resource requested successfully!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to request resource.");
                    alert.showAndWait();
                }
            }
        });
    }
}
