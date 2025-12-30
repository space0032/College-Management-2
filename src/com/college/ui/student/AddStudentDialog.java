package com.college.ui.student;

import com.college.dao.StudentDAO;
import com.college.dao.DepartmentDAO;
import com.college.models.Student;
import com.college.models.Department;
import com.college.utils.UIHelper;
import com.college.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Add/Edit Student Dialog
 * Modal dialog for adding new students or editing existing ones
 */
public class AddStudentDialog extends JDialog {

    private Student student;
    private StudentDAO studentDAO;
    private DepartmentDAO departmentDAO;
    private boolean success = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<DepartmentItem> departmentCombo;
    private JTextField batchField;
    private JTextField enrollmentDateField;
    private JTextArea addressArea;
    private JCheckBox hostelCheckBox;

    public AddStudentDialog(Frame parent, Student student) {
        super(parent, student == null ? "Add Student" : "Edit Student", true);
        this.student = student;
        this.studentDAO = new StudentDAO();
        this.departmentDAO = new DepartmentDAO();
        initComponents();

        if (student != null) {
            fillData();
        }
    }

    private void initComponents() {
        setSize(500, 600);
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

        // Load departments into dropdown
        departmentCombo = new JComboBox<>();
        List<Department> departments = departmentDAO.getAllDepartments();
        departmentCombo.addItem(new DepartmentItem(null)); // "None" option
        for (Department dept : departments) {
            departmentCombo.addItem(new DepartmentItem(dept));
        }
        formPanel.add(departmentCombo, gbc);

        // Batch
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(UIHelper.createLabel("Batch:"), gbc);
        gbc.gridx = 1;
        batchField = UIHelper.createTextField(20);
        formPanel.add(batchField, gbc);

        // Enrollment Date
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(UIHelper.createLabel("Enrollment Date:"), gbc);
        gbc.gridx = 1;
        enrollmentDateField = UIHelper.createTextField(20);
        enrollmentDateField.setToolTipText("Format: yyyy-MM-dd");
        // Set default to today's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        enrollmentDateField.setText(sdf.format(new Date()));
        formPanel.add(enrollmentDateField, gbc);

        // Hostel Required Checkbox
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(UIHelper.createLabel("Hostel Required:"), gbc);
        gbc.gridx = 1;
        JCheckBox hostelCheckBox = new JCheckBox("Yes");
        hostelCheckBox.setBackground(Color.WHITE);
        hostelCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(hostelCheckBox, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(UIHelper.createLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        addressArea = new JTextArea(4, 20);
        addressArea.setFont(new Font("Arial", Font.PLAIN, 14));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane scrollPane = new JScrollPane(addressArea);
        formPanel.add(scrollPane, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createSuccessButton("Save");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.addActionListener(e -> saveStudent());

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
     * Fill form with student data (for editing)
     */
    private void fillData() {
        nameField.setText(student.getName());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getPhone());

        // Set department dropdown selection
        if (student.getDepartment() != null && !student.getDepartment().isEmpty()) {
            for (int i = 0; i < departmentCombo.getItemCount(); i++) {
                DepartmentItem item = departmentCombo.getItemAt(i);
                if (item.getDepartment() != null &&
                        item.getDepartment().getName().equals(student.getDepartment())) {
                    departmentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        batchField.setText(student.getBatch());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        enrollmentDateField.setText(sdf.format(student.getEnrollmentDate()));
        addressArea.setText(student.getAddress());
        if (hostelCheckBox != null) {
            hostelCheckBox.setSelected(student.isHostelite());
        }
    }

    /**
     * Save student data
     */
    private void saveStudent() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Create or update student object
            boolean isNewStudent = (student == null);
            if (student == null) {
                student = new Student();
            }

            student.setName(nameField.getText().trim());
            student.setEmail(emailField.getText().trim());
            student.setPhone(phoneField.getText().trim());

            // Set department from dropdown
            DepartmentItem selectedDept = (DepartmentItem) departmentCombo.getSelectedItem();
            if (selectedDept != null && selectedDept.getDepartment() != null) {
                student.setDepartment(selectedDept.getDepartment().getName());
            } else {
                student.setDepartment("");
            }

            student.setBatch(batchField.getText().trim());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            student.setEnrollmentDate(sdf.parse(enrollmentDateField.getText().trim()));
            student.setEnrollmentDate(sdf.parse(enrollmentDateField.getText().trim()));
            student.setAddress(addressArea.getText().trim());
            student.setHostelite(hostelCheckBox.isSelected());

            // Save to database
            if (isNewStudent) {
                // Add new student and get generated ID
                int studentId = studentDAO.addStudent(student);

                if (studentId > 0) {
                    // Create login credentials
                    String[] credentials = createLoginCredentials(studentId);

                    if (credentials != null) {
                        // Show success and credentials dialog
                        showCredentialsDialog(credentials[0], credentials[1]);
                        success = true;
                        dispose();
                    } else {
                        UIHelper.showErrorMessage(this, "Student created but failed to create login credentials!");
                    }
                } else {
                    UIHelper.showErrorMessage(this, "Failed to save student!");
                }
            } else {
                // Update existing student
                boolean result = studentDAO.updateStudent(student);
                if (result) {
                    UIHelper.showSuccessMessage(this, "Student updated successfully!");
                    success = true;
                    dispose();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to update student!");
                }
            }

        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create login credentials for student
     * 
     * @param studentId Generated student ID
     * @return Array with [username, password] or null if failed
     */
    private String[] createLoginCredentials(int studentId) {
        try {
            // Generate username and password
            String username = "student" + studentId;
            String plainPassword = "Student@" + studentId + ValidationUtils.generateRandom4Digits();
            String hashedPassword = ValidationUtils.hashPassword(plainPassword);

            // Insert into users table
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'STUDENT')";

            try (java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
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

                        // Update student record with user_id
                        String updateSql = "UPDATE students SET user_id=? WHERE id=?";
                        try (java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, userId);
                            updateStmt.setInt(2, studentId);
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
        JDialog credDialog = new JDialog((Frame) getParent(), "Student Login Credentials", true);
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

        JLabel noteLabel = new JLabel("<html><i>Note: Save these credentials. Student can login now.</i></html>");
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

        // Department is optional now (can select "None")
        // No validation needed

        if (!ValidationUtils.isNotEmpty(batchField.getText())) {
            UIHelper.showErrorMessage(this, "Batch is required!");
            batchField.requestFocus();
            return false;
        }

        // Validate date format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(enrollmentDateField.getText().trim());
        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid date format! Use yyyy-MM-dd");
            enrollmentDateField.requestFocus();
            return false;
        }

        return true;
    }

    public boolean isSuccess() {
        return success;
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
            return department == null ? "-- None --" : department.getName() + " (" + department.getCode() + ")";
        }
    }
}
