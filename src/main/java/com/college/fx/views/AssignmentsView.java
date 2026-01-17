package com.college.fx.views;

import com.college.dao.AssignmentDAO;
import com.college.dao.StudentDAO;
import com.college.models.Assignment;
import com.college.models.Student;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;

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
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("glass-card");
        header.setPadding(new Insets(10));

        Label title = new Label("Assignments");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> loadAssignments());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
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

        TableColumn<Assignment, String> semesterCol = new TableColumn<>("Semester");
        semesterCol.setCellValueFactory(data -> new SimpleStringProperty(
                "Sem " + data.getValue().getSemester()));
        semesterCol.setPrefWidth(90);

        TableColumn<Assignment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            if (role.equals("STUDENT")) {
                Student student = studentDAO.getStudentByUserId(userId);
                if (student != null) {
                    Submission sub = submissionDAO.getSubmission(data.getValue().getId(), student.getId());
                    return new SimpleStringProperty(sub != null ? "Submitted" : "Pending");
                }
            }
            return new SimpleStringProperty("-");
        });
        statusCol.setPrefWidth(100);

        if (role.equals("STUDENT")) {
            tableView.getColumns()
                    .addAll(java.util.Arrays.asList(courseCol, titleCol, descCol, semesterCol, dueCol, statusCol));
        } else {
            tableView.getColumns().addAll(java.util.Arrays.asList(courseCol, titleCol, descCol, semesterCol, dueCol));
        }
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
            Button addBtn = new Button("+ New Assignment");
            addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            addBtn.setOnAction(e -> showAddAssignmentDialog());

            Button reviewBtn = new Button("Review Submissions");
            reviewBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            reviewBtn.setOnAction(e -> showReviewSubmissionsDialog());

            section.getChildren().addAll(addBtn, reviewBtn);
        } else if (role.equals("STUDENT")) {
            Button submitBtn = new Button("Submit Assignment");
            submitBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
            submitBtn.setOnAction(e -> showSubmitAssignmentDialog());
            section.getChildren().add(submitBtn);
        }

        return section;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        // Style classes are added by caller
        return btn;
    }

    private void showAddAssignmentDialog() {
        Dialog<Assignment> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
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

        TextField titleField = new TextField();
        titleField.setPromptText("Assignment Title");
        titleField.setPrefWidth(250);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefHeight(100);
        descArea.setPrefWidth(250);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(250);
        datePicker.setPromptText("Select Due Date");

        ComboBox<Integer> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8);
        semesterCombo.setValue(1); // Default semester
        semesterCombo.setPrefWidth(250);

        DialogUtils.addFormRow(grid, "Course:", courseCombo, 0);
        DialogUtils.addFormRow(grid, "Title:", titleField, 1);
        DialogUtils.addFormRow(grid, "Description:", descArea, 2);
        DialogUtils.addFormRow(grid, "Due Date:", datePicker, 3);
        DialogUtils.addFormRow(grid, "Semester:", semesterCombo, 4);

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
                a.setSemester(semesterCombo.getValue()); // Set semester
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
        DialogUtils.styleDialog(dialog);
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
        contentArea.setPrefWidth(400);

        // File attachment
        TextField filePathField = new TextField();
        filePathField.setPromptText("No file selected");
        filePathField.setEditable(false);
        filePathField.setPrefWidth(300);

        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select File to Attach");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*"),
                    new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                    new javafx.stage.FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                    new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
            java.io.File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
            }
        });

        HBox fileBox = new HBox(10);
        fileBox.getChildren().addAll(filePathField, browseBtn);

        Label lblContent = new Label("Content:");
        lblContent.setStyle("-fx-font-size: 14px; -fx-text-fill: #e2e8f0;");

        Label lblFile = new Label("Attach File:");
        lblFile.setStyle("-fx-font-size: 14px; -fx-text-fill: #e2e8f0;");

        DialogUtils.addFormRow(grid, "Content:", contentArea, 0);
        DialogUtils.addFormRow(grid, "Attach File:", fileBox, 1);

        dialog.getDialogPane().setContent(grid);

        // Define the button conversion
        // We need to handle the upload logic potentially before converting, or inside.
        // Dialog conversion is synchronous, so it blocks UI, which is acceptable for
        // simple apps but ideally async.
        // For this context, blocking is okay.

        dialog.setResultConverter(btn -> {
            if (btn == submitBtnType) {
                String finalPath = filePathField.getText();

                // Dropbox Upload
                if (finalPath != null && !finalPath.isEmpty() && !finalPath.startsWith("http")) {
                    java.io.File file = new java.io.File(finalPath);
                    if (file.exists()) {
                        com.college.services.DropboxService dbService = new com.college.services.DropboxService();
                        if (dbService.isConfigured()) {
                            try (java.io.InputStream is = new java.io.FileInputStream(file)) {
                                String fileName = "submission_" + student.getId() + "_" + selected.getId() + "_"
                                        + file.getName();
                                String sharableLink = dbService.uploadFile(is, fileName);
                                if (sharableLink != null) {
                                    finalPath = sharableLink;
                                    System.out.println("File uploaded to Dropbox: " + finalPath);
                                } else {
                                    System.err.println("Dropbox upload failed, falling back to local path.");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                Submission s = new Submission();
                s.setAssignmentId(selected.getId());
                s.setStudentId(student.getId());
                s.setSubmissionText(contentArea.getText());
                s.setFilePath(finalPath);
                submissionDAO.submitAssignment(s);
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            loadAssignments();
            showAlert("Success", "Assignment submitted!");
        });
    }

    private void showReviewSubmissionsDialog() {
        Assignment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select an assignment to review.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Review Submissions");
        dialog.setHeaderText("Submissions for: " + selected.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Submission> submissionTable = new TableView<>();
        submissionTable.setPrefWidth(900);
        submissionTable.setPrefHeight(400);

        TableColumn<Submission, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<Submission, String> dateCol = new TableColumn<>("Submitted At");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSubmittedAt() != null ? data.getValue().getSubmittedAt().toString() : "-"));
        dateCol.setPrefWidth(150);

        TableColumn<Submission, String> plagCol = new TableColumn<>("Plagiarism");
        plagCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlagiarismScore() + "%"));
        plagCol.setPrefWidth(80);
        plagCol.setCellFactory(col -> new TableCell<Submission, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.endsWith("%")) {
                        try {
                            int score = Integer.parseInt(item.replace("%", ""));
                            if (score > 50) {
                                setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            } else if (score > 20) {
                                setStyle("-fx-text-fill: orange;");
                            } else {
                                setStyle("-fx-text-fill: green;");
                            }
                        } catch (NumberFormatException e) {
                            setStyle("");
                        }
                    }
                }
            }
        });

        TableColumn<Submission, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Submission, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGrade() != null ? String.valueOf(data.getValue().getGrade()) : "-"));
        gradeCol.setPrefWidth(80);

        // Action Column for Viewed File
        TableColumn<Submission, Void> fileCol = new TableColumn<>("File");
        fileCol.setPrefWidth(100);
        fileCol.setCellFactory(param -> new TableCell<>() {
            private final Button openBtn = new Button("View");

            {
                openBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-size: 10px;");
                openBtn.setOnAction(event -> {
                    Submission s = getTableView().getItems().get(getIndex());
                    String path = s.getFilePath();
                    if (path != null && !path.isEmpty()) {
                        if (path.startsWith("http")) {
                            com.college.MainFX.getHostServicesInstance().showDocument(path);
                        } else {
                            // Try to open local file if possible
                            try {
                                java.io.File f = new java.io.File(path);
                                if (f.exists()) {
                                    com.college.MainFX.getHostServicesInstance().showDocument(f.toURI().toString());
                                } else {
                                    Alert a = new Alert(Alert.AlertType.ERROR);
                                    DialogUtils.styleDialog(a);
                                    a.setTitle("File Not Found");
                                    a.setContentText("Local file not found: " + path);
                                    a.show();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Submission s = getTableView().getItems().get(getIndex());
                    if (s.getFilePath() != null && !s.getFilePath().isEmpty()) {
                        setGraphic(openBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        submissionTable.getColumns()
                .addAll(java.util.Arrays.asList(studentCol, dateCol, plagCol, statusCol, gradeCol, fileCol));

        // Load data
        List<Submission> submissions = submissionDAO.getSubmissionsByAssignment(selected.getId());
        submissionTable.setItems(FXCollections.observableArrayList(submissions));

        // Add Grade Button
        Button gradeBtn = new Button("Grade Submission");
        gradeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
        gradeBtn.setOnAction(e -> {
            Submission s = submissionTable.getSelectionModel().getSelectedItem();
            if (s != null) {
                showGradeDialog(s, () -> {
                    // Refresh table
                    List<Submission> updated = submissionDAO.getSubmissionsByAssignment(selected.getId());
                    submissionTable.setItems(FXCollections.observableArrayList(updated));
                });
            } else {
                showAlert("Warning", "Select a submission to grade.");
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(submissionTable, gradeBtn);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showGradeDialog(Submission submission, Runnable onGraded) {
        Dialog<Boolean> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Grade Submission");
        dialog.setHeaderText("Grade submission from " + submission.getStudentName());
        ButtonType saveBtn = new ButtonType("Save Grade", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextArea contentArea = new TextArea(submission.getSubmissionText());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(100);

        TextField marksField = new TextField();
        marksField.setPromptText("Marks (0-100)");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Enter feedback...");
        feedbackArea.setPrefHeight(60);

        DialogUtils.addFormRow(grid, "Submission Content:", contentArea, 0);

        int rowOffset = 1;
        if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
            HBox fileBox = new HBox(10);
            TextField fileField = new TextField(submission.getFilePath());
            fileField.setEditable(false);
            fileField.setPrefWidth(200);

            Button openBtn = new Button("Open");
            openBtn.setOnAction(e -> {
                if (submission.getFilePath().startsWith("http")) {
                    com.college.MainFX.getHostServicesInstance().showDocument(submission.getFilePath());
                } else {
                    // local
                    try {
                        java.io.File f = new java.io.File(submission.getFilePath());
                        if (f.exists()) {
                            com.college.MainFX.getHostServicesInstance().showDocument(f.toURI().toString());
                        }
                    } catch (Exception ex) {
                    }
                }
            });

            fileBox.getChildren().addAll(fileField, openBtn);

            DialogUtils.addFormRow(grid, "Attached File:", fileBox, rowOffset++);
        }

        DialogUtils.addFormRow(grid, "Marks:", marksField, rowOffset++);
        DialogUtils.addFormRow(grid, "Feedback:", feedbackArea, rowOffset);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    double marks = Double.parseDouble(marksField.getText());
                    submissionDAO.gradeSubmission(submission.getId(), marks, feedbackArea.getText());
                    return true;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid marks entered.");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                onGraded.run();
                showAlert("Success", "Graded successfully!");
            }
        });
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
