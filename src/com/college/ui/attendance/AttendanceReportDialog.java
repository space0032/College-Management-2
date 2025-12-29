package com.college.ui.attendance;

import com.college.dao.AttendanceDAO;
import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Attendance Report Dialog
 * Shows attendance statistics for a course
 */
public class AttendanceReportDialog extends JDialog {

    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private int courseId;
    private String courseName;

    private JTable reportTable;
    private DefaultTableModel tableModel;

    public AttendanceReportDialog(Frame parent, int courseId, String courseName) {
        super(parent, "Attendance Report - " + courseName, true);
        this.courseId = courseId;
        this.courseName = courseName;
        this.attendanceDAO = new AttendanceDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        loadReport();
    }

    private void initComponents() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Attendance Report");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "Student ID", "Student Name", "Attendance %", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        UIHelper.styleTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton closeButton = UIHelper.createDangerButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.addActionListener(e -> dispose());

        JButton exportButton = UIHelper.createPrimaryButton("Export");
        exportButton.setPreferredSize(new Dimension(120, 35));
        exportButton.addActionListener(
                e -> com.college.utils.TableExporter.showExportDialog(buttonPanel, reportTable, "attendance_report"));

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReport() {
        Map<Integer, Double> stats = attendanceDAO.getCourseAttendanceStats(courseId);

        for (Map.Entry<Integer, Double> entry : stats.entrySet()) {
            int studentId = entry.getKey();
            double percentage = entry.getValue();

            Student student = studentDAO.getStudentById(studentId);
            if (student != null) {
                String status = percentage >= 75 ? "Good" : percentage >= 60 ? "Warning" : "Critical";

                Color statusColor = percentage >= 75 ? new Color(46, 204, 113)
                        : percentage >= 60 ? new Color(243, 156, 18) : new Color(231, 76, 60);

                Object[] row = {
                        student.getId(),
                        student.getName(),
                        String.format("%.1f%%", percentage),
                        status
                };
                tableModel.addRow(row);
            }
        }

        if (tableModel.getRowCount() == 0) {
            Object[] row = { "", "No attendance recorded yet", "", "" };
            tableModel.addRow(row);
        }
    }
}
