package com.college.ui.attendance;

import com.college.dao.AttendanceDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Attendance;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Attendance Management Panel
 * For faculty to mark and view attendance
 */
public class AttendanceManagementPanel extends JPanel {

    private AttendanceDAO attendanceDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;

    private JComboBox<CourseItem> courseCombo;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JTextField dateField;

    private List<Student> currentStudents;

    public AttendanceManagementPanel() {
        attendanceDAO = new AttendanceDAO();
        courseDAO = new CourseDAO();
        studentDAO = new StudentDAO();
        currentStudents = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = createTopPanel();

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load courses after all components are initialized
        loadCourses();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Selection Panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        selectionPanel.setBackground(Color.WHITE);

        JLabel courseLabel = UIHelper.createLabel("Course:");
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(200, 30));
        courseCombo.addActionListener(e -> loadStudentsForCourse());

        JLabel dateLabel = UIHelper.createLabel("Date:");
        dateField = UIHelper.createTextField(10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));

        JButton loadButton = UIHelper.createPrimaryButton("Load");
        loadButton.addActionListener(e -> loadAttendance());

        selectionPanel.add(courseLabel);
        selectionPanel.add(courseCombo);
        selectionPanel.add(dateLabel);
        selectionPanel.add(dateField);
        selectionPanel.add(loadButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(selectionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Added "Enrollment ID" and kept "ID" for internal use (will hide it)
        String[] columns = { "ID", "Enrollment ID", "Student Name", "Status", "" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only status column editable (index 3)
            }
        };

        attendanceTable = new JTable(tableModel);
        UIHelper.styleTable(attendanceTable);

        // Hide the ID column
        attendanceTable.getColumnModel().getColumn(0).setMinWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setWidth(0);

        // Set width for Enrollment ID
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(120);

        // Status column with combo box
        JComboBox<String> statusCombo = new JComboBox<>(new String[] { "PRESENT", "ABSENT", "LATE" });
        attendanceTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusCombo));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        JButton markAllPresentButton = UIHelper.createSuccessButton("Mark All Present");
        markAllPresentButton.setPreferredSize(new Dimension(180, 40));
        markAllPresentButton.addActionListener(e -> markAllPresent());

        JButton saveButton = UIHelper.createPrimaryButton("Save Attendance");
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> saveAttendance());

        JButton viewReportButton = UIHelper.createPrimaryButton("View Report");
        viewReportButton.setPreferredSize(new Dimension(150, 40));
        viewReportButton.addActionListener(e -> viewReport());

        panel.add(markAllPresentButton);
        panel.add(saveButton);
        panel.add(viewReportButton);

        return panel;
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course course : courses) {
            courseCombo.addItem(new CourseItem(course));
        }
    }

    private void loadStudentsForCourse() {
        currentStudents.clear();
        tableModel.setRowCount(0);

        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected != null) {
            // For now, load all students (in real system, filter by enrolled students)
            currentStudents = studentDAO.getAllStudents();

            for (Student student : currentStudents) {
                Object[] row = {
                        student.getId(), // Hidden ID
                        student.getUsername() != null ? student.getUsername() : "-", // Enrollment ID
                        student.getName(),
                        "PRESENT", // Default status
                        ""
                };
                tableModel.addRow(row);
            }
        }
    }

    private void loadAttendance() {
        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateField.getText());

            List<Attendance> records = attendanceDAO.getAttendanceByCourseAndDate(
                    selected.course.getId(), date);

            // Update table with existing attendance
            for (Attendance record : records) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int studentId = (Integer) tableModel.getValueAt(i, 0);
                    if (studentId == record.getStudentId()) {
                        tableModel.setValueAt(record.getStatus(), i, 3); // Update Status column (index 3)
                        break;
                    }
                }
            }

        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid date format! Use yyyy-MM-dd");
        }
    }

    private void markAllPresent() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("PRESENT", i, 3); // Update Status column (index 3)
        }
    }

    private void saveAttendance() {
        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateField.getText());

            List<Attendance> attendanceList = new ArrayList<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int studentId = (Integer) tableModel.getValueAt(i, 0);
                String status = (String) tableModel.getValueAt(i, 3); // Get Status from index 3

                Attendance attendance = new Attendance();
                attendance.setStudentId(studentId);
                attendance.setCourseId(selected.course.getId());
                attendance.setDate(date);
                attendance.setStatus(status);

                attendanceList.add(attendance);
            }

            int saved = attendanceDAO.markBulkAttendance(attendanceList);

            if (saved > 0) {
                UIHelper.showSuccessMessage(this,
                        "Attendance saved successfully for " + saved + " students!");
            } else {
                UIHelper.showErrorMessage(this, "Failed to save attendance!");
            }

        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewReport() {
        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        // Show attendance report dialog
        AttendanceReportDialog dialog = new AttendanceReportDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                selected.course.getId(),
                selected.course.getName());
        dialog.setVisible(true);
    }

    // Helper class to store course in combo box
    private static class CourseItem {
        Course course;

        CourseItem(Course course) {
            this.course = course;
        }

        @Override
        public String toString() {
            return course.getName();
        }
    }
}
