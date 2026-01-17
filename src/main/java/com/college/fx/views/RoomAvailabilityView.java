package com.college.fx.views;

import com.college.dao.TimetableDAO;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RoomAvailabilityView {

    private final TimetableDAO timetableDAO = new TimetableDAO();
    private VBox resultBox;
    private Label statsLabel;
    private TextField searchField;
    private ComboBox<String> filterCombo;

    private static final String[] TIME_SLOTS = { "9:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-1:00", "2:00-3:00",
            "3:00-4:00", "4:00-5:00" };

    public VBox getView() {
        // Check permission
        if (!SessionManager.getInstance().hasPermission("ROOM_CHECK")) {
            return new VBox(new Label("Access Denied: You do not have permission to view room availability."));
        }

        VBox view = new VBox(20);
        view.setPadding(new Insets(25));
        view.getStyleClass().add("glass-pane");
        view.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("glass-card");

        Label title = new Label("Room Availability Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statsLabel = new Label("Total Rooms: 0");
        statsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0; -fx-font-size: 14px;");

        header.getChildren().addAll(title, spacer, statsLabel);

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(15));
        controls.getStyleClass().add("glass-card");

        searchField = new TextField();
        searchField.setPromptText("Search rooms...");
        searchField.setPrefWidth(200);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(150);

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll(TIME_SLOTS);
        timeCombo.setPromptText("Select Time Slot");
        timeCombo.setPrefWidth(150);

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Rooms", "Available Only", "Occupied Only");
        filterCombo.setValue("All Rooms");
        filterCombo.setPrefWidth(150);

        Button checkBtn = new Button("Check Availability");
        checkBtn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        Button todayBtn = new Button("Today's Schedule");
        todayBtn.setStyle(
                "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        todayBtn.setOnAction(e -> showTodaySchedule());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshAllRoomsOverview());

        Button manageBtn = new Button("Manage Rooms");
        manageBtn.setStyle(
                "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        manageBtn.setOnAction(e -> showManageRoomsDialog());

        // Only show Manage Rooms button if user has permission
        if (!SessionManager.getInstance().hasPermission("MANAGE_ROOMS")) {
            manageBtn.setVisible(false);
            manageBtn.setManaged(false);
        }

        controls.getChildren().addAll(searchField, new Label("Date:"), datePicker, new Label("Time:"), timeCombo,
                filterCombo, checkBtn, todayBtn, refreshBtn, manageBtn);

        resultBox = new VBox(15);
        VBox.setVgrow(resultBox, Priority.ALWAYS);

        checkBtn.setOnAction(e -> {
            if (datePicker.getValue() == null || timeCombo.getValue() == null) {
                showAlert("Input Error", "Please select both date and time.");
                return;
            }
            checkAvailability(datePicker.getValue(), timeCombo.getValue());
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (datePicker.getValue() != null && timeCombo.getValue() != null) {
                checkAvailability(datePicker.getValue(), timeCombo.getValue());
            }
        });

        filterCombo.setOnAction(e -> {
            if (datePicker.getValue() != null && timeCombo.getValue() != null) {
                checkAvailability(datePicker.getValue(), timeCombo.getValue());
            }
        });

        refreshAllRoomsOverview();

        view.getChildren().addAll(header, controls, new Separator(), resultBox);
        return view;
    }

    private void checkAvailability(LocalDate date, String timeSlot) {
        resultBox.getChildren().clear();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<String> allRooms = timetableDAO.getAllRooms();
        List<String> occupiedRooms = timetableDAO.getOccupiedRooms(dayOfWeek, timeSlot);

        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();

        List<String> available = allRooms.stream()
                .filter(r -> !occupiedRooms.contains(r))
                .filter(r -> searchText.isEmpty() || r.toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        List<String> occupied = occupiedRooms.stream()
                .filter(r -> searchText.isEmpty() || r.toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        VBox content = new VBox(20);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("glass-card");

        Label header = new Label("Room Status on " + dayOfWeek + ", " + timeSlot);
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label("Total: " + allRooms.size());
        totalLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label availLabel = new Label("Available: " + available.size());
        availLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label occLabel = new Label("Occupied: " + occupied.size());
        occLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-font-weight: bold;");

        stats.getChildren().addAll(totalLabel, availLabel, occLabel);

        content.getChildren().addAll(header, stats, new Separator());

        if (!filter.equals("Occupied Only") && !available.isEmpty()) {
            Label availHeader = new Label("Available Rooms");
            availHeader.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 16px; -fx-font-weight: bold;");

            FlowPane availFlow = new FlowPane();
            availFlow.setHgap(15);
            availFlow.setVgap(15);
            availFlow.setPadding(new Insets(10, 0, 0, 0));

            for (String room : available) {
                Label badge = new Label(room);
                badge.setStyle(
                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 13px;");
                availFlow.getChildren().add(badge);
            }

            content.getChildren().addAll(availHeader, availFlow);
        }

        if (!filter.equals("Available Only") && !occupied.isEmpty()) {
            Label occHeader = new Label("Occupied Rooms");
            occHeader.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: bold;");

            FlowPane occFlow = new FlowPane();
            occFlow.setHgap(15);
            occFlow.setVgap(15);
            occFlow.setPadding(new Insets(10, 0, 0, 0));

            for (String room : occupied) {
                Label badge = new Label(room);
                badge.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 13px;");
                occFlow.getChildren().add(badge);
            }

            content.getChildren().addAll(new Separator(), occHeader, occFlow);
        }

        if ((filter.equals("Available Only") && available.isEmpty()) ||
                (filter.equals("Occupied Only") && occupied.isEmpty()) ||
                (filter.equals("All Rooms") && available.isEmpty() && occupied.isEmpty())) {
            Label none = new Label("No rooms found matching the criteria.");
            none.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            content.getChildren().add(none);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        resultBox.getChildren().add(scroll);
        statsLabel.setText(String.format("Total: %d | Available: %d | Occupied: %d", allRooms.size(), available.size(),
                occupied.size()));
    }

    private void showTodaySchedule() {
        resultBox.getChildren().clear();
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        VBox content = new VBox(20);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("glass-card");

        Label header = new Label("Today's Room Schedule - " + dayOfWeek + ", " + today);
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        content.getChildren().add(header);

        for (String timeSlot : TIME_SLOTS) {
            List<String> allRooms = timetableDAO.getAllRooms();
            List<String> occupiedRooms = timetableDAO.getOccupiedRooms(dayOfWeek, timeSlot);
            int available = allRooms.size() - occupiedRooms.size();

            HBox slotBox = new HBox(20);
            slotBox.setAlignment(Pos.CENTER_LEFT);
            slotBox.setPadding(new Insets(10));
            slotBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.5); -fx-background-radius: 8;");

            Label timeLabel = new Label(timeSlot);
            timeLabel.setStyle(
                    "-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 120;");

            Label availLabel = new Label("Available: " + available);
            availLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 13px;");

            Label occLabel = new Label("Occupied: " + occupiedRooms.size());
            occLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px;");

            slotBox.getChildren().addAll(timeLabel, availLabel, occLabel);
            content.getChildren().add(slotBox);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        resultBox.getChildren().add(scroll);
    }

    private void refreshAllRoomsOverview() {
        resultBox.getChildren().clear();
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.getStyleClass().add("glass-card");

        Label info = new Label("Select a date and time to see real-time room availability.");
        info.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        Label tip = new Label("💡 Tip: Use 'Today's Schedule' to see all time slots at once.");
        tip.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 13px; -fx-padding: 10 0 0 0;");

        content.getChildren().addAll(info, tip);
        resultBox.getChildren().add(content);

        List<String> allRooms = timetableDAO.getAllRooms();
        statsLabel.setText("Total Rooms: " + allRooms.size());
    }

    private void showManageRoomsDialog() {
        // Check permission
        if (!SessionManager.getInstance().hasPermission("MANAGE_ROOMS")) {
            showAlert("Access Denied", "You don't have permission to manage rooms.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Manage Rooms");
        dialog.setHeaderText("Add or Remove Rooms");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setPrefHeight(500);

        HBox addBox = new HBox(10);
        addBox.setAlignment(Pos.CENTER_LEFT);

        TextField roomField = new TextField();
        roomField.setPromptText("Room Number (e.g., R101)");
        roomField.setPrefWidth(150);

        TextField nameField = new TextField();
        nameField.setPromptText("Room Name (e.g., Lecture Hall)");
        nameField.setPrefWidth(200);

        Button addBtn = new Button("Add Room");
        addBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        addBox.getChildren().addAll(roomField, nameField, addBtn);

        TableView<String> roomTable = new TableView<>();
        roomTable.getStyleClass().add("glass-table");
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<String, String> roomCol = new TableColumn<>("Room Number");
        roomCol.setCellValueFactory(data -> {
            String val = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(val.contains(" - ") ? val.split(" - ")[0] : val);
        });
        roomCol.setPrefWidth(150);

        TableColumn<String, String> nameCol = new TableColumn<>("Room Name");
        nameCol.setCellValueFactory(data -> {
            String val = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(val.contains(" - ") ? val.split(" - ", 2)[1] : "-");
        });
        nameCol.setPrefWidth(250);

        TableColumn<String, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 15;");
                deleteBtn.setOnAction(event -> {
                    String room = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    DialogUtils.styleDialog(confirm);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText("Delete Room?");
                    confirm.setContentText("Are you sure you want to delete " + room + "?");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Remove from table only - no DAO method needed
                            getTableView().getItems().remove(room);
                            refreshAllRoomsOverview();
                            DialogUtils.showSuccess("Success", "Room removed from list.");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionCol.setPrefWidth(150);

        roomTable.getColumns().addAll(roomCol, nameCol, actionCol);
        roomTable.getItems().addAll(timetableDAO.getAllRooms());
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        addBtn.setOnAction(e -> {
            String roomNum = roomField.getText().trim();
            String roomName = nameField.getText().trim();
            if (roomNum.isEmpty()) {
                showAlert("Error", "Please enter a room number.");
                return;
            }
            String roomEntry = roomName.isEmpty() ? roomNum : roomNum + " - " + roomName;
            if (roomTable.getItems().stream().anyMatch(r -> r.startsWith(roomNum + " "))) {
                showAlert("Error", "Room number already exists.");
                return;
            }
            roomTable.getItems().add(roomEntry);
            roomField.clear();
            nameField.clear();
            refreshAllRoomsOverview();
            DialogUtils.showSuccess("Success", "Room added to list.");
        });

        Label statsLbl = new Label("Total Rooms: " + roomTable.getItems().size());
        statsLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        content.getChildren().addAll(new Label("Add New Room"), addBox, new Separator(), statsLbl, roomTable);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
