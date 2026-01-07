package com.college.dao;

import com.college.models.PayrollEntry;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class PayrollDAO {

    public boolean createPayrollEntry(PayrollEntry entry) {
        String sql = "INSERT INTO payroll_entries (employee_id, month, year, basic_salary, bonuses, deductions, net_salary, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            entry.calculateNet(); // Ensure net is updated

            pstmt.setInt(1, entry.getEmployeeId());
            pstmt.setInt(2, entry.getMonth());
            pstmt.setInt(3, entry.getYear());
            pstmt.setBigDecimal(4, entry.getBasicSalary());
            pstmt.setBigDecimal(5, entry.getBonuses());
            pstmt.setBigDecimal(6, entry.getDeductions());
            pstmt.setBigDecimal(7, entry.getNetSalary());
            pstmt.setString(8, entry.getStatus().name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<PayrollEntry> getHistoryByEmployee(int employeeId) {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries WHERE employee_id = ? ORDER BY year DESC, month DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public boolean markAsPaid(int payrollId) {
        String sql = "UPDATE payroll_entries SET status = 'PAID', payment_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, payrollId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean markMonthAsPaid(int month, int year) {
        String sql = "UPDATE payroll_entries SET status = 'PAID', payment_date = ? WHERE month = ? AND year = ? AND status != 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            return pstmt.executeUpdate() >= 0; // Return true even if 0 rows updated (no error)
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<PayrollEntry> getAllPayrollEntries() {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries ORDER BY year DESC, month DESC, employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public boolean updatePayrollEntry(PayrollEntry entry) {
        String sql = "UPDATE payroll_entries SET bonuses = ?, deductions = ?, net_salary = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            entry.calculateNet(); // Recalculate net salary before saving

            pstmt.setBigDecimal(1, entry.getBonuses());
            pstmt.setBigDecimal(2, entry.getDeductions());
            pstmt.setBigDecimal(3, entry.getNetSalary());
            pstmt.setString(4, entry.getStatus().name());
            pstmt.setInt(5, entry.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update payroll entry", e);
            return false;
        }
    }

    public List<PayrollEntry> getPayrollEntriesByMonthYear(int month, int year) {
        List<PayrollEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll_entries WHERE month = ? AND year = ? ORDER BY employee_id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PayrollEntry p = mapResultSetToPayrollEntry(rs);
                list.add(p);
            }
        } catch (SQLException e) {
            Logger.error("Database operation failed", e);
        }
        return list;
    }

    public boolean deletePayrollEntry(int id) {
        String sql = "DELETE FROM payroll_entries WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete payroll entry", e);
            return false;
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
