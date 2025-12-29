package com.college.ui.courses;

import com.college.dao.CourseDAO;
import com.college.models.Course;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Course Management Panel
 * Displays courses and provides management interface
 */
public class CourseManagementPanel extends JPanel {

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private CourseDAO courseDAO;
    private String userRole;

    public CourseManagementPanel(String role) {
        this.userRole = role;
        courseDAO = new CourseDAO();
        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel(userRole.equals("STUDENT") ? "My Courses" : "Course Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel - Only show for Admin/Faculty
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JButton addButton = UIHelper.createSuccessButton("Add Course");
            addButton.setPreferredSize(new Dimension(150, 40));
            addButton.addActionListener(e -> addCourse());
            buttonPanel.add(addButton);
        }

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Code", "Name", "Credits", "Department", "Semester" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        UIHelper.styleTable(courseTable);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> courses = courseDAO.getAllCourses();

        for (Course course : courses) {
            Object[] row = {
                    course.getId(),
                    course.getCode(),
                    course.getName(),
                    course.getCredits(),
                    course.getDepartment(),
                    course.getSemester()
            };
            tableModel.addRow(row);
        }
    }

    private void addCourse() {
        // Simple add dialog
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField creditsField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField semesterField = new JTextField();

        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);
        panel.add(new JLabel("Department:"));
        panel.add(deptField);
        panel.add(new JLabel("Semester:"));
        panel.add(semesterField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Course course = new Course();
                course.setCode(codeField.getText().trim());
                course.setName(nameField.getText().trim());
                course.setCredits(Integer.parseInt(creditsField.getText().trim()));
                course.setDepartment(deptField.getText().trim());
                course.setSemester(Integer.parseInt(semesterField.getText().trim()));

                if (courseDAO.addCourse(course)) {
                    UIHelper.showSuccessMessage(this, "Course added successfully!");
                    loadCourses();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to add course!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Invalid data!");
            }
        }
    }
}
