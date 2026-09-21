package com.college.utils;

/** Expected management API failures; safe messages may be shown to users. */
public class ManagementException extends RuntimeException {
    private final int status;
    public ManagementException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
    public static ManagementException database(java.sql.SQLException error) {
        Logger.error("Management database operation failed", error);
        String state = error.getSQLState();
        if ("23505".equals(state)) return new ManagementException(409, "This identifier or record already exists.");
        if ("23503".equals(state)) return new ManagementException(409, "This record is referenced by other records or its parent no longer exists.");
        return new ManagementException(500, "Could not complete the database operation. Please retry.");
    }
}
