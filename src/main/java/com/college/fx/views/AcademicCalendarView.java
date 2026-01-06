package com.college.fx.views;

import com.college.dao.CalendarDAO;
import com.college.models.CalendarEvent;
import com.college.models.CalendarEvent.EventType;
import com.college.services.GoogleCalendarService;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class AcademicCalendarView {

    private VBox root;
    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private CalendarDAO calendarDAO;
    private GoogleCalendarService googleCalendarService;

    public AcademicCalendarView() {
        this.calendarDAO = new CalendarDAO();
        this.googleCalendarService = new GoogleCalendarService();
        this.currentYearMonth = YearMonth.now();
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<");
        prevBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        monthYearLabel = new Label();
        monthYearLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Button nextBtn = new Button(">");
        nextBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        header.getChildren().addAll(prevBtn, monthYearLabel, nextBtn);

        // Legend
        HBox legend = createLegend();

        // Calendar Grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        root.getChildren().addAll(header, legend, calendarGrid);

        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        // Header Row (Days)
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < 7; i++) {
            Label dayName = new Label(days[i]);
            dayName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            dayName.setMinWidth(100);
            dayName.setAlignment(Pos.CENTER);
            calendarGrid.add(dayName, i, 0);
        }

        // Days
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        List<CalendarEvent> dbEvents = calendarDAO.getEventsByMonth(currentYearMonth.getYear(),
                currentYearMonth.getMonthValue());

        // Fetch holidays from Google Calendar Service
        List<CalendarEvent> googleHolidays = googleCalendarService.getHolidays(currentYearMonth.getYear(),
                currentYearMonth.getMonthValue());

        // Merge lists (dbEvents is mutable or not? DAO returns generic list. Let's
        // create a combined list)
        List<CalendarEvent> events = new java.util.ArrayList<>(dbEvents);
        events.addAll(googleHolidays);

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date, events);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, List<CalendarEvent> allEvents) {
        VBox cell = new VBox(5);
        cell.setPrefSize(100, 100);
        cell.setPadding(new Insets(5));
        cell.setStyle("-fx-border-color: #e2e8f0; -fx-background-color: white;");

        // Highlight today
        if (date.equals(LocalDate.now())) {
            cell.setStyle("-fx-border-color: #3b82f6; -fx-background-color: #eff6ff; -fx-border-width: 2;");
        } else if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            // Highlight Weekends (Holidays)
            cell.setStyle("-fx-border-color: #e2e8f0; -fx-background-color: #fef2f2;"); // Light red background
        }

        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        cell.getChildren().add(dateLbl);

        // Filter events for this day
        List<CalendarEvent> dayEvents = allEvents.stream()
                .filter(e -> e.getEventDate().equals(date))
                .collect(Collectors.toList());

        for (CalendarEvent e : dayEvents) {
            Label eventLbl = new Label(e.getTitle());
            eventLbl.setFont(Font.font("Segoe UI", 10));
            eventLbl.setWrapText(true);
            eventLbl.setTextFill(Color.WHITE);
            eventLbl.setPadding(new Insets(2));
            eventLbl.setMaxWidth(Double.MAX_VALUE);

            String color = "#64748b"; // Default
            if (e.getEventType() == EventType.HOLIDAY)
                color = "#ef4444";
            else if (e.getEventType() == EventType.EXAM)
                color = "#f59e0b";
            else if (e.getEventType() == EventType.EVENT)
                color = "#3b82f6";

            eventLbl.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

            // Tooltip
            Tooltip tooltip = new Tooltip(e.getTitle() + "\n" + e.getDescription());
            Tooltip.install(eventLbl, tooltip);

            // Delete action for Admin
            if (SessionManager.getInstance().getRole().equals("ADMIN")) {
                ContextMenu cm = new ContextMenu();
                MenuItem deleteItem = new MenuItem("Delete Event");
                deleteItem.setOnAction(ev -> {
                    calendarDAO.deleteEvent(e.getId());
                    updateCalendar();
                });
                cm.getItems().add(deleteItem);
                eventLbl.setContextMenu(cm);
            }

            cell.getChildren().add(eventLbl);
        }

        // Add Event Action (Admin only)
        if (SessionManager.getInstance().getRole().equals("ADMIN")) {
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    showAddEventDialog(date);
                }
            });
        }

        return cell;
    }

    private void showAddEventDialog(LocalDate date) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Event");
        dialog.setHeaderText("Add Event for " + date);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add column constraints to prevent label truncation
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField titleField = new TextField();
        titleField.setPromptText("Event Title");

        ComboBox<EventType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(EventType.values());
        typeCombo.setValue(EventType.EVENT);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                CalendarEvent event = new CalendarEvent();
                event.setTitle(titleField.getText());
                event.setEventDate(date);
                event.setEventType(typeCombo.getValue());
                event.setDescription(descArea.getText());

                if (calendarDAO.addEvent(event)) {
                    updateCalendar();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Failed to add event").show();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private HBox createLegend() {
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);

        legend.getChildren().addAll(
                createLegendItem("Holiday", "#ef4444"),
                createLegendItem("Exam", "#f59e0b"),
                createLegendItem("Event", "#3b82f6"));

        if (SessionManager.getInstance().getRole().equals("ADMIN")) {
            Label hint = new Label("(Double-click a day to add event)");
            hint.setTextFill(Color.GRAY);
            legend.getChildren().add(hint);
        }

        return legend;
    }

    private HBox createLegendItem(String label, String colorCode) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Circle c = new Circle(5, Color.web(colorCode));
        Label l = new Label(label);
        item.getChildren().addAll(c, l);
        return item;
    }

    public VBox getView() {
        return root;
    }
}
