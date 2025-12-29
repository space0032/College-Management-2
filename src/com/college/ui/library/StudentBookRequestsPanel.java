package com.college.ui.library;

import com.college.dao.BookRequestDAO;
import com.college.models.BookRequest;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student Book Requests Panel
 * For students to view their book request history
 */
public class StudentBookRequestsPanel extends JPanel {

    private BookRequestDAO requestDAO;
    private int studentId;

    private JTable requestsTable;
    private DefaultTableModel tableModel;

    public StudentBookRequestsPanel(int studentId) {
        this.studentId = studentId;
        this.requestDAO = new BookRequestDAO();

        initComponents();
        loadRequests();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("My Book Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonGroup.setBackground(Color.WHITE);

        JButton exportButton = UIHelper.createSuccessButton("Export");
        exportButton.addActionListener(
                e -> com.college.utils.TableExporter.showExportDialog(this, requestsTable, "BookRequests"));

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadRequests());

        buttonGroup.add(exportButton);
        buttonGroup.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonGroup, BorderLayout.EAST);

        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel infoLabel = new JLabel(
                "ℹ️ Track the status of your book requests here. Approved requests are automatically issued.");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        infoPanel.add(infoLabel);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        add(topPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Book Title", "Author", "Request Date", "Approved Date", "Status", "Remarks" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        UIHelper.styleTable(requestsTable);

        // Custom column widths
        requestsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        requestsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        requestsTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<BookRequest> requests = requestDAO.getRequestsByStudent(studentId);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        for (BookRequest request : requests) {
            String approvedDate = request.getApprovedDate() != null ? sdf.format(request.getApprovedDate()) : "-";

            Object[] row = {
                    request.getId(),
                    request.getBookTitle(),
                    request.getBookAuthor(),
                    sdf.format(request.getRequestDate()),
                    approvedDate,
                    getStatusBadge(request.getStatus()),
                    request.getRemarks() != null ? request.getRemarks() : "-"
            };
            tableModel.addRow(row);
        }

        if (requests.isEmpty()) {
            tableModel.addRow(new Object[] { "", "No book requests yet", "", "", "", "",
                    "Click 'Request Book' in Library to submit a request" });
        }
    }

    private String getStatusBadge(String status) {
        switch (status) {
            case "PENDING":
                return "⏳ PENDING";
            case "APPROVED":
                return "✅ APPROVED";
            case "REJECTED":
                return "❌ REJECTED";
            default:
                return status;
        }
    }
}
