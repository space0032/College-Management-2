package com.college.fx.views;

import com.college.dao.GradeDAO;
import com.college.dao.StudentDAO;
import com.college.models.Grade;
import com.college.models.Student;
import com.college.utils.SearchableStudentComboBox;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.college.dao.CourseDAO;
import com.college.models.Course;
// Already there but safe to include if targeted

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * JavaFX Grades View
 */
public class GradesView {

    private VBox root;
    private TableView<Grade> tableView;
    private ObservableList<Grade> gradeData;
    private GradeDAO gradeDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    public GradesView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.gradeDAO = new GradeDAO();
        this.studentDAO = new StudentDAO();
        this.gradeData = FXCollections.observableArrayList();
        createView();
        loadGrades();
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

        Label title = new Label(role.equals("STUDENT") ? "My Grades" : "Grade Management");
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh");
        refreshBtn.getStyleClass().add("icon-button");
        refreshBtn.setOnAction(e -> loadGrades());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableView.setItems(gradeData);

        // Columns definitions
        TableColumn<Grade, String> studentCol = new TableColumn<>("Student Name");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStudentName() != null ? data.getValue().getStudentName() : "-"));
        studentCol.setPrefWidth(150);

        TableColumn<Grade, String> enrollCol = new TableColumn<>("Enrollment No");
        enrollCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnrollmentNumber() != null ? data.getValue().getEnrollmentNumber() : "-"));
        enrollCol.setPrefWidth(120);

        TableColumn<Grade, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDepartment() != null ? data.getValue().getDepartment() : "-"));
        deptCol.setPrefWidth(120);

        TableColumn<Grade, String> semCol = new TableColumn<>("Sem");
        semCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getSemester())));
        semCol.setPrefWidth(50);

        TableColumn<Grade, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCourseName() != null ? data.getValue().getCourseName()
                        : String.valueOf(data.getValue().getCourseId())));
        courseCol.setPrefWidth(200);

        TableColumn<Grade, String> examCol = new TableColumn<>("Exam Type");
        examCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExamType()));
        examCol.setPrefWidth(100);

        TableColumn<Grade, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMarksObtained() + " / " + data.getValue().getMaxMarks()));
        marksCol.setPrefWidth(100);

        TableColumn<Grade, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));
        gradeCol.setPrefWidth(60);
        gradeCol.setCellFactory(col -> new TableCell<Grade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                    if (item.startsWith("A"))
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;");
                    else if (item.startsWith("F"))
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
                }
            }
        });

        // Add columns based on role
        if (role.equals("STUDENT")) {
            tableView.getColumns()
                    .addAll(java.util.Arrays.asList(courseCol, deptCol, semCol, examCol, marksCol, gradeCol));
        } else {
            tableView.getColumns()
                    .addAll(java.util.Arrays.asList(studentCol, enrollCol, courseCol, deptCol, semCol, examCol,
                            marksCol,
                            gradeCol));
        }

        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        if (SessionManager.getInstance().hasPermission("MANAGE_GRADES")) {
            Button addGradeBtn = createButton("Add Grade");
            addGradeBtn.getStyleClass().add("accent-button");
            addGradeBtn.setOnAction(e -> showAddGradeDialog());

            Button bulkGradeBtn = createButton("Bulk Grade Entry");
            bulkGradeBtn.getStyleClass().add("accent-button");
            bulkGradeBtn.setOnAction(e -> showBulkGradeDialog());

            section.getChildren().addAll(addGradeBtn, bulkGradeBtn);

            Button importBtn = createButton("Import CSV");
            importBtn.getStyleClass().add("accent-button");
            importBtn.setOnAction(e -> showImportDialog());
            section.getChildren().add(importBtn);
        }

        Button exportBtn = createButton("Export Report");
        exportBtn.getStyleClass().add("icon-button");
        section.getChildren().add(exportBtn);

        return section;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        // Style classes are added by caller
        return btn;
    }

    private void loadGrades() {
        gradeData.clear();

        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                List<Grade> grades = gradeDAO.getGradesByStudent(student.getId());
                gradeData.addAll(grades);
            }
        } else if ("FACULTY".equals(role)) {
            gradeData.addAll(gradeDAO.getGradesByFaculty(userId));
        } else {
            gradeData.addAll(gradeDAO.getAllGrades());
        }
    }

    private void showAddGradeDialog() {
        Dialog<Grade> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Add/Edit Grades");
        dialog.setHeaderText("Enter Grade Details");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Course> courseCombo = new ComboBox<>();
        try {
            CourseDAO courseDAO = new CourseDAO();
            List<Course> courses;
            if ("FACULTY".equals(role)) {
                courses = courseDAO.getCoursesByFaculty(userId);
            } else {
                courses = courseDAO.getAllCourses();
            }
            courseCombo.getItems().addAll(courses);
        } catch (Exception e) {
            /* Ignore */ }

        // Student selector - will be populated based on course selection
        SearchableStudentComboBox studentSelector = new SearchableStudentComboBox(FXCollections.observableArrayList());

        courseCombo.setOnAction(e -> {
            Course selected = courseCombo.getValue();
            if (selected != null) {
                com.college.dao.CourseRegistrationDAO crDAO = new com.college.dao.CourseRegistrationDAO();
                List<Student> enrolled = crDAO.getEnrolledStudents(selected.getId());
                studentSelector.setStudents(enrolled);
            }
        });

        ComboBox<String> examTypeCombo = new ComboBox<>();
        examTypeCombo.setPromptText("Exam Type (e.g. Final)");
        examTypeCombo.getItems().addAll("Midterm", "Final", "Quiz", "Assignment", "Project");
        examTypeCombo.setValue("Final"); // Default selection

        TextField marksField = new TextField();
        marksField.setPromptText("Marks Obtained");
        TextField maxMarksField = new TextField();
        maxMarksField.setPromptText("Max Marks");

        DialogUtils.addFormRow(grid, "Course:", courseCombo, 0);
        DialogUtils.addFormRow(grid, "Student:", studentSelector, 1);
        DialogUtils.addFormRow(grid, "Exam:", examTypeCombo, 2);
        DialogUtils.addFormRow(grid, "Marks:", marksField, 3);
        DialogUtils.addFormRow(grid, "Max Marks:", maxMarksField, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && courseCombo.getValue() != null && studentSelector.getSelectedStudent() != null) {
                try {
                    double m = Double.parseDouble(marksField.getText());
                    double max = Double.parseDouble(maxMarksField.getText());
                    if (max == 0)
                        max = 100;
                    Grade g = new Grade();
                    g.setCourseId(courseCombo.getValue().getId());
                    g.setStudentId(studentSelector.getSelectedStudent().getId());
                    // Ensure exam_type is not null
                    String examType = examTypeCombo.getValue();
                    if (examType == null || examType.trim().isEmpty()) {
                        examType = "Final"; // Default value
                    }
                    g.setExamType(examType);
                    g.setMarksObtained(m);
                    g.setMaxMarks(max);

                    double p = (m / max) * 100;
                    g.setPercentage(p);

                    String l = "F";
                    if (p >= 90)
                        l = "A+";
                    else if (p >= 80)
                        l = "A";
                    else if (p >= 70)
                        l = "B";
                    else if (p >= 60)
                        l = "C";
                    else if (p >= 50)
                        l = "D";
                    g.setGrade(l);

                    gradeDAO.saveGrade(g);
                    return g;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(g -> {
            loadGrades();
            showAlert("Success", "Grade Saved!");
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showBulkGradeDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Bulk Grade Entry");
        dialog.setHeaderText("Enter Grades for Entire Class");
        ButtonType saveBtn = new ButtonType("Save All", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(700);

        GridPane selectionGrid = new GridPane();
        selectionGrid.setHgap(10);
        selectionGrid.setVgap(10);

        ComboBox<Course> courseCombo = new ComboBox<>();
        try {
            CourseDAO courseDAO = new CourseDAO();
            List<Course> courses;
            if ("FACULTY".equals(role)) {
                courses = courseDAO.getCoursesByFaculty(userId);
            } else {
                courses = courseDAO.getAllCourses();
            }
            courseCombo.getItems().addAll(courses);
        } catch (Exception e) {
            /* Ignore */ }

        ComboBox<String> examTypeCombo = new ComboBox<>();
        examTypeCombo.getItems().addAll("Midterm", "Final", "Quiz", "Assignment", "Project");
        examTypeCombo.setValue("Final"); // Default selection

        TextField maxMarksField = new TextField("100");

        Label courseVal = new Label("Course:");
        courseVal.setStyle("-fx-text-fill: white;");
        selectionGrid.add(courseVal, 0, 0);
        selectionGrid.add(courseCombo, 1, 0);

        Label examVal = new Label("Exam Type:");
        examVal.setStyle("-fx-text-fill: white;");
        selectionGrid.add(examVal, 0, 1);
        selectionGrid.add(examTypeCombo, 1, 1);

        Label maxVal = new Label("Max Marks:");
        maxVal.setStyle("-fx-text-fill: white;");
        selectionGrid.add(maxVal, 0, 2);
        selectionGrid.add(maxMarksField, 1, 2);

        Button loadBtn = createButton("Load Students");
        loadBtn.getStyleClass().add("accent-button");

        TableView<BulkGradeRecord> gradeTable = new TableView<>();
        gradeTable.setPrefHeight(300);
        ObservableList<BulkGradeRecord> records = FXCollections.observableArrayList();
        gradeTable.setItems(records);

        TableColumn<BulkGradeRecord, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName));
        nameCol.setPrefWidth(200);

        TableColumn<BulkGradeRecord, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellFactory(col -> new TableCell<BulkGradeRecord, String>() {
            private TextField textField = new TextField();
            {
                textField.setPrefWidth(80);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    BulkGradeRecord record = getTableView().getItems().get(getIndex());
                    textField.textProperty().addListener((obs, old, newVal) -> {
                        try {
                            record.marks = newVal.isEmpty() ? null : Double.parseDouble(newVal);
                        } catch (Exception e) {
                            record.marks = null;
                        }
                    });
                    setGraphic(textField);
                } else
                    setGraphic(null);
            }
        });
        marksCol.setPrefWidth(100);

        gradeTable.getColumns().addAll(java.util.Arrays.asList(nameCol, marksCol));

        loadBtn.setOnAction(e -> {
            if (courseCombo.getValue() == null) {
                showAlert("Error", "Please select a course first.");
                return;
            }
            records.clear();
            com.college.dao.CourseRegistrationDAO crDAO = new com.college.dao.CourseRegistrationDAO();
            List<Student> enrolled = crDAO.getEnrolledStudents(courseCombo.getValue().getId());
            for (Student s : enrolled) {
                records.add(new BulkGradeRecord(s.getId(), s.getName()));
            }
        });

        content.getChildren().addAll(selectionGrid, loadBtn, gradeTable);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && courseCombo.getValue() != null) {
                int saved = 0;
                double max = Double.parseDouble(maxMarksField.getText());
                // Ensure exam_type is not null
                String examType = examTypeCombo.getValue();
                if (examType == null || examType.trim().isEmpty()) {
                    examType = "Final"; // Default value
                }
                for (BulkGradeRecord r : records) {
                    if (r.marks != null) {
                        Grade g = new Grade();
                        g.setStudentId(r.studentId);
                        g.setCourseId(courseCombo.getValue().getId());
                        g.setExamType(examType);
                        g.setMarksObtained(r.marks);
                        g.setMaxMarks(max);
                        gradeDAO.saveGrade(g);
                        saved++;
                    }
                }
                showAlert("Success", saved + " grades saved!");
                loadGrades();
                return true;
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void showImportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Grades CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int success = 0;
                int fail = 0;

                // Expected format: student_id, course_id, exam_type, marks, max_marks
                for (String line : lines) {
                    if (line.trim().isEmpty() || line.startsWith("student_id"))
                        continue; // Skip header/empty

                    try {
                        String[] parts = line.split(",");
                        if (parts.length < 5)
                            throw new Exception("Invalid format");

                        int sid = Integer.parseInt(parts[0].trim());
                        int cid = Integer.parseInt(parts[1].trim());
                        String type = parts[2].trim();
                        double marks = Double.parseDouble(parts[3].trim());
                        double max = Double.parseDouble(parts[4].trim());

                        Grade g = new Grade();
                        g.setStudentId(sid);
                        g.setCourseId(cid);
                        g.setExamType(type);
                        g.setMarksObtained(marks);
                        g.setMaxMarks(max);

                        // Calculate percentage/grade
                        double p = (marks / max) * 100;
                        g.setPercentage(p);
                        String l = "F";
                        if (p >= 90)
                            l = "A+";
                        else if (p >= 80)
                            l = "A";
                        else if (p >= 70)
                            l = "B";
                        else if (p >= 60)
                            l = "C";
                        else if (p >= 50)
                            l = "D";
                        g.setGrade(l);

                        if (gradeDAO.saveGrade(g)) {
                            success++;
                        } else {
                            fail++;
                        }
                    } catch (Exception e) {
                        fail++;
                    }
                }

                showAlert("Import Result", "Successfully imported: " + success + "\nFailed: " + fail);
                loadGrades();

            } catch (Exception e) {
                showAlert("Error", "Failed to read file: " + e.getMessage());
            }
        }
    }

    private static class BulkGradeRecord {
        int studentId;
        String studentName;
        Double marks;

        BulkGradeRecord(int id, String name) {
            studentId = id;
            studentName = name;
        }
    }

    public VBox getView() {
        return root;
    }
}
