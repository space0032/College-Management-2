package com.college.utils;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * H2 Trigger implementation that logs all data changes to the _sync_journal table.
 * 
 * This trigger is installed on every tracked table via ChangeTracker.
 * When a row is inserted, updated, or deleted, this trigger fires and 
 * records the change so it can be replayed to PostgreSQL when it recovers.
 * 
 * Implements org.h2.api.Trigger for H2's Java trigger mechanism.
 */
public class SyncTrigger implements Trigger {

    private String tableName;
    private int triggerType;

    @Override
    public void init(Connection conn, String schemaName, String triggerName,
                     String tableName, boolean before, int type) throws SQLException {
        this.tableName = tableName.toLowerCase();
        this.triggerType = type;
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        // Skip if tracking is not enabled
        if (!ChangeTracker.isTrackingEnabled()) {
            return;
        }

        // Skip changes to our own meta tables
        if (tableName.startsWith("_sync_") || tableName.equals("schema_version")) {
            return;
        }

        try {
            String operation;
            String rowData = null;
            String oldRowData = null;
            String primaryKeyValue = null;

            // Determine operation type and build data
            if (triggerType == INSERT) {
                operation = "INSERT";
                rowData = rowToJson(conn, tableName, newRow);
                primaryKeyValue = getPrimaryKeyValue(newRow);
            } else if (triggerType == UPDATE) {
                operation = "UPDATE";
                rowData = rowToJson(conn, tableName, newRow);
                oldRowData = rowToJson(conn, tableName, oldRow);
                primaryKeyValue = getPrimaryKeyValue(newRow);
            } else if (triggerType == DELETE) {
                operation = "DELETE";
                oldRowData = rowToJson(conn, tableName, oldRow);
                primaryKeyValue = getPrimaryKeyValue(oldRow);
            } else {
                return; // Unknown operation
            }

            // Insert into sync journal
            String sql = "INSERT INTO _sync_journal (table_name, operation, primary_key_column, " +
                         "primary_key_value, row_data, old_row_data) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tableName);
                pstmt.setString(2, operation);
                pstmt.setString(3, "id"); // Most tables use 'id' as PK
                pstmt.setString(4, primaryKeyValue);
                pstmt.setString(5, rowData);
                pstmt.setString(6, oldRowData);
                pstmt.executeUpdate();
            }

        } catch (Exception e) {
            // Don't let trigger errors break the main operation
            System.err.println("[SyncTrigger] Error logging change for " + tableName + ": " + e.getMessage());
        }
    }

    @Override
    public void close() throws SQLException {
        // No cleanup needed
    }

    @Override
    public void remove() throws SQLException {
        // No cleanup needed
    }

    /**
     * Convert a row (Object array) to a JSON string.
     * Uses the table's column metadata to build proper key-value pairs.
     */
    private String rowToJson(Connection conn, String table, Object[] row) {
        if (row == null) return null;

        try {
            // Get column names from table metadata
            String[] columnNames = getColumnNames(conn, table);

            StringBuilder json = new StringBuilder("{");
            for (int i = 0; i < row.length && i < columnNames.length; i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(escapeJson(columnNames[i])).append("\":");

                if (row[i] == null) {
                    json.append("null");
                } else if (row[i] instanceof Number) {
                    json.append(row[i]);
                } else if (row[i] instanceof Boolean) {
                    json.append(row[i]);
                } else {
                    json.append("\"").append(escapeJson(row[i].toString())).append("\"");
                }
            }
            json.append("}");
            return json.toString();

        } catch (Exception e) {
            // Fallback: just serialize values without column names
            StringBuilder json = new StringBuilder("{");
            for (int i = 0; i < row.length; i++) {
                if (i > 0) json.append(",");
                json.append("\"col_").append(i).append("\":");
                if (row[i] == null) {
                    json.append("null");
                } else {
                    json.append("\"").append(escapeJson(row[i].toString())).append("\"");
                }
            }
            json.append("}");
            return json.toString();
        }
    }

    /**
     * Get column names for a table from database metadata
     */
    private String[] getColumnNames(Connection conn, String table) throws SQLException {
        // Use a quick metadata query
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(), null)) {
            java.util.List<String> columns = new java.util.ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            return columns.toArray(new String[0]);
        }
    }

    /**
     * Extract the primary key value from a row.
     * Assumes the first column is the primary key (id).
     */
    private String getPrimaryKeyValue(Object[] row) {
        if (row != null && row.length > 0 && row[0] != null) {
            return row[0].toString();
        }
        return "unknown";
    }

    /**
     * Escape special characters for JSON strings
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
