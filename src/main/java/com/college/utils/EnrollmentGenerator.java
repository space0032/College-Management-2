package com.college.utils;

import com.college.dao.StudentDAO;

import java.util.Calendar;

/**
 * Utility class for generating unique enrollment numbers
 */
public class EnrollmentGenerator {

    /**
     * Generate student enrollment number in format: DEPT_CODE + YEAR + SEQ_NUM
     * Example: CS2023001, EE2023002
     */
    public static String generateStudentEnrollment(String department) {
        String deptCode = getDepartmentCode(department);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String prefix = deptCode + year;

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {

            // Find highest sequence number for this Dept + Year prefix
            String sql = "SELECT username FROM users WHERE username LIKE '" + prefix
                    + "%' ORDER BY username DESC LIMIT 1";
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            int nextNum = 1;
            if (rs.next()) {
                String lastId = rs.getString("username");
                if (lastId.length() >= prefix.length() + 3) {
                    String numPart = lastId.substring(prefix.length());
                    try {
                        nextNum = Integer.parseInt(numPart) + 1;
                    } catch (NumberFormatException e) {
                        nextNum = 1;
                    }
                }
            }
            return prefix + String.format("%03d", nextNum);
        } catch (Exception e) {
            // Fallback unique ID
            return prefix + System.currentTimeMillis() % 1000;
        }
    }

    /**
     * Generate faculty ID in format: FAC + SEQ_NUM
     * Example: FAC001, FAC002
     * Queries users table to find next available ID
     */
    public static String generateFacultyId() {
        try (java.sql.Connection conn = com.college.utils.DatabaseConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {

            // Query to find the highest number in existing FAC IDs
            String sql = "SELECT username FROM users WHERE username LIKE 'FAC%' ORDER BY username DESC LIMIT 1";
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            int nextNum = 1;
            if (rs.next()) {
                String lastId = rs.getString("username");
                // Extract number from FAC001, FAC002, etc.
                String numPart = lastId.substring(3); // Remove "FAC"
                try {
                    nextNum = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException e) {
                    // If parsing fails, start from 1
                    nextNum = 1;
                }
            }

            return "FAC" + String.format("%03d", nextNum);

        } catch (Exception e) {
            // Fallback: use timestamp-based ID to ensure uniqueness
            return "FAC" + System.currentTimeMillis() % 1000;
        }
    }

    /**
     * Get department code from department name
     */
    private static String getDepartmentCode(String department) {
        if (department == null || department.isEmpty()) {
            return "GEN";
        }

        switch (department.toUpperCase()) {
            case "COMPUTER SCIENCE":
            case "CS":
                return "CS";
            case "ELECTRICAL ENGINEERING":
            case "EE":
                return "EE";
            case "MECHANICAL ENGINEERING":
            case "ME":
                return "ME";
            case "CIVIL ENGINEERING":
            case "CE":
                return "CE";
            case "ELECTRONICS AND COMMUNICATION":
            case "ECE":
                return "ECE";
            case "INFORMATION TECHNOLOGY":
            case "IT":
                return "IT";
            default:
                // Use first 2-3 letters as code
                String code = department.replaceAll("[^A-Z]", "");
                return code.length() >= 2 ? code.substring(0, Math.min(3, code.length())) : "GEN";
        }
    }
}
