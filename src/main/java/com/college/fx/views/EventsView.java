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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;

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
    private com.college.dao.EventDetailsDAO eventDetailsDAO;
    private StudentDAO studentDAO;
    private Student currentStudent;

    private ObservableList<Event> allEventsData;
    private ObservableList<Event> myEventsData;
    private TableView<Event> allEventsTable;
    private TableView<Event> myEventsTable;
    private ComboBox<String> filterCombo;

    public EventsView(int userId) {
        this.eventDAO = new EventDAO();
        this.eventDetailsDAO = new com.college.dao.EventDetailsDAO();
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
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = createHeader();

        // Tab Pane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("floating");
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

        Label title = new Label("College Events");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh");
        refreshBtn.getStyleClass().add("icon-button");
        refreshBtn.setOnAction(e -> loadData());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createBrowseTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        // Filters
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        // Search field
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search events...");
        searchField.setPrefWidth(250);
        searchField.textProperty()
                .addListener((obs, oldVal, newVal) -> applySearchAndFilter(newVal, filterCombo.getValue()));

        Label filterLabel = new Label("Type:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Events", "FEST", "CULTURAL", "SPORTS", "ACADEMIC", "CLUB", "SEMINAR");
        filterCombo.setValue("All Events");
        filterCombo.setOnAction(e -> applySearchAndFilter(searchField.getText(), filterCombo.getValue()));

        filters.getChildren().addAll(searchField, filterLabel, filterCombo);

        // Table
        allEventsTable = createEventsTable(true);
        allEventsTable.getStyleClass().add("glass-table");
        VBox.setVgrow(allEventsTable, Priority.ALWAYS);

        content.getChildren().addAll(filters, allEventsTable);
        return content;
    }

    private VBox createMyEventsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        myEventsTable = createEventsTable(false);
        myEventsTable.getStyleClass().add("glass-table");
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
        nameCol.setSortable(true);

        TableColumn<Event, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEventType()));
        typeCol.setPrefWidth(100);
        typeCol.setSortable(true);

        TableColumn<Event, String> dateCol = new TableColumn<>("Start Time");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                dateFormat.format(data.getValue().getStartTime())));
        dateCol.setPrefWidth(150);
        dateCol.setSortable(true);
        // Sort by actual date, not string
        dateCol.setComparator((date1, date2) -> {
            try {
                return dateFormat.parse(date1).compareTo(dateFormat.parse(date2));
            } catch (Exception e) {
                return date1.compareTo(date2);
            }
        });

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        locationCol.setPrefWidth(150);
        locationCol.setSortable(true);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setSortable(true);

        table.getColumns().addAll(nameCol, typeCol, dateCol, locationCol, statusCol);

        if (includeActions) {
            TableColumn<Event, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button registerBtn = new Button("Register");
                private final Button volunteerBtn = new Button("Volunteer");
                private final Button viewBtn = new Button("View");

                {
                    registerBtn.setStyle(
                            "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                    volunteerBtn.setStyle(
                            "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                    viewBtn.setStyle(
                            "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");

                    registerBtn.setOnAction(e -> {
                        Event event = getTableView().getItems().get(getIndex());
                        registerForEvent(event);
                    });

                    volunteerBtn.setOnAction(e -> {
                        Event event = getTableView().getItems().get(getIndex());
                        showVolunteerDialog(event);
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

                        // Register Button Logic
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

                        // Volunteer Button Logic
                        if (currentStudent != null) {
                            if (eventDetailsDAO.isVolunteer(event.getId(), currentStudent.getId())) {
                                volunteerBtn.setText("Volunteered");
                                volunteerBtn.setDisable(true);
                                volunteerBtn.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white;");
                            } else {
                                volunteerBtn.setText("Volunteer");
                                volunteerBtn.setDisable(false);
                                volunteerBtn.setStyle(
                                        "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                        }

                        HBox buttons = new HBox(5, viewBtn, registerBtn, volunteerBtn);
                        setGraphic(buttons);
                    }
                }
            });
            actionCol.setPrefWidth(280);
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

    private void applySearchAndFilter(String searchText, String typeFilter) {
        ObservableList<Event> filtered = allEventsData;

        // Apply type filter
        if (typeFilter != null && !typeFilter.equals("All Events")) {
            filtered = filtered.filtered(e -> e.getEventType().equals(typeFilter));
        }

        // Apply search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase();
            filtered = filtered.filtered(e -> e.getName().toLowerCase().contains(search) ||
                    e.getLocation().toLowerCase().contains(search) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(search)));
        }

        allEventsTable.setItems(filtered);
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

    private void showVolunteerDialog(Event event) {
        if (currentStudent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student profile not found.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Volunteer for Event");
        dialog.setHeaderText("Volunteer for " + event.getName());

        ButtonType submitBtn = new ButtonType("Submit", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea taskArea = new TextArea();
        taskArea.setPromptText("Describe the task or role you'd like to help with...");
        taskArea.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Proposed Task/Role:"), taskArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btnType -> {
            if (btnType == submitBtn) {
                return taskArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            if (eventDetailsDAO.registerVolunteer(event.getId(), currentStudent.getId(), task)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "You have signed up as a volunteer!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to sign up.");
            }
        });
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

    private Button createButton(String text) {
        Button btn = new Button(text);
        // Style classes are added by caller
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
