package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.Hostel;
import com.college.models.Room;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog to add a new room
 */
public class AddRoomDialog extends JDialog {

    private HostelDAO hostelDAO;
    private boolean success = false;

    private JComboBox<HostelItem> hostelCombo;
    private JTextField roomNumberField;
    private JSpinner floorSpinner;
    private JSpinner capacitySpinner;
    private JComboBox<String> typeCombo;

    public AddRoomDialog(Frame parent) {
        super(parent, "Add New Room", true);
        this.hostelDAO = new HostelDAO();
        initComponents();
        loadHostels();
    }

    private void initComponents() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Hostel
        formPanel.add(UIHelper.createLabel("Select Hostel:"), gbc);
        gbc.gridx = 1;
        hostelCombo = new JComboBox<>();
        formPanel.add(hostelCombo, gbc);

        // Room Number
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        roomNumberField = UIHelper.createTextField();
        formPanel.add(roomNumberField, gbc);

        // Floor
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Floor:"), gbc);
        gbc.gridx = 1;
        floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        formPanel.add(floorSpinner, gbc);

        // Capacity
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        formPanel.add(capacitySpinner, gbc);

        // Type
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Room Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[] { "NON_AC", "AC" });
        formPanel.add(typeCombo, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createSuccessButton("Save Room");
        saveButton.addActionListener(e -> saveRoom());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadHostels() {
        List<Hostel> hostels = hostelDAO.getAllHostels();
        for (Hostel hostel : hostels) {
            hostelCombo.addItem(new HostelItem(hostel));
        }
    }

    private void saveRoom() {
        HostelItem selectedHostel = (HostelItem) hostelCombo.getSelectedItem();
        String roomNumber = roomNumberField.getText().trim();

        if (selectedHostel == null || roomNumber.isEmpty()) {
            UIHelper.showErrorMessage(this, "Please select hostel and enter room number!");
            return;
        }

        Room room = new Room();
        room.setHostelId(selectedHostel.hostel.getId());
        room.setRoomNumber(roomNumber);
        room.setFloor((Integer) floorSpinner.getValue());
        room.setCapacity((Integer) capacitySpinner.getValue());
        room.setRoomType((String) typeCombo.getSelectedItem());

        if (hostelDAO.addRoom(room)) {
            success = true;
            UIHelper.showSuccessMessage(this, "Room added successfully!");
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to add room!");
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private static class HostelItem {
        Hostel hostel;

        HostelItem(Hostel h) {
            this.hostel = h;
        }

        @Override
        public String toString() {
            return hostel.getName();
        }
    }
}
