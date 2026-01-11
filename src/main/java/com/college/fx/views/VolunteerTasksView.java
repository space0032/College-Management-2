package com.college.fx.views;

import com.college.dao.EventDAO;
import com.college.dao.EventDetailsDAO;
import com.college.dao.StudentDAO;
import com.college.models.Event;
import com.college.models.EventVolunteer;
import com.college.models.Student;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.stream.Collectors;

public class VolunteerTasksView {

    private final EventDetailsDAO eventDetailsDAO = new EventDetailsDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private TableView<EventVolunteer> myTasksTable;
    private TableView<Event> opportunitiesTable;

    public VBox getView() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        Label title = new Label("Volunteer Portal");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #e2e8f0;");

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("pill-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab myTasksTab = new Tab("My Tasks");
        myTasksTab.setContent(createMyTasksTab());

        Tab browseTab = new Tab("Browse Opportunities");
        browseTab.setContent(createBrowseTab());

        tabPane.getTabs().addAll(myTasksTab, browseTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        mainLayout.getChildren().addAll(title, tabPane);
        return mainLayout;
    }

    private VBox createMyTasksTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        myTasksTable = new TableView<>();
        myTasksTable.getStyleClass().add("glass-table");

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
                    requestBtn.setVisible("APPROVED".equalsIgnoreCase(task.getStatus()));
                    if ("APPROVED".equalsIgnoreCase(task.getStatus())) {
                        setGraphic(requestBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        myTasksTable.getColumns().addAll(java.util.Arrays.asList(eventCol, taskCol, statusCol, hoursCol, actionCol));
        myTasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(myTasksTable, Priority.ALWAYS);

        content.getChildren().add(myTasksTable);
        refreshMyTasks();
        return content;
    }

    private VBox createBrowseTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        opportunitiesTable = new TableView<>();
        opportunitiesTable.getStyleClass().add("glass-table");
        opportunitiesTable.setPlaceholder(new Label("No upcoming events found."));

        TableColumn<Event, String> eventNameCol = new TableColumn<>("Event Name");
        eventNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        eventNameCol.setPrefWidth(200);

        TableColumn<Event, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventType()));
        typeCol.setPrefWidth(120);

        TableColumn<Event, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartTime() != null ? data.getValue().getStartTime().toString() : "TBA"));
        dateCol.setPrefWidth(150);

        TableColumn<Event, Void> applyCol = new TableColumn<>("Action");
        applyCol.setCellFactory(param -> new TableCell<>() {
            private final Button volunteerBtn = new Button("Volunteer");

            {
                volunteerBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
                volunteerBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showVolunteerDialog(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(volunteerBtn);
                }
            }
        });

        opportunitiesTable.getColumns().addAll(java.util.Arrays.asList(eventNameCol, typeCol, dateCol, applyCol));
        VBox.setVgrow(opportunitiesTable, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Opportunities");
        refreshBtn.getStyleClass().add("accent-button");
        refreshBtn.setOnAction(e -> refreshOpportunities());

        content.getChildren().addAll(refreshBtn, opportunitiesTable);
        refreshOpportunities();
        return content;
    }

    private void refreshMyTasks() {
        int userId = SessionManager.getInstance().getUserId();
        Student student = studentDAO.getStudentByUserId(userId);

        if (student != null) {
            myTasksTable.getItems().setAll(eventDetailsDAO.getVolunteersByStudent(student.getId()));
        }
    }

    private void refreshOpportunities() {
        // Fetch UPCOMING events
        List<Event> allEvents = eventDAO.getAllEvents();
        List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> "UPCOMING".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());
        opportunitiesTable.getItems().setAll(upcomingEvents);
    }

    private void showVolunteerDialog(Event event) {
        Dialog<String> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Volunteer Application");
        dialog.setHeaderText("Volunteer for " + event.getName());

        ButtonType submitBtn = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe what you can help with (e.g., Technical setup, Promotion, Logistics)...");
        descArea.setWrapText(true);
        descArea.setPrefHeight(100);

        content.getChildren().addAll(new Label("How would you like to help?"), descArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btnType -> {
            if (btnType == submitBtn) {
                return descArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(taskDesc -> {
            if (taskDesc != null && !taskDesc.trim().isEmpty()) {
                int userId = SessionManager.getInstance().getUserId();
                Student student = studentDAO.getStudentByUserId(userId);
                if (student != null) {
                    // Check if already volunteered
                    if (eventDetailsDAO.isVolunteer(event.getId(), student.getId())) {
                        showAlert(Alert.AlertType.WARNING, "Already Registered",
                                "You have already volunteered for this event.");
                        return;
                    }

                    if (eventDetailsDAO.registerVolunteer(event.getId(), student.getId(), taskDesc)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted! Wait for approval.");
                        refreshMyTasks();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit application.");
                    }
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Input Required", "Please describe how you can help.");
            }
        });
    }

    private void showRequestResourceDialog(EventVolunteer task) {
        Dialog<String> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
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

                if (eventDetailsDAO.addResource(res)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Resource requested successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to request resource.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
