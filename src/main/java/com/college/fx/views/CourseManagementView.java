package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
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
    private FacultyDAO facultyDAO;
    private String role;
    private int userId;
    private TextField searchField;
    private com.college.dao.CourseRegistrationDAO registrationDAO; // New DAO

    public CourseManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        this.facultyDAO = new FacultyDAO();
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
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("pill-tab-pane");

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
        table.getStyleClass().add("glass-table");

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

        table.getColumns().addAll(java.util.Arrays.asList(idCol, studentCol, courseCol, dateCol, statusCol));
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
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("My Academics");
        title.getStyleClass().add("section-title");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("pill-tab-pane");

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
        table.getStyleClass().add("glass-table");

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

        table.getColumns().addAll(java.util.Arrays.asList(codeCol, nameCol, typeCol, creditsCol, capCol));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("glass-card");

        Label title = new Label(role.equals("STUDENT") ? "My Courses" : "Course Management");
        title.getStyleClass().add("section-title");

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

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
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

        TableColumn<Course, String> facultyCol = new TableColumn<>("Instructor");
        facultyCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFacultyName() != null ? data.getValue().getFacultyName() : "-"));
        facultyCol.setPrefWidth(150);

        tableView.getColumns().addAll(java.util.Arrays.asList(codeCol, nameCol, deptCol, semCol, creditsCol, typeCol,
                capacityCol, facultyCol));
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
        // Show loading state if desired (optional)
        tableView.setPlaceholder(new Label("Loading courses..."));

        javafx.concurrent.Task<List<Course>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<Course> call() throws Exception {
                return courseDAO.getAllCourses();
            }
        };

        task.setOnSucceeded(e -> {
            List<Course> courses = task.getValue();
            courseData.clear();

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
                }
            } else {
                courseData.addAll(courses);
            }

            // Restore placeholder
            tableView.setPlaceholder(new Label("No courses found."));
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace(); // Log error
            showAlert("Error", "Failed to load courses.");
            tableView.setPlaceholder(new Label("Error loading courses."));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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
        DialogUtils.styleDialog(dialog);
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

        ComboBox<Department> deptCombo = new ComboBox<>();
        try {
            DepartmentDAO deptDAO = new DepartmentDAO();
            List<Department> depts = deptDAO.getAllDepartments();
            deptCombo.getItems().addAll(depts);

            if (depts != null && !depts.isEmpty()) {
                // Try to select if exists
                if (selected.getDepartmentId() > 0) {
                    depts.stream().filter(d -> d.getId() == selected.getDepartmentId()).findFirst()
                            .ifPresent(deptCombo::setValue);
                } else if (selected.getDepartment() != null) {
                    // Fallback check by name
                    depts.stream().filter(d -> d.getName().equalsIgnoreCase(selected.getDepartment())).findFirst()
                            .ifPresent(deptCombo::setValue);
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        ComboBox<Faculty> facultyCombo = new ComboBox<>();
        List<Faculty> allFaculty = facultyDAO.getAllFaculty();
        facultyCombo.getItems().addAll(allFaculty);
        if (selected.getFacultyId() > 0) {
            allFaculty.stream().filter(f -> f.getId() == selected.getFacultyId()).findFirst()
                    .ifPresent(facultyCombo::setValue);
        }

        DialogUtils.addFormRow(grid, "Name:", nameField, 0);
        DialogUtils.addFormRow(grid, "Code:", codeField, 1);
        DialogUtils.addFormRow(grid, "Department:", deptCombo, 2);
        DialogUtils.addFormRow(grid, "Credits:", creditsField, 3);
        DialogUtils.addFormRow(grid, "Semester:", semesterField, 4);
        DialogUtils.addFormRow(grid, "Type:", typeCombo, 5);
        DialogUtils.addFormRow(grid, "Capacity:", capacityField, 6);
        DialogUtils.addFormRow(grid, "Instructor:", facultyCombo, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    selected.setName(nameField.getText());
                    selected.setCode(codeField.getText());
                    selected.setCredits(Integer.parseInt(creditsField.getText()));
                    selected.setSemester(Integer.parseInt(semesterField.getText()));
                    if (deptCombo.getValue() != null) {
                        selected.setDepartment(deptCombo.getValue().getName());
                        selected.setDepartmentId(deptCombo.getValue().getId());
                    }
                    selected.setCourseType(typeCombo.getValue());
                    selected.setCapacity(Integer.parseInt(capacityField.getText()));
                    if (facultyCombo.getValue() != null) {
                        System.out.println("DEBUG: Selected Faculty: " + facultyCombo.getValue().getName() + " ID: "
                                + facultyCombo.getValue().getId());
                        selected.setFacultyId(facultyCombo.getValue().getId());
                        selected.setFacultyName(facultyCombo.getValue().getName());
                    } else {
                        System.out.println("DEBUG: Selected Faculty is NULL");
                        selected.setFacultyId(0);
                        selected.setFacultyName(null);
                    }

                    System.out.println("DEBUG: Updating course " + selected.getCode() + " with Faculty ID: "
                            + selected.getFacultyId());
                    if (courseDAO.updateCourse(selected)) {
                        return selected;
                    } else {
                        System.out.println("DEBUG: Update Failed");
                    }
                } catch (NumberFormatException e) {
                    // Invalid number
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c ->

        {
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
        DialogUtils.styleDialog(confirm);
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
        DialogUtils.styleDialog(dialog);
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

        DialogUtils.addFormRow(grid, "Name:", nameField, 0);
        DialogUtils.addFormRow(grid, "Code:", codeField, 1);
        DialogUtils.addFormRow(grid, "Department:", deptCombo, 2);
        DialogUtils.addFormRow(grid, "Semester:", semSpinner, 3);
        DialogUtils.addFormRow(grid, "Credits:", creditsSpinner, 4);
        DialogUtils.addFormRow(grid, "Type:", typeCombo, 5);
        DialogUtils.addFormRow(grid, "Capacity:", capacityField, 6);

        ComboBox<Faculty> facultyCombo = new ComboBox<>();
        facultyCombo.getItems().addAll(facultyDAO.getAllFaculty());
        DialogUtils.addFormRow(grid, "Instructor:", facultyCombo, 7);

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
                    if (facultyCombo.getValue() != null) {
                        c.setFacultyId(facultyCombo.getValue().getId());
                    }

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
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
