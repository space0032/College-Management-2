package com.college.ui.security;

import com.college.dao.AuditLogDAO;
import com.college.models.AuditLog;
import com.college.utils.UIHelper;
import com.college.utils.TableExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Audit Log Viewer Panel
 * Allows administrators to view and filter audit logs
 */
public class AuditLogViewerPanel extends JPanel {

    private JTable logTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> actionFilterCombo;
    private JComboBox<String> dateRangeCombo;

    public AuditLogViewerPanel() {
        initComponents();
        loadAuditLogs();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();

        // Filter Panel
        JPanel filterPanel = createFilterPanel();

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        // Combine header and filter
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Audit Logs");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));

        // Search field
        panel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> applyFilters());
        panel.add(searchField);

        // Action filter
        panel.add(new JLabel("Action:"));
        actionFilterCombo = new JComboBox<>(new String[] {
                "All Actions", "LOGIN", "LOGOUT", "LOGIN_FAILED",
                "CHANGE_PASSWORD", "CREATE_GATE_PASS", "APPROVE_GATE_PASS", "REJECT_GATE_PASS"
        });
        actionFilterCombo.addActionListener(e -> applyFilters());
        panel.add(actionFilterCombo);

        // Date range filter
        panel.add(new JLabel("Period:"));
        dateRangeCombo = new JComboBox<>(new String[] {
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days"
        });
        dateRangeCombo.addActionListener(e -> applyFilters());
        panel.add(dateRangeCombo);

        // Apply button
        JButton applyButton = UIHelper.createPrimaryButton("Apply Filters");
        applyButton.addActionListener(e -> applyFilters());
        panel.add(applyButton);

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFilters());
        panel.add(clearButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columnNames = { "ID", "User", "Action", "Entity Type", "Entity ID", "Details", "Timestamp" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        logTable.setRowHeight(25);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        logTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        logTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        logTable.getColumnModel().getColumn(5).setPreferredWidth(300);
        logTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Add sorting
        sorter = new TableRowSorter<>(tableModel);
        logTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(Color.WHITE);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadAuditLogs());

        JButton exportButton = UIHelper.createPrimaryButton("Export");
        exportButton.setPreferredSize(new Dimension(120, 35));
        exportButton.addActionListener(e -> TableExporter.showExportDialog(this, logTable, "audit_logs"));

        JButton deleteOldButton = new JButton("Cleanup Old Logs");
        deleteOldButton.setPreferredSize(new Dimension(150, 35));
        deleteOldButton.setToolTipText("Delete logs older than 6 months");
        deleteOldButton.addActionListener(e -> cleanupOldLogs());

        panel.add(refreshButton);
        panel.add(exportButton);
        panel.add(deleteOldButton);

        return panel;
    }

    private void loadAuditLogs() {
        tableModel.setRowCount(0);

        String dateRange = (String) dateRangeCombo.getSelectedItem();
        List<AuditLog> logs;

        if (dateRange.equals("All Time")) {
            logs = AuditLogDAO.getAllLogs();
        } else {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (dateRange) {
                case "Today":
                    startDate = LocalDate.now();
                    break;
                case "Last 7 Days":
                    startDate = endDate.minusDays(7);
                    break;
                case "Last 30 Days":
                    startDate = endDate.minusDays(30);
                    break;
                case "Last 90 Days":
                    startDate = endDate.minusDays(90);
                    break;
                default:
                    logs = AuditLogDAO.getAllLogs();
                    populateTable(logs);
                    return;
            }

            logs = AuditLogDAO.getLogsByDateRange(startDate, endDate);
        }

        populateTable(logs);
    }

    private void populateTable(List<AuditLog> logs) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuditLog log : logs) {
            Object[] row = {
                    log.getId(),
                    log.getUsername(),
                    log.getAction(),
                    log.getEntityType() != null ? log.getEntityType() : "-",
                    log.getEntityId() != null ? log.getEntityId() : "-",
                    log.getDetails() != null ? log.getDetails() : "-",
                    log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "-"
            };
            tableModel.addRow(row);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String actionFilter = (String) actionFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Search filter (searches all columns)
                if (!searchText.isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        return false;
                }

                // Action filter
                if (!actionFilter.equals("All Actions")) {
                    String action = entry.getStringValue(2); // Column 2 is Action
                    if (!action.equals(actionFilter)) {
                        return false;
                    }
                }

                return true;
            }
        };

        sorter.setRowFilter(rf);
    }

    private void clearFilters() {
        searchField.setText("");
        actionFilterCombo.setSelectedIndex(0);
        dateRangeCombo.setSelectedIndex(0);
        loadAuditLogs();
    }

    private void cleanupOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will delete all audit logs older than 6 months.\nAre you sure?",
                "Confirm Cleanup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int deleted = AuditLogDAO.deleteOldLogs(180); // 6 months = ~180 days
            UIHelper.showSuccessMessage(this, "Deleted " + deleted + " old log entries.");
            loadAuditLogs();
        }
    }
}
