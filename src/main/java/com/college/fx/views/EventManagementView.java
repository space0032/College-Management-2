package com.college.fx.views;

import com.college.dao.EventDAO;
import com.college.models.Event;
import com.college.models.EventRegistration;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Admin/Faculty view for managing college events
 */
public class EventManagementView {
    private VBox root;
    private EventDAO eventDAO;
    private int userId;

    private ObservableList<Event> eventsData;
    private TableView<Event> eventsTable;

    public EventManagementView(int userId) {
        this.userId = userId;
        this.eventDAO = new EventDAO();
        this.eventsData = FXCollections.observableArrayList();

        createView();
        loadEvents();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();

        root.getChildren().addAll(header, tableSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("📅 Event Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = createButton("Create Event", "#22c55e");
        createBtn.setOnAction(e -> showCreateEventDialog());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadEvents());

        header.getChildren().addAll(title, spacer, createBtn, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        VBox.setVgrow(section, Priority.ALWAYS);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        eventsTable = new TableView<>();
        eventsTable.setItems(eventsData);
        VBox.setVgrow(eventsTable, Priority.ALWAYS);

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Event, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventType()));
        typeCol.setPrefWidth(100);

        TableColumn<Event, String> dateCol = new TableColumn<>("Start Time");
        dateCol.setCellValueFactory(
                data -> new SimpleStringProperty(dateFormat.format(data.getValue().getStartTime())));
        dateCol.setPrefWidth(150);

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        locationCol.setPrefWidth(150);

        TableColumn<Event, String> registrationsCol = new TableColumn<>("Registrations");
        registrationsCol.setCellValueFactory(data -> {
            int count = data.getValue().getRegistrationCount();
            Integer max = data.getValue().getMaxParticipants();
            String text = String.valueOf(count);
            if (max != null) {
                text += " / " + max;
            }
            return new SimpleStringProperty(text);
        });
        registrationsCol.setPrefWidth(120);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Event, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button registrationsBtn = new Button("Registrations");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10;");
                registrationsBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showEditEventDialog(event);
                });

                registrationsBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    showRegistrationsDialog(event);
                });

                deleteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    deleteEvent(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, registrationsBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionCol.setPrefWidth(280);

        eventsTable.getColumns().addAll(nameCol, typeCol, dateCol, locationCol, registrationsCol, statusCol, actionCol);
        section.getChildren().add(eventsTable);
        return section;
    }

    private void loadEvents() {
        List<Event> events = eventDAO.getAllEvents();
        eventsData.setAll(events);
    }

    private void showCreateEventDialog() {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Create Event");
        dialog.setHeaderText("Create New Event");

        ButtonType createBtn = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = createEventForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btnType -> {
            if (btnType == createBtn) {
                return extractEventFromForm(grid, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(event -> {
            event.setCreatedBy(userId);
            if (eventDAO.createEvent(event)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event created successfully!");
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event.");
            }
        });
    }

    private void showEditEventDialog(Event event) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");
        dialog.setHeaderText("Edit Event: " + event.getName());

        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = createEventForm(event);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btnType -> {
            if (btnType == saveBtn) {
                return extractEventFromForm(grid, event);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedEvent -> {
            if (eventDAO.updateEvent(updatedEvent)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!");
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event.");
            }
        });
    }

    private GridPane createEventForm(Event event) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(600);

        TextField nameField = new TextField(event != null ? event.getName() : "");
        nameField.setPromptText("Event Name");
        nameField.setUserData("name");

        TextArea descArea = new TextArea(event != null ? event.getDescription() : "");
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        descArea.setUserData("description");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("FEST", "CULTURAL", "SPORTS", "ACADEMIC", "CLUB", "SEMINAR");
        typeCombo.setValue(event != null ? event.getEventType() : "ACADEMIC");
        typeCombo.setUserData("type");

        TextField locationField = new TextField(event != null ? event.getLocation() : "");
        locationField.setPromptText("Location");
        locationField.setUserData("location");

        DatePicker startDatePicker = new DatePicker();
        if (event != null && event.getStartTime() != null) {
            startDatePicker.setValue(event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        startDatePicker.setUserData("startDate");

        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23,
                event != null ? event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).getHour() : 10);
        startHourSpinner.setEditable(true);
        startHourSpinner.setUserData("startHour");

        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59,
                event != null ? event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).getMinute() : 0);
        startMinuteSpinner.setEditable(true);
        startMinuteSpinner.setUserData("startMinute");

        DatePicker endDatePicker = new DatePicker();
        if (event != null && event.getEndTime() != null) {
            endDatePicker.setValue(event.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        endDatePicker.setUserData("endDate");

        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23,
                event != null ? event.getEndTime().toInstant().atZone(ZoneId.systemDefault()).getHour() : 17);
        endHourSpinner.setEditable(true);
        endHourSpinner.setUserData("endHour");

        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59,
                event != null ? event.getEndTime().toInstant().atZone(ZoneId.systemDefault()).getMinute() : 0);
        endMinuteSpinner.setEditable(true);
        endMinuteSpinner.setUserData("endMinute");

        TextField maxParticipantsField = new TextField(
                event != null && event.getMaxParticipants() != null ? String.valueOf(event.getMaxParticipants()) : "");
        maxParticipantsField.setPromptText("Max Participants (optional)");
        maxParticipantsField.setUserData("maxParticipants");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("UPCOMING", "ONGOING", "COMPLETED", "CANCELLED");
        statusCombo.setValue(event != null ? event.getStatus() : "UPCOMING");
        statusCombo.setUserData("status");

        int row = 0;
        grid.add(new Label("Event Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descArea, 1, row++);

        grid.add(new Label("Type:"), 0, row);
        grid.add(typeCombo, 1, row++);

        grid.add(new Label("Location:"), 0, row);
        grid.add(locationField, 1, row++);

        grid.add(new Label("Start Date:"), 0, row);
        grid.add(startDatePicker, 1, row++);

        grid.add(new Label("Start Time:"), 0, row);
        HBox startTimeBox = new HBox(5, startHourSpinner, new Label(":"), startMinuteSpinner);
        grid.add(startTimeBox, 1, row++);

        grid.add(new Label("End Date:"), 0, row);
        grid.add(endDatePicker, 1, row++);

        grid.add(new Label("End Time:"), 0, row);
        HBox endTimeBox = new HBox(5, endHourSpinner, new Label(":"), endMinuteSpinner);
        grid.add(endTimeBox, 1, row++);

        grid.add(new Label("Max Participants:"), 0, row);
        grid.add(maxParticipantsField, 1, row++);

        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);

        return grid;
    }

    @SuppressWarnings("unchecked")
    private Event extractEventFromForm(GridPane grid, Event existingEvent) {
        Event event = existingEvent != null ? existingEvent : new Event();

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node.getUserData() == null)
                continue;

            String field = (String) node.getUserData();
            switch (field) {
                case "name":
                    event.setName(((TextField) node).getText());
                    break;
                case "description":
                    event.setDescription(((TextArea) node).getText());
                    break;
                case "type":
                    event.setEventType(((ComboBox<String>) node).getValue());
                    break;
                case "location":
                    event.setLocation(((TextField) node).getText());
                    break;
                case "status":
                    event.setStatus(((ComboBox<String>) node).getValue());
                    break;
                case "maxParticipants":
                    String maxStr = ((TextField) node).getText().trim();
                    if (!maxStr.isEmpty()) {
                        try {
                            event.setMaxParticipants(Integer.parseInt(maxStr));
                        } catch (NumberFormatException e) {
                            event.setMaxParticipants(null);
                        }
                    }
                    break;
            }
        }

        // Extract dates and times
        DatePicker startDatePicker = null, endDatePicker = null;
        Spinner<Integer> startHour = null, startMinute = null, endHour = null, endMinute = null;

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node.getUserData() != null) {
                String field = (String) node.getUserData();
                if ("startDate".equals(field))
                    startDatePicker = (DatePicker) node;
                if ("endDate".equals(field))
                    endDatePicker = (DatePicker) node;
            }

            // Check if node is an HBox containing spinners
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (javafx.scene.Node child : hbox.getChildren()) {
                    if (child.getUserData() != null) {
                        String field = (String) child.getUserData();
                        if ("startHour".equals(field))
                            startHour = (Spinner<Integer>) child;
                        if ("startMinute".equals(field))
                            startMinute = (Spinner<Integer>) child;
                        if ("endHour".equals(field))
                            endHour = (Spinner<Integer>) child;
                        if ("endMinute".equals(field))
                            endMinute = (Spinner<Integer>) child;
                    }
                }
            }
        }

        if (startDatePicker != null && startDatePicker.getValue() != null && startHour != null && startMinute != null) {
            LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    java.time.LocalTime.of(startHour.getValue(), startMinute.getValue()));
            event.setStartTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (endDatePicker != null && endDatePicker.getValue() != null && endHour != null && endMinute != null) {
            LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    java.time.LocalTime.of(endHour.getValue(), endMinute.getValue()));
            event.setEndTime(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }

        return event;
    }

    private void showRegistrationsDialog(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Event Registrations");
        dialog.setHeaderText("Registrations for " + event.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setPrefHeight(500);

        TableView<EventRegistration> regTable = new TableView<>();
        TableColumn<EventRegistration, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(200);

        TableColumn<EventRegistration, String> dateCol = new TableColumn<>("Registered On");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                new SimpleDateFormat("MMM dd, yyyy").format(data.getValue().getRegisteredAt())));
        dateCol.setPrefWidth(150);

        TableColumn<EventRegistration, String> statusCol = new TableColumn<>("Attendance");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAttendanceStatus()));
        statusCol.setPrefWidth(120);

        TableColumn<EventRegistration, Void> actionCol = new TableColumn<>("Mark");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            {
                statusCombo.getItems().addAll("REGISTERED", "ATTENDED", "ABSENT");
                statusCombo.setOnAction(e -> {
                    EventRegistration reg = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (eventDAO.markAttendance(reg.getId(), newStatus)) {
                        reg.setAttendanceStatus(newStatus);
                        getTableView().refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventRegistration reg = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(reg.getAttendanceStatus());
                    setGraphic(statusCombo);
                }
            }
        });
        actionCol.setPrefWidth(130);

        regTable.getColumns().addAll(studentCol, dateCol, statusCol, actionCol);

        List<EventRegistration> registrations = eventDAO.getEventRegistrations(event.getId());
        regTable.setItems(FXCollections.observableArrayList(registrations));
        VBox.setVgrow(regTable, Priority.ALWAYS);

        content.getChildren().add(regTable);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void deleteEvent(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete " + event.getName() + "?");
        confirm.setContentText("This will remove all registrations. This action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (eventDAO.deleteEvent(event.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully!");
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event.");
            }
        }
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        return btn;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
