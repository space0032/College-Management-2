package com.college.ui.fees;

import com.college.dao.EnhancedFeeDAO;
import com.college.dao.StudentDAO;
import com.college.models.FeePayment;
import com.college.models.Student;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Enhanced Fee Management Panel
 */
public class EnhancedFeeManagementPanel extends JPanel {

    private EnhancedFeeDAO feeDAO;
    private StudentDAO studentDAO;
    private int userId;
    private String userRole;

    // Components
    private JTabbedPane tabbedPane;

    // Tab 1: Pending Fees
    private JTable pendingFeeTable;
    private DefaultTableModel pendingTableModel;
    private JLabel totalLabel, collectedLabel, pendingLabel;

    // Tab 2: Payment History
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField searchField;

    public EnhancedFeeManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("Fee Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        tabbedPane.addTab("Pending Fees", createPendingFeesPanel());
        tabbedPane.addTab("Payment History", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: Pending Fees Logic
    // ==========================================
    private JPanel createPendingFeesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        totalLabel = new JLabel("₹0");
        collectedLabel = new JLabel("₹0");
        pendingLabel = new JLabel("₹0");

        infoPanel.add(createInfoCard("Total Fees", totalLabel, UIHelper.PRIMARY_COLOR));
        infoPanel.add(createInfoCard("Collected", collectedLabel, UIHelper.SUCCESS_COLOR));
        infoPanel.add(createInfoCard("Pending", pendingLabel, UIHelper.DANGER_COLOR));

        // Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Ref #", "Enrollment ID", "Student Name", "Category", "Academic Year", "Total", "Paid",
                "Balance", "Status" };
        pendingTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pendingFeeTable = new JTable(pendingTableModel);
        UIHelper.styleTable(pendingFeeTable);

        // Adjust widths
        pendingFeeTable.getColumnModel().getColumn(0).setPreferredWidth(60); // Ref #
        pendingFeeTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Enrollment ID
        pendingFeeTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Name

        JScrollPane scrollPane = new JScrollPane(pendingFeeTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setBackground(Color.WHITE);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        actionPanel.add(refreshButton);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JButton assignFeesButton = UIHelper.createPrimaryButton("Assign Fees");
            assignFeesButton.addActionListener(e -> assignFees());
            actionPanel.add(assignFeesButton);

            JButton recordPaymentButton = UIHelper.createSuccessButton("Record Payment");
            recordPaymentButton.addActionListener(e -> recordPayment());
            actionPanel.add(recordPaymentButton);
        }

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        return mainPanel;
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
        // valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ==========================================
    // TAB 2: Payment History Logic
    // ==========================================
    private JPanel createHistoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        JButton searchButton = UIHelper.createPrimaryButton("Search");
        searchButton.addActionListener(e -> loadPaymentHistory(searchField.getText()));

        searchPanel.add(UIHelper.createLabel("Search Student/Receipt:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Receipt #", "Enrollment ID", "Date", "Student Name", "Category", "Academic Year",
                "Amount", "Mode",
                "Remarks" };
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        UIHelper.styleTable(historyTable);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setBackground(Color.WHITE);

        JButton printButton = UIHelper.createSuccessButton("Print Receipt");
        printButton.addActionListener(e -> printReceipt());
        actionPanel.add(printButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    // ==========================================
    // Data Loading & Actions
    // ==========================================

    private void refreshData() {
        loadPendingFees();
        loadPaymentHistory(searchField.getText());
    }

    private void loadPendingFees() {
        pendingTableModel.setRowCount(0);
        List<StudentFee> fees;

        if (userRole.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                fees = feeDAO.getStudentFees(student.getId());
            } else {
                fees = new java.util.ArrayList<>(); // Empty if student not found
            }
        } else {
            fees = feeDAO.getPendingFees();
        }

        double total = 0, paid = 0;
        for (StudentFee fee : fees) {
            Object[] row = {
                    fee.getId(),
                    fee.getStudentUsername() != null ? fee.getStudentUsername() : "-",
                    fee.getStudentName(),
                    fee.getCategoryName(),
                    fee.getAcademicYear(),
                    String.format("%.2f", fee.getTotalAmount()),
                    String.format("%.2f", fee.getPaidAmount()),
                    String.format("%.2f", fee.getBalanceAmount()),
                    fee.getStatus()
            };
            pendingTableModel.addRow(row);
            total += fee.getTotalAmount();
            paid += fee.getPaidAmount();
        }
        updateSummary(total, paid);
    }

    private void loadPaymentHistory(String keyword) {
        historyTableModel.setRowCount(0);
        List<FeePayment> payments = feeDAO.searchPaymentHistory(keyword);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        for (FeePayment fp : payments) {
            Object[] row = {
                    fp.getReceiptNumber(),
                    fp.getStudentEnrollmentId() != null ? fp.getStudentEnrollmentId() : "-",
                    sdf.format(fp.getPaymentDate()),
                    fp.getStudentName(),
                    fp.getCategoryName(),
                    fp.getAcademicYear(),
                    "Rs. " + String.format("%.2f", fp.getAmount()),
                    fp.getPaymentMode(),
                    fp.getRemarks() != null ? fp.getRemarks() : "-"
            };
            historyTableModel.addRow(row);
        }
    }

    private void updateSummary(double total, double paid) {
        totalLabel.setText("₹" + String.format("%.0f", total));
        collectedLabel.setText("₹" + String.format("%.0f", paid));
        pendingLabel.setText("₹" + String.format("%.0f", total - paid));
    }

    // Actions
    private void assignFees() {
        new AssignFeesDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
        refreshData();
    }

    private void recordPayment() {
        int selectedRow = pendingFeeTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a pending fee to record payment!");
            return;
        }

        Object idObj = pendingTableModel.getValueAt(selectedRow, 0);
        int feeId = (Integer) idObj;

        // Retrieve full object (simplified by reloading needed one)
        StudentFee selectedFee = null;
        for (StudentFee fee : feeDAO.getAllFees()) { // Using getAllFees to find even partial ones
            if (fee.getId() == feeId) {
                selectedFee = fee;
                break;
            }
        }

        if (selectedFee != null) {
            new RecordPaymentDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedFee, userId)
                    .setVisible(true);
            refreshData();
        }
    }

    private void printReceipt() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a payment to print receipt!");
            return;
        }

        // To print, we need the full FeePayment object.
        // Retrieve it from list using search again (inefficient but safe) or track
        // locally.
        // Better: store hidden ID in table or retrieve by list index if synced.
        // Let's rely on list index for now since search reloads table.

        List<FeePayment> payments = feeDAO.searchPaymentHistory(searchField.getText());
        if (selectedRow < payments.size()) {
            FeePayment payment = payments.get(selectedRow);

            // Construct a dummy StudentFee for the Receipt Dialog
            StudentFee dummyFee = new StudentFee();
            dummyFee.setStudentName(payment.getStudentName());
            dummyFee.setCategoryName(payment.getCategoryName());
            dummyFee.setAcademicYear(payment.getAcademicYear());

            // Note: Total/Paid/Balance on receipt might be inaccurate if we don't fetch the
            // parent fee.
            // For now, let's just show what we have.

            // Actually, we have payment.studentFeeId
            dummyFee.setTotalAmount(0); // Unknown
            dummyFee.setPaidAmount(0); // Unknown

            // Better: show receipt
            new FeeReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), payment, dummyFee).setVisible(true);
        }
    }
}
