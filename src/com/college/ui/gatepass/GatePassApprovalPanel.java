package com.college.ui.gatepass;

import com.college.dao.GatePassDAO;
import com.college.dao.AuditLogDAO;
import com.college.models.GatePass;
import com.college.utils.SessionManager;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin Gate Pass Approval Panel
 * Allows admin/warden to approve or reject gate pass requests
 */
public class GatePassApprovalPanel extends JPanel {

    private JTable passTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;

    public GatePassApprovalPanel() {
        initComponents();
        loadGatePasses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Gate Pass Approvals");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Filter by Status:"));
        statusFilter = new JComboBox<>(new String[] { "ALL", "PENDING", "APPROVED", "REJECTED" });
        statusFilter.addActionListener(e -> loadGatePasses());
        filterPanel.add(statusFilter);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        // Table
        String[] columnNames = { "ID", "Student Name", "From", "To", "Reason",
                "Destination", "Contact", "Status", "Requested" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        passTable = new JTable(tableModel);
        passTable.setRowHeight(25);
        passTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        passTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        passTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        passTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        passTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        passTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(passTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton approveButton = UIHelper.createPrimaryButton("Approve");
        approveButton.setPreferredSize(new Dimension(120, 35));
        approveButton.addActionListener(e -> approveSelectedPass());

        JButton rejectButton = UIHelper.createDangerButton("Reject");
        rejectButton.setPreferredSize(new Dimension(120, 35));
        rejectButton.addActionListener(e -> rejectSelectedPass());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadGatePasses());

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void approveSelectedPass() {
        int selectedRow = passTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a gate pass to approve!");
            return;
        }

        int gatePassId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 7);

        // Remove status icon for comparison
        currentStatus = currentStatus.replace("✅ ", "").replace("❌ ", "").replace("⏳ ", "");

        if (!currentStatus.equals("PENDING")) {
            UIHelper.showErrorMessage(this, "Only pending requests can be approved!");
            return;
        }

        // Show comment dialog
        String comment = JOptionPane.showInputDialog(this,
                "Enter approval comment (optional):",
                "Approve Gate Pass",
                JOptionPane.QUESTION_MESSAGE);

        if (comment == null) { // User cancelled
            return;
        }

        if (comment.trim().isEmpty()) {
            comment = "Approved";
        }

        SessionManager session = SessionManager.getInstance();
        if (GatePassDAO.approveRequest(gatePassId, session.getUserId(), comment)) {
            // Log action
            String studentName = (String) tableModel.getValueAt(selectedRow, 1);
            AuditLogDAO.logAction(session.getUserId(), session.getUsername(),
                    "APPROVE_GATE_PASS", "GATE_PASS", gatePassId,
                    "Approved gate pass for student: " + studentName);

            UIHelper.showSuccessMessage(this, "Gate pass approved successfully!");
            loadGatePasses();
        } else {
            UIHelper.showErrorMessage(this, "Failed to approve gate pass. Please try again.");
        }
    }

    private void rejectSelectedPass() {
        int selectedRow = passTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a gate pass to reject!");
            return;
        }

        int gatePassId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 7);

        // Remove status icon for comparison
        currentStatus = currentStatus.replace("✅ ", "").replace("❌ ", "").replace("⏳ ", "");

        if (!currentStatus.equals("PENDING")) {
            UIHelper.showErrorMessage(this, "Only pending requests can be rejected!");
            return;
        }

        // Show comment dialog
        String comment = JOptionPane.showInputDialog(this,
                "Enter rejection reason (required):",
                "Reject Gate Pass",
                JOptionPane.QUESTION_MESSAGE);

        if (comment == null) { // User cancelled
            return;
        }

        if (comment.trim().isEmpty()) {
            UIHelper.showErrorMessage(this, "Rejection reason is required!");
            return;
        }

        SessionManager session = SessionManager.getInstance();
        if (GatePassDAO.rejectRequest(gatePassId, session.getUserId(), comment)) {
            // Log action
            String studentName = (String) tableModel.getValueAt(selectedRow, 1);
            AuditLogDAO.logAction(session.getUserId(), session.getUsername(),
                    "REJECT_GATE_PASS", "GATE_PASS", gatePassId,
                    "Rejected gate pass for student: " + studentName + " - Reason: " + comment);

            UIHelper.showSuccessMessage(this, "Gate pass rejected!");
            loadGatePasses();
        } else {
            UIHelper.showErrorMessage(this, "Failed to reject gate pass. Please try again.");
        }
    }

    private void loadGatePasses() {
        tableModel.setRowCount(0);

        String filter = (String) statusFilter.getSelectedItem();
        List<GatePass> passes;

        if (filter.equals("ALL")) {
            passes = GatePassDAO.getAllPasses();
        } else {
            passes = GatePassDAO.getPassesByStatus(filter);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (GatePass pass : passes) {
            Object[] row = {
                    pass.getId(),
                    pass.getStudentName(),
                    pass.getFromDate().format(dateFormatter),
                    pass.getToDate().format(dateFormatter),
                    pass.getReason().length() > 30 ? pass.getReason().substring(0, 27) + "..." : pass.getReason(),
                    pass.getDestination(),
                    pass.getParentContact(),
                    getStatusWithIcon(pass.getStatus()),
                    pass.getRequestedAt().format(dateTimeFormatter)
            };
            tableModel.addRow(row);
        }
    }

    private String getStatusWithIcon(String status) {
        switch (status) {
            case "APPROVED":
                return "✅ APPROVED";
            case "REJECTED":
                return "❌ REJECTED";
            case "PENDING":
                return "⏳ PENDING";
            default:
                return status;
        }
    }
}
