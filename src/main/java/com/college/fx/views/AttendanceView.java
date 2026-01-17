package com.college.fx.views;

import com.college.dao.AttendanceDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Attendance;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.SearchableStudentComboBox;
import com.college.utils.SessionManager;
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

import java.time.ZoneId;
import java.util.Date;

import java.util.List;

/**
 * JavaFX Attendance View
 */
public class AttendanceView {

    private VBox root;
    private TableView<Attendance> tableView;
    private ObservableList<Attendance> attendanceData;
    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private CourseDAO courseDAO;
    private String role;
    private int userId;

    public AttendanceView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        this.attendanceData = FXCollections.observableArrayList();
        createView();
        loadAttendance();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("glass-card");
        header.setPadding(new Insets(10));

        Label title = new Label(role.equals("STUDENT") ? "My Attendance" : "Attendance Management");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> loadAttendance());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableView.setItems(attendanceData);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDate() != null ? dateFormat.format(data.getValue().getDate()) : "-"));
        dateCol.setPrefWidth(120);

        TableColumn<Attendance, String> courseCol = new TableColumn<>("Course");
        // Assuming courseName is available or we show ID
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCourseId())));
        courseCol.setPrefWidth(150);

        TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Attendance, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-present", "status-absent", "status-late");
                } else {
                    setText(status);
                    if ("PRESENT".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else if ("ABSENT".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    }
                }
            }
        });

        if (!role.equals("STUDENT")) {
            TableColumn<Attendance, String> studentCol = new TableColumn<>("Student ID");
            studentCol.setCellValueFactory(
                    data -> new SimpleStringProperty(String.valueOf(data.getValue().getStudentId())));
            studentCol.setPrefWidth(100);
            tableView.getColumns().add(studentCol);
        }

        tableView.getColumns().addAll(java.util.Arrays.asList(dateCol, courseCol, statusCol));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_ATTENDANCE")) {
            Button markBtn = new Button("+ Mark Attendance");
            markBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            markBtn.setOnAction(e -> showMarkAttendanceDialog());

            Button bulkMarkBtn = new Button("Bulk Mark");
            bulkMarkBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            bulkMarkBtn.setOnAction(e -> showBulkAttendanceDialog());

            section.getChildren().addAll(markBtn, bulkMarkBtn);
        }

        return section;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        // Style classes are added by caller
        return btn;
    }

    private void loadAttendance() {
        attendanceData.clear();

        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                List<Attendance> records = attendanceDAO.getAttendanceByStudent(student.getId());
                attendanceData.addAll(records);
            }
        } else {
            // Admin/Faculty: load all students' attendance
            List<Student> allStudents = studentDAO.getAllStudents();
            for (Student s : allStudents) {
                List<Attendance> records = attendanceDAO.getAttendanceByStudent(s.getId());
                attendanceData.addAll(records);
            }
        }
    }

    private void showMarkAttendanceDialog() {
        Dialog<Attendance> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Mark Attendance");
        dialog.setHeaderText("Mark Student Attendance");
        ButtonType markBtnType = new ButtonType("Mark", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(markBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPrefWidth(250);
        courseCombo.getItems().addAll(courseDAO.getAllCourses());

        SearchableStudentComboBox studentSelector = new SearchableStudentComboBox(studentDAO.getAllStudents());

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PRESENT", "ABSENT", "LATE");
        statusCombo.setValue("PRESENT");

        DialogUtils.addFormRow(grid, "Course:", courseCombo, 0);
        DialogUtils.addFormRow(grid, "Student:", studentSelector, 1);
        DialogUtils.addFormRow(grid, "Date:", datePicker, 2);
        DialogUtils.addFormRow(grid, "Status:", statusCombo, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == markBtnType && courseCombo.getValue() != null && studentSelector.getSelectedStudent() != null) {
                Attendance a = new Attendance();
                a.setStudentId(studentSelector.getSelectedStudent().getId());
                a.setCourseId(courseCombo.getValue().getId());
                if (datePicker.getValue() != null) {
                    a.setDate(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                a.setStatus(statusCombo.getValue());

                if (attendanceDAO.markAttendance(a)) {
                    return a;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            showAlert("Success", "Attendance marked for " + studentSelector.getSelectedStudent().getName());
            loadAttendance();
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showBulkAttendanceDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Bulk Attendance Marking");
        dialog.setHeaderText("Mark Attendance for Entire Class");
        ButtonType saveBtn = new ButtonType("Save All", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(700);

        GridPane selectionGrid = new GridPane();
        selectionGrid.setHgap(10);
        selectionGrid.setVgap(10);

        ComboBox<Course> courseCombo = new ComboBox<>();
        try {
            CourseDAO courseDAO = new CourseDAO();
            courseCombo.getItems().addAll(courseDAO.getAllCourses());
        } catch (Exception e) {
            /* Ignore */ }

        ComboBox<Integer> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8);
        semesterCombo.setPromptText("Select Semester");

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

        Label courseLabel = new Label("Course:");
        courseLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        selectionGrid.add(courseLabel, 0, 0);
        selectionGrid.add(courseCombo, 1, 0);
        
        Label semesterLabel = new Label("Semester:");
        semesterLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        selectionGrid.add(semesterLabel, 0, 1);
        selectionGrid.add(semesterCombo, 1, 1);
        
        Label dateLabel = new Label("Date:");
        dateLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        selectionGrid.add(dateLabel, 0, 2);
        selectionGrid.add(datePicker, 1, 2);

        Button loadBtn = new Button("Load Students");
        loadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");

        TableView<BulkAttendanceRecord> attendanceTable = new TableView<>();
        attendanceTable.setPrefHeight(350);
        ObservableList<BulkAttendanceRecord> records = FXCollections.observableArrayList();
        attendanceTable.setItems(records);

        TableColumn<BulkAttendanceRecord, String> nameCol = new TableColumn<>("Student");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName));
        nameCol.setPrefWidth(200);

        TableColumn<BulkAttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<BulkAttendanceRecord, String>() {
            private ComboBox<String> comboBox = new ComboBox<>();
            {
                comboBox.getItems().addAll("PRESENT", "ABSENT", "LATE");
                comboBox.setValue("PRESENT");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    BulkAttendanceRecord record = getTableView().getItems().get(getIndex());
                    comboBox.valueProperty().addListener((obs, old, newVal) -> record.status = newVal);
                    setGraphic(comboBox);
                } else
                    setGraphic(null);
            }
        });
        statusCol.setPrefWidth(140);

        attendanceTable.getColumns().addAll(java.util.Arrays.asList(nameCol, statusCol));

        loadBtn.setOnAction(e -> {
            if (courseCombo.getValue() == null) {
                showAlert("Error", "Please select a course");
                return;
            }
            if (semesterCombo.getValue() == null) {
                showAlert("Error", "Please select a semester");
                return;
            }

            Course selectedCourse = courseCombo.getValue();
            int selectedSemester = semesterCombo.getValue();

            records.clear();
            // Filter students by course department and semester
            for (Student s : studentDAO.getAllStudents()) {
                if (s.getDepartment().equals(selectedCourse.getDepartment()) &&
                        s.getSemester() == selectedSemester) {
                    records.add(new BulkAttendanceRecord(s.getId(), s.getName()));
                }
            }

            if (records.isEmpty()) {
                showAlert("Info", "No students found for this course/semester combination");
            }
        });

        content.getChildren().addAll(selectionGrid, loadBtn, attendanceTable);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && courseCombo.getValue() != null && datePicker.getValue() != null) {
                int saved = 0;
                Date attendanceDate = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                for (BulkAttendanceRecord r : records) {
                    Attendance a = new Attendance();
                    a.setStudentId(r.studentId);
                    a.setCourseId(courseCombo.getValue().getId());
                    a.setDate(attendanceDate);
                    a.setStatus(r.status);
                    if (attendanceDAO.markAttendance(a))
                        saved++;
                }
                showAlert("Success", saved + " attendance records saved!");
                loadAttendance();
                return true;
            }
            return false;
        });

        dialog.showAndWait();
    }

    private static class BulkAttendanceRecord {
        int studentId;
        String studentName;
        String status;

        BulkAttendanceRecord(int id, String name) {
            studentId = id;
            studentName = name;
            status = "PRESENT";
        }
    }

    public VBox getView() {
        return root;
    }
}
