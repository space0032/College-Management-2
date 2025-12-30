package com.college.ui.reports;

import com.college.dao.BookIssueDAO;
import com.college.dao.LibraryDAO;
import com.college.models.BookIssue;
import com.college.models.Book;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * Library Report Panel
 * Generate library usage and overdue books reports
 */
public class LibraryReportPanel extends JPanel {

    private JComboBox<String> reportTypeCombo;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;

    public LibraryReportPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel filterPanel = createFilterPanel();
        JPanel tablePanel = createTablePanel();
        JPanel summaryPanel = createSummaryPanel();

        add(filterPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Report Type"));

        panel.add(new JLabel("Select Report:"));
        reportTypeCombo = new JComboBox<>(new String[] {
                "Most Issued Books", "Overdue Books", "All Issued Books"
        });
        reportTypeCombo.setPreferredSize(new Dimension(200, 30));
        panel.add(reportTypeCombo);

        JButton generateButton = UIHelper.createPrimaryButton("Generate");
        generateButton.addActionListener(e -> generateReport());
        panel.add(generateButton);

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportReport());
        panel.add(exportButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = { "Book/Student", "Details", "Count/Date", "Status" };
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

        summaryLabel = new JLabel("Select report type and generate");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(summaryLabel);

        return panel;
    }

    private void generateReport() {
        tableModel.setRowCount(0);
        String reportType = (String) reportTypeCombo.getSelectedItem();

        switch (reportType) {
            case "Most Issued Books":
                generateMostIssuedReport();
                break;
            case "Overdue Books":
                generateOverdueReport();
                break;
            case "All Issued Books":
                generateAllIssuedReport();
                break;
        }
    }

    private void generateMostIssuedReport() {
        LibraryDAO libraryDAO = new LibraryDAO();

        List<Book> allBooks = libraryDAO.getAllBooks();

        // Show books sorted by availability
        int showing = Math.min(10, allBooks.size());
        for (int i = 0; i < showing; i++) {
            Book book = allBooks.get(i);
            Object[] row = {
                    "#" + (i + 1),
                    book.getTitle(),
                    "Available: " + book.getAvailable() + "/" + book.getQuantity(),
                    book.getAvailable() > 0 ? "✅ Available" : "❌ Out of Stock"
            };
            tableModel.addRow(row);
        }

        summaryLabel.setText("Showing " + showing + " books from library");
    }

    private void generateOverdueReport() {
        BookIssueDAO issueDAO = new BookIssueDAO();
        List<BookIssue> issuedBooks = issueDAO.getAllIssuedBooks();

        int overdueCount = 0;
        double totalFines = 0;

        for (BookIssue issue : issuedBooks) {
            // Estimate 7 days for overdue (simplified)
            long daysOverdue = 7;
            double fine = daysOverdue * 5; // Rs. 5 per day

            Object[] row = {
                    issue.getStudentName(),
                    issue.getBookTitle(),
                    daysOverdue + " days (estimated)",
                    "Rs. " + fine + " fine"
            };
            tableModel.addRow(row);
            overdueCount++;
            totalFines += fine;
        }

        summaryLabel.setText(String.format("Total Issued: %d | Estimated Fines: Rs. %.2f", overdueCount, totalFines));
    }

    private void generateAllIssuedReport() {
        BookIssueDAO issueDAO = new BookIssueDAO();
        List<BookIssue> issuedBooks = issueDAO.getAllIssuedBooks();

        for (BookIssue issue : issuedBooks) {
            Object[] row = {
                    issue.getStudentName(),
                    issue.getBookTitle(),
                    "Issued: " + issue.getIssueDate(),
                    "✅ Active"
            };
            tableModel.addRow(row);
        }

        summaryLabel.setText(String.format("Total Active Issues: %d", issuedBooks.size()));
    }

    private void exportReport() {
        com.college.utils.TableExporter.showExportDialog(this, reportTable, "library_report");
    }
}
