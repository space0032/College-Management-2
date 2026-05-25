package com.college.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Sync Engine — replays H2 changes back to PostgreSQL.
 * 
 * When PostgreSQL recovers after a failover period, this engine:
 * 1. Reads all unsynced entries from H2's _sync_journal
 * 2. Replays each change (INSERT/UPDATE/DELETE) against PostgreSQL
 * 3. Marks successfully synced entries in the journal
 * 4. Reports sync status via callbacks
 * 
 * Sync Strategy:
 * - INSERT: Uses INSERT ... ON CONFLICT (id) DO UPDATE for upsert safety
 * - UPDATE: Executes UPDATE ... WHERE id = ?
 * - DELETE: Executes DELETE ... WHERE id = ?
 * - All operations run in a single transaction for consistency
 */
public class DataSyncEngine {

    private static final Gson gson = new Gson();

    /**
     * Sync all pending H2 changes to PostgreSQL.
     * 
     * @return true if sync completed successfully, false if it failed
     */
    public static boolean syncToPostgres() {
        Logger.info("[DataSync] ═══════════════════════════════════════════════");
        Logger.info("[DataSync] Starting data sync: H2 → PostgreSQL...");

        List<ChangeTracker.SyncEntry> entries = ChangeTracker.getUnsyncedEntries();

        if (entries.isEmpty()) {
            Logger.info("[DataSync] No pending changes to sync.");
            return true;
        }

        Logger.info("[DataSync] Found " + entries.size() + " pending changes to sync.");

        HikariDataSource pgDs = DatabaseConnection.getPostgresDataSource();
        if (pgDs == null || pgDs.isClosed()) {
            Logger.error("[DataSync] PostgreSQL data source not available for sync.");
            return false;
        }

        Connection pgConn = null;
        int synced = 0;
        int failed = 0;
        List<Long> syncedIds = new ArrayList<>();

        try {
            pgConn = pgDs.getConnection();
            pgConn.setAutoCommit(false);

            for (ChangeTracker.SyncEntry entry : entries) {
                try {
                    boolean success = replayEntry(pgConn, entry);
                    if (success) {
                        syncedIds.add(entry.id);
                        synced++;
                    } else {
                        failed++;
                        Logger.warn("[DataSync] Failed to sync entry: " + entry);
                    }
                } catch (Exception e) {
                    failed++;
                    Logger.warn("[DataSync] Error syncing entry [" + entry.tableName + "/" + 
                               entry.operation + "/" + entry.primaryKeyValue + "]: " + e.getMessage());
                    // Continue with other entries — don't let one failure stop the whole sync
                }
            }

            // Commit all successful replays
            pgConn.commit();

            // Mark synced entries in H2
            if (!syncedIds.isEmpty()) {
                ChangeTracker.markAsSynced(syncedIds);
            }

            Logger.info("[DataSync] Sync complete. Synced: " + synced + ", Failed: " + failed);
            Logger.info("[DataSync] ═══════════════════════════════════════════════");

            // Clean up synced entries from the journal
            ChangeTracker.clearSyncedEntries();

            // Notify listener
            DatabaseConnection.DatabaseStatusListener listener = DatabaseConnection.getStatusListener();
            if (listener != null) {
                listener.onSyncComplete(failed == 0,
                        "Synced " + synced + " changes" + (failed > 0 ? " (" + failed + " failed)" : ""));
            }

            return failed == 0;

        } catch (Exception e) {
            Logger.error("[DataSync] Critical sync error, rolling back", e);
            if (pgConn != null) {
                try {
                    pgConn.rollback();
                } catch (SQLException ex) {
                    Logger.error("[DataSync] Rollback failed", ex);
                }
            }
            return false;

        } finally {
            if (pgConn != null) {
                try {
                    pgConn.setAutoCommit(true);
                    pgConn.close();
                } catch (SQLException e) {
                    Logger.error("[DataSync] Error closing PG connection", e);
                }
            }
        }
    }

    /**
     * Replay a single sync journal entry against PostgreSQL
     */
    private static boolean replayEntry(Connection pgConn, ChangeTracker.SyncEntry entry) throws Exception {
        return switch (entry.operation) {
            case "INSERT" -> replayInsert(pgConn, entry);
            case "UPDATE" -> replayUpdate(pgConn, entry);
            case "DELETE" -> replayDelete(pgConn, entry);
            default -> {
                Logger.warn("[DataSync] Unknown operation: " + entry.operation);
                yield false;
            }
        };
    }

