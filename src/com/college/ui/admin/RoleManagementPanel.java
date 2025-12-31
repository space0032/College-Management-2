package com.college.ui.admin;

import com.college.dao.PermissionDAO;
import com.college.dao.RoleDAO;
import com.college.models.Permission;
import com.college.models.Role;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Role Management Panel for RBAC Administration
 */
public class RoleManagementPanel extends JPanel {

    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    private JTable rolesTable;
    private DefaultTableModel rolesModel;
    private JList<Permission> permissionsList;
    private DefaultListModel<Permission> permissionsListModel;
    private Role selectedRole;

    public RoleManagementPanel() {
        this.roleDAO = new RoleDAO();
        this.permissionDAO = new PermissionDAO();
        initComponents();
        loadRoles();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Role & Permission Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton addRoleBtn = UIHelper.createSuccessButton("Add Role");
        addRoleBtn.addActionListener(e -> showAddRoleDialog());
        headerPanel.add(addRoleBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);

        // Left: Roles Table
        JPanel rolesPanel = createRolesPanel();
        splitPane.setLeftComponent(rolesPanel);

        // Right: Permissions List
        JPanel permissionsPanel = createPermissionsPanel();
        splitPane.setRightComponent(permissionsPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createRolesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Roles"));

        String[] columns = { "ID", "Code", "Name", "System Role" };
        rolesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rolesTable = new JTable(rolesModel);
        UIHelper.styleTable(rolesTable);
        rolesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRoleSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(rolesTable);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(Color.WHITE);

        JButton editBtn = new JButton("Edit Role");
        editBtn.addActionListener(e -> editSelectedRole());

        JButton deleteBtn = UIHelper.createDangerButton("Delete Role");
        deleteBtn.addActionListener(e -> deleteSelectedRole());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadRoles());

        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPermissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Permissions for Selected Role"));

        permissionsListModel = new DefaultListModel<>();
        permissionsList = new JList<>(permissionsListModel);
        permissionsList.setCellRenderer(new PermissionCellRenderer());
        JScrollPane scrollPane = new JScrollPane(permissionsList);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(Color.WHITE);

        JButton managePermBtn = new JButton("Manage Permissions");
        managePermBtn.addActionListener(e -> showManagePermissionsDialog());
        btnPanel.add(managePermBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRoles() {
        rolesModel.setRowCount(0);
        List<Role> roles = roleDAO.getAllRoles();
        for (Role role : roles) {
            Object[] row = {
                    role.getId(),
                    role.getCode(),
                    role.getName(),
                    role.isSystemRole() ? "Yes" : "No"
            };
            rolesModel.addRow(row);
        }
    }

    private void onRoleSelected() {
        int selectedRow = rolesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int roleId = (int) rolesModel.getValueAt(selectedRow, 0);
            selectedRole = roleDAO.getRoleById(roleId);
            loadPermissionsForRole();
        } else {
            selectedRole = null;
            permissionsListModel.clear();
        }
    }

    private void loadPermissionsForRole() {
        permissionsListModel.clear();
        if (selectedRole != null) {
            for (Permission p : selectedRole.getPermissions()) {
                permissionsListModel.addElement(p);
            }
        }
    }

    private void showAddRoleDialog() {
        JTextField codeField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextArea descField = new JTextArea(3, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Role",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Role role = new Role();
            role.setCode(codeField.getText().toUpperCase().trim());
            role.setName(nameField.getText().trim());
            role.setDescription(descField.getText().trim());
            role.setSystemRole(false);

            if (roleDAO.createRole(role)) {
                UIHelper.showSuccessMessage(this, "Role created successfully!");
                loadRoles();
            } else {
                UIHelper.showErrorMessage(this, "Failed to create role.");
            }
        }
    }

    private void editSelectedRole() {
        if (selectedRole == null) {
            UIHelper.showErrorMessage(this, "Please select a role to edit.");
            return;
        }

        if (selectedRole.isSystemRole()) {
            UIHelper.showErrorMessage(this, "System roles cannot be edited.");
            return;
        }

        JTextField codeField = new JTextField(selectedRole.getCode(), 20);
        JTextField nameField = new JTextField(selectedRole.getName(), 20);
        JTextArea descField = new JTextArea(selectedRole.getDescription(), 3, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Role",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            selectedRole.setCode(codeField.getText().toUpperCase().trim());
            selectedRole.setName(nameField.getText().trim());
            selectedRole.setDescription(descField.getText().trim());

            if (roleDAO.updateRole(selectedRole)) {
                UIHelper.showSuccessMessage(this, "Role updated successfully!");
                loadRoles();
            } else {
                UIHelper.showErrorMessage(this, "Failed to update role.");
            }
        }
    }

    private void deleteSelectedRole() {
        if (selectedRole == null) {
            UIHelper.showErrorMessage(this, "Please select a role to delete.");
            return;
        }

        if (selectedRole.isSystemRole()) {
            UIHelper.showErrorMessage(this, "System roles cannot be deleted.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete role '" + selectedRole.getName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (roleDAO.deleteRole(selectedRole.getId())) {
                UIHelper.showSuccessMessage(this, "Role deleted successfully!");
                selectedRole = null;
                loadRoles();
                permissionsListModel.clear();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete role.");
            }
        }
    }

    private void showManagePermissionsDialog() {
        if (selectedRole == null) {
            UIHelper.showErrorMessage(this, "Please select a role first.");
            return;
        }

        List<Permission> allPermissions = permissionDAO.getAllPermissions();
        JCheckBox[] checkboxes = new JCheckBox[allPermissions.size()];
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Group by category
        String currentCategory = "";
        for (int i = 0; i < allPermissions.size(); i++) {
            Permission p = allPermissions.get(i);
            if (!p.getCategory().equals(currentCategory)) {
                currentCategory = p.getCategory();
                JLabel categoryLabel = new JLabel("-- " + currentCategory + " --");
                categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
                panel.add(categoryLabel);
            }
            checkboxes[i] = new JCheckBox(p.getName() + " (" + p.getCode() + ")");
            checkboxes[i].setSelected(selectedRole.hasPermission(p.getCode()));
            panel.add(checkboxes[i]);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        int result = JOptionPane.showConfirmDialog(this, scrollPane,
                "Manage Permissions for: " + selectedRole.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            List<Integer> selectedPermIds = new ArrayList<>();
            for (int i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].isSelected()) {
                    selectedPermIds.add(allPermissions.get(i).getId());
                }
            }

            if (roleDAO.setRolePermissions(selectedRole.getId(), selectedPermIds)) {
                UIHelper.showSuccessMessage(this, "Permissions updated!");
                selectedRole = roleDAO.getRoleById(selectedRole.getId());
                loadPermissionsForRole();
            } else {
                UIHelper.showErrorMessage(this, "Failed to update permissions.");
            }
        }
    }

    // Custom renderer for permissions list
    private class PermissionCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Permission) {
                Permission p = (Permission) value;
                setText(p.getName() + " [" + p.getCategory() + "]");
            }
            return this;
        }
    }
}
