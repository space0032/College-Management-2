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

    public LibraryManagementPanel() {
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

        JLabel titleLabel = new JLabel("Library Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadBooks());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = UIHelper.createSuccessButton("Add Book");
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> addBook());

        JButton issueButton = UIHelper.createPrimaryButton("Issue Book");
        issueButton.setPreferredSize(new Dimension(150, 40));
        issueButton.addActionListener(e -> UIHelper.showSuccessMessage(this, "Issue book feature coming soon!"));

        buttonPanel.add(addButton);
        buttonPanel.add(issueButton);

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
}
