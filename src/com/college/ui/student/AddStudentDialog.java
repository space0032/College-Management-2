package com.college.ui.student;

import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.UIHelper;
import com.college.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Add/Edit Student Dialog
 * Modal dialog for adding new students or editing existing ones
 */
public class AddStudentDialog extends JDialog {

    private Student student;
    private StudentDAO studentDAO;
    private boolean success = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField courseField;
    private JTextField batchField;
    private JTextField enrollmentDateField;
    private JTextArea addressArea;

    public AddStudentDialog(Frame parent, Student student) {
        super(parent, student == null ? "Add Student" : "Edit Student", true);
        this.student = student;
        this.studentDAO = new StudentDAO();
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

        // Course
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Course:"), gbc);
        gbc.gridx = 1;
        courseField = UIHelper.createTextField(20);
        formPanel.add(courseField, gbc);

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

        // Address
        gbc.gridx = 0;
        gbc.gridy = 6;
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
        courseField.setText(student.getCourse());
        batchField.setText(student.getBatch());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        enrollmentDateField.setText(sdf.format(student.getEnrollmentDate()));
        addressArea.setText(student.getAddress());
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
            if (student == null) {
                student = new Student();
            }

            student.setName(nameField.getText().trim());
            student.setEmail(emailField.getText().trim());
            student.setPhone(phoneField.getText().trim());
            student.setCourse(courseField.getText().trim());
            student.setBatch(batchField.getText().trim());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            student.setEnrollmentDate(sdf.parse(enrollmentDateField.getText().trim()));
            student.setAddress(addressArea.getText().trim());

            // Save to database
            boolean result;
            if (student.getId() == 0) {
                result = studentDAO.addStudent(student);
            } else {
                result = studentDAO.updateStudent(student);
            }

            if (result) {
                UIHelper.showSuccessMessage(this, "Student saved successfully!");
                success = true;
                dispose();
            } else {
                UIHelper.showErrorMessage(this, "Failed to save student!");
            }

        } catch (Exception e) {
            UIHelper.showErrorMessage(this, "Invalid data: " + e.getMessage());
            e.printStackTrace();
        }
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

        if (!ValidationUtils.isNotEmpty(courseField.getText())) {
            UIHelper.showErrorMessage(this, "Course is required!");
            courseField.requestFocus();
            return false;
        }

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
}
