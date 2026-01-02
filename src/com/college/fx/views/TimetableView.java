package com.college.fx.views;

import com.college.dao.TimetableDAO;
import com.college.models.Timetable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * JavaFX Timetable View
 */
public class TimetableView {

    private VBox root;
    private GridPane timetableGrid;
    private TimetableDAO timetableDAO;
    private String role;
    private int userId;
    private ComboBox<String> departmentCombo;
    private ComboBox<Integer> semesterCombo;

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] TIME_SLOTS = {"9:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-1:00", "2:00-3:00", "3:00-4:00", "4:00-5:00"};

    public TimetableView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.timetableDAO = new TimetableDAO();
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header with controls
        HBox header = createHeader();
        
        // Timetable grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox gridContainer = new VBox(15);
        gridContainer.setPadding(new Insets(20));
        gridContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );
        
        timetableGrid = createTimetableGrid();
        gridContainer.getChildren().add(timetableGrid);
        scrollPane.setContent(gridContainer);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        root.getChildren().addAll(header, scrollPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );

        Label title = new Label("Weekly Timetable");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Department selector
        Label deptLabel = new Label("Department:");
        deptLabel.setFont(Font.font("Segoe UI", 13));
        departmentCombo = new ComboBox<>();
        departmentCombo.getItems().addAll(timetableDAO.getAllDepartments());
        if (!departmentCombo.getItems().isEmpty()) {
            departmentCombo.setValue(departmentCombo.getItems().get(0));
        }

        // Semester selector
        Label semLabel = new Label("Semester:");
        semLabel.setFont(Font.font("Segoe UI", 13));
        semesterCombo = new ComboBox<>();
        for (int i = 1; i <= 8; i++) {
            semesterCombo.getItems().add(i);
        }
        semesterCombo.setValue(1);

        Button loadBtn = new Button("Load");
        loadBtn.setStyle(
            "-fx-background-color: #14b8a6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        loadBtn.setOnAction(e -> loadTimetable());

        header.getChildren().addAll(title, spacer, deptLabel, departmentCombo, semLabel, semesterCombo, loadBtn);
        return header;
    }

    private GridPane createTimetableGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        // Time header column
        Label timeHeader = createHeaderCell("Time");
        grid.add(timeHeader, 0, 0);

        // Day headers
        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = createHeaderCell(DAYS[i]);
            grid.add(dayLabel, i + 1, 0);
        }

        // Time slots
        for (int row = 0; row < TIME_SLOTS.length; row++) {
            Label timeLabel = createTimeCell(TIME_SLOTS[row]);
            grid.add(timeLabel, 0, row + 1);

            for (int col = 0; col < DAYS.length; col++) {
                Label cell = createEmptyCell();
                grid.add(cell, col + 1, row + 1);
            }
        }

        return grid;
    }

    private Label createHeaderCell(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(120);
        label.setPrefHeight(45);
        label.setStyle("-fx-background-color: #14b8a6; -fx-padding: 10;");
        return label;
    }

    private Label createTimeCell(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#475569"));
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(100);
        label.setPrefHeight(60);
        label.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10;");
        return label;
    }

    private Label createEmptyCell() {
        Label label = new Label("-");
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web("#94a3b8"));
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(120);
        label.setPrefHeight(60);
        label.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-padding: 5;");
        return label;
    }

    private Label createFilledCell(String subject, String room) {
        Label label = new Label(subject + "\n" + room);
        label.setFont(Font.font("Segoe UI", 11));
        label.setTextFill(Color.web("#0f172a"));
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setPrefWidth(120);
        label.setPrefHeight(60);
        label.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #14b8a6; -fx-padding: 5;");
        return label;
    }

    private void loadTimetable() {
        String department = departmentCombo.getValue();
        Integer semester = semesterCombo.getValue();
        
        if (department == null || semester == null) {
            return;
        }

        // Clear and recreate grid
        timetableGrid.getChildren().clear();
        
        // Recreate headers
        timetableGrid.add(createHeaderCell("Time"), 0, 0);
        for (int i = 0; i < DAYS.length; i++) {
            timetableGrid.add(createHeaderCell(DAYS[i]), i + 1, 0);
        }
        
        // Recreate time slots with empty cells
        for (int row = 0; row < TIME_SLOTS.length; row++) {
            timetableGrid.add(createTimeCell(TIME_SLOTS[row]), 0, row + 1);
            for (int col = 0; col < DAYS.length; col++) {
                timetableGrid.add(createEmptyCell(), col + 1, row + 1);
            }
        }

        // Load and populate entries
        try {
            List<Timetable> entries = timetableDAO.getTimetableByDepartmentAndSemester(department, semester);
            
            for (Timetable entry : entries) {
                int dayIndex = getDayIndex(entry.getDayOfWeek());
                int timeIndex = getTimeIndex(entry.getTimeSlot());
                
                if (dayIndex >= 0 && timeIndex >= 0) {
                    String subject = entry.getSubject();
                    String room = entry.getRoomNumber() != null ? entry.getRoomNumber() : "";
                    
                    Label cell = createFilledCell(subject, room);
                    
                    // Remove existing cell
                    timetableGrid.getChildren().removeIf(node -> 
                        GridPane.getColumnIndex(node) != null && 
                        GridPane.getRowIndex(node) != null &&
                        GridPane.getColumnIndex(node) == dayIndex + 1 && 
                        GridPane.getRowIndex(node) == timeIndex + 1
                    );
                    timetableGrid.add(cell, dayIndex + 1, timeIndex + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDayIndex(String day) {
        if (day == null) return -1;
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(day)) return i;
        }
        return -1;
    }

    private int getTimeIndex(String timeSlot) {
        if (timeSlot == null) return -1;
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (TIME_SLOTS[i].equals(timeSlot) || 
                TIME_SLOTS[i].startsWith(timeSlot.substring(0, Math.min(timeSlot.length(), 4)))) {
                return i;
            }
        }
        return -1;
    }

    public VBox getView() {
        return root;
    }
}
