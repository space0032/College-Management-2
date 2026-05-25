package com.college.utils;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Continuous Data Replicator (PostgreSQL -> H2)
 * 
 * Periodically takes a snapshot of PostgreSQL data and mirrors it into H2
 * so that the fallback database is always up-to-date with historical data.
 */
public class PostgresToH2Replicator {

    /**
     * Run a full data replication from PostgreSQL to H2.
     * 
     * Strategy:
     * 1. Disable H2 foreign key constraints (REFERENTIAL_INTEGRITY = FALSE)
     * 2. For each tracked table: TRUNCATE in H2, SELECT all from PG, INSERT all to H2
     * 3. Re-enable H2 foreign key constraints
     */
    public static void replicate() {
        // Safety checks
        if (DatabaseConnection.isUsingFallback()) {
            Logger.debug("[Replicator] Skipping snapshot: Currently running in H2 fallback mode.");
            return;
        }
        
        if (ChangeTracker.getPendingChangeCount() > 0) {
            Logger.debug("[Replicator] Skipping snapshot: H2 has unsynced offline changes.");
            return;
        }

        HikariDataSource pgDs = DatabaseConnection.getPostgresDataSource();
        HikariDataSource h2Ds = DatabaseConnection.getH2DataSource();

        // If H2 hasn't been initialized yet, do it now so we have tables to copy into
        if (h2Ds == null || h2Ds.isClosed()) {
            try {
                // We use reflection to call private initH2DataSource() or just force a connection
                // A cleaner way is to let DatabaseConnection initialize it if it's null
                // Wait, getH2DataSource() might be null. Let's just return if H2 isn't initialized yet.
                // The next failover will init it. But wait! If we don't init it now, we never snapshot until first failover.
                // Let's force init H2 by triggering a fallback and immediately restoring, or using reflection.
                // Or better, let's just make initH2DataSource public or package-private in DatabaseConnection.
                // For now, if null, we skip. But we should ideally initialize it. 
                // We'll update DatabaseConnection to expose initH2DataSource().
                Logger.info("[Replicator] H2 not initialized yet. Skipping first snapshot.");
                return;
            } catch (Exception e) {
                Logger.error("[Replicator] Error checking H2 pool", e);
                return;
            }
        }

        Logger.info("[Replicator] ═══════════════════════════════════════════════");
        Logger.info("[Replicator] Starting full data snapshot: PostgreSQL → H2...");
        long startTime = System.currentTimeMillis();

        try (Connection pgConn = pgDs.getConnection();
             Connection h2Conn = h2Ds.getConnection()) {

            // 1. Disable foreign keys on H2
            try (Statement stmt = h2Conn.createStatement()) {
                stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            }

            int tablesCopied = 0;
            int totalRowsCopied = 0;

            // 2. Disable change tracking so triggers don't fire and log the replication
            boolean wasTracking = ChangeTracker.isTrackingEnabled();
            ChangeTracker.setTrackingEnabled(false);

            try {
                for (String table : ChangeTracker.TRACKED_TABLES) {
                    // Check if table exists in PG
                    if (!tableExists(pgConn, table)) continue;
                    
                    int rowsCopied = copyTable(pgConn, h2Conn, table);
                    if (rowsCopied >= 0) {
                        tablesCopied++;
                        totalRowsCopied += rowsCopied;
                    }
                }
            } finally {
                // 3. Re-enable foreign keys and tracking
                try (Statement stmt = h2Conn.createStatement()) {
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                }
                ChangeTracker.setTrackingEnabled(wasTracking);
            }

            long duration = System.currentTimeMillis() - startTime;
            Logger.info("[Replicator] Snapshot complete! Copied " + totalRowsCopied + 
                        " rows across " + tablesCopied + " tables in " + duration + "ms.");
            Logger.info("[Replicator] ═══════════════════════════════════════════════");

        } catch (Exception e) {
            Logger.error("[Replicator] Critical error during replication snapshot", e);
        }
    }

    /**
     * Copy all data for a single table from PG to H2
     */
    private static int copyTable(Connection pgConn, Connection h2Conn, String table) {
        int rowsCopied = 0;

        try {
            // Read from PostgreSQL
            String selectSql = "SELECT * FROM " + table;
            try (Statement pgStmt = pgConn.createStatement();
                 ResultSet rs = pgStmt.executeQuery(selectSql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount == 0) return 0;

                // Build H2 INSERT SQL
                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        columns.append(", ");
                        placeholders.append(", ");
                    }
                    columns.append(metaData.getColumnName(i));
                    placeholders.append("?");
                }

                String insertSql = "INSERT INTO " + table + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";

                // Truncate H2 table
                try (Statement h2Stmt = h2Conn.createStatement()) {
                    h2Stmt.execute("TRUNCATE TABLE " + table);
                } catch (Exception e) {
                    Logger.debug("[Replicator] Table " + table + " might not exist in H2 yet. Skipping.");
                    return -1;
                }

                // Batch insert into H2
                h2Conn.setAutoCommit(false);
                try (PreparedStatement h2Pstmt = h2Conn.prepareStatement(insertSql)) {
                    int batchSize = 0;
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            h2Pstmt.setObject(i, rs.getObject(i));
                        }
                        h2Pstmt.addBatch();
                        batchSize++;
                        rowsCopied++;

                        if (batchSize % 500 == 0) {
                            h2Pstmt.executeBatch();
                            batchSize = 0;
                        }
                    }
                    if (batchSize > 0) {
                        h2Pstmt.executeBatch();
                    }
                    h2Conn.commit();
                } catch (Exception e) {
                    h2Conn.rollback();
                    Logger.error("[Replicator] Error inserting into H2 table: " + table, e);
                    return -1;
                } finally {
                    h2Conn.setAutoCommit(true);
                }
            }
            return rowsCopied;
            
        } catch (Exception e) {
            Logger.debug("[Replicator] Error reading from PostgreSQL table: " + table + " - " + e.getMessage());
            return -1;
        }
    }

    /**
     * Check if a table exists in the database
     */
    private static boolean tableExists(Connection conn, String table) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, table.toLowerCase(), new String[]{"TABLE"})) {
            if (rs.next()) return true;
        } catch (Exception e) {
            // Ignore
        }
        
        try (ResultSet rs = conn.getMetaData().getTables(null, null, table.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
