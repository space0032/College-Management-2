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
    private com.college.dao.StudentDAO studentDAO; // Added StudentDAO
    private int userId;
    private String userRole;

    private JTable feeTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel, collectedLabel, pendingLabel;

    public EnhancedFeeManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();
        this.studentDAO = new com.college.dao.StudentDAO(); // Initialize StudentDAO

        initComponents();
        if (role.equals("STUDENT")) {
            // FIX: Resolve Student ID from User ID
            com.college.models.Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                loadStudentFees(student.getId()); // Use resolved Student ID
            } else {
                UIHelper.showErrorMessage(this, "Student record not found associated with this user!");
            }
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

        // Updated Columns: Added "Enrollment ID"
        String[] columns = { "Ref #", "Enrollment ID", "Student Name", "Category", "Academic Year", "Total", "Paid",
                "Balance", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feeTable = new JTable(tableModel);
        UIHelper.styleTable(feeTable);

        // Adjust widths
        feeTable.getColumnModel().getColumn(0).setPreferredWidth(60); // Ref #
        feeTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Enrollment ID
        feeTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Name

        JScrollPane scrollPane = new JScrollPane(feeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JButton assignFeesButton = UIHelper.createPrimaryButton("Assign Fees");
            assignFeesButton.setPreferredSize(new Dimension(140, 40));
            assignFeesButton.addActionListener(e -> assignFees());
            panel.add(assignFeesButton);

            JButton viewPaymentsButton = UIHelper.createPrimaryButton("View Payments");
            viewPaymentsButton.setPreferredSize(new Dimension(160, 40));
            viewPaymentsButton.addActionListener(e -> viewPayments());
            panel.add(viewPaymentsButton);

            JButton recordPaymentButton = UIHelper.createSuccessButton("Record Payment");
            recordPaymentButton.setPreferredSize(new Dimension(160, 40));
            recordPaymentButton.addActionListener(e -> recordPayment());
            panel.add(recordPaymentButton);
        } else if (userRole.equals("STUDENT")) {
            // Students can also view their payment history
            JButton viewPaymentsButton = UIHelper.createPrimaryButton("View Payments");
            viewPaymentsButton.setPreferredSize(new Dimension(160, 40));
            viewPaymentsButton.addActionListener(e -> viewPayments());
            panel.add(viewPaymentsButton);
        }

        return panel;
    }

    private void assignFees() {
        AssignFeesDialog dialog = new AssignFeesDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        refreshData();
    }

    private void loadStudentFees(int studentId) {
        tableModel.setRowCount(0);
        List<StudentFee> fees = feeDAO.getStudentFees(studentId);

        double total = 0, paid = 0;

        for (StudentFee fee : fees) {
            Object[] row = {
                    fee.getId(),
                    fee.getStudentUsername() != null ? fee.getStudentUsername() : "-", // Enrollment ID
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
                    fee.getStudentUsername() != null ? fee.getStudentUsername() : "-", // Enrollment ID
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
            // Empty row placeholder
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

    private void viewPayments() {
        int selectedRow = feeTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a fee to view payments!");
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        if (!(idObj instanceof Integer)) {
            UIHelper.showErrorMessage(this, "Invalid fee selected!");
            return;
        }

        int feeId = (Integer) idObj;

        // Get the StudentFee object
        // Resolve studentId again for efficiency or just load all relevant fees
        List<StudentFee> fees;
        if (userRole.equals("STUDENT")) {
            com.college.models.Student student = studentDAO.getStudentByUserId(userId);
            fees = (student != null) ? feeDAO.getStudentFees(student.getId()) : new java.util.ArrayList<>();
        } else {
            fees = feeDAO.getPendingFees();
        }

        StudentFee selectedFee = null;
        for (StudentFee fee : fees) {
            if (fee.getId() == feeId) {
                selectedFee = fee;
                break;
            }
        }

        if (selectedFee != null) {
            PaymentHistoryDialog dialog = new PaymentHistoryDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    selectedFee);
            dialog.setVisible(true);
        }
    }

    private void refreshData() {
        if (userRole.equals("STUDENT")) {
            com.college.models.Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                loadStudentFees(student.getId());
            }
        } else {
            loadAllPendingFees();
        }
    }
}
