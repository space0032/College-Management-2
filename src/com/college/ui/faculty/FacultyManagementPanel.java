package com.college.ui.faculty;

import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Faculty/Staff Management Panel
 * Displays faculty and provides management interface
 */
public class FacultyManagementPanel extends JPanel {

    private JTable facultyTable;
    private DefaultTableModel tableModel;
    private FacultyDAO facultyDAO;
    private JTextField searchField;

    public FacultyManagementPanel() {
        facultyDAO = new FacultyDAO();
        initComponents();
        loadFaculty();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);

        searchField = UIHelper.createTextField(15);
        searchField.setToolTipText("Search by name or email");

        JButton searchButton = UIHelper.createPrimaryButton("Search");
        searchButton.addActionListener(e -> searchFaculty());

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadFaculty());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = UIHelper.createSuccessButton("Add Faculty");
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> addFaculty());

        JButton editButton = UIHelper.createPrimaryButton("Edit Faculty");
        editButton.setPreferredSize(new Dimension(150, 40));
        editButton.addActionListener(e -> editFaculty());

        JButton deleteButton = UIHelper.createDangerButton("Delete Faculty");
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteFaculty());

        JButton assignRoleButton = UIHelper.createPrimaryButton("Assign Role");
        assignRoleButton.setPreferredSize(new Dimension(150, 40));
        assignRoleButton.addActionListener(e -> assignRole());

        JButton exportButton = UIHelper.createPrimaryButton("Export");
        exportButton.setPreferredSize(new Dimension(120, 40));
        exportButton.addActionListener(
                e -> com.college.utils.TableExporter.showExportDialog(this, facultyTable, "faculty"));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(assignRoleButton);
        buttonPanel.add(exportButton);

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "User ID", "Name", "Role", "Email", "Phone", "Department", "Qualification",
                "Join Date" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        facultyTable = new JTable(tableModel);
        UIHelper.styleTable(facultyTable);

        // Hide Database ID column (Index 0)
        facultyTable.getColumnModel().getColumn(0).setMinWidth(0);
        facultyTable.getColumnModel().getColumn(0).setMaxWidth(0);
        facultyTable.getColumnModel().getColumn(0).setWidth(0);

        facultyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // User ID
        facultyTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Name
        facultyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Role
        facultyTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Email

        JScrollPane scrollPane = new JScrollPane(facultyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadFaculty() {
        tableModel.setRowCount(0);
        searchField.setText("");
        List<Faculty> facultyList = facultyDAO.getAllFaculty();

        for (Faculty faculty : facultyList) {
            Object[] row = {
                    faculty.getId(),
                    faculty.getUsername(),
                    faculty.getName(),
                    faculty.getRoleName(),
                    faculty.getEmail(),
                    faculty.getPhone(),
                    faculty.getDepartment(),
                    faculty.getQualification(),
                    faculty.getJoinDate()
            };
            tableModel.addRow(row);
        }
    }

    private void searchFaculty() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadFaculty();
            return;
        }

        tableModel.setRowCount(0);
        List<Faculty> facultyList = facultyDAO.searchFaculty(keyword);

        for (Faculty faculty : facultyList) {
            Object[] row = {
                    faculty.getId(),
                    faculty.getUsername(),
                    faculty.getName(),
                    faculty.getRoleName(),
                    faculty.getEmail(),
                    faculty.getPhone(),
                    faculty.getDepartment(),
                    faculty.getQualification(),
                    faculty.getJoinDate()
            };
            tableModel.addRow(row);
        }

        if (facultyList.isEmpty()) {
            UIHelper.showErrorMessage(this, "No faculty found with keyword: " + keyword);
        }
    }

    private void addFaculty() {
        AddFacultyDialog dialog = new AddFacultyDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            loadFaculty();
        }
    }

    private void editFaculty() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a faculty member to edit!");
            return;
        }

        int facultyId = (int) tableModel.getValueAt(selectedRow, 0);
        Faculty faculty = facultyDAO.getFacultyById(facultyId);

        if (faculty != null) {
            AddFacultyDialog dialog = new AddFacultyDialog((Frame) SwingUtilities.getWindowAncestor(this), faculty);
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                loadFaculty();
            }
        }
    }

    private void deleteFaculty() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a faculty member to delete!");
            return;
        }

        String name = (String) tableModel.getValueAt(selectedRow, 2); // Index is now 2 due to added UserID
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete faculty: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int facultyId = (int) tableModel.getValueAt(selectedRow, 0);
            if (facultyDAO.deleteFaculty(facultyId)) {
                UIHelper.showSuccessMessage(this, "Faculty deleted successfully!");
                loadFaculty();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete faculty!");
            }
        }
    }

    private void assignRole() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a faculty member to assign a role!");
            return;
        }

        int facultyId = (int) tableModel.getValueAt(selectedRow, 0);
        String facultyName = (String) tableModel.getValueAt(selectedRow, 2); // Index is now 2 due to added UserID

        // Get user_id for this faculty
        Faculty faculty = facultyDAO.getFacultyById(facultyId);
        if (faculty == null || faculty.getUserId() == 0) {
            UIHelper.showErrorMessage(this, "Could not find user account for this faculty.");
            return;
        }

        // Load available roles
        com.college.dao.RoleDAO roleDAO = new com.college.dao.RoleDAO();
        java.util.List<com.college.models.Role> roles = roleDAO.getAllRoles();

        if (roles.isEmpty()) {
            UIHelper.showErrorMessage(this, "No roles available. Please set up RBAC first.");
            return;
        }

        // Create dropdown
        com.college.models.Role[] roleArray = roles.toArray(new com.college.models.Role[0]);
        com.college.models.Role selectedRole = (com.college.models.Role) JOptionPane.showInputDialog(
                this,
                "Select role for: " + facultyName,
                "Assign Role",
                JOptionPane.PLAIN_MESSAGE,
                null,
                roleArray,
                roleArray[0]);

        if (selectedRole != null) {
            if (roleDAO.assignRoleToUser(faculty.getUserId(), selectedRole.getId())) {
                UIHelper.showSuccessMessage(this, "Role '" + selectedRole.getName() + "' assigned to " + facultyName);
            } else {
                UIHelper.showErrorMessage(this, "Failed to assign role.");
            }
        }
    }
}
