package com.college.ui.grades;

import com.college.dao.GradeDAO;
import com.college.dao.CourseDAO;
import com.college.dao.StudentDAO;
import com.college.models.Grade;
import com.college.models.Course;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Grade Management Panel
 * For faculty to enter and manage student gr
 * ades
 */
public class GradeManagementPanel extends JPanel {

    private GradeDAO gradeDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;

    private JComboBox<CourseItem> courseCombo;
    private JComboBox<String> examTypeCombo;
    private JTable gradeTable;
    private DefaultTableModel tableModel;

    public GradeManagementPanel() {
        gradeDAO = new GradeDAO();
        courseDAO = new CourseDAO();
        studentDAO = new StudentDAO();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = createTopPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCourses();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Grade Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        selectionPanel.setBackground(Color.WHITE);

        JLabel courseLabel = UIHelper.createLabel("Course:");
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(200, 30));

        JLabel examLabel = UIHelper.createLabel("Exam Type:");
        examTypeCombo = new JComboBox<>(new String[] {
                "MID_TERM", "END_TERM", "ASSIGNMENT", "QUIZ", "PROJECT"
        });

        JButton loadButton = UIHelper.createPrimaryButton("Load Students");
        loadButton.addActionListener(e -> loadStudents());

        selectionPanel.add(courseLabel);
        selectionPanel.add(courseCombo);
        selectionPanel.add(examLabel);
        selectionPanel.add(examTypeCombo);
        selectionPanel.add(loadButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(selectionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Student ID", "Student Name", "Marks Obtained", "Max Marks", "Percentage", "Grade" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3; // Only marks columns editable
            }
        };

        gradeTable = new JTable(tableModel);
        UIHelper.styleTable(gradeTable);

        JScrollPane scrollPane = new JScrollPane(gradeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);

        JButton saveButton = UIHelper.createPrimaryButton("Save Grades");
        saveButton.setPreferredSize(new Dimension(150, 40));
        saveButton.addActionListener(e -> saveGrades());

        JButton viewReportButton = UIHelper.createPrimaryButton("View Report");
        viewReportButton.setPreferredSize(new Dimension(150, 40));
        viewReportButton.addActionListener(e -> viewReport());

        JButton exportButton = UIHelper.createPrimaryButton("Export");
        exportButton.setPreferredSize(new Dimension(120, 40));
        exportButton
                .addActionListener(e -> com.college.utils.TableExporter.showExportDialog(this, gradeTable, "grades"));

        panel.add(saveButton);
        panel.add(viewReportButton);
        panel.add(exportButton);

        return panel;
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course course : courses) {
            courseCombo.addItem(new CourseItem(course));
        }
    }

    private void loadStudents() {
        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        tableModel.setRowCount(0);
        List<Student> students = studentDAO.getAllStudents();

        for (Student student : students) {
            Object[] row = {
                    student.getId(),
                    student.getName(),
                    "", // Marks obtained
                    "100", // Max marks (default)
                    "", // Percentage (calculated)
                    "" // Grade (calculated)
            };
            tableModel.addRow(row);
        }
    }

    private void saveGrades() {
        CourseItem selected = (CourseItem) courseCombo.getSelectedItem();
        if (selected == null) {
            UIHelper.showErrorMessage(this, "Please select a course!");
            return;
        }

        String examType = (String) examTypeCombo.getSelectedItem();
        int saved = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                int studentId = (Integer) tableModel.getValueAt(i, 0);
                String marksStr = tableModel.getValueAt(i, 2).toString();
                String maxMarksStr = tableModel.getValueAt(i, 3).toString();

                if (marksStr.isEmpty())
                    continue;

                double marksObtained = Double.parseDouble(marksStr);
                double maxMarks = Double.parseDouble(maxMarksStr);

                Grade grade = new Grade(studentId, selected.course.getId(),
                        examType, marksObtained, maxMarks);

                if (gradeDAO.saveGrade(grade)) {
                    // Update table with calculated values
                    tableModel.setValueAt(String.format("%.1f%%", grade.getPercentage()), i, 4);
                    tableModel.setValueAt(grade.getGrade(), i, 5);
                    saved++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (saved > 0) {
            UIHelper.showSuccessMessage(this, "Grades saved for " + saved + " students!");
        }
    }

    private void viewReport() {
        UIHelper.showSuccessMessage(this, "Grade reports coming soon!");
    }

    private static class CourseItem {
        Course course;

        CourseItem(Course course) {
            this.course = course;
        }

        @Override
        public String toString() {
            return course.getName();
        }
    }
}
