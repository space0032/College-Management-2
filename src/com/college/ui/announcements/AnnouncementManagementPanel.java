package com.college.ui.announcements;

import com.college.dao.AnnouncementDAO;
import com.college.models.Announcement;
import com.college.utils.SessionManager;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Announcement Management Panel
 * Allows Admin and Faculty to create, edit, and delete announcements
 */
public class AnnouncementManagementPanel extends JPanel {

    private JTable announcementTable;
    private DefaultTableModel tableModel;
    private AnnouncementDAO announcementDAO;
    private String userRole;
    private int userId;

    public AnnouncementManagementPanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        announcementDAO = new AnnouncementDAO();
        initComponents();
        loadAnnouncements();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Announcement Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JButton refreshButton = UIHelper.createSuccessButton("Refresh");
        refreshButton.addActionListener(e -> loadAnnouncements());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = UIHelper.createSuccessButton("Add Announcement");
        addButton.setPreferredSize(new Dimension(200, 40));
        addButton.addActionListener(e -> addAnnouncement());
        buttonPanel.add(addButton);

        JButton editButton = UIHelper.createPrimaryButton("Edit");
        editButton.setPreferredSize(new Dimension(150, 40));
        editButton.addActionListener(e -> editAnnouncement());
        buttonPanel.add(editButton);

        JButton deleteButton = UIHelper.createDangerButton("Delete");
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.addActionListener(e -> deleteAnnouncement());
        buttonPanel.add(deleteButton);

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = {"ID", "Title", "Target", "Priority", "Created By", "Created At", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        announcementTable = new JTable(tableModel);
        UIHelper.styleTable(announcementTable);

        JScrollPane scrollPane = new JScrollPane(announcementTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadAnnouncements() {
        tableModel.setRowCount(0);
        List<Announcement> announcements = announcementDAO.getAllAnnouncements();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Announcement announcement : announcements) {
            String createdAt = announcement.getCreatedAt() != null
                    ? announcement.getCreatedAt().format(formatter)
                    : "N/A";

            Object[] row = {
                    announcement.getId(),
                    announcement.getTitle(),
                    announcement.getTargetAudience(),
                    announcement.getPriorityIcon() + " " + announcement.getPriority(),
                    announcement.getCreatedByName() != null ? announcement.getCreatedByName() : "Unknown",
                    createdAt,
                    announcement.isActive() ? "Yes" : "No"
            };
            tableModel.addRow(row);
        }
    }

    private void addAnnouncement() {
        // Create form panel
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField titleField = new JTextField();
        JTextArea contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        String[] audiences = {"ALL", "STUDENTS", "FACULTY", "STUDENTS_FACULTY"};
        JComboBox<String> audienceCombo = new JComboBox<>(audiences);

        String[] priorities = {"LOW", "NORMAL", "HIGH", "URGENT"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedItem("NORMAL");

        JCheckBox activeCheck = new JCheckBox("Active", true);

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Content:"));
        panel.add(contentScroll);
        panel.add(new JLabel("Target Audience:"));
        panel.add(audienceCombo);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Status:"));
        panel.add(activeCheck);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Announcement announcement = new Announcement();
                announcement.setTitle(titleField.getText().trim());
                announcement.setContent(contentArea.getText().trim());
                announcement.setTargetAudience((String) audienceCombo.getSelectedItem());
                announcement.setPriority((String) priorityCombo.getSelectedItem());
                announcement.setCreatedBy(userId);
                announcement.setActive(activeCheck.isSelected());

                if (announcementDAO.addAnnouncement(announcement) > 0) {
                    UIHelper.showSuccessMessage(this, "Announcement created successfully!");
                    loadAnnouncements();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to create announcement!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Error: " + e.getMessage());
            }
        }
    }

    private void editAnnouncement() {
        int selectedRow = announcementTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select an announcement to edit!");
            return;
        }

        int announcementId = (int) tableModel.getValueAt(selectedRow, 0);
        Announcement announcement = announcementDAO.getAnnouncementById(announcementId);

        if (announcement == null) {
            UIHelper.showErrorMessage(this, "Announcement not found!");
            return;
        }

        // Create edit dialog
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField titleField = new JTextField(announcement.getTitle());
        JTextArea contentArea = new JTextArea(announcement.getContent(), 3, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        String[] audiences = {"ALL", "STUDENTS", "FACULTY", "STUDENTS_FACULTY"};
        JComboBox<String> audienceCombo = new JComboBox<>(audiences);
        audienceCombo.setSelectedItem(announcement.getTargetAudience());

        String[] priorities = {"LOW", "NORMAL", "HIGH", "URGENT"};
        JComboBox<String> priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedItem(announcement.getPriority());

        JCheckBox activeCheck = new JCheckBox("Active", announcement.isActive());

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Content:"));
        panel.add(contentScroll);
        panel.add(new JLabel("Target Audience:"));
        panel.add(audienceCombo);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Status:"));
        panel.add(activeCheck);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Announcement",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                announcement.setTitle(titleField.getText().trim());
                announcement.setContent(contentArea.getText().trim());
                announcement.setTargetAudience((String) audienceCombo.getSelectedItem());
                announcement.setPriority((String) priorityCombo.getSelectedItem());
                announcement.setActive(activeCheck.isSelected());

                if (announcementDAO.updateAnnouncement(announcement)) {
                    UIHelper.showSuccessMessage(this, "Announcement updated successfully!");
                    loadAnnouncements();
                } else {
                    UIHelper.showErrorMessage(this, "Failed to update announcement!");
                }
            } catch (Exception e) {
                UIHelper.showErrorMessage(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteAnnouncement() {
        int selectedRow = announcementTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showErrorMessage(this, "Please select an announcement to delete!");
            return;
        }

        int announcementId = (int) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete announcement '" + title + "'?\\nThis action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (announcementDAO.deleteAnnouncement(announcementId)) {
                UIHelper.showSuccessMessage(this, "Announcement deleted successfully!");
                loadAnnouncements();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete announcement!");
            }
        }
    }
}
