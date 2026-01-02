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
            addBtn.setOnAction(e -> showAlert("Add Course", "Add course dialog would open here."));

            Button editBtn = createButton("Edit Course", "#3b82f6");
            editBtn.setOnAction(e -> editCourse());

            Button deleteBtn = createButton("Delete Course", "#ef4444");
            deleteBtn.setOnAction(e -> deleteCourse());

            section.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        Button exportBtn = createButton("Export", "#64748b");
        exportBtn.setOnAction(e -> showAlert("Export", "Export functionality."));
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
