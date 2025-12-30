package com.college.ui.reports;

import com.college.dao.GradeDAO;
import com.college.dao.CourseDAO;
import com.college.models.Course;
import com.college.models.Grade;
import com.college.utils.UIHelper;
import com.college.utils.ReportGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Grade Report Panel
 * Generate and view grade reports with distribution analysis
 */
public class GradeReportPanel extends JPanel {

    private JComboBox<CourseItem> courseCombo;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private GradeDAO gradeDAO;
    private String role;
    private int userId;

    public GradeReportPanel(String role, int userId) {
        this.role = role;
        this.userId = userId;
        gradeDAO = new GradeDAO();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel filterPanel = createFilterPanel();
        JPanel tablePanel = createTablePanel();
        JPanel summaryPanel = createSummaryPanel();

        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Select Course"));

        panel.add(new JLabel("Course:"));
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(250, 30));
        loadCourses();
        panel.add(courseCombo);

        JButton generateButton = UIHelper.createPrimaryButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        panel.add(generateButton);

        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> exportReport());
        panel.add(exportButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = { "Student Name", "Exam Type", "Marks", "Max Marks", "Percentage", "Grade" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Grade Distribution"));

        summaryLabel = new JLabel("Select a course and generate report");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(summaryLabel);

        return panel;
    }

    private void loadCourses() {
        CourseDAO courseDAO = new CourseDAO();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course course : courses) {
            courseCombo.addItem(new CourseItem(course));
        }
    }

    private void generateReport() {
        tableModel.setRowCount(0);

        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null)
            return;

        List<Grade> grades;

        // Filter by role - students only see their own grades
        if ("STUDENT".equals(role)) {
            int studentId = getStudentIdFromUserId();
            if (studentId > 0) {
                grades = gradeDAO.getGradesByStudent(studentId);
                // Further filter by course if specific course selected
                if (selected.course != null) {
                    final int courseId = selected.course.getId();
                    grades = grades.stream()
                            .filter(g -> g.getCourseId() == courseId)
                            .collect(java.util.stream.Collectors.toList());
                }
            } else {
                grades = new ArrayList<>();
            }
        } else {
            grades = gradeDAO.getGradesByCourse(selected.course.getId());
        }
        List<String> gradeLetters = new ArrayList<>();

        for (Grade grade : grades) {
            Object[] row = {
                    grade.getStudentName(),
                    grade.getExamType(),
                    grade.getMarksObtained(),
                    grade.getMaxMarks(),
                    String.format("%.1f%%", grade.getPercentage()),
                    grade.getGrade()
            };
            tableModel.addRow(row);
            gradeLetters.add(grade.getGrade());
        }

        // Calculate distribution
        Map<String, Integer> distribution = ReportGenerator.calculateGradeDistribution(gradeLetters);
        updateSummary(distribution, grades.size());
    }

    private void updateSummary(Map<String, Integer> distribution, int total) {
        String summary = String.format(
                "Total: %d | A+: %d | A: %d | B: %d | C: %d | D: %d | F: %d",
                total,
                distribution.get("A+"),
                distribution.get("A"),
                distribution.get("B"),
                distribution.get("C"),
                distribution.get("D"),
                distribution.get("F"));
        summaryLabel.setText(summary);
    }

    private void exportReport() {
        com.college.utils.TableExporter.showExportDialog(this, reportTable, "grade_report");
    }

    private static class CourseItem {
        Course course;

        CourseItem(Course course) {
            this.course = course;
        }

        @Override
        public String toString() {
            return course.getName() + " (" + course.getCode() + ")";
        }
    }

    private int getStudentIdFromUserId() {
        try {
            java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id FROM students WHERE user_id = ?");
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
