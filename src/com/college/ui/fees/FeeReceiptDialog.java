package com.college.ui.fees;

import com.college.models.FeePayment;
import com.college.models.StudentFee;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;

/**
 * Fee Receipt Viewer/Printer
 */
public class FeeReceiptDialog extends JDialog implements Printable {

    private FeePayment payment;
    private StudentFee studentFee;
    private JPanel receiptPanel;

    public FeeReceiptDialog(Frame parent, FeePayment payment, StudentFee studentFee) {
        super(parent, "Fee Receipt - " + payment.getReceiptNumber(), true);
        this.payment = payment;
        this.studentFee = studentFee;

        initComponents();
    }

    private void initComponents() {
        setSize(600, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Receipt Panel
        receiptPanel = createReceiptPanel();
        JScrollPane scrollPane = new JScrollPane(receiptPanel);
        scrollPane.setBorder(null);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton printButton = UIHelper.createPrimaryButton("Print Receipt");
        printButton.setPreferredSize(new Dimension(140, 35));
        printButton.addActionListener(e -> printReceipt());

        JButton closeButton = UIHelper.createDangerButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        // Header
        JLabel headerLabel = new JLabel("FEE PAYMENT RECEIPT");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(headerLabel);

        panel.add(Box.createVerticalStrut(10));

        JLabel collegeLabel = new JLabel("College Management System");
        collegeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        collegeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(collegeLabel);

        panel.add(Box.createVerticalStrut(20));

        // Horizontal line
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(500, 1));
        panel.add(separator);

        panel.add(Box.createVerticalStrut(20));

        // Receipt details
        addReceiptRow(panel, "Receipt Number:", payment.getReceiptNumber());
        addReceiptRow(panel, "Date:", sdf.format(payment.getPaymentDate()));

        panel.add(Box.createVerticalStrut(15));

        addReceiptRow(panel, "Student Name:", studentFee.getStudentName());
        addReceiptRow(panel, "Fee Category:", studentFee.getCategoryName());
        addReceiptRow(panel, "Academic Year:", studentFee.getAcademicYear());

        panel.add(Box.createVerticalStrut(15));

        addReceiptRow(panel, "Amount Paid:", "Rs. " + String.format("%.2f", payment.getAmount()));
        addReceiptRow(panel, "Payment Mode:", payment.getPaymentMode());

        if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
            addReceiptRow(panel, "Transaction ID:", payment.getTransactionId());
        }

        panel.add(Box.createVerticalStrut(15));

        addReceiptRow(panel, "Total Fee:", "Rs. " + String.format("%.2f", studentFee.getTotalAmount()));
        addReceiptRow(panel, "Total Paid:", "Rs. " + String.format("%.2f", studentFee.getPaidAmount()));
        addReceiptRow(panel, "Balance Due:", "Rs. " + String.format("%.2f", studentFee.getBalanceAmount()));

        panel.add(Box.createVerticalStrut(20));

        if (payment.getRemarks() != null && !payment.getRemarks().isEmpty()) {
            JLabel remarksLabel = new JLabel("Remarks: " + payment.getRemarks());
            remarksLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            remarksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(remarksLabel);
            panel.add(Box.createVerticalStrut(15));
        }

        // Footer
        panel.add(Box.createVerticalStrut(30));
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(500, 1));
        panel.add(separator2);
        panel.add(Box.createVerticalStrut(15));

        JLabel footerLabel = new JLabel("This is a computer-generated receipt");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(footerLabel);

        return panel;
    }

    private void addReceiptRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(500, 30));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 13));

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.EAST);

        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(8));
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        if (job.printDialog()) {
            try {
                job.print();
                UIHelper.showSuccessMessage(this, "Receipt sent to printer!");
            } catch (PrinterException e) {
                UIHelper.showErrorMessage(this, "Failed to print: " + e.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Scale to fit page
        double scaleX = pageFormat.getImageableWidth() / receiptPanel.getWidth();
        double scaleY = pageFormat.getImageableHeight() / receiptPanel.getHeight();
        double scale = Math.min(scaleX, scaleY);
        g2d.scale(scale, scale);

        receiptPanel.print(g2d);

        return PAGE_EXISTS;
    }
}
