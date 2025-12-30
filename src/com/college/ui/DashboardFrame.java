package com.college.ui;

import com.college.ui.student.StudentManagementPanel;
import com.college.ui.faculty.FacultyManagementPanel;
import com.college.ui.courses.CourseManagementPanel;
import com.college.ui.library.LibraryManagementPanel;

import com.college.ui.attendance.AttendanceManagementPanel;
import com.college.ui.attendance.StudentAttendancePanel;
import com.college.ui.grades.GradeManagementPanel;
import com.college.ui.timetable.TimetablePanel;
import com.college.ui.assignments.AssignmentManagementPanel;
import com.college.ui.assignments.StudentAssignmentPanel;
import com.college.utils.UIHelper;
import com.college.utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

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

        contentPanel.add(new StudentManagementPanel(role), "STUDENTS");
        contentPanel.add(new FacultyManagementPanel(), "FACULTY");
        contentPanel.add(new CourseManagementPanel(role, userId), "COURSES");
        contentPanel.add(new AttendanceManagementPanel(), "ATTENDANCE");
        contentPanel.add(new StudentAttendancePanel(userId), "MY_ATTENDANCE");
        contentPanel.add(new GradeManagementPanel(), "GRADES");
        contentPanel.add(new TimetablePanel(role, userId), "TIMETABLE");
        contentPanel.add(new LibraryManagementPanel(role, userId), "LIBRARY");
        contentPanel.add(new com.college.ui.hostel.HostelManagementPanel(role, userId), "HOSTEL");
        contentPanel.add(new com.college.ui.hostel.StudentHostelInfoPanel(userId), "MY_HOSTEL");
        contentPanel.add(new com.college.ui.fees.EnhancedFeeManagementPanel(role, userId), "FEES");
        contentPanel.add(createChangePasswordPanel(), "CHANGE_PASSWORD");
        contentPanel.add(new com.college.ui.gatepass.GatePassRequestPanel(userId), "GATE_PASS");
        contentPanel.add(new com.college.ui.gatepass.GatePassApprovalPanel(), "GATE_PASS_APPROVAL");
        contentPanel.add(new com.college.ui.security.AuditLogViewerPanel(), "AUDIT_LOGS");
        contentPanel.add(new com.college.ui.reports.ReportsPanel(role, userId), "REPORTS");
        contentPanel.add(new com.college.ui.department.DepartmentManagementPanel(), "DEPARTMENTS");

        // Add Assignments Panels
        if (role.equals("FACULTY") || role.equals("ADMIN")) {
            contentPanel.add(new AssignmentManagementPanel(userId), "ASSIGNMENTS");
        } else if (role.equals("STUDENT")) {
            contentPanel.add(new StudentAssignmentPanel(userId), "ASSIGNMENTS");
        }

        // Admin - Warden Management
        // if (role.equals("ADMIN")) {
        // contentPanel.add(new com.college.ui.admin.WardenManagementPanel(),
        // "WARDENS");
        // }

        // Warden Specific Panels
        if (role.equals("WARDEN")) {
            contentPanel.add(new com.college.ui.warden.HostelAttendancePanel(userId), "HOSTEL_ATTENDANCE");
            // Reusing panels for Warden
            // "HOSTEL" is already added (HostelManagementPanel)
            // "STUDENTS" is already added (StudentManagementPanel)
            // "GATE_PASS_APPROVAL" is already added
            // "REPORTS" is already added
        }

        // My Profile - create a simple wrapper panel that shows the dialog
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        JLabel profileLabel = new JLabel("Loading Profile...", SwingConstants.CENTER);
        profileLabel.setFont(new Font("Arial", Font.BOLD, 18));
        profilePanel.add(profileLabel, BorderLayout.CENTER);
        contentPanel.add(profilePanel, "MY_PROFILE");

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
                // addMenuItem(sidebar, "Warden Management", "WARDENS"); // Moved to Hostel
                // Management
            }
            addMenuItem(sidebar, "Course Management", "COURSES");
            if (role.equals("ADMIN")) {
                addMenuItem(sidebar, "Department Management", "DEPARTMENTS");
            }
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
            addMenuItem(sidebar, "Assignments", "ASSIGNMENTS"); // Add for Faculty/Admin
        }

        if (role.equals("WARDEN")) {
            addMenuItem(sidebar, "Hostel Management", "HOSTEL");
            addMenuItem(sidebar, "Hostel Attendance", "HOSTEL_ATTENDANCE");
            addMenuItem(sidebar, "Student Details", "STUDENTS");
            addMenuItem(sidebar, "Gate Pass Approvals", "GATE_PASS_APPROVAL");
            addMenuItem(sidebar, "Reports", "REPORTS");
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
            addMenuItem(sidebar, "Assignments", "ASSIGNMENTS"); // Add for Student
        }

        // Add glue to push items to top
        sidebar.add(Box.createVerticalGlue());

        // Add Settings section at bottom
        sidebar.add(createSettingsSection());

        return sidebar;
    }

    /**
     * Create Settings section for sidebar
     */
    private JPanel createSettingsSection() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(new Color(44, 62, 80));
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.setMaximumSize(new Dimension(230, 180));
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(127, 140, 141)),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)));

        // Settings label
        JLabel settingsLabel = new JLabel("  ⚙ SETTINGS");
        settingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        settingsLabel.setForeground(new Color(189, 195, 199));
        settingsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsLabel.setMaximumSize(new Dimension(230, 30));
        settingsPanel.add(settingsLabel);

        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // My Profile button
        JButton profileButton = createSettingsButton("My Profile", "MY_PROFILE");
        settingsPanel.add(profileButton);

        settingsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Change Password button
        JButton passwordButton = createSettingsButton("Change Password", "CHANGE_PASSWORD");
        settingsPanel.add(passwordButton);

        return settingsPanel;
    }

    /**
     * Create settings button
     */
    private JButton createSettingsButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
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
            if (cardName.equals("MY_PROFILE")) {
                showProfileDialog();
            } else {
                cardLayout.show(contentPanel, cardName);
            }
        });

        return button;
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
            if (cardName.equals("MY_PROFILE")) {
                showProfileDialog();
            } else {
                cardLayout.show(contentPanel, cardName);
            }
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
     * Show user profile dialog
     */
    private void showProfileDialog() {
        com.college.ui.profile.ProfileDialog dialog = new com.college.ui.profile.ProfileDialog(this);
        dialog.setVisible(true);
    }

    /**
     * Create Change Password Panel
     */
    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Change Password Button
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
        panel.add(changePasswordButton, gbc);

        // Instruction Label
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 10, 10);
        JLabel instructionLabel = new JLabel("<html><center>Click the button to change your password.<br>" +
                "Ensure your new password is strong and secure.</center></html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(127, 140, 141));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(instructionLabel, gbc);

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
