package com.college.ui.security;

import com.college.utils.DatabaseConnection;
import com.college.utils.SessionManager;
import com.college.utils.UIHelper;
import com.college.dao.AuditLogDAO;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Change Password Dialog
 * Allows users to change their password with validation
 */
public class ChangePasswordDialog extends JDialog {

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel strengthLabel;

    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Password", true);
        initComponents();
    }

    private void initComponents() {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(UIHelper.PRIMARY_COLOR);
        titlePanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Current Password
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        currentPasswordField = createPasswordField();
        formPanel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("New Password:"), gbc);

        gbc.gridx = 1;
        newPasswordField = createPasswordField();
        newPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePasswordStrength();
            }
        });
        formPanel.add(newPasswordField, gbc);

        // Password Strength Label
        gbc.gridx = 1;
        gbc.gridy = 2;
        strengthLabel = new JLabel("");
        strengthLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        formPanel.add(strengthLabel, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        confirmPasswordField = createPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        // Password Requirements
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JLabel requirementsLabel = new JLabel("<html><small>Requirements: Min 8 characters, " +
                "1 uppercase, 1 lowercase, 1 number</small></html>");
        requirementsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        requirementsLabel.setForeground(new Color(127, 140, 141));
        formPanel.add(requirementsLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton changeButton = UIHelper.createPrimaryButton("Change Password");
        changeButton.setPreferredSize(new Dimension(150, 35));
        changeButton.addActionListener(e -> handleChangePassword());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        // Add panels
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        getRootPane().setDefaultButton(changeButton);
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private void updatePasswordStrength() {
        String password = new String(newPasswordField.getPassword());
        int strength = calculatePasswordStrength(password);

        if (password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }

        if (strength >= 4) {
            strengthLabel.setText("Strong");
            strengthLabel.setForeground(new Color(46, 204, 113));
        } else if (strength >= 3) {
            strengthLabel.setText("Medium");
            strengthLabel.setForeground(new Color(230, 126, 34));
        } else {
            strengthLabel.setText("Weak");
            strengthLabel.setForeground(new Color(231, 76, 60));
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8)
            strength++;
        if (password.length() >= 12)
            strength++;
        if (password.matches(".*[A-Z].*"))
            strength++;
        if (password.matches(".*[a-z].*"))
            strength++;
        if (password.matches(".*\\d.*"))
            strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            strength++;

        return strength;
    }

    private void handleChangePassword() {
        SessionManager session = SessionManager.getInstance();

        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            UIHelper.showErrorMessage(this, "All fields are required!");
            return;
        }

        // Verify current password
        if (!verifyCurrentPassword(session.getUserId(), currentPassword)) {
            UIHelper.showErrorMessage(this, "Current password is incorrect!");
            currentPasswordField.setText("");
            currentPasswordField.requestFocus();
            return;
        }

        // Check if new password meets requirements
        if (!isPasswordValid(newPassword)) {
            UIHelper.showErrorMessage(this,
                    "Password must be at least 8 characters long and contain:\n" +
                            "- At least one uppercase letter\n" +
                            "- At least one lowercase letter\n" +
                            "- At least one number");
            return;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            UIHelper.showErrorMessage(this, "New passwords do not match!");
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        // Check if new password is different from current
        if (currentPassword.equals(newPassword)) {
            UIHelper.showErrorMessage(this, "New password must be different from current password!");
            return;
        }

        // Update password
        if (updatePassword(session.getUserId(), newPassword)) {
            // Log the action
            AuditLogDAO.logAction(session.getUserId(), session.getUsername(),
                    "CHANGE_PASSWORD", "USER", session.getUserId(),
                    "User changed their password");

            UIHelper.showSuccessMessage(this, "Password changed successfully!");
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to change password. Please try again.");
        }
    }

    private boolean verifyCurrentPassword(int userId, String password) {
        String sql = "SELECT id FROM users WHERE id = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, hashPassword(password));

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashPassword(newPassword));
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            if (Character.isLowerCase(c))
                hasLower = true;
            if (Character.isDigit(c))
                hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
