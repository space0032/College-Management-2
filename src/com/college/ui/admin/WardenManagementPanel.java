package com.college.ui.admin;

import com.college.dao.WardenDAO;
import com.college.models.Warden;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Warden Management Panel for Admin
 */
public class WardenManagementPanel extends JPanel {

    private WardenDAO wardenDAO;
    private JTable wardenTable;
    private DefaultTableModel tableModel;

    public WardenManagementPanel() {
        this.wardenDAO = new WardenDAO();
        initComponents();
        loadWardens();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Warden Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton addButton = UIHelper.createSuccessButton("Add New Warden");
        addButton.addActionListener(e -> showAddWardenDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        // Table
        String[] columns = { "ID", "Name", "Email", "Phone", "Assigned Hostel" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        wardenTable = new JTable(tableModel);
        UIHelper.styleTable(wardenTable);
        JScrollPane scrollPane = new JScrollPane(wardenTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton editButton = UIHelper.createPrimaryButton("Edit");
        editButton.addActionListener(e -> editWarden());

        JButton deleteButton = UIHelper.createDangerButton("Delete");
        deleteButton.addActionListener(e -> deleteWarden());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshButton.setBackground(Color.WHITE);
        refreshButton.addActionListener(e -> loadWardens());

        actionPanel.add(refreshButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadWardens() {
        tableModel.setRowCount(0);
        List<Warden> wardens = wardenDAO.getAllWardens();
        for (Warden w : wardens) {
            Object[] row = {
                    w.getId(),
                    w.getName(),
                    w.getEmail(),
                    w.getPhone(),
                    w.getHostelName() != null ? w.getHostelName() : "Unassigned"
            };
            tableModel.addRow(row);
        }
    }

    private void showAddWardenDialog() {
        AddWardenDialog dialog = new AddWardenDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadWardens();
        }
    }

    private void editWarden() {
        int selectedRow = wardenTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a warden to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Warden warden = wardenDAO.getWardenById(id);
        if (warden != null) {
            AddWardenDialog dialog = new AddWardenDialog((Frame) SwingUtilities.getWindowAncestor(this), warden);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadWardens();
            }
        }
    }

    private void deleteWarden() {
        int selectedRow = wardenTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a warden to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete warden '" + name + "'?\nThis will also remove their user account.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (wardenDAO.deleteWarden(id)) {
                UIHelper.showSuccessMessage(this, "Warden deleted successfully.");
                loadWardens();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete warden.");
            }
        }
    }
}
