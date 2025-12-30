package com.college.ui.reports;

import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Reports Panel - Centralized hub for all reports
 * Provides tabbed interface for different report categories
 */
public class ReportsPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private String role;
    private int userId;

    public ReportsPanel(String role, int userId) {
        this.role = role;
        this.userId = userId;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Generate comprehensive reports for all modules");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Tabbed pane for different report categories
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        // Add report panels
        tabbedPane.addTab("Attendance", new AttendanceReportPanel(role, userId));
        tabbedPane.addTab("Grades", new GradeReportPanel(role, userId));
        tabbedPane.addTab("Fees", new FeeReportPanel(role, userId));
        tabbedPane.addTab("Library", new LibraryReportPanel(role, userId));
        tabbedPane.addTab("Gate Pass", new GatePassReportPanel(role, userId));

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
}
