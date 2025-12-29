package com.college.ui.fees;

import com.college.dao.EnhancedFeeDAO;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Enhanced Fee Management Panel
 */
public class EnhancedFeeManagementPanel extends JPanel {

    private EnhancedFeeDAO feeDAO;
    private int userId;
    private String userRole;

    private JTable feeTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, collectedLabel, pendingLabel;

    public EnhancedFeeManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();

        initComponents();
        if (role.equals("STUDENT")) {
            loadStudentFees(userId);
        } else {
            loadAllPendingFees();
        }
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

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        totalLabel = new JLabel("₹0");
        collectedLabel = new JLabel("₹0");
        pendingLabel = new JLabel("₹0");

        infoPanel.add(createInfoCard("Total Fees", totalLabel, UIHelper.PRIMARY_COLOR));
        infoPanel.add(createInfoCard("Collected", collectedLabel, UIHelper.SUCCESS_COLOR));
        infoPanel.add(createInfoCard("Pending", pendingLabel, UIHelper.DANGER_COLOR));

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        // Combine panels
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        String[] columns = { "ID", "Student", "Category", "Academic Year", "Total", "Paid", "Balance", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feeTable = new JTable(tableModel);
        UIHelper.styleTable(feeTable);

        JScrollPane scrollPane = new JScrollPane(feeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JButton recordPaymentButton = UIHelper.createSuccessButton("Record Payment");
            recordPaymentButton.setPreferredSize(new Dimension(160, 40));
            recordPaymentButton.addActionListener(e -> recordPayment());
            panel.add(recordPaymentButton);
        }

        return panel;
    }

    private void loadStudentFees(int studentId) {
        tableModel.setRowCount(0);
        List<StudentFee> fees = feeDAO.getStudentFees(studentId);

        double total = 0, paid = 0;

        for (StudentFee fee : fees) {
            Object[] row = {
                    fee.getId(),
                    fee.getStudentName(),
                    fee.getCategoryName(),
                    fee.getAcademicYear(),
                    String.format("%.2f", fee.getTotalAmount()),
                    String.format("%.2f", fee.getPaidAmount()),
                    String.format("%.2f", fee.getBalanceAmount()),
                    fee.getStatus()
            };
            tableModel.addRow(row);

            total += fee.getTotalAmount();
            paid += fee.getPaidAmount();
        }

        updateSummary(total, paid);
    }

    private void loadAllPendingFees() {
        tableModel.setRowCount(0);
        List<StudentFee> fees = feeDAO.getPendingFees();

        double total = 0, paid = 0;

        for (StudentFee fee : fees) {
            Object[] row = {
                    fee.getId(),
                    fee.getStudentName(),
                    fee.getCategoryName(),
                    fee.getAcademicYear(),
                    String.format("%.2f", fee.getTotalAmount()),
                    String.format("%.2f", fee.getPaidAmount()),
                    String.format("%.2f", fee.getBalanceAmount()),
                    fee.getStatus()
            };
            tableModel.addRow(row);

            total += fee.getTotalAmount();
            paid += fee.getPaidAmount();
        }

        updateSummary(total, paid);

        if (fees.isEmpty()) {
            tableModel.addRow(new Object[] { "", "No pending fees", "", "", "", "", "", "" });
        }
    }

    private void updateSummary(double total, double paid) {
        totalLabel.setText("₹" + String.format("%.2f", total));
        collectedLabel.setText("₹" + String.format("%.2f", paid));
        pendingLabel.setText("₹" + String.format("%.2f", total - paid));
    }

    private void recordPayment() {
        int selectedRow = feeTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a fee to record payment!");
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        if (!(idObj instanceof Integer)) {
            UIHelper.showErrorMessage(this, "Invalid fee selected!");
            return;
        }

        int feeId = (Integer) idObj;

        // Get the full StudentFee object
        List<StudentFee> fees = feeDAO.getPendingFees();
        StudentFee selectedFee = null;
        for (StudentFee fee : fees) {
            if (fee.getId() == feeId) {
                selectedFee = fee;
                break;
            }
        }

        if (selectedFee != null) {
            RecordPaymentDialog dialog = new RecordPaymentDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    selectedFee,
                    userId);
            dialog.setVisible(true);
            refreshData();
        }
    }

    private void refreshData() {
        if (userRole.equals("STUDENT")) {
            loadStudentFees(userId);
        } else {
            loadAllPendingFees();
        }
    }
}
