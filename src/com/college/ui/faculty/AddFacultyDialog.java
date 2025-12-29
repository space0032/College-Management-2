package com.college.ui.faculty;

import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
import com.college.utils.DatabaseConnection;
import com.college.utils.UIHelper;
import com.college.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Add/Edit Faculty Dialog
 * Modal dialog for adding new faculty or editing existing ones
 */
public class AddFacultyDialog extends JDialog {

    private Faculty faculty;
    private FacultyDAO facultyDAO;
    private boolean success = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField qualificationField;
    private JTextField joinDateField;

    public AddFacultyDialog(Frame parent, Faculty faculty) {
        super(parent, faculty == null ? "Add Faculty" : "Edit Faculty", true);
        this.faculty = faculty;
        this.facultyDAO = new FacultyDAO();
        initComponents();

        if (faculty != null) {
            fillData();
        }
    }

    private void initComponents() {
        setSize(500, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = UIHelper.createTextField(20);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = UIHelper.createTextField(20);
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = UIHelper.createTextField(20);
        formPanel.add(phoneField, gbc);

        // Department
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Department:"), gbc);
        gbc.gridx = 1;
        departmentField = UIHelper.createTextField(20);
        formPanel.add(departmentField, gbc);

        // Qualification
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(UIHelper.createLabel("Qualification:"), gbc);
        gbc.gridx = 1;
        qualificationField = UIHelper.createTextField(20);
        formPanel.add(qualificationField, gbc);

        // Join Date
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(UIHelper.createLabel("Join Date:"), gbc);
        gbc.gridx = 1;
        joinDateField = UIHelper.createTextField(20);
        joinDateField.setToolTipText("Format: yyyy-MM-dd");
        // Set default to today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        joinDateField.setText(sdf.format(new Date()));
        formPanel.add(joinDateField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createSuccessButton("Save");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.addActionListener(e -> saveFaculty());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Fill form with faculty data (for editing)
     */
    private void fillData() {
        nameField.setText(faculty.getName());
        emailField.setText(faculty.getEmail());
        phoneField.setText(faculty.getPhone());
        departmentField.setText(faculty.getDepartment());
        qualificationField.setText(faculty.getQualification());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        joinDateField.setText(sdf.format(faculty.getJoinDate()));
    }

    /**
     * Save faculty data
     */
    private void saveFaculty() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Create or update faculty object
            boolean isNewFaculty = (faculty == null);
            if (faculty == null) {
                faculty = new Faculty();
            }

            faculty.setName(nameField.getText().trim());
            faculty.setEmail(emailField.getText().trim());
            faculty.setPhone(phoneField.getText().trim());
            faculty.setDepartment(departmentField.getText().trim());
            faculty.setQualification(qualificationField.getText().trim());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            faculty.setJoinDate(sdf.parse(joinDateField.getText().trim()));

            // Save to database
            if (isNewFaculty) {
                // Add new faculty and get generated ID
                int facultyId = facultyDAO.addFaculty(faculty);

                if (facultyId > 0) {
                    // Create login credentials
                    String[] credentials = createLoginCredentials(facultyId);

                    if (credentials != null) {
                        // Show success and credentials dialog
                        showCredentialsDialog(credentials[0], credentials[1]);
                        success = true;
                        dispose();
                    } else {
                        UIHelper.showErrorMessage(this, "Faculty created but failed to create login credentials!");
                    }
                } else {
                    UIHelper.showErrorMessage(this, "Failed to save faculty!");
                }
            } else {
                // Update existing faculty
                boolean result = facultyDAO.updateFaculty(faculty);
                if (result) {
                    UIHelper.showSuccessMessage(this, "Faculty updated successfully!");
                    success = true;
                    dispose();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to update faculty!");
                }
            }

        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create login credentials for faculty
     * 
     * @param facultyId Generated faculty ID
     * @return Array with [username, password] or null if failed
     */
    private String[] createLoginCredentials(int facultyId) {
        try {
            // Generate username and password
            String username = "faculty" + facultyId;
            String plainPassword = "Faculty@" + facultyId + ValidationUtils.generateRandom4Digits();
            String hashedPassword = ValidationUtils.hashPassword(plainPassword);

            // Insert into users table
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'FACULTY')";

            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                    java.sql.PreparedStatement pstmt = conn.prepareStatement(sql,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Get generated user ID
                    java.sql.ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        // Update faculty record with user_id
                        String updateSql = "UPDATE faculty SET user_id=? WHERE id=?";
                        try (java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, userId);
                            updateStmt.setInt(2, facultyId);
                            updateStmt.executeUpdate();
                        }

                        return new String[] { username, plainPassword };
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Show credentials dialog to admin
     */
    private void showCredentialsDialog(String username, String password) {
        JDialog credDialog = new JDialog((Frame) getParent(), "Faculty Login Credentials", true);
        credDialog.setSize(450, 250);
        credDialog.setLocationRelativeTo(this);
        credDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Login credentials created!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(UIHelper.SUCCESS_COLOR);

        contentPanel.add(new JLabel(""));
        contentPanel.add(titleLabel);

        contentPanel.add(UIHelper.createLabel("Username:"));
        JTextField usernameField = new JTextField(username);
        usernameField.setEditable(false);
        usernameField.setFont(new Font("Arial", Font.BOLD, 14));
        contentPanel.add(usernameField);

        contentPanel.add(UIHelper.createLabel("Password:"));
        JTextField passwordField = new JTextField(password);
        passwordField.setEditable(false);
        passwordField.setFont(new Font("Arial", Font.BOLD, 14));
        passwordField.setForeground(UIHelper.DANGER_COLOR);
        contentPanel.add(passwordField);

        JLabel noteLabel = new JLabel("<html><i>Note: Save these credentials. Faculty can login now.</i></html>");
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        contentPanel.add(new JLabel(""));
        contentPanel.add(noteLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        JButton okButton = UIHelper.createPrimaryButton("OK");
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.addActionListener(e -> credDialog.dispose());
        buttonPanel.add(okButton);

        credDialog.add(contentPanel, BorderLayout.CENTER);
        credDialog.add(buttonPanel, BorderLayout.SOUTH);
        credDialog.setVisible(true);
    }

    /**
     * Validate form inputs
     */
    private boolean validateInputs() {
        // Check required fields
        if (!ValidationUtils.isNotEmpty(nameField.getText())) {
            UIHelper.showErrorMessage(this, "Name is required!");
            nameField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            UIHelper.showErrorMessage(this, "Invalid email address!");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidPhone(phoneField.getText())) {
            UIHelper.showErrorMessage(this, "Phone number must be 10 digits!");
            phoneField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(departmentField.getText())) {
            UIHelper.showErrorMessage(this, "Department is required!");
            departmentField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(qualificationField.getText())) {
            UIHelper.showErrorMessage(this, "Qualification is required!");
            qualificationField.requestFocus();
            return false;
        }

        // Validate date format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(joinDateField.getText().trim());
        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid date format! Use yyyy-MM-dd");
            joinDateField.requestFocus();
            return false;
        }

        return true;
    }

    public boolean isSuccess() {
        return success;
    }
}
