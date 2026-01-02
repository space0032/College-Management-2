package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.models.Course;
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
    private String role;
    private TextField searchField;

    public CourseManagementView(String role, int userId) {
        this.role = role;
        this.courseDAO = new CourseDAO();
        this.courseData = FXCollections.observableArrayList();
        createView();
        loadCourses();
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
            "-fx-border-radius: 12;"
        );
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
            data.getValue().getDepartmentName() != null ? data.getValue().getDepartmentName() : data.getValue().getDepartment()
        ));
        deptCol.setPrefWidth(150);

        TableColumn<Course, String> semCol = new TableColumn<>("Semester");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSemester() > 0 ? String.valueOf(data.getValue().getSemester()) : "-"
        ));
        semCol.setPrefWidth(80);

        TableColumn<Course, String> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));
        creditsCol.setPrefWidth(80);

        tableView.getColumns().addAll(codeCol, nameCol, deptCol, semCol, creditsCol);
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
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private void loadCourses() {
        courseData.clear();
        List<Course> courses = courseDAO.getAllCourses();
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
        showAlert("Edit Course", "Edit dialog for: " + selected.getName());
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
             deptCombo.getItems().addAll(deptDAO.getAllDepartments().stream().map(Department::getName).collect(Collectors.toList()));
             if(!deptCombo.getItems().isEmpty()) deptCombo.getSelectionModel().select(0);
        } catch(Exception e) {
             deptCombo.getItems().addAll("CS", "IT", "EC", "ME", "Civil");
        }

        Spinner<Integer> semSpinner = new Spinner<>(1, 8, 1);
        Spinner<Integer> creditsSpinner = new Spinner<>(1, 6, 3);

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

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        nameField.textProperty().addListener((o, old, newValue) -> 
            saveButton.setDisable(newValue.trim().isEmpty() || codeField.getText().trim().isEmpty()));
        codeField.textProperty().addListener((o, old, newValue) -> 
            saveButton.setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Course c = new Course();
                c.setName(nameField.getText());
                c.setCode(codeField.getText());
                c.setDepartment(deptCombo.getValue());
                c.setSemester(semSpinner.getValue());
                c.setCredits(creditsSpinner.getValue());
                
                courseDAO.addCourse(c); // assuming returns int or boolean, we ignore result for now or strict check?
                // CourseDAO typically returns ID.
                return c;
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
