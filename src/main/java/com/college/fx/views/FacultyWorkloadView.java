package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.FacultyDAO;
import com.college.dao.TimetableDAO;
import com.college.models.Course;
import com.college.models.Faculty;
import com.college.models.Timetable;
import com.college.utils.DialogUtils;
import com.college.utils.NotificationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.PieChart;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

public class FacultyWorkloadView {

    private VBox root;
    private TableView<FacultyWorkloadItem> table;
    private ObservableList<FacultyWorkloadItem> data;
    private FacultyDAO facultyDAO;
    private CourseDAO courseDAO;
    private ComboBox<String> departmentFilter;

    private PieChart chart;
    private NotificationService notificationService;
    private TimetableDAO timetableDAO;

    public FacultyWorkloadView() {
        this.facultyDAO = new FacultyDAO();
        this.courseDAO = new CourseDAO();
        this.notificationService = new NotificationService();
        this.timetableDAO = new TimetableDAO();
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
        title.getStyleClass().add("section-title");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        departmentFilter = new ComboBox<>();
        departmentFilter.setPromptText("Filter by Department");
        departmentFilter.getItems().add("All Departments");
        departmentFilter.getSelectionModel().selectFirst();
        departmentFilter.setOnAction(e -> loadData());

        // Populate departments - hardcoded for now or fetch?
        // Let's just fetch unique departments from faculty
        List<String> depts = facultyDAO.getAllFaculty().stream()
                .map(Faculty::getDepartment)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        departmentFilter.getItems().addAll(depts);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = createButton("Export Report", "#10b981");
        exportBtn.setOnAction(e -> exportReport());

        Label deptLabel = new Label("Department:");
        deptLabel.setStyle("-fx-text-fill: white;");
        toolbar.getChildren().addAll(deptLabel, departmentFilter, refreshBtn, spacer, exportBtn);

        // Chart
        chart = new PieChart();
        chart.setTitle("Average Credits per Department");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(false); // Too cluttered if many depts
        chart.setPrefHeight(250);
        chart.setMaxHeight(250);

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

        TableColumn<FacultyWorkloadItem, String> studentCol = new TableColumn<>("Total Students");
        studentCol.setCellValueFactory(
                d -> new SimpleStringProperty(String.valueOf(d.getValue().getStats().totalStudents)));

        TableColumn<FacultyWorkloadItem, Void> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    int credits = getTableView().getItems().get(getIndex()).getStats().credits;
                    Label lbl = new Label();
                    lbl.setStyle(
                            "-fx-padding: 2 8; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");

                    if (credits < 8) {
                        lbl.setText("UNDERLOAD");
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #f59e0b;"); // Amber
                    } else if (credits <= 18) {
                        lbl.setText("OPTIMAL");
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #22c55e;"); // Green
                    } else {
                        lbl.setText("OVERLOAD");
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #ef4444;"); // Red
                    }
                    setGraphic(lbl);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

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

        table.getColumns().addAll(
                java.util.Arrays.asList(nameCol, deptCol, countCol, creditCol, studentCol, statusCol, actionCol));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, toolbar, chart, table);
    }

    private void loadData() {
        data.clear();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        String selectedDept = departmentFilter.getValue();

        // For Chart
        Map<String, Double> deptCredits = new java.util.HashMap<>();
        Map<String, Integer> deptFacultyCount = new java.util.HashMap<>();

        for (Faculty f : facultyList) {
            CourseDAO.WorkloadStats stats = courseDAO.getFacultyWorkload(f.getId());

            // Populate table data (filtered)
            if (selectedDept == null || selectedDept.equals("All Departments")
                    || f.getDepartment().equals(selectedDept)) {
                data.add(new FacultyWorkloadItem(f, stats));
            }

            // Populate chart data (unfiltered to show overall view? Or filtered? Let's show
            // unfiltered overall)
            deptCredits.merge(f.getDepartment(), (double) stats.credits, Double::sum);
            deptFacultyCount.merge(f.getDepartment(), 1, Integer::sum);
        }

        // Update Chart
        chart.getData().clear();
        deptCredits.forEach((dept, totalCredits) -> {
            int count = deptFacultyCount.getOrDefault(dept, 1);
            double avg = totalCredits / count;
            if (avg > 0) {
                chart.getData().add(new PieChart.Data(dept + String.format(" (%.1f)", avg), avg));
            }
        });
    }

    private void manageAssignments(Faculty faculty) {
        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Manage Assignments: " + faculty.getName());
        dialog.setHeaderText("Assign/Unassign Courses for " + faculty.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setPrefWidth(500);

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
        addBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ComboBox<Course> availableCombo = new ComboBox<>();
        availableCombo.setPromptText("Select Course to Assign");
        availableCombo.setPrefWidth(200);

        // Load filtered courses
        List<Course> allAvailable = courseDAO.getAllCourses().stream()
                .filter(c -> c.getFacultyId() == 0)
                .collect(Collectors.toList());
        availableCombo.getItems().addAll(allAvailable);

        // Custom Cell Factory for Specialization Highlighting
        availableCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = item.getCode() + " - " + item.getName();
                    if (faculty.getSpecialization() != null
                            && faculty.getSpecialization().equalsIgnoreCase(item.getSpecialization())) {
                        text += " (Recommended)";
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;"); // Green for match
                    } else {
                        setStyle("");
                    }
                    setText(text);
                }
            }
        });
        // Also update button cell
        availableCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " - " + item.getName());
                }
            }
        });

        Button suggestBtn = createButton("Suggest Fit", "#f59e0b");
        suggestBtn.setOnAction(e -> {
            if (faculty.getSpecialization() != null) {
                availableCombo.getItems().sort((c1, c2) -> {
                    boolean s1 = faculty.getSpecialization().equalsIgnoreCase(c1.getSpecialization());
                    boolean s2 = faculty.getSpecialization().equalsIgnoreCase(c2.getSpecialization());
                    return Boolean.compare(s2, s1); // True first
                });
                availableCombo.getSelectionModel().selectFirst();
                availableCombo.show(); // Auto open
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                DialogUtils.styleDialog(alert);
                alert.setContentText("Faculty has no specialization set.");
                alert.showAndWait();
            }
        });

        Button assignBtn = createButton("Assign", "#22c55e");
        assignBtn.setOnAction(e -> {
            Course c = availableCombo.getValue();
            if (c != null) {
                // 1. Check for Overload
                int currentCredits = courseList.getItems().stream().mapToInt(Course::getCredits).sum();
                if (currentCredits + c.getCredits() > 18) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    DialogUtils.styleDialog(alert);
                    alert.setTitle("Overload Warning");
                    alert.setHeaderText("Workload Limit Exceeded");
                    alert.setContentText("Assigning this course will exceed 18 credits (Total: "
                            + (currentCredits + c.getCredits()) + "). Continue?");
                    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return;
                    }
                }

                // 2. Check for Time Conflict
                // Get faculty schedule
                List<Timetable> facultySchedule = timetableDAO.getTimetableByFaculty(faculty.getName());
                // Get course schedule (assuming subject name matches)
                List<Timetable> courseSchedule = timetableDAO.getTimetableBySubject(c.getName());

                for (Timetable fEntry : facultySchedule) {
                    for (Timetable cEntry : courseSchedule) {
                        if (fEntry.getDayOfWeek().equals(cEntry.getDayOfWeek()) &&
                                fEntry.getTimeSlot().equals(cEntry.getTimeSlot())) {

                            Alert conflictAlert = new Alert(Alert.AlertType.ERROR);
                            DialogUtils.styleDialog(conflictAlert);
                            conflictAlert.setTitle("Time Conflict");
                            conflictAlert.setHeaderText("Schedule Conflict Detected");
                            conflictAlert.setContentText("Faculty is already busy on " + fEntry.getDayOfWeek() +
                                    " at " + fEntry.getTimeSlot() + " (" + fEntry.getSubject() + ").");
                            conflictAlert.showAndWait();
                            return; // Block assignment
                        }
                    }
                }

                // 3. Assign
                if (courseDAO.assignFaculty(c.getId(), faculty.getId())) {
                    // 4. Notification
                    if (faculty.getEmail() != null && !faculty.getEmail().isEmpty()) {
                        String subject = "New Course Assignment: " + c.getName();
                        String body = "Dear " + faculty.getName() + ",\n\n" +
                                "You have been assigned the course: " + c.getCode() + " - " + c.getName() + ".\n" +
                                "Credits: " + c.getCredits() + "\n\n" +
                                "Please check your schedule.";
                        notificationService.queueEmail(faculty.getUserId(), faculty.getEmail(), subject, body);
                    }

                    updateCourseList(courseList, faculty.getId());
                    availableCombo.getItems().remove(c);
                    loadData();
                }
            }
        });

        addBox.getChildren().addAll(availableCombo, suggestBtn, assignBtn);

        content.getChildren().addAll(new Label("Current Assignments:"), courseList, unassignBtn, new Separator(),
                new Label("Assign New Course:"), addBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void updateCourseList(ListView<Course> list, int facultyId) {
        List<Course> mine = courseDAO.getCoursesByFaculty(facultyId);
        list.getItems().setAll(mine);
    }

    private void exportReport() {
        try {
            java.io.File file = new java.io.File("faculty_workload_report.csv");
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("Faculty Name,Department,Courses Assigned,Total Credits,Total Students,Status");
                for (FacultyWorkloadItem item : data) {
                    int credits = item.getStats().credits;
                    String status = credits < 8 ? "UNDERLOAD" : (credits <= 18 ? "OPTIMAL" : "OVERLOAD");

                    writer.printf("%s,%s,%d,%d,%d,%s%n",
                            item.getFaculty().getName(),
                            item.getFaculty().getDepartment(),
                            item.getStats().count,
                            credits,
                            item.getStats().totalStudents,
                            status);
                }
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            DialogUtils.styleDialog(alert);
            alert.setTitle("Export Successful");
            alert.setHeaderText("Report Exported");
            alert.setContentText("Report saved to: " + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            DialogUtils.styleDialog(alert);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Could not export report");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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
