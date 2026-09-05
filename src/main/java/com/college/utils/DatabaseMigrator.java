package com.college.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Database migrator that applies SQL migration scripts.
 * Supports both PostgreSQL (primary) and H2 (fallback) databases.
 * 
 * Migration scripts are written in PostgreSQL syntax. When running against H2
 * (MODE=PostgreSQL), most syntax is compatible natively.
 */
public class DatabaseMigrator {

    public static void migrate() {
        System.out.println("Starting Database Migration...");
        System.out.println("Active Database: " + DatabaseConnection.getActiveDatabase().getDisplayName());

        if (DatabaseConnection.isUsingFallback()) {
            System.out.println("[Migration] H2 fallback is active; schema was already initialized by H2SchemaInitializer. Skipping PostgreSQL migrations.");
            return;
        }
        
        String[] migrations = {
            "V1__Supabase_Schema.sql",
            "V2__Fix_Schema_Permissions_Wardens.sql",
            "V3__Restore_Missing_Tables.sql",
            "V4__Fix_Column_Names.sql",
            "V5__Restore_Employees_And_Columns.sql",
            "V6__Restore_All_Remaining_Tables.sql",
            "V7__Fix_Runtime_Schema_Issues.sql",
            "V7__Fix_Schema_And_Constraints.sql",
            "V8__Add_Enrollment_Id.sql",
            "V9__Cascade_Delete_Constraints.sql",
            "V10__Fix_Attendance_Column.sql",
            "V11__Fix_Club_Membership_Status.sql",
            "V12__Add_Approved_Date_To_BookRequests.sql",
            "V34__Add_Student_Leaves.sql",
            "V35__Add_Staff_Leaves.sql",
            "V36__Add_Payroll_Permission.sql",
            "V40__Add_New_Feature_Permissions.sql",
            "V41__Add_System_Settings.sql",
            "V43__Add_Placement_Cell.sql",
            "V44__Add_View_Placement_Permission.sql",
            "V45__Add_Visitor_Management.sql",
            "V46__Add_Manage_Rooms_Permission.sql",
            "V47__Grant_Room_Check_Permission.sql",
            "V48__Add_Created_At_To_Courses.sql",
            "V49__Fix_Runtime_Schema_Issues.sql",
            "V51__Complete_Course_Schema.sql",
            "V53__Complete_Faculty_And_Content_Schema.sql",
            "V62__Add_Track_Specialization.sql",
            "V63__Add_Track_To_Program_Fees.sql"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String fileName : migrations) {
                String path = "/db/migration/" + fileName;
                try (InputStream is = DatabaseMigrator.class.getResourceAsStream(path)) {
                    if (is == null) {
                        System.err.println("Migration file not found: " + path);
                        continue;
                    }

                    String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    // Adapt SQL for H2 if running in fallback mode
                    if (DatabaseConnection.isUsingFallback()) {
                        sql = adaptForH2(sql);
                    }

                    // Execute migration
                    stmt.execute(sql);
                    System.out.println(fileName + " executed successfully!");
                } catch (Exception e) {
                    System.err.println("Failed to execute " + fileName + ": " + e.getMessage());
                    // Continue with others if possible
                }
            }
            System.out.println("Database Migration Complete!");

        } catch (Exception e) {
            System.err.println("Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adapt PostgreSQL SQL for H2 compatibility.
     * Most syntax works with MODE=PostgreSQL, but some edge cases need fixing.
     */
    private static String adaptForH2(String sql) {
        String adapted = sql;

        // Remove PostgreSQL-specific extension commands
        adapted = adapted.replaceAll("(?i)CREATE\\s+EXTENSION\\s+.*?;", "-- extension removed for H2");

        // Remove SET search_path
        adapted = adapted.replaceAll("(?i)SET\\s+search_path\\s*=.*?;", "-- search_path removed for H2");

        return adapted;
    }
}
