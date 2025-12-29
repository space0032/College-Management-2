package com.college.ui.fees;

import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Fee Management Panel
 * Manages student fees and payments
 */
public class FeeManagementPanel extends JPanel {

    private JTable feeTable;
    private DefaultTableModel tableModel;

    public FeeManagementPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Fee Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        topPanel.add(titleLabel, BorderLayout.WEST);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        infoPanel.add(createInfoCard("Total Fees", "₹0", UIHelper.PRIMARY_COLOR));
        infoPanel.add(createInfoCard("Collected", "₹0", UIHelper.SUCCESS_COLOR));
        infoPanel.add(createInfoCard("Pending", "₹0", UIHelper.DANGER_COLOR));

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Combine panels
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createInfoCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        String[] columns = { "Student ID", "Student Name", "Total Amount", "Paid Amount", "Pending", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feeTable = new JTable(tableModel);
        UIHelper.styleTable(feeTable);

        // Add sample data
        tableModel.addRow(new Object[] { "1", "Sample Student", "₹50000", "₹50000", "₹0", "PAID" });
        tableModel.addRow(new Object[] { "2", "Demo Student", "₹50000", "₹25000", "₹25000", "PARTIAL" });

        JScrollPane scrollPane = new JScrollPane(feeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}
