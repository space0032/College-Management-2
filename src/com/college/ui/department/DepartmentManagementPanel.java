package com.college.ui.department;

import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Department Management Panel
 * Allows admins to manage academic departments
 */
public class DepartmentManagementPanel extends JPanel {

    private DepartmentDAO departmentDAO;
    private JTable departmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public DepartmentManagementPanel() {
        departmentDAO = new DepartmentDAO();
        initComponents();
        loadDepartments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Table
        add(createTablePanel(), BorderLayout.CENTER);

        // Buttons
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Department Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchDepartments());

        JButton addButton = UIHelper.createPrimaryButton("Add Department");
        addButton.addActionListener(e -> showAddDialog());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = { "ID", "Code", "Name", "Head of Department", "Description" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        departmentTable = new JTable(tableModel);
        departmentTable.setRowHeight(25);
        departmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(departmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editDepartment());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteDepartment());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadDepartments());

        panel.add(refreshButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    private void loadDepartments() {
        tableModel.setRowCount(0);

        for (Department dept : departmentDAO.getAllDepartments()) {
            Object[] row = {
                    dept.getId(),
                    dept.getCode(),
                    dept.getName(),
                    dept.getHeadOfDepartment(),
                    dept.getDescription()
            };
            tableModel.addRow(row);
        }
    }

    private void searchDepartments() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadDepartments();
            return;
        }

        tableModel.setRowCount(0);
        for (Department dept : departmentDAO.searchDepartments(query)) {
            Object[] row = {
                    dept.getId(),
                    dept.getCode(),
                    dept.getName(),
                    dept.getHeadOfDepartment(),
                    dept.getDescription()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddDialog() {
        DepartmentDialog dialog = new DepartmentDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isDepartmentSaved()) {
            loadDepartments();
        }
    }

    private void editDepartment() {
        int selectedRow = departmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to edit", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Department dept = departmentDAO.getDepartmentById(id);

        if (dept != null) {
            DepartmentDialog dialog = new DepartmentDialog((Frame) SwingUtilities.getWindowAncestor(this), dept);
            dialog.setVisible(true);

            if (dialog.isDepartmentSaved()) {
                loadDepartments();
            }
        }
    }

    private void deleteDepartment() {
        int selectedRow = departmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department to delete", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);

        // Check if department has courses
        if (departmentDAO.hasCourses(id)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete department '" + name
                            + "' because it has courses assigned.\nPlease reassign or delete the courses first.",
                    "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete department '" + name + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (departmentDAO.deleteDepartment(id)) {
                JOptionPane.showMessageDialog(this, "Department deleted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadDepartments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete department", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
