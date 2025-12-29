package com.college.ui.fees;

import com.college.dao.EnhancedFeeDAO;
import com.college.dao.StudentDAO;
import com.college.models.FeeCategory;
import com.college.models.Student;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Assign Fees Dialog
 * For admin to assign fees to students
 */
public class AssignFeesDialog extends JDialog {

    private EnhancedFeeDAO feeDAO;
    private StudentDAO studentDAO;

    private JComboBox<String> studentCombo;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField academicYearField;
    private JSpinner dueDateSpinner;

    private List<Student> students;
    private List<FeeCategory> categories;

    public AssignFeesDialog(Frame parent) {
        super(parent, "Assign Fees to Student", true);
        this.feeDAO = new EnhancedFeeDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(UIHelper.PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Assign Fee to Student");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

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
        formPanel.add(studentCombo, gbc);

        // Fee Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Fee Category:"), gbc);

        gbc.gridx = 1;
        categoryCombo = new JComboBox<>();
        categoryCombo.addActionListener(e -> updateAmount());
        formPanel.add(categoryCombo, gbc);

        // Academic Year
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Academic Year:"), gbc);

        gbc.gridx = 1;
        academicYearField = new JTextField(15);
        academicYearField.setText(getCurrentAcademicYear());
        formPanel.add(academicYearField, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Amount (Rs.):"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);

        // Due Date
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(UIHelper.createLabel("Due Date:"), gbc);

        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dueDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "dd-MMM-yyyy");
        dueDateSpinner.setEditor(dateEditor);
        formPanel.add(dueDateSpinner, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton assignButton = UIHelper.createSuccessButton("Assign Fee");
        assignButton.setPreferredSize(new Dimension(130, 35));
        assignButton.addActionListener(e -> assignFee());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);

        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Load students
        students = studentDAO.getAllStudents();
        for (Student student : students) {
            studentCombo.addItem(student.getId() + " - " + student.getName());
        }

        // Load fee categories
        categories = feeDAO.getAllCategories();
        for (FeeCategory category : categories) {
            categoryCombo.addItem(category.getCategoryName());
        }

        if (categoryCombo.getItemCount() > 0) {
            updateAmount();
        }
    }

    private void updateAmount() {
        int selectedIndex = categoryCombo.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < categories.size()) {
            FeeCategory category = categories.get(selectedIndex);
            amountField.setText(String.format("%.2f", category.getBaseAmount()));
        }
    }

    private String getCurrentAcademicYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        if (month >= Calendar.APRIL) {
            return year + "-" + String.valueOf(year + 1).substring(2);
        } else {
            return (year - 1) + "-" + String.valueOf(year).substring(2);
        }
    }

    private void assignFee() {
        try {
            // Validate inputs
            if (studentCombo.getSelectedIndex() == -1) {
                UIHelper.showErrorMessage(this, "Please select a student!");
                return;
            }

            if (categoryCombo.getSelectedIndex() == -1) {
                UIHelper.showErrorMessage(this, "Please select a fee category!");
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                UIHelper.showErrorMessage(this, "Amount must be greater than zero!");
                return;
            }

            // Extract student ID
            Student student = students.get(studentCombo.getSelectedIndex());
            FeeCategory category = categories.get(categoryCombo.getSelectedIndex());

            // Create student fee
            StudentFee studentFee = new StudentFee();
            studentFee.setStudentId(student.getId());
            studentFee.setCategoryId(category.getId());
            studentFee.setAcademicYear(academicYearField.getText().trim());
            studentFee.setTotalAmount(amount);
            studentFee.setDueDate((Date) dueDateSpinner.getValue());

            if (feeDAO.assignFeeToStudent(studentFee)) {
                UIHelper.showSuccessMessage(this,
                        "Fee assigned successfully!\n\n" +
                                "Student: " + student.getName() + "\n" +
                                "Category: " + category.getCategoryName() + "\n" +
                                "Amount: Rs. " + String.format("%.2f", amount));
                dispose();
            } else {
                UIHelper.showErrorMessage(this, "Failed to assign fee!");
            }

        } catch (NumberFormatException e) {
            UIHelper.showErrorMessage(this, "Invalid amount!");
        }
    }
}
