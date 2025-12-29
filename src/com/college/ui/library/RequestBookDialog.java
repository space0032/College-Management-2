package com.college.ui.library;

import com.college.dao.BookRequestDAO;
import com.college.dao.LibraryDAO;
import com.college.models.Book;
import com.college.models.BookRequest;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Request Book Dialog
 * For students to request books
 */
public class RequestBookDialog extends JDialog {

    private BookRequestDAO requestDAO;
    private LibraryDAO libraryDAO;
    private int studentId;

    private JComboBox<BookItem> bookCombo;
    private JSpinner loanPeriodSpinner;
    private JTextArea remarksArea;

    public RequestBookDialog(Frame parent, int studentId) {
        super(parent, "Request Book", true);
        this.studentId = studentId;
        this.requestDAO = new BookRequestDAO();
        this.libraryDAO = new LibraryDAO();

        initComponents();
        loadBooks();
    }

    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Book Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Select Book:"), gbc);

        gbc.gridx = 1;
        bookCombo = new JComboBox<>();
        bookCombo.setPreferredSize(new Dimension(300, 30));
        formPanel.add(bookCombo, gbc);

        // Loan Period
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Loan Period (days):"), gbc);

        gbc.gridx = 1;
        SpinnerModel model = new SpinnerNumberModel(14, 7, 30, 1);
        loanPeriodSpinner = new JSpinner(model);
        loanPeriodSpinner.setPreferredSize(new Dimension(100, 30));
        formPanel.add(loanPeriodSpinner, gbc);

        // Remarks
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(UIHelper.createLabel("Remarks (optional):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        remarksArea = new JTextArea(5, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        JScrollPane scrollPane = new JScrollPane(remarksArea);
        formPanel.add(scrollPane, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton submitButton = UIHelper.createPrimaryButton("Submit Request");
        submitButton.setPreferredSize(new Dimension(150, 35));
        submitButton.addActionListener(e -> submitRequest());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(130, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadBooks() {
        List<Book> books = libraryDAO.getAllBooks();
        for (Book book : books) {
            if (book.getAvailable() > 0) {
                bookCombo.addItem(new BookItem(book));
            }
        }
    }

    private void submitRequest() {
        BookItem selectedItem = (BookItem) bookCombo.getSelectedItem();
        if (selectedItem == null) {
            UIHelper.showErrorMessage(this, "Please select a book!");
            return;
        }

        int loanPeriod = (Integer) loanPeriodSpinner.getValue();
        String remarks = remarksArea.getText().trim();

        BookRequest request = new BookRequest(studentId, selectedItem.book.getId(), loanPeriod);
        if (!remarks.isEmpty()) {
            request.setRemarks(remarks);
        }

        if (requestDAO.createRequest(request)) {
            UIHelper.showSuccessMessage(this,
                    "Book request submitted successfully!\n\n" +
                            "Your request will be reviewed by the library staff.\n" +
                            "You will be notified once approved.");
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to submit request!");
        }
    }

    private static class BookItem {
        Book book;

        BookItem(Book b) {
            this.book = b;
        }

        @Override
        public String toString() {
            return book.getTitle() + " by " + book.getAuthor() +
                    " (Available: " + book.getAvailable() + ")";
        }
    }
}
