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
    private String displayName; // Actual name (e.g., "John Doe" instead of "john_doe")

    public DashboardFrame(String username, String role, int userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
        // Get the actual display name based on role
        this.displayName = com.college.utils.UserDisplayNameUtil.getDisplayName(userId, role, username);
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

        // Unified Admin Panel
        if (role.equals("ADMIN")) {
            contentPanel.add(new com.college.ui.admin.UnifiedManagementPanel(role, userId), "INSTITUTE_MANAGEMENT");
        }

        // Announcements Panel for Faculty
        if (role.equals("FACULTY")) {
            contentPanel.add(new com.college.ui.announcements.AnnouncementManagementPanel(role, userId), "ANNOUNCEMENTS");
        }

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
        panel.setBackground(com.college.utils.ModernTheme.PRIMARY);
        panel.setPreferredSize(new Dimension(0, 70));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Title
        JLabel titleLabel = new JLabel("College Management System");
        titleLabel.setFont(com.college.utils.ModernTheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        // User Info Panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Welcome, " + displayName);
        userLabel.setFont(com.college.utils.ModernTheme.FONT_BODY);
        userLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("[" + role + "]");
        roleLabel.setFont(com.college.utils.ModernTheme.FONT_SMALL);
        roleLabel.setForeground(new Color(199, 210, 254));

        JButton logoutButton = com.college.utils.ModernTheme.createButton("Logout", com.college.utils.ModernTheme.DANGER);
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.addActionListener(e -> logout());

        userPanel.add(userLabel);
        userPanel.add(roleLabel);
        userPanel.add(Box.createHorizontalStrut(10));
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
        sidebar.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(com.college.utils.ModernTheme.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        com.college.utils.SessionManager session = com.college.utils.SessionManager.getInstance();

        // Add menu items
        addMenuItem(sidebar, "Home", "HOME");

        // 1. Institute Management (Admin Unified Panel)
        if (session.hasPermission("MANAGE_SYSTEM") || session.hasPermission("VIEW_AUDIT_LOGS")) {
            addMenuItem(sidebar, "Institute Management", "INSTITUTE_MANAGEMENT");
        }

        // 2. Student Management
        if (session.hasPermission("VIEW_STUDENTS") || session.hasPermission("MANAGE_STUDENTS")) {
            // If not covered by Unified Panel (which is only for Admins/System Managers)
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Student Management", "STUDENTS");
            }
        }

        // 3. Faculty Management
        if (session.hasPermission("VIEW_FACULTY") || session.hasPermission("MANAGE_FACULTY")) {
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Faculty Management", "FACULTY");
            }
        }

        // 4. Course Management
        if (session.hasPermission("VIEW_COURSES") || session.hasPermission("MANAGE_ALL_COURSES")
                || session.hasPermission("MANAGE_OWN_COURSES")) {
            if (!session.hasPermission("MANAGE_SYSTEM") && !session.isStudent()) {
                addMenuItem(sidebar, "Course Management", "COURSES");
            } else {
                // Even admins might want direct access sometimes, but usually it's in Unified.
                // Let's hide it for Admin to reduce clutter as per consolidation goal.
            }
            // Students see "My Courses"
            if (session.isStudent()) {
                addMenuItem(sidebar, "My Courses", "COURSES");
            }
        }

        // 5. Attendance
        if (session.hasPermission("VIEW_ATTENDANCE") || session.hasPermission("MANAGE_ATTENDANCE")) {
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Attendance", "ATTENDANCE");
            }
        }
        if (session.hasPermission("VIEW_OWN_ATTENDANCE")) {
            addMenuItem(sidebar, "My Attendance", "MY_ATTENDANCE");
        }

        // 6. Grades
        if (session.hasPermission("VIEW_GRADES") || session.hasPermission("MANAGE_GRADES")) {
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Grades", "GRADES");
            }
        }
        // Students usually view grades in Reports or specific panel

        // 7. Timetable
        if (session.hasPermission("VIEW_TIMETABLE") || session.hasPermission("MANAGE_TIMETABLE")) {
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Timetable", "TIMETABLE");
            }
        }

        // 8. Library
        if (session.hasPermission("VIEW_LIBRARY") || session.hasPermission("MANAGE_LIBRARY")) {
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Library", "LIBRARY");
            }
        }

        // 9. Hostel
        // Hostel Management (For Warden/Admin)
        if (session.hasPermission("MANAGE_HOSTEL") || session.hasPermission("MANAGE_ALLOCATIONS")) {
            addMenuItem(sidebar, "Hostel Management", "HOSTEL");
            if (session.hasPermission("APPROVE_GATE_PASS")) {
                addMenuItem(sidebar, "Gate Pass Approvals", "GATE_PASS_APPROVAL");
            }
        }
        // My Hostel (For Students)
        if (session.hasPermission("VIEW_HOSTEL") && session.isStudent()) {
            addMenuItem(sidebar, "My Hostel", "MY_HOSTEL");
            addMenuItem(sidebar, "Gate Pass", "GATE_PASS");
        }

        // Warden Specific
        if (session.hasPermission("MANAGE_HOSTEL") && role.equals("WARDEN")) {
            addMenuItem(sidebar, "Hostel Attendance", "HOSTEL_ATTENDANCE");
        }

        // 10. Fees
        if (session.hasPermission("MANAGE_FEES") || session.hasPermission("VIEW_ALL_FEES")) {
            // Usually in Unified for Admin, but Faculty might need access if permitted
            if (!session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Student Fees", "FEES");
            }
        }
        // My Fees - only for students, not for admin/faculty
        if (session.hasPermission("VIEW_OWN_FEES") && session.isStudent()) {
            addMenuItem(sidebar, "My Fees", "FEES");
        }

        // 11. Reports
        if (session.hasPermission("VIEW_REPORTS")) {
            addMenuItem(sidebar, "Reports", "REPORTS");
        }

        // 12. Audit Logs (Explicit permission check)
        if (session.hasPermission("VIEW_AUDIT_LOGS") && !session.hasPermission("MANAGE_SYSTEM")) {
            // If they have Unified Panel, Audit Logs is inside it.
            // If they have Audit permission but NO Unified Panel (e.g. Auditor role), show
            // it here.
            addMenuItem(sidebar, "Audit Logs", "AUDIT_LOGS");
        }

        // 13. Announcements (For Faculty only, Admin has it in Unified Panel)
        if (role.equals("FACULTY")) {
            addMenuItem(sidebar, "Announcements", "ANNOUNCEMENTS");
        }

        // 14. Assignments
        if (session.hasPermission("VIEW_ASSIGNMENTS") || session.hasPermission("MANAGE_ASSIGNMENTS")) {
            addMenuItem(sidebar, "Assignments", "ASSIGNMENTS");
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
        settingsPanel.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.setMaximumSize(new Dimension(220, 180));
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, com.college.utils.ModernTheme.SIDEBAR_HOVER),
                BorderFactory.createEmptyBorder(15, 0, 10, 0)));

        // Settings label
        JLabel settingsLabel = new JLabel("  SETTINGS");
        settingsLabel.setFont(com.college.utils.ModernTheme.FONT_HEADING);
        settingsLabel.setForeground(com.college.utils.ModernTheme.SIDEBAR_TEXT);
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
        button.setMaximumSize(new Dimension(210, 42));
        button.setFont(com.college.utils.ModernTheme.FONT_BODY);
        button.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
        button.setForeground(com.college.utils.ModernTheme.SIDEBAR_TEXT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(com.college.utils.ModernTheme.SIDEBAR_HOVER);
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
                button.setForeground(com.college.utils.ModernTheme.SIDEBAR_TEXT);
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
        button.setMaximumSize(new Dimension(220, 48));
        button.setFont(com.college.utils.ModernTheme.FONT_BODY);
        button.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
        button.setForeground(com.college.utils.ModernTheme.SIDEBAR_TEXT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(com.college.utils.ModernTheme.SIDEBAR_HOVER);
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(com.college.utils.ModernTheme.SIDEBAR_BG);
                button.setForeground(com.college.utils.ModernTheme.SIDEBAR_TEXT);
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
        return new com.college.ui.dashboard.EnhancedHomePanel(displayName, role, userId);
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
