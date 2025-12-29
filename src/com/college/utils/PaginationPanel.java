package com.college.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Pagination component for tables
 * Provides page navigation controls
 */
public class PaginationPanel extends JPanel {

    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = 20;
    private PaginationListener listener;

    private JLabel pageLabel;
    private JButton firstButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton lastButton;
    private JComboBox<Integer> pageSizeCombo;

    public interface PaginationListener {
        void onPageChanged(int page, int pageSize);
    }

    public PaginationPanel(PaginationListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBackground(Color.WHITE);

        // First button
        firstButton = new JButton("<<");
        firstButton.setToolTipText("First Page");
        firstButton.addActionListener(e -> goToPage(1));

        // Previous button
        prevButton = new JButton("<");
        prevButton.setToolTipText("Previous Page");
        prevButton.addActionListener(e -> goToPage(currentPage - 1));

        // Page label
        pageLabel = new JLabel("Page 1 of 1");
        pageLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Next button
        nextButton = new JButton(">");
        nextButton.setToolTipText("Next Page");
        nextButton.addActionListener(e -> goToPage(currentPage + 1));

        // Last button
        lastButton = new JButton(">>");
        lastButton.setToolTipText("Last Page");
        lastButton.addActionListener(e -> goToPage(totalPages));

        // Page size selector
        JLabel pageSizeLabel = new JLabel("Rows:");
        Integer[] pageSizes = { 10, 20, 50, 100 };
        pageSizeCombo = new JComboBox<>(pageSizes);
        pageSizeCombo.setSelectedItem(20);
        pageSizeCombo.addActionListener(e -> {
            pageSize = (Integer) pageSizeCombo.getSelectedItem();
            currentPage = 1;
            notifyListener();
        });

        // Add components
        add(firstButton);
        add(prevButton);
        add(pageLabel);
        add(nextButton);
        add(lastButton);
        add(new JLabel("  |  "));
        add(pageSizeLabel);
        add(pageSizeCombo);

        updateButtons();
    }

    public void setTotalRecords(int totalRecords) {
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages < 1)
            totalPages = 1;
        if (currentPage > totalPages)
            currentPage = totalPages;
        updateButtons();
    }

    private void goToPage(int page) {
        if (page >= 1 && page <= totalPages && page != currentPage) {
            currentPage = page;
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onPageChanged(currentPage, pageSize);
        }
        updateButtons();
    }

    private void updateButtons() {
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        firstButton.setEnabled(currentPage > 1);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
        lastButton.setEnabled(currentPage < totalPages);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }
}
