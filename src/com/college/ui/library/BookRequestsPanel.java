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
 * Book Requests Panel
 * For faculty/admin to view and approve/reject book requests
 */
public class BookRequestsPanel extends JPanel {

    private BookRequestDAO requestDAO;
    private int userId;

    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public BookRequestsPanel(int userId) {
        this.userId = userId;
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

        JLabel titleLabel = new JLabel("Pending Book Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        countLabel = new JLabel();
        countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        countLabel.setForeground(new Color(127, 140, 141));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(countLabel, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton approveButton = UIHelper.createSuccessButton("Approve Request");
        approveButton.setPreferredSize(new Dimension(160, 40));
        approveButton.addActionListener(e -> approveRequest());

        JButton rejectButton = UIHelper.createDangerButton("Reject Request");
        rejectButton.setPreferredSize(new Dimension(160, 40));
        rejectButton.addActionListener(e -> rejectRequest());

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.addActionListener(e -> loadRequests());

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Student", "Book Title", "Author", "Request Date", "Loan Period" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        UIHelper.styleTable(requestsTable);

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<BookRequest> requests = requestDAO.getPendingRequests();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        for (BookRequest request : requests) {
            Object[] row = {
                    request.getId(),
                    request.getStudentName(),
                    request.getBookTitle(),
                    request.getBookAuthor(),
                    sdf.format(request.getRequestDate()),
                    request.getLoanPeriodDays() + " days"
            };
            tableModel.addRow(row);
        }

        countLabel.setText(requests.size() + " pending request(s)");

        if (requests.isEmpty()) {
            Object[] row = { "", "No pending requests", "", "", "", "" };
            tableModel.addRow(row);
        }
    }

    private void approveRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a request to approve!");
            return;
        }

        int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve book request?\n\n" +
                        "Student: " + studentName + "\n" +
                        "Book: " + bookTitle + "\n\n" +
                        "The book will be automatically issued.",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (requestDAO.approveRequest(requestId, userId)) {
                UIHelper.showSuccessMessage(this, "Request approved and book issued successfully!");
                loadRequests();
            } else {
                UIHelper.showErrorMessage(this, "Failed to approve request!\nBook may not be available.");
            }
        }
    }

    private void rejectRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a request to reject!");
            return;
        }

        int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);

        String remarks = JOptionPane.showInputDialog(this,
                "Reject book request for " + studentName + "?\n" +
                        "Book: " + bookTitle + "\n\n" +
                        "Please provide a reason:",
                "Reject Request",
                JOptionPane.QUESTION_MESSAGE);

        if (remarks != null && !remarks.trim().isEmpty()) {
            if (requestDAO.rejectRequest(requestId, userId, remarks)) {
                UIHelper.showSuccessMessage(this, "Request rejected!");
                loadRequests();
            } else {
                UIHelper.showErrorMessage(this, "Failed to reject request!");
            }
        }
    }
}
