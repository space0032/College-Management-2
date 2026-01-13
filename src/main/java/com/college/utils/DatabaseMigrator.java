package com.college.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseMigrator {

    public static void migrate() {
        System.out.println("Starting Database Migration...");
        String schemaPath = "/db/migration/V1__Supabase_Schema.sql";

        try (InputStream is = DatabaseMigrator.class.getResourceAsStream(schemaPath)) {
            if (is == null) {
                System.err.println("Migration file not found: " + schemaPath);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Split by semicolon, but be careful about semicolons in strings/functions if
            // any
            // For this simple schema, splitting by ";\n" or similar might be enough,
            // but the schema file uses IF NOT EXISTS, so running the whole block might work
            // if the driver supports multi-statement execution.
            // PostgreSQL driver usually supports allowing multi-statement if valid.

            try (Connection conn = DatabaseConnection.getConnection();
                    Statement stmt = conn.createStatement()) {

                stmt.execute(sql);
                System.out.println("V1 Migration executed successfully!");

                // Execute V7 Fix
                String v7Path = "/db/migration/V7__Fix_Schema_And_Constraints.sql";
                try (InputStream v7is = DatabaseMigrator.class.getResourceAsStream(v7Path)) {
                    if (v7is != null) {
                        String v7sql = new BufferedReader(new InputStreamReader(v7is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v7sql);
                        System.out.println("V7 Fix Migration executed successfully!");
                    } else {
                        System.err.println("V7 Migration file not found!");
                    }
                }

                // Execute V8 Add Enrollment ID
                String v8Path = "/db/migration/V8__Add_Enrollment_Id.sql";
                try (InputStream v8is = DatabaseMigrator.class.getResourceAsStream(v8Path)) {
                    if (v8is != null) {
                        String v8sql = new BufferedReader(new InputStreamReader(v8is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v8sql);
                        System.out.println("V8 Add Enrollment ID Migration executed successfully!");
                    } else {
                        System.err.println("V8 Migration file not found!");
                    }
                }

                // Execute V9 Cascade Delete
                String v9Path = "/db/migration/V9__Cascade_Delete_Constraints.sql";
                try (InputStream v9is = DatabaseMigrator.class.getResourceAsStream(v9Path)) {
                    if (v9is != null) {
                        String v9sql = new BufferedReader(new InputStreamReader(v9is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v9sql);
                        System.out.println("V9 Cascade Delete Migration executed successfully!");
                    } else {
                        System.err.println("V9 Migration file not found!");
                    }
                }

                // Execute V10 Fix Attendance Column
                String v10Path = "/db/migration/V10__Fix_Attendance_Column.sql";
                try (InputStream v10is = DatabaseMigrator.class.getResourceAsStream(v10Path)) {
                    if (v10is != null) {
                        String v10sql = new BufferedReader(new InputStreamReader(v10is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v10sql);
                        System.out.println("V10 Fix Attendance Column Migration executed successfully!");
                    } else {
                        System.err.println("V10 Migration file not found!");
                    }
                }

                // Execute V11 Fix Club Membership Status
                String v11Path = "/db/migration/V11__Fix_Club_Membership_Status.sql";
                try (InputStream v11is = DatabaseMigrator.class.getResourceAsStream(v11Path)) {
                    if (v11is != null) {
                        String v11sql = new BufferedReader(new InputStreamReader(v11is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v11sql);
                        System.out.println("V11 Fix Club Membership Status Migration executed successfully!");
                    } else {
                        System.err.println("V11 Migration file not found!");
                    }
                }

                // Execute V12 Add Approved Date to BookRequests
                String v12Path = "/db/migration/V12__Add_Approved_Date_To_BookRequests.sql";
                try (InputStream v12is = DatabaseMigrator.class.getResourceAsStream(v12Path)) {
                    if (v12is != null) {
                        String v12sql = new BufferedReader(new InputStreamReader(v12is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v12sql);
                        System.out.println("V12 Add Approved Date Migration executed successfully!");
                    } else {
                        System.err.println("V12 Migration file not found!");
                    }
                }

                // Execute V34 Add Student Leaves
                String v34Path = "/db/migration/V34__Add_Student_Leaves.sql";
                try (InputStream v34is = DatabaseMigrator.class.getResourceAsStream(v34Path)) {
                    if (v34is != null) {
                        String v34sql = new BufferedReader(new InputStreamReader(v34is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v34sql);
                        System.out.println("V34 Add Student Leaves Migration executed successfully!");
                    } else {
                        System.err.println("V34 Migration file not found!");
                    }
                }

                // Execute V35 Add Staff Leaves
                String v35Path = "/db/migration/V35__Add_Staff_Leaves.sql";
                try (InputStream v35is = DatabaseMigrator.class.getResourceAsStream(v35Path)) {
                    if (v35is != null) {
                        String v35sql = new BufferedReader(new InputStreamReader(v35is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v35sql);
                        System.out.println("V35 Add Staff Leaves Migration executed successfully!");
                    } else {
                        System.err.println("V35 Migration file not found!");
                    }
                }

                // Execute V36 Add Payroll Permission
                String v36Path = "/db/migration/V36__Add_Payroll_Permission.sql";
                try (InputStream v36is = DatabaseMigrator.class.getResourceAsStream(v36Path)) {
                    if (v36is != null) {
                        String v36sql = new BufferedReader(new InputStreamReader(v36is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v36sql);
                        System.out.println("V36 Add Payroll Permission Migration executed successfully!");
                    } else {
                        System.err.println("V36 Migration file not found!");
                    }
                }

                // Execute V40 Add New Feature Permissions & missing tables
                String v40Path = "/db/migration/V40__Add_New_Feature_Permissions.sql";
                try (InputStream v40is = DatabaseMigrator.class.getResourceAsStream(v40Path)) {
                    if (v40is != null) {
                        String v40sql = new BufferedReader(new InputStreamReader(v40is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v40sql);
                        System.out.println("V40 Add New Feature Permissions Migration executed successfully!");
                    } else {
                        System.err.println("V40 Migration file not found!");
                    }
                }

                // Execute V41 Add System Settings
                String v41Path = "/db/migration/V41__Add_System_Settings.sql";
                try (InputStream v41is = DatabaseMigrator.class.getResourceAsStream(v41Path)) {
                    if (v41is != null) {
                        String v41sql = new BufferedReader(new InputStreamReader(v41is, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        stmt.execute(v41sql);
                        System.out.println("V41 Add System Settings Migration executed successfully!");
                    } else {
                        System.err.println("V41 Migration file not found!");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
