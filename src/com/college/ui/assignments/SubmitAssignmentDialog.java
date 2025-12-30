package com.college.ui.assignments;

import com.college.dao.SubmissionDAO;
import com.college.models.Assignment;
import com.college.models.Submission;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SubmitAssignmentDialog extends JDialog {

    private SubmissionDAO submissionDAO;
    private Assignment assignment;
    private int studentId;
    private boolean isSuccess = false;

    private JTextArea submissionArea;
    private JTextField filePathField;
    private JButton uploadButton;

    public SubmitAssignmentDialog(Frame parent, Assignment assignment, int studentId) {
        super(parent, "Submit Assignment: " + assignment.getTitle(), true);
        this.assignment = assignment;
        this.studentId = studentId;
        this.submissionDAO = new SubmissionDAO();

        setSize(600, 500);
        setLocationRelativeTo(parent);
        initComponents();
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

        // Description
        formPanel.add(UIHelper.createLabel("Description:"), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea descArea = new JTextArea(assignment.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(245, 245, 245));
        formPanel.add(new JScrollPane(descArea), gbc);

        // Text Submission
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(UIHelper.createLabel("Your Answer (Text):"), gbc);
        gbc.gridy++;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        submissionArea = new JTextArea();
        submissionArea.setLineWrap(true);
        submissionArea.setWrapStyleWord(true);
        submissionArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.add(new JScrollPane(submissionArea), gbc);

        // File Upload (Simulation)
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(UIHelper.createLabel("Or Attach File:"), gbc);

        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setBackground(Color.WHITE);
        filePathField = new JTextField();
        filePathField.setEditable(false);
        uploadButton = new JButton("Browse...");
        uploadButton.addActionListener(e -> chooseFile());

        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(uploadButton, BorderLayout.EAST);

        gbc.gridy++;
        formPanel.add(filePanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton submitButton = UIHelper.createSuccessButton("Submit");
        submitButton.addActionListener(e -> submit());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void submit() {
        String text = submissionArea.getText().trim();
        String file = filePathField.getText().trim();

        if (text.isEmpty() && file.isEmpty()) {
            UIHelper.showErrorMessage(this, "Please provide text or attach a file.");
            return;
        }

        Submission submission = new Submission(assignment.getId(), studentId, text, file);

        if (submissionDAO.submitAssignment(submission)) {
            UIHelper.showSuccessMessage(this, "Assignment submitted successfully!");
            isSuccess = true;
            dispose();
        } else {
            UIHelper.showErrorMessage(this, "Failed to submit assignment.");
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
