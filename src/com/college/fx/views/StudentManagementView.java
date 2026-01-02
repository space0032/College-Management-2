package com.college.fx.views;

import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * JavaFX Student Management View
 * Shows student list with CRUD operations
 */
public class StudentManagementView {

    private VBox root;
    private TableView<Student> tableView;
    private ObservableList<Student> studentData;
    private StudentDAO studentDAO;
    private String role;
    private int userId;
    private TextField searchField;

    public StudentManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.studentDAO = new StudentDAO();
        this.studentData = FXCollections.observableArrayList();
        createView();
        loadStudents();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header section
        HBox header = createHeader();

        // Table section
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Button section
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );

        Label title = new Label("Student Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search
        searchField = new TextField();
        searchField.setPromptText("Search students...");
        searchField.setPrefWidth(250);
        searchField.setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #e2e8f0;"
        );

        Button searchBtn = new Button("Search");
        searchBtn.setStyle(
            "-fx-background-color: #14b8a6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        searchBtn.setOnAction(e -> searchStudents());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        refreshBtn.setOnAction(e -> loadStudents());

        header.getChildren().addAll(title, spacer, searchField, searchBtn, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox();
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(studentData);
        tableView.setStyle("-fx-background-color: transparent;");

        // Columns
        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getUsername() != null ? data.getValue().getUsername() : String.valueOf(data.getValue().getId())
        ));
        idCol.setPrefWidth(100);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<Student, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        phoneCol.setPrefWidth(120);

        TableColumn<Student, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDepartment() != null ? data.getValue().getDepartment() : "-"
        ));
        deptCol.setPrefWidth(150);

        TableColumn<Student, String> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSemester() > 0 ? String.valueOf(data.getValue().getSemester()) : "-"
        ));
        semCol.setPrefWidth(80);

        TableColumn<Student, String> batchCol = new TableColumn<>("Batch");
        batchCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBatch()));
        batchCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, deptCol, semCol, batchCol);

        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_STUDENTS")) {
            Button addBtn = createButton("Add Student", "#22c55e");
            addBtn.setOnAction(e -> addStudent());

            Button editBtn = createButton("Edit Student", "#3b82f6");
            editBtn.setOnAction(e -> editStudent());

            Button deleteBtn = createButton("Delete Student", "#ef4444");
            deleteBtn.setOnAction(e -> deleteStudent());

            section.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        Button exportBtn = createButton("Export", "#64748b");
        exportBtn.setOnAction(e -> exportData());
        section.getChildren().add(exportBtn);

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
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

    private void loadStudents() {
        studentData.clear();
        List<Student> students;
        if ("WARDEN".equals(role)) {
            students = studentDAO.getHostelStudents();
        } else {
            students = studentDAO.getAllStudents();
        }
        studentData.addAll(students);
    }

    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }
        
        studentData.clear();
        List<Student> students = studentDAO.searchStudents(keyword);
        studentData.addAll(students);
    }

    private void addStudent() {
        showAlert("Add Student", "Add student dialog would open here.");
    }

    private void editStudent() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a student to edit.");
            return;
        }
        showAlert("Edit Student", "Edit dialog for: " + selected.getName());
    }

    private void deleteStudent() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a student to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Student");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete student: " + selected.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (studentDAO.deleteStudent(selected.getId())) {
                    loadStudents();
                    showAlert("Success", "Student deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete student.");
                }
            }
        });
    }

    private void exportData() {
        showAlert("Export", "Export functionality would be implemented here.");
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
