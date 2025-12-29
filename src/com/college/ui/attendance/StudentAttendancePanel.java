package com.college.ui.attendance;

import com.college.dao.AttendanceDAO;
import com.college.dao.CourseDAO;
import com.college.models.Course;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student Attendance View Panel
 * Shows attendance percentage for logged-in student
 */
public class StudentAttendancePanel extends JPanel {

    private AttendanceDAO attendanceDAO;
    private CourseDAO courseDAO;
    private int studentId;

    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel overallLabel;

    public StudentAttendancePanel(int studentId) {
        this.studentId = studentId;
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();

        initComponents();
        loadAttendanceData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("My Attendance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        overallLabel = new JLabel("Overall: Calculating...");
        overallLabel.setFont(new Font("Arial", Font.BOLD, 16));
        overallLabel.setForeground(new Color(52, 152, 219));

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(overallLabel, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JLabel infoLabel = new JLabel("📊 Minimum 75% attendance required");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        infoPanel.add(infoLabel);

        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Course", "Total Classes", "Present", "Absent", "Attendance %", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        UIHelper.styleTable(attendanceTable);
        attendanceTable.setRowHeight(50);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadAttendanceData() {
        tableModel.setRowCount(0);
        List<Course> courses = courseDAO.getAllCourses();

        double totalPercentage = 0;
        int courseCount = 0;

        for (Course course : courses) {
            double percentage = attendanceDAO.getAttendancePercentage(studentId, course.getId());

            if (percentage > 0 || attendanceDAO.getAttendanceByCourseAndDate(course.getId(),
                    new java.util.Date()).size() > 0) {

                // Get total classes and present count (simplified calculation)
                int totalClasses = (int) Math.round(100 / (percentage > 0 ? percentage : 1));
                int presentClasses = (int) Math.round(totalClasses * percentage / 100);
                int absentClasses = totalClasses - presentClasses;

                String status;
                Color statusColor;
                if (percentage >= 75) {
                    status = "✓ Good";
                    statusColor = new Color(46, 204, 113);
                } else if (percentage >= 60) {
                    status = "⚠ Warning";
                    statusColor = new Color(243, 156, 18);
                } else {
                    status = "✗ Critical";
                    statusColor = new Color(231, 76, 60);
                }

                Object[] row = {
                        course.getName(),
                        totalClasses,
                        presentClasses,
                        absentClasses,
                        String.format("%.1f%%", percentage),
                        status
                };
                tableModel.addRow(row);

                totalPercentage += percentage;
                courseCount++;
            }
        }

        // Update overall percentage
        if (courseCount > 0) {
            double overallPercentage = totalPercentage / courseCount;
            overallLabel.setText(String.format("Overall: %.1f%%", overallPercentage));

            if (overallPercentage >= 75) {
                overallLabel.setForeground(new Color(46, 204, 113));
            } else if (overallPercentage >= 60) {
                overallLabel.setForeground(new Color(243, 156, 18));
            } else {
                overallLabel.setForeground(new Color(231, 76, 60));
            }
        } else {
            overallLabel.setText("Overall: No data");
        }

        if (tableModel.getRowCount() == 0) {
            Object[] row = { "No attendance data available", "", "", "", "", "" };
            tableModel.addRow(row);
        }
    }
}
