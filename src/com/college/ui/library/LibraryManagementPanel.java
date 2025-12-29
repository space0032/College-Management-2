package com.college.ui.library;

import com.college.dao.LibraryDAO;
import com.college.models.Book;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Library Management Panel
 * Manages books and book issues
 */
public class LibraryManagementPanel extends JPanel {

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private LibraryDAO libraryDAO;
    private String userRole;
    private int userId;

    public LibraryManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        libraryDAO = new LibraryDAO();
        initComponents();
        loadBooks();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel(userRole.equals("STUDENT") ? "Library" : "Library Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadBooks());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel - Different buttons for different roles
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            // Admin/Faculty buttons
            JButton addButton = UIHelper.createSuccessButton("Add Book");
            addButton.setPreferredSize(new Dimension(150, 40));
            addButton.addActionListener(e -> addBook());

            JButton issueButton = UIHelper.createPrimaryButton("Issue Book");
            issueButton.setPreferredSize(new Dimension(150, 40));
            issueButton.addActionListener(e -> issueBook());

            JButton returnButton = UIHelper.createPrimaryButton("Return Book");
            returnButton.setPreferredSize(new Dimension(150, 40));
            returnButton.addActionListener(e -> returnBook());

            JButton exportButton = UIHelper.createPrimaryButton("Export");
            exportButton.setPreferredSize(new Dimension(120, 40));
            exportButton
                    .addActionListener(e -> com.college.utils.TableExporter.showExportDialog(this, bookTable, "books"));

            buttonPanel.add(addButton);
            buttonPanel.add(issueButton);
            buttonPanel.add(returnButton);
            buttonPanel.add(exportButton);
        } else if (userRole.equals("STUDENT")) {
            // Student button - Request Book
            JButton requestButton = UIHelper.createPrimaryButton("Request Book");
            requestButton.setPreferredSize(new Dimension(150, 40));
            requestButton.addActionListener(e -> requestBook());
            buttonPanel.add(requestButton);
        }

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Title", "Author", "ISBN", "Quantity", "Available" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        UIHelper.styleTable(bookTable);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        List<Book> books = libraryDAO.getAllBooks();

        for (Book book : books) {
            Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getQuantity(),
                    book.getAvailable()
            };
            tableModel.addRow(row);
        }
    }

    private void addBook() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField quantityField = new JTextField();

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("ISBN:"));
        panel.add(isbnField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Book book = new Book();
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setIsbn(isbnField.getText().trim());
                int qty = Integer.parseInt(quantityField.getText().trim());
                book.setQuantity(qty);
                book.setAvailable(qty);

                if (libraryDAO.addBook(book)) {
                    UIHelper.showSuccessMessage(this, "Book added successfully!");
                    loadBooks();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to add book!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Invalid data!");
            }
        }
    }

    /**
     * Request a book (for students)
     */
    private void requestBook() {
        // Get selected book
        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select a book to request!");
            return;
        }

        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String bookAuthor = (String) tableModel.getValueAt(selectedRow, 2);
        int available = (Integer) tableModel.getValueAt(selectedRow, 5);

        if (available <= 0) {
            UIHelper.showErrorMessage(this, "This book is currently not available!");
            return;
        }

        // Show confirmation
        String message = String.format(
                "Do you want to request this book?\n\nTitle: %s\nAuthor: %s\nAvailable: %d copies",
                bookTitle, bookAuthor, available);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "Request Book",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // In a real system, this would create a book request in the database
            // For now, just show a success message
            UIHelper.showSuccessMessage(
                    this,
                    String.format(
                            "Book request submitted successfully!\n\n" +
                                    "Book: %s\n" +
                                    "Author: %s\n\n" +
                                    "Please contact the librarian to collect your book.",
                            bookTitle, bookAuthor));
        }
    }
    
    private void issueBook() {
        IssueBookDialog dialog = new IssueBookDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), userId);
        dialog.setVisible(true);
        loadBooks();
    }
    
    private void returnBook() {
        ReturnBookDialog dialog = new ReturnBookDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), userId);
        dialog.setVisible(true);
        loadBooks();
    }
}
