package com.college.ui.admin;

import com.college.dao.WardenDAO;
import com.college.dao.HostelDAO;
import com.college.models.Warden;
import com.college.models.Hostel;
import com.college.utils.DatabaseConnection;
import com.college.utils.UIHelper;
import com.college.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog to Add/Edit Warden
 */
public class AddWardenDialog extends JDialog {

    private Warden warden;
    private WardenDAO wardenDAO;
    private boolean success = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<HostelItem> hostelCombo;

    public AddWardenDialog(Frame parent, Warden warden) {
        super(parent, warden == null ? "Add Warden" : "Edit Warden", true);
        this.warden = warden;
        this.wardenDAO = new WardenDAO();
        initComponents();

        if (warden != null) {
            fillData();
        }
    }

    private void initComponents() {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(warden == null ? "Add New Warden" : "Edit Warden");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        // Hostel (Optional)
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Assign Hostel:"), gbc);
        gbc.gridx = 1;

        HostelDAO hostelDAO = new HostelDAO();
        List<Hostel> hostels = hostelDAO.getAllHostels();
        hostelCombo = new JComboBox<>();
        hostelCombo.addItem(new HostelItem(null)); // None option
        for (Hostel hostel : hostels) {
            hostelCombo.addItem(new HostelItem(hostel));
        }
        formPanel.add(hostelCombo, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = UIHelper.createSuccessButton("Save");
        saveButton.addActionListener(e -> saveWarden());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillData() {
        nameField.setText(warden.getName());
        emailField.setText(warden.getEmail());
        phoneField.setText(warden.getPhone());

        if (warden.getHostelId() > 0) {
            for (int i = 0; i < hostelCombo.getItemCount(); i++) {
                HostelItem item = hostelCombo.getItemAt(i);
                if (item.getHostel() != null && item.getHostel().getId() == warden.getHostelId()) {
                    hostelCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveWarden() {
        if (!validateInputs())
            return;

        boolean isNew = (warden == null);
        if (isNew) {
            warden = new Warden();
        }

        warden.setName(nameField.getText().trim());
        warden.setEmail(emailField.getText().trim());
        warden.setPhone(phoneField.getText().trim());

        HostelItem selectedHostel = (HostelItem) hostelCombo.getSelectedItem();
        if (selectedHostel != null && selectedHostel.getHostel() != null) {
            warden.setHostelId(selectedHostel.getHostel().getId());
        } else {
            warden.setHostelId(0);
        }

        try {
            if (isNew) {
                int wardenId = wardenDAO.addWarden(warden);
                if (wardenId > 0) {
                    // Create Login Credentials
                    String[] creds = createLoginCredentials(wardenId);
                    if (creds != null) {
                        showCredentialsDialog(creds[0], creds[1]);
                        success = true;
                        dispose();
                    } else {
                        UIHelper.showErrorMessage(this, "Warden added but failed to generate credentials.");
                    }
                } else {
                    UIHelper.showErrorMessage(this, "Failed to add warden.");
                }
            } else {
                if (wardenDAO.updateWarden(warden)) {
                    UIHelper.showSuccessMessage(this, "Warden updated successfully!");
                    success = true;
                    dispose();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to update warden.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIHelper.showErrorMessage(this, "Error saving warden: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (!ValidationUtils.isNotEmpty(nameField.getText())) {
            UIHelper.showErrorMessage(this, "Name is required.");
            return false;
        }
        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            UIHelper.showErrorMessage(this, "Invalid email.");
            return false;
        }
        if (!ValidationUtils.isValidPhone(phoneField.getText())) {
            UIHelper.showErrorMessage(this, "Invalid phone number.");
            return false;
        }
        return true;
    }

    private String[] createLoginCredentials(int wardenId) {
        try {
            String username = "warden" + wardenId;
            String plainPassword = "Warden@" + wardenId + ValidationUtils.generateRandom4Digits();
            String hashedPassword = ValidationUtils.hashPassword(plainPassword);

            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'WARDEN')";

            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                    java.sql.PreparedStatement pstmt = conn.prepareStatement(sql,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);

                if (pstmt.executeUpdate() > 0) {
                    try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            // Link User ID to Warden
                            String updateSql = "UPDATE wardens SET user_id = ? WHERE id = ?";
                            try (java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, userId);
                                updateStmt.setInt(2, wardenId);
                                updateStmt.executeUpdate();
                            }
                            return new String[] { username, plainPassword };
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showCredentialsDialog(String username, String password) {
        JDialog dialog = new JDialog((Frame) getParent(), "Warden Login Credentials", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Warden Account Created!");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(UIHelper.SUCCESS_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField userField = new JTextField("Username: " + username);
        userField.setEditable(false);
        userField.setFont(new Font("Arial", Font.BOLD, 14));
        userField.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField passField = new JTextField("Password: " + password);
        passField.setEditable(false);
        passField.setFont(new Font("Arial", Font.BOLD, 14));
        passField.setForeground(UIHelper.DANGER_COLOR);
        passField.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = UIHelper.createPrimaryButton("OK");
        okButton.addActionListener(e -> dialog.dispose());

        panel.add(title);
        panel.add(userField);
        panel.add(passField);
        panel.add(okButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public boolean isSuccess() {
        return success;
    }

    // Helper for ComboBox
    private static class HostelItem {
        private Hostel hostel;

        public HostelItem(Hostel hostel) {
            this.hostel = hostel;
        }

        public Hostel getHostel() {
            return hostel;
        }

        @Override
        public String toString() {
            return hostel == null ? "-- None --" : hostel.getName();
        }
    }
}
