package com.college.fx.views;

import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;

import com.college.utils.CSVImporter;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.*;

import javafx.scene.layout.*;

import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;

import java.util.List;
import java.io.File;

/**
 * JavaFX Student Management View
 * Shows student list with CRUD operations
 */
public class StudentManagementView implements com.college.fx.interfaces.ContextAware {

    private VBox root;
    private TableView<Student> tableView;
    private ObservableList<Student> studentData;
    private ObservableList<Student> allStudents;
    private StudentDAO studentDAO;
    private DepartmentDAO departmentDAO;
    private String role;
    @SuppressWarnings("unused")
    private int userId;
    private TextField searchField;
    private ComboBox<String> deptFilter;
    private Label statsLabel;

    public StudentManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.studentDAO = new StudentDAO();
        this.departmentDAO = new DepartmentDAO();
        this.studentData = FXCollections.observableArrayList();
        this.allStudents = FXCollections.observableArrayList();
        createView();
        loadStudents();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

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
        header.getStyleClass().add("glass-card");

        Label title = new Label("Student Management");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statsLabel = new Label();
        statsLabel.getStyleClass().add("text-white");
        statsLabel.setStyle("-fx-font-size: 14px;");

        // Search
        searchField = new TextField();
        searchField.setPromptText("Search students...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, newVal) -> filterStudents());

        Label filterLabel = new Label("Dept:");
        filterLabel.getStyleClass().add("text-white");

        deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All");
        deptFilter.getItems().addAll(departmentDAO.getAllDepartments().stream()
                .map(Department::getName).collect(Collectors.toList()));
        deptFilter.setValue("All");
        deptFilter.getStyleClass().add("combo-box");
        deptFilter.setOnAction(e -> filterStudents());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("icon-button");
        refreshBtn.setOnAction(e -> loadStudents());

        header.getChildren().addAll(title, spacer, statsLabel, searchField, filterLabel, deptFilter, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(studentData);
        tableView.getStyleClass().add("glass-table");
        tableView.setStyle("-fx-background-color: transparent;");

        // Columns - Enrollment Number should be first and prominent
        TableColumn<Student, String> enrollmentCol = new TableColumn<>("Enrollment Number");
        enrollmentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUsername() != null ? data.getValue().getUsername() : "N/A"));
        enrollmentCol.setPrefWidth(140);
        enrollmentCol.setStyle("-fx-font-weight: bold;");

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
        deptCol.setPrefWidth(120);

