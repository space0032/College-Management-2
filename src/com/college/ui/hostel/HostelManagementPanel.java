package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.Hostel;
import com.college.models.HostelAllocation;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Hostel Management Panel
 * Manages Hostels and Allocations
 */
public class HostelManagementPanel extends JPanel {

    private HostelDAO hostelDAO;
    private int userId;
    private String userRole;
    private JTabbedPane tabbedPane;

    // Allocations Components
    private JTable allocationsTable;
    private DefaultTableModel allocationsModel;

    // Hostels Components
    private JTable hostelsTable;
    private DefaultTableModel hostelsModel;

    public HostelManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.hostelDAO = new HostelDAO();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header (Optional, if we want a global header, otherwise tabs handle it)

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        // Tab 1: Hostels List
        tabbedPane.addTab("Hostels", createHostelsPanel());

        // Tab 2: Rooms
        tabbedPane.addTab("Rooms", new RoomManagementPanel(userRole));

        // Tab 3: Allocations
        tabbedPane.addTab("Allocations", createAllocationsPanel());

        // Tab 4: Wardens (Admin only)
        if (userRole.equals("ADMIN")) {
            tabbedPane.addTab("Wardens", new com.college.ui.admin.WardenManagementPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadData() {
        loadHostels();
        loadAllocations();
    }

    // ==================== HOSTELS TAB ====================

    private JPanel createHostelsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Hostel List");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadHostels());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(refreshButton);

        if (userRole.equals("ADMIN")) {
            JButton addButton = UIHelper.createSuccessButton("Add Hostel");
            addButton.addActionListener(e -> showAddHostelDialog());
            actionPanel.add(addButton);

            JButton deleteButton = UIHelper.createDangerButton("Delete");
            deleteButton.addActionListener(e -> deleteHostel());
            actionPanel.add(deleteButton);
        }

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Table
        String[] columns = { "ID", "Name", "Type", "Warden", "Contact", "Rooms", "Capacity", "Address" };
        hostelsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        hostelsTable = new JTable(hostelsModel);
        UIHelper.styleTable(hostelsTable);
        JScrollPane scrollPane = new JScrollPane(hostelsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadHostels() {
        hostelsModel.setRowCount(0);
        List<Hostel> hostels = hostelDAO.getAllHostels();
        for (Hostel h : hostels) {
            Object[] row = {
                    h.getId(),
                    h.getName(),
                    h.getType(),
                    h.getWardenName(),
                    h.getWardenContact(),
                    h.getTotalRooms(),
                    h.getTotalCapacity(),
                    h.getAddress()
            };
            hostelsModel.addRow(row);
        }
    }

    private void showAddHostelDialog() {
        AddHostelDialog dialog = new AddHostelDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadHostels();
        }
    }

    private void deleteHostel() {
        int selectedRow = hostelsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a hostel to delete.");
            return;
        }

        int id = (int) hostelsModel.getValueAt(selectedRow, 0);
        String name = (String) hostelsModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete hostel '" + name + "'?\nThis will remove related rooms and data!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (hostelDAO.deleteHostel(id)) {
                UIHelper.showSuccessMessage(this, "Hostel deleted successfully.");
                loadHostels();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete hostel.");
            }
        }
    }

    // ==================== ALLOCATIONS TAB ====================

    private JPanel createAllocationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Room Allocations");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAllocations());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(refreshButton);

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Table
        String[] columns = { "ID", "Student", "Hostel", "Room", "Check-In", "Check-Out", "Status" };
        allocationsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        allocationsTable = new JTable(allocationsModel);
        UIHelper.styleTable(allocationsTable);

        JScrollPane scrollPane = new JScrollPane(allocationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("WARDEN")) {
            JButton allocateButton = UIHelper.createSuccessButton("Allocate Room");
            allocateButton.setPreferredSize(new Dimension(150, 40));
            allocateButton.addActionListener(e -> allocateRoom());

            JButton vacateButton = UIHelper.createDangerButton("Vacate Room");
            vacateButton.setPreferredSize(new Dimension(150, 40));
            vacateButton.addActionListener(e -> vacateRoom());

            buttonPanel.add(allocateButton);
            buttonPanel.add(vacateButton);
        }

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAllocations() {
        allocationsModel.setRowCount(0);
        List<HostelAllocation> allocations;

        if (userRole.equals("STUDENT")) {
            allocations = hostelDAO.getAllocationsByStudent(userId);
        } else {
            allocations = hostelDAO.getAllActiveAllocations();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        for (HostelAllocation allocation : allocations) {
            String checkOut = allocation.getCheckOutDate() != null ? sdf.format(allocation.getCheckOutDate()) : "-";

            Object[] row = {
                    allocation.getId(),
                    allocation.getStudentName(),
                    allocation.getHostelName(),
                    allocation.getRoomNumber(),
                    sdf.format(allocation.getCheckInDate()),
                    checkOut,
                    allocation.getStatus()
            };
            allocationsModel.addRow(row);
        }
    }

    private void allocateRoom() {
        AllocateRoomDialog dialog = new AllocateRoomDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), userId);
        dialog.setVisible(true);
        loadAllocations(); // Refresh
    }

    private void vacateRoom() {
        int selectedRow = allocationsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a room allocation to vacate!");
            return;
        }

        int allocationId = (Integer) allocationsModel.getValueAt(selectedRow, 0);
        String studentName = (String) allocationsModel.getValueAt(selectedRow, 1);
        String roomNumber = (String) allocationsModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Vacate room for " + studentName + "?\n\n" +
                        "Room: " + roomNumber,
                "Confirm Vacate",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (hostelDAO.vacateRoom(allocationId)) {
                UIHelper.showSuccessMessage(this, "Room vacated successfully!");
                loadAllocations();
            } else {
                UIHelper.showErrorMessage(this, "Failed to vacate room!");
            }
        }
    }
}
