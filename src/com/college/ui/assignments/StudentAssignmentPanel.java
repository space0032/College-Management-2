package com.college.ui.assignments;

import com.college.dao.AssignmentDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.dao.SubmissionDAO;
import com.college.models.Assignment;
import com.college.models.Course;
import com.college.models.Student;
import com.college.models.Submission;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class StudentAssignmentPanel extends JPanel {

    private AssignmentDAO assignmentDAO;
    private SubmissionDAO submissionDAO;
    private StudentDAO studentDAO;
    private int userId;
    private int studentId;

    private JComboBox<CourseItem> courseCombo;
    private JTable assignmentTable;
    private DefaultTableModel tableModel;

    public StudentAssignmentPanel(int userId) {
        this.userId = userId;
        this.assignmentDAO = new AssignmentDAO();
        this.submissionDAO = new SubmissionDAO();
        this.studentDAO = new StudentDAO();

        // Resolve Student ID
        Student student = studentDAO.getStudentByUserId(userId);
        if (student != null) {
            this.studentId = student.getId();
        }

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("My Assignments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        // Course Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("Filter by Course: "));
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(200, 30));
        courseCombo.addActionListener(e -> loadAssignments());
        filterPanel.add(courseCombo);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> loadAssignments());
        filterPanel.add(refreshButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Course", "Assignment", "Due Date", "Status", "Grade" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        assignmentTable = new JTable(tableModel);
        UIHelper.styleTable(assignmentTable);

        // Adjust column widths
        assignmentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        assignmentTable.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(Color.WHITE);

        JButton submitButton = UIHelper.createSuccessButton("Submit Assignment");
        submitButton.setPreferredSize(new Dimension(180, 40));
        submitButton.addActionListener(e -> openSubmitDialog());

        JButton viewFeedbackButton = UIHelper.createPrimaryButton("View Feedback");
        viewFeedbackButton.setPreferredSize(new Dimension(180, 40));
        viewFeedbackButton.addActionListener(e -> viewFeedback());

        footerPanel.add(submitButton);
        footerPanel.add(viewFeedbackButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        // ideally load only enrolled courses, but simplifying to all for now or
        // courseDAO doesn't support it yet
        // A better approach: distinct courses from assignments
        CourseDAO courseDAO = new CourseDAO();
        List<Course> courses = courseDAO.getAllCourses();
        courseCombo.addItem(new CourseItem(null, "All Courses"));
        for (Course c : courses) {
            courseCombo.addItem(new CourseItem(c, c.getName()));
        }
    }

    private void loadAssignments() {
        tableModel.setRowCount(0);
        CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
        if (selectedCourse == null)
            return;

        // Get student details to know semester
        Student student = studentDAO.getStudentByUserId(userId);
        if (student == null) {
            UIHelper.showErrorMessage(this, "Student profile not found.");
            return;
        }

        List<Assignment> assignments;
        if (selectedCourse.course == null) {
            // "All Courses" - fetch all assignments for the student's semester
            assignments = assignmentDAO.getAssignmentsBySemester(student.getSemester());
        } else {
            // Filter by Course AND Semester
            assignments = assignmentDAO.getAssignmentsByCourseAndSemester(
                    selectedCourse.course.getId(),
                    student.getSemester());
        }

        if (selectedCourse.course == null)
            return; // Skip for "All" for now to save complexity

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        for (Assignment a : assignments) {
            Submission sub = submissionDAO.getSubmission(a.getId(), studentId);
            String status = "PENDING";
            String grade = "-";

            if (sub != null) {
                status = sub.getStatus();
                if (sub.getGrade() != null) {
                    grade = String.valueOf(sub.getGrade());
                }
            } else if (new java.util.Date().after(a.getDueDate())) {
                status = "LATE / MISSED";
            }

            Object[] row = {
                    a.getId(),
                    a.getCourseName(),
                    a.getTitle(),
                    sdf.format(a.getDueDate()),
                    status,
                    grade
            };
            tableModel.addRow(row);
        }
    }

    private void openSubmitDialog() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select an assignment.");
            return;
        }

        String status = (String) tableModel.getValueAt(selectedRow, 4);
        if ("GRADED".equals(status)) {
            UIHelper.showErrorMessage(this, "This assignment is already graded.");
            return;
        }
        // Allow resubmission if not graded? let's say yes.

        int assignmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);

        if (assignment != null) {
            SubmitAssignmentDialog dialog = new SubmitAssignmentDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), assignment, studentId);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadAssignments();
            }
        }
    }

    private void viewFeedback() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select an assignment.");
            return;
        }

        int assignmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Submission sub = submissionDAO.getSubmission(assignmentId, studentId);

        if (sub != null && sub.getFeedback() != null) {
            JOptionPane.showMessageDialog(this,
                    "Grade: " + sub.getGrade() + "\nFeedback: " + sub.getFeedback(),
                    "Feedback", JOptionPane.INFORMATION_MESSAGE);
        } else {
            UIHelper.showErrorMessage(this, "No feedback available.");
        }
    }

    private static class CourseItem {
        Course course;
        String label;

        CourseItem(Course c, String l) {
            this.course = c;
            this.label = l;
        }

        public String toString() {
            return label;
        }
    }
}
