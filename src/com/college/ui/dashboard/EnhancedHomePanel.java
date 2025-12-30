package com.college.ui.dashboard;

import com.college.dao.*;
import com.college.models.AuditLog;

import com.college.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced Home Panel
 * Displays quick stats, recent activity, and alerts
 */
public class EnhancedHomePanel extends JPanel {

    private String role;
    private int userId;

    public EnhancedHomePanel(String username, String role, int userId) {
        this.role = role;
        this.userId = userId;
        initComponents(username);
    }

    private void initComponents(String username) {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome Panel
        JPanel welcomePanel = createWelcomePanel(username);

        // Stats Panel
        JPanel statsPanel = createStatsPanel();

        // Bottom Panel (Activity Feed + Alerts)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(createActivityFeedPanel());
        bottomPanel.add(createAlertsPanel());

        add(welcomePanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createWelcomePanel(String username) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("Role: " + role);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        roleLabel.setForeground(new Color(236, 240, 241));

        JPanel textPanel = new JPanel(new BorderLayout(0, 5));
        textPanel.setBackground(new Color(41, 128, 185));
        textPanel.add(welcomeLabel, BorderLayout.NORTH);
        textPanel.add(roleLabel, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Quick Statistics"));

        // Get stats based on role
        if (role.equals("ADMIN") || role.equals("FACULTY")) {
            panel.add(createStatCard("👥 Students", getStudentCount(), new Color(52, 152, 219)));
            panel.add(createStatCard("👨‍🏫 Faculty", getFacultyCount(), new Color(46, 204, 113)));
            panel.add(createStatCard("📚 Courses", getCourseCount(), new Color(155, 89, 182)));
            panel.add(createStatCard("📖 Books", getBookCount(), new Color(230, 126, 34)));

            panel.add(createStatCard("🎫 Pending Passes", getPendingGatePasses(), new Color(241, 196, 15)));
            panel.add(createStatCard("💰 Pending Fees", getPendingFeesCount(), new Color(231, 76, 60)));
            panel.add(createStatCard("📕 Issued Books", getIssuedBooksCount(), new Color(26, 188, 156)));
            panel.add(createStatCard("🏠 Hostel Rooms", getHostelRoomsCount(), new Color(149, 165, 166)));
        } else if (role.equals("WARDEN")) {
            // Warden View
            panel.add(createStatCard("🏠 Hostel Students", getHostelStudentCount(), new Color(52, 152, 219)));
            panel.add(createStatCard("🛏️ Available Beds", getAvailableBedsCount(), new Color(46, 204, 113)));
            panel.add(createStatCard("🎫 Pending Passes", getPendingGatePasses(), new Color(241, 196, 15)));
            panel.add(createStatCard("⚠️ Alerts", "View", new Color(231, 76, 60)));
        } else {
            // Student view
            panel.add(createStatCard("📚 My Courses", getStudentCourses(), new Color(52, 152, 219)));
            panel.add(createStatCard("📊 Attendance", getMyAttendance() + "%", new Color(46, 204, 113)));
            panel.add(createStatCard("💰 Fee Status", getMyFeeStatus(), new Color(241, 196, 15)));
            panel.add(createStatCard("📖 Books Issued", getMyIssuedBooks(), new Color(155, 89, 182)));

            panel.add(createStatCard("🎫 Gate Passes", getMyGatePasses(), new Color(230, 126, 34)));
            panel.add(createStatCard("🏠 Hostel", getMyHostelStatus(), new Color(26, 188, 156)));
            panel.add(createStatCard("📝 Grades", "View Reports", new Color(231, 76, 60)));
            panel.add(createStatCard("⏰ Timetable", "View Schedule", new Color(149, 165, 166)));
        }

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivityFeedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(listModel);
        activityList.setFont(new Font("Arial", Font.PLAIN, 12));

        // Get recent audit logs FOR THIS USER ONLY
        String currentUsername = SessionManager.getInstance().getUsername();
        List<AuditLog> recentLogs = AuditLogDAO.getLogsByUser(currentUsername, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (AuditLog log : recentLogs) {
            String entry = String.format("%s - %s",
                    log.getTimestamp().format(formatter),
                    log.getAction().replace("_", " "));
            listModel.addElement(entry);
        }

        if (recentLogs.isEmpty()) {
            listModel.addElement("No recent activity");
        }

        JScrollPane scrollPane = new JScrollPane(activityList);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Alerts & Notifications"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> alertsList = new JList<>(listModel);
        alertsList.setFont(new Font("Arial", Font.PLAIN, 12));

        // Add alerts based on role
        if (role.equals("ADMIN") || role.equals("FACULTY")) {
            int pendingPasses = getPendingGatePassesInt();
            if (pendingPasses > 0) {
                listModel.addElement("⚠️ " + pendingPasses + " gate pass requests pending approval");
            }

            int pendingFees = getPendingFeesCountInt();
            if (pendingFees > 0) {
                listModel.addElement("💰 " + pendingFees + " students have pending fees");
            }

            int issuedBooks = getIssuedBooksCountInt();
            if (issuedBooks > 0) {
                listModel.addElement("📕 " + issuedBooks + " books currently issued");
            }
        } else {
            // Student alerts
            listModel.addElement("ℹ️ Check your attendance regularly");
            listModel.addElement("ℹ️ View timetable for class schedule");
            listModel.addElement("ℹ️ Pay fees before due date");
        }

        if (listModel.isEmpty()) {
            listModel.addElement("✅ No alerts at this time");
        }

        JScrollPane scrollPane = new JScrollPane(alertsList);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Stats calculation methods
    private String getStudentCount() {
        try {
            StudentDAO dao = new StudentDAO();
            return String.valueOf(dao.getAllStudents().size());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getFacultyCount() {
        try {
            FacultyDAO dao = new FacultyDAO();
            return String.valueOf(dao.getAllFaculty().size());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getCourseCount() {
        try {
            CourseDAO dao = new CourseDAO();
            return String.valueOf(dao.getAllCourses().size());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getBookCount() {
        try {
            LibraryDAO dao = new LibraryDAO();
            return String.valueOf(dao.getAllBooks().size());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getPendingGatePasses() {
        return String.valueOf(getPendingGatePassesInt());
    }

    private int getPendingGatePassesInt() {
        try {
            return GatePassDAO.getPassesByStatus("PENDING").size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getPendingFeesCount() {
        return String.valueOf(getPendingFeesCountInt());
    }

    private int getPendingFeesCountInt() {
        try {
            EnhancedFeeDAO dao = new EnhancedFeeDAO();
            return dao.getPendingFees().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getIssuedBooksCount() {
        return String.valueOf(getIssuedBooksCountInt());
    }

    private int getIssuedBooksCountInt() {
        try {
            BookIssueDAO dao = new BookIssueDAO();
            return dao.getAllIssuedBooks().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getHostelRoomsCount() {
        try {
            HostelDAO dao = new HostelDAO();
            return String.valueOf(dao.getTotalAvailableCapacity());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getHostelStudentCount() {
        try {
            HostelDAO dao = new HostelDAO();
            return String.valueOf(dao.getTotalHostelStudents());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getAvailableBedsCount() {
        try {
            HostelDAO dao = new HostelDAO();
            return String.valueOf(dao.getTotalAvailableCapacity());
        } catch (Exception e) {
            return "N/A";
        }
    }

    // Student-specific stats
    private String getStudentCourses() {
        try {
            // Get student's enrolled courses
            return "View";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getMyAttendance() {
        try {
            AttendanceDAO dao = new AttendanceDAO();
            // Simplified - would need actual calculation
            return "View";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getMyFeeStatus() {
        try {
            EnhancedFeeDAO dao = new EnhancedFeeDAO();
            int studentId = getStudentIdFromUserId();
            if (studentId > 0) {
                List<com.college.models.StudentFee> fees = dao.getStudentFees(studentId);
                boolean allPaid = fees.stream().allMatch(f -> "PAID".equals(f.getStatus()));
                return allPaid ? "✅ Paid" : "⚠️ Pending";
            }
            return "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getMyIssuedBooks() {
        try {
            BookIssueDAO dao = new BookIssueDAO();
            int studentId = getStudentIdFromUserId();
            if (studentId > 0) {
                return String.valueOf(dao.getIssuedBooksByStudent(studentId).size());
            }
            return "0";
        } catch (Exception e) {
            return "0";
        }
    }

    private String getMyGatePasses() {
        try {
            int studentId = getStudentIdFromUserId();
            if (studentId > 0) {
                return String.valueOf(GatePassDAO.getStudentPasses(studentId).size());
            }
            return "0";
        } catch (Exception e) {
            return "0";
        }
    }

    private String getMyHostelStatus() {
        try {
            // Simplified
            return "View";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private int getStudentIdFromUserId() {
        try {
            java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id FROM students WHERE user_id = ?");
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
