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
            "V48__Add_Created_At_To_Courses.sql"
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

                    // Execute migration
                    stmt.execute(sql);
                    System.out.println(fileName + " executed successfully!");
                } catch (Exception e) {
                    System.err.println("Failed to execute " + fileName + ": " + e.getMessage());
                    // Continue with others if possible, or break if critical
                }
            }
            System.out.println("Database Migration Complete!");

        } catch (Exception e) {
            System.err.println("Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
