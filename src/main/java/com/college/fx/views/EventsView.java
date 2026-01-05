package com.college.fx.views;

import com.college.dao.EventDAO;
import com.college.dao.StudentDAO;
import com.college.models.Event;
import com.college.models.Student;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student-facing view for browsing and registering for college events
 */
public class EventsView {
    private VBox root;
    private EventDAO eventDAO;
    private StudentDAO studentDAO;
    private Student currentStudent;

    private ObservableList<Event> allEventsData;
    private ObservableList<Event> myEventsData;
    private TableView<Event> allEventsTable;
    private TableView<Event> myEventsTable;
    private ComboBox<String> filterCombo;

    public EventsView(int userId) {
        this.eventDAO = new EventDAO();
        this.studentDAO = new StudentDAO();
        this.currentStudent = studentDAO.getStudentByUserId(userId);
        this.allEventsData = FXCollections.observableArrayList();
        this.myEventsData = FXCollections.observableArrayList();

        createView();
        loadData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = createHeader();

        // Tab Pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab browseTab = new Tab("Browse Events");
        browseTab.setContent(createBrowseTab());

        Tab myEventsTab = new Tab("My Events");
        myEventsTab.setContent(createMyEventsTab());

        tabPane.getTabs().addAll(browseTab, myEventsTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("College Events");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createBrowseTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // Filters
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter by Type:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Events", "FEST", "CULTURAL", "SPORTS", "ACADEMIC", "CLUB", "SEMINAR");
        filterCombo.setValue("All Events");
        filterCombo.setOnAction(e -> applyFilter());

        filters.getChildren().addAll(filterLabel, filterCombo);

        // Table
        allEventsTable = createEventsTable(true);
        // TODO: Fix CSS resource loading
        // allEventsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(allEventsTable, Priority.ALWAYS);

        content.getChildren().addAll(filters, allEventsTable);
        return content;
    }

    private VBox createMyEventsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        myEventsTable = createEventsTable(false);
        // TODO: Fix CSS resource loading
        // myEventsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(myEventsTable, Priority.ALWAYS);

        content.getChildren().add(myEventsTable);
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Event> createEventsTable(boolean includeActions) {
        TableView<Event> table = new TableView<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        TableColumn<Event, String> nameCol = new TableColumn<>("Event Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Event, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventType()));
        typeCol.setPrefWidth(100);

        TableColumn<Event, String> dateCol = new TableColumn<>("Start Time");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                dateFormat.format(data.getValue().getStartTime())));
        dateCol.setPrefWidth(150);

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        locationCol.setPrefWidth(150);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, typeCol, dateCol, locationCol, statusCol);

        if (includeActions) {
            TableColumn<Event, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button registerBtn = new Button("Register");
                private final Button viewBtn = new Button("View");

                {
                    registerBtn.setStyle(
                            "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                    viewBtn.setStyle(
                            "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");

                    registerBtn.setOnAction(e -> {
                        Event event = getTableView().getItems().get(getIndex());
                        registerForEvent(event);
                    });

                    viewBtn.setOnAction(e -> {
                        Event event = getTableView().getItems().get(getIndex());
                        showEventDetails(event);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Event event = getTableView().getItems().get(getIndex());
                        if (currentStudent != null
                                && eventDAO.isStudentRegistered(event.getId(), currentStudent.getId())) {
                            registerBtn.setText("Registered");
                            registerBtn.setDisable(true);
                            registerBtn.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white;");
                        } else {
                            registerBtn.setText("Register");
                            registerBtn.setDisable(false);
                            registerBtn.setStyle(
                                    "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
                        }

                        HBox buttons = new HBox(5, viewBtn, registerBtn);
                        setGraphic(buttons);
                    }
                }
            });
            actionCol.setPrefWidth(180);
            table.getColumns().add(actionCol);
        }

        return table;
    }

    private void loadData() {
        List<Event> allEvents = eventDAO.getUpcomingEvents();
        allEventsData.setAll(allEvents);
        allEventsTable.setItems(allEventsData);

        if (currentStudent != null) {
            List<Event> myEvents = eventDAO.getStudentRegisteredEvents(currentStudent.getId());
            myEventsData.setAll(myEvents);
            myEventsTable.setItems(myEventsData);
        }
    }

    private void applyFilter() {
        String filter = filterCombo.getValue();
        if (filter.equals("All Events")) {
            allEventsTable.setItems(allEventsData);
        } else {
            ObservableList<Event> filtered = allEventsData.filtered(e -> e.getEventType().equals(filter));
            allEventsTable.setItems(filtered);
        }
    }

    private void registerForEvent(Event event) {
        if (currentStudent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student profile not found.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Registration");
        confirm.setHeaderText("Register for " + event.getName() + "?");
        confirm.setContentText("You will be registered for this event.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (eventDAO.registerStudent(event.getId(), currentStudent.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully registered for " + event.getName());
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to register. You may already be registered or the event is full.");
            }
        }
    }

    private void showEventDetails(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(event.getName());
        dialog.setHeaderText(event.getEventType() + " Event");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mm a");

        content.getChildren().addAll(
                new Label("Location: " + event.getLocation()),
                new Label("Start: " + dateFormat.format(event.getStartTime())),
                new Label("End: " + dateFormat.format(event.getEndTime())),
                new Separator(),
                new Label("Description:"),
                new Label(event.getDescription() != null ? event.getDescription() : "No description available."),
                new Separator(),
                new Label("Registered: " + event.getRegistrationCount() +
                        (event.getMaxParticipants() != null ? " / " + event.getMaxParticipants() : "")));

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
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
