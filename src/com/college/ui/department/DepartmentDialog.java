package com.college.ui.department;

import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for adding/editing departments
 */
public class DepartmentDialog extends JDialog {

    private DepartmentDAO departmentDAO;
    private Department department;
    private boolean departmentSaved = false;

    private JTextField nameField;
    private JTextField codeField;
    private JTextField hodField;
    private JTextArea descriptionArea;

    public DepartmentDialog(Frame parent, Department department) {
        super(parent, department == null ? "Add Department" : "Edit Department", true);
        this.department = department;
        this.departmentDAO = new DepartmentDAO();

        initComponents();
        if (department != null) {
            populateFields();
        }

        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Department Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Code
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Department Code:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        codeField = new JTextField(10);
        formPanel.add(codeField, gbc);

        // Head of Department
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Head of Department:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        hodField = new JTextField(20);
        formPanel.add(hodField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createPrimaryButton("Save");
        saveButton.addActionListener(e -> saveDepartment());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        nameField.setText(department.getName());
        codeField.setText(department.getCode());
        hodField.setText(department.getHeadOfDepartment());
        descriptionArea.setText(department.getDescription());
    }

    private void saveDepartment() {
        // Validate
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();

        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Department name and code are required!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create or update department
        if (department == null) {
            department = new Department();
        }

        department.setName(name);
        department.setCode(code.toUpperCase());
        department.setHeadOfDepartment(hodField.getText().trim());
        department.setDescription(descriptionArea.getText().trim());

        boolean success;
        if (department.getId() == 0) {
            success = departmentDAO.addDepartment(department);
        } else {
            success = departmentDAO.updateDepartment(department);
        }

        if (success) {
            departmentSaved = true;
            JOptionPane.showMessageDialog(this,
                    "Department saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save department. Code might already exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDepartmentSaved() {
        return departmentSaved;
    }
}
