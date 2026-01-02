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
        setLayout(new BorderLayout(20, 20));
        setBackground(com.college.utils.ModernTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Welcome Panel
        JPanel welcomePanel = createWelcomePanel(username);

        // Stats Panel
        JPanel statsPanel = createStatsPanel();

        // Bottom Panel (Activity Feed + Alerts)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createActivityFeedPanel());
        bottomPanel.add(createAlertsPanel());

        add(welcomePanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createWelcomePanel(String username) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, com.college.utils.ModernTheme.PRIMARY,
                    getWidth(), 0, com.college.utils.ModernTheme.PRIMARY_DARK
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel welcomeLabel = new JLabel("Welcome back, " + username + "!");
        welcomeLabel.setFont(com.college.utils.ModernTheme.FONT_TITLE);
        welcomeLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel(role + " Dashboard");
        roleLabel.setFont(com.college.utils.ModernTheme.FONT_BODY);
        roleLabel.setForeground(new Color(199, 210, 254));

        JPanel textPanel = new JPanel(new BorderLayout(0, 8));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel, BorderLayout.NORTH);
        textPanel.add(roleLabel, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Modern color palette for stats
        Color[] colors = {
            com.college.utils.ModernTheme.PRIMARY,
            com.college.utils.ModernTheme.SUCCESS,
            com.college.utils.ModernTheme.CHART_5,
            com.college.utils.ModernTheme.WARNING,
            com.college.utils.ModernTheme.INFO,
            com.college.utils.ModernTheme.DANGER,
            new Color(20, 184, 166),
            com.college.utils.ModernTheme.TEXT_SECONDARY
        };

        // Get stats based on role
        if (role.equals("ADMIN") || role.equals("FACULTY")) {
            panel.add(createStatCard("Students", getStudentCount(), colors[0]));
            panel.add(createStatCard("Faculty", getFacultyCount(), colors[1]));
            panel.add(createStatCard("Courses", getCourseCount(), colors[2]));
            panel.add(createStatCard("Books", getBookCount(), colors[3]));

            panel.add(createStatCard("Pending Passes", getPendingGatePasses(), colors[4]));
            panel.add(createStatCard("Pending Fees", getPendingFeesCount(), colors[5]));
            panel.add(createStatCard("Issued Books", getIssuedBooksCount(), colors[6]));
            panel.add(createStatCard("Hostel Rooms", getHostelRoomsCount(), colors[7]));
        } else if (role.equals("WARDEN")) {
            // Warden View
            panel.add(createStatCard("Hostel Students", getHostelStudentCount(), colors[0]));
            panel.add(createStatCard("Available Beds", getAvailableBedsCount(), colors[1]));
            panel.add(createStatCard("Pending Passes", getPendingGatePasses(), colors[4]));
            panel.add(createStatCard("Alerts", "View", colors[5]));
        } else {
            // Student view
            panel.add(createStatCard("My Courses", getStudentCourses(), colors[0]));
            panel.add(createStatCard("Attendance", getMyAttendance() + "%", colors[1]));
            panel.add(createStatCard("Fee Status", getMyFeeStatus(), colors[4]));
            panel.add(createStatCard("Books Issued", getMyIssuedBooks(), colors[2]));

            panel.add(createStatCard("Gate Passes", getMyGatePasses(), colors[3]));
            panel.add(createStatCard("Hostel", getMyHostelStatus(), colors[6]));
            panel.add(createStatCard("Grades", "View Reports", colors[5]));
            panel.add(createStatCard("Timetable", "View Schedule", colors[7]));
        }

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Soft shadow
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Card background with subtle border
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Subtle border line
                g2.setColor(new Color(226, 232, 240)); // Slate-200
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                // Top accent bar (full width)
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth() - 2, 4, 4, 4);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(com.college.utils.ModernTheme.FONT_SMALL);
        titleLabel.setForeground(com.college.utils.ModernTheme.TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(com.college.utils.ModernTheme.TEXT_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivityFeedPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Subtle border
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Recent Activity");
        headerLabel.setFont(com.college.utils.ModernTheme.FONT_HEADING);
        headerLabel.setForeground(com.college.utils.ModernTheme.TEXT_PRIMARY);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(listModel);
        activityList.setFont(com.college.utils.ModernTheme.FONT_BODY);
        activityList.setBackground(Color.WHITE);

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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Subtle border
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Announcements & Alerts");
        headerLabel.setFont(com.college.utils.ModernTheme.FONT_HEADING);
        headerLabel.setForeground(com.college.utils.ModernTheme.TEXT_PRIMARY);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> alertsList = new JList<>(listModel);
        alertsList.setFont(com.college.utils.ModernTheme.FONT_BODY);
        alertsList.setBackground(Color.WHITE);
        alertsList.setFont(new Font("Arial", Font.PLAIN, 12));

        // Fetch announcements from database
        try {
            com.college.dao.AnnouncementDAO announcementDAO = new com.college.dao.AnnouncementDAO();
            List<com.college.models.Announcement> announcements = announcementDAO.getActiveAnnouncements(role);

            for (com.college.models.Announcement announcement : announcements) {
                String entry = String.format("%s %s: %s",
                        announcement.getPriorityIcon(),
                        announcement.getTitle(),
                        announcement.getContent().length() > 60
                                ? announcement.getContent().substring(0, 60) + "..."
                                : announcement.getContent());
                listModel.addElement(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add role-based alerts
        if (role.equals("ADMIN") || role.equals("FACULTY")) {
            int pendingPasses = getPendingGatePassesInt();
            if (pendingPasses > 0) {
                listModel.addElement("[!] " + pendingPasses + " gate pass requests pending approval");
            }

            int pendingFees = getPendingFeesCountInt();
            if (pendingFees > 0) {
                listModel.addElement("[i] " + pendingFees + " students have pending fees");
            }

            int issuedBooks = getIssuedBooksCountInt();
            if (issuedBooks > 0) {
                listModel.addElement("[i] " + issuedBooks + " books currently issued");
            }
        } else {
            // Student reminders
            listModel.addElement("[i] Check your attendance regularly");
            listModel.addElement("[i] View timetable for class schedule");
            listModel.addElement("[i] Pay fees before due date");
        }

        if (listModel.isEmpty()) {
            listModel.addElement("No announcements or alerts at this time");
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
