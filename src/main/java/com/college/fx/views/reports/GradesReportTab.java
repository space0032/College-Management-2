package com.college.fx.views.reports;

import com.college.dao.CourseDAO;
import com.college.dao.GradeDAO;
import com.college.models.Course;
import com.college.utils.ReportGenerator;
import com.college.utils.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GradesReportTab {

    private VBox content;
    private ComboBox<Course> courseComboBox;
    private PieChart gradeChart;
    private TableView<com.college.models.Grade> gradeTable;
    private GradeDAO gradeDAO;
    private CourseDAO courseDAO;
    private Map<String, Integer> currentDistribution;

    public GradesReportTab() {
        this.gradeDAO = new GradeDAO();
        this.courseDAO = new CourseDAO();
        createContent();
    }

    private void createContent() {
        content = new VBox(20);
        content.setPadding(new Insets(20));

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Select Course:");
        courseLabel.setStyle("-fx-text-fill: white;");
        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Choose Course...");
        courseComboBox.setPrefWidth(200);
        loadCourses();
        courseComboBox.setOnAction(e -> loadReport());

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> exportReport());
        exportBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold;");

        controls.getChildren().addAll(courseLabel, courseComboBox, exportBtn);

        // Split Pane
        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Chart
        VBox chartBox = new VBox(10);
        chartBox.setAlignment(Pos.CENTER);
        gradeChart = new PieChart();
        gradeChart.setTitle("Grade Distribution");
        gradeChart.setLabelsVisible(true);
        gradeChart.setLegendVisible(true);
        chartBox.getChildren().add(gradeChart);

        // Table
        VBox tableBox = new VBox(10);
        gradeTable = new TableView<>();
        gradeTable.getStyleClass().add("glass-table");
        VBox.setVgrow(gradeTable, Priority.ALWAYS);

        TableColumn<com.college.models.Grade, String> studentCol = new TableColumn<>("Student Name");
        studentCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentName()));

        TableColumn<com.college.models.Grade, String> examCol = new TableColumn<>("Exam Type");
        examCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getExamType()));

        TableColumn<com.college.models.Grade, Double> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMarksObtained()).asObject());

        TableColumn<com.college.models.Grade, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getGrade()));

        gradeTable.getColumns().addAll(java.util.Arrays.asList(studentCol, examCol, marksCol, gradeCol));

        Label detailLabel = new Label("Detailed Performance");
        detailLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        tableBox.getChildren().add(detailLabel);
        tableBox.getChildren().add(gradeTable);

        splitPane.getItems().addAll(chartBox, tableBox);
        splitPane.setDividerPositions(0.4);

        content.getChildren().addAll(controls, splitPane);
    }

    private void loadCourses() {
        List<Course> courses = courseDAO.getAllCourses();
        courseComboBox.getItems().addAll(courses);
    }

    private void loadReport() {
        Course selected = courseComboBox.getValue();
        if (selected == null)
            return;

        // Load Chart Data
        currentDistribution = gradeDAO.getGradeDistribution(selected.getId());
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : currentDistribution.entrySet()) {
            if (entry.getValue() > 0) {
                chartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        gradeChart.setData(chartData);
        gradeChart.setTitle("Grade Distribution: " + selected.getName());

        // Load Table Data
        List<com.college.models.Grade> grades = gradeDAO.getGradesByCourse(selected.getId());
        gradeTable.setItems(FXCollections.observableArrayList(grades));
    }

    private void exportReport() {
        List<com.college.models.Grade> items = gradeTable.getItems();
        if (items.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogUtils.styleDialog(alert);
            alert.setTitle("No Data");
            alert.setContentText("No data to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Grades Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("grades_report.csv");
        File file = fileChooser.showSaveDialog(content.getScene().getWindow());

        if (file != null) {
            List<Object[]> data = new ArrayList<>();
            for (com.college.models.Grade g : items) {
                data.add(new Object[] {
                        g.getStudentName(),
                        g.getExamType(),
                        String.valueOf(g.getMarksObtained()),
                        g.getGrade()
                });
            }

            boolean success = ReportGenerator.generateCSV(file.getAbsolutePath(),
                    new String[] { "Student Name", "Exam Type", "Marks", "Grade" }, data);

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
}
