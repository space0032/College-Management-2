package com.college.fx.views;

import com.college.dao.EventDAO;
import com.college.models.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcademicCalendarView {

    private final EventDAO eventDAO = new EventDAO();
    private final com.college.services.GoogleCalendarService googleService = new com.college.services.GoogleCalendarService();
    private YearMonth currentYearMonth;
    private final GridPane calendarGrid;
    private final Text calendarTitle;

    public AcademicCalendarView() {
        this.currentYearMonth = YearMonth.now();
        this.calendarGrid = new GridPane();
        this.calendarTitle = new Text();
    }

    public VBox getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.getStyleClass().add("glass-pane");
        view.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<<");
        prevBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 50%;");
        prevBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextBtn = new Button(">>");
        nextBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 50%;");
        nextBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        calendarTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        calendarTitle.setFill(Color.WHITE);

        header.getChildren().addAll(prevBtn, calendarTitle, nextBtn);

        // Calendar Grid
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setAlignment(Pos.CENTER);

        updateCalendar();

        ScrollPane scroll = new ScrollPane(calendarGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(header, scroll);
        return view;
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        calendarTitle.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        // Days of week header
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < 7; i++) {
            Label dayName = new Label(days[i]);
            dayName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            dayName.setTextFill(Color.web("#cbd5e1"));
            calendarGrid.add(dayName, i, 0);
            GridPane.setHalignment(dayName, javafx.geometry.HPos.CENTER);
        }

        LocalDate calendarDate = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), 1);
        int dayOfWeek = calendarDate.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun

        // Load events for this month
        List<Event> allEvents = eventDAO.getAllEvents();

        // Fetch and merge Google Holidays
        List<com.college.models.CalendarEvent> holidays = googleService.getHolidays(currentYearMonth.getYear(),
                currentYearMonth.getMonthValue());
        for (com.college.models.CalendarEvent holiday : holidays) {
            Event e = new Event();
            e.setName(holiday.getTitle());
            e.setEventType("HOLIDAY");
            e.setStartTime(java.sql.Date.valueOf(holiday.getEventDate()));
            allEvents.add(e);
        }

        Map<LocalDate, List<Event>> eventsByDate = allEvents.stream()
                .filter(e -> e.getStartTime() != null)
                .collect(Collectors.groupingBy(e -> {
                    if (e.getStartTime() instanceof java.sql.Date) {
                        return ((java.sql.Date) e.getStartTime()).toLocalDate();
                    }
                    return e.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                }));

        int row = 1;
        int col = dayOfWeek - 1; // 0-indexed column

        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
            VBox dayCell = createDayCell(day, eventsByDate.get(date));

            calendarGrid.add(dayCell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(int day, List<Event> events) {
        VBox cell = new VBox(5);
        cell.setPrefSize(120, 100);
        cell.setPadding(new Insets(5));
        cell.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        dayLabel.setTextFill(Color.WHITE);
        cell.getChildren().add(dayLabel);

        if (events != null) {
            for (Event e : events) {
                Label eventLabel = new Label(e.getName());
                eventLabel.setMaxWidth(110);
                eventLabel.getStyleClass().add("event-card-small"); // Need to ensure style exists or inline

                String color = "#3b82f6"; // Default blue
                if ("HOLIDAY".equalsIgnoreCase(e.getEventType()))
                    color = "#ef4444"; // Red
                if ("EXAM".equalsIgnoreCase(e.getEventType()))
                    color = "#f59e0b"; // Amber

                eventLabel.setStyle("-fx-background-color: " + color
                        + "; -fx-text-fill: white; -fx-padding: 2 5; -fx-background-radius: 4; -fx-font-size: 10px;");

                cell.getChildren().add(eventLabel);
            }
        }

        return cell;
    }
}
