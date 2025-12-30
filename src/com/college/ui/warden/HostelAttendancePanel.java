package com.college.ui.warden;

import com.college.dao.HostelDAO;
import com.college.dao.WardenDAO;
import com.college.models.Warden;
import com.college.models.HostelAllocation;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Warden's view of Hostel Attendance
 */
public class HostelAttendancePanel extends JPanel {

    private int userId;
    private Warden warden;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public HostelAttendancePanel(int userId) {
        this.userId = userId;
        loadWardenDetails();
        initComponents();
    }

    private void loadWardenDetails() {
        WardenDAO wardenDAO = new WardenDAO();
        this.warden = wardenDAO.getWardenByUserId(userId);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Hostel Attendance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JLabel hostelLabel = new JLabel(warden != null ? "Hostel: " + warden.getHostelName() : "No Hostel Assigned");
        hostelLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        hostelLabel.setForeground(Color.GRAY);

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setBackground(Color.WHITE);
        titleBox.add(titleLabel);
        titleBox.add(hostelLabel);

        JButton markButton = UIHelper.createSuccessButton("Mark Today's Attendance");
        markButton.addActionListener(e -> markAttendance());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(markButton, BorderLayout.EAST);

        // Placeholder for attendance table (could be expanded)
        JLabel placeholder = new JLabel("Attendance Module Integration Pending...", SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.PLAIN, 16));

        add(headerPanel, BorderLayout.NORTH);
        add(placeholder, BorderLayout.CENTER);
    }

    private void markAttendance() {
        JOptionPane.showMessageDialog(this, "Attendance marking feature coming soon!");
    }
}
