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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import com.college.dao.CourseDAO;
import com.college.dao.SubmissionDAO;
import com.college.models.Course;
import com.college.models.Submission;
import java.time.ZoneId;
import java.util.Date;

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
    private CourseDAO courseDAO;
    private SubmissionDAO submissionDAO;
    private String role;
    private int userId;

    public AssignmentsView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.assignmentDAO = new AssignmentDAO();
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        this.submissionDAO = new SubmissionDAO();
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
                        "-fx-border-radius: 12;");

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
                        "-fx-border-radius: 12;");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(assignmentData);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TableColumn<Assignment, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCourseName() != null ? data.getValue().getCourseName()
                        : String.valueOf(data.getValue().getCourseId())));
        courseCol.setPrefWidth(150);

        TableColumn<Assignment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Assignment, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(250);

        TableColumn<Assignment, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDueDate() != null ? dateFormat.format(data.getValue().getDueDate()) : "-"));
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
            addBtn.setOnAction(e -> showAddAssignmentDialog());

            Button editBtn = createButton("Edit", "#3b82f6");
            editBtn.setOnAction(e -> showAlert("Edit Assignment", "Edit functionality coming soon.")); // Placeholder
                                                                                                       // for now

            section.getChildren().addAll(addBtn, editBtn);
        } else if (role.equals("STUDENT")) {
            Button submitBtn = createButton("Submit Assignment", "#14b8a6");
            submitBtn.setOnAction(e -> showSubmitAssignmentDialog());
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
                        "-fx-cursor: hand;");
        return btn;
    }

    private void showAddAssignmentDialog() {
        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("New Assignment");
        dialog.setHeaderText("Create New Assignment");
        ButtonType saveBtn = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPrefWidth(250);
        courseCombo.getItems().addAll(courseDAO.getAllCourses());
        // Simple String Converter for Course if needed, but toString might handle it.

        TextField titleField = new TextField();
        titleField.setPromptText("Assignment Title");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefHeight(100);

        DatePicker datePicker = new DatePicker();

        grid.add(new Label("Course:"), 0, 0);
        grid.add(courseCombo, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && courseCombo.getValue() != null) {
                Assignment a = new Assignment();
                a.setCourseId(courseCombo.getValue().getId());
                a.setTitle(titleField.getText());
                a.setDescription(descArea.getText());
                if (datePicker.getValue() != null) {
                    a.setDueDate(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                a.setCreatedBy(userId); // Logged in user (Faculty/Admin)
                assignmentDAO.createAssignment(a);
                return a;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(a -> {
            loadAssignments();
            showAlert("Success", "Assignment created!");
        });
    }

    private void showSubmitAssignmentDialog() {
        Assignment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an assignment to submit.");
            return;
        }

        Student student = studentDAO.getStudentByUserId(userId);
        if (student == null) {
            showAlert("Error", "Student record not found for current user.");
            return;
        }

        Dialog<Submission> dialog = new Dialog<>();
        dialog.setTitle("Submit Assignment");
        dialog.setHeaderText("Submit: " + selected.getTitle());
        ButtonType submitBtnType = new ButtonType("Submit", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter your submission text or link...");
        contentArea.setPrefHeight(150);

        grid.add(new Label("Content:"), 0, 0);
        grid.add(contentArea, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtnType) {
                Submission s = new Submission();
                s.setAssignmentId(selected.getId());
                s.setStudentId(student.getId());
                s.setSubmissionText(contentArea.getText());
                s.setFilePath(""); // Optional for now
                submissionDAO.submitAssignment(s);
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> showAlert("Success", "Assignment submitted!"));
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
