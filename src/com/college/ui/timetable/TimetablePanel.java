package com.college.ui.timetable;

import com.college.dao.TimetableDAO;
import com.college.dao.StudentDAO;
import com.college.models.Timetable;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Enhanced Timetable Panel
 * Shows weekly schedule filtered by department and semester
 * Students see their own department/semester automatically
 * Admin/Faculty can select department and semester
 */
public class TimetablePanel extends JPanel {

    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private String userRole;
    private int userId;
    private JComboBox<String> departmentCombo;
    private JComboBox<Integer> semesterCombo;
    private TimetableDAO timetableDAO;
    private StudentDAO studentDAO;
    private String currentDepartment;
    private int currentSemester;

    private static final String[] TIME_SLOTS = {
            "9:00 - 10:00",
            "10:00 - 11:00",
            "11:00 - 12:00",
            "12:00 - 1:00",
            "1:00 - 2:00",
            "2:00 - 3:00",
            "3:00 - 4:00"
    };

    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };

    public TimetablePanel(String role) {
        this(role, 0);
    }

    public TimetablePanel(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.timetableDAO = new TimetableDAO();
        this.studentDAO = new StudentDAO();
        initComponents();
        loadTimetable();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel with title and filters
        JPanel topPanel = createTopPanel();

        // Table Panel
        JPanel tablePanel = createTablePanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Title
        JLabel titleLabel = new JLabel("Weekly Timetable");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterPanel.setBackground(Color.WHITE);

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            // Admin/Faculty can select department and semester
            filterPanel.add(new JLabel("Department:"));
            departmentCombo = new JComboBox<>();
            loadDepartments();
            departmentCombo.setPreferredSize(new Dimension(200, 30));
            departmentCombo.addActionListener(e -> loadTimetable());
            filterPanel.add(departmentCombo);

            filterPanel.add(new JLabel("Semester:"));
            semesterCombo = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });
            semesterCombo.setPreferredSize(new Dimension(80, 30));
            semesterCombo.addActionListener(e -> loadTimetable());
            filterPanel.add(semesterCombo);

            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> loadTimetable());
            filterPanel.add(refreshButton);
        } else {
            // Student sees their department and semester (read-only)
            Student student = getStudentInfo();
            if (student != null) {
                currentDepartment = student.getDepartment();
                currentSemester = student.getSemester();

                JLabel infoLabel = new JLabel(String.format("Department: %s | Semester: %d",
                        currentDepartment != null ? currentDepartment : "General",
                        currentSemester));
                infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
                filterPanel.add(infoLabel);
            } else {
                currentDepartment = "General";
                currentSemester = 1;
            }
        }

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(filterPanel, BorderLayout.CENTER);

        panel.add(titlePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Time column never editable
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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            JLabel infoLabel = new JLabel("Click cells to edit schedule");
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            infoLabel.setForeground(new Color(127, 140, 141));

            JButton saveButton = UIHelper.createPrimaryButton("Save Changes");
            saveButton.setPreferredSize(new Dimension(150, 35));
            saveButton.addActionListener(e -> saveTimetable());

            panel.add(infoLabel);
            panel.add(Box.createHorizontalStrut(20));
            panel.add(saveButton);
        } else {
            JLabel infoLabel = new JLabel("View-only timetable");
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            infoLabel.setForeground(new Color(127, 140, 141));
            panel.add(infoLabel);
        }

        return panel;
    }

    private void loadDepartments() {
        List<String> departments = timetableDAO.getAllDepartments();
        for (String dept : departments) {
            departmentCombo.addItem(dept);
        }
        if (departmentCombo.getItemCount() > 0) {
            departmentCombo.setSelectedIndex(0);
        }
    }

    private Student getStudentInfo() {
        try {
            // Get student by user_id
            java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id FROM students WHERE user_id = ?");
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int studentId = rs.getInt("id");
                return studentDAO.getStudentById(studentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadTimetable() {
        // Clear current table
        tableModel.setRowCount(0);

        // Determine department and semester
        if (userRole.equals("ADMIN") || userRole.equals("FACULTY")) {
            if (departmentCombo.getSelectedItem() != null) {
                currentDepartment = (String) departmentCombo.getSelectedItem();
            } else {
                currentDepartment = "General";
            }
            if (semesterCombo.getSelectedItem() != null) {
                currentSemester = (Integer) semesterCombo.getSelectedItem();
            } else {
                currentSemester = 1;
            }
        }

        // Load timetable from database
        List<Timetable> timetableEntries = timetableDAO.getTimetableByDepartmentAndSemester(
                currentDepartment, currentSemester);

        // Create a map for quick lookup
        java.util.Map<String, String> scheduleMap = new java.util.HashMap<>();
        for (Timetable entry : timetableEntries) {
            String key = entry.getTimeSlot() + "|" + entry.getDayOfWeek();
            String value = entry.getSubject();
            if (entry.getFacultyName() != null && !entry.getFacultyName().isEmpty()) {
                value += "\n" + entry.getFacultyName();
            }
            if (entry.getRoomNumber() != null && !entry.getRoomNumber().isEmpty()) {
                value += "\n" + entry.getRoomNumber();
            }
            scheduleMap.put(key, value);
        }

        // Populate table
        for (String timeSlot : TIME_SLOTS) {
            Object[] row = new Object[6];
            row[0] = timeSlot;

            for (int i = 0; i < DAYS.length; i++) {
                String key = timeSlot + "|" + DAYS[i];
                row[i + 1] = scheduleMap.getOrDefault(key, "");
            }

            tableModel.addRow(row);
        }
    }

    private void saveTimetable() {
        if (!userRole.equals("ADMIN") && !userRole.equals("FACULTY")) {
            return; // Students cannot save
        }

        try {
            // Save all cells
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                String timeSlot = (String) tableModel.getValueAt(row, 0);

                for (int col = 1; col < tableModel.getColumnCount(); col++) {
                    String dayOfWeek = DAYS[col - 1];
                    String cellValue = (String) tableModel.getValueAt(row, col);

                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        // Parse cell value (format: Subject\nFaculty\nRoom)
                        String[] parts = cellValue.split("\n");
                        String subject = parts.length > 0 ? parts[0].trim() : "";
                        String faculty = parts.length > 1 ? parts[1].trim() : "";
                        String room = parts.length > 2 ? parts[2].trim() : "";

                        Timetable entry = new Timetable();
                        entry.setDepartment(currentDepartment);
                        entry.setSemester(currentSemester);
                        entry.setDayOfWeek(dayOfWeek);
                        entry.setTimeSlot(timeSlot);
                        entry.setSubject(subject);
                        entry.setFacultyName(faculty);
                        entry.setRoomNumber(room);

                        timetableDAO.saveTimetableEntry(entry);
                    }
                }
            }

            UIHelper.showSuccessMessage(this, "Timetable saved successfully!");
            loadTimetable(); // Reload to show saved data

        } catch (Exception e) {
            e.printStackTrace();
            UIHelper.showErrorMessage(this, "Error saving timetable: " + e.getMessage());
        }
    }
}
