package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.Hostel;
import com.college.utils.UIHelper;
import com.college.utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to Add New Hostel
 */
public class AddHostelDialog extends JDialog {

    private HostelDAO hostelDAO;
    private boolean success = false;

    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField wardenNameField;
    private JTextField wardenContactField;
    private JTextField addressField;
    private JTextField capacityField;
    private JTextField roomsField;

    public AddHostelDialog(Frame parent) {
        super(parent, "Add New Hostel", true);
        this.hostelDAO = new HostelDAO();
        initComponents();
    }

    private void initComponents() {
        setSize(450, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Add New Hostel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Hostel Name:"), gbc);
        gbc.gridx = 1;
        nameField = UIHelper.createTextField(20);
        formPanel.add(nameField, gbc);

        // Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[] { "BOYS", "GIRLS", "COED" });
        formPanel.add(typeCombo, gbc);

        // Warden Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Warden Name:"), gbc);
        gbc.gridx = 1;
        wardenNameField = UIHelper.createTextField(20);
        formPanel.add(wardenNameField, gbc);

        // Warden Contact
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Warden Contact:"), gbc);
        gbc.gridx = 1;
        wardenContactField = UIHelper.createTextField(20);
        formPanel.add(wardenContactField, gbc);

        // Total Rooms
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(UIHelper.createLabel("Total Rooms:"), gbc);
        gbc.gridx = 1;
        roomsField = UIHelper.createTextField(20);
        formPanel.add(roomsField, gbc);

        // Capacity
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(UIHelper.createLabel("Total Capacity:"), gbc);
        gbc.gridx = 1;
        capacityField = UIHelper.createTextField(20);
        formPanel.add(capacityField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(UIHelper.createLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = UIHelper.createTextField(20);
        formPanel.add(addressField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = UIHelper.createSuccessButton("Save");
        saveButton.addActionListener(e -> saveHostel());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveHostel() {
        if (!validateInputs())
            return;

        Hostel hostel = new Hostel();
        hostel.setName(nameField.getText().trim());
        hostel.setType((String) typeCombo.getSelectedItem());
        hostel.setWardenName(wardenNameField.getText().trim());
        hostel.setWardenContact(wardenContactField.getText().trim());
        hostel.setAddress(addressField.getText().trim());

        try {
            hostel.setTotalRooms(Integer.parseInt(roomsField.getText().trim()));
            hostel.setTotalCapacity(Integer.parseInt(capacityField.getText().trim()));
        } catch (NumberFormatException e) {
            UIHelper.showErrorMessage(this, "Rooms and Capacity must be numbers.");
            return;
        }

        if (hostelDAO.addHostel(hostel)) {
            UIHelper.showSuccessMessage(this, "Hostel added successfully!");
            success = true;
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to add hostel.");
        }
    }

    private boolean validateInputs() {
        if (!ValidationUtils.isNotEmpty(nameField.getText())) {
            UIHelper.showErrorMessage(this, "Hostel Name is required.");
            return false;
        }
        return true;
    }

    public boolean isSuccess() {
        return success;
    }
}
