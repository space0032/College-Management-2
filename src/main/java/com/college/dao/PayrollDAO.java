package com.college.dao;

import com.college.models.PayrollEntry;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class PayrollDAO {
    @FunctionalInterface public interface ConnectionProvider { Connection open() throws SQLException; }
    private final ConnectionProvider connections;
    public PayrollDAO() { this(DatabaseConnection::getConnection); }
    public PayrollDAO(ConnectionProvider connections) { this.connections = connections; }

    public List<Integer> generateBatch(List<PayrollEntry> entries) {
        List<Integer> inserted = new ArrayList<>();
        try (Connection conn = connections.open()) {
            conn.setAutoCommit(false);
            try {
                // All writers lock the same employee rows in ascending order before checking periods.
                List<PayrollEntry> sorted = new ArrayList<>(entries);
                sorted.sort(java.util.Comparator.comparingInt(PayrollEntry::getEmployeeId));
                for (PayrollEntry entry : sorted) {
                    validateEntry(entry);
                    try (PreparedStatement lock = conn.prepareStatement("SELECT id FROM employees WHERE id = ? FOR UPDATE")) {
                        lock.setInt(1, entry.getEmployeeId());
                        try (ResultSet rs = lock.executeQuery()) { if (!rs.next()) throw new com.college.utils.ManagementException(409, "Employee profile no longer exists."); }
                    }
                    try (PreparedStatement find = conn.prepareStatement("SELECT id FROM payroll_entries WHERE employee_id = ? AND month = ? AND year = ?")) {
                        find.setInt(1, entry.getEmployeeId()); find.setInt(2, entry.getMonth()); find.setInt(3, entry.getYear());
                        try (ResultSet rs = find.executeQuery()) { if (rs.next()) continue; }
                    }
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO payroll_entries (employee_id, month, year, basic_salary, bonuses, deductions, net_salary, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        entry.calculateNet();
                        insert.setInt(1, entry.getEmployeeId()); insert.setInt(2, entry.getMonth()); insert.setInt(3, entry.getYear());
                        insert.setBigDecimal(4, entry.getBasicSalary()); insert.setBigDecimal(5, entry.getBonuses()); insert.setBigDecimal(6, entry.getDeductions()); insert.setBigDecimal(7, entry.getNetSalary()); insert.setString(8, "PENDING");
                        insert.executeUpdate(); inserted.add(entry.getEmployeeId());
                    }
                }
                conn.commit();
                return inserted;
            } catch (Exception e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { throw com.college.utils.ManagementException.database(e); }
    }

    private static void validateEntry(PayrollEntry entry) {
        if (entry.getEmployeeId() <= 0 || entry.getMonth() < 1 || entry.getMonth() > 12 || entry.getYear() < 1 || entry.getYear() > 9999)
            throw new com.college.utils.ManagementException(400, "Invalid payroll employee or period.");
        for (java.math.BigDecimal amount : new java.math.BigDecimal[]{entry.getBasicSalary(), entry.getBonuses(), entry.getDeductions()}) {
            if (amount == null || amount.signum() < 0 || amount.compareTo(new java.math.BigDecimal("99999999.99")) > 0 || amount.stripTrailingZeros().scale() > 2)
                throw new com.college.utils.ManagementException(400, "Invalid payroll amount.");
        }
        entry.calculateNet();
        if (entry.getNetSalary().signum() < 0 || entry.getNetSalary().compareTo(new java.math.BigDecimal("99999999.99")) > 0)
            throw new com.college.utils.ManagementException(400, "Net salary is outside the supported range.");
    }

    public PayrollEntry getById(int id) {
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement("SELECT * FROM payroll_entries WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? mapResultSetToPayrollEntry(rs) : null; }
        } catch (SQLException e) { throw com.college.utils.ManagementException.database(e); }
    }


    public boolean createPayrollEntry(PayrollEntry entry) {
        return !generateBatch(java.util.List.of(entry)).isEmpty();
    }

    public List<PayrollEntry> getHistoryByEmployee(int employeeId) {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries WHERE employee_id = ? ORDER BY year DESC, month DESC";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
        return list;
    }

    public boolean markAsPaid(int payrollId) {
        String sql = "UPDATE payroll_entries SET status = 'PAID', payment_date = ? WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, payrollId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    public boolean markMonthAsPaid(int month, int year) {
        String sql = "UPDATE payroll_entries SET status = 'PAID', payment_date = ? WHERE month = ? AND year = ? AND status = 'PENDING'";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            return pstmt.executeUpdate() >= 0; // Return true even if 0 rows updated (no error)
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    public List<PayrollEntry> getAllPayrollEntries() {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries ORDER BY year DESC, month DESC, employee_id";
        try (Connection conn = connections.open();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
        return list;
    }

    public boolean updatePayrollEntry(PayrollEntry entry) {
        validateEntry(entry);
        String sql = "UPDATE payroll_entries SET bonuses = ?, deductions = ?, net_salary = ? WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            entry.calculateNet(); // Recalculate net salary before saving

            pstmt.setBigDecimal(1, entry.getBonuses());
            pstmt.setBigDecimal(2, entry.getDeductions());
            pstmt.setBigDecimal(3, entry.getNetSalary());
            pstmt.setInt(4, entry.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    public List<PayrollEntry> getPayrollEntriesByMonthYear(int month, int year) {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries WHERE month = ? AND year = ? ORDER BY employee_id";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
        return list;
    }

    public boolean deletePayrollEntry(int id) {
        String sql = "DELETE FROM payroll_entries WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = connections.open();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw com.college.utils.ManagementException.database(e);
        }
    }

    private PayrollEntry mapResultSetToPayrollEntry(ResultSet rs) throws SQLException {
        PayrollEntry p = new PayrollEntry();
        p.setId(rs.getInt("id"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setMonth(rs.getInt("month"));
        p.setYear(rs.getInt("year"));
        p.setBasicSalary(rs.getBigDecimal("basic_salary"));
        p.setBonuses(rs.getBigDecimal("bonuses"));
        p.setDeductions(rs.getBigDecimal("deductions"));
        p.setNetSalary(rs.getBigDecimal("net_salary"));
        p.setStatus(PayrollEntry.Status.valueOf(rs.getString("status")));
        Date d = rs.getDate("payment_date");
        if (d != null)
            p.setPaymentDate(d.toLocalDate());
        return p;
    }
}
