package com.college.fx.views.reports;

import com.college.dao.AttendanceDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.ReportGenerator;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceReportTab {

    private VBox content;
    private ComboBox<Course> courseComboBox;
    private TextField searchField;
    private TableView<AttendanceStat> tableView;
    private ObservableList<AttendanceStat> masterData;
    private javafx.collections.transformation.FilteredList<AttendanceStat> filteredData;
    private AttendanceDAO attendanceDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;

    public AttendanceReportTab() {
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.studentDAO = new StudentDAO();
        this.masterData = FXCollections.observableArrayList();
        this.filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        createContent();
    }

    private void createContent() {
        content = new VBox(20);
        content.setPadding(new Insets(20));

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Select Course:");
        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Choose Course...");
        courseComboBox.setPrefWidth(200);
        loadCourses();
        courseComboBox.setOnAction(e -> loadReport());

        Label searchLabel = new Label("Search Student:");
        searchField = new TextField();
        searchField.setPromptText("Type name...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(stat -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return stat.getStudentName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> exportReport());
        exportBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold;");

        controls.getChildren().addAll(courseLabel, courseComboBox, searchLabel, searchField, exportBtn);

        // Table
        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableView.setItems(filteredData);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<AttendanceStat, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        nameCol.setPrefWidth(200);

        TableColumn<AttendanceStat, Double> percentCol = new TableColumn<>("Attendance %");
        percentCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPercentage()).asObject());
        percentCol.setPrefWidth(150);
        percentCol.setCellFactory(col -> new TableCell<AttendanceStat, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    if (item < 75.0) {
                        setTextFill(javafx.scene.paint.Color.RED);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                        setStyle("");
                    }
                }
            }
        });

        tableView.getColumns().addAll(java.util.Arrays.asList(nameCol, percentCol));

        content.getChildren().addAll(controls, tableView);
    }

    private void loadCourses() {
        List<Course> courses = courseDAO.getAllCourses();
        courseComboBox.getItems().addAll(courses);
    }

    private void loadReport() {
        Course selected = courseComboBox.getValue();
        if (selected == null)
            return;

        masterData.clear();
        Map<Integer, Double> stats = attendanceDAO.getCourseAttendanceStats(selected.getId());

        for (Map.Entry<Integer, Double> entry : stats.entrySet()) {
            Student s = studentDAO.getStudentById(entry.getKey());
            if (s != null) {
                masterData.add(new AttendanceStat(s.getName(), entry.getValue()));
            }
        }
    }

    private void exportReport() {
        if (filteredData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogUtils.styleDialog(alert);
            alert.setTitle("No Data");
            alert.setContentText("No data to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attendance Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("attendance_report.csv");
        File file = fileChooser.showSaveDialog(content.getScene().getWindow());

        if (file != null) {
            List<Object[]> data = new ArrayList<>();
            for (AttendanceStat stat : filteredData) {
                data.add(new Object[] { stat.getStudentName(), String.format("%.2f", stat.getPercentage()) });
            }

            boolean success = ReportGenerator.generateCSV(file.getAbsolutePath(),
                    new String[] { "Student Name", "Percentage" }, data);

            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                DialogUtils.styleDialog(alert);
                alert.setTitle("Success");
                alert.setContentText("Report exported successfully!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                DialogUtils.styleDialog(alert);
                alert.setTitle("Error");
                alert.setContentText("Failed to export report.");
                alert.showAndWait();
            }
        }
    }

    public javafx.scene.Node getContent() {
        return content;
    }

    // Helper class for table
    public static class AttendanceStat {
        private String studentName;
        private double percentage;

        public AttendanceStat(String studentName, double percentage) {
            this.studentName = studentName;
            this.percentage = percentage;
        }

        public String getStudentName() {
            return studentName;
        }

        public double getPercentage() {
            return percentage;
        }
    }
}
