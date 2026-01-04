package com.college.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationRunner {

    public static void runMigrations() {
        System.out.println("[Migration] Checking for pending migrations...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Ensure schema_version table exists
            ensureSchemaVersionTable(conn);

            // 2. Find migration files
            List<String> migrationFiles = findMigrationFiles();

            for (String file : migrationFiles) {
                String version = extractVersion(file);
                if (!isMigrationApplied(conn, version)) {
                    System.out.println("[Migration] Applying " + file + "...");
                    applyMigration(conn, file, version);
                }
            }
            System.out.println("[Migration] Database is up to date.");

        } catch (Exception e) {
            System.err.println("[Migration] Failed to run migrations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureSchemaVersionTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version VARCHAR(50) PRIMARY KEY, " +
                    "file_name VARCHAR(255), " +
                    "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private static List<String> findMigrationFiles() {
        List<String> files = new ArrayList<>();
        try {
            // Use java.nio to properly scan the migration directory
            java.nio.file.Path migrationDir = java.nio.file.Paths.get("src/main/resources/db/migration");

            if (java.nio.file.Files.exists(migrationDir)) {
                files = java.nio.file.Files.list(migrationDir)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(".sql"))
                        .sorted() // Natural sort first
                        .collect(Collectors.toList());

                // Custom sort: V1, V2, ..., V9, V10, V11
                Collections.sort(files, (a, b) -> {
                    try {
                        int verA = Integer.parseInt(a.substring(1, a.indexOf("__")));
                        int verB = Integer.parseInt(b.substring(1, b.indexOf("__")));
                        return Integer.compare(verA, verB);
                    } catch (Exception e) {
                        return a.compareTo(b); // Fallback to lexical
                    }
                });
            }
        } catch (Exception e) {
            Logger.error("Failed to find migration files", e);
        }
        return files;
    }

    private static String extractVersion(String fileName) {
        // V1__Description.sql -> 1
        // We will store "1" or "V1"
        return fileName.split("__")[0];
    }

    private static boolean isMigrationApplied(Connection conn, String version) throws SQLException {
        String sql = "SELECT 1 FROM schema_version WHERE version = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, version);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void applyMigration(Connection conn, String fileName, String version) throws SQLException {
        try {
            // Read SQL content
            String sqlContent;
            try (InputStream is = MigrationRunner.class.getClassLoader()
                    .getResourceAsStream("db/migration/" + fileName)) {
                if (is == null)
                    throw new RuntimeException("Migration file not found: " + fileName);
                sqlContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Execute SQL (Simple split by semicolon for multiple statements, simplistic
            // approach)
            // Ideally we should use a proper SQL parser, but for this scope, simple
            // splitting is fine
            // provided statements don't contain semicolons in string literals.
            String[] statements = sqlContent.split(";");
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }

                // Record migration
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO schema_version (version, file_name) VALUES (?, ?)")) {
                    pstmt.setString(1, version);
                    pstmt.setString(2, fileName);
                    pstmt.executeUpdate();
                }

                conn.commit();
                System.out.println("[Migration] Applied " + fileName);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new SQLException("Error applying migration " + fileName, e);
        }
    }
}
