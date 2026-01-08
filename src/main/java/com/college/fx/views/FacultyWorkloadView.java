package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.FacultyDAO;
import com.college.models.Course;
import com.college.models.Faculty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.stream.Collectors;

public class FacultyWorkloadView {

    private VBox root;
    private TableView<FacultyWorkloadItem> table;
    private ObservableList<FacultyWorkloadItem> data;
    private FacultyDAO facultyDAO;
    private CourseDAO courseDAO;

    public FacultyWorkloadView() {
        this.facultyDAO = new FacultyDAO();
        this.courseDAO = new CourseDAO();
        this.data = FXCollections.observableArrayList();
        createView();
        loadData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Faculty Workload Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        // title.setTextFill(Color.web("#0f172a"));

        HBox toolbar = new HBox(10);
        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());
        toolbar.getChildren().add(refreshBtn);

        table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setItems(data);

        TableColumn<FacultyWorkloadItem, String> nameCol = new TableColumn<>("Faculty Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFaculty().getName()));

        TableColumn<FacultyWorkloadItem, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFaculty().getDepartment()));

        TableColumn<FacultyWorkloadItem, String> countCol = new TableColumn<>("Courses Assigned");
        countCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStats().count)));

        TableColumn<FacultyWorkloadItem, String> creditCol = new TableColumn<>("Total Credits");
        creditCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStats().credits)));

        TableColumn<FacultyWorkloadItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(width -> new TableCell<>() {
            Button btn = createButton("Manage", "#8b5cf6");
            {
                btn.setPrefWidth(80);
                btn.setPrefHeight(30);
                btn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-size: 11px;");
                btn.setOnAction(e -> manageAssignments(getTableView().getItems().get(getIndex()).getFaculty()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(nameCol, deptCol, countCol, creditCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, toolbar, table);
    }

    private void loadData() {
        data.clear();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        for (Faculty f : facultyList) {
            CourseDAO.WorkloadStats stats = courseDAO.getFacultyWorkload(f.getId());
            data.add(new FacultyWorkloadItem(f, stats));
        }
    }

    private void manageAssignments(Faculty faculty) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Assignments: " + faculty.getName());
        dialog.setHeaderText("Assign/Unassign Courses for " + faculty.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // 1. List current courses
        ListView<Course> courseList = new ListView<>();
        updateCourseList(courseList, faculty.getId());

        Button unassignBtn = createButton("Unassign Selected", "#ef4444");
        unassignBtn.setOnAction(e -> {
            Course sel = courseList.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (courseDAO.assignFaculty(sel.getId(), 0)) { // 0 to unassign
                    updateCourseList(courseList, faculty.getId());
                    loadData(); // Update main table
                }
            }
        });

        // 2. Add new assignment
        HBox addBox = new HBox(10);
        ComboBox<Course> availableCombo = new ComboBox<>();
        // Load courses that have NO faculty (faculty_id is 0 or null)
        // Need a method for this, or just filter getAllCourses
        List<Course> available = courseDAO.getAllCourses().stream()
                .filter(c -> c.getFacultyId() == 0) // Assuming 0 is default/null-like
                .collect(Collectors.toList());
        availableCombo.getItems().addAll(available);

        Button assignBtn = createButton("Assign", "#22c55e");
        assignBtn.setOnAction(e -> {
            Course c = availableCombo.getValue();
            if (c != null) {
                if (courseDAO.assignFaculty(c.getId(), faculty.getId())) {
                    updateCourseList(courseList, faculty.getId());
                    // Refresh available
                    availableCombo.getItems().remove(c);
                    loadData();
                }
            }
        });

        addBox.getChildren().addAll(new Label("Assign Course:"), availableCombo, assignBtn);

        content.getChildren().addAll(new Label("Current Assignments:"), courseList, unassignBtn, new Separator(),
                addBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void updateCourseList(ListView<Course> list, int facultyId) {
        // We don't have getCoursesByFaculty yet in DAO?
        // We have getFacultyWorkload but not the list.
        // I can filter getAllCourses for now.
        List<Course> all = courseDAO.getAllCourses();
        List<Course> mine = all.stream().filter(c -> c.getFacultyId() == facultyId).collect(Collectors.toList());
        list.getItems().setAll(mine);
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 4;");
        return btn;
    }

    public VBox getView() {
        return root;
    }

    // Spec Helper
    public static class FacultyWorkloadItem {
        private Faculty faculty;
        private CourseDAO.WorkloadStats stats;

        public FacultyWorkloadItem(Faculty f, CourseDAO.WorkloadStats s) {
            this.faculty = f;
            this.stats = s;
        }

        public Faculty getFaculty() {
            return faculty;
        }

        public CourseDAO.WorkloadStats getStats() {
            return stats;
        }
    }
}
