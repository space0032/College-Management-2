package com.college.ui.assignments;

import com.college.dao.AssignmentDAO;
import com.college.models.Assignment;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AssignmentManagementPanel extends JPanel {

    private AssignmentDAO assignmentDAO;
    private int facultyId;
    private JTable assignmentTable;
    private DefaultTableModel tableModel;

    public AssignmentManagementPanel(int facultyId) {
        this.facultyId = facultyId;
        this.assignmentDAO = new AssignmentDAO();
        initComponents();
        loadAssignments();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Assignment Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(UIHelper.PRIMARY_COLOR);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);

        JButton addButton = UIHelper.createSuccessButton("Create Assignment");
        addButton.addActionListener(e -> openAddAssignmentDialog());

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> loadAssignments());

        actionPanel.add(addButton);
        actionPanel.add(refreshButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] columns = { "ID", "Course", "Title", "Due Date", "Created At" };
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
        assignmentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        assignmentTable.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(Color.WHITE);

        JButton viewSubmissionsButton = UIHelper.createPrimaryButton("View Submissions");
        viewSubmissionsButton.setPreferredSize(new Dimension(180, 40));
        viewSubmissionsButton.addActionListener(e -> openSubmissionReview());

        JButton deleteButton = UIHelper.createDangerButton("Delete Assignment");
        deleteButton.setPreferredSize(new Dimension(180, 40));
        deleteButton.addActionListener(e -> deleteAssignment());

        footerPanel.add(viewSubmissionsButton);
        footerPanel.add(deleteButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadAssignments() {
        tableModel.setRowCount(0);
        List<Assignment> assignments = assignmentDAO.getAssignmentsByFaculty(facultyId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        for (Assignment a : assignments) {
            Object[] row = {
                    a.getId(),
                    a.getCourseName(),
                    a.getTitle(),
                    sdf.format(a.getDueDate()),
                    sdf.format(a.getCreatedAt())
            };
            tableModel.addRow(row);
        }
    }

    private void openAddAssignmentDialog() {
        AddAssignmentDialog dialog = new AddAssignmentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), facultyId);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadAssignments();
        }
    }

    private void openSubmissionReview() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select an assignment.");
            return;
        }

        int assignmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);

        if (assignment != null) {
            SubmissionReviewDialog dialog = new SubmissionReviewDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), assignment);
            dialog.setVisible(true);
        }
    }

    private void deleteAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select an assignment to delete.");
            return;
        }

        int assignmentId = (Integer) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this assignment?\nAll submissions will be deleted as well.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (assignmentDAO.deleteAssignment(assignmentId)) {
                UIHelper.showSuccessMessage(this, "Assignment deleted.");
                loadAssignments();
            } else {
                UIHelper.showErrorMessage(this, "Failed to delete assignment.");
            }
        }
    }
}
