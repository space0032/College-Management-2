package com.college.ui.fees;

import com.college.dao.EnhancedFeeDAO;
import com.college.models.FeePayment;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Payment History Dialog
 * Shows all payments for a specific fee
 */
public class PaymentHistoryDialog extends JDialog {

    private EnhancedFeeDAO feeDAO;
    private StudentFee studentFee;

    private JTable paymentsTable;
    private DefaultTableModel tableModel;

    public PaymentHistoryDialog(Frame parent, StudentFee studentFee) {
        super(parent, "Payment History - " + studentFee.getCategoryName(), true);
        this.studentFee = studentFee;
        this.feeDAO = new EnhancedFeeDAO();

        initComponents();
        loadPayments();
    }

    private void initComponents() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Top Panel with Fee Info
        JPanel topPanel = createInfoPanel();

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton viewReceiptButton = UIHelper.createPrimaryButton("View Receipt");
        viewReceiptButton.setPreferredSize(new Dimension(140, 35));
        viewReceiptButton.addActionListener(e -> viewReceipt());

        JButton closeButton = UIHelper.createDangerButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(viewReceiptButton);
        buttonPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        panel.add(createInfoLabel("Student: " + studentFee.getStudentName()));
        panel.add(createInfoLabel("Category: " + studentFee.getCategoryName()));
        panel.add(createInfoLabel("Total: Rs. " + String.format("%.2f", studentFee.getTotalAmount())));
        panel.add(createInfoLabel("Paid: Rs. " + String.format("%.2f", studentFee.getPaidAmount())));
        panel.add(createInfoLabel("Balance: Rs. " + String.format("%.2f", studentFee.getBalanceAmount())));
        panel.add(createInfoLabel("Status: " + studentFee.getStatus()));

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        return label;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Receipt #", "Payment Date", "Amount", "Mode", "Transaction ID", "Remarks" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentsTable = new JTable(tableModel);
        UIHelper.styleTable(paymentsTable);

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        List<FeePayment> payments = feeDAO.getPaymentHistory(studentFee.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        for (FeePayment payment : payments) {
            Object[] row = {
                    payment.getReceiptNumber(),
                    sdf.format(payment.getPaymentDate()),
                    "Rs. " + String.format("%.2f", payment.getAmount()),
                    payment.getPaymentMode(),
                    payment.getTransactionId() != null ? payment.getTransactionId() : "-",
                    payment.getRemarks() != null ? payment.getRemarks() : "-"
            };
            tableModel.addRow(row);
        }

        if (payments.isEmpty()) {
            tableModel.addRow(new Object[] { "No payments recorded yet", "", "", "", "", "" });
        }
    }

    private void viewReceipt() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a payment to view receipt!");
            return;
        }

        // Get the payment
        List<FeePayment> payments = feeDAO.getPaymentHistory(studentFee.getId());
        if (selectedRow < payments.size()) {
            FeePayment payment = payments.get(selectedRow);
            FeeReceiptDialog dialog = new FeeReceiptDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    payment,
                    studentFee);
            dialog.setVisible(true);
        }
    }
}
