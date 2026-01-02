package com.college.fx.views;

import com.college.dao.AssignmentDAO;
import com.college.dao.StudentDAO;
import com.college.models.Assignment;
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
 * JavaFX Assignments View
 */
public class AssignmentsView {

    private VBox root;
    private TableView<Assignment> tableView;
    private ObservableList<Assignment> assignmentData;
    private AssignmentDAO assignmentDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    public AssignmentsView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.assignmentDAO = new AssignmentDAO();
        this.studentDAO = new StudentDAO();
        this.assignmentData = FXCollections.observableArrayList();
        createView();
        loadAssignments();
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

        Label title = new Label("Assignments");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadAssignments());

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
        tableView.setItems(assignmentData);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TableColumn<Assignment, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCourseName() != null ? data.getValue().getCourseName() : String.valueOf(data.getValue().getCourseId())
        ));
        courseCol.setPrefWidth(150);

        TableColumn<Assignment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Assignment, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(250);

        TableColumn<Assignment, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDueDate() != null ? dateFormat.format(data.getValue().getDueDate()) : "-"
        ));
        dueCol.setPrefWidth(120);

        tableView.getColumns().addAll(courseCol, titleCol, descCol, dueCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_ASSIGNMENTS")) {
            Button addBtn = createButton("New Assignment", "#22c55e");
            addBtn.setOnAction(e -> showAlert("New Assignment", "Create assignment dialog."));

            Button editBtn = createButton("Edit", "#3b82f6");
            editBtn.setOnAction(e -> showAlert("Edit Assignment", "Edit assignment dialog."));
            
            section.getChildren().addAll(addBtn, editBtn);
        } else if (role.equals("STUDENT")) {
            Button submitBtn = createButton("Submit Assignment", "#14b8a6");
            submitBtn.setOnAction(e -> showAlert("Submit Assignment", "Submission dialog would open here."));
            section.getChildren().add(submitBtn);
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

    private void loadAssignments() {
        assignmentData.clear();
        
        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                // Assuming students see assignments for their semester
                List<Assignment> assignments = assignmentDAO.getAssignmentsBySemester(student.getSemester());
                assignmentData.addAll(assignments);
            }
        } else {
             // Faculty see assignments they created or by semester
             // Basic implementation: get by semester 1 for now or all if possible
             List<Assignment> assignments = assignmentDAO.getAssignmentsBySemester(1);
             assignmentData.addAll(assignments);
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
