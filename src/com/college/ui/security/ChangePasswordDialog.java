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
        setSize(500, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Current Password
        contentPanel.add(createLabel("Current Password"));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        currentPasswordField = createStyledPasswordField();
        contentPanel.add(currentPasswordField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // New Password
        contentPanel.add(createLabel("New Password"));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        newPasswordField = createStyledPasswordField();
        newPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePasswordStrength();
            }
        });
        contentPanel.add(newPasswordField);

        // Strength Indicator
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        strengthLabel = new JLabel(" ");
        strengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        contentPanel.add(strengthLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Confirm Password
        contentPanel.add(createLabel("Confirm New Password"));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        confirmPasswordField = createStyledPasswordField();
        contentPanel.add(confirmPasswordField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Requirements Note
        JLabel noteLabel = new JLabel("<html><body style='width: 350px; color: #7f8c8d; font-size: 10px;'>" +
                "Password must be at least 8 chars with uppercase, lowercase & number</body></html>");
        contentPanel.add(noteLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Update Password");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(160, 38));
        saveButton.setBackground(UIHelper.PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> handleChangePassword());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(new Color(50, 50, 50));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(400, 40));
        field.setMaximumSize(new Dimension(400, 40));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private void updatePasswordStrength() {
        String password = new String(newPasswordField.getPassword());
        int strength = calculatePasswordStrength(password);

        if (password.isEmpty()) {
            strengthLabel.setText(" ");
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
