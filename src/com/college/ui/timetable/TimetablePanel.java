package com.college.ui.timetable;

import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Timetable Panel
 * Shows weekly schedule with editing for faculty/admin
 */
public class TimetablePanel extends JPanel {

    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private String userRole;

    public TimetablePanel(String role) {
        this.userRole = role;
        initComponents();
        loadSampleTimetable();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Weekly Timetable");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel with role-based controls
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JLabel infoLabel = new JLabel("✏️ Click cells to edit schedule");
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            infoLabel.setForeground(new Color(127, 140, 141));

            JButton saveButton = UIHelper.createPrimaryButton("Save Changes");
            saveButton.setPreferredSize(new Dimension(150, 35));
            saveButton.addActionListener(e -> saveTimetable());

            buttonPanel.add(infoLabel);
            buttonPanel.add(Box.createHorizontalStrut(20));
            buttonPanel.add(saveButton);
        } else {
            JLabel infoLabel = new JLabel("📅 View-only timetable");
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            infoLabel.setForeground(new Color(127, 140, 141));
            buttonPanel.add(infoLabel);
        }

        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Time column (0) is never editable
                if (column == 0)
                    return false;
                // Other columns editable only for ADMIN/FACULTY
                return userRole.equals("ADMIN") || userRole.equals("FACULTY");
            }
        };

        timetableTable = new JTable(tableModel);
        UIHelper.styleTable(timetableTable);
        timetableTable.setRowHeight(60);

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadSampleTimetable() {
        // Sample timetable data
        String[][] schedule = {
                { "9:00 - 10:00", "Math", "Physics", "Chemistry", "Math", "Physics" },
                { "10:00 - 11:00", "English", "Math", "Biology", "English", "Chemistry" },
                { "11:00 - 12:00", "Computer Sci", "Chemistry", "Math", "Physics", "Biology" },
                { "12:00 - 1:00", "LUNCH BREAK", "LUNCH BREAK", "LUNCH BREAK", "LUNCH BREAK", "LUNCH BREAK" },
                { "1:00 - 2:00", "Physics Lab", "English", "Computer Sci", "Chemistry", "Math" },
                { "2:00 - 3:00", "Chemistry Lab", "Biology", "Physics", "Computer Sci", "English" },
                { "3:00 - 4:00", "Library", "Sports", "Library", "Sports", "Library" }
        };

        for (String[] row : schedule) {
            tableModel.addRow(row);
        }
    }

    private void saveTimetable() {
        UIHelper.showSuccessMessage(this, "Timetable changes saved successfully!");
        // In a full implementation, this would save to database
    }
}
