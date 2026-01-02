package com.college.fx.views;

import com.college.dao.AttendanceDAO;
import com.college.dao.StudentDAO;
import com.college.models.Attendance;
import com.college.models.Student;
import com.college.utils.SessionManager;
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
 * JavaFX Attendance View
 */
public class AttendanceView {

    private VBox root;
    private TableView<Attendance> tableView;
    private ObservableList<Attendance> attendanceData;
    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    public AttendanceView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();
        this.attendanceData = FXCollections.observableArrayList();
        createView();
        loadAttendance();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
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

        Label title = new Label(role.equals("STUDENT") ? "My Attendance" : "Attendance Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadAttendance());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox();
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(attendanceData);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate() != null ? dateFormat.format(data.getValue().getDate()) : "-"
        ));
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
                    setStyle("");
                } else {
                    setText(status);
                    if ("PRESENT".equalsIgnoreCase(status)) {
                        setTextFill(Color.web("#22c55e"));
                    } else if ("ABSENT".equalsIgnoreCase(status)) {
                        setTextFill(Color.web("#ef4444"));
                    } else {
                        setTextFill(Color.web("#f59e0b"));
                    }
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                }
            }
        });

        if (!role.equals("STUDENT")) {
            TableColumn<Attendance, String> studentCol = new TableColumn<>("Student ID");
            studentCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStudentId())));
            studentCol.setPrefWidth(100);
            tableView.getColumns().add(studentCol);
        }

        tableView.getColumns().addAll(dateCol, courseCol, statusCol);
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
            Button markBtn = createButton("Mark Attendance", "#22c55e");
            markBtn.setOnAction(e -> showAlert("Mark Attendance", "Attendance marking dialog would open here."));
            section.getChildren().add(markBtn);
        }

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setPrefHeight(40);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
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
            // Admin/Faculty - for now showing placeholder or handling complex query
            // Ideally we need a method to get recent attendance for all or filtering
            // For now, let's just show a message if empty
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
