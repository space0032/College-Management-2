package com.college.ui.student;

import com.college.dao.StudentDAO;
import com.college.dao.DepartmentDAO;
import com.college.models.Student;
import com.college.models.Department;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student Management Panel
 * Displays student list and provides CRUD operations
 */
public class StudentManagementPanel extends JPanel {

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private StudentDAO studentDAO;
    private DepartmentDAO departmentDAO;
    private JTextField searchField;
    private JComboBox<DepartmentFilterItem> departmentFilter;
    private String userRole;

    public StudentManagementPanel() {
        this("ADMIN"); // Default to ADMIN if no role provided (backward compatibility)
    }

    public StudentManagementPanel(String role) {
        this.userRole = role;
        studentDAO = new StudentDAO();
        departmentDAO = new DepartmentDAO();
        initComponents();
        loadStudents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel with Title and Search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);

        JLabel searchLabel = UIHelper.createLabel("Search:");
        searchField = UIHelper.createTextField(20);
        JButton searchButton = UIHelper.createPrimaryButton("Search");
        searchButton.addActionListener(e -> searchStudents());

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadStudents());

        // Department filter
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Department:"));
        departmentFilter = new JComboBox<>();
        departmentFilter.addItem(new DepartmentFilterItem(null)); // "All Departments"
        for (Department dept : departmentDAO.getAllDepartments()) {
            departmentFilter.addItem(new DepartmentFilterItem(dept));
        }
        departmentFilter.addActionListener(e -> filterByDepartment());
        searchPanel.add(departmentFilter);

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create table panel with student data
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Table columns - Added "ID" as first hidden column
        String[] columns = { "ID", "Enrollment ID", "Name", "Email", "Phone", "Department", "Semester", "Batch",
                "Enrollment Date" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        studentTable = new JTable(tableModel);
        UIHelper.styleTable(studentTable);

        // Hide ID column (index 0)
        studentTable.getColumnModel().removeColumn(studentTable.getColumnModel().getColumn(0));

        // Set widths (View indices, so 0 is now Enrollment ID)
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Width for Enrollment ID
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create button panel with actions
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        JButton addButton = UIHelper.createSuccessButton("Add Student");
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> addStudent());

        JButton editButton = UIHelper.createPrimaryButton("Edit Student");
        editButton.setPreferredSize(new Dimension(150, 40));
        editButton.addActionListener(e -> editStudent());

        JButton deleteButton = UIHelper.createDangerButton("Delete Student");
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteStudent());

        JButton exportButton = UIHelper.createPrimaryButton("Export");
        exportButton.setPreferredSize(new Dimension(120, 40));
        exportButton.addActionListener(
                e -> com.college.utils.TableExporter.showExportDialog(this, studentTable, "students"));

        if (!"WARDEN".equals(userRole)) {
            panel.add(addButton);
            panel.add(editButton);
            panel.add(deleteButton);
        }
        panel.add(exportButton);

        return panel;
    }

    /**
     * Load all students into table
     */
    private void loadStudents() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Student> students;
        if ("WARDEN".equals(userRole)) {
            students = studentDAO.getHostelStudents();
        } else {
            students = studentDAO.getAllStudents();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Student student : students) {
            Object[] row = {
                    student.getId(), // Hidden ID column
                    student.getUsername() != null ? student.getUsername() : student.getId(), // Show username
                                                                                             // (Enrollment ID)
                    student.getName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getDepartment() != null && !student.getDepartment().isEmpty() ? student.getDepartment()
                            : "-",
                    student.getSemester() > 0 ? String.valueOf(student.getSemester()) : "-",
                    student.getBatch(),
                    student.getEnrollmentDate() != null ? sdf.format(student.getEnrollmentDate()) : "-"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Search students by keyword
     */
    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }

        tableModel.setRowCount(0);
        List<Student> students;
        if ("WARDEN".equals(userRole)) {
            students = studentDAO.searchHostelStudents(keyword);
        } else {
            students = studentDAO.searchStudents(keyword);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Student student : students) {
            Object[] row = {
                    student.getId(), // Hidden ID
                    student.getUsername() != null ? student.getUsername() : student.getId(),
                    student.getName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getDepartment() != null && !student.getDepartment().isEmpty() ? student.getDepartment()
                            : "-",
                    student.getSemester() > 0 ? String.valueOf(student.getSemester()) : "-",
                    student.getBatch(),
                    student.getEnrollmentDate() != null ? sdf.format(student.getEnrollmentDate()) : "-"
            };
            tableModel.addRow(row);
        }
    }

    // ... (addStudent skipped) ...

    /**
     * Add new student
     */
    private void addStudent() {
        AddStudentDialog dialog = new AddStudentDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            loadStudents();
        }
    }

    /**
     * Edit selected student
     */
    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a student to edit!");
            return;
        }

        // Get ID from model (always index 0)
        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        Student student = studentDAO.getStudentById(studentId);

        AddStudentDialog dialog = new AddStudentDialog((Frame) SwingUtilities.getWindowAncestor(this), student);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            loadStudents();
        }
    }

    /**
     * Delete selected student
     */
    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a student to delete!");
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0); // ID is at 0
        String studentName = (String) tableModel.getValueAt(selectedRow, 2); // Name is now at 2

        boolean confirmed = UIHelper.showConfirmDialog(this,
                "Are you sure you want to delete student: " + studentName + "?");

        if (confirmed) {
            if (studentDAO.deleteStudent(studentId)) {
                UIHelper.showSuccessMessage(this, "Student deleted successfully!");
                loadStudents();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete student!");
            }
        }
    }

    /**
     * Filter students by selected department
     */
    private void filterByDepartment() {
        DepartmentFilterItem selected = (DepartmentFilterItem) departmentFilter.getSelectedItem();
        if (selected == null || selected.getDepartment() == null) {
            loadStudents(); // Show all if no filter
            return;
        }

        tableModel.setRowCount(0);
        List<Student> allStudents = studentDAO.getAllStudents();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String filterDept = selected.getDepartment().getName();

        for (Student student : allStudents) {
            // Filter by department name
            if (student.getDepartment() != null && student.getDepartment().equals(filterDept)) {
                Object[] row = {
                        student.getId(), // Hidden ID
                        student.getUsername() != null ? student.getUsername() : student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getPhone(),
                        student.getDepartment() != null && !student.getDepartment().isEmpty() ? student.getDepartment()
                                : "-",
                        student.getSemester() > 0 ? String.valueOf(student.getSemester()) : "-",
                        student.getBatch(),
                        student.getEnrollmentDate() != null ? sdf.format(student.getEnrollmentDate()) : "-"
                };
                tableModel.addRow(row);
            }
        }
    }

    // Helper class for department filter dropdown
    private static class DepartmentFilterItem {
        private Department department;

        public DepartmentFilterItem(Department department) {
            this.department = department;
        }

        public Department getDepartment() {
            return department;
        }

        @Override
        public String toString() {
            return department == null ? "All Departments" : department.getName();
        }
    }
}
