package com.college.ui.student;

import com.college.dao.StudentDAO;
import com.college.models.Student;
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
    private JTextField searchField;

    public StudentManagementPanel() {
        studentDAO = new StudentDAO();
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

        // Table columns
        String[] columns = { "ID", "Name", "Email", "Phone", "Course", "Batch", "Enrollment Date" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        studentTable = new JTable(tableModel);
        UIHelper.styleTable(studentTable);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(200);

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

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    /**
     * Load all students into table
     */
    private void loadStudents() {
        tableModel.setRowCount(0); // Clear existing rows
        List<Student> students = studentDAO.getAllStudents();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Student student : students) {
            Object[] row = {
                    student.getId(),
                    student.getName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getCourse(),
                    student.getBatch(),
                    sdf.format(student.getEnrollmentDate())
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
        List<Student> students = studentDAO.searchStudents(keyword);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Student student : students) {
            Object[] row = {
                    student.getId(),
                    student.getName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getCourse(),
                    student.getBatch(),
                    sdf.format(student.getEnrollmentDate())
            };
            tableModel.addRow(row);
        }
    }

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

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);

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
}