    /**
     * Replay an INSERT operation as an upsert (INSERT ... ON CONFLICT DO UPDATE)
     */
    private static boolean replayInsert(Connection pgConn, ChangeTracker.SyncEntry entry) throws Exception {
        if (entry.rowData == null || entry.rowData.isEmpty()) {
            Logger.warn("[DataSync] No row data for INSERT on " + entry.tableName);
            return false;
        }

        JsonObject data = JsonParser.parseString(entry.rowData).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> fields = data.entrySet();

        if (fields.isEmpty()) return false;

        // Build column lists
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updateClause = new StringBuilder();
        List<Object> values = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, JsonElement> field : fields) {
            String colName = field.getKey();
            
            if (i > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(colName);
            placeholders.append("?");

            // Build ON CONFLICT update clause (skip PK column)
            if (!colName.equals(entry.primaryKeyColumn) && !colName.equals("id")) {
                if (updateClause.length() > 0) updateClause.append(", ");
                updateClause.append(colName).append(" = EXCLUDED.").append(colName);
            }

            values.add(jsonElementToJava(field.getValue()));
            i++;
        }

        // Determine conflict column - try to find the primary key
        String conflictColumn = (entry.primaryKeyColumn != null && !entry.primaryKeyColumn.isEmpty()) 
                                ? entry.primaryKeyColumn : "id";

        // Build upsert SQL
        String sql;
        if (updateClause.length() > 0) {
            sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO UPDATE SET %s",
                    entry.tableName, columns, placeholders, conflictColumn, updateClause);
        } else {
            sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO NOTHING",
                    entry.tableName, columns, placeholders, conflictColumn);
        }

        try (PreparedStatement pstmt = pgConn.prepareStatement(sql)) {
            for (int j = 0; j < values.size(); j++) {
                setParameter(pstmt, j + 1, values.get(j));
            }
            pstmt.executeUpdate();
            return true;
        }
    }

    /**
     * Replay an UPDATE operation
     */
    private static boolean replayUpdate(Connection pgConn, ChangeTracker.SyncEntry entry) throws Exception {
        if (entry.rowData == null || entry.rowData.isEmpty()) {
            Logger.warn("[DataSync] No row data for UPDATE on " + entry.tableName);
            return false;
        }

        JsonObject data = JsonParser.parseString(entry.rowData).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> fields = data.entrySet();

        if (fields.isEmpty()) return false;

        // Build SET clause (exclude primary key)
        StringBuilder setClause = new StringBuilder();
        List<Object> values = new ArrayList<>();
        Object pkValue = null;

        for (Map.Entry<String, JsonElement> field : fields) {
            String colName = field.getKey();
            Object value = jsonElementToJava(field.getValue());

            if (colName.equals("id") || colName.equals(entry.primaryKeyColumn)) {
                pkValue = value;
                continue; // Skip PK in SET clause
            }

            if (setClause.length() > 0) setClause.append(", ");
            setClause.append(colName).append(" = ?");
            values.add(value);
        }

        if (pkValue == null) {
            // Try to use the stored primary key value
            pkValue = entry.primaryKeyValue;
        }

        if (pkValue == null || setClause.length() == 0) {
            Logger.warn("[DataSync] Cannot build UPDATE for " + entry.tableName + " — missing PK or no fields");
            return false;
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s = ?",
                entry.tableName, setClause, 
                entry.primaryKeyColumn != null ? entry.primaryKeyColumn : "id");

        try (PreparedStatement pstmt = pgConn.prepareStatement(sql)) {
            for (int j = 0; j < values.size(); j++) {
                setParameter(pstmt, j + 1, values.get(j));
            }
            setParameter(pstmt, values.size() + 1, pkValue);

            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                // Row doesn't exist in PG — try INSERT instead
                Logger.debug("[DataSync] UPDATE affected 0 rows for " + entry.tableName + 
                            " PK=" + pkValue + ", attempting INSERT...");
                return replayInsert(pgConn, entry);
            }
            return true;
        }
    }

    /**
     * Replay a DELETE operation
     */
    private static boolean replayDelete(Connection pgConn, ChangeTracker.SyncEntry entry) throws Exception {
        if (entry.primaryKeyValue == null || entry.primaryKeyValue.equals("unknown")) {
            Logger.warn("[DataSync] No primary key for DELETE on " + entry.tableName);
            return false;
        }

        String sql = String.format("DELETE FROM %s WHERE %s = ?",
                entry.tableName,
                entry.primaryKeyColumn != null ? entry.primaryKeyColumn : "id");

        try (PreparedStatement pstmt = pgConn.prepareStatement(sql)) {
            // Try to parse PK as integer (most tables use int PKs)
            try {
                pstmt.setInt(1, Integer.parseInt(entry.primaryKeyValue));
            } catch (NumberFormatException e) {
                pstmt.setString(1, entry.primaryKeyValue);
            }

            pstmt.executeUpdate();
            return true; // DELETE succeeding with 0 rows is fine (already deleted)
        }
    }

    // ─── Helper Methods ──────────────────────────────────────────────────

    /**
     * Convert a Gson JsonElement to a Java object
     */
    private static Object jsonElementToJava(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            var prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) {
                // Try integer first, then double
                try {
                    long longVal = prim.getAsLong();
                    if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                        return (int) longVal;
                    }
                    return longVal;
                } catch (Exception e) {
                    return prim.getAsDouble();
                }
            }
            return prim.getAsString();
        }
        // For arrays/objects, return as string
        return element.toString();
    }

    /**
     * Set a PreparedStatement parameter with type-appropriate method
     */
    private static void setParameter(PreparedStatement pstmt, int index, Object value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.VARCHAR);
        } else if (value instanceof Integer) {
            pstmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            pstmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            pstmt.setDouble(index, (Double) value);
        } else if (value instanceof Boolean) {
            pstmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof String) {
            // Check if it looks like a timestamp
            String str = (String) value;
            if (str.matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}.*")) {
                try {
                    pstmt.setTimestamp(index, Timestamp.valueOf(str.replace("T", " ")));
                    return;
                } catch (Exception ignored) {}
            }
            // Check if it looks like a date
            if (str.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    pstmt.setDate(index, Date.valueOf(str));
                    return;
                } catch (Exception ignored) {}
            }
            pstmt.setString(index, str);
        } else {
            pstmt.setString(index, value.toString());
        }
    }
}
