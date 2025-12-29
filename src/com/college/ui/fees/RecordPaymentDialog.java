package com.college.ui.fees;

import com.college.dao.EnhancedFeeDAO;
import com.college.models.StudentFee;
import com.college.models.FeePayment;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Record Payment Dialog
 */
public class RecordPaymentDialog extends JDialog {

    private EnhancedFeeDAO feeDAO;
    private StudentFee studentFee;
    private int userId;

    private JTextField amountField;
    private JComboBox<String> paymentModeCombo;
    private JTextField transactionIdField;
    private JTextArea remarksArea;

    public RecordPaymentDialog(Frame parent, StudentFee studentFee, int userId) {
        super(parent, "Record Payment", true);
        this.studentFee = studentFee;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();

        initComponents();
    }

    private void initComponents() {
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        infoPanel.add(createInfoLabel("Student: " + studentFee.getStudentName()));
        infoPanel.add(createInfoLabel("Fee Category: " + studentFee.getCategoryName()));
        infoPanel.add(createInfoLabel("Total Amount: Rs. " + String.format("%.2f", studentFee.getTotalAmount())));
        infoPanel.add(createInfoLabel("Balance Due: Rs. " + String.format("%.2f", studentFee.getBalanceAmount())));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Amount:"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setText(String.format("%.2f", studentFee.getBalanceAmount()));
        formPanel.add(amountField, gbc);

        // Payment Mode
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Payment Mode:"), gbc);

        gbc.gridx = 1;
        String[] modes = { "CASH", "ONLINE", "CHEQUE", "CARD" };
        paymentModeCombo = new JComboBox<>(modes);
        formPanel.add(paymentModeCombo, gbc);

        // Transaction ID
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Transaction ID:"), gbc);

        gbc.gridx = 1;
        transactionIdField = new JTextField(15);
        formPanel.add(transactionIdField, gbc);

        // Remarks
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(UIHelper.createLabel("Remarks:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        remarksArea = new JTextArea(4, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        JScrollPane scrollPane = new JScrollPane(remarksArea);
        formPanel.add(scrollPane, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton recordButton = UIHelper.createSuccessButton("Record Payment");
        recordButton.setPreferredSize(new Dimension(150, 35));
        recordButton.addActionListener(e -> recordPayment());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(recordButton);
        buttonPanel.add(cancelButton);

        add(infoPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    private void recordPayment() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());

            if (amount <= 0) {
                UIHelper.showErrorMessage(this, "Amount must be greater than zero!");
                return;
            }

            if (amount > studentFee.getBalanceAmount()) {
                UIHelper.showErrorMessage(this, "Amount cannot exceed balance due!");
                return;
            }

            FeePayment payment = new FeePayment(studentFee.getId(), amount);
            payment.setPaymentMode((String) paymentModeCombo.getSelectedItem());
            payment.setTransactionId(transactionIdField.getText().trim());
            payment.setRemarks(remarksArea.getText().trim());
            payment.setReceivedBy(userId);

            if (feeDAO.recordPayment(payment)) {
                UIHelper.showSuccessMessage(this,
                        "Payment recorded successfully!\n\n" +
                                "Receipt Number: " + payment.getReceiptNumber() + "\n" +
                                "Amount: Rs. " + String.format("%.2f", amount));
                dispose();
            } else {
                UIHelper.showErrorMessage(this, "Failed to record payment!");
            }

        } catch (NumberFormatException e) {
            UIHelper.showErrorMessage(this, "Invalid amount!");
        }
    }
}
