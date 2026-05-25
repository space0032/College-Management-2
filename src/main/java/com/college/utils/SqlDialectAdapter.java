package com.college.utils;

/**
 * SQL Dialect Adapter for cross-database compatibility.
 * Handles syntax differences between PostgreSQL and H2 (even in PG compatibility mode).
 * 
 * H2 in MODE=PostgreSQL supports most PG syntax natively:
 * - SERIAL / BIGSERIAL
 * - ILIKE
 * - ON CONFLICT ... DO UPDATE / DO NOTHING
 * - BOOLEAN types
 * - TIMESTAMP defaults
 * 
 * This adapter handles the remaining edge cases.
 */
public class SqlDialectAdapter {

    public enum Dialect {
        POSTGRESQL, H2
    }

    private static volatile Dialect currentDialect = Dialect.POSTGRESQL;

    /**
     * Set the current SQL dialect based on active database
     */
    public static void setDialect(Dialect dialect) {
        currentDialect = dialect;
        Logger.info("[SqlDialect] Active dialect set to: " + dialect);
    }

    /**
     * Get the current active dialect
     */
    public static Dialect getDialect() {
        return currentDialect;
    }

    /**
     * Adapt SQL for the current dialect.
     * Most SQL is compatible between PG and H2 (MODE=PostgreSQL).
     * This handles remaining edge cases.
     * 
     * @param sql The original SQL (written for PostgreSQL)
     * @return The adapted SQL for the current dialect
     */
    public static String adapt(String sql) {
        if (currentDialect == Dialect.POSTGRESQL) {
            return sql; // No adaptation needed for the native dialect
        }

        // H2 adaptations (edge cases not covered by MODE=PostgreSQL)
        String adapted = sql;

        // H2 doesn't support "RETURNING id" clause in all contexts
        // DAOs already use Statement.RETURN_GENERATED_KEYS, so strip RETURNING if present
        // Only strip simple "RETURNING id" at end of statements
        if (adapted.toUpperCase().contains("RETURNING ")) {
            adapted = adapted.replaceAll("(?i)\\s+RETURNING\\s+\\w+(\\s*,\\s*\\w+)*\\s*$", "");
        }

        return adapted;
    }

    /**
     * Check if the current dialect is the fallback (H2)
     */
    public static boolean isFallbackDialect() {
        return currentDialect == Dialect.H2;
    }

    /**
     * Get the JDBC driver class name for the given dialect
     */
    public static String getDriverClassName(Dialect dialect) {
        return switch (dialect) {
            case POSTGRESQL -> "org.postgresql.Driver";
            case H2 -> "org.h2.Driver";
        };
    }
}
