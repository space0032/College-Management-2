package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
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

import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;
import java.util.List;

/**
 * JavaFX Course Management View
 */
public class CourseManagementView {

    private VBox root;
    private TableView<Course> tableView;
    private ObservableList<Course> courseData;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;
    private TextField searchField;
    private com.college.dao.CourseRegistrationDAO registrationDAO; // New DAO

    public CourseManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        this.registrationDAO = new com.college.dao.CourseRegistrationDAO();
        this.courseData = FXCollections.observableArrayList();
        createView();
        if (!"STUDENT".equals(role)) {
            loadCourses();
        }
    }

    private void createView() {
        if ("STUDENT".equals(role)) {
            createStudentView();
        } else {
            createAdminView();
        }
    }

    private void createAdminView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab coursesTab = new Tab("Courses");
        coursesTab.setClosable(false);
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();
        VBox courseContent = new VBox(10, tableSection, buttonSection);
        courseContent.setPadding(new Insets(10, 0, 0, 0));
        coursesTab.setContent(courseContent);

        Tab requestsTab = new Tab("Enrollment Requests");
        requestsTab.setClosable(false);
        requestsTab.setContent(createRequestsTab());

        tabPane.getTabs().addAll(coursesTab, requestsTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(header, tabPane);
    }

    // New Method for Requests Tab
    private VBox createRequestsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        TableView<com.college.dao.CourseRegistrationDAO.RegistrationRequest> table = new TableView<>();

        TableColumn<com.college.dao.CourseRegistrationDAO.RegistrationRequest, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<com.college.dao.CourseRegistrationDAO.RegistrationRequest, String> studentCol = new TableColumn<>(
                "Student");
        studentCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));

        TableColumn<com.college.dao.CourseRegistrationDAO.RegistrationRequest, String> courseCol = new TableColumn<>(
                "Course");
        courseCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCourseName() + " (" + d.getValue().getCourseCode() + ")"));

        TableColumn<com.college.dao.CourseRegistrationDAO.RegistrationRequest, String> dateCol = new TableColumn<>(
                "Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));

        TableColumn<com.college.dao.CourseRegistrationDAO.RegistrationRequest, String> statusCol = new TableColumn<>(
                "Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        table.getColumns().addAll(idCol, studentCol, courseCol, dateCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> {
            table.getItems().setAll(registrationDAO.getPendingRequests());
        });

        Button approveBtn = createButton("Approve", "#22c55e");
        approveBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (registrationDAO.approveRequest(sel.getId())) {
                    showAlert("Success", "Approved!");
                    table.getItems().setAll(registrationDAO.getPendingRequests());
                    loadCourses(); // Update capacities
                } else {
                    showAlert("Error", "Failed to approve.");
                }
            }
        });

        Button rejectBtn = createButton("Reject", "#ef4444");
        rejectBtn.setOnAction(e -> {
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (registrationDAO.rejectRequest(sel.getId())) {
                    showAlert("Success", "Rejected.");
                    table.getItems().setAll(registrationDAO.getPendingRequests());
                } else {
                    showAlert("Error", "Failed to reject.");
                }
            }
        });

        HBox actions = new HBox(10, refreshBtn, approveBtn, rejectBtn);
        content.getChildren().addAll(actions, table);

        // Initial load
        table.getItems().setAll(registrationDAO.getPendingRequests());

        return content;
    }

    private void createStudentView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("My Academics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab myCoursesTab = new Tab("My Courses");
        myCoursesTab.setClosable(false);
        myCoursesTab.setContent(createStudentMyCoursesTab());

        Tab electivesTab = new Tab("Elective Registration");
        electivesTab.setClosable(false);
        electivesTab.setContent(createStudentElectivesTab());

        tabPane.getTabs().addAll(myCoursesTab, electivesTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(title, tabPane);
    }

    private VBox createStudentMyCoursesTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        TableView<Course> table = createBasicCourseTable();
        ObservableList<Course> myData = FXCollections.observableArrayList();
        table.setItems(myData);

        // Load Data
        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadStudentMyCourses(myData));

        Button dropBtn = createButton("Drop Selected", "#ef4444");
        dropBtn.setOnAction(e -> {
            Course sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && "ELECTIVE".equalsIgnoreCase(sel.getCourseType())) {
                Student s = studentDAO.getStudentByUserId(userId);
                if (registrationDAO.dropCourse(s.getId(), sel.getId())) {
                    showAlert("Success", "Course dropped.");
                    loadStudentMyCourses(myData);
                } else {
                    showAlert("Error", "Could not drop course.");
                }
            } else if (sel != null) {
                showAlert("Restriction", "Cannot drop CORE courses.");
            }
        });

        content.getChildren().addAll(new HBox(10, refreshBtn, dropBtn), table);
        loadStudentMyCourses(myData);
        return content;
    }

    private VBox createStudentElectivesTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        TableView<Course> table = createBasicCourseTable();
        ObservableList<Course> electiveData = FXCollections.observableArrayList();
        table.setItems(electiveData);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadStudentElectives(electiveData));

        Button approveBtn = createButton("Request Course", "#22c55e");
        approveBtn.setOnAction(e -> {
            Course sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Student s = studentDAO.getStudentByUserId(userId);
                String result = registrationDAO.registerCourse(s.getId(), sel.getId());
                if ("SUCCESS".equals(result)) {
                    showAlert("Success", "Request sent for " + sel.getName());
                    loadStudentElectives(electiveData);
                } else {
                    showAlert("Info", result);
                }
            }
        });

        content.getChildren().addAll(new HBox(10, refreshBtn, approveBtn), table);
        loadStudentElectives(electiveData);
        return content;
    }

    private void loadStudentMyCourses(ObservableList<Course> data) {
        data.clear();
        Student s = studentDAO.getStudentByUserId(userId);
        if (s == null)
            return;

        List<Course> all = courseDAO.getAllCourses();
        List<Integer> registeredIds = registrationDAO.getRegisteredCourseIds(s.getId());

        for (Course c : all) {
            // Core courses of same dept/sem are auto-included
            boolean isCore = "CORE".equalsIgnoreCase(c.getCourseType())
                    && c.getDepartment() != null
                    && s.getDepartment() != null
                    && c.getDepartment().equalsIgnoreCase(s.getDepartment())
                    && c.getSemester() == s.getSemester();

            // Electives must be registered
            boolean isRegistered = registeredIds.contains(c.getId());

            if (isCore || isRegistered) {
                data.add(c);
            }
        }
    }

    private void loadStudentElectives(ObservableList<Course> data) {
        data.clear();
        Student s = studentDAO.getStudentByUserId(userId);
        if (s == null)
            return;

        List<Course> all = courseDAO.getAllCourses();
        List<Integer> registeredIds = registrationDAO.getRegisteredCourseIds(s.getId());
        List<Integer> pendingIds = registrationDAO.getPendingCourseIds(s.getId());

        for (Course c : all) {
            // Show electives that are NOT registered (or show them as Pending)
            if ("ELECTIVE".equalsIgnoreCase(c.getCourseType())
                    && c.getSemester() == s.getSemester()
                    && !registeredIds.contains(c.getId())) {

                // If pending, visually mark it?
                // For now, list it. The "Request" button should fail if Pending.
                // Or better: Mark status in UI.
                if (pendingIds.contains(c.getId())) {
                    c.setName(c.getName() + " (PENDING)"); // Hack for visibility
                }
                data.add(c);
            }
        }
    }

    // Helper to create table columns
    private TableView<Course> createBasicCourseTable() {
        TableView<Course> table = new TableView<>();

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));

        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Course, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseType()));

        TableColumn<Course, String> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));

        TableColumn<Course, String> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnrolledCount() + " / " + data.getValue().getCapacity()));

        table.getColumns().addAll(codeCol, nameCol, typeCol, creditsCol, capCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;");

        Label title = new Label(role.equals("STUDENT") ? "My Courses" : "Course Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");

        Button searchBtn = createButton("Search", "#14b8a6");
        searchBtn.setOnAction(e -> searchCourses());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadCourses());

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
                        "-fx-border-radius: 12;");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(courseData);

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        codeCol.setPrefWidth(100);

        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Course, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDepartmentName() != null ? data.getValue().getDepartmentName()
                        : data.getValue().getDepartment()));
        deptCol.setPrefWidth(150);

        TableColumn<Course, String> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSemester() > 0 ? String.valueOf(data.getValue().getSemester()) : "-"));
        semCol.setPrefWidth(80);

        TableColumn<Course, String> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));
        creditsCol.setPrefWidth(80);

        TableColumn<Course, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseType()));
        typeCol.setPrefWidth(100);

        TableColumn<Course, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnrolledCount() + " / " + data.getValue().getCapacity()));
        capacityCol.setPrefWidth(100);

        tableView.getColumns().addAll(codeCol, nameCol, deptCol, semCol, creditsCol, typeCol, capacityCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_ALL_COURSES") || session.hasPermission("MANAGE_OWN_COURSES")) {
            Button addBtn = createButton("Add Course", "#22c55e");
            addBtn.setOnAction(e -> showAddCourseDialog());

            Button editBtn = createButton("Edit Course", "#3b82f6");
            editBtn.setOnAction(e -> editCourse());

            Button deleteBtn = createButton("Delete Course", "#ef4444");
            deleteBtn.setOnAction(e -> deleteCourse());

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

    private void loadCourses() {
        courseData.clear();
        List<Course> courses = courseDAO.getAllCourses();

        // Filter for students
        if ("STUDENT".equals(role)) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                String dept = student.getDepartment();
                int sem = student.getSemester();
                for (Course c : courses) {
                    if (dept.equals(c.getDepartment()) && sem == c.getSemester()) {
                        courseData.add(c);
                    }
                }
                return;
            }
        }

        courseData.addAll(courses);
    }

    private void searchCourses() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadCourses();
            return;
        }
        courseData.clear();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            if (c.getName().toLowerCase().contains(keyword) || c.getCode().toLowerCase().contains(keyword)) {
                courseData.add(c);
            }
        }
    }

    private void editCourse() {
        Course selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a course to edit.");
            return;
        }

        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit: " + selected.getName());
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField codeField = new TextField(selected.getCode());
        TextField creditsField = new TextField(String.valueOf(selected.getCredits()));
        TextField semesterField = new TextField(String.valueOf(selected.getSemester()));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("CORE", "ELECTIVE");
        typeCombo.setValue(selected.getCourseType() != null ? selected.getCourseType() : "CORE");

        TextField capacityField = new TextField(String.valueOf(selected.getCapacity()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Credits:"), 0, 2);
        grid.add(creditsField, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semesterField, 1, 3);
        grid.add(new Label("Type:"), 0, 4);
        grid.add(typeCombo, 1, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(capacityField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    selected.setName(nameField.getText());
                    selected.setCode(codeField.getText());
                    selected.setCredits(Integer.parseInt(creditsField.getText()));
                    selected.setSemester(Integer.parseInt(semesterField.getText()));
                    selected.setCourseType(typeCombo.getValue());
                    selected.setCapacity(Integer.parseInt(capacityField.getText()));

                    if (courseDAO.updateCourse(selected)) {
                        return selected;
                    }
                } catch (NumberFormatException e) {
                    // Invalid number
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            loadCourses();
            showAlert("Success", "Course updated successfully!");
        });
    }

    private void deleteCourse() {
        Course selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a course to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Course");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete course: " + selected.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (courseDAO.deleteCourse(selected.getId())) {
                    loadCourses();
                    showAlert("Success", "Course deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete course.");
                }
            }
        });
    }

    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add Course");
        dialog.setHeaderText("Create New Course");

        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        TextField codeField = new TextField();
        codeField.setPromptText("Course Code (e.g. CS101)");

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

        Spinner<Integer> semSpinner = new Spinner<>(1, 8, 1);
        Spinner<Integer> creditsSpinner = new Spinner<>(1, 6, 3);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("CORE", "ELECTIVE");
        typeCombo.setValue("CORE");

        TextField capacityField = new TextField("60"); // Default

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(deptCombo, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semSpinner, 1, 3);
        grid.add(new Label("Credits:"), 0, 4);
        grid.add(creditsSpinner, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeCombo, 1, 5);
        grid.add(new Label("Capacity:"), 0, 6);
        grid.add(capacityField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        nameField.textProperty().addListener((o, old, newValue) -> saveButton
                .setDisable(newValue.trim().isEmpty() || codeField.getText().trim().isEmpty()));
        codeField.textProperty().addListener((o, old, newValue) -> saveButton
                .setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Course c = new Course();
                    c.setName(nameField.getText());
                    c.setCode(codeField.getText());
                    c.setDepartment(deptCombo.getValue());
                    c.setSemester(semSpinner.getValue());
                    c.setCredits(creditsSpinner.getValue());
                    c.setCourseType(typeCombo.getValue());
                    c.setCapacity(Integer.parseInt(capacityField.getText()));

                    courseDAO.addCourse(c);
                    return c;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(c -> {
            loadCourses();
            showAlert("Success", "Course added successfully!");
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
