package com.college.ui.reports;

import com.college.dao.AttendanceDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.UIHelper;
import com.college.utils.ReportGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Attendance Report Panel
 * Generate and view attendance reports with filtering
 */
public class AttendanceReportPanel extends JPanel {

    private JComboBox<CourseItem> courseCombo;
    private JComboBox<String> periodCombo;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private String role;
    private int userId;

    public AttendanceReportPanel(String role, int userId) {
        this.role = role;
        this.userId = userId;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Filter Panel
        JPanel filterPanel = createFilterPanel();

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Summary Panel
        JPanel summaryPanel = createSummaryPanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.EAST);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));

        panel.add(new JLabel("Course:"));
        courseCombo = new JComboBox<>();
        courseCombo.addItem(new CourseItem(null)); // All courses
        courseCombo.setPreferredSize(new Dimension(200, 30));
        loadCourses();
        panel.add(courseCombo);

        panel.add(new JLabel("Period:"));
        periodCombo = new JComboBox<>(new String[] {
                "This Month", "Last 30 Days", "Last 90 Days", "This Semester", "All Time"
        });
        panel.add(periodCombo);

        JButton generateButton = UIHelper.createPrimaryButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        panel.add(generateButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = { "Student ID", "Student Name", "Course", "Present", "Absent", "Total", "Percentage",
                "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(25);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));

        summaryLabel = new JLabel("No data");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(summaryLabel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JButton exportButton = UIHelper.createPrimaryButton("Export CSV");
        exportButton.setPreferredSize(new Dimension(120, 35));
        exportButton.setMaximumSize(new Dimension(120, 35));
        exportButton.addActionListener(e -> exportReport());

        JButton printButton = new JButton("Print");
        printButton.setPreferredSize(new Dimension(120, 35));
        printButton.setMaximumSize(new Dimension(120, 35));
        printButton.addActionListener(e -> printReport());

        panel.add(exportButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(printButton);

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

        CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
        StudentDAO studentDAO = new StudentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();

        List<Student> students;

        // Filter by role - students only see their own data
        if ("STUDENT".equals(role)) {
            int studentId = getStudentIdFromUserId();
            if (studentId > 0) {
                Student student = studentDAO.getStudentById(studentId);
                students = student != null ? java.util.Collections.singletonList(student) : new ArrayList<>();
            } else {
                students = new ArrayList<>();
            }
        } else {
            students = studentDAO.getAllStudents();
        }
        int totalStudents = 0;
        int lowAttendanceCount = 0;

        for (Student student : students) {
            if (selectedCourse != null && selectedCourse.course != null) {
                // Get attendance for specific course
                int courseId = selectedCourse.course.getId();
                double percentage = attendanceDAO.getAttendancePercentage(student.getId(), courseId);

                if (percentage >= 0) { // Only add if student has attendance records
                    String status = getAttendanceStatus(percentage);
                    if (percentage < 75)
                        lowAttendanceCount++;

                    Object[] row = {
                            student.getId(),
                            student.getName(),
                            selectedCourse.course.getName(),
                            "-",
                            "-",
                            "-",
                            String.format("%.1f%%", percentage),
                            status
                    };
                    tableModel.addRow(row);
                    totalStudents++;
                }
            } else {
                // Overall attendance - simplified to just show status
                String status = "N/A";

                Object[] row = {
                        student.getId(),
                        student.getName(),
                        "All Courses",
                        "-",
                        "-",
                        "-",
                        "-",
                        status
                };
                tableModel.addRow(row);
                totalStudents++;
            }
        }

        // Update summary
        updateSummary(totalStudents, lowAttendanceCount);
    }

    private String getAttendanceStatus(double percentage) {
        if (percentage >= 85)
            return "Excellent";
        if (percentage >= 75)
            return "Good";
        return "Low";
    }

    private void updateSummary(int total, int lowCount) {
        summaryLabel.setText(String.format(
                "Total Students: %d | Low Attendance (<75%%): %d | Percentage with Low Attendance: %.1f%%",
                total, lowCount, total > 0 ? (lowCount * 100.0 / total) : 0));
    }

    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Report");
        fileChooser.setSelectedFile(new java.io.File("attendance_report.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();

            // Prepare data
            List<Object[]> data = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object[] row = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row[j] = tableModel.getValueAt(i, j);
                }
                data.add(row);
            }

            // Export
            String[] headers = new String[tableModel.getColumnCount()];
            for (int i = 0; i < headers.length; i++) {
                headers[i] = tableModel.getColumnName(i);
            }

            if (ReportGenerator.generateCSV(filename, headers, data)) {
                UIHelper.showSuccessMessage(this, "Report exported successfully!");
            } else {
                UIHelper.showErrorMessage(this, "Failed to export report.");
            }
        }
    }

    private void printReport() {
        try {
            reportTable.print();
        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Failed to print report.");
        }
    }

    private static class CourseItem {
        Course course;

        CourseItem(Course course) {
            this.course = course;
        }

        @Override
        public String toString() {
            return course == null ? "All Courses" : course.getName();
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
