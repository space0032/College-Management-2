package com.college.ui.library;

import com.college.dao.BookIssueDAO;
import com.college.dao.LibraryDAO;
import com.college.dao.StudentDAO;
import com.college.models.Book;
import com.college.models.BookIssue;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Issue Book Dialog
 * For issuing books to students
 */
public class IssueBookDialog extends JDialog {

    private BookIssueDAO bookIssueDAO;
    private LibraryDAO libraryDAO;
    private StudentDAO studentDAO;
    private int issuedBy;

    private JComboBox<StudentItem> studentCombo;
    private JComboBox<BookItem> bookCombo;
    private JTextField dueDaysField;
    private JLabel dueDateLabel;

    public IssueBookDialog(Frame parent, int issuedBy) {
        super(parent, "Issue Book", true);
        this.issuedBy = issuedBy;
        this.bookIssueDAO = new BookIssueDAO();
        this.libraryDAO = new LibraryDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(500, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Student
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Student:"), gbc);

        gbc.gridx = 1;
        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(250, 30));
        formPanel.add(studentCombo, gbc);

        // Book
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Book:"), gbc);

        gbc.gridx = 1;
        bookCombo = new JComboBox<>();
        bookCombo.setPreferredSize(new Dimension(250, 30));
        formPanel.add(bookCombo, gbc);

        // Due Days
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Loan Period (days):"), gbc);

        gbc.gridx = 1;
        dueDaysField = UIHelper.createTextField(10);
        dueDaysField.setText("14"); // Default 14 days
        dueDaysField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateDueDate();
            }
        });
        formPanel.add(dueDaysField, gbc);

        // Due Date Display
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Due Date:"), gbc);

        gbc.gridx = 1;
        dueDateLabel = new JLabel();
        dueDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dueDateLabel.setForeground(UIHelper.PRIMARY_COLOR);
        updateDueDate();
        formPanel.add(dueDateLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton issueButton = UIHelper.createPrimaryButton("Issue Book");
        issueButton.setPreferredSize(new Dimension(130, 35));
        issueButton.addActionListener(e -> issueBook());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(130, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(issueButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Load students
        List<Student> students = studentDAO.getAllStudents();
        for (Student student : students) {
            studentCombo.addItem(new StudentItem(student));
        }

        // Load available books
        List<Book> books = libraryDAO.getAllBooks();
        for (Book book : books) {
            if (book.getAvailable() > 0) {
                bookCombo.addItem(new BookItem(book));
            }
        }
    }

    private void updateDueDate() {
        try {
            int days = Integer.parseInt(dueDaysField.getText());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, days);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yyyy");
            dueDateLabel.setText(sdf.format(cal.getTime()));
        } catch (NumberFormatException e) {
            dueDateLabel.setText("Invalid");
        }
    }

    private void issueBook() {
        StudentItem student = (StudentItem) studentCombo.getSelectedItem();
        BookItem book = (BookItem) bookCombo.getSelectedItem();

        if (student == null || book == null) {
            UIHelper.showErrorMessage(this, "Please select student and book!");
            return;
        }

        try {
            int loanPeriod = Integer.parseInt(dueDaysField.getText());

            // Check if book is available
            if (!bookIssueDAO.isBookAvailable(book.book.getId())) {
                UIHelper.showErrorMessage(this, "Book is not available!");
                return;
            }

            // Create book issue
            Date issueDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, loanPeriod);
            Date dueDate = cal.getTime();

            BookIssue issue = new BookIssue(student.student.getId(), book.book.getId(), issueDate, dueDate);
            issue.setIssuedBy(issuedBy);

            if (bookIssueDAO.issueBook(issue)) {
                UIHelper.showSuccessMessage(this, "Book issued successfully!");
                dispose();
            } else {
                UIHelper.showErrorMessage(this, "Failed to issue book!");
            }

        } catch (NumberFormatException e) {
            UIHelper.showErrorMessage(this, "Invalid loan period!");
        }
    }

    private static class StudentItem {
        Student student;

        StudentItem(Student s) {
            this.student = s;
        }

        @Override
        public String toString() {
            return student.getId() + " - " + student.getName();
        }
    }

    private static class BookItem {
        Book book;

        BookItem(Book b) {
            this.book = b;
        }

        @Override
        public String toString() {
            return book.getTitle() + " (Available: " + book.getAvailable() + ")";
        }
    }
}
