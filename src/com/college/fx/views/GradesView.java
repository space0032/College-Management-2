package com.college.fx.views;

import com.college.dao.GradeDAO;
import com.college.dao.StudentDAO;
import com.college.models.Grade;
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

import java.util.List;

/**
 * JavaFX Grades View
 */
public class GradesView {

    private VBox root;
    private TableView<Grade> tableView;
    private ObservableList<Grade> gradeData;
    private GradeDAO gradeDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    public GradesView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.gradeDAO = new GradeDAO();
        this.studentDAO = new StudentDAO();
        this.gradeData = FXCollections.observableArrayList();
        createView();
        loadGrades();
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

        Label title = new Label(role.equals("STUDENT") ? "My Grades" : "Grade Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadGrades());

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
        tableView.setItems(gradeData);

        TableColumn<Grade, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCourseName() != null ? data.getValue().getCourseName() : String.valueOf(data.getValue().getCourseId())
        ));
        courseCol.setPrefWidth(200);

        TableColumn<Grade, String> examCol = new TableColumn<>("Exam Type");
        examCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExamType()));
        examCol.setPrefWidth(120);

        TableColumn<Grade, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMarksObtained() + " / " + data.getValue().getMaxMarks()
        ));
        marksCol.setPrefWidth(120);

        TableColumn<Grade, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));
        gradeCol.setPrefWidth(80);
        gradeCol.setCellFactory(col -> new TableCell<Grade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
                    if (item.startsWith("A")) setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a;");
                    else if (item.startsWith("F")) setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
                }
            }
        });

        if (!role.equals("STUDENT")) {
            TableColumn<Grade, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStudentName() != null ? data.getValue().getStudentName() : String.valueOf(data.getValue().getStudentId())
            ));
            studentCol.setPrefWidth(150);
            tableView.getColumns().add(0, studentCol);
        }

        tableView.getColumns().addAll(courseCol, examCol, marksCol, gradeCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_GRADES")) {
            Button addBtn = createButton("Add Grades", "#22c55e");
            addBtn.setOnAction(e -> showAlert("Add Grades", "Grade entry dialog would open here."));
            section.getChildren().add(addBtn);
        }

        Button exportBtn = createButton("Export Report", "#64748b");
        section.getChildren().add(exportBtn);

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

    private void loadGrades() {
        gradeData.clear();
        
        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                List<Grade> grades = gradeDAO.getGradesByStudent(student.getId());
                gradeData.addAll(grades);
            }
        } else {
            // For admin/faculty - ideally filter by course first
            // Just showing nothing or needing a filter
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
