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
}
