package com.college.ui.assignments;

import com.college.dao.SubmissionDAO;
import com.college.models.Assignment;
import com.college.models.Submission;
import com.college.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubmissionReviewDialog extends JDialog {

    private SubmissionDAO submissionDAO;
    private Assignment assignment;
    private JTable submissionTable;
    private DefaultTableModel tableModel;

    public SubmissionReviewDialog(Frame parent, Assignment assignment) {
        super(parent, "Review Submissions - " + assignment.getTitle(), true);
        this.assignment = assignment;
        this.submissionDAO = new SubmissionDAO();

        setSize(900, 600);
        setLocationRelativeTo(parent);

        initComponents();
        loadSubmissions();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Submissions for: " + assignment.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = UIHelper.createPrimaryButton("Refresh");
        refreshButton.addActionListener(e -> loadSubmissions());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Student", "Enrollment ID", "Submitted At", "Status", "Plagiarism", "Grade" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        submissionTable = new JTable(tableModel);
        UIHelper.styleTable(submissionTable);

        JScrollPane scrollPane = new JScrollPane(submissionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton viewButton = UIHelper.createPrimaryButton("View & Grade");
        viewButton.addActionListener(e -> viewSubmission());

        JButton closeButton = UIHelper.createDangerButton("Close");
        closeButton.addActionListener(e -> dispose());

        footerPanel.add(viewButton);
        footerPanel.add(closeButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadSubmissions() {
        tableModel.setRowCount(0);
        List<Submission> submissions = submissionDAO.getSubmissionsByAssignment(assignment.getId());

        for (Submission s : submissions) {
            String plagiarismText = s.getPlagiarismScore() + "%";
            // Highlight plagiarism
            if (s.getPlagiarismScore() > 30) {
                plagiarismText = "⚠️ " + plagiarismText;
            }

            Object[] row = {
                    s.getId(),
                    s.getStudentName(),
                    s.getStudentEnrollmentId(),
                    s.getSubmittedAt(),
                    s.getStatus(),
                    plagiarismText,
                    s.getGrade() != null ? s.getGrade() : "-"
            };
            tableModel.addRow(row);
        }
    }

    private void viewSubmission() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showErrorMessage(this, "Please select a submission to view.");
            return;
        }

        int submissionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        // Ideally we fetch the full object, but for now we can refetch or store in map.
        // Actually, we can get list and find by ID.

        List<Submission> submissions = submissionDAO.getSubmissionsByAssignment(assignment.getId());
        Submission selectedSubmission = submissions.stream()
                .filter(s -> s.getId() == submissionId)
                .findFirst()
                .orElse(null);

        if (selectedSubmission != null) {
            showGradeDialog(selectedSubmission);
        }
    }

    private void showGradeDialog(Submission submission) {
        JDialog dialog = new JDialog(this, "Grade Submission - " + submission.getStudentName(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Submission content
        content.add(new JLabel("Submission Text:"), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea submissionArea = new JTextArea(submission.getSubmissionText());
        submissionArea.setEditable(false);
        submissionArea.setLineWrap(true);
        submissionArea.setWrapStyleWord(true);
        content.add(new JScrollPane(submissionArea), gbc);

        // Grade Input
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JLabel("Grade (0-100):"), gbc);
        gbc.gridy++;
        JTextField gradeField = new JTextField(
                submission.getGrade() != null ? String.valueOf(submission.getGrade()) : "");
        content.add(gradeField, gbc);

        // Feedback
        gbc.gridy++;
        content.add(new JLabel("Feedback:"), gbc);
        gbc.gridy++;
        gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea feedbackArea = new JTextArea(submission.getFeedback());
        feedbackArea.setLineWrap(true);
        content.add(new JScrollPane(feedbackArea), gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton saveBtn = UIHelper.createSuccessButton("Save Grade");
        saveBtn.addActionListener(e -> {
            try {
                double grade = Double.parseDouble(gradeField.getText());
                String feedback = feedbackArea.getText();
                if (submissionDAO.gradeSubmission(submission.getId(), grade, feedback)) {
                    UIHelper.showSuccessMessage(dialog, "Graded successfully!");
                    dialog.dispose();
                    loadSubmissions();
                } else {
                    UIHelper.showErrorMessage(dialog, "Failed to save.");
                }
            } catch (NumberFormatException ex) {
                UIHelper.showErrorMessage(dialog, "Invalid grade format.");
            }
        });

        JButton cancelBtn = UIHelper.createDangerButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
