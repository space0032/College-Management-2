package com.college.ui.gatepass;

import com.college.dao.GatePassDAO;
import com.college.dao.AuditLogDAO;
import com.college.models.GatePass;
import com.college.utils.SessionManager;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student Gate Pass Request Panel
 * Allows students to request gate passes and view their history
 */
public class GatePassRequestPanel extends JPanel {

    private int userId;
    private int studentId;
    private JTable passTable;
    private DefaultTableModel tableModel;

    public GatePassRequestPanel(int userId) {
        this.userId = userId;
        this.studentId = getUserStudentId();
        initComponents();
        loadGatePasses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Gate Pass Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton newRequestButton = UIHelper.createPrimaryButton("New Request");
        newRequestButton.setPreferredSize(new Dimension(150, 35));
        newRequestButton.addActionListener(e -> showNewRequestDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(newRequestButton, BorderLayout.EAST);

        // Table
        String[] columnNames = { "ID", "From Date", "To Date", "Reason", "Destination",
                "Status", "Requested At", "Approved By", "Comment" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        passTable = new JTable(tableModel);
        passTable.setRowHeight(25);
        passTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        passTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        passTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        passTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        passTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        passTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        passTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(passTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> loadGatePasses());

        buttonPanel.add(refreshButton);

        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showNewRequestDialog() {
        if (!isHostelite) {
            UIHelper.showErrorMessage(this, "Only hostel students can request gate passes!");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "New Gate Pass Request", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // From Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("From Date:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> fromDayCombo = createDayComboBox();
        JComboBox<String> fromMonthCombo = createMonthComboBox();
        JComboBox<String> fromYearCombo = createYearComboBox();
        JPanel fromDatePanel = createDatePanel(fromDayCombo, fromMonthCombo, fromYearCombo);
        formPanel.add(fromDatePanel, gbc);

        // To Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("To Date:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> toDayCombo = createDayComboBox();
        JComboBox<String> toMonthCombo = createMonthComboBox();
        JComboBox<String> toYearCombo = createYearComboBox();
        JPanel toDatePanel = createDatePanel(toDayCombo, toMonthCombo, toYearCombo);
        formPanel.add(toDatePanel, gbc);

        // Reason
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(UIHelper.createLabel("Reason:"), gbc);

        gbc.gridx = 1;
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        formPanel.add(reasonScroll, gbc);

        // Destination
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(UIHelper.createLabel("Destination:"), gbc);

        gbc.gridx = 1;
        JTextField destinationField = UIHelper.createTextField(20);
        formPanel.add(destinationField, gbc);

        // Parent Contact
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(UIHelper.createLabel("Parent Contact:"), gbc);

        gbc.gridx = 1;
        JTextField contactField = UIHelper.createTextField(20);
        formPanel.add(contactField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton submitButton = UIHelper.createPrimaryButton("Submit Request");
        submitButton.setPreferredSize(new Dimension(150, 35));
        submitButton.addActionListener(e -> {
            if (submitGatePassRequest(fromDayCombo, fromMonthCombo, fromYearCombo,
                    toDayCombo, toMonthCombo, toYearCombo,
                    reasonArea, destinationField, contactField)) {
                dialog.dispose();
                loadGatePasses();
            }
        });

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createDatePanel(JComboBox<String> day, JComboBox<String> month, JComboBox<String> year) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);
        panel.add(day);
        panel.add(new JLabel("/"));
        panel.add(month);
        panel.add(new JLabel("/"));
        panel.add(year);
        return panel;
    }

    private JComboBox<String> createDayComboBox() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        return new JComboBox<>(days);
    }

    private JComboBox<String> createMonthComboBox() {
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.format("%02d", i + 1);
        }
        return new JComboBox<>(months);
    }

    private JComboBox<String> createYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[3];
        for (int i = 0; i < 3; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        return new JComboBox<>(years);
    }

    private boolean submitGatePassRequest(JComboBox<String> fromDay, JComboBox<String> fromMonth,
            JComboBox<String> fromYear, JComboBox<String> toDay,
            JComboBox<String> toMonth, JComboBox<String> toYear,
            JTextArea reasonArea, JTextField destinationField,
            JTextField contactField) {
        try {
            LocalDate fromDate = LocalDate.of(
                    Integer.parseInt((String) fromYear.getSelectedItem()),
                    Integer.parseInt((String) fromMonth.getSelectedItem()),
                    Integer.parseInt((String) fromDay.getSelectedItem()));

            LocalDate toDate = LocalDate.of(
                    Integer.parseInt((String) toYear.getSelectedItem()),
                    Integer.parseInt((String) toMonth.getSelectedItem()),
                    Integer.parseInt((String) toDay.getSelectedItem()));

            String reason = reasonArea.getText().trim();
            String destination = destinationField.getText().trim();
            String contact = contactField.getText().trim();

            // Validation
            if (reason.isEmpty() || destination.isEmpty() || contact.isEmpty()) {
                UIHelper.showErrorMessage(this, "All fields are required!");
                return false;
            }

            if (fromDate.isBefore(LocalDate.now())) {
                UIHelper.showErrorMessage(this, "From date cannot be in the past!");
                return false;
            }

            if (toDate.isBefore(fromDate)) {
                UIHelper.showErrorMessage(this, "To date must be after from date!");
                return false;
            }

            // Create gate pass
            GatePass gatePass = new GatePass(studentId, fromDate, toDate, reason, destination, contact);

            if (GatePassDAO.createRequest(gatePass)) {
                // Log action
                SessionManager session = SessionManager.getInstance();
                AuditLogDAO.logAction(session.getUserId(), session.getUsername(),
                        "CREATE_GATE_PASS", "GATE_PASS", studentId,
                        "Requested gate pass from " + fromDate + " to " + toDate);

                UIHelper.showSuccessMessage(this, "Gate pass request submitted successfully!");
                return true;
            } else {
                UIHelper.showErrorMessage(this, "Failed to submit request. Please try again.");
                return false;
            }

        } catch (Exception ex) {
            UIHelper.showErrorMessage(this, "Invalid date format!");
            return false;
        }
    }

    private void loadGatePasses() {
        tableModel.setRowCount(0);

        List<GatePass> passes = GatePassDAO.getStudentPasses(studentId);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (GatePass pass : passes) {
            Object[] row = {
                    pass.getId(),
                    pass.getFromDate().format(dateFormatter),
                    pass.getToDate().format(dateFormatter),
                    pass.getReason().length() > 30 ? pass.getReason().substring(0, 27) + "..." : pass.getReason(),
                    pass.getDestination(),
                    getStatusWithIcon(pass.getStatus()),
                    pass.getRequestedAt().format(dateTimeFormatter),
                    pass.getApprovedByName() != null ? pass.getApprovedByName() : "-",
                    pass.getApprovalComment() != null ? pass.getApprovalComment() : "-"
            };
            tableModel.addRow(row);
        }
    }

    private String getStatusWithIcon(String status) {
        switch (status) {
            case "APPROVED":
                return "✅ APPROVED";
            case "REJECTED":
                return "❌ REJECTED";
            case "PENDING":
                return "⏳ PENDING";
            default:
                return status;
        }
    }

    private boolean isHostelite = false;

    private int getUserStudentId() {
        try {
            java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, is_hostelite FROM students WHERE user_id = ?");
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                this.isHostelite = rs.getBoolean("is_hostelite");
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
