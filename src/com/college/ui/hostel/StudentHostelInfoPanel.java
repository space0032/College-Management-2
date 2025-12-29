package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.models.HostelAllocation;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student Hostel Info Panel
 * Shows hostel allocation details for students
 */
public class StudentHostelInfoPanel extends JPanel {

    private HostelDAO hostelDAO;
    private int studentId;

    public StudentHostelInfoPanel(int studentId) {
        this.studentId = studentId;
        this.hostelDAO = new HostelDAO();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("My Hostel Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        topPanel.add(titleLabel, BorderLayout.WEST);

        // Info Panel
        JPanel infoPanel = createInfoPanel();

        add(topPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Get student's active allocation
        List<HostelAllocation> allocations = hostelDAO.getAllocationsByStudent(studentId);
        HostelAllocation activeAllocation = null;

        for (HostelAllocation allocation : allocations) {
            if ("ACTIVE".equals(allocation.getStatus())) {
                activeAllocation = allocation;
                break;
            }
        }

        if (activeAllocation != null) {
            // Show allocation details
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel statusLabel = new JLabel("✅ You are allocated to a hostel room");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
            statusLabel.setForeground(UIHelper.SUCCESS_COLOR);
            panel.add(statusLabel, gbc);

            gbc.gridwidth = 1;
            gbc.gridy = 1;
            addInfoRow(panel, "Hostel:", activeAllocation.getHostelName(), gbc);

            gbc.gridy = 2;
            addInfoRow(panel, "Room Number:", activeAllocation.getRoomNumber(), gbc);

            gbc.gridy = 3;
            addInfoRow(panel, "Check-in Date:", sdf.format(activeAllocation.getCheckInDate()), gbc);

            if (activeAllocation.getRemarks() != null && !activeAllocation.getRemarks().isEmpty()) {
                gbc.gridy = 4;
                addInfoRow(panel, "Remarks:", activeAllocation.getRemarks(), gbc);
            }

        } else {
            // No allocation
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;

            JPanel noAllocPanel = new JPanel();
            noAllocPanel.setBackground(new Color(236, 240, 241));
            noAllocPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            noAllocPanel.setLayout(new BoxLayout(noAllocPanel, BoxLayout.Y_AXIS));

            JLabel icon = new JLabel("🏠");
            icon.setFont(new Font("Arial", Font.PLAIN, 48));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel msg1 = new JLabel("No Hostel Allocation");
            msg1.setFont(new Font("Arial", Font.BOLD, 18));
            msg1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel msg2 = new JLabel("You are not currently allocated to any hostel room.");
            msg2.setFont(new Font("Arial", Font.PLAIN, 14));
            msg2.setForeground(Color.GRAY);
            msg2.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel msg3 = new JLabel("Please contact the hostel office for room allocation.");
            msg3.setFont(new Font("Arial", Font.PLAIN, 14));
            msg3.setForeground(Color.GRAY);
            msg3.setAlignmentX(Component.CENTER_ALIGNMENT);

            noAllocPanel.add(icon);
            noAllocPanel.add(Box.createVerticalStrut(10));
            noAllocPanel.add(msg1);
            noAllocPanel.add(Box.createVerticalStrut(5));
            noAllocPanel.add(msg2);
            noAllocPanel.add(Box.createVerticalStrut(5));
            noAllocPanel.add(msg3);

            panel.add(noAllocPanel, gbc);
        }

        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(valueComp, gbc);
    }
}
