package com.college;

import com.college.ui.LoginFrame;
import com.college.utils.DatabaseConnection;

import javax.swing.*;

/**
 * Main class - Entry point for College Management System
 * 
 * This application manages all college operations including:
 * - Student and Faculty Management
 * - Course Management
 * - Attendance Tracking
 * - Grade Management
 * - Library Management
 * - Hostel Management
 * - Fee Management
 * - Timetable Management
 */
public class Main {

    public static void main(String[] args) {
        // Set Nimbus Look and Feel for modern UI
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to set Nimbus Look and Feel");
            e.printStackTrace();
        }

        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database!\n" +
                            "Please check your MySQL connection settings.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch Login Screen
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
