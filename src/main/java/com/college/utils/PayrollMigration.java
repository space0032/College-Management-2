package com.college.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** No automatic financial-history cleanup: duplicates require explicit review. */
public final class PayrollMigration {
    private PayrollMigration() {}
    public static final class Failure extends RuntimeException {
        Failure(String message, Throwable cause) { super(message, cause); }
    }
    public static void apply(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            StringBuilder duplicates = new StringBuilder();
            try (ResultSet rows = statement.executeQuery("SELECT employee_id, month, year, COUNT(*) FROM payroll_entries GROUP BY employee_id, month, year HAVING COUNT(*) > 1")) {
                while (rows.next()) duplicates.append(" employee=").append(rows.getInt(1)).append(" period=").append(rows.getInt(2)).append('/').append(rows.getInt(3)).append(" records=").append(rows.getInt(4)).append(';');
            }
            if (duplicates.length() > 0) throw new Failure("Payroll migration stopped. Review duplicate periods; no records were deleted:" + duplicates, null);
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_payroll_employee_period ON payroll_entries (employee_id, month, year)");
        } catch (java.sql.SQLException e) { throw new Failure("Payroll uniqueness migration failed; financial records were not changed.", e); }
    }
}