        TableColumn<Student, String> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSemester() > 0 ? String.valueOf(data.getValue().getSemester()) : "-"));
        semCol.setPrefWidth(80);

        TableColumn<Student, String> batchCol = new TableColumn<>("Batch");
        batchCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBatch()));
        batchCol.setPrefWidth(100);

        tableView.getColumns()
                .addAll(java.util.Arrays.asList(enrollmentCol, nameCol, emailCol, phoneCol, deptCol, semCol, batchCol));

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
            Button addBtn = new Button("+ Add Student");
            addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            addBtn.setOnAction(e -> addStudent());

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            editBtn.setOnAction(e -> editStudent());

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            deleteBtn.setOnAction(e -> deleteStudent());

            Button importBtn = new Button("Import CSV");
            importBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            importBtn.setOnAction(e -> importStudentsFromCSV());

            section.getChildren().addAll(addBtn, editBtn, deleteBtn, importBtn);
        }

        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        viewProfileBtn.setOnAction(e -> viewStudentProfile());
        section.getChildren().add(viewProfileBtn);

        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        exportBtn.setOnAction(e -> exportData());
        section.getChildren().add(exportBtn);

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
        btn.setPrefHeight(40);
        btn.getStyleClass().add("icon-button");
        return btn;
    }

    private void loadStudents() {
        allStudents.clear();
        List<Student> students;
        if ("WARDEN".equals(role)) {
            students = studentDAO.getHostelStudents();
        } else {
            students = studentDAO.getAllStudents();
        }
        allStudents.addAll(students);
        filterStudents();
        updateStats();
    }

    private void filterStudents() {
        if (allStudents == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String deptValue = deptFilter.getValue();

        List<Student> filtered = allStudents.stream()
                .filter(s -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            s.getName().toLowerCase().contains(searchText) ||
                            (s.getEmail() != null && s.getEmail().toLowerCase().contains(searchText)) ||
                            (s.getUsername() != null && s.getUsername().toLowerCase().contains(searchText)) ||
                            (s.getBatch() != null && s.getBatch().toLowerCase().contains(searchText));

                    boolean matchesDept = deptValue.equals("All") ||
                            (s.getDepartment() != null && s.getDepartment().equals(deptValue));

                    return matchesSearch && matchesDept;
                })
                .collect(Collectors.toList());

        studentData.setAll(filtered);
    }

    private void updateStats() {
        if (allStudents == null) return;
        statsLabel.setText(String.format("Total: %d students", allStudents.size()));
    }

    private void searchStudents() {
        filterStudents();
    }

    private void addStudent() {
        showAddStudentDialog();
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
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

        DialogUtils.addFormRow(grid, "Name:", nameField, 0);
        DialogUtils.addFormRow(grid, "Email:", emailField, 1);
        DialogUtils.addFormRow(grid, "Phone:", phoneField, 2);
        DialogUtils.addFormRow(grid, "Address:", addressField, 3);
        DialogUtils.addFormRow(grid, "Department:", deptCombo, 4);
        DialogUtils.addFormRow(grid, "Course:", courseCombo, 5);
        DialogUtils.addFormRow(grid, "Batch:", batchField, 6);
        DialogUtils.addFormRow(grid, "Semester:", semSpinner, 7);
        DialogUtils.addFormRow(grid, "Enrollment:", enrollDate, 8);
        grid.add(hosteliteCheck, 1, 9);

        grid.add(sep, 0, 10, 2, 1);
        grid.add(userLabel, 0, 11, 2, 1);

        Label autoGenLabel = new Label("Enrollment number will be auto-generated and used as username");
        autoGenLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-style: italic;");
        grid.add(autoGenLabel, 0, 12, 2, 1);

        DialogUtils.addFormRow(grid, "Password:", passwordField, 13);

        Label passHint = new Label("(Leave empty for default: 123)");
        passHint.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");
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
                    String name = nameField.getText().trim();
                    String email = emailField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String selectedDept = deptCombo.getValue();

                    // Name validation
                    if (name.isEmpty()) {
                        showAlert("Validation Error", "Name is required");
                        return null;
                    }

                    // Email validation
                    if (email.isEmpty()) {
                        showAlert("Validation Error", "Email is required");
                        return null;
                    }
                    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        showAlert("Validation Error", "Please enter a valid email (e.g., student@college.com)");
                        return null;
                    }

                    // Phone validation (if provided)
                    if (!phone.isEmpty() && !phone.matches("^[0-9]{10}$")) {
                        showAlert("Validation Error", "Phone must be 10 digits");
                        return null;
                    }

                    // Department validation
                    if (selectedDept == null || selectedDept.isEmpty()) {
                        showAlert("Validation Error", "Please select a department");
                        return null;
                    }

                    // Validation passed - use EnrollmentDAO
                    String password = passwordField.getText().trim().isEmpty() ? "123" : passwordField.getText();

                    // Create minimal Student object for enrollment
                    Student s = new Student();
                    s.setName(name);
                    s.setEmail(email);
                    s.setPhone(phone);
                    s.setAddress(addressField.getText());
                    s.setDepartment(selectedDept);
                    s.setCourse(courseCombo.getValue());
                    s.setBatch(batchField.getText());
                    s.setSemester(semSpinner.getValue());
                    s.setHostelite(hosteliteCheck.isSelected());
                    s.setEnrollmentDate(
                            Date.from(enrollDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

                    com.college.dao.EnrollmentDAO enrollmentDAO = null;
                    Student enrolledStudent = null;
                    try {
                        enrollmentDAO = new com.college.dao.EnrollmentDAO();
                        enrolledStudent = enrollmentDAO.enrollStudent(s, password);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        showAlert("Error", "Failed to initialize EnrollmentDAO: " + t.getMessage());
                        return null;
                    }

                    if (enrolledStudent != null) {
                        Student finalStudent = enrolledStudent;
                        // Show success
                        Platform.runLater(() -> {
                            showAlert("Success", "Student enrolled successfully!\n\n" +
                                    "Enrollment Number: " + finalStudent.getUsername() + "\n" +
                                    "Username: " + finalStudent.getUsername() + "\n" +
                                    "Password: " + password);
                        });
                        return enrolledStudent;
                    } else {
                        showAlert("Error", "Failed to enroll student. Check logs.");
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
        DialogUtils.styleDialog(dialog);
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

        DialogUtils.addFormRow(grid, "Name:", nameField, 0);
        DialogUtils.addFormRow(grid, "Email:", emailField, 1);
        DialogUtils.addFormRow(grid, "Phone:", phoneField, 2);
        DialogUtils.addFormRow(grid, "Address:", addressField, 3);
        DialogUtils.addFormRow(grid, "Department:", deptCombo, 4);

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
        DialogUtils.styleDialog(confirm);
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
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void viewStudentProfile() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a student to view.");
            return;
        }

        // Fetch full details including new profile fields
        Student fullDetails = studentDAO.getStudentById(selected.getId());
        if (fullDetails == null)
            fullDetails = selected;

        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Student Profile");
        dialog.setHeaderText("Profile: " + fullDetails.getName());

        ButtonType closeBtn = new ButtonType("Close", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        // Allow editing if Admin or Faculty with permission
        boolean canEdit = SessionManager.getInstance().hasPermission("MANAGE_STUDENTS");

        StudentProfileView profileView = new StudentProfileView(fullDetails, canEdit, () -> loadStudents());

        // Set content size
        dialog.getDialogPane().setContent(profileView.getView());
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(600);

        dialog.showAndWait();
    }

    private void importStudentsFromCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(tableView.getScene().getWindow());

        if (file != null) {
            // Show progress
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            DialogUtils.styleDialog(progressAlert);
            progressAlert.setTitle("Importing");
            progressAlert.setHeaderText("Importing students from CSV...");
            progressAlert.setContentText("Please wait...");
            progressAlert.show();

            // Import in background
            new Thread(() -> {
                CSVImporter.ImportResult result = CSVImporter.importStudents(file);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    progressAlert.close();
                    loadStudents(); // Refresh table

                    Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                    DialogUtils.styleDialog(resultAlert);
                    resultAlert.setTitle("Import Complete");
                    resultAlert.setHeaderText(null);
                    resultAlert.setContentText(result.getSummary());

                    TextArea detailsArea = new TextArea(result.getSummary());
                    detailsArea.setEditable(false);
                    detailsArea.setWrapText(true);
                    detailsArea.setMaxWidth(Double.MAX_VALUE);
                    detailsArea.setMaxHeight(Double.MAX_VALUE);

                    resultAlert.getDialogPane().setExpandableContent(detailsArea);
                    resultAlert.showAndWait();
                });
            }).start();
        }
    }

    public VBox getView() {
        return root;
    }

    @Override
    public String getContextData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current View: Student Management\n");
        sb.append("Visible Student Data:\n");
        sb.append("Name, Email, Dept, Batch\n");

        if (studentData != null) {
            // Limit to top 20 to avoid token limits
            studentData.stream().limit(20).forEach(s -> {
                sb.append(String.format("%s, %s, %s, %s\n",
                        s.getName(), s.getEmail(), s.getDepartment(), s.getBatch()));
            });

            if (studentData.size() > 20) {
                sb.append("... (" + (studentData.size() - 20) + " more students not shown)");
            }
        } else {
            sb.append("No data loaded.");
        }

        return sb.toString();
    }
}
