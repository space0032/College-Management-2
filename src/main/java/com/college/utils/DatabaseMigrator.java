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

                if (conn == null) {
                    System.err.println("Cannot run migration: No connection.");
                    return;
                }

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
            }

        } catch (Exception e) {
            System.err.println("Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
