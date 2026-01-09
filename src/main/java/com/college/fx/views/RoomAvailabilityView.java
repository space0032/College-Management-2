package com.college.fx.views;

import com.college.dao.TimetableDAO;
import com.college.utils.DialogUtils;
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

    private static final String[] TIME_SLOTS = { "9:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-1:00", "2:00-3:00",
            "3:00-4:00", "4:00-5:00" };

    public VBox getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(25));
        view.getStyleClass().add("glass-pane");
        view.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        Label title = new Label("Room Availability Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll(TIME_SLOTS);
        timeCombo.setPromptText("Select Time Slot");

        Button checkBtn = new Button("Check Availability");
        checkBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");

        controls.getChildren().addAll(new Label("Date:"), datePicker, new Label("Time:"), timeCombo, checkBtn);

        resultBox = new VBox(15);
        ScrollPane scroll = new ScrollPane(resultBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        checkBtn.setOnAction(e -> {
            if (datePicker.getValue() == null || timeCombo.getValue() == null) {
                showAlert("Input Error", "Please select both date and time.");
                return;
            }
            checkAvailability(datePicker.getValue(), timeCombo.getValue());
        });

        refreshAllRoomsOverview();

        view.getChildren().addAll(title, controls, new Separator(), new Label("Overview"), scroll);
        return view;
    }

    private void checkAvailability(LocalDate date, String timeSlot) {
        resultBox.getChildren().clear();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<String> allRooms = timetableDAO.getAllRooms();
        List<String> occupiedRooms = timetableDAO.getOccupiedRooms(dayOfWeek, timeSlot);

        List<String> available = allRooms.stream()
                .filter(r -> !occupiedRooms.contains(r))
                .collect(Collectors.toList());

        Label header = new Label("Available Rooms on " + dayOfWeek + ", " + timeSlot);
        header.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 16px;");

        FlowPane flow = new FlowPane();
        flow.setHgap(15);
        flow.setVgap(15);

        for (String room : available) {
            Label badge = new Label(room);
            badge.setStyle(
                    "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20; -fx-font-weight: bold;");
            flow.getChildren().add(badge);
        }

        if (available.isEmpty()) {
            Label none = new Label("No rooms available.");
            none.setStyle("-fx-text-fill: #ef4444;");
            flow.getChildren().add(none);
        }

        resultBox.getChildren().addAll(header, flow);
    }

    private void refreshAllRoomsOverview() {
        // Initially act as placeholder or show generic info
        Label info = new Label("Select a date and time to see real-time room availability.");
        info.setStyle("-fx-text-fill: #94a3b8;");
        resultBox.getChildren().add(info);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
