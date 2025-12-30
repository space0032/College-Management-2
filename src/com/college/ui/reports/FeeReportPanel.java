package com.college.ui.reports;

import com.college.dao.EnhancedFeeDAO;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Fee Report Panel
 * Generate fee collection and outstanding fees reports
 */
public class FeeReportPanel extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private EnhancedFeeDAO feeDAO;

    public FeeReportPanel() {
        feeDAO = new EnhancedFeeDAO();
        initComponents();
        generateReport();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        JPanel tablePanel = createTablePanel();
        JPanel summaryPanel = createSummaryPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> generateReport());
        panel.add(refreshButton);

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportReport());
        panel.add(exportButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = { "Student ID", "Student Name", "Category", "Amount", "Paid", "Balance", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));

        summaryLabel = new JLabel("Loading...");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(summaryLabel);

        return panel;
    }

    private void generateReport() {
        tableModel.setRowCount(0);

        List<StudentFee> allFees = feeDAO.getPendingFees();

        double totalAmount = 0;
        double totalPaid = 0;
        double totalPending = 0;
        int paidCount = 0;
        int pendingCount = 0;
        int partialCount = 0;

        for (StudentFee fee : allFees) {
            double balance = fee.getTotalAmount() - fee.getPaidAmount();

            Object[] row = {
                    fee.getStudentId(),
                    fee.getStudentName(),
                    fee.getCategoryName(),
                    String.format("Rs. %.2f", fee.getTotalAmount()),
                    String.format("Rs. %.2f", fee.getPaidAmount()),
                    String.format("Rs. %.2f", balance),
                    getStatusIcon(fee.getStatus())
            };
            tableModel.addRow(row);

            totalAmount += fee.getTotalAmount();
            totalPaid += fee.getPaidAmount();
            totalPending += balance;

            switch (fee.getStatus()) {
                case "PAID":
                    paidCount++;
                    break;
                case "PENDING":
                    pendingCount++;
                    break;
                case "PARTIAL":
                    partialCount++;
                    break;
            }
        }

        updateSummary(totalAmount, totalPaid, totalPending, paidCount, pendingCount, partialCount);
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "PAID":
                return "✅ PAID";
            case "PARTIAL":
                return "⚠️ PARTIAL";
            case "PENDING":
                return "❌ PENDING";
            default:
                return status;
        }
    }

    private void updateSummary(double total, double paid, double pending, int paidCnt, int pendingCnt, int partialCnt) {
        summaryLabel.setText(String.format(
                "Total: Rs. %.2f | Collected: Rs. %.2f | Outstanding: Rs. %.2f | Paid: %d | Partial: %d | Pending: %d",
                total, paid, pending, paidCnt, partialCnt, pendingCnt));
    }

    private void exportReport() {
        com.college.utils.TableExporter.showExportDialog(this, reportTable, "fee_report");
    }
}
