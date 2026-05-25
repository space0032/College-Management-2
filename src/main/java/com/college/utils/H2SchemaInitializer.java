package com.college.utils;

import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Initializes the H2 fallback database schema.
 * 
 * Reads the same PostgreSQL migration files used for the primary database
 * and executes them against H2 in PostgreSQL compatibility mode.
 * 
 * H2 with MODE=PostgreSQL supports:
 * - SERIAL / BIGSERIAL → auto-mapped to identity columns
 * - ILIKE → case-insensitive LIKE
 * - ON CONFLICT ... DO UPDATE / DO NOTHING
 * - BOOLEAN, TIMESTAMP, TEXT, VARCHAR types
 * - Standard PostgreSQL INSERT/UPDATE/DELETE syntax
 */
public class H2SchemaInitializer {

    /**
     * List of migration files to apply (same as DatabaseMigrator)
     */
    private static final String[] MIGRATIONS = {
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

    /**
     * Initialize the H2 database schema from migration files.
     * Only runs migrations that haven't been applied yet.
     * 
     * @param h2DataSource The H2 HikariCP data source
     */
    public static void initialize(HikariDataSource h2DataSource) {
        Logger.info("[H2SchemaInit] Starting H2 schema initialization...");

        try (Connection conn = h2DataSource.getConnection()) {
            // Create schema version tracking table
            ensureSchemaVersionTable(conn);

            int applied = 0;
            int skipped = 0;

            for (String fileName : MIGRATIONS) {
                String version = extractVersion(fileName);
                
                if (isMigrationApplied(conn, version)) {
                    skipped++;
                    continue;
                }

                try {
                    applyMigration(conn, fileName, version);
                    applied++;
                } catch (Exception e) {
                    Logger.warn("[H2SchemaInit] Migration " + fileName + " failed (may be expected): " + e.getMessage());
                    // Continue with other migrations — some may fail due to 
                    // IF NOT EXISTS / ON CONFLICT which is fine
                }
            }

            Logger.info("[H2SchemaInit] H2 schema initialization complete. Applied: " + applied + ", Skipped: " + skipped);

        } catch (Exception e) {
            Logger.error("[H2SchemaInit] Failed to initialize H2 schema", e);
        }
    }

    /**
     * Create the schema_version tracking table in H2
     */
    private static void ensureSchemaVersionTable(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version VARCHAR(50) PRIMARY KEY, " +
                    "file_name VARCHAR(255), " +
                    "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) {
            Logger.error("[H2SchemaInit] Failed to create schema_version table", e);
        }
    }

    /**
     * Check if a migration has already been applied
     */
    private static boolean isMigrationApplied(Connection conn, String version) {
        try (var pstmt = conn.prepareStatement("SELECT 1 FROM schema_version WHERE version = ?")) {
            pstmt.setString(1, version);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false; // Table might not exist yet
        }
    }

    /**
     * Apply a single migration file to H2
     */
    private static void applyMigration(Connection conn, String fileName, String version) throws Exception {
        String path = "/db/migration/" + fileName;
        
        try (InputStream is = H2SchemaInitializer.class.getResourceAsStream(path)) {
            if (is == null) {
                Logger.warn("[H2SchemaInit] Migration file not found: " + path);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Adapt SQL for H2 if needed
            sql = adaptSqlForH2(sql);

            // Execute migration (split by semicolons for multiple statements)
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                String[] statements = sql.split(";");
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception e) {
                            // Log but continue — some statements may fail in H2
                            // (e.g., PG-specific ALTER statements)
                            Logger.debug("[H2SchemaInit] Statement skipped in " + fileName + ": " + e.getMessage());
                        }
                    }
                }

                // Record migration as applied
                try (var pstmt = conn.prepareStatement(
                        "INSERT INTO schema_version (version, file_name) VALUES (?, ?)")) {
                    pstmt.setString(1, version);
                    pstmt.setString(2, fileName);
                    pstmt.executeUpdate();
                }

                conn.commit();
                Logger.info("[H2SchemaInit] Applied migration: " + fileName);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Adapt PostgreSQL SQL to be H2-compatible.
     * Most syntax works natively with MODE=PostgreSQL, but some edge cases need fixing.
     */
    private static String adaptSqlForH2(String sql) {
        String adapted = sql;

        // H2 in PG mode handles SERIAL, but if any issue arises, we can patch here
        // Most CREATE TABLE, INSERT, ON CONFLICT statements work as-is

        // Remove any PostgreSQL-specific extension commands
        adapted = adapted.replaceAll("(?i)CREATE\\s+EXTENSION\\s+.*?;", "-- extension removed for H2");

        // Remove any SET statements that are PG-specific
        adapted = adapted.replaceAll("(?i)SET\\s+search_path\\s*=.*?;", "-- search_path removed for H2");

        return adapted;
    }

    /**
     * Extract version identifier from migration filename.
     * e.g., "V1__Supabase_Schema.sql" → "V1"
     */
    private static String extractVersion(String fileName) {
        return fileName.split("__")[0];
    }
}
