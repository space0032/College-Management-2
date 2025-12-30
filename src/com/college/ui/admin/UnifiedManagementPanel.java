package com.college.ui.admin;

import com.college.ui.student.StudentManagementPanel;
import com.college.ui.faculty.FacultyManagementPanel;
import com.college.ui.courses.CourseManagementPanel;
import com.college.ui.department.DepartmentManagementPanel;
import com.college.ui.library.LibraryManagementPanel;
import com.college.ui.fees.EnhancedFeeManagementPanel;
import com.college.ui.attendance.AttendanceManagementPanel;
import com.college.ui.grades.GradeManagementPanel;
import com.college.ui.timetable.TimetablePanel;
import com.college.ui.gatepass.GatePassApprovalPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Unified Management Panel for Admin
 * Combines various management functionalities into a single tabbed interface
 */
public class UnifiedManagementPanel extends JPanel {

    private String role;
    private int userId;

    public UnifiedManagementPanel(String role, int userId) {
        this.role = role;
        this.userId = userId;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Add tabs
        tabbedPane.addTab("Students", new StudentManagementPanel(role));
        tabbedPane.addTab("Faculty", new FacultyManagementPanel());
        tabbedPane.addTab("Departments", new DepartmentManagementPanel());
        tabbedPane.addTab("Courses", new CourseManagementPanel(role, userId));
        tabbedPane.addTab("Timetable", new TimetablePanel(role, userId));
        tabbedPane.addTab("Attendance", new AttendanceManagementPanel());
        tabbedPane.addTab("Grades", new GradeManagementPanel());
        tabbedPane.addTab("Library", new LibraryManagementPanel(role, userId));
        tabbedPane.addTab("Fees", new EnhancedFeeManagementPanel(role, userId));
        tabbedPane.addTab("Gate Pass Requests", new GatePassApprovalPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
