package com.college.ui;

import com.college.ui.student.StudentManagementPanel;
import com.college.ui.faculty.FacultyManagementPanel;
import com.college.ui.courses.CourseManagementPanel;
import com.college.ui.library.LibraryManagementPanel;
import com.college.ui.fees.FeeManagementPanel;
import com.college.ui.attendance.AttendanceManagementPanel;
import com.college.ui.attendance.StudentAttendancePanel;
import com.college.ui.grades.GradeManagementPanel;
import com.college.ui.timetable.TimetablePanel;
import com.college.utils.UIHelper;
import com.college.utils.DatabaseConnection;
import com.college.dao.StudentDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Main Dashboard Frame
 * Displays after successful login with role-based menu
 */
public class DashboardFrame extends JFrame {

    private String username;
    private String role;
    private int userId;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public DashboardFrame(String username, String role, int userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
        initComponents();
    }

    private void initComponents() {
        setTitle("College Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Main Layout
        setLayout(new BorderLayout());

        // Top Panel with User Info
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Left Sidebar Menu
        JPanel sidebarPanel = createSidebarMenu();
        add(sidebarPanel, BorderLayout.WEST);

        // Content Panel (CardLayout for switching views)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        // Add Home Panel
        JPanel homePanel = createHomePanel();
        contentPanel.add(homePanel, "HOME");

        contentPanel.add(new StudentManagementPanel(), "STUDENTS");
        contentPanel.add(new FacultyManagementPanel(), "FACULTY");
        contentPanel.add(new CourseManagementPanel(role), "COURSES");
        contentPanel.add(new AttendanceManagementPanel(), "ATTENDANCE");
        contentPanel.add(new StudentAttendancePanel(userId), "MY_ATTENDANCE");
        contentPanel.add(new GradeManagementPanel(), "GRADES");
        contentPanel.add(new TimetablePanel(role), "TIMETABLE");
        contentPanel.add(new LibraryManagementPanel(role, userId), "LIBRARY");
        contentPanel.add(new com.college.ui.hostel.HostelManagementPanel(role, userId), "HOSTEL");
        contentPanel.add(new com.college.ui.hostel.StudentHostelInfoPanel(userId), "MY_HOSTEL");
        contentPanel.add(new com.college.ui.fees.EnhancedFeeManagementPanel(role, userId), "FEES");
        contentPanel.add(createChangePasswordPanel(), "CHANGE_PASSWORD");
        contentPanel.add(new com.college.ui.gatepass.GatePassRequestPanel(userId), "GATE_PASS");
        contentPanel.add(new com.college.ui.gatepass.GatePassApprovalPanel(), "GATE_PASS_APPROVAL");
        contentPanel.add(new com.college.ui.security.AuditLogViewerPanel(), "AUDIT_LOGS");
        contentPanel.add(new com.college.ui.reports.ReportsPanel(), "REPORTS");

        add(contentPanel, BorderLayout.CENTER);

        // Show home by default
        cardLayout.show(contentPanel, "HOME");
    }

    /**
     * Create top panel with user info and logout
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Title
        JLabel titleLabel = new JLabel("College Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // User Info Panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(UIHelper.PRIMARY_COLOR);

        JLabel userLabel = new JLabel("Welcome, " + username + " (" + role + ")");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(UIHelper.DANGER_COLOR);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());

        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create sidebar navigation menu
     */
    private JPanel createSidebarMenu() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Add menu items
        addMenuItem(sidebar, "Home", "HOME");

        if (role.equals("ADMIN") || role.equals("FACULTY")) {
            addMenuItem(sidebar, "Student Management", "STUDENTS");
            if (role.equals("ADMIN")) {
                addMenuItem(sidebar, "Staff Management", "FACULTY");
            }
            addMenuItem(sidebar, "Course Management", "COURSES");
            addMenuItem(sidebar, "Attendance", "ATTENDANCE");
            addMenuItem(sidebar, "Grades", "GRADES");
            addMenuItem(sidebar, "Timetable", "TIMETABLE");
            addMenuItem(sidebar, "Library Management", "LIBRARY");
            addMenuItem(sidebar, "Hostel Management", "HOSTEL");
            addMenuItem(sidebar, "Fee Management", "FEES");
            addMenuItem(sidebar, "Gate Pass Approvals", "GATE_PASS_APPROVAL");
            addMenuItem(sidebar, "Reports", "REPORTS");
            if (role.equals("ADMIN")) {
                addMenuItem(sidebar, "Audit Logs", "AUDIT_LOGS");
            }
        }

        if (role.equals("STUDENT")) {
            addMenuItem(sidebar, "My Attendance", "MY_ATTENDANCE");
            addMenuItem(sidebar, "My Courses", "COURSES");
            addMenuItem(sidebar, "Timetable", "TIMETABLE");
            addMenuItem(sidebar, "Library", "LIBRARY");
            addMenuItem(sidebar, "My Hostel", "MY_HOSTEL");
            addMenuItem(sidebar, "My Fees", "FEES");
            addMenuItem(sidebar, "Gate Pass", "GATE_PASS");
            addMenuItem(sidebar, "Reports", "REPORTS");
        }

        // Add glue to push items to top
        sidebar.add(Box.createVerticalGlue());

        // Add Change Password at bottom for all users
        addMenuItem(sidebar, "Change Password", "CHANGE_PASSWORD");

        return sidebar;
    }

    /**
     * Add menu item to sidebar
     */
    private void addMenuItem(JPanel sidebar, String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(230, 50));
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(52, 73, 94));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UIHelper.PRIMARY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 73, 94));
            }
        });

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(10));
    }

    /**
     * Create enhanced home panel with stats and activity feed
     */
    private JPanel createHomePanel() {
        return new com.college.ui.dashboard.EnhancedHomePanel(username, role, userId);
    }

    /**
     * Create a statistic card
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Get count from database table
     */
    private String getCountFromDB(String tableName) {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * Create Change Password Panel
     */
    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);

        JButton changePasswordButton = new JButton("Change My Password");
        changePasswordButton.setFont(new Font("Arial", Font.BOLD, 16));
        changePasswordButton.setBackground(UIHelper.PRIMARY_COLOR);
        changePasswordButton.setForeground(Color.WHITE);
        changePasswordButton.setPreferredSize(new Dimension(250, 50));
        changePasswordButton.setFocusPainted(false);
        changePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePasswordButton.addActionListener(e -> {
            com.college.ui.security.ChangePasswordDialog dialog = new com.college.ui.security.ChangePasswordDialog(
                    this);
            dialog.setVisible(true);
        });

        contentPanel.add(changePasswordButton);

        JLabel instructionLabel = new JLabel("<html><center>Click the button to change your password.<br>" +
                "Ensure your new password is strong and secure.</center></html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(127, 140, 141));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Handle logout
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Log logout action
            com.college.utils.SessionManager session = com.college.utils.SessionManager.getInstance();
            com.college.dao.AuditLogDAO.logAction(session.getUserId(), session.getUsername(),
                    "LOGOUT", "USER", session.getUserId(), "User logged out");

            // Clear session
            session.clearSession();

            // Close dashboard
            dispose();

            // Open login screen
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}
