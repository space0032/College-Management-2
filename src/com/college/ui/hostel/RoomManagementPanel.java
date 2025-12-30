package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.Room;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel to manage and view rooms
 */
public class RoomManagementPanel extends JPanel {

    private HostelDAO hostelDAO;
    private JTable roomsTable;
    private DefaultTableModel tableModel;
    private String userRole;

    public RoomManagementPanel(String userRole) {
        this.userRole = userRole;
        this.hostelDAO = new HostelDAO();
        initComponents();
        loadRooms();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Rooms Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRooms());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(refreshButton);

        if ("ADMIN".equals(userRole) || "WARDEN".equals(userRole)) {
            JButton addRoomButton = UIHelper.createSuccessButton("Add New Room");
            addRoomButton.addActionListener(e -> showAddRoomDialog());
            actionPanel.add(addRoomButton);

            JButton deleteRoomButton = UIHelper.createDangerButton("Delete Room");
            deleteRoomButton.addActionListener(e -> deleteRoom());
            actionPanel.add(Box.createHorizontalStrut(10));
            actionPanel.add(deleteRoomButton);
        }

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Table
        String[] columns = { "ID", "Hostel", "Room No", "Floor", "Type", "Capacity", "Occupied", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomsTable = new JTable(tableModel);
        UIHelper.styleTable(roomsTable);

        // Hide ID column
        roomsTable.getColumnModel().removeColumn(roomsTable.getColumnModel().getColumn(0));

        JScrollPane scrollPane = new JScrollPane(roomsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadRooms() {
        tableModel.setRowCount(0);
        List<Room> rooms = hostelDAO.getAllRooms();

        for (Room room : rooms) {
            Object[] row = {
                    room.getId(),
                    room.getHostelName(),
                    room.getRoomNumber(),
                    room.getFloor(),
                    room.getRoomType(),
                    room.getCapacity(),
                    room.getOccupiedCount(),
                    room.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddRoomDialog() {
        AddRoomDialog dialog = new AddRoomDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadRooms();
        }
    }

    private void deleteRoom() {
        int selectedRow = roomsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a room to delete.");
            return;
        }

        // Get ID from model (column 0) - need to convert view index to model index if
        // sorted,
        // but currently not sorting. However, standard JTable usage:
        int modelRow = roomsTable.convertRowIndexToModel(selectedRow);
        int roomId = (int) tableModel.getValueAt(modelRow, 0);
        String hostelName = (String) tableModel.getValueAt(modelRow, 1);
        String roomNumber = (String) tableModel.getValueAt(modelRow, 2);
        int occupied = (int) tableModel.getValueAt(modelRow, 6);

        if (occupied > 0) {
            UIHelper.showErrorMessage(this, "Cannot delete an occupied room!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Room " + roomNumber + " in " + hostelName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (hostelDAO.deleteRoom(roomId)) {
                UIHelper.showSuccessMessage(this, "Room deleted successfully!");
                loadRooms();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete room. It might be occupied.");
            }
        }
    }
}
