package com.college.fx.views;

import com.college.dao.StudentDAO;
import com.college.dao.DepartmentDAO;
import com.college.models.Student;
import com.college.utils.SessionManager;
import com.college.utils.EnrollmentGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.*;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.college.dao.UserDAO;
import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;

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
    private DepartmentDAO departmentDAO;
    private String role;
    @SuppressWarnings("unused")
    private int userId;
    private TextField searchField;

    public StudentManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.studentDAO = new StudentDAO();
        this.departmentDAO = new DepartmentDAO();
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
                        "-fx-border-width: 1;");

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
                        "-fx-border-color: #e2e8f0;");

        Button searchBtn = new Button("Search");
        searchBtn.setStyle(
                "-fx-background-color: #14b8a6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        searchBtn.setOnAction(e -> searchStudents());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
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
                        "-fx-border-width: 1;");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(studentData);
        tableView.setStyle("-fx-background-color: transparent;");

        // Columns
        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUsername() != null ? data.getValue().getUsername()
                        : String.valueOf(data.getValue().getId())));
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
                data.getValue().getDepartment() != null ? data.getValue().getDepartment() : "-"));
        deptCol.setPrefWidth(150);

        TableColumn<Student, String> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSemester() > 0 ? String.valueOf(data.getValue().getSemester()) : "-"));
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
                        "-fx-cursor: hand;");
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
        showAddStudentDialog();
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Create New Student Profile");

        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        ComboBox<String> deptCombo = new ComboBox<>();
        try {
            DepartmentDAO deptDAO = new DepartmentDAO();
            deptCombo.getItems()
                    .addAll(deptDAO.getAllDepartments().stream().map(Department::getName).collect(Collectors.toList()));
            if (!deptCombo.getItems().isEmpty())
                deptCombo.getSelectionModel().select(0);
        } catch (Exception e) {
            deptCombo.getItems().addAll("CS", "IT", "EC", "ME", "Civil");
        }

        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll("B.Tech", "M.Tech", "MBA", "BCA", "MCA");
        courseCombo.setValue("B.Tech");

        TextField batchField = new TextField();
        batchField.setPromptText("e.g. 2023-2027");

        Spinner<Integer> semSpinner = new Spinner<>(1, 8, 1);

        CheckBox hosteliteCheck = new CheckBox("Is Hostelite?");

        DatePicker enrollDate = new DatePicker(LocalDate.now());

        // User Account Fields
        Separator sep = new Separator();
        Label userLabel = new Label("User Account Credentials");
        userLabel.setStyle("-fx-font-weight: bold");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (optional, default: 123)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);

        grid.add(new Label("Department:"), 0, 4);
        grid.add(deptCombo, 1, 4);
        grid.add(new Label("Course:"), 0, 5);
        grid.add(courseCombo, 1, 5);
        grid.add(new Label("Batch:"), 0, 6);
        grid.add(batchField, 1, 6);
        grid.add(new Label("Semester:"), 0, 7);
        grid.add(semSpinner, 1, 7);
        grid.add(new Label("Enrollment:"), 0, 8);
        grid.add(enrollDate, 1, 8);
        grid.add(hosteliteCheck, 1, 9);

        grid.add(sep, 0, 10, 2, 1);
        grid.add(userLabel, 0, 11, 2, 1);

        Label autoGenLabel = new Label("Enrollment number will be auto-generated and used as username");
        autoGenLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-style: italic;");
        grid.add(autoGenLabel, 0, 12, 2, 1);

        grid.add(new Label("Password:"), 0, 13);
        grid.add(passwordField, 1, 13);

        Label passHint = new Label("(Leave empty for default: 123)");
        passHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        grid.add(passHint, 1, 14);

        dialog.getDialogPane().setContent(grid);

        // Validation - only check name
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || deptCombo.getValue() == null);
        });
        deptCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue == null || nameField.getText().trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate required fields
                    if (nameField.getText().trim().isEmpty() || deptCombo.getValue() == null) {
                        showAlert("Error", "Please fill in Name and Department");
                        return null;
                    }

                    // Auto-generate enrollment number based on department
                    String enrollmentNumber = EnrollmentGenerator.generateStudentEnrollment(deptCombo.getValue());

                    // Use enrollment number as username
                    String username = enrollmentNumber;
                    String password = passwordField.getText().trim().isEmpty() ? "123" : passwordField.getText();

                    // Create User account
                    UserDAO userDAO = new UserDAO();
                    int newUserId = userDAO.addUser(username, password, "STUDENT");

                    if (newUserId != -1) {
                        Student s = new Student();
                        s.setName(nameField.getText());
                        s.setEmail(emailField.getText());
                        s.setPhone(phoneField.getText());
                        s.setAddress(addressField.getText());
                        s.setDepartment(deptCombo.getValue());
                        s.setCourse(courseCombo.getValue());
                        s.setBatch(batchField.getText());
                        s.setSemester(semSpinner.getValue());
                        s.setHostelite(hosteliteCheck.isSelected());
                        s.setEnrollmentDate(
                                Date.from(enrollDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        s.setUserId(newUserId);
                        s.setUsername(enrollmentNumber); // Store enrollment number

                        studentDAO.addStudent(s, newUserId);

                        // Show success with enrollment number
                        Platform.runLater(() -> {
                            showAlert("Success", "Student added successfully!\n\n" +
                                    "Enrollment Number: " + enrollmentNumber + "\n" +
                                    "Username: " + enrollmentNumber + "\n" +
                                    "Password: " + password);
                        });

                        return s;
                    } else {
                        showAlert("Error", "Failed to create user account.");
                        return null;
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to add student: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(student -> {
            if (student != null) {
                loadStudents();
                showAlert("Success", "Student added successfully!");
            }
        });
    }

    private void editStudent() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a student to edit.");
            return;
        }

        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit: " + selected.getName());
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());
        TextField addressField = new TextField(selected.getAddress());
        ComboBox<String> deptCombo = new ComboBox<>();
        List<Department> depts = departmentDAO.getAllDepartments();
        deptCombo.getItems().addAll(depts.stream().map(Department::getName).collect(Collectors.toList()));
        deptCombo.setValue(selected.getDepartment());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new Label("Department:"), 0, 4);
        grid.add(deptCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                selected.setPhone(phoneField.getText());
                selected.setAddress(addressField.getText());
                selected.setDepartment(deptCombo.getValue());

                if (studentDAO.updateStudent(selected)) {
                    return selected;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            loadStudents();
            showAlert("Success", "Student updated successfully!");
        });
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
        if (tableView.getItems().isEmpty()) {
            showAlert("Export", "No data to export.");
            return;
        }
        com.college.utils.FxTableExporter.exportWithDialog(tableView, root.getScene().getWindow());
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
