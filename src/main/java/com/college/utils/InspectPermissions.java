package com.college.utils;

import java.sql.*;

public class InspectPermissions {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println(String.format("%-30s | %-20s | %-50s", "CODE", "CATEGORY", "NAME"));
            System.out.println(
                    "------------------------------------------------------------------------------------------------");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT code, category, name FROM permissions ORDER BY category, code");
            while (rs.next()) {
                System.out.println(String.format("%-30s | %-20s | %-50s",
                        rs.getString("code"),
                        rs.getString("category"),
                        rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
