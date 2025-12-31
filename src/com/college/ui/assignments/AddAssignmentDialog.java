package com.college.ui.assignments;

import com.college.dao.AssignmentDAO;
import com.college.dao.CourseDAO;
import com.college.models.Assignment;
import com.college.models.Course;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class AddAssignmentDialog extends JDialog {

    private AssignmentDAO assignmentDAO;
    private CourseDAO courseDAO;
    private int facultyId;
    private boolean isSuccess = false;

    private JComboBox<CourseItem> courseCombo;
    private JComboBox<Integer> semesterCombo; // Added semester combo
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner dateSpinner;

    public AddAssignmentDialog(Frame parent, int facultyId) {
        super(parent, "Add New Assignment", true);
        this.facultyId = facultyId;
        this.assignmentDAO = new AssignmentDAO();
        this.courseDAO = new CourseDAO();

        initComponents();
        loadCourses();

        setSize(500, 550); // Increased height
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Course Selection
        formPanel.add(UIHelper.createLabel("Select Course:"), gbc);
        gbc.gridx = 1;
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(250, 30));
        formPanel.add(courseCombo, gbc);

        // Semester Selection
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Select Semester:"), gbc);
        gbc.gridx = 1;
        semesterCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++) {
            semesterCombo.addItem(i);
        }
        semesterCombo.setPreferredSize(new Dimension(250, 30));
        formPanel.add(semesterCombo, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIHelper.createLabel("Assignment Title:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(UIHelper.createLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);

        // Due Date
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(UIHelper.createLabel("Due Date:"), gbc);
        gbc.gridx = 1;

        // Date Spinner
        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd-MMM-yyyy HH:mm");
        dateSpinner.setEditor(editor);
        dateSpinner.setValue(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)); // Default 1 week
        formPanel.add(dateSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createSuccessButton("Create Assignment");
        saveButton.addActionListener(e -> saveAssignment());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            courseCombo.addItem(new CourseItem(c));
        }
    }

    private void saveAssignment() {
        CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
        if (selectedCourse == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            UIHelper.showErrorMessage(this, "Title cannot be empty!");
            return;
        }

        String description = descriptionArea.getText().trim();
        Date dueDate = (Date) dateSpinner.getValue();
        int semester = (Integer) semesterCombo.getSelectedItem();

        Assignment assignment = new Assignment();
        assignment.setCourseId(selectedCourse.course.getId());
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setCreatedBy(facultyId);
        assignment.setSemester(semester);

        if (assignmentDAO.createAssignment(assignment)) {
            UIHelper.showSuccessMessage(this, "Assignment created successfully!");
            isSuccess = true;
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to create assignment.");
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    private static class CourseItem {
        Course course;

        CourseItem(Course c) {
            this.course = c;
        }

        public String toString() {
            return course.getName();
        }
    }
}
