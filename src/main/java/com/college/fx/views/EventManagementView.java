package com.college.fx.views;

import com.college.dao.EventDAO;
import com.college.models.Event;
import com.college.models.EventBudget;
import com.college.models.EventPoll;
import com.college.models.EventRegistration;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;

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
    private com.college.dao.EventDetailsDAO eventDetailsDAO;
    private com.college.dao.DepartmentDAO departmentDAO;
    private int userId;

    private ObservableList<Event> eventsData;
    private TableView<Event> eventsTable;

    public EventManagementView(int userId) {
        this.userId = userId;
        this.eventDAO = new EventDAO();
        this.eventDetailsDAO = new com.college.dao.EventDetailsDAO();
        this.departmentDAO = new com.college.dao.DepartmentDAO();
        this.eventsData = FXCollections.observableArrayList();

        createView();
        loadEvents();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        HBox header = createHeader();
        VBox tableSection = createTableSection();

        root.getChildren().addAll(header, tableSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("glass-card");

        Label title = new Label("Event Management");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = createButton("Create Event", "#22c55e");
        createBtn.setOnAction(e -> showCreateEventDialog());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadEvents());

        header.getChildren().addAll(title, spacer, createBtn, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("glass-card");
        VBox.setVgrow(section, Priority.ALWAYS);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        eventsTable = new TableView<>();
        eventsTable.getStyleClass().add("glass-table");
        eventsTable.setItems(eventsData);
        eventsTable.setPlaceholder(new Label("No events yet.\nClick 'Create Event' to get started."));
        // eventsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
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
            private final Button editBtn = new Button("Edit Details");
            private final Button manageBtn = new Button("Manage");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10;");
                manageBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    EventManagementView.this.showEditDetailsDialog(event);
                });

                manageBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    EventManagementView.this.showManageEventDialog(event);
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
                    HBox buttons = new HBox(5, editBtn, manageBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionCol.setPrefWidth(300);

        eventsTable.getColumns().addAll(java.util.Arrays.asList(nameCol, typeCol, dateCol, locationCol,
                registrationsCol, statusCol, actionCol));
        section.getChildren().add(eventsTable);
        return section;
    }

    private void loadEvents() {
        eventDAO.updateEventStatuses();
        List<Event> events = eventDAO.getAllEvents();
        eventsData.setAll(events);
    }

    private void showCreateEventDialog() {
        Dialog<Event> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
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
                // Trigger Announcement
                com.college.models.Announcement ann = new com.college.models.Announcement();
                ann.setTitle("New Event: " + event.getName());
                ann.setContent("A new event '" + event.getName() + "' has been scheduled at " + event.getLocation()
                        + ". Register now!");
                ann.setTargetAudience("ALL");
                ann.setPriority("HIGH");
                ann.setCreatedBy(userId);
                ann.setActive(true);
                new com.college.dao.AnnouncementDAO().addAnnouncement(ann);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Event created successfully!");
                loadEvents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create event.");
            }
        });
    }

    private void showEditDetailsDialog(Event event) {
        Dialog<Event> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Edit Event Details");
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

    private void showManageEventDialog(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Manage Event");
        dialog.setHeaderText("Manage Event: " + event.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefWidth(700);
        tabPane.setPrefHeight(600);

        // Tab 1: Collaboration
        Tab collabTab = new Tab("Collaboration");
        collabTab.setContent(createCollaborationTab(event));

        // Tab 2: Resources
        Tab resourceTab = new Tab("Resources");
        resourceTab.setContent(createResourcesTab(event));

        // Tab 3: Volunteers
        Tab volunteerTab = new Tab("Volunteers");
        volunteerTab.setContent(createVolunteersTab(event));

        // Tab 4: Registrations (Moved from separate dialog for consolidation)
        Tab regTab = new Tab("Registrations");
        regTab.setContent(createRegistrationsContent(event));

        // Tab 5: Budgets
        Tab budgetTab = new Tab("Budgets");
        budgetTab.setContent(createBudgetsTab(event));

        // Tab 6: Polls
        Tab pollTab = new Tab("Polls");
        pollTab.setContent(createPollsTab(event));

        tabPane.getTabs().addAll(collabTab, resourceTab, volunteerTab, regTab, budgetTab, pollTab);
        dialog.getDialogPane().setContent(tabPane);
        dialog.showAndWait();
    }

    private VBox createRegistrationsContent(Event event) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TableView<EventRegistration> regTable = new TableView<>();
        regTable.getStyleClass().add("glass-table");
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

        regTable.getColumns().addAll(java.util.Arrays.asList(studentCol, dateCol, statusCol, actionCol));

        List<EventRegistration> registrations = eventDAO.getEventRegistrations(event.getId());
        regTable.setItems(FXCollections.observableArrayList(registrations));
        VBox.setVgrow(regTable, Priority.ALWAYS);

        content.getChildren().add(regTable);
        return content;
    }

    private VBox createCollaborationTab(Event event) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Add Collaborator Section
        HBox addBox = new HBox(10);
        addBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<com.college.models.Department> deptCombo = new ComboBox<>();
        deptCombo.setPromptText("Select Department");
        deptCombo.setItems(FXCollections.observableArrayList(departmentDAO.getAllDepartments()));
        // Display department name in ComboBox
        deptCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.college.models.Department object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public com.college.models.Department fromString(String string) {
                return null;
            }
        });

        Button addBtn = new Button("Add Collaborator");
        addBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");

        // List Section
        TableView<com.college.models.EventCollaborator> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        TableColumn<com.college.models.EventCollaborator, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartmentName()));
        deptCol.setPrefWidth(200);

        TableColumn<com.college.models.EventCollaborator, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(java.util.Arrays.asList(deptCol, statusCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable loadCollaborators = () -> {
            table.setItems(FXCollections.observableArrayList(eventDetailsDAO.getCollaborators(event.getId())));
        };
        loadCollaborators.run();

        addBtn.setOnAction(e -> {
            com.college.models.Department dept = deptCombo.getValue();
            if (dept != null) {
                if (eventDetailsDAO.addCollaborator(event.getId(), dept.getId())) {
                    loadCollaborators.run();
                    deptCombo.getSelectionModel().clearSelection();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add collaborator.");
                }
            }
        });
        addBox.getChildren().addAll(deptCombo, addBtn);

        content.getChildren().addAll(new Label("Manage Event Collaborators"), addBox, table);
        return content;
    }

    private VBox createResourcesTab(Event event) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Add Resource Section
        HBox addBox = new HBox(10);
        addBox.setAlignment(Pos.CENTER_LEFT);

        TextField resNameField = new TextField();
        resNameField.setPromptText("Resource Name");

        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setPrefWidth(80);

        Button addBtn = new Button("Request Resource");
        addBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");

        // List Section
        TableView<com.college.models.EventResource> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        TableColumn<com.college.models.EventResource, String> nameCol = new TableColumn<>("Resource");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceName()));
        nameCol.setPrefWidth(200);

        TableColumn<com.college.models.EventResource, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        qtyCol.setPrefWidth(100);

        TableColumn<com.college.models.EventResource, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(120);

        TableColumn<com.college.models.EventResource, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button issueBtn = new Button("Issue");
            private final Button returnBtn = new Button("Return");
            private final HBox pane = new HBox(5, approveBtn, issueBtn, returnBtn);

            {
                approveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 10px;");
                issueBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px;");
                returnBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 10px;");

                approveBtn.setOnAction(e -> updateStatus("APPROVED"));
                issueBtn.setOnAction(e -> updateStatus("ISSUED"));
                returnBtn.setOnAction(e -> updateStatus("RETURNED"));
            }

            private void updateStatus(String status) {
                com.college.models.EventResource item = getTableView().getItems().get(getIndex());
                if (eventDetailsDAO.updateResourceStatus(item.getId(), status)) {
                    item.setStatus(status);
                    getTableView().refresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    com.college.models.EventResource row = getTableView().getItems().get(getIndex());
                    String status = row.getStatus();

                    approveBtn.setVisible("REQUESTED".equals(status));
                    issueBtn.setVisible("APPROVED".equals(status));
                    returnBtn.setVisible("ISSUED".equals(status));

                    setGraphic(pane);
                }
            }
        });
        actionCol.setPrefWidth(200);

        table.getColumns().addAll(java.util.Arrays.asList(nameCol, qtyCol, statusCol, actionCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable loadResources = () -> {
            table.setItems(FXCollections.observableArrayList(eventDetailsDAO.getResources(event.getId())));
        };
        loadResources.run();

        addBtn.setOnAction(e -> {
            String name = resNameField.getText();
            if (!name.isEmpty()) {
                com.college.models.EventResource res = new com.college.models.EventResource();
                res.setEventId(event.getId());
                res.setResourceName(name);
                res.setQuantity(qtySpinner.getValue());

                if (eventDetailsDAO.addResource(res)) {
                    loadResources.run();
                    resNameField.clear();
                    qtySpinner.getValueFactory().setValue(1);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add resource.");
                }
            }
        });
        addBox.getChildren().addAll(resNameField, qtySpinner, addBtn);

        content.getChildren().addAll(new Label("Manage Event Resources"), addBox, table);
        return content;
    }

    private VBox createVolunteersTab(Event event) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Note: Students usually volunteer themselves, but admins can assign?
        // For now, let's just View volunteers and their tasks.

        TableView<com.college.models.EventVolunteer> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        TableColumn<com.college.models.EventVolunteer, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<com.college.models.EventVolunteer, String> taskCol = new TableColumn<>("Task");
        taskCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTaskDescription()));
        taskCol.setPrefWidth(200);

        TableColumn<com.college.models.EventVolunteer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<com.college.models.EventVolunteer, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.valueOf(data.getValue().getHoursLogged())));
        hoursCol.setPrefWidth(80);

        TableColumn<com.college.models.EventVolunteer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button("Assign Task");
            private final Button hoursBtn = new Button("Log Hours");

            {
                assignBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                assignBtn.setOnAction(e -> {
                    com.college.models.EventVolunteer volunteer = getTableView().getItems().get(getIndex());
                    showAssignTaskDialog(volunteer);
                });

                hoursBtn.setStyle(
                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                hoursBtn.setOnAction(e -> {
                    com.college.models.EventVolunteer volunteer = getTableView().getItems().get(getIndex());
                    showLogHoursDialog(volunteer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, assignBtn, hoursBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionCol.setPrefWidth(220);

        table.getColumns().addAll(java.util.Arrays.asList(studentCol, taskCol, statusCol, hoursCol, actionCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setItems(FXCollections.observableArrayList(eventDetailsDAO.getVolunteers(event.getId())));

        content.getChildren().addAll(new Label("Registered Volunteers"), table);
        return content;
    }

    private VBox createBudgetsTab(Event event) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox addBox = new HBox(10);
        addBox.setAlignment(Pos.CENTER_LEFT);

        TextField itemField = new TextField();
        itemField.setPromptText("Item Name");

        TextField costField = new TextField();
        costField.setPromptText("Est. Cost");
        costField.setPrefWidth(100);

        Button addBtn = new Button("Add Item");
        addBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");

        TableView<EventBudget> table = new TableView<>();
        table.getStyleClass().add("glass-table");

        TableColumn<EventBudget, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItem()));
        itemCol.setPrefWidth(200);

        TableColumn<EventBudget, String> estCol = new TableColumn<>("Est. Cost");
        estCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getEstimatedCost())));
        estCol.setPrefWidth(100);

        TableColumn<EventBudget, String> actCol = new TableColumn<>("Actual Cost");
        actCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getActualCost())));
        actCol.setPrefWidth(100);

        TableColumn<EventBudget, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<EventBudget, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");

            {
                approveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 8;");
                approveBtn.setOnAction(e -> {
                    EventBudget b = getTableView().getItems().get(getIndex());
                    if (eventDetailsDAO.updateBudgetStatus(b.getId(), "APPROVED")) {
                        b.setStatus("APPROVED");
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
                    EventBudget b = getTableView().getItems().get(getIndex());
                    if ("PLANNED".equals(b.getStatus())) {
                        setGraphic(approveBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(java.util.Arrays.asList(itemCol, estCol, actCol, statusCol, actionCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable loadBudgets = () -> {
            table.setItems(FXCollections.observableArrayList(eventDetailsDAO.getBudgets(event.getId())));
        };
        loadBudgets.run();

        addBtn.setOnAction(e -> {
            try {
                String item = itemField.getText();
                double cost = Double.parseDouble(costField.getText());
                EventBudget b = new EventBudget();
                b.setEventId(event.getId());
                b.setItem(item);
                b.setEstimatedCost(cost);
                b.setActualCost(0);
                b.setStatus("PLANNED");

                if (eventDetailsDAO.addBudget(b)) {
                    loadBudgets.run();
                    itemField.clear();
                    costField.clear();
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Cost must be a number.");
            }
        });

        addBox.getChildren().addAll(itemField, costField, addBtn);
        content.getChildren().addAll(new Label("Budget Tracker"), addBox, table);
        return content;
    }

    private VBox createPollsTab(Event event) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Button createBtn = new Button("Create Poll");
        createBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white;");
        createBtn.setOnAction(e -> showCreatePollDialog(event));

        TableView<EventPoll> table = new TableView<>();
        table.getStyleClass().add("glass-table");

        TableColumn<EventPoll, String> qCol = new TableColumn<>("Question");
        qCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuestion()));
        qCol.setPrefWidth(250);

        TableColumn<EventPoll, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(java.util.Arrays.asList(qCol, statusCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable loadPolls = () -> {
            table.setItems(FXCollections.observableArrayList(eventDetailsDAO.getPolls(event.getId())));
        };
        loadPolls.run();

        // Refresh polls when created
        createBtn.setUserData(loadPolls);

        content.getChildren().addAll(new Label("Event Polls"), createBtn, table);
        return content;
    }

    private void showCreatePollDialog(Event event) {
        Dialog<EventPoll> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Create Poll");
        dialog.setHeaderText("New Poll for " + event.getName());

        ButtonType createType = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField qField = new TextField();
        qField.setPromptText("Question");

        TextArea optArea = new TextArea();
        optArea.setPromptText("Options (comma separated)");
        optArea.setPrefRowCount(3);

        grid.add(new Label("Question:"), 0, 0);
        grid.add(qField, 1, 0);
        grid.add(new Label("Options:"), 0, 1);
        grid.add(optArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createType) {
                EventPoll p = new EventPoll();
                p.setEventId(event.getId());
                p.setQuestion(qField.getText());
                p.setOptions(optArea.getText());
                p.setStatus("ACTIVE");
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(poll -> {
            if (eventDetailsDAO.createPoll(poll)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Poll created!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create poll.");
            }
        });
    }

    private GridPane createEventForm(Event event) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(600);

        // Set column constraints to prevent label truncation
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

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

    private void deleteEvent(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        DialogUtils.styleDialog(confirm);
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
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAssignTaskDialog(com.college.models.EventVolunteer volunteer) {
        Dialog<String> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Assign Task");
        dialog.setHeaderText("Assign Task to " + volunteer.getStudentName());

        ButtonType saveBtn = new ButtonType("Assign", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea taskArea = new TextArea(volunteer.getTaskDescription() != null ? volunteer.getTaskDescription() : "");
        taskArea.setPromptText("Enter task description...");
        taskArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Task Description:"), taskArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btnType -> {
            if (btnType == saveBtn) {
                return taskArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            if (eventDetailsDAO.updateVolunteerTask(volunteer.getId(), task, "APPROVED")) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task assigned successfully!");
                // Refresh logic would ideally go here, but since this is inside a specific tab
                // create method,
                // we might need to manually refresh or reload the edit dialog.
                // For now, simple success message. The user might need to close/reopen to see
                // changes or we can trigger a refresh if we had access.
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign task.");
            }
        });
    }

    private void showLogHoursDialog(com.college.models.EventVolunteer volunteer) {
        Dialog<Float> dialog = new Dialog<>();
        dialog.setTitle("Log Hours");
        dialog.setHeaderText("Log Hours for " + volunteer.getStudentName());

        ButtonType saveBtn = new ButtonType("Save & Complete", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Spinner<Double> hoursSpinner = new Spinner<>(0.0, 100.0,
                volunteer.getHoursLogged() > 0 ? (double) volunteer.getHoursLogged() : 0.0, 0.5);
        hoursSpinner.setEditable(true);

        content.getChildren().addAll(new Label("Hours Worked:"), hoursSpinner);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btnType -> {
            if (btnType == saveBtn) {
                return hoursSpinner.getValue().floatValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(hours -> {
            if (eventDetailsDAO.updateVolunteerHours(volunteer.getId(), hours, "COMPLETED")) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Hours logged and task marked as completed!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update volunteer record.");
            }
        });
    }

    public VBox getView() {
        return root;
    }
}
