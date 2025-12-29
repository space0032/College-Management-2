package com.college.ui.library;

import com.college.dao.BookIssueDAO;
import com.college.models.BookIssue;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Return Book Dialog
 * For processing book returns
 */
public class ReturnBookDialog extends JDialog {

    private BookIssueDAO bookIssueDAO;
    private int returnedTo;

    private JTable issueTable;
    private DefaultTableModel tableModel;

    public ReturnBookDialog(Frame parent, int returnedTo) {
        super(parent, "Return Book", true);
        this.returnedTo = returnedTo;
        this.bookIssueDAO = new BookIssueDAO();

        initComponents();
        loadIssuedBooks();
    }

    private void initComponents() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Issued Books - Select to Return");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // Table
        JPanel tablePanel = createTablePanel();

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton returnButton = UIHelper.createPrimaryButton("Return Selected Book");
        returnButton.setPreferredSize(new Dimension(180, 35));
        returnButton.addActionListener(e -> returnBook());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(130, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(returnButton);
        buttonPanel.add(cancelButton);

        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Student", "Book", "Issue Date", "Due Date", "Fine" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        issueTable = new JTable(tableModel);
        UIHelper.styleTable(issueTable);

        JScrollPane scrollPane = new JScrollPane(issueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadIssuedBooks() {
        tableModel.setRowCount(0);
        List<BookIssue> issues = bookIssueDAO.getAllIssuedBooks();

        for (BookIssue issue : issues) {
            double fine = issue.calculateFine(5.0); // Rs. 5 per day

            Object[] row = {
                    issue.getId(),
                    issue.getStudentName(),
                    issue.getBookTitle(),
                    new java.text.SimpleDateFormat("dd-MMM-yyyy").format(issue.getIssueDate()),
                    new java.text.SimpleDateFormat("dd-MMM-yyyy").format(issue.getDueDate()),
                    fine > 0 ? String.format("Rs. %.2f", fine) : "No Fine"
            };
            tableModel.addRow(row);
        }

        if (tableModel.getRowCount() == 0) {
            Object[] row = { "", "No issued books", "", "", "", "" };
            tableModel.addRow(row);
        }
    }

    private void returnBook() {
        int selectedRow = issueTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a book to return!");
            return;
        }

        int issueId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);
        String fineStr = (String) tableModel.getValueAt(selectedRow, 5);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Return book '" + bookTitle + "' from " + studentName + "?\nFine: " + fineStr,
                "Confirm Return",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookIssueDAO.returnBook(issueId, returnedTo)) {
                UIHelper.showSuccessMessage(this, "Book returned successfully!");
                loadIssuedBooks(); // Refresh table
            } else {
                UIHelper.showErrorMessage(this, "Failed to return book!");
            }
        }
    }
}
