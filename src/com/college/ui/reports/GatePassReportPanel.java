package com.college.ui.reports;

import com.college.dao.GatePassDAO;
import com.college.models.GatePass;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Gate Pass Report Panel
 * Generate gate pass analytics and statistics
 */
public class GatePassReportPanel extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    private GatePassDAO gatePassDAO;

    public GatePassReportPanel() {
        gatePassDAO = new GatePassDAO();
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

        String[] columns = { "Student Name", "From Date", "To Date", "Reason", "Status", "Approved By" };
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
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        summaryLabel = new JLabel("Loading...");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(summaryLabel);

        return panel;
    }

    private void generateReport() {
        tableModel.setRowCount(0);

        List<GatePass> allPasses = GatePassDAO.getAllPasses();

        int total = allPasses.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (GatePass pass : allPasses) {
            Object[] row = {
                    pass.getStudentName(),
                    pass.getFromDate(),
                    pass.getToDate(),
                    pass.getReason().length() > 30 ? pass.getReason().substring(0, 27) + "..." : pass.getReason(),
                    getStatusIcon(pass.getStatus()),
                    pass.getApprovedByName() != null ? pass.getApprovedByName() : "-"
            };
            tableModel.addRow(row);

            switch (pass.getStatus()) {
                case "PENDING":
                    pending++;
                    break;
                case "APPROVED":
                    approved++;
                    break;
                case "REJECTED":
                    rejected++;
                    break;
            }
        }

        double approvalRate = total > 0 ? (approved * 100.0 / total) : 0;
        updateSummary(total, pending, approved, rejected, approvalRate);
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "APPROVED":
                return "APPROVED";
            case "REJECTED":
                return "REJECTED";
            case "PENDING":
                return "PENDING";
            default:
                return status;
        }
    }

    private void updateSummary(int total, int pending, int approved, int rejected, double approvalRate) {
        summaryLabel.setText(String.format(
                "Total: %d | Pending: %d | Approved: %d | Rejected: %d | Approval Rate: %.1f%%",
                total, pending, approved, rejected, approvalRate));
    }

    private void exportReport() {
        com.college.utils.TableExporter.showExportDialog(this, reportTable, "gate_pass_report");
    }
}
