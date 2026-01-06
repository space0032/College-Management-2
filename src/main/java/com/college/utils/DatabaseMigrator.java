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
                System.out.println("Migration executed successfully!");
            }

        } catch (Exception e) {
            System.err.println("Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
