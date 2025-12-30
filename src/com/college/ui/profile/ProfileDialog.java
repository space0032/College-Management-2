package com.college.ui.profile;

import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.models.Student;
import com.college.models.Faculty;
import com.college.utils.SessionManager;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * User Profile Dialog
 * Shows user's personal information based on their role
 */
public class ProfileDialog extends JDialog {

    private String role;
    private int userId;

    public ProfileDialog(Frame parent) {
        super(parent, "My Profile", true);
        this.role = SessionManager.getInstance().getRole();
        this.userId = SessionManager.getInstance().getUserId();

        initComponents();
    }

    private void initComponents() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(500, 80));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("My Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Load user data based on role
        loadUserData(contentPanel);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton closeButton = UIHelper.createPrimaryButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // Add panels
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserData(JPanel contentPanel) {
        if (role.equals("STUDENT")) {
            loadStudentData(contentPanel);
        } else if (role.equals("FACULTY")) {
            loadFacultyData(contentPanel);
        } else {
            loadAdminData(contentPanel);
        }
    }

    private void loadStudentData(JPanel contentPanel) {
        StudentDAO studentDAO = new StudentDAO();
        Student student = studentDAO.getStudentByUserId(userId);

        if (student != null) {
            addInfoRow(contentPanel, "Role", "Student");
            addInfoRow(contentPanel, "Name", student.getName());
            addInfoRow(contentPanel, "Email", student.getEmail());
            addInfoRow(contentPanel, "Phone", student.getPhone());
            addInfoRow(contentPanel, "Department", student.getDepartment() != null ? student.getDepartment() : "N/A");
            addInfoRow(contentPanel, "Semester", String.valueOf(student.getSemester()));
            addInfoRow(contentPanel, "Course", student.getCourse());
            addInfoRow(contentPanel, "Batch", student.getBatch());

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            addInfoRow(contentPanel, "Enrollment Date", sdf.format(student.getEnrollmentDate()));
            addInfoRow(contentPanel, "Address", student.getAddress());
        } else {
            addInfoRow(contentPanel, "Error", "Student data not found");
        }
    }

    private void loadFacultyData(JPanel contentPanel) {
        FacultyDAO facultyDAO = new FacultyDAO();
        Faculty faculty = getFacultyByUserId(facultyDAO, userId);

        if (faculty != null) {
            addInfoRow(contentPanel, "Role", "Faculty");
            addInfoRow(contentPanel, "Name", faculty.getName());
            addInfoRow(contentPanel, "Email", faculty.getEmail());
            addInfoRow(contentPanel, "Phone", faculty.getPhone());
            addInfoRow(contentPanel, "Department", faculty.getDepartment());
            addInfoRow(contentPanel, "Qualification", faculty.getQualification());

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            addInfoRow(contentPanel, "Join Date", sdf.format(faculty.getJoinDate()));
        } else {
            addInfoRow(contentPanel, "Error", "Faculty data not found");
        }
    }

    private void loadAdminData(JPanel contentPanel) {
        String username = SessionManager.getInstance().getUsername();

        addInfoRow(contentPanel, "Role", "Administrator");
        addInfoRow(contentPanel, "Username", username);
        addInfoRow(contentPanel, "User ID", String.valueOf(userId));
        addInfoRow(contentPanel, "Access Level", "Full System Access");
    }

    private Faculty getFacultyByUserId(FacultyDAO facultyDAO, int userId) {
        // Get faculty by user_id
        try {
            java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
            String sql = "SELECT * FROM faculty WHERE user_id = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Faculty faculty = new Faculty();
                faculty.setId(rs.getInt("id"));
                faculty.setName(rs.getString("name"));
                faculty.setEmail(rs.getString("email"));
                faculty.setPhone(rs.getString("phone"));
                faculty.setDepartment(rs.getString("department"));
                faculty.setQualification(rs.getString("qualification"));
                faculty.setJoinDate(rs.getDate("join_date"));
                return faculty;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("Arial", Font.BOLD, 14));
        labelComponent.setPreferredSize(new Dimension(150, 25));

        JLabel valueComponent = new JLabel(value != null ? value : "N/A");
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 14));

        rowPanel.add(labelComponent, BorderLayout.WEST);
        rowPanel.add(valueComponent, BorderLayout.CENTER);

        panel.add(rowPanel);

        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(220, 220, 220));
        panel.add(separator);
    }
}
