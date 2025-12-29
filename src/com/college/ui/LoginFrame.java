package com.college.ui;

import com.college.utils.DatabaseConnection;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Login Frame - Authentication screen
 * Allows users to login as Admin, Faculty, or Student
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private String currentRole;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("College Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(UIHelper.BACKGROUND_COLOR);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(UIHelper.PRIMARY_COLOR);
        titlePanel.setPreferredSize(new Dimension(500, 80));

        JLabel titleLabel = new JLabel("College Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username Label and Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = UIHelper.createLabel("Username:");
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = UIHelper.createTextField(20);
        formPanel.add(usernameField, gbc);

        // Password Label and Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = UIHelper.createLabel("Password:");
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(passwordField, gbc);

        // Role Label and ComboBox
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel roleLabel = UIHelper.createLabel("Login As:");
        formPanel.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = { "ADMIN", "FACULTY", "STUDENT" };
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(roleComboBox, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(UIHelper.BACKGROUND_COLOR);

        JButton loginButton = UIHelper.createPrimaryButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(e -> handleLogin());

        JButton clearButton = UIHelper.createDangerButton("Clear");
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Allow Enter key to submit
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Handle login button click
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            UIHelper.showErrorMessage(this, "Please enter username and password!");
            return;
        }

        // Authenticate user
        if (authenticateUser(username, password, role)) {
            currentRole = role;
            UIHelper.showSuccessMessage(this, "Login successful!");

            // Open Dashboard
            SwingUtilities.invokeLater(() -> {
                DashboardFrame dashboard = new DashboardFrame(username, role);
                dashboard.setVisible(true);
            });

            // Close login window
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Invalid credentials or role!");
            clearFields();
        }
    }

    /**
     * Authenticate user against database
     * 
     * @param username Username
     * @param password Password
     * @param role     User role
     * @return true if authentication successful
     */
    private boolean authenticateUser(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hash password using SHA-256
     * 
     * @param password Plain text password
     * @return Hashed password
     */
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

    /**
     * Clear all input fields
     */
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
        usernameField.requestFocus();
    }
}
