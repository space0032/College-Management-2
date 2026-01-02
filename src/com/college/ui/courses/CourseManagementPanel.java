package com.college.ui.courses;

import com.college.dao.CourseDAO;
import com.college.dao.DepartmentDAO;
import com.college.dao.StudentDAO;
import com.college.models.Course;
import com.college.models.Department;
import com.college.models.Student;
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
    private int userId;

    public CourseManagementPanel(String role) {
        this(role, 0);
    }

    public CourseManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
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

        // Button Panel - Only show for users with proper permissions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        // Get session for permission checks
        com.college.utils.SessionManager session = com.college.utils.SessionManager.getInstance();

        // Add/Edit/Delete Course buttons - require MANAGE_ALL_COURSES or MANAGE_OWN_COURSES
        if (session.hasPermission("MANAGE_ALL_COURSES") || session.hasPermission("MANAGE_OWN_COURSES")) {
            JButton addButton = UIHelper.createSuccessButton("Add Course");
            addButton.setPreferredSize(new Dimension(150, 40));
            addButton.addActionListener(e -> addCourse());
            buttonPanel.add(addButton);

            JButton editButton = UIHelper.createPrimaryButton("Edit Course");
            editButton.setPreferredSize(new Dimension(150, 40));
            editButton.addActionListener(e -> editCourse());
            buttonPanel.add(editButton);

            JButton deleteButton = UIHelper.createDangerButton("Delete Course");
            deleteButton.setPreferredSize(new Dimension(150, 40));
            deleteButton.addActionListener(e -> deleteCourse());
            buttonPanel.add(deleteButton);
        }

        // Export button - available to anyone who can view courses
        if (session.hasPermission("VIEW_COURSES")) {
            JButton exportButton = UIHelper.createPrimaryButton("Export");
            exportButton.setPreferredSize(new Dimension(120, 40));
            exportButton.addActionListener(
                    e -> com.college.utils.TableExporter.showExportDialog(this, courseTable, "courses"));
            buttonPanel.add(exportButton);
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
        List<Course> courses;

        // Filter courses for students - show only their department and semester courses
        if (userRole.equals("STUDENT") && userId > 0) {
            courses = getStudentCourses();
        } else {
            courses = courseDAO.getAllCourses();
        }

        for (Course course : courses) {
            Object[] row = {
                    course.getId(),
                    course.getCode(),
                    course.getName(),
                    course.getCredits(),
                    course.getDepartmentName() != null ? course.getDepartmentName() : course.getDepartment(),
                    course.getSemester()
            };
            tableModel.addRow(row);
        }
    }

    private void addCourse() {
        // Load departments for dropdown
        DepartmentDAO departmentDAO = new DepartmentDAO();
        List<Department> departments = departmentDAO.getAllDepartments();

        // Create form panel
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField creditsField = new JTextField();

        // Department dropdown instead of text field
        JComboBox<DepartmentItem> deptCombo = new JComboBox<>();
        deptCombo.addItem(new DepartmentItem(null)); // "No Department" option
        for (Department dept : departments) {
            deptCombo.addItem(new DepartmentItem(dept));
        }

        JTextField semesterField = new JTextField();

        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);
        panel.add(new JLabel("Department:"));
        panel.add(deptCombo);
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
                course.setSemester(Integer.parseInt(semesterField.getText().trim()));

                // Set department from dropdown
                DepartmentItem selectedDept = (DepartmentItem) deptCombo.getSelectedItem();
                if (selectedDept != null && selectedDept.getDepartment() != null) {
                    course.setDepartmentId(selectedDept.getDepartment().getId());
                    course.setDepartment(selectedDept.getDepartment().getName());
                } else {
                    course.setDepartmentId(0);
                    course.setDepartment("");
                }

                if (courseDAO.addCourse(course)) {
                    UIHelper.showSuccessMessage(this, "Course added successfully!");
                    loadCourses();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to add course!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Invalid data: " + e.getMessage());
            }
        }
    }

    // Helper class for department dropdown
    private static class DepartmentItem {
        private Department department;

        public DepartmentItem(Department department) {
            this.department = department;
        }

        public Department getDepartment() {
            return department;
        }

        @Override
        public String toString() {
            return department == null ? "-- No Department --"
                    : department.getName() + " (" + department.getCode() + ")";
        }
    }

    private void editCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a course to edit!");
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        Course course = courseDAO.getCourseById(courseId);

        if (course == null) {
            UIHelper.showErrorMessage(this, "Course not found!");
            return;
        }

        // Create edit dialog
        DepartmentDAO departmentDAO = new DepartmentDAO();
        List<Department> departments = departmentDAO.getAllDepartments();

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField codeField = new JTextField(course.getCode());
        JTextField nameField = new JTextField(course.getName());
        JTextField creditsField = new JTextField(String.valueOf(course.getCredits()));

        // Department dropdown
        JComboBox<DepartmentItem> deptCombo = new JComboBox<>();
        deptCombo.addItem(new DepartmentItem(null));
        for (Department dept : departments) {
            DepartmentItem item = new DepartmentItem(dept);
            deptCombo.addItem(item);
            // Select current department
            if (course.getDepartmentId() > 0 && dept.getId() == course.getDepartmentId()) {
                deptCombo.setSelectedItem(item);
            }
        }

        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);
        panel.add(new JLabel("Department:"));
        panel.add(deptCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                course.setCode(codeField.getText().trim());
                course.setName(nameField.getText().trim());
                course.setCredits(Integer.parseInt(creditsField.getText().trim()));

                DepartmentItem selectedDept = (DepartmentItem) deptCombo.getSelectedItem();
                if (selectedDept != null && selectedDept.getDepartment() != null) {
                    course.setDepartmentId(selectedDept.getDepartment().getId());
                    course.setDepartment(selectedDept.getDepartment().getName());
                } else {
                    course.setDepartmentId(0);
                    course.setDepartment("");
                }

                if (courseDAO.updateCourse(course)) {
                    UIHelper.showSuccessMessage(this, "Course updated successfully!");
                    loadCourses();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to update course!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Invalid data: " + e.getMessage());
            }
        }
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a course to delete!");
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course '" + courseName + "'?\nThis action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (courseDAO.deleteCourse(courseId)) {
                UIHelper.showSuccessMessage(this, "Course deleted successfully!");
                loadCourses();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete course!");
            }
        }
    }

    /**
     * Get courses for a specific student based on their department and semester
     */
    private List<Course> getStudentCourses() {
        List<Course> allCourses = courseDAO.getAllCourses();
        List<Course> studentCourses = new java.util.ArrayList<>();

        try {
            // Get student's department and semester
            StudentDAO studentDAO = new StudentDAO();
            Student student = studentDAO.getStudentByUserId(userId);

            if (student != null) {
                String studentDept = student.getDepartment();
                int studentSemester = student.getSemester();

                // Filter courses that match student's department AND semester
                for (Course course : allCourses) {
                    boolean deptMatches = course.getDepartmentName() != null &&
                            course.getDepartmentName().equals(studentDept);
                    boolean semesterMatches = course.getSemester() == studentSemester;
                    
                    if (deptMatches && semesterMatches) {
                        studentCourses.add(course);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return studentCourses;
    }
}
