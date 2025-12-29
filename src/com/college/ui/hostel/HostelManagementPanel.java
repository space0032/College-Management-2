package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.HostelAllocation;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Hostel Management Panel
 */
public class HostelManagementPanel extends JPanel {

    private HostelDAO hostelDAO;
    private int userId;
    private String userRole;

    private JTable allocationsTable;
    private DefaultTableModel tableModel;

    public HostelManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.hostelDAO = new HostelDAO();

        initComponents();
        loadAllocations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Hostel Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadAllocations());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Student", "Hostel", "Room", "Check-In", "Check-Out", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        allocationsTable = new JTable(tableModel);
        UIHelper.styleTable(allocationsTable);

        JScrollPane scrollPane = new JScrollPane(allocationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN")) {
            JButton allocateButton = UIHelper.createSuccessButton("Allocate Room");
            allocateButton.setPreferredSize(new Dimension(150, 40));
            allocateButton.addActionListener(e -> allocateRoom());

            JButton vacateButton = UIHelper.createDangerButton("Vacate Room");
            vacateButton.setPreferredSize(new Dimension(150, 40));
            vacateButton.addActionListener(e -> vacateRoom());

            panel.add(allocateButton);
            panel.add(vacateButton);
        }

        return panel;
    }

    private void loadAllocations() {
        tableModel.setRowCount(0);
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
            tableModel.addRow(row);
        }

        if (allocations.isEmpty()) {
            Object[] row = { "", "No allocations found", "", "", "", "", "" };
            tableModel.addRow(row);
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

        int allocationId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        String roomNumber = (String) tableModel.getValueAt(selectedRow, 3);

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
